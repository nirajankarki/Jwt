package com.cotiviti.loginportal.service;

import com.cotiviti.loginportal.domain.User;
import com.cotiviti.loginportal.exception.domain.EmailExitsException;
import com.cotiviti.loginportal.exception.domain.EmailNotFoundException;
import com.cotiviti.loginportal.exception.domain.UserExitsException;
import com.cotiviti.loginportal.exception.domain.UserNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

public interface UserService {
    User register(String firstName,String lastName,String userName,String email) throws UserNotFoundException, UserExitsException, EmailExitsException, MessagingException;
    List<User> getUsers();
    User findUserByUsername(String userName);
    User findUserByEmail(String email);
    User addNewUser(String firstName, String lastName, String username, String email, String role , boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UserExitsException, EmailExitsException, IOException;
    User updateUser(String currentUsername, String newfirstName, String newlastName, String newUsername, String newEmail, String role , boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UserExitsException, EmailExitsException, IOException;
    void deleteUser(long id);
    void resetPassword(String email) throws EmailNotFoundException, MessagingException;
    User updateProfileImage(String user, MultipartFile profileImage) throws UserNotFoundException, UserExitsException, EmailExitsException, IOException;
}
