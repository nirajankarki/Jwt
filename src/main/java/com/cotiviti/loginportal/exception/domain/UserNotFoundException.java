package com.cotiviti.loginportal.exception.domain;

public class UserNotFoundException extends Exception{
    public UserNotFoundException(String messagge){
        super(messagge);
    }
}
