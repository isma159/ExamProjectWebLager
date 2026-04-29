package ScanHub.BE;

import java.util.List;

public class User {

    private int userId;
    private String username;
    private String passwordHash;
    private Role role;
    private List<Profile> profiles;

    public User(int userId, String username, String passwordHash, Role role, List<Profile> profiles) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.profiles = profiles;
    }

    public User(String username, String passwordHash, Role role, List<Profile> profiles) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.profiles = profiles;
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
}
