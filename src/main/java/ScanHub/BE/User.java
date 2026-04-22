package ScanHub.BE;

public class User {

    private int id;
    private String username;
    private String fullName;
    private String password;
    private Roles role;

    public User(int id, String username, String fullName, String password, Roles role) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.password = password;
        this.role = role;
    }

    public User(String username, String fullName, String password, Roles role) {
        this.username = username;
        this.fullName = fullName;
        this.password = password;
        this.role = role;
    }

    public int getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }
    public String getFullName() {
        return fullName;
    }
    public String getPassword() {
        return password;
    }
    public Roles getRole() {
        return role;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setRole(Roles role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return this.fullName + "|" + this.username;
    }
}
