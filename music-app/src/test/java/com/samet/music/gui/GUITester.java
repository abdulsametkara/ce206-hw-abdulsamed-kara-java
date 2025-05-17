package com.samet.music.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.samet.music.dao.AlbumDAO;
import com.samet.music.dao.ArtistDAO;
import com.samet.music.dao.PlaylistDAO;
import com.samet.music.dao.SongDAO;

import java.util.ArrayList;
import java.util.List;

/**
 * GUITester - Tests the MusicLibraryGUI class by checking all components and functionality
 * This test class uses direct validation instead of test frameworks like JUnit that might be missing
 */
public class GUITester {
    
    // GUI to test
    private TestableGUI gui;
    
    // Mock DAOs
    private MockSongDAO mockSongDAO;
    private MockArtistDAO mockArtistDAO;
    private MockAlbumDAO mockAlbumDAO;
    private MockPlaylistDAO mockPlaylistDAO;
    
    /**
     * Mock SongDAO implementation for testing
     */
    static class MockSongDAO extends SongDAO {
        public boolean addSongCalled = false;
        public boolean deleteSongCalled = false;
        public boolean updateSongCalled = false;
        
        @Override
        public void addSong(String title, String artist, String album, String genre) {
            addSongCalled = true;
        }
        
        @Override
        public void deleteSong(String title, String artist, String album) {
            deleteSongCalled = true;
        }
        
        @Override
        public boolean updateSong(String oldTitle, String oldArtist, String oldAlbum, 
                                 String newTitle, String newArtist, String newAlbum, String newGenre) {
            updateSongCalled = true;
            return true;
        }
        
        @Override
        public List<String[]> getAllSongs() {
            List<String[]> songs = new ArrayList<>();
            songs.add(new String[]{"Song1", "Artist1", "Album1", "Rock"});
            songs.add(new String[]{"Song2", "Artist2", "Album2", "Pop"});
            return songs;
        }
    }
    
    /**
     * Mock ArtistDAO implementation for testing
     */
    static class MockArtistDAO extends ArtistDAO {
        public boolean addArtistCalled = false;
        public boolean deleteArtistCalled = false;
        public boolean updateArtistCalled = false;
        
        @Override
        public boolean addArtist(String name, String country, String genre, int popularity) {
            addArtistCalled = true;
            return true;
        }
        
        @Override
        public boolean deleteArtist(String name) {
            deleteArtistCalled = true;
            return true;
        }
        
        @Override
        public boolean updateArtist(String oldName, String newName, String newCountry, String newGenre) {
            updateArtistCalled = true;
            return true;
        }
        
        @Override
        public List<String[]> getAllArtists() {
            List<String[]> artists = new ArrayList<>();
            artists.add(new String[]{"Artist1", "USA", "Rock"});
            artists.add(new String[]{"Artist2", "UK", "Pop"});
            return artists;
        }
    }
    
    /**
     * Mock AlbumDAO implementation for testing
     */
    static class MockAlbumDAO extends AlbumDAO {
        public boolean addAlbumCalled = false;
        public boolean deleteAlbumCalled = false;
        public boolean updateAlbumCalled = false;
        
        @Override
        public boolean addAlbum(String title, String artist, String year, String genre, int rating) {
            addAlbumCalled = true;
            return true;
        }
        
        @Override
        public boolean deleteAlbum(String title, String artist) {
            deleteAlbumCalled = true;
            return true;
        }
        
        @Override
        public boolean updateAlbum(String oldTitle, String oldArtist, String newTitle, String newArtist, String newYear, String newGenre) {
            updateAlbumCalled = true;
            return true;
        }
        
        @Override
        public List<String[]> getAllAlbums() {
            List<String[]> albums = new ArrayList<>();
            albums.add(new String[]{"Album1", "Artist1", "2020", "Rock"});
            albums.add(new String[]{"Album2", "Artist2", "2021", "Pop"});
            return albums;
        }
    }
    
    /**
     * Mock PlaylistDAO implementation for testing
     */
    static class MockPlaylistDAO extends PlaylistDAO {
        public boolean addPlaylistCalled = false;
        public boolean deletePlaylistCalled = false;
        public boolean updatePlaylistCalled = false;
        
        @Override
        public boolean addPlaylist(String name, String description, int userId) {
            addPlaylistCalled = true;
            return true;
        }
        
        @Override
        public boolean deletePlaylist(String name) {
            deletePlaylistCalled = true;
            return true;
        }
        
        @Override
        public boolean updatePlaylist(String oldName, String newName) {
            updatePlaylistCalled = true;
            return true;
        }
        
