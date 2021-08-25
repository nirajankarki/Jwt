package com.cotiviti.loginportal.listner;

import com.cotiviti.loginportal.domain.User;
import com.cotiviti.loginportal.domain.UserPrinciple;
import com.cotiviti.loginportal.service.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSuccessListner {
    private LoginAttemptService loginAttemptService;
    @Autowired
    public AuthenticationSuccessListner(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event){
        Object principle = event.getAuthentication().getPrincipal();
        if(principle instanceof UserPrinciple){
            UserPrinciple user = (UserPrinciple) event.getAuthentication().getPrincipal();
            loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
        }
    }
}
