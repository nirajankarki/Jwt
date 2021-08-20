package com.cotiviti.loginportal.service.impl;

import com.cotiviti.loginportal.domain.User;
import com.cotiviti.loginportal.exception.domain.EmailExitsException;
import com.cotiviti.loginportal.exception.domain.UserExitsException;
import com.cotiviti.loginportal.exception.domain.UserNotFoundException;
import com.cotiviti.loginportal.repository.UserRepository;
import com.cotiviti.loginportal.security.UserPriniciple;
import com.cotiviti.loginportal.service.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

import static com.cotiviti.loginportal.constansts.UserImplConstants.*;
import static com.cotiviti.loginportal.enumeration.Role.ROLE_USER;

@Service
@Transactional
@Qualifier("UserDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService {

    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username);
        if(user == null){
            LOGGER.error(USER_NOT_FOUND +username);
            throw new UsernameNotFoundException(USER_NOT_FOUND +username);
        } else {
            user.setLastLogInDateDisplay(user.getLastLogInDate());
            user.setLastLogInDate(new Date());
            userRepository.save(user);
            UserPriniciple userPriniciple = new UserPriniciple(user);
            LOGGER.info(RETURINING_USER_BY_USER +username);
            return userPriniciple;

        }
    }

    @Override
    public User register(String firstName, String lastName, String userName, String email) throws UserNotFoundException, UserExitsException, EmailExitsException {
        validateNewUsernameAndEmail(StringUtils.EMPTY,userName,email);
        User user = new User();
        user.setUserId(generateUserId());
        String password = generatepassword();
        String encodedPassword= encodePassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(userName);
        user.setEmail(email);
        user.setJoinDate(new Date());
        user.setPassword(encodedPassword);
        user.setActive(true);
        user.setUnlocked(true);
        user.setRoles(ROLE_USER.name());
        user.setAuthorities(ROLE_USER.getAuthorities());
        user.setPrifileImageUrl(getTemporaryProfileImg());
        userRepository.save(user);
        LOGGER.info("New User Password :" +password);
        return user;
    }

    private String getTemporaryProfileImg() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH).toUriString();
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private String generatepassword() {
        return RandomStringUtils.randomAlphanumeric(10);
    }

    private String generateUserId() {
        return RandomStringUtils.randomNumeric(10);
    }

    private User validateNewUsernameAndEmail(String currentUsername,String newUsername,String email) throws UserNotFoundException, UserExitsException, EmailExitsException {
        User userByEmail = findUserByEmail(email);
        User userByUsername = findUserByUsername(newUsername);
        if(StringUtils.isNotBlank(currentUsername)){
            User currrentUser = findUserByUsername(currentUsername);
            if(currrentUser == null){
                throw new UserNotFoundException(USER_NOT_FOUND);
            }
            if(userByUsername != null && currrentUser.getId()!= userByUsername.getId()){
                throw new UserExitsException(USER_EXISTS);
            }
            if(userByEmail != null && currrentUser.getId()!= userByEmail.getId()){
                throw new EmailExitsException(EMAIL_EXISTS);
            }
            return currrentUser;
        }else {
            if(userByUsername != null){
                throw new UserExitsException(USER_EXISTS);
            }
            if(userByEmail != null){
                throw new EmailExitsException(EMAIL_EXISTS);
            }
            return null;
        }
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public User findUserByUsername(String userName) {
        return userRepository.findUserByUsername(userName);
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }
}