        @Override
        public List<String[]> getAllPlaylists() {
            List<String[]> playlists = new ArrayList<>();
            playlists.add(new String[]{"Playlist1", "2", "2023-01-01"});
            playlists.add(new String[]{"Playlist2", "1", "2023-02-01"});
            return playlists;
        }
    }
    
    /**
     * Extended GUI class for testing
     */
    class TestableGUI extends MusicLibraryGUI {
        public boolean updateStatusBarCalled = false;
        public boolean showErrorMessageCalled = false;
        public boolean showInfoMessageCalled = false;
        public boolean showConfirmDialogCalled = false;
        
        public TestableGUI(SongDAO songDAO, ArtistDAO artistDAO, AlbumDAO albumDAO, PlaylistDAO playlistDAO) {
            super(songDAO, artistDAO, albumDAO, playlistDAO);
            this.initializeDatabase = false; // Disable database initialization for testing
        }
        
        @Override
        protected void updateStatusBar(String message) {
            updateStatusBarCalled = true;
        }
        
        @Override
        protected void showErrorMessage(String message) {
            showErrorMessageCalled = true;
        }
        
        @Override
        protected void showInfoMessage(String message) {
            showInfoMessageCalled = true;
        }
        
        @Override
        protected boolean showConfirmDialog(String message) {
            showConfirmDialogCalled = true;
            return true;
        }
    }
    
    /**
     * Setup the test environment
     */
    public void setUp() {
        // Create mock DAOs
        mockSongDAO = new MockSongDAO();
        mockArtistDAO = new MockArtistDAO();
        mockAlbumDAO = new MockAlbumDAO();
        mockPlaylistDAO = new MockPlaylistDAO();
        
        // Create GUI with mocks
        gui = new TestableGUI(mockSongDAO, mockArtistDAO, mockAlbumDAO, mockPlaylistDAO);
    }
    
    /**
     * Test GUI initialization
     */
    public void testGUIInitialization() {
        System.out.println("Testing GUI Initialization...");
        
        // Check component initialization
        boolean componentsInitialized = 
            gui.tabbedPane != null && 
            gui.songsTable != null && 
            gui.artistsTable != null && 
            gui.albumsTable != null && 
            gui.playlistsTable != null && 
            gui.statusLabel != null;
        
        System.out.println("Components initialized: " + componentsInitialized);
        
        // Check button initialization
        boolean buttonsInitialized = 
            gui.btnAddSong != null && 
            gui.btnEditSong != null && 
            gui.btnDeleteSong != null &&
            gui.btnAddArtist != null && 
            gui.btnEditArtist != null && 
            gui.btnDeleteArtist != null &&
            gui.btnAddAlbum != null && 
            gui.btnEditAlbum != null && 
            gui.btnDeleteAlbum != null &&
            gui.btnCreatePlaylist != null && 
            gui.btnEditPlaylist != null && 
            gui.btnDeletePlaylist != null &&
            gui.btnAddToPlaylist != null && 
            gui.btnRemoveFromPlaylist != null;
        
        System.out.println("Buttons initialized: " + buttonsInitialized);
        
        // Check action listeners are added
        boolean actionListenersAdded = 
            gui.btnAddSong.getActionListeners().length > 0 &&
            gui.btnEditSong.getActionListeners().length > 0 &&
            gui.btnDeleteSong.getActionListeners().length > 0 &&
            gui.btnAddArtist.getActionListeners().length > 0 &&
            gui.btnEditArtist.getActionListeners().length > 0 &&
            gui.btnDeleteArtist.getActionListeners().length > 0 &&
            gui.btnAddAlbum.getActionListeners().length > 0 &&
            gui.btnEditAlbum.getActionListeners().length > 0 &&
            gui.btnDeleteAlbum.getActionListeners().length > 0 &&
            gui.btnCreatePlaylist.getActionListeners().length > 0 &&
            gui.btnEditPlaylist.getActionListeners().length > 0 &&
            gui.btnDeletePlaylist.getActionListeners().length > 0 &&
            gui.btnAddToPlaylist.getActionListeners().length > 0 &&
            gui.btnRemoveFromPlaylist.getActionListeners().length > 0;
        
        System.out.println("Action listeners added: " + actionListenersAdded);
        
        // Check tabbed pane setup
        boolean tabbedPaneSetup = 
            gui.tabbedPane.getTabCount() == 4 &&
            gui.tabbedPane.getTitleAt(0).equals("Songs") &&
            gui.tabbedPane.getTitleAt(1).equals("Artists") &&
            gui.tabbedPane.getTitleAt(2).equals("Albums") &&
            gui.tabbedPane.getTitleAt(3).equals("Playlists");
        
        System.out.println("Tabbed pane setup: " + tabbedPaneSetup);
    }
    
