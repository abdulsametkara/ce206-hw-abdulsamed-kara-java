package com.samet.music.model;

/**
 * User model class
 */
public class User extends BaseEntity {
    private String username;
    private String password;

    /**
     * Constructor
     */
    public User(String username, String password) {
        super(username);
        this.username = username;
        this.password = password;
    }

    /**
     * Constructor with ID
     */
    public User(String id, String username, String password) {
        super(username);
        setId(id);
        this.username = username;
        this.password = password;
    }

    /**
     * Gets the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password
     */
    public void setPassword(String password) {
        this.password = password;
    }
} 