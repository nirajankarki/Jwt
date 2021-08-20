package com.cotiviti.loginportal.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
@Entity
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false,updatable = false)
    private long id;
    private String userId;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String email;
    private String prifileImageUrl;
    private Date lastLogInDate;
    private Date lastLogInDateDisplay;
    private Date joinDate;
    private String role;
    private String[] authorities;
    private boolean active;
    private boolean unlocked;

    public User() {
    }

    public User(long id, String userId, String firstName, String lastName, String username, String password, String email, String prifileImageUrl, Date lastLogInDate, Date lastLogInDateDisplay, Date joinDate, String role, String[] authorities, boolean active, boolean unlocked) {
        this.id = id;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.email = email;
        this.prifileImageUrl = prifileImageUrl;
        this.lastLogInDate = lastLogInDate;
        this.lastLogInDateDisplay = lastLogInDateDisplay;
        this.joinDate = joinDate;
        this.role = role;
        this.authorities = authorities;
        this.active = active;
        this.unlocked = unlocked;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPrifileImageUrl() {
        return prifileImageUrl;
    }

    public void setPrifileImageUrl(String prifileImageUrl) {
        this.prifileImageUrl = prifileImageUrl;
    }

    public Date getLastLogInDate() {
        return lastLogInDate;
    }

    public void setLastLogInDate(Date lastLogInDate) {
        this.lastLogInDate = lastLogInDate;
    }

    public Date getLastLogInDateDisplay() {
        return lastLogInDateDisplay;
    }

    public void setLastLogInDateDisplay(Date lastLogInDateDisplay) {
        this.lastLogInDateDisplay = lastLogInDateDisplay;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    public String getRole() {
        return role;
    }

    public void setRoles(String role) {
        this.role = role;
    }

    public String[] getAuthorities() {
        return authorities;
    }

    public void setAuthorities(String[] authorities) {
        this.authorities = authorities;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
}
