package ScanHub.BE;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User {

    private int userId;
    private String username;
    private String passwordHash;
    private Role role;
    private List<Profile> profiles;

    public User(int userId, String username, String passwordHash, Role role) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.profiles = new ArrayList<>();
    }

    public User(String username, String passwordHash, Role role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.profiles = new ArrayList<>();
    }

    public int getUserId() {
        return userId;
    }
    public String getUsername() {
        return username;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public Role getRole() {
        return role;
    }
    public List<Profile> getProfiles() {return profiles;}

    public void setUserId(int userId) {
        this.userId = userId;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    public void setRole(Role role) {
        this.role = role;
    }
    public void setProfiles(List<Profile> profiles) {this.profiles = profiles;}

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    @Override
    public String toString() {
        return this.username;
    }

    @Override
    public int hashCode() {
        return Objects.hash("User_" + this.userId);
    }

    @Override
    public boolean equals(Object o) {
        // if this (object) is equal to the object given as argument, then return true
        if (this == o) return true;
        // if it's not an instance of Profile class, then it's not a profile and returns false
        if (!(o instanceof User)) return false;
        // if all else fails, converts given object to profile and compares with equals method from Objects class
        User other = (User) o;
        return Objects.equals(this.userId, other.userId);
    }
}
