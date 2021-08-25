package com.cotiviti.loginportal.service.impl;

import com.cotiviti.loginportal.domain.User;
import com.cotiviti.loginportal.enumeration.Role;
import com.cotiviti.loginportal.exception.domain.EmailExitsException;
import com.cotiviti.loginportal.exception.domain.EmailNotFoundException;
import com.cotiviti.loginportal.exception.domain.UserExitsException;
import com.cotiviti.loginportal.exception.domain.UserNotFoundException;
import com.cotiviti.loginportal.repository.UserRepository;
import com.cotiviti.loginportal.security.UserPriniciple;
import com.cotiviti.loginportal.service.EmailService;
import com.cotiviti.loginportal.service.LoginAttemptService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.mail.MessagingException;
import javax.persistence.AssociationOverride;
import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.cotiviti.loginportal.constansts.FIleConstant.*;
import static com.cotiviti.loginportal.constansts.UserImplConstants.*;
import static com.cotiviti.loginportal.enumeration.Role.ROLE_USER;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
@Transactional
@Qualifier("UserDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService {

    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private LoginAttemptService loginAttemptService;
    private EmailService emailService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,BCryptPasswordEncoder passwordEncoder,
                           LoginAttemptService loginAttemptService,EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService =loginAttemptService;
        this.emailService = emailService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username);
        if(user == null){
            LOGGER.error(USER_NOT_FOUND +username);
            throw new UsernameNotFoundException(USER_NOT_FOUND +username);
        } else {
            validateLoginAttempt(user);
            user.setLastLogInDateDisplay(user.getLastLogInDate());
            user.setLastLogInDate(new Date());
            userRepository.save(user);
            UserPriniciple userPriniciple = new UserPriniciple(user);
            LOGGER.info(RETURINING_USER_BY_USER +username);
            return userPriniciple;

        }
    }

    private void validateLoginAttempt(User user) {
        if(user.isUnlocked()){
            if(loginAttemptService.hasExceedMaxAttemps(user.getUsername())){
                user.setUnlocked(false);
            }else{
                user.setUnlocked(true);
            }
        }else{
            loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
        }
    }

    @Override
    public User register(String firstName, String lastName, String userName, String email) throws UserNotFoundException, UserExitsException, EmailExitsException, MessagingException {
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
        user.setPrifileImageUrl(getTemporaryProfileImg(userName));
        userRepository.save(user);
        emailService.sendNewPasswordEmail(firstName,password,email);
        LOGGER.info("New User Password :" +password);
        return user;
    }

    private String getTemporaryProfileImg(String username) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH +username).toUriString();
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

    @Override
    public User addNewUser(String firstName, String lastName, String username, String email, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UserExitsException, EmailExitsException, IOException {
        validateNewUsernameAndEmail(StringUtils.EMPTY,username,email);
        User user = new User();
        user.setUserId(generateUserId());
        String password = generatepassword();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encodePassword(password));
        user.setJoinDate(new Date());
        user.setActive(isActive);
        user.setUnlocked(isNonLocked);
        user.setRoles(getRoleEnumName(role).name());
        user.setAuthorities(getRoleEnumName(role).getAuthorities());
        user.setPrifileImageUrl(getTemporaryProfileImg(username));
        userRepository.save(user);
        saveProfileImage(user,profileImage);
        return user;

    }

    private void saveProfileImage(User user, MultipartFile profileImage) throws IOException {
        if(profileImage != null){
            Path userFolder = Paths.get(USER_FOLDER + user.getUsername()).toAbsolutePath().normalize();
            if(!Files.exists(userFolder)){
                Files.createDirectories(userFolder);
                LOGGER.info(DIRECTORY_CREATED +userFolder);
            }
            Files.deleteIfExists(Paths.get(userFolder + user.getUsername() + DOT + JPG_EXTENSION));
            Files.copy(profileImage.getInputStream(), userFolder.resolve(user.getUsername() +DOT + JPG_EXTENSION), REPLACE_EXISTING);
            user.setPrifileImageUrl(setsProfileImageUrl(user.getUsername()));
            userRepository.save(user);
            LOGGER.info(FILE_SAVED_IN_FILE_SYSTEM + profileImage.getOriginalFilename());
        }
    }

    private String setsProfileImageUrl(String username) {
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path(USER_IMAGE_PATH + username + FORWARD_SLASH +username + DOT + JPG_EXTENSION)
                .toUriString();
    }

    private Role getRoleEnumName(String role) {
        return Role.valueOf(role.toUpperCase());
    }

    @Override
    public User updateUser(String currentUsername, String newfirstName, String newlastName, String newUsername, String newEmail, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UserExitsException, EmailExitsException, IOException {
        User createUser = validateNewUsernameAndEmail(currentUsername,newUsername,newEmail);
        createUser.setFirstName(newfirstName);
        createUser.setLastName(newlastName);
        createUser.setUsername(newUsername);
        createUser.setEmail(newEmail);
        createUser.setActive(isActive);
        createUser.setUnlocked(isNonLocked);
        createUser.setRoles(getRoleEnumName(role).name());
        createUser.setAuthorities(getRoleEnumName(role).getAuthorities());
        userRepository.save(createUser);
        saveProfileImage(createUser,profileImage);
        return createUser;
    }

    @Override
    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }

    @Override
    public void resetPassword(String email) throws EmailNotFoundException, MessagingException {
        User user = userRepository.findUserByEmail(email);
        if(user == null){
            throw new EmailNotFoundException(NO_USER_FOUND_BY_EMAIL + email);
        }
        String password = generatepassword();
        user.setPassword(encodePassword(password));
        userRepository.save(user);
        emailService.sendNewPasswordEmail(user.getFirstName(),password,user.getEmail());

    }

    @Override
    public User updateProfileImage(String username, MultipartFile profileImage) throws UserNotFoundException, UserExitsException, EmailExitsException, IOException {
        User user = validateNewUsernameAndEmail(username,null,null);
        saveProfileImage(user,profileImage);
        return user;
    }
}
