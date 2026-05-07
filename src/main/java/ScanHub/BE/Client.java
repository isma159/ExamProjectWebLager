package ScanHub.BE;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Client {

    private int clientId;
    private String clientName;
    private List<Profile> profiles;
    private List<User> users;

    public Client() {
        this.profiles = new ArrayList<>();
        this.users = new ArrayList<>();
    }

    public Client(int clientId, String clientName) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.profiles = new ArrayList<>();
        this.users = new ArrayList<>();
    }

    public Client(String clientName) {
        this.clientName = clientName;
        this.profiles = new ArrayList<>();
        this.users = new ArrayList<>();
    }

    public int getClientId() { return clientId; }
    public String getClientName() { return clientName; }
    public List<Profile> getProfiles() { return profiles; }
    public List<User> getUsers() { return users; }

    public void setClientId(int clientId) { this.clientId = clientId; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public void setProfiles(List<Profile> profiles) { this.profiles = profiles; }
    public void setUsers(List<User> users) { this.users = users; }

    @Override
    public String toString() { return this.clientName; }

    @Override
    public int hashCode() { return Objects.hash("Client_" + this.clientId); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Client)) return false;
        Client other = (Client) o;
        return Objects.equals(this.clientId, other.clientId);
    }
}