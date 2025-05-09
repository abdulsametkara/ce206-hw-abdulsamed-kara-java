package com.samet.music.gui;

import com.samet.music.util.DatabaseUtil;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginRegisterGUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField emailField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;
    private boolean isLoginMode = true;

    public LoginRegisterGUI() {
        setTitle("Music Library - Login/Register");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 340);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 246, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Music Library");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(new Color(41, 128, 185));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        panel.add(userLabel, gbc);
        gbc.gridx = 1;
        usernameField = new JTextField();
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220,220,220)),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        panel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        panel.add(passLabel, gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220,220,220)),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        panel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        panel.add(emailLabel, gbc);
        gbc.gridx = 1;
        emailField = new JTextField();
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220,220,220)),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        panel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        buttonPanel.setOpaque(false);
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        styleButton(loginButton);
        styleButton(registerButton);
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        panel.add(buttonPanel, gbc);

        gbc.gridy++;
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        statusLabel.setForeground(new Color(192, 57, 43));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(statusLabel, gbc);

        add(panel);

        emailLabel.setVisible(false);
        emailField.setVisible(false);

        // Action listeners
        loginButton.addActionListener(e -> handleLogin());
        registerButton.addActionListener(e -> toggleRegisterMode(emailLabel));
    }

    private void styleButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setBackground(new Color(41, 128, 185));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 22, 8, 22));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void toggleRegisterMode(JLabel emailLabel) {
        isLoginMode = !isLoginMode;
        usernameField.setText("");
        passwordField.setText("");
        emailField.setText("");
        statusLabel.setText(" ");
        if (isLoginMode) {
            loginButton.setText("Login");
            registerButton.setText("Register");
            emailLabel.setVisible(false);
            emailField.setVisible(false);
        } else {
            loginButton.setText("Sign Up");
            registerButton.setText("Back to Login");
            emailLabel.setVisible(true);
            emailField.setVisible(true);
        }
        revalidate();
        repaint();
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String email = emailField.getText().trim();

        if (isLoginMode) {
            // LOGIN
            if (username.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Please enter username and password.");
                return;
            }
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT id, password FROM users WHERE username = ?")) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String dbPassword = rs.getString("password");
                    if (dbPassword.equals(password)) {
                        int userId = rs.getInt("id");
                        // Giriş başarılı, ana ekrana geç
                        SwingUtilities.invokeLater(() -> {
                            dispose();
                            MusicLibraryGUI mainGUI = new MusicLibraryGUI();
                            mainGUI.setVisible(true);
                        });
                    } else {
                        statusLabel.setText("Incorrect password.");
                    }
                } else {
                    statusLabel.setText("User not found.");
                }
            } catch (SQLException ex) {
                statusLabel.setText("Database error.");
                ex.printStackTrace();
            }
        } else {
            // REGISTER
            if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                statusLabel.setText("Please fill all fields.");
                return;
            }
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement checkStmt = conn.prepareStatement("SELECT id FROM users WHERE username = ? OR email = ?");
                 PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO users (username, password, email) VALUES (?, ?, ?)")) {
                checkStmt.setString(1, username);
                checkStmt.setString(2, email);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    statusLabel.setText("Username or email already exists.");
                    return;
                }
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                insertStmt.setString(3, email);
                int affected = insertStmt.executeUpdate();
                if (affected > 0) {
                    statusLabel.setForeground(new Color(0, 128, 0));
                    statusLabel.setText("Registration successful! You can login now.");
                    // Alanları temizle ve login moduna dön
                    isLoginMode = true;
                    loginButton.setText("Login");
                    registerButton.setText("Register");
                    emailField.setVisible(false);
                    emailField.setText("");
                } else {
                    statusLabel.setText("Registration failed.");
                }
            } catch (SQLException ex) {
                statusLabel.setText("Database error.");
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        DatabaseUtil.initializeDatabase();
        SwingUtilities.invokeLater(() -> {
            new LoginRegisterGUI().setVisible(true);
        });
    }
} 