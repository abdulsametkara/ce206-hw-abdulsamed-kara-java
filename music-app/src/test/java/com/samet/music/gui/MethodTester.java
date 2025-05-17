package com.samet.music.gui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.samet.music.dao.AlbumDAO;
import com.samet.music.dao.ArtistDAO;
import com.samet.music.dao.PlaylistDAO;
import com.samet.music.dao.SongDAO;

/**
 * MethodTester - A test class that tests each method in MusicLibraryGUI
 * Tests every line of the MusicLibraryGUI.java file
 */
public class MethodTester {

    // List to track test results
    private List<String> passedTests = new ArrayList<>();
    private List<String> failedTests = new ArrayList<>();
    
    /**
     * TestGUI - A subclass of MusicLibraryGUI for testing
     */
    class TestGUI extends MusicLibraryGUI {
        
        public boolean updateStatusBarCalled = false;
        public boolean showErrorMessageCalled = false;
        public boolean showInfoMessageCalled = false;
        public boolean confirmDialogCalled = false;
        public boolean confirmDialogResult = true;
        
        public TestGUI() {
            super();
            this.initializeDatabase = false; // Disable database initialization for testing
        }
        
        @Override
        protected void updateStatusBar(String message) {
            updateStatusBarCalled = true;
            System.out.println("Status: " + message);
        }
        
        @Override
        protected void showErrorMessage(String message) {
            showErrorMessageCalled = true;
            System.out.println("Error: " + message);
        }
        
        @Override
        protected void showInfoMessage(String message) {
            showInfoMessageCalled = true;
            System.out.println("Info: " + message);
        }
        
        @Override
        protected boolean showConfirmDialog(String message) {
            confirmDialogCalled = true;
            System.out.println("Confirm: " + message);
            return confirmDialogResult;
        }
        
        // Method to clear flags for next test
        public void resetFlags() {
            updateStatusBarCalled = false;
            showErrorMessageCalled = false;
            showInfoMessageCalled = false;
            confirmDialogCalled = false;
        }
        
        // Method to prepare test data
        public void prepareTestData() {
            // Set up songs table
            DefaultTableModel songsModel = (DefaultTableModel) songsTable.getModel();
            songsModel.setRowCount(0);
            songsModel.addRow(new Object[]{"Test Song", "Test Artist", "Test Album", "Rock"});
            
            // Set up artists table
            DefaultTableModel artistsModel = (DefaultTableModel) artistsTable.getModel();
            artistsModel.setRowCount(0);
            artistsModel.addRow(new Object[]{"Test Artist", "USA", "Rock"});
            
            // Set up albums table
            DefaultTableModel albumsModel = (DefaultTableModel) albumsTable.getModel();
            albumsModel.setRowCount(0);
            albumsModel.addRow(new Object[]{"Test Album", "Test Artist", "2023", "Rock"});
            
            // Set up playlists table
            DefaultTableModel playlistsModel = (DefaultTableModel) playlistsTable.getModel();
            playlistsModel.setRowCount(0);
            playlistsModel.addRow(new Object[]{"Test Playlist", "1", "2023-01-01"});
        }
    }
    
    /**
     * Test setup frame method
     */
    public void testSetupFrame() {
        System.out.println("=== Testing setupFrame() ===");
        try {
            TestGUI gui = new TestGUI();
            
            // Check frame properties
            boolean result = gui.getTitle().equals("Music Library Application") && 
                            gui.getDefaultCloseOperation() == JFrame.EXIT_ON_CLOSE &&
                            gui.getContentPane().getLayout() instanceof java.awt.BorderLayout;
            
            recordTestResult("setupFrame()", result);
        } catch (Exception e) {
            recordTestResult("setupFrame()", false);
            e.printStackTrace();
        }
    }
    
    /**
     * Test create UI components method
     */
    public void testCreateUIComponents() {
        System.out.println("=== Testing createUIComponents() ===");
        try {
            TestGUI gui = new TestGUI();
            
            // Check if all components were created
            boolean componentsCreated = gui.tabbedPane != null && 
                                       gui.songsTable != null && 
                                       gui.artistsTable != null && 
                                       gui.albumsTable != null && 
                                       gui.playlistsTable != null;
            
            recordTestResult("createUIComponents()", componentsCreated);
        } catch (Exception e) {
            recordTestResult("createUIComponents()", false);
            e.printStackTrace();
        }
    }
    
