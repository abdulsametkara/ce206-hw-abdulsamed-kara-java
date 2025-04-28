package com.samet.music.view;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Scanner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.samet.music.controller.UserController;
import com.samet.music.model.User;

/**
 * UserSettingsView için test sınıfı
 */
public class UserSettingsViewTest {
    
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    
    private UserSettingsView userSettingsView;
    private MockUserController userController;
    private User testUser;
    
    @Before
    public void setUp() {
        // Standart output akışını yakalama
        originalOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        
        // Test kullanıcısı oluşturma
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setCreatedAt(LocalDateTime.now());
        
        // Mock controller oluşturma
        userController = new MockUserController();
        
        // Kullanıcı oturumunu ayarlama
        userController.setLoggedIn(true);
        userController.setCurrentUser(testUser);
    }
    
    @After
    public void tearDown() {
        // Standart output akışını geri yükleme
        System.setOut(originalOut);
    }
    
    /**
     * Oturum açılmamış durumu test eder
     */
    @Test
    public void testNotLoggedIn() {
        // Kullanıcı oturumunu kapatma
        userController.setLoggedIn(false);
        
        // Test için scanner oluşturma
        Scanner scanner = new Scanner("");
        
        // View oluşturma
        userSettingsView = new UserSettingsView(scanner, userController);
        
        // Görünümü gösterme
        MenuView resultView = userSettingsView.display();
        
        // LoginMenuView'a yönlendirildiğini doğrulama
        assertTrue("Oturum açılmamışsa LoginMenuView'a yönlendirilmeli", resultView instanceof LoginMenuView);
    }
    
    /**
     * Kullanıcı profilinin doğru gösterilmesini test eder
     */
    @Test
    public void testDisplayUserProfile() {
        // Ana menüye dönüş
        String input = "0\n";
        Scanner scanner = new Scanner(input);
        
        // View oluşturma
        userSettingsView = new UserSettingsView(scanner, userController);
        
        // Görünümü gösterme
        userSettingsView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Kullanıcı adı gösterilmeli", output.contains("Username: testuser"));
        assertTrue("E-posta gösterilmeli", output.contains("Email: test@example.com"));
        assertTrue("Hesap oluşturma tarihi gösterilmeli", output.contains("Account created:"));
    }
    
    /**
     * Geçersiz menü seçimi test eder
     */
    @Test
    public void testInvalidMenuChoice() {
        // Geçersiz seçim, ana menüye dönüş
        String input = "99\n0\n";
        Scanner scanner = new Scanner(input);
        
        // View oluşturma
        userSettingsView = new UserSettingsView(scanner, userController);
        
        // Görünümü gösterme
        userSettingsView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Geçersiz seçim mesajı gösterilmeli", output.contains("Invalid choice"));
    }
    
    /**
     * Profil güncelleme işlemini test eder
     */
    @Test
    public void testUpdateProfile() {
        // Profil güncelle (1), yeni e-posta, ana menüye dönüş
        String input = "1\nnew@example.com\n0\n";
        Scanner scanner = new Scanner(input);
        
        // View oluşturma
        userSettingsView = new UserSettingsView(scanner, userController);
        
        // Görünümü gösterme
        userSettingsView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Profil güncelleme başlığı gösterilmeli", output.contains("UPDATE PROFILE"));
        assertTrue("Mevcut e-posta gösterilmeli", output.contains("Current email: test@example.com"));
        assertTrue("Başarı mesajı gösterilmeli", output.contains("Profile updated successfully"));
    }
    
    /**
     * Herhangi bir değişiklik yapmadan profil güncelleme işlemini test eder
     */
    @Test
    public void testUpdateProfileNoChanges() {
        // Profil güncelle (1), değişiklik yok, ana menüye dönüş
        String input = "1\n\n0\n";
        Scanner scanner = new Scanner(input);
        
        // View oluşturma
        userSettingsView = new UserSettingsView(scanner, userController);
        
        // Görünümü gösterme
        userSettingsView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Değişiklik yok mesajı gösterilmeli", output.contains("No changes made to profile"));
    }
    
