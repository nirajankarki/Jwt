package com.cotiviti.loginportal.listner;

import com.cotiviti.loginportal.service.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;


@Component
public class AuthenticationFailureListner {
    private LoginAttemptService loginAttemptService;
    @Autowired
    public AuthenticationFailureListner(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }
    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        Object principle = event.getAuthentication().getPrincipal();
        if(principle instanceof String){
            String username = (String) event.getAuthentication().getPrincipal();
            loginAttemptService.addUserToLoginAttemptCache(username);
        }
    }
}