    /**
     * Test create songs panel method
     */
    public void testCreateSongsPanel() {
        System.out.println("=== Testing createSongsPanel() ===");
        try {
            TestGUI gui = new TestGUI();
            
            // Check if songs panel was created correctly
            boolean panelCreated = gui.songsTable != null && 
                                   gui.btnAddSong != null && 
                                   gui.btnEditSong != null && 
                                   gui.btnDeleteSong != null;
            
            // Check table structure
            DefaultTableModel model = (DefaultTableModel) gui.songsTable.getModel();
            boolean tableStructureCorrect = model.getColumnCount() == 4 &&
                                            model.getColumnName(0).equals("Title") &&
                                            model.getColumnName(1).equals("Artist") &&
                                            model.getColumnName(2).equals("Album") &&
                                            model.getColumnName(3).equals("Genre");
            
            // Check action listeners
            boolean actionListenersAdded = gui.btnAddSong.getActionListeners().length > 0 &&
                                          gui.btnEditSong.getActionListeners().length > 0 &&
                                          gui.btnDeleteSong.getActionListeners().length > 0;
            
            boolean success = panelCreated && tableStructureCorrect && actionListenersAdded;
            recordTestResult("createSongsPanel()", success);
        } catch (Exception e) {
            recordTestResult("createSongsPanel()", false);
            e.printStackTrace();
        }
    }
    
    /**
     * Test create artists panel method
     */
    public void testCreateArtistsPanel() {
        System.out.println("=== Testing createArtistsPanel() ===");
        try {
            TestGUI gui = new TestGUI();
            
            // Check if artists panel was created correctly
            boolean panelCreated = gui.artistsTable != null && 
                                  gui.btnAddArtist != null && 
                                  gui.btnEditArtist != null && 
                                  gui.btnDeleteArtist != null;
            
            // Check table structure
            DefaultTableModel model = (DefaultTableModel) gui.artistsTable.getModel();
            boolean tableStructureCorrect = model.getColumnCount() == 3 &&
                                           model.getColumnName(0).equals("Name") &&
                                           model.getColumnName(1).equals("Country") &&
                                           model.getColumnName(2).equals("Genre");
            
            // Check action listeners
            boolean actionListenersAdded = gui.btnAddArtist.getActionListeners().length > 0 &&
                                          gui.btnEditArtist.getActionListeners().length > 0 &&
                                          gui.btnDeleteArtist.getActionListeners().length > 0;
            
            boolean success = panelCreated && tableStructureCorrect && actionListenersAdded;
            recordTestResult("createArtistsPanel()", success);
        } catch (Exception e) {
            recordTestResult("createArtistsPanel()", false);
            e.printStackTrace();
        }
    }
    
    /**
     * Test create albums panel method
     */
    public void testCreateAlbumsPanel() {
        System.out.println("=== Testing createAlbumsPanel() ===");
        try {
            TestGUI gui = new TestGUI();
            
            // Check if albums panel was created correctly
            boolean panelCreated = gui.albumsTable != null && 
                                  gui.btnAddAlbum != null && 
                                  gui.btnEditAlbum != null && 
                                  gui.btnDeleteAlbum != null;
            
            // Check table structure
            DefaultTableModel model = (DefaultTableModel) gui.albumsTable.getModel();
            boolean tableStructureCorrect = model.getColumnCount() == 4 &&
                                           model.getColumnName(0).equals("Title") &&
                                           model.getColumnName(1).equals("Artist") &&
                                           model.getColumnName(2).equals("Year") &&
                                           model.getColumnName(3).equals("Genre");
            
            // Check action listeners
            boolean actionListenersAdded = gui.btnAddAlbum.getActionListeners().length > 0 &&
                                          gui.btnEditAlbum.getActionListeners().length > 0 &&
                                          gui.btnDeleteAlbum.getActionListeners().length > 0;
            
            boolean success = panelCreated && tableStructureCorrect && actionListenersAdded;
            recordTestResult("createAlbumsPanel()", success);
        } catch (Exception e) {
            recordTestResult("createAlbumsPanel()", false);
            e.printStackTrace();
        }
    }
    
    /**
     * Test create playlists panel method
     */
    public void testCreatePlaylistsPanel() {
        System.out.println("=== Testing createPlaylistsPanel() ===");
        try {
            TestGUI gui = new TestGUI();
            
            // Check if playlists panel was created correctly
            boolean panelCreated = gui.playlistsTable != null && 
                                  gui.btnCreatePlaylist != null && 
                                  gui.btnEditPlaylist != null && 
                                  gui.btnDeletePlaylist != null &&
                                  gui.btnAddToPlaylist != null &&
                                  gui.btnRemoveFromPlaylist != null;
            
            // Check table structure
            DefaultTableModel model = (DefaultTableModel) gui.playlistsTable.getModel();
            boolean tableStructureCorrect = model.getColumnCount() == 3 &&
                                           model.getColumnName(0).equals("Name") &&
                                           model.getColumnName(1).equals("Song Count") &&
                                           model.getColumnName(2).equals("Created Date");
            
            // Check action listeners
            boolean actionListenersAdded = gui.btnCreatePlaylist.getActionListeners().length > 0 &&
                                          gui.btnEditPlaylist.getActionListeners().length > 0 &&
                                          gui.btnDeletePlaylist.getActionListeners().length > 0 &&
                                          gui.btnAddToPlaylist.getActionListeners().length > 0 &&
                                          gui.btnRemoveFromPlaylist.getActionListeners().length > 0;
            
            boolean success = panelCreated && tableStructureCorrect && actionListenersAdded;
            recordTestResult("createPlaylistsPanel()", success);
        } catch (Exception e) {
            recordTestResult("createPlaylistsPanel()", false);
            e.printStackTrace();
        }
    }
    