    /**
     * Profil güncelleme başarısız olma durumunu test eder
     */
    @Test
    public void testUpdateProfileFailure() {
        // Profil güncelleme başarısız olacak
        userController.setUpdateProfileSuccess(false);
        
        // Profil güncelle (1), yeni e-posta, ana menüye dönüş
        String input = "1\nnew@example.com\n0\n";
        Scanner scanner = new Scanner(input);
        
        // View oluşturma
        userSettingsView = new UserSettingsView(scanner, userController);
        
        // Görünümü gösterme
        userSettingsView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Profil güncelleme hatası gösterilmeli", output.contains("Failed to update profile"));
    }
    
    /**
     * Şifre değiştirme işlemini test eder
     */
    @Test
    public void testChangePassword() {
        // Şifre değiştir (2), mevcut şifre, yeni şifre, onay, ana menüye dönüş
        String input = "2\npassword123\nnewpassword\nnewpassword\n0\n";
        Scanner scanner = new Scanner(input);
        
        // View oluşturma
        userSettingsView = new UserSettingsView(scanner, userController);
        
        // Görünümü gösterme
        userSettingsView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Şifre değiştirme başlığı gösterilmeli", output.contains("CHANGE PASSWORD"));
        assertTrue("Başarı mesajı gösterilmeli", output.contains("Password changed successfully"));
    }
    
    /**
     * Yanlış mevcut şifre ile şifre değiştirme işlemini test eder
     */
    @Test
    public void testChangePasswordWrongCurrentPassword() {
        // Şifre değiştir (2), yanlış mevcut şifre, ana menüye dönüş
        String input = "2\nwrongpassword\n0\n";
        Scanner scanner = new Scanner(input);
        
        // View oluşturma
        userSettingsView = new UserSettingsView(scanner, userController);
        
        // Görünümü gösterme
        userSettingsView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Yanlış şifre hatası gösterilmeli", output.contains("Current password is incorrect"));
    }
    
    /**
     * Boş yeni şifre ile şifre değiştirme işlemini test eder
     */
    @Test
    public void testChangePasswordEmptyNewPassword() {
        // Şifre değiştir (2), mevcut şifre, boş yeni şifre, ana menüye dönüş
        String input = "2\npassword123\n\n0\n";
        Scanner scanner = new Scanner(input);
        
        // View oluşturma
        userSettingsView = new UserSettingsView(scanner, userController);
        
        // Görünümü gösterme
        userSettingsView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Boş şifre uyarısı gösterilmeli", output.contains("Password change cancelled"));
    }
    
    /**
     * Eşleşmeyen şifreler ile şifre değiştirme işlemini test eder
     */
    @Test
    public void testChangePasswordMismatch() {
        // Şifre değiştir (2), mevcut şifre, yeni şifre, farklı onay şifresi, ana menüye dönüş
        String input = "2\npassword123\nnewpassword\ndifferentpassword\n0\n";
        Scanner scanner = new Scanner(input);
        
        // View oluşturma
        userSettingsView = new UserSettingsView(scanner, userController);
        
        // Görünümü gösterme
        userSettingsView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Şifre eşleşmeme hatası gösterilmeli", output.contains("Passwords do not match"));
    }
    
    /**
     * Şifre değiştirme başarısız olma durumunu test eder
     */
    @Test
    public void testChangePasswordFailure() {
        // Şifre değiştirme başarısız olacak
        userController.setUpdateProfileSuccess(false);
        
        // Şifre değiştir (2), mevcut şifre, yeni şifre, onay, ana menüye dönüş
        String input = "2\npassword123\nnewpassword\nnewpassword\n0\n";
        Scanner scanner = new Scanner(input);
        
        // View oluşturma
        userSettingsView = new UserSettingsView(scanner, userController);
        
        // Görünümü gösterme
        userSettingsView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Şifre değiştirme hatası gösterilmeli", output.contains("Failed to change password"));
    }
    