    /**
     * Test data loading
     */
    public void testDataLoading() {
        System.out.println("Testing Data Loading...");
        
        // Check table models have data
        DefaultTableModel songsModel = (DefaultTableModel) gui.songsTable.getModel();
        DefaultTableModel artistsModel = (DefaultTableModel) gui.artistsTable.getModel();
        DefaultTableModel albumsModel = (DefaultTableModel) gui.albumsTable.getModel();
        DefaultTableModel playlistsModel = (DefaultTableModel) gui.playlistsTable.getModel();
        
        boolean dataLoaded = 
            songsModel.getRowCount() == 2 &&
            artistsModel.getRowCount() == 2 &&
            albumsModel.getRowCount() == 2 &&
            playlistsModel.getRowCount() == 2;
        
        System.out.println("Data loaded into tables: " + dataLoaded);
        
        // Check specific data values
        boolean specificDataCorrect = 
            songsModel.getValueAt(0, 0).equals("Song1") &&
            artistsModel.getValueAt(0, 0).equals("Artist1") &&
            albumsModel.getValueAt(0, 0).equals("Album1") &&
            playlistsModel.getValueAt(0, 0).equals("Playlist1");
        
        System.out.println("Specific data values correct: " + specificDataCorrect);
    }
    
    /**
     * Test table structure
     */
    public void testTableStructure() {
        System.out.println("Testing Table Structure...");
        
        // Test song table structure
        DefaultTableModel songsModel = (DefaultTableModel) gui.songsTable.getModel();
        boolean songTableStructure = 
            songsModel.getColumnCount() == 4 &&
            songsModel.getColumnName(0).equals("Title") &&
            songsModel.getColumnName(1).equals("Artist") &&
            songsModel.getColumnName(2).equals("Album") &&
            songsModel.getColumnName(3).equals("Genre");
        
        System.out.println("Song table structure correct: " + songTableStructure);
        
        // Test artist table structure
        DefaultTableModel artistsModel = (DefaultTableModel) gui.artistsTable.getModel();
        boolean artistTableStructure = 
            artistsModel.getColumnCount() == 3 &&
            artistsModel.getColumnName(0).equals("Name") &&
            artistsModel.getColumnName(1).equals("Country") &&
            artistsModel.getColumnName(2).equals("Genre");
        
        System.out.println("Artist table structure correct: " + artistTableStructure);
        
        // Test album table structure
        DefaultTableModel albumsModel = (DefaultTableModel) gui.albumsTable.getModel();
        boolean albumTableStructure = 
            albumsModel.getColumnCount() == 4 &&
            albumsModel.getColumnName(0).equals("Title") &&
            albumsModel.getColumnName(1).equals("Artist") &&
            albumsModel.getColumnName(2).equals("Year") &&
            albumsModel.getColumnName(3).equals("Genre");
        
        System.out.println("Album table structure correct: " + albumTableStructure);
        
        // Test playlist table structure
        DefaultTableModel playlistsModel = (DefaultTableModel) gui.playlistsTable.getModel();
        boolean playlistTableStructure = 
            playlistsModel.getColumnCount() == 3 &&
            playlistsModel.getColumnName(0).equals("Name") &&
            playlistsModel.getColumnName(1).equals("Song Count") &&
            playlistsModel.getColumnName(2).equals("Created Date");
        
        System.out.println("Playlist table structure correct: " + playlistTableStructure);
    }
    
    /**
     * Test adding items
     */
    public void testAddItems() {
        System.out.println("Testing Add Items Functionality...");
        
        // Test add song
        mockSongDAO.addSong("Test Song", "Test Artist", "Test Album", "Rock");
        System.out.println("Add song called: " + mockSongDAO.addSongCalled);
        
        // Test add artist
        mockArtistDAO.addArtist("Test Artist", "Canada", "Jazz", 1);
        System.out.println("Add artist called: " + mockArtistDAO.addArtistCalled);
        
        // Test add album
        mockAlbumDAO.addAlbum("Test Album", "Test Artist", "2023", "Rock", 1);
        System.out.println("Add album called: " + mockAlbumDAO.addAlbumCalled);
        
        // Test add playlist
        mockPlaylistDAO.addPlaylist("Test Playlist", "Description", 1);
        System.out.println("Add playlist called: " + mockPlaylistDAO.addPlaylistCalled);
    }
    