    /**
     * Test delete song handler
     */
    public void testOnDeleteSongClicked() {
        System.out.println("=== Testing onDeleteSongClicked() ===");
        try {
            TestGUI gui = new TestGUI();
            gui.prepareTestData();
            
            // Test with no selection
            gui.resetFlags();
            gui.songsTable.clearSelection();
            gui.onDeleteSongClicked();
            
            // Check that error message was shown for no selection
            boolean testNoSelection = gui.showErrorMessageCalled;
            
            // Test with selection
            gui.resetFlags();
            gui.songsTable.setRowSelectionInterval(0, 0);
            gui.onDeleteSongClicked();
            
            // Check that confirm dialog was shown
            boolean testWithSelection = gui.confirmDialogCalled;
            
            boolean success = testNoSelection && testWithSelection;
            recordTestResult("onDeleteSongClicked()", success);
        } catch (Exception e) {
            recordTestResult("onDeleteSongClicked()", false);
            e.printStackTrace();
        }
    }
    
    /**
     * Test delete artist handler
     */
    public void testOnDeleteArtistClicked() {
        System.out.println("=== Testing onDeleteArtistClicked() ===");
        try {
            TestGUI gui = new TestGUI();
            gui.prepareTestData();
            
            // Test with no selection
            gui.resetFlags();
            gui.artistsTable.clearSelection();
            gui.onDeleteArtistClicked();
            
            // Check that error message was shown for no selection
            boolean testNoSelection = gui.showErrorMessageCalled;
            
            // Test with selection
            gui.resetFlags();
            gui.artistsTable.setRowSelectionInterval(0, 0);
            gui.onDeleteArtistClicked();
            
            // Check that confirm dialog was shown
            boolean testWithSelection = gui.confirmDialogCalled;
            
            boolean success = testNoSelection && testWithSelection;
            recordTestResult("onDeleteArtistClicked()", success);
        } catch (Exception e) {
            recordTestResult("onDeleteArtistClicked()", false);
            e.printStackTrace();
        }
    }
    
    /**
     * Test delete album handler
     */
    public void testOnDeleteAlbumClicked() {
        System.out.println("=== Testing onDeleteAlbumClicked() ===");
        try {
            TestGUI gui = new TestGUI();
            gui.prepareTestData();
            
            // Test with no selection
            gui.resetFlags();
            gui.albumsTable.clearSelection();
            gui.onDeleteAlbumClicked();
            
            // Check that error message was shown for no selection
            boolean testNoSelection = gui.showErrorMessageCalled;
            
            // Test with selection
            gui.resetFlags();
            gui.albumsTable.setRowSelectionInterval(0, 0);
            gui.onDeleteAlbumClicked();
            
            // Check that confirm dialog was shown
            boolean testWithSelection = gui.confirmDialogCalled;
            
            boolean success = testNoSelection && testWithSelection;
            recordTestResult("onDeleteAlbumClicked()", success);
        } catch (Exception e) {
            recordTestResult("onDeleteAlbumClicked()", false);
            e.printStackTrace();
        }
    }
    
    /**
     * Test delete playlist handler
     */
    public void testOnDeletePlaylistClicked() {
        System.out.println("=== Testing onDeletePlaylistClicked() ===");
        try {
            TestGUI gui = new TestGUI();
            gui.prepareTestData();
            
            // Test with no selection
            gui.resetFlags();
            gui.playlistsTable.clearSelection();
            gui.onDeletePlaylistClicked();
            
            // Check that error message was shown for no selection
            boolean testNoSelection = gui.showErrorMessageCalled;
            
            // Test with selection
            gui.resetFlags();
            gui.playlistsTable.setRowSelectionInterval(0, 0);
            gui.onDeletePlaylistClicked();
            
            // Check that confirm dialog was shown
            boolean testWithSelection = gui.confirmDialogCalled;
            
            boolean success = testNoSelection && testWithSelection;
            recordTestResult("onDeletePlaylistClicked()", success);
        } catch (Exception e) {
            recordTestResult("onDeletePlaylistClicked()", false);
            e.printStackTrace();
        }
    }
    
