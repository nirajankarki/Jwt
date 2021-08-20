package com.cotiviti.loginportal.enumeration;

import com.cotiviti.loginportal.constansts.Authority;

import static com.cotiviti.loginportal.constansts.Authority.*;

public enum  Role {
    ROLE_USER(USER_AUTHORITITES),
    ROLE_HR(HR_AUTHORITITES),
    ROLE_MANAGER(MANAGER_AUTHORITITES),
    ROLE_ADMIN(ADMIN_AUTHORITITES),
    ROLE_SUPER_ADMIN(SUPER_ADMIN_AUTHORITITES);

    private String[] authorities;

    Role(String ... authorities){
        this.authorities = authorities;
    }
    public String[] getAuthorities(){
        return authorities;
    }
}
