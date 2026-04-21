package ScanHub.DAL.interfaces;

public interface IPasswordEncrypter {
    String hashedPassword(String password);
    boolean verifyPassword(String password, String hashedPassword);
}