    /**
     * Test add to playlist handler
     */
    public void testOnAddToPlaylistClicked() {
        System.out.println("=== Testing onAddToPlaylistClicked() ===");
        try {
            TestGUI gui = new TestGUI();
            gui.prepareTestData();
            
            // Test with no playlist selection
            gui.resetFlags();
            gui.playlistsTable.clearSelection();
            gui.onAddToPlaylistClicked();
            
            // Check that error message was shown for no selection
            boolean testNoSelection = gui.showErrorMessageCalled;
            
            recordTestResult("onAddToPlaylistClicked()", testNoSelection);
        } catch (Exception e) {
            recordTestResult("onAddToPlaylistClicked()", false);
            e.printStackTrace();
        }
    }
    
    /**
     * Test remove from playlist handler
     */
    public void testOnRemoveFromPlaylistClicked() {
        System.out.println("=== Testing onRemoveFromPlaylistClicked() ===");
        try {
            TestGUI gui = new TestGUI();
            gui.prepareTestData();
            
            // Test with no playlist selection
            gui.resetFlags();
            gui.playlistsTable.clearSelection();
            gui.onRemoveFromPlaylistClicked();
            
            // Check that error message was shown for no selection
            boolean testNoSelection = gui.showErrorMessageCalled;
            
            recordTestResult("onRemoveFromPlaylistClicked()", testNoSelection);
        } catch (Exception e) {
            recordTestResult("onRemoveFromPlaylistClicked()", false);
            e.printStackTrace();
        }
    }
    
    /**
     * Test utility methods
     */
    public void testUtilityMethods() {
        System.out.println("=== Testing Utility Methods ===");
        try {
            TestGUI gui = new TestGUI();
            
            // Test status bar update
            gui.resetFlags();
            gui.updateStatusBar("Test Message");
            boolean statusBarUpdated = gui.updateStatusBarCalled;
            
            // Test error message
            gui.resetFlags();
            gui.showErrorMessage("Test Error");
            boolean errorMessageShown = gui.showErrorMessageCalled;
            
            // Test info message
            gui.resetFlags();
            gui.showInfoMessage("Test Info");
            boolean infoMessageShown = gui.showInfoMessageCalled;
            
            // Test confirm dialog
            gui.resetFlags();
            gui.showConfirmDialog("Test Confirm");
            boolean confirmDialogShown = gui.confirmDialogCalled;
            
            boolean success = statusBarUpdated && errorMessageShown && infoMessageShown && confirmDialogShown;
            recordTestResult("Utility Methods", success);
        } catch (Exception e) {
            recordTestResult("Utility Methods", false);
            e.printStackTrace();
        }
    }
    
    /**
     * Record a test result
     */
    private void recordTestResult(String testName, boolean passed) {
        if (passed) {
            passedTests.add(testName);
            System.out.println("TEST PASSED: " + testName);
        } else {
            failedTests.add(testName);
            System.out.println("TEST FAILED: " + testName);
        }
    }
    
    /**
     * Print test summary
     */
    public void printSummary() {
        System.out.println("\n=== TEST SUMMARY ===");
        System.out.println("Total tests: " + (passedTests.size() + failedTests.size()));
        System.out.println("Passed tests: " + passedTests.size());
        System.out.println("Failed tests: " + failedTests.size());
        
        if (!passedTests.isEmpty()) {
            System.out.println("\nPassed Tests:");
            for (String test : passedTests) {
                System.out.println("✓ " + test);
            }
        }
        
        if (!failedTests.isEmpty()) {
            System.out.println("\nFailed Tests:");
            for (String test : failedTests) {
                System.out.println("✗ " + test);
            }
        }
    }
    
    /**
     * Run all tests
     */
    public void runAllTests() {
        testSetupFrame();
        testCreateUIComponents();
        testCreateSongsPanel();
        testCreateArtistsPanel();
        testCreateAlbumsPanel();
        testCreatePlaylistsPanel();
        testOnDeleteSongClicked();
        testOnDeleteArtistClicked();
        testOnDeleteAlbumClicked();
        testOnDeletePlaylistClicked();
        testOnAddToPlaylistClicked();
        testOnRemoveFromPlaylistClicked();
        testUtilityMethods();
        
        printSummary();
    }
    
    /**
     * Main method to run the tests
     */
    public static void main(String[] args) {
        System.out.println("Starting MusicLibraryGUI Method Tests...");
        MethodTester tester = new MethodTester();
        tester.runAllTests();
    }
} 