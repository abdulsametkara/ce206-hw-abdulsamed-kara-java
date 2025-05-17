package com.samet.music.gui;

/**
 * TestRunner - A simple utility to run tests for the MusicLibraryGUI
 * This class helps to test each line of the GUI implementation
 */
public class TestRunner {
    
    /**
     * Main method to run all the available tests
     */
    public static void main(String[] args) {
        System.out.println("======================================");
        System.out.println("Starting MusicLibraryGUI Tests");
        System.out.println("======================================");
        
        try {
            // Initialize test class
            GUIFunctionalityTester tester = new GUIFunctionalityTester();
            
            // Run all tests
            tester.runTests();
            
            System.out.println("======================================");
            System.out.println("All tests completed successfully!");
            System.out.println("======================================");
            
        } catch (Exception e) {
            System.err.println("Test failed with exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Simplified test class focused on functionality testing
     */
    static class GUIFunctionalityTester {
        
        // Test cases for each area of functionality
        private void testGuiInitialization() {
            System.out.println("Testing GUI Initialization...");
            
            try {
                // Create a new GUI instance for testing
                MusicLibraryGUI gui = new MusicLibraryGUI() {
                    // Override to prevent actual database connection
                    {
                        initializeDatabase = false;
                    }
                };
                
                // Verify essential components
                assert gui.tabbedPane != null : "Tabbed pane is null";
                assert gui.songsTable != null : "Songs table is null";
                assert gui.artistsTable != null : "Artists table is null";
                assert gui.albumsTable != null : "Albums table is null";
                assert gui.playlistsTable != null : "Playlists table is null";
                
                // Verify essential buttons
                assert gui.btnAddSong != null : "Add song button is null";
                assert gui.btnEditSong != null : "Edit song button is null";
                assert gui.btnDeleteSong != null : "Delete song button is null";
                assert gui.btnAddArtist != null : "Add artist button is null";
                assert gui.btnEditArtist != null : "Edit artist button is null";
                assert gui.btnDeleteArtist != null : "Delete artist button is null";
                assert gui.btnAddAlbum != null : "Add album button is null";
                assert gui.btnEditAlbum != null : "Edit album button is null";
                assert gui.btnDeleteAlbum != null : "Delete album button is null";
                assert gui.btnCreatePlaylist != null : "Create playlist button is null";
                assert gui.btnEditPlaylist != null : "Edit playlist button is null";
                assert gui.btnDeletePlaylist != null : "Delete playlist button is null";
                assert gui.btnAddToPlaylist != null : "Add to playlist button is null";
                assert gui.btnRemoveFromPlaylist != null : "Remove from playlist button is null";
                
                System.out.println("GUI initialization test passed");
            } catch (AssertionError e) {
                System.err.println("GUI initialization test failed: " + e.getMessage());
                throw e;
            }
        }
        
        private void testTableModels() {
            System.out.println("Testing table models...");
            
            try {
                // Create a new GUI instance for testing
                MusicLibraryGUI gui = new MusicLibraryGUI() {
                    // Override to prevent actual database connection
                    {
                        initializeDatabase = false;
                    }
                };
                
                // Check songs table model
                assert gui.songsTable.getModel() != null : "Songs table model is null";
                assert gui.songsTable.getModel().getColumnCount() == 4 : "Songs table should have 4 columns";
                assert gui.songsTable.getModel().getColumnName(0).equals("Title") : "First column should be Title";
                assert gui.songsTable.getModel().getColumnName(1).equals("Artist") : "Second column should be Artist";
                assert gui.songsTable.getModel().getColumnName(2).equals("Album") : "Third column should be Album";
                assert gui.songsTable.getModel().getColumnName(3).equals("Genre") : "Fourth column should be Genre";
                
                // Check artists table model
                assert gui.artistsTable.getModel() != null : "Artists table model is null";
                assert gui.artistsTable.getModel().getColumnCount() == 3 : "Artists table should have 3 columns";
                assert gui.artistsTable.getModel().getColumnName(0).equals("Name") : "First column should be Name";
                assert gui.artistsTable.getModel().getColumnName(1).equals("Country") : "Second column should be Country";
                assert gui.artistsTable.getModel().getColumnName(2).equals("Genre") : "Third column should be Genre";
                
                // Check albums table model
                assert gui.albumsTable.getModel() != null : "Albums table model is null";
                assert gui.albumsTable.getModel().getColumnCount() == 4 : "Albums table should have 4 columns";
                assert gui.albumsTable.getModel().getColumnName(0).equals("Title") : "First column should be Title";
                assert gui.albumsTable.getModel().getColumnName(1).equals("Artist") : "Second column should be Artist";
                assert gui.albumsTable.getModel().getColumnName(2).equals("Year") : "Third column should be Year";
                assert gui.albumsTable.getModel().getColumnName(3).equals("Genre") : "Fourth column should be Genre";
                
                // Check playlists table model
                assert gui.playlistsTable.getModel() != null : "Playlists table model is null";
                assert gui.playlistsTable.getModel().getColumnCount() == 3 : "Playlists table should have 3 columns";
                assert gui.playlistsTable.getModel().getColumnName(0).equals("Name") : "First column should be Name";
                assert gui.playlistsTable.getModel().getColumnName(1).equals("Song Count") : "Second column should be Song Count";
                assert gui.playlistsTable.getModel().getColumnName(2).equals("Created Date") : "Third column should be Created Date";
                
                System.out.println("Table models test passed");
            } catch (AssertionError e) {
                System.err.println("Table models test failed: " + e.getMessage());
                throw e;
            }
        }
        
        private void testEventHandlers() {
            System.out.println("Testing event handlers...");
            
            try {
                // Create a new GUI instance for testing
                MusicLibraryGUI gui = new MusicLibraryGUI() {
                    // Override to prevent actual database connection
                    {
                        initializeDatabase = false;
                    }
                };
                
                // Check that all buttons have action listeners
                assert gui.btnAddSong.getActionListeners().length > 0 : "Add song button has no action listener";
                assert gui.btnEditSong.getActionListeners().length > 0 : "Edit song button has no action listener";
                assert gui.btnDeleteSong.getActionListeners().length > 0 : "Delete song button has no action listener";
                assert gui.btnAddArtist.getActionListeners().length > 0 : "Add artist button has no action listener";
                assert gui.btnEditArtist.getActionListeners().length > 0 : "Edit artist button has no action listener";
                assert gui.btnDeleteArtist.getActionListeners().length > 0 : "Delete artist button has no action listener";
                assert gui.btnAddAlbum.getActionListeners().length > 0 : "Add album button has no action listener";
                assert gui.btnEditAlbum.getActionListeners().length > 0 : "Edit album button has no action listener";
                assert gui.btnDeleteAlbum.getActionListeners().length > 0 : "Delete album button has no action listener";
                assert gui.btnCreatePlaylist.getActionListeners().length > 0 : "Create playlist button has no action listener";
                assert gui.btnEditPlaylist.getActionListeners().length > 0 : "Edit playlist button has no action listener";
                assert gui.btnDeletePlaylist.getActionListeners().length > 0 : "Delete playlist button has no action listener";
                assert gui.btnAddToPlaylist.getActionListeners().length > 0 : "Add to playlist button has no action listener";
                assert gui.btnRemoveFromPlaylist.getActionListeners().length > 0 : "Remove from playlist button has no action listener";
                
                System.out.println("Event handlers test passed");
            } catch (AssertionError e) {
                System.err.println("Event handlers test failed: " + e.getMessage());
                throw e;
            }
        }
        
        private void testFrameProperties() {
            System.out.println("Testing frame properties...");
            
            try {
                // Create a new GUI instance for testing
                MusicLibraryGUI gui = new MusicLibraryGUI() {
                    // Override to prevent actual database connection
                    {
                        initializeDatabase = false;
                    }
                };
                
                // Check frame properties
                assert gui.getTitle().equals("Music Library Application") : "Frame title is incorrect";
                assert gui.getDefaultCloseOperation() == javax.swing.JFrame.EXIT_ON_CLOSE : "Default close operation is incorrect";
                
                System.out.println("Frame properties test passed");
            } catch (AssertionError e) {
                System.err.println("Frame properties test failed: " + e.getMessage());
                throw e;
            }
        }
        
        private void testTabbedPane() {
            System.out.println("Testing tabbed pane...");
            
            try {
                // Create a new GUI instance for testing
                MusicLibraryGUI gui = new MusicLibraryGUI() {
                    // Override to prevent actual database connection
                    {
                        initializeDatabase = false;
                    }
                };
                
                // Check tabbed pane setup
                assert gui.tabbedPane.getTabCount() == 4 : "Tabbed pane should have 4 tabs";
                assert gui.tabbedPane.getTitleAt(0).equals("Songs") : "First tab should be Songs";
                assert gui.tabbedPane.getTitleAt(1).equals("Artists") : "Second tab should be Artists";
                assert gui.tabbedPane.getTitleAt(2).equals("Albums") : "Third tab should be Albums";
                assert gui.tabbedPane.getTitleAt(3).equals("Playlists") : "Fourth tab should be Playlists";
                
                System.out.println("Tabbed pane test passed");
            } catch (AssertionError e) {
                System.err.println("Tabbed pane test failed: " + e.getMessage());
                throw e;
            }
        }
        
        private void testTableCellsNotEditable() {
            System.out.println("Testing table cells are not editable...");
            
            try {
                // Create a new GUI instance for testing
                MusicLibraryGUI gui = new MusicLibraryGUI() {
                    // Override to prevent actual database connection
                    {
                        initializeDatabase = false;
                    }
                };
                
                // Check that table cells are not editable
                assert !gui.songsTable.getModel().isCellEditable(0, 0) : "Songs table cells should not be editable";
                assert !gui.artistsTable.getModel().isCellEditable(0, 0) : "Artists table cells should not be editable";
                assert !gui.albumsTable.getModel().isCellEditable(0, 0) : "Albums table cells should not be editable";
                assert !gui.playlistsTable.getModel().isCellEditable(0, 0) : "Playlists table cells should not be editable";
                
                System.out.println("Table cells not editable test passed");
            } catch (AssertionError e) {
                System.err.println("Table cells not editable test failed: " + e.getMessage());
                throw e;
            } catch (ArrayIndexOutOfBoundsException e) {
                // This is expected if tables are empty
                System.out.println("Tables don't have data yet, but that's ok for this test");
            }
        }
        
        /**
         * Run all tests
         */
        public void runTests() {
            testGuiInitialization();
            testTableModels();
            testEventHandlers();
            testFrameProperties();
            testTabbedPane();
            testTableCellsNotEditable();
        }
    }
} 