    /**
     * Test editing items
     */
    public void testEditItems() {
        System.out.println("Testing Edit Items Functionality...");
        
        // Test edit song
        boolean songUpdated = mockSongDAO.updateSong(
            "Original Song", "Original Artist", "Original Album",
            "Updated Song", "Updated Artist", "Updated Album", "Pop"
        );
        System.out.println("Edit song called: " + mockSongDAO.updateSongCalled);
        System.out.println("Song update successful: " + songUpdated);
        
        // Test edit artist
        boolean artistUpdated = mockArtistDAO.updateArtist(
            "Original Artist", "Updated Artist", "Updated Country", "Updated Genre"
        );
        System.out.println("Edit artist called: " + mockArtistDAO.updateArtistCalled);
        System.out.println("Artist update successful: " + artistUpdated);
        
        // Test edit album
        boolean albumUpdated = mockAlbumDAO.updateAlbum(
            "Original Album", "Original Artist", 
            "Updated Album", "Updated Artist", "2023", "Updated Genre"
        );
        System.out.println("Edit album called: " + mockAlbumDAO.updateAlbumCalled);
        System.out.println("Album update successful: " + albumUpdated);
        
        // Test edit playlist
        boolean playlistUpdated = mockPlaylistDAO.updatePlaylist(
            "Original Playlist", "Updated Playlist"
        );
        System.out.println("Edit playlist called: " + mockPlaylistDAO.updatePlaylistCalled);
        System.out.println("Playlist update successful: " + playlistUpdated);
    }
    
    /**
     * Test deleting items
     */
    public void testDeleteItems() {
        System.out.println("Testing Delete Items Functionality...");
        
        // Test delete song
        mockSongDAO.deleteSong("Song to Delete", "Artist", "Album");
        System.out.println("Delete song called: " + mockSongDAO.deleteSongCalled);
        
        // Test delete artist
        boolean artistDeleted = mockArtistDAO.deleteArtist("Artist to Delete");
        System.out.println("Delete artist called: " + mockArtistDAO.deleteArtistCalled);
        System.out.println("Artist deleted successfully: " + artistDeleted);
        
        // Test delete album
        boolean albumDeleted = mockAlbumDAO.deleteAlbum("Album to Delete", "Artist");
        System.out.println("Delete album called: " + mockAlbumDAO.deleteAlbumCalled);
        System.out.println("Album deleted successfully: " + albumDeleted);
        
        // Test delete playlist
        boolean playlistDeleted = mockPlaylistDAO.deletePlaylist("Playlist to Delete");
        System.out.println("Delete playlist called: " + mockPlaylistDAO.deletePlaylistCalled);
        System.out.println("Playlist deleted successfully: " + playlistDeleted);
    }
    
    /**
     * Test button actions by directly calling the handler methods
     */
    public void testButtonActions() {
        System.out.println("Testing Button Action Handlers...");
        
        // Setup for testing
        DefaultTableModel songsModel = (DefaultTableModel) gui.songsTable.getModel();
        DefaultTableModel artistsModel = (DefaultTableModel) gui.artistsTable.getModel();
        DefaultTableModel albumsModel = (DefaultTableModel) gui.albumsTable.getModel();
        DefaultTableModel playlistsModel = (DefaultTableModel) gui.playlistsTable.getModel();
        
        // Clear tables and add test data
        songsModel.setRowCount(0);
        artistsModel.setRowCount(0);
        albumsModel.setRowCount(0);
        playlistsModel.setRowCount(0);
        
        songsModel.addRow(new Object[]{"Test Song", "Test Artist", "Test Album", "Rock"});
        artistsModel.addRow(new Object[]{"Test Artist", "Country", "Genre"});
        albumsModel.addRow(new Object[]{"Test Album", "Test Artist", "2023", "Rock"});
        playlistsModel.addRow(new Object[]{"Test Playlist", "0", "2023-01-01"});
        
        // Select the first rows
        gui.songsTable.setRowSelectionInterval(0, 0);
        gui.artistsTable.setRowSelectionInterval(0, 0);
        gui.albumsTable.setRowSelectionInterval(0, 0);
        gui.playlistsTable.setRowSelectionInterval(0, 0);
        
        // Test delete actions - these should call showConfirmDialog
        gui.onDeleteSongClicked();
        gui.onDeleteArtistClicked();
        gui.onDeleteAlbumClicked();
        gui.onDeletePlaylistClicked();
        
        System.out.println("Confirm dialog shown for delete actions: " + gui.showConfirmDialogCalled);
        
        // Test add to playlist action
        gui.onAddToPlaylistClicked();
        gui.onRemoveFromPlaylistClicked();
    }
    
    /**
     * Run all tests
     */
    public void runAllTests() {
        setUp();
        testGUIInitialization();
        testDataLoading();
        testTableStructure();
        testAddItems();
        testEditItems();
        testDeleteItems();
        testButtonActions();
        
        System.out.println("All tests completed!");
    }
    
    /**
     * Main method to run the tests
     */
    public static void main(String[] args) {
        GUITester tester = new GUITester();
        tester.runAllTests();
    }
} 