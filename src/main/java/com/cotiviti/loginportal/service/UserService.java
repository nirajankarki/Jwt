package com.cotiviti.loginportal.service;

import com.cotiviti.loginportal.domain.User;
import com.cotiviti.loginportal.exception.domain.EmailExitsException;
import com.cotiviti.loginportal.exception.domain.UserExitsException;
import com.cotiviti.loginportal.exception.domain.UserNotFoundException;

import java.util.List;

public interface UserService {
    User register(String firstName,String lastName,String userName,String email) throws UserNotFoundException, UserExitsException, EmailExitsException;
    List<User> getUsers();
    User findUserByUsername(String userName);
    User findUserByEmail(String email);
}
