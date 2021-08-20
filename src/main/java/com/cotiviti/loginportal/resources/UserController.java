package com.cotiviti.loginportal.resources;

import com.cotiviti.loginportal.domain.User;
import com.cotiviti.loginportal.exception.domain.EmailExitsException;
import com.cotiviti.loginportal.exception.domain.ExceptionHandling;
import com.cotiviti.loginportal.exception.domain.UserExitsException;
import com.cotiviti.loginportal.exception.domain.UserNotFoundException;
import com.cotiviti.loginportal.security.UserPriniciple;
import com.cotiviti.loginportal.service.UserService;
import com.cotiviti.loginportal.utility.JWTTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import static com.cotiviti.loginportal.constansts.SecurityContstant.JWT_TOKEN_HEADER;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(path = {"/","/user"})
public class UserController extends ExceptionHandling {
    private UserService userService;
    private AuthenticationManager authenticationManager;
    private JWTTokenProvider jwtTokenProvider;
    @Autowired
    public UserController(UserService userService, AuthenticationManager authenticationManager, JWTTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User user) {
        authentication(user.getUsername(),user.getPassword());
        User loginUser = userService.findUserByUsername(user.getUsername());
        UserPriniciple userPriniciple = new UserPriniciple(loginUser);
        HttpHeaders jwtHeader = getJwtHeader(userPriniciple);
        return new ResponseEntity<>(loginUser,jwtHeader, OK);
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) throws UserNotFoundException, UserExitsException, EmailExitsException {
        User newUser = userService.register(user.getFirstName(),user.getLastName(),user.getUsername(),user.getEmail());
        return new ResponseEntity<>(newUser, OK);
    }
    private HttpHeaders getJwtHeader(UserPriniciple userPriniciple) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JWT_TOKEN_HEADER,jwtTokenProvider.genertateJwtToken(userPriniciple) );
        return httpHeaders;
    }

    private void authentication(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username,password));
    }
}
