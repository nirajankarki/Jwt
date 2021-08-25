package com.cotiviti.loginportal.resources;

import com.cotiviti.loginportal.domain.HttpResponse;
import com.cotiviti.loginportal.domain.User;
import com.cotiviti.loginportal.exception.domain.*;
import com.cotiviti.loginportal.security.UserPriniciple;
import com.cotiviti.loginportal.service.UserService;
import com.cotiviti.loginportal.utility.JWTTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.cotiviti.loginportal.constansts.FIleConstant.*;
import static com.cotiviti.loginportal.constansts.SecurityContstant.JWT_TOKEN_HEADER;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;

@RestController
@RequestMapping(path = {"/","/user"})
public class UserController extends ExceptionHandling {
    public static final String Email_SENT = "An email with the new password is sent :";
    public static final String USER_DELETED_SUCCESSFULLY = "User deleted Successfully ";
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
    public ResponseEntity<User> register(@RequestBody User user) throws UserNotFoundException, UserExitsException, EmailExitsException, MessagingException {
        User newUser = userService.register(user.getFirstName(),user.getLastName(),user.getUsername(),user.getEmail());
        return new ResponseEntity<>(newUser, OK);
    }

    @PostMapping("/add")
    public ResponseEntity<User> addNewUser(@RequestParam("firstName") String firstName,
                                           @RequestParam("lastName") String lastName,
                                           @RequestParam("username") String username,
                                           @RequestParam("email") String email,
                                           @RequestParam("role") String role,
                                           @RequestParam("isActive") String isActive,
                                           @RequestParam("isNonLocked") String isNonLocked,
                                           @RequestParam(value = "profileImage",required = false) MultipartFile profileImage) throws UserNotFoundException, UserExitsException, IOException, EmailExitsException {
        User addNewuser = userService.addNewUser(firstName,lastName,username,email,role,Boolean.parseBoolean(isNonLocked),Boolean.parseBoolean(isActive),profileImage);
        return new ResponseEntity<>(addNewuser,OK);
    }

    @PostMapping("/update")
    public ResponseEntity<User> updaetUser(@RequestParam("currentUsername") String currentUsername,
                                           @RequestParam("firstName") String firstName,
                                           @RequestParam("lastName") String lastName,
                                           @RequestParam("username") String username,
                                           @RequestParam("email") String email,
                                           @RequestParam("role") String role,
                                           @RequestParam("isActive") String isActive,
                                           @RequestParam("isNonLocked") String isNonLocked,
                                           @RequestParam(value = "profileImage",required = false) MultipartFile profileImage) throws UserNotFoundException, UserExitsException, IOException, EmailExitsException {
        User updateUser = userService.updateUser(currentUsername,firstName,lastName,username,email,role,Boolean.parseBoolean(isNonLocked),Boolean.parseBoolean(isActive),profileImage);
        return new ResponseEntity<>(updateUser,OK);
    }

    @GetMapping("/find/{username}")
    public ResponseEntity<User> findUser(@PathVariable("username") String username){
        User user = userService.findUserByUsername(username);
        return new ResponseEntity<>(user,OK);
    }
    @GetMapping("/list")
    public ResponseEntity<List<User>> getAll(){
        List<User> users = userService.getUsers();
        return new ResponseEntity<>(users,OK);
    }

    @GetMapping("resetPassword/{email}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email) throws EmailNotFoundException, MessagingException {
        userService.resetPassword(email);
        return response(OK, Email_SENT +email);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('user:delete')")
    public ResponseEntity<HttpResponse> delete(@PathVariable("id") long id){
        userService.deleteUser(id);
        return response(NO_CONTENT, USER_DELETED_SUCCESSFULLY);
    }

    @PostMapping("/updateProfileImage")
    public ResponseEntity<User> updateProfileImage(@RequestParam("firstName") String firstName, @RequestParam(value = "profileImage") MultipartFile profileImage) throws UserNotFoundException, UserExitsException, IOException, EmailExitsException {
        User updateProfile = userService.updateProfileImage(firstName,profileImage);
        return new ResponseEntity<>(updateProfile, OK);
    }
    @GetMapping(path = "/image/{username}/{fileName}",produces = IMAGE_JPEG_VALUE)
    public byte[] getProfileImage(@PathVariable("username") String username, @PathVariable("fileName") String fileName) throws IOException {
        return Files.readAllBytes(Paths.get(USER_FOLDER + username + FORWARD_SLASH + fileName));
    }

    @GetMapping(path = "/image/profile/{username}",produces = IMAGE_JPEG_VALUE)
    public byte[] getTempProfileImage(@PathVariable("username") String username) throws IOException {
        URL url = new URL(TEMP_PROFILE_IMAGE_BASE_URL +username);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try(InputStream inputStream = url.openStream()){
            int bytesRead;
            byte[] chunk = new byte[1024];
            while((bytesRead = inputStream.read(chunk)) > 0){
                byteArrayOutputStream.write(chunk, 0, bytesRead);
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    private ResponseEntity<HttpResponse> response(HttpStatus status, String message) {
        return new ResponseEntity<>(new HttpResponse(status.value(),status,status.getReasonPhrase(),message),status);
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