    /**
     * Hesap silme işlemini test eder
     */
    @Test
    public void testDeleteAccount() {
        // Hesap sil (3), onaylama (y), doğru şifre, ana menüye dönüş
        String input = "3\ny\npassword123\n0\n";
        Scanner scanner = new Scanner(input);
        
        // View oluşturma
        userSettingsView = new UserSettingsView(scanner, userController);
        
        // Görünümü gösterme
        MenuView resultView = userSettingsView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Hesap silme başlığı gösterilmeli", output.contains("DELETE ACCOUNT"));
        assertTrue("Uyarı mesajı gösterilmeli", output.contains("WARNING"));
        assertTrue("Onay mesajı gösterilmeli", output.contains("Are you sure"));
        assertTrue("Başarı mesajı gösterilmeli", output.contains("Your account has been deleted"));
        
        // LoginMenuView'a yönlendirildiğini doğrulama
        assertTrue("Hesap silindiğinde LoginMenuView'a yönlendirilmeli", resultView instanceof LoginMenuView);
    }
    
    /**
     * Hesap silme iptal işlemini test eder
     */
    @Test
    public void testDeleteAccountCancel() {
        // Hesap sil (3), iptal (n), ana menüye dönüş
        String input = "3\nn\n0\n";
        Scanner scanner = new Scanner(input);
        
        // View oluşturma
        userSettingsView = new UserSettingsView(scanner, userController);
        
        // Görünümü gösterme
        userSettingsView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("İptal mesajı gösterilmeli", output.contains("Account deletion cancelled"));
    }
    
    /**
     * Yanlış şifre ile hesap silme işlemini test eder
     */
    @Test
    public void testDeleteAccountWrongPassword() {
        // Hesap sil (3), onaylama (y), yanlış şifre, ana menüye dönüş
        String input = "3\ny\nwrongpassword\n0\n";
        Scanner scanner = new Scanner(input);
        
        // View oluşturma
        userSettingsView = new UserSettingsView(scanner, userController);
        
        // Görünümü gösterme
        userSettingsView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Yanlış şifre hatası gösterilmeli", output.contains("Password is incorrect"));
    }
    
    /**
     * Hesap silme başarısız olma durumunu test eder
     */
    @Test
    public void testDeleteAccountFailure() {
        // Hesap silme başarısız olacak
        userController.setDeleteAccountSuccess(false);
        
        // Hesap sil (3), onaylama (y), doğru şifre, ana menüye dönüş
        String input = "3\ny\npassword123\n0\n";
        Scanner scanner = new Scanner(input);
        
        // View oluşturma
        userSettingsView = new UserSettingsView(scanner, userController);
        
        // Görünümü gösterme
        userSettingsView.display();
        
        // Çıktıyı doğrulama
        String output = outputStream.toString();
        assertTrue("Hesap silme hatası gösterilmeli", output.contains("Failed to delete account"));
    }
    
    /**
     * Ana menüye dönüşü test eder
     */
    @Test
    public void testBackToMainMenu() {
        // Ana menüye dönüş (0)
        String input = "0\n";
        Scanner scanner = new Scanner(input);
        
        // View oluşturma
        userSettingsView = new UserSettingsView(scanner, userController);
        
        // Görünümü gösterme
        MenuView resultView = userSettingsView.display();
        
        // MainMenuView'a yönlendirildiğini doğrulama
        assertTrue("Ana menüye dönüş seçildiğinde MainMenuView'a yönlendirilmeli", resultView instanceof MainMenuView);
    }
    
    // Mock Controller Sınıfı
    
    /**
     * Mock UserController sınıfı
     */
    private class MockUserController extends UserController {
        private boolean loggedIn = true;
        private User currentUser;
        private boolean updateProfileSuccess = true;
        private boolean deleteAccountSuccess = true;
        
        @Override
        public boolean isLoggedIn() {
            return loggedIn;
        }
        
        public void setLoggedIn(boolean loggedIn) {
            this.loggedIn = loggedIn;
        }
        
        @Override
        public User getCurrentUser() {
            return currentUser;
        }
        
        public void setCurrentUser(User user) {
            this.currentUser = user;
        }
        
        @Override
        public boolean updateUserProfile(String email, String password) {
            if (updateProfileSuccess) {
                if (email != null) {
                    currentUser.setEmail(email);
                }
                if (password != null) {
                    currentUser.setPassword(password);
                }
                return true;
            }
            return false;
        }
        
        public void setUpdateProfileSuccess(boolean success) {
            this.updateProfileSuccess = success;
        }
        
        @Override
        public boolean deleteAccount() {
            return deleteAccountSuccess;
        }
        
        public void setDeleteAccountSuccess(boolean success) {
            this.deleteAccountSuccess = success;
        }
    }
} 