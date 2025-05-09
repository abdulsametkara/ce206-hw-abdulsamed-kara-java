package com.samet.music.gui;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assume;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class LoginRegisterGUITest {
    
    private LoginRegisterGUI loginRegisterGUI;
    private boolean isHeadless;
    
    @Before
    public void setUp() {
        isHeadless = GraphicsEnvironment.isHeadless();
        Assume.assumeFalse("Skipping GUI tests in headless environment", isHeadless);
        
        try {
            // Create GUI instance for testing
            loginRegisterGUI = new LoginRegisterGUI();
        } catch (HeadlessException e) {
            // Skip tests if running in headless environment
            Assume.assumeNoException("Skipping GUI tests in headless environment", e);
        }
    }
    
    @After
    public void tearDown() {
        if (loginRegisterGUI != null) {
            loginRegisterGUI.dispose();
            loginRegisterGUI = null;
        }
    }
    
    @Test
    public void testConstructor() {
        Assume.assumeFalse("Skipping GUI tests in headless environment", isHeadless);
        
        assertNotNull("LoginRegisterGUI should not be null", loginRegisterGUI);
        assertEquals("Music Library - Login/Register", loginRegisterGUI.getTitle());
        assertEquals(JFrame.EXIT_ON_CLOSE, loginRegisterGUI.getDefaultCloseOperation());
        assertEquals(420, loginRegisterGUI.getSize().width);
        assertEquals(340, loginRegisterGUI.getSize().height);
        assertFalse(loginRegisterGUI.isResizable());
    }
    
    @Test
    public void testInitialComponentsState() throws Exception {
        Assume.assumeFalse("Skipping GUI tests in headless environment", isHeadless);
        
        // Use reflection to access private fields
        Field usernameField = LoginRegisterGUI.class.getDeclaredField("usernameField");
        Field passwordField = LoginRegisterGUI.class.getDeclaredField("passwordField");
        Field emailField = LoginRegisterGUI.class.getDeclaredField("emailField");
        Field loginButton = LoginRegisterGUI.class.getDeclaredField("loginButton");
        Field registerButton = LoginRegisterGUI.class.getDeclaredField("registerButton");
        Field statusLabel = LoginRegisterGUI.class.getDeclaredField("statusLabel");
        Field isLoginMode = LoginRegisterGUI.class.getDeclaredField("isLoginMode");
        
        usernameField.setAccessible(true);
        passwordField.setAccessible(true);
        emailField.setAccessible(true);
        loginButton.setAccessible(true);
        registerButton.setAccessible(true);
        statusLabel.setAccessible(true);
        isLoginMode.setAccessible(true);
        
        // Verify fields exist and are initialized
        assertNotNull(usernameField.get(loginRegisterGUI));
        assertNotNull(passwordField.get(loginRegisterGUI));
        assertNotNull(emailField.get(loginRegisterGUI));
        assertNotNull(loginButton.get(loginRegisterGUI));
        assertNotNull(registerButton.get(loginRegisterGUI));
        assertNotNull(statusLabel.get(loginRegisterGUI));
        
        // Check initial values
        assertTrue((boolean) isLoginMode.get(loginRegisterGUI));
        assertEquals("Login", ((JButton) loginButton.get(loginRegisterGUI)).getText());
        assertEquals("Register", ((JButton) registerButton.get(loginRegisterGUI)).getText());
        
        // Email field should be initially invisible in login mode
        JLabel emailLabel = null;
        Component[] components = loginRegisterGUI.getContentPane().getComponents();
        if (components.length > 0 && components[0] instanceof JPanel) {
            Component[] panelComponents = ((JPanel) components[0]).getComponents();
            for (Component c : panelComponents) {
                if (c instanceof JLabel && ((JLabel) c).getText().equals("Email:")) {
                    emailLabel = (JLabel) c;
                    break;
                }
            }
        }
        
        // This test may fail in headless mode, so handle that
        if (emailLabel != null) {
            assertFalse(emailLabel.isVisible());
            assertFalse(((JTextField) emailField.get(loginRegisterGUI)).isVisible());
        }
    }
    
    @Test
    public void testToggleRegisterMode() throws Exception {
        Assume.assumeFalse("Skipping GUI tests in headless environment", isHeadless);
        
        // Get access to private fields and methods
        Field isLoginMode = LoginRegisterGUI.class.getDeclaredField("isLoginMode");
        Field loginButton = LoginRegisterGUI.class.getDeclaredField("loginButton");
        Field registerButton = LoginRegisterGUI.class.getDeclaredField("registerButton");
        
        Method toggleRegisterMode = LoginRegisterGUI.class.getDeclaredMethod("toggleRegisterMode", JLabel.class);
        
        isLoginMode.setAccessible(true);
        loginButton.setAccessible(true);
        registerButton.setAccessible(true);
        toggleRegisterMode.setAccessible(true);
        
        // Create a dummy JLabel for emailLabel parameter
        JLabel dummyEmailLabel = new JLabel("Email:");
        
        // Initial state should be login mode
        assertTrue((boolean) isLoginMode.get(loginRegisterGUI));
        
        // Toggle to register mode
        toggleRegisterMode.invoke(loginRegisterGUI, dummyEmailLabel);
        
        // Should now be in register mode
        assertFalse((boolean) isLoginMode.get(loginRegisterGUI));
        assertEquals("Sign Up", ((JButton) loginButton.get(loginRegisterGUI)).getText());
        assertEquals("Back to Login", ((JButton) registerButton.get(loginRegisterGUI)).getText());
        
        // Toggle back to login mode
        toggleRegisterMode.invoke(loginRegisterGUI, dummyEmailLabel);
        
        // Should be back in login mode
        assertTrue((boolean) isLoginMode.get(loginRegisterGUI));
        assertEquals("Login", ((JButton) loginButton.get(loginRegisterGUI)).getText());
        assertEquals("Register", ((JButton) registerButton.get(loginRegisterGUI)).getText());
    }
    
    @Test
    public void testHandleLoginWithEmptyFields() throws Exception {
        Assume.assumeFalse("Skipping GUI tests in headless environment", isHeadless);
        
        // Test the login handling with empty fields
        Field statusLabel = LoginRegisterGUI.class.getDeclaredField("statusLabel");
        Method handleLogin = LoginRegisterGUI.class.getDeclaredMethod("handleLogin");
        
        statusLabel.setAccessible(true);
        handleLogin.setAccessible(true);
        
        // Call the handleLogin method
        handleLogin.invoke(loginRegisterGUI);
        
        // Status label should show an error message
        String statusText = ((JLabel) statusLabel.get(loginRegisterGUI)).getText();
        assertEquals("Please enter username and password.", statusText);
    }
    
    @Test
    public void testStyleButton() throws Exception {
        Assume.assumeFalse("Skipping GUI tests in headless environment", isHeadless);
        
        // Test the button styling
        Method styleButton = LoginRegisterGUI.class.getDeclaredMethod("styleButton", JButton.class);
        styleButton.setAccessible(true);
        
        JButton testButton = new JButton("Test");
        styleButton.invoke(loginRegisterGUI, testButton);
        
        // Verify styling was applied
        assertEquals(new Color(41, 128, 185), testButton.getBackground());
        assertEquals(Color.WHITE, testButton.getForeground());
        assertFalse(testButton.isFocusPainted());
        assertEquals(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR), testButton.getCursor());
    }
} 