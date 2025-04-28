/*package com.samet.music.view.fx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.samet.music.controller.UserController;
import com.samet.music.model.User;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Test class for LoginViewFX
 * Note: This is a unit test using Mockito, not a UI test with TestFX
 */
/*
 public class LoginViewFXTest {
    
    @Mock
    private Stage stageMock;
    
    @Mock
    private UserController userControllerMock;
    
    private LoginViewFX loginView;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        loginView = new LoginViewFX(stageMock, userControllerMock);
    }
    
    @Test
    public void testGetRoot() {
        // Test that getRoot returns a BorderPane
        Parent root = loginView.getRoot();
        assertNotNull("Root should not be null", root);
        assertTrue("Root should be a BorderPane", root instanceof BorderPane);
    }
    
    @Test
    public void testGetStage() {
        // Test that getStage returns the provided stage
        Stage stage = loginView.getStage();
        assertEquals("Stage should be the mock stage", stageMock, stage);
    }
    
    @Test
    public void testLoginSuccess() {
        // This would be a TestFX test in a real UI test
        // But we'll use Mockito to simulate the login process
        
        // Set up the mock to return true (login success)
        when(userControllerMock.loginUser(anyString(), anyString())).thenReturn(true);
        User mockUser = mock(User.class);
        when(userControllerMock.getCurrentUser()).thenReturn(mockUser);
        
        // Simulate the user entering credentials and clicking login
        // In a real TestFX test, we would find these components and interact with them
        TextField usernameField = new TextField();
        usernameField.setText("testuser");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setText("password");
        
        Button loginButton = new Button();
        
        // Call the login method directly (in a real test we would click the button)
        loginView.handleLogin("testuser", "password");
        
        // Verify the controller was called with the correct credentials
        verify(userControllerMock).loginUser("testuser", "password");
    }
    
    @Test
    public void testLoginFailure() throws Exception {
        // Set up the mock to return false (login failure)
        when(userControllerMock.loginUser(anyString(), anyString())).thenReturn(false);
        
        // Simulate login attempt with invalid credentials
        loginView.handleLogin("wronguser", "wrongpass");
        
        // Verify the controller was called
        verify(userControllerMock).loginUser("wronguser", "wrongpass");
        
        // In a real UI test, we would verify that an error message is displayed
    }
    
    @Test
    public void testRegistration() {
        // Set up the mock for successful registration
        when(userControllerMock.registerUser(anyString(), anyString(), anyString())).thenReturn(true);
        
        // Simulate registration
        loginView.handleRegister("newuser", "newpass");
        
        // We don't need to verify registerUser here because in the implementation,
        // handleRegister just opens the registration page, it doesn't call registerUser directly
    }
} 
*/