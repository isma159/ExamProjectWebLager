package ScanHub.BLL;

// project imports
import ScanHub.BE.User;
import ScanHub.BE.enums.Role;
import ScanHub.BLL.util.PasswordEncrypter;

// library imports
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoginTest {

    private SessionManager sessionManager;
    private PasswordEncrypter encrypter;

    private User adminUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        sessionManager = new SessionManager();
        encrypter = new PasswordEncrypter();

        // create hashed passwords
        String adminHash = encrypter.hashedPassword("admin123");
        String userHash = encrypter.hashedPassword("user123");

        adminUser = new User(1, "admin", adminHash, Role.ADMIN);
        regularUser = new User(2, "user", userHash, Role.USER);
    }


    // password verification
    @Test
    void correctPassword_shouldReturnTrue() {
        assertTrue(encrypter.verifyPassword("admin123", adminUser.getPasswordHash()));
    }

    @Test
    void wrongPassword_shouldReturnFalse() {
        assertFalse(encrypter.verifyPassword("wrongPassword", adminUser.getPasswordHash()));
    }

    @Test
    void emptyPassword_shouldReturnFalse() {
        assertFalse(encrypter.verifyPassword("", adminUser.getPasswordHash()));
    }

    @Test
    void correctPassword_forUser_shouldReturnTrue() {
        assertTrue(encrypter.verifyPassword("user123", regularUser.getPasswordHash()));
    }


    // session login
    @Test
    void login_withAdminUser_shouldSucceed() {
        assertTrue(sessionManager.login(adminUser));
        assertEquals(adminUser, sessionManager.getCurrentUser());
    }

    @Test
    void login_withRegularUser_shouldSucceed() {
        assertTrue(sessionManager.login(regularUser));
        assertEquals(regularUser, sessionManager.getCurrentUser());
    }

    @Test
    void logout_shouldClearSession() {
        sessionManager.login(adminUser);
        sessionManager.logout();
        assertNull(sessionManager.getCurrentUser());
    }

    @Test
    void logout_thenLoginAgain_shouldSucceed() {
        sessionManager.login(adminUser);
        sessionManager.logout();
        assertTrue(sessionManager.login(regularUser));
        assertEquals(regularUser, sessionManager.getCurrentUser());
    }


    // role checking
    @Test
    void adminUser_isAdmin_shouldReturnTrue() {
        assertTrue(adminUser.isAdmin());
    }

    @Test
    void regularUser_isAdmin_shouldReturnFalse() {
        assertFalse(regularUser.isAdmin());
    }

    @Test
    void adminUser_role_shouldBeAdmin() {
        assertEquals(Role.ADMIN, adminUser.getRole());
    }

    @Test
    void regularUser_role_shouldBeUser() {
        assertEquals(Role.USER, regularUser.getRole());
    }
}