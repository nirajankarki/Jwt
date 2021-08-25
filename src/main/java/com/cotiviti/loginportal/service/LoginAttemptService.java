package com.cotiviti.loginportal.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MINUTES;

@Service
public class LoginAttemptService {
    public static final int MAX_NUM_OF_ATTEMPT = 5;
    public static final int ATTEMPT_INCREMENT = 1;
    private LoadingCache<String,Integer> loginAttempCache;

    public LoginAttemptService() {
        super();
        loginAttempCache = CacheBuilder.newBuilder().expireAfterWrite(15, MINUTES)
                .maximumSize(100).build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String key) throws Exception {
                        return 0;
                    }
                });
    }
    public void evictUserFromLoginAttemptCache(String username){
        loginAttempCache.invalidate(username);
    }
    public void addUserToLoginAttemptCache(String username) {
        int atttempts = 0;
        try {
            atttempts = ATTEMPT_INCREMENT + loginAttempCache.get(username);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        loginAttempCache.put(username,atttempts);
    }
    public boolean hasExceedMaxAttemps(String username) {
        try {
            return loginAttempCache.get(username)>= MAX_NUM_OF_ATTEMPT;
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }
}
