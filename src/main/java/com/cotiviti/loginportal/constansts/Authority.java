package com.cotiviti.loginportal.constansts;

public class Authority {
    public static final String[] USER_AUTHORITITES = {"user:read"};
    public static final String[] HR_AUTHORITITES = {"user:read","user:update"};
    public static final String[] MANAGER_AUTHORITITES = {"user:read","user:update"};
    public static final String[] ADMIN_AUTHORITITES = {"user:read","read:create","user:update"};
    public static final String[] SUPER_ADMIN_AUTHORITITES = {"user:read","user:create","user:update","user:delete"};
}
