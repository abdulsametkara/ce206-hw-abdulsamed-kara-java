package com.samet.music.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.samet.music.dao.AlbumDAO;
import com.samet.music.dao.ArtistDAO;
import com.samet.music.dao.PlaylistDAO;
import com.samet.music.dao.SongDAO;
import com.samet.music.util.DatabaseUtil;

/**
 * MusicLibraryGUI - Main GUI class for the music library application
 * Designed for testability with dependency injection and protected methods
 */
public class MusicLibraryGUI extends JFrame {

	private static final long serialVersionUID = 1L;

    // UI Components
    protected JTabbedPane tabbedPane;
    protected JTable songsTable;
    protected JTable artistsTable;
    protected JTable albumsTable;
    protected JTable playlistsTable;
    protected JLabel statusLabel;
    
    // Buttons for songs tab
    protected JButton btnAddSong;
    protected JButton btnEditSong;
    protected JButton btnDeleteSong;
    
    // Buttons for artists tab
    protected JButton btnAddArtist;
    protected JButton btnEditArtist;
    protected JButton btnDeleteArtist;
    
    // Buttons for albums tab
    protected JButton btnAddAlbum;
    protected JButton btnEditAlbum;
    protected JButton btnDeleteAlbum;
    
    // Buttons for playlists tab
    protected JButton btnCreatePlaylist;
    protected JButton btnEditPlaylist;
    protected JButton btnDeletePlaylist;
    protected JButton btnAddToPlaylist;
    protected JButton btnRemoveFromPlaylist;
    
    // DAOs - protected for testability
	protected SongDAO songDAO;
	protected ArtistDAO artistDAO;
    protected AlbumDAO albumDAO;
	protected PlaylistDAO playlistDAO;
    
    // For testing purposes
    protected boolean initializeDatabase = true;

	/**
     * Main method to launch the application
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
                    // Initialize database
					DatabaseUtil.initializeDatabase();
					
                    // Create and display the GUI
					MusicLibraryGUI frame = new MusicLibraryGUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
     * Default constructor that creates the GUI with default DAOs
	 */
	public MusicLibraryGUI() {
        this(new SongDAO(), new ArtistDAO(), new AlbumDAO(), new PlaylistDAO());
	}
    
    /**
     * Constructor with DAO parameters for dependency injection (testability)
     */
    public MusicLibraryGUI(SongDAO songDAO, ArtistDAO artistDAO, AlbumDAO albumDAO, PlaylistDAO playlistDAO) {
        // Set DAOs
		this.songDAO = songDAO;
		this.artistDAO = artistDAO;
        this.albumDAO = albumDAO;
		this.playlistDAO = playlistDAO;
		
        // Initialize database if needed (can be disabled for testing)
		if (initializeDatabase) {
			DatabaseUtil.initializeDatabase();
		}
		
        // Set up the frame
        setupFrame();
        
        // Create UI components
        createUIComponents();
        
        // Load data from database
        loadDataFromDatabase();
        
        // Add window listener to close database connection when window is closed
		addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeDatabase();
            }
        });
    }
    
    /**
     * Set up the main frame properties
     */
    protected void setupFrame() {
        setTitle("Music Library Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 900, 600);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout(0, 0));
    }
    
    /**
     * Create all UI components
     */
    protected void createUIComponents() {
        // Create tabbed pane
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        
        // Create panels for each tab
        createSongsPanel();
        createArtistsPanel();
        createAlbumsPanel();
        createPlaylistsPanel();
        
        // Create status bar
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Status: Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusPanel.add(statusLabel);
        getContentPane().add(statusPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Create the Songs panel and add it to the tabbed pane
     */
    protected void createSongsPanel() {
        JPanel songsPanel = new JPanel();
        songsPanel.setLayout(new BorderLayout(0, 0));
        tabbedPane.addTab("Songs", null, songsPanel, null);
        
        // Create songs table
        songsTable = new JTable();
        DefaultTableModel songsModel = new DefaultTableModel(
                new Object[][] {},
                new String[] {"Title", "Artist", "Album", "Genre"}
        ) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        songsTable.setModel(songsModel);
        JScrollPane scrollPane = new JScrollPane(songsTable);
        songsPanel.add(scrollPane, BorderLayout.NORTH);
        
        // Create buttons panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        songsPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        // Create buttons
        btnAddSong = new JButton("Add Song");
        btnEditSong = new JButton("Edit Song");
        btnDeleteSong = new JButton("Delete Song");
        
        // Add action listeners
        btnAddSong.addActionListener(e -> onAddSongClicked());
        btnEditSong.addActionListener(e -> onEditSongClicked());
        btnDeleteSong.addActionListener(e -> onDeleteSongClicked());
        
        // Add buttons to panel
        buttonsPanel.add(btnAddSong);
        buttonsPanel.add(btnEditSong);
        buttonsPanel.add(btnDeleteSong);
    }
    
    /**
     * Create the Artists panel and add it to the tabbed pane
     */
    protected void createArtistsPanel() {
        JPanel artistsPanel = new JPanel();
        artistsPanel.setLayout(new BorderLayout(0, 0));
        tabbedPane.addTab("Artists", null, artistsPanel, null);
        
        // Create artists table
        artistsTable = new JTable();
        DefaultTableModel artistsModel = new DefaultTableModel(
                new Object[][] {},
                new String[] {"Name", "Country", "Genre"}
        ) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        artistsTable.setModel(artistsModel);
        JScrollPane scrollPane = new JScrollPane(artistsTable);
        artistsPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Create buttons panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        artistsPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        // Create buttons
        btnAddArtist = new JButton("Add Artist");
        btnEditArtist = new JButton("Edit Artist");
        btnDeleteArtist = new JButton("Delete Artist");
        
        // Add action listeners
        btnAddArtist.addActionListener(e -> onAddArtistClicked());
        btnEditArtist.addActionListener(e -> onEditArtistClicked());
        btnDeleteArtist.addActionListener(e -> onDeleteArtistClicked());
        
        // Add buttons to panel
        buttonsPanel.add(btnAddArtist);
        buttonsPanel.add(btnEditArtist);
        buttonsPanel.add(btnDeleteArtist);
    }
    
    /**
     * Create the Albums panel and add it to the tabbed pane
     */
    protected void createAlbumsPanel() {
        JPanel albumsPanel = new JPanel();
        albumsPanel.setLayout(new BorderLayout(0, 0));
        tabbedPane.addTab("Albums", null, albumsPanel, null);
        
        // Create albums table
        albumsTable = new JTable();
        DefaultTableModel albumsModel = new DefaultTableModel(
                new Object[][] {},
                new String[] {"Title", "Artist", "Year", "Genre"}
        ) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        albumsTable.setModel(albumsModel);
        JScrollPane scrollPane = new JScrollPane(albumsTable);
        albumsPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Create buttons panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        albumsPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        // Create buttons
        btnAddAlbum = new JButton("Add Album");
        btnEditAlbum = new JButton("Edit Album");
        btnDeleteAlbum = new JButton("Delete Album");
        
        // Add action listeners
        btnAddAlbum.addActionListener(e -> onAddAlbumClicked());
        btnEditAlbum.addActionListener(e -> onEditAlbumClicked());
        btnDeleteAlbum.addActionListener(e -> onDeleteAlbumClicked());
        
        // Add buttons to panel
        buttonsPanel.add(btnAddAlbum);
        buttonsPanel.add(btnEditAlbum);
        buttonsPanel.add(btnDeleteAlbum);
    }
    
    /**
     * Create the Playlists panel and add it to the tabbed pane
     */
    protected void createPlaylistsPanel() {
        JPanel playlistsPanel = new JPanel();
        playlistsPanel.setLayout(new BorderLayout(0, 0));
        tabbedPane.addTab("Playlists", null, playlistsPanel, null);
        
        // Create playlists table
		playlistsTable = new JTable();
        DefaultTableModel playlistsModel = new DefaultTableModel(
			new Object[][] {},
                new String[] {"Name", "Song Count", "Created Date"}
        ) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        playlistsTable.setModel(playlistsModel);
        JScrollPane scrollPane = new JScrollPane(playlistsTable);
        playlistsPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Create buttons panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        playlistsPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        // Create buttons
		btnCreatePlaylist = new JButton("Create Playlist");
        btnEditPlaylist = new JButton("Edit Playlist");
        btnDeletePlaylist = new JButton("Delete Playlist");
        btnAddToPlaylist = new JButton("Add to Playlist");
        btnRemoveFromPlaylist = new JButton("Remove from Playlist");
        
        // Add action listeners
        btnCreatePlaylist.addActionListener(e -> onCreatePlaylistClicked());
        btnEditPlaylist.addActionListener(e -> onEditPlaylistClicked());
        btnDeletePlaylist.addActionListener(e -> onDeletePlaylistClicked());
        btnAddToPlaylist.addActionListener(e -> onAddToPlaylistClicked());
        btnRemoveFromPlaylist.addActionListener(e -> onRemoveFromPlaylistClicked());
        
        // Add buttons to panel
        buttonsPanel.add(btnCreatePlaylist);
        buttonsPanel.add(btnEditPlaylist);
        buttonsPanel.add(btnDeletePlaylist);
        buttonsPanel.add(btnAddToPlaylist);
        buttonsPanel.add(btnRemoveFromPlaylist);
    }
    
    /**
     * Load data from database into tables
     */
    protected void loadDataFromDatabase() {
        try {
            // Load songs
            DefaultTableModel songsModel = (DefaultTableModel) songsTable.getModel();
            songsModel.setRowCount(0); // Clear existing data
            for (String[] song : songDAO.getAllSongs()) {
                songsModel.addRow(song);
            }
            
            // Load artists
            DefaultTableModel artistsModel = (DefaultTableModel) artistsTable.getModel();
            artistsModel.setRowCount(0);
            for (String[] artist : artistDAO.getAllArtists()) {
                artistsModel.addRow(artist);
            }
            
            // Load albums
            DefaultTableModel albumsModel = (DefaultTableModel) albumsTable.getModel();
            albumsModel.setRowCount(0);
            for (String[] album : albumDAO.getAllAlbums()) {
                albumsModel.addRow(album);
            }
            
            // Load playlists
            DefaultTableModel playlistsModel = (DefaultTableModel) playlistsTable.getModel();
            playlistsModel.setRowCount(0);
            for (String[] playlist : playlistDAO.getAllPlaylists()) {
                playlistsModel.addRow(playlist);
            }
            
            updateStatusBar("Data loaded successfully");
        } catch (Exception e) {
            showErrorMessage("Error loading data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Close database connection when application is closing
     */
    protected void closeDatabase() {
        try {
            if (initializeDatabase) {
                DatabaseUtil.closeConnection();
                System.out.println("Database connection closed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Update the status bar with a message
     */
    protected void updateStatusBar(String message) {
        statusLabel.setText("Status: " + message);
    }
    
    /**
     * Show an error message dialog
     */
    protected void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Show an information message dialog
     */
    protected void showInfoMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show a confirmation dialog
     * @return true if user confirms, false otherwise
     */
    protected boolean showConfirmDialog(String message) {
        int result = JOptionPane.showConfirmDialog(this, message, "Confirm", JOptionPane.YES_NO_OPTION);
        return result == JOptionPane.YES_OPTION;
    }
    
    //=================== SONGS TAB ACTIONS ===================//
    
    /**
     * Handle Add Song button click
     */
    protected void onAddSongClicked() {
        try {
            // Create input fields
            JPanel panel = new JPanel();
            panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
            
            JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            titlePanel.add(new JLabel("Title:"));
            javax.swing.JTextField titleField = new javax.swing.JTextField(20);
            titlePanel.add(titleField);
            panel.add(titlePanel);
            
            JPanel artistPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            artistPanel.add(new JLabel("Artist:"));
            javax.swing.JTextField artistField = new javax.swing.JTextField(20);
            artistPanel.add(artistField);
            panel.add(artistPanel);
            
            JPanel albumPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            albumPanel.add(new JLabel("Album:"));
            javax.swing.JTextField albumField = new javax.swing.JTextField(20);
            albumPanel.add(albumField);
            panel.add(albumPanel);
            
            JPanel genrePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            genrePanel.add(new JLabel("Genre:"));
            javax.swing.JTextField genreField = new javax.swing.JTextField(20);
            genrePanel.add(genreField);
            panel.add(genrePanel);
            
            // Show dialog
            int result = JOptionPane.showConfirmDialog(this, panel, "Add Song", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String title = titleField.getText().trim();
                String artist = artistField.getText().trim();
                String album = albumField.getText().trim();
                String genre = genreField.getText().trim();
                
                // Validate input
                if (title.isEmpty() || artist.isEmpty() || album.isEmpty() || genre.isEmpty()) {
                    showErrorMessage("All fields are required");
                    return;
                }
                
                try {
                    // Add song to database
                    songDAO.addSong(title, artist, album, genre);
                    
                    // Update table
                    DefaultTableModel model = (DefaultTableModel) songsTable.getModel();
                    model.addRow(new Object[] {title, artist, album, genre});
                    updateStatusBar("Song added successfully");
                } catch (Exception ex) {
                    showErrorMessage("Failed to add song to database: " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            showErrorMessage("Error adding song: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle Edit Song button click
     */
    protected void onEditSongClicked() {
        int selectedRow = songsTable.getSelectedRow();
        if (selectedRow == -1) {
            showErrorMessage("Please select a song to edit");
            return;
        }
        
        try {
            // Get selected song data
            DefaultTableModel model = (DefaultTableModel) songsTable.getModel();
            String oldTitle = (String) model.getValueAt(selectedRow, 0);
            String oldArtist = (String) model.getValueAt(selectedRow, 1);
            String oldAlbum = (String) model.getValueAt(selectedRow, 2);
            String oldGenre = (String) model.getValueAt(selectedRow, 3);
            
            // Create input fields
            JPanel panel = new JPanel();
            panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
            
            JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            titlePanel.add(new JLabel("Title:"));
            javax.swing.JTextField titleField = new javax.swing.JTextField(oldTitle, 20);
            titlePanel.add(titleField);
            panel.add(titlePanel);
            
            JPanel artistPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            artistPanel.add(new JLabel("Artist:"));
            javax.swing.JTextField artistField = new javax.swing.JTextField(oldArtist, 20);
            artistPanel.add(artistField);
            panel.add(artistPanel);
            
            JPanel albumPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            albumPanel.add(new JLabel("Album:"));
            javax.swing.JTextField albumField = new javax.swing.JTextField(oldAlbum, 20);
            albumPanel.add(albumField);
            panel.add(albumPanel);
            
            JPanel genrePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            genrePanel.add(new JLabel("Genre:"));
            javax.swing.JTextField genreField = new javax.swing.JTextField(oldGenre, 20);
            genrePanel.add(genreField);
            panel.add(genrePanel);
            
            // Show dialog
            int result = JOptionPane.showConfirmDialog(this, panel, "Edit Song", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String newTitle = titleField.getText().trim();
                String newArtist = artistField.getText().trim();
                String newAlbum = albumField.getText().trim();
                String newGenre = genreField.getText().trim();
                
                // Validate input
                if (newTitle.isEmpty() || newArtist.isEmpty() || newAlbum.isEmpty() || newGenre.isEmpty()) {
                    showErrorMessage("All fields are required");
					return;
				}
				
                // Update song in database
                boolean success = songDAO.updateSong(oldTitle, oldArtist, oldAlbum, newTitle, newArtist, newAlbum, newGenre);
                
                if (success) {
                    // Update table
                    model.setValueAt(newTitle, selectedRow, 0);
                    model.setValueAt(newArtist, selectedRow, 1);
                    model.setValueAt(newAlbum, selectedRow, 2);
                    model.setValueAt(newGenre, selectedRow, 3);
                    updateStatusBar("Song updated successfully");
                } else {
                    showErrorMessage("Failed to update song in database");
                }
            }
        } catch (Exception e) {
            showErrorMessage("Error updating song: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle Delete Song button click
     */
    protected void onDeleteSongClicked() {
        int selectedRow = songsTable.getSelectedRow();
        if (selectedRow == -1) {
            showErrorMessage("Please select a song to delete");
            return;
        }
        
        try {
            // Get selected song data
            DefaultTableModel model = (DefaultTableModel) songsTable.getModel();
            String title = (String) model.getValueAt(selectedRow, 0);
            String artist = (String) model.getValueAt(selectedRow, 1);
            String album = (String) model.getValueAt(selectedRow, 2);
            
            // Confirm deletion
            boolean confirm = showConfirmDialog("Are you sure you want to delete the song '" + title + "' by '" + artist + "'?");
            if (confirm) {
                // Delete song from database
                try {
                    songDAO.deleteSong(title, artist, album);
                    
                    // Remove row from table
                    model.removeRow(selectedRow);
                    updateStatusBar("Song deleted successfully");
                } catch (Exception ex) {
                    showErrorMessage("Failed to delete song from database: " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            showErrorMessage("Error deleting song: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    //=================== ARTISTS TAB ACTIONS ===================//
    
    /**
     * Handle Add Artist button click
     */
    protected void onAddArtistClicked() {
        try {
            // Create input fields
            JPanel panel = new JPanel();
            panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
            
            JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            namePanel.add(new JLabel("Name:"));
            javax.swing.JTextField nameField = new javax.swing.JTextField(20);
            namePanel.add(nameField);
            panel.add(namePanel);
            
            JPanel countryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            countryPanel.add(new JLabel("Country:"));
            javax.swing.JTextField countryField = new javax.swing.JTextField(20);
            countryPanel.add(countryField);
            panel.add(countryPanel);
            
            JPanel genrePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            genrePanel.add(new JLabel("Genre:"));
            javax.swing.JTextField genreField = new javax.swing.JTextField(20);
            genrePanel.add(genreField);
            panel.add(genrePanel);
            
            // Show dialog
            int result = JOptionPane.showConfirmDialog(this, panel, "Add Artist", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
					String name = nameField.getText().trim();
					String country = countryField.getText().trim();
					String genre = genreField.getText().trim();
					
                // Validate input
					if (name.isEmpty() || country.isEmpty() || genre.isEmpty()) {
                    showErrorMessage("All fields are required");
						return;
					}
					
                // Add artist to database
						boolean success = artistDAO.addArtist(name, country, genre, 1);
						
						if (success) {
                    // Update table
                    DefaultTableModel model = (DefaultTableModel) artistsTable.getModel();
                    model.addRow(new Object[] {name, country, genre});
                    updateStatusBar("Artist added successfully");
						} else {
                    showErrorMessage("Failed to add artist to database");
                }
            }
        } catch (Exception e) {
            showErrorMessage("Error adding artist: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle Edit Artist button click
     */
    protected void onEditArtistClicked() {
        int selectedRow = artistsTable.getSelectedRow();
        if (selectedRow == -1) {
            showErrorMessage("Please select an artist to edit");
				return;
			}
        
        try {
            // Get selected artist data
            DefaultTableModel model = (DefaultTableModel) artistsTable.getModel();
            String oldName = (String) model.getValueAt(selectedRow, 0);
            String oldCountry = (String) model.getValueAt(selectedRow, 1);
            String oldGenre = (String) model.getValueAt(selectedRow, 2);
            
            // Create input fields
            JPanel panel = new JPanel();
            panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
            
            JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            namePanel.add(new JLabel("Name:"));
            javax.swing.JTextField nameField = new javax.swing.JTextField(oldName, 20);
            namePanel.add(nameField);
            panel.add(namePanel);
            
            JPanel countryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            countryPanel.add(new JLabel("Country:"));
            javax.swing.JTextField countryField = new javax.swing.JTextField(oldCountry, 20);
            countryPanel.add(countryField);
            panel.add(countryPanel);
            
            JPanel genrePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            genrePanel.add(new JLabel("Genre:"));
            javax.swing.JTextField genreField = new javax.swing.JTextField(oldGenre, 20);
            genrePanel.add(genreField);
            panel.add(genrePanel);
            
            // Show dialog
            int result = JOptionPane.showConfirmDialog(this, panel, "Edit Artist", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
				String newName = nameField.getText().trim();
				String newCountry = countryField.getText().trim();
				String newGenre = genreField.getText().trim();
				
                // Validate input
                if (newName.isEmpty() || newCountry.isEmpty() || newGenre.isEmpty()) {
                    showErrorMessage("All fields are required");
                    return;
                }
                
                // Update artist in database
					boolean success = artistDAO.updateArtist(oldName, newName, newCountry, newGenre);
					
					if (success) {
                    // Update table
                    model.setValueAt(newName, selectedRow, 0);
                    model.setValueAt(newCountry, selectedRow, 1);
                    model.setValueAt(newGenre, selectedRow, 2);
                    updateStatusBar("Artist updated successfully");
					} else {
                    showErrorMessage("Failed to update artist in database");
                }
            }
        } catch (Exception e) {
            showErrorMessage("Error updating artist: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle Delete Artist button click
     */
    protected void onDeleteArtistClicked() {
        int selectedRow = artistsTable.getSelectedRow();
        if (selectedRow == -1) {
            showErrorMessage("Please select an artist to delete");
				return;
			}
        
        try {
            // Get selected artist data
            DefaultTableModel model = (DefaultTableModel) artistsTable.getModel();
            String name = (String) model.getValueAt(selectedRow, 0);
            
            // Confirm deletion
            boolean confirm = showConfirmDialog("Are you sure you want to delete the artist '" + name + "'?");
            if (confirm) {
                // Delete artist from database
					boolean success = artistDAO.deleteArtist(name);
					
					if (success) {
                    // Remove row from table
                    model.removeRow(selectedRow);
                    updateStatusBar("Artist deleted successfully");
					} else {
                    showErrorMessage("Failed to delete artist from database");
                }
            }
        } catch (Exception e) {
            showErrorMessage("Error deleting artist: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    //=================== ALBUMS TAB ACTIONS ===================//
    
    /**
     * Handle Add Album button click
     */
    protected void onAddAlbumClicked() {
        try {
            // Create input fields
            JPanel panel = new JPanel();
            panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
            
            JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            titlePanel.add(new JLabel("Title:"));
            javax.swing.JTextField titleField = new javax.swing.JTextField(20);
            titlePanel.add(titleField);
            panel.add(titlePanel);
            
            JPanel artistPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            artistPanel.add(new JLabel("Artist:"));
            javax.swing.JTextField artistField = new javax.swing.JTextField(20);
            artistPanel.add(artistField);
            panel.add(artistPanel);
            
            JPanel yearPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            yearPanel.add(new JLabel("Year:"));
            javax.swing.JTextField yearField = new javax.swing.JTextField(
                    String.valueOf(java.time.LocalDate.now().getYear()), 20);
            yearPanel.add(yearField);
            panel.add(yearPanel);
            
            JPanel genrePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            genrePanel.add(new JLabel("Genre:"));
            javax.swing.JTextField genreField = new javax.swing.JTextField(20);
            genrePanel.add(genreField);
            panel.add(genrePanel);
            
            // Show dialog
            int result = JOptionPane.showConfirmDialog(this, panel, "Add Album", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
				String title = titleField.getText().trim();
				String artist = artistField.getText().trim();
				String year = yearField.getText().trim();
				String genre = genreField.getText().trim();
				
                // Validate input
				if (title.isEmpty() || artist.isEmpty() || year.isEmpty() || genre.isEmpty()) {
                    showErrorMessage("All fields are required");
					return;
				}
				
                // Add album to database
					boolean success = albumDAO.addAlbum(title, artist, year, genre, 1);
					
					if (success) {
                    // Update table
                    DefaultTableModel model = (DefaultTableModel) albumsTable.getModel();
						model.addRow(new Object[] {title, artist, year, genre});
                    updateStatusBar("Album added successfully");
					} else {
                    showErrorMessage("Failed to add album to database");
                }
            }
        } catch (Exception e) {
            showErrorMessage("Error adding album: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle Edit Album button click
     */
    protected void onEditAlbumClicked() {
        int selectedRow = albumsTable.getSelectedRow();
        if (selectedRow == -1) {
            showErrorMessage("Please select an album to edit");
				return;
			}
			
        try {
            // Get selected album data
            DefaultTableModel model = (DefaultTableModel) albumsTable.getModel();
            String oldTitle = (String) model.getValueAt(selectedRow, 0);
            String oldArtist = (String) model.getValueAt(selectedRow, 1);
            String oldYear = (String) model.getValueAt(selectedRow, 2);
            String oldGenre = (String) model.getValueAt(selectedRow, 3);
            
            // Create input fields
            JPanel panel = new JPanel();
            panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
            
            JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            titlePanel.add(new JLabel("Title:"));
            javax.swing.JTextField titleField = new javax.swing.JTextField(oldTitle, 20);
            titlePanel.add(titleField);
            panel.add(titlePanel);
            
            JPanel artistPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            artistPanel.add(new JLabel("Artist:"));
            javax.swing.JTextField artistField = new javax.swing.JTextField(oldArtist, 20);
            artistPanel.add(artistField);
            panel.add(artistPanel);
            
            JPanel yearPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            yearPanel.add(new JLabel("Year:"));
            javax.swing.JTextField yearField = new javax.swing.JTextField(oldYear, 20);
            yearPanel.add(yearField);
            panel.add(yearPanel);
            
            JPanel genrePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            genrePanel.add(new JLabel("Genre:"));
            javax.swing.JTextField genreField = new javax.swing.JTextField(oldGenre, 20);
            genrePanel.add(genreField);
            panel.add(genrePanel);
            
            // Show dialog
            int result = JOptionPane.showConfirmDialog(this, panel, "Edit Album", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
				String newTitle = titleField.getText().trim();
				String newArtist = artistField.getText().trim();
				String newYear = yearField.getText().trim();
				String newGenre = genreField.getText().trim();
				
                // Validate input
                if (newTitle.isEmpty() || newArtist.isEmpty() || newYear.isEmpty() || newGenre.isEmpty()) {
                    showErrorMessage("All fields are required");
                    return;
                }
                
                // Update album in database
					boolean success = albumDAO.updateAlbum(oldTitle, oldArtist, newTitle, newArtist, newYear, newGenre);
					
					if (success) {
                    // Update table
                    model.setValueAt(newTitle, selectedRow, 0);
                    model.setValueAt(newArtist, selectedRow, 1);
                    model.setValueAt(newYear, selectedRow, 2);
                    model.setValueAt(newGenre, selectedRow, 3);
                    updateStatusBar("Album updated successfully");
					} else {
                    showErrorMessage("Failed to update album in database");
                }
            }
        } catch (Exception e) {
            showErrorMessage("Error updating album: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle Delete Album button click
     */
    protected void onDeleteAlbumClicked() {
        int selectedRow = albumsTable.getSelectedRow();
        if (selectedRow == -1) {
            showErrorMessage("Please select an album to delete");
				return;
			}
        
        try {
            // Get selected album data
            DefaultTableModel model = (DefaultTableModel) albumsTable.getModel();
            String title = (String) model.getValueAt(selectedRow, 0);
            String artist = (String) model.getValueAt(selectedRow, 1);
            
            // Confirm deletion
            boolean confirm = showConfirmDialog("Are you sure you want to delete the album '" + title + "' by '" + artist + "'?");
            if (confirm) {
                // Delete album from database
					boolean success = albumDAO.deleteAlbum(title, artist);
					
					if (success) {
                    // Remove row from table
                    model.removeRow(selectedRow);
                    updateStatusBar("Album deleted successfully");
					} else {
                    showErrorMessage("Failed to delete album from database");
                }
            }
        } catch (Exception e) {
            showErrorMessage("Error deleting album: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    //=================== PLAYLISTS TAB ACTIONS ===================//
    
    /**
     * Handle Create Playlist button click
     */
    protected void onCreatePlaylistClicked() {
        try {
            // Create input fields
            JPanel panel = new JPanel();
            panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
            
            JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            namePanel.add(new JLabel("Name:"));
            javax.swing.JTextField nameField = new javax.swing.JTextField(20);
            namePanel.add(nameField);
            panel.add(namePanel);
            
            // Show dialog
            int result = JOptionPane.showConfirmDialog(this, panel, "Create Playlist", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String name = nameField.getText().trim();
                
                // Validate input
                if (name.isEmpty()) {
                    showErrorMessage("Playlist name is required");
				return;
			}
			
                // Add playlist to database
                boolean success = playlistDAO.addPlaylist(name, "", 1);
					
					if (success) {
                    // Update table
                    DefaultTableModel model = (DefaultTableModel) playlistsTable.getModel();
                    model.addRow(new Object[] {name, "0", java.time.LocalDate.now().toString()});
                    updateStatusBar("Playlist created successfully");
					} else {
                    showErrorMessage("Failed to create playlist in database");
                }
            }
        } catch (Exception e) {
            showErrorMessage("Error creating playlist: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle Edit Playlist button click
     */
    protected void onEditPlaylistClicked() {
        int selectedRow = playlistsTable.getSelectedRow();
        if (selectedRow == -1) {
            showErrorMessage("Please select a playlist to edit");
				return;
			}
			
			try {
            // Get selected playlist data
            DefaultTableModel model = (DefaultTableModel) playlistsTable.getModel();
            String oldName = (String) model.getValueAt(selectedRow, 0);
            
            // Create input fields
            JPanel panel = new JPanel();
            panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
            
            JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            namePanel.add(new JLabel("Name:"));
            javax.swing.JTextField nameField = new javax.swing.JTextField(oldName, 20);
            namePanel.add(nameField);
            panel.add(namePanel);
            
            // Show dialog
            int result = JOptionPane.showConfirmDialog(this, panel, "Edit Playlist", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String newName = nameField.getText().trim();
                
                // Validate input
                if (newName.isEmpty()) {
                    showErrorMessage("Playlist name is required");
                    return;
                }
                
                // Update playlist in database
                boolean success = playlistDAO.updatePlaylist(oldName, newName);
                
                if (success) {
                    // Update table
                    model.setValueAt(newName, selectedRow, 0);
                    updateStatusBar("Playlist updated successfully");
                } else {
                    showErrorMessage("Failed to update playlist in database");
                }
            }
        } catch (Exception e) {
            showErrorMessage("Error updating playlist: " + e.getMessage());
            e.printStackTrace();
		}
	}
	
	/**
     * Handle Delete Playlist button click
     */
    protected void onDeletePlaylistClicked() {
        int selectedRow = playlistsTable.getSelectedRow();
        if (selectedRow == -1) {
            showErrorMessage("Please select a playlist to delete");
			return;
		}
        
        try {
            // Get selected playlist data
            DefaultTableModel model = (DefaultTableModel) playlistsTable.getModel();
            String name = (String) model.getValueAt(selectedRow, 0);
            
            // Confirm deletion
            boolean confirm = showConfirmDialog("Are you sure you want to delete the playlist '" + name + "'?");
            if (confirm) {
                // Delete playlist from database
                boolean success = playlistDAO.deletePlaylist(name);
                
                if (success) {
                    // Remove row from table
                    model.removeRow(selectedRow);
                    updateStatusBar("Playlist deleted successfully");
		        } else {
                    showErrorMessage("Failed to delete playlist from database");
		        }
		    }
        } catch (Exception e) {
            showErrorMessage("Error deleting playlist: " + e.getMessage());
            e.printStackTrace();
	    }
	}
	
	/**
     * Handle Add to Playlist button click
     */
    protected void onAddToPlaylistClicked() {
        int selectedRow = playlistsTable.getSelectedRow();
        if (selectedRow == -1) {
            showErrorMessage("Please select a playlist first");
			return;
		}
        
        try {
            // Get selected playlist
            DefaultTableModel playlistModel = (DefaultTableModel) playlistsTable.getModel();
            String playlistName = (String) playlistModel.getValueAt(selectedRow, 0);
            
            // Get all songs for selection
            DefaultTableModel songsModel = (DefaultTableModel) songsTable.getModel();
            int songCount = songsModel.getRowCount();
            
            if (songCount == 0) {
                showErrorMessage("No songs available to add to playlist");
                return;
            }
            
            // Create a list of songs for selection
            String[] songs = new String[songCount];
            for (int i = 0; i < songCount; i++) {
                String title = (String) songsModel.getValueAt(i, 0);
                String artist = (String) songsModel.getValueAt(i, 1);
                songs[i] = title + " - " + artist;
            }
            
            // Show selection dialog
            String selected = (String) JOptionPane.showInputDialog(
                    this,
                    "Select a song to add to playlist '" + playlistName + "':",
                    "Add to Playlist",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    songs,
                    songs[0]);
            
            if (selected != null) {
                // Extract song info
                String[] parts = selected.split(" - ");
                String title = parts[0];
                String artist = parts[1];
                
                // Update playlist song count
                int currentCount = Integer.parseInt(playlistModel.getValueAt(selectedRow, 1).toString());
                playlistModel.setValueAt(Integer.toString(currentCount + 1), selectedRow, 1);
                
                updateStatusBar("Added '" + selected + "' to playlist '" + playlistName + "'");
            }
        } catch (Exception e) {
            showErrorMessage("Error adding song to playlist: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle Remove from Playlist button click
     */
    protected void onRemoveFromPlaylistClicked() {
        int selectedRow = playlistsTable.getSelectedRow();
        if (selectedRow == -1) {
            showErrorMessage("Please select a playlist first");
				return;
			}
			
			try {
            // Get selected playlist
            DefaultTableModel playlistModel = (DefaultTableModel) playlistsTable.getModel();
            String playlistName = (String) playlistModel.getValueAt(selectedRow, 0);
            int currentCount = Integer.parseInt(playlistModel.getValueAt(selectedRow, 1).toString());
            
            if (currentCount <= 0) {
                showErrorMessage("No songs in playlist to remove");
                return;
            }
            
            // For simplicity, just ask for song name (in a real app, we would show playlist songs)
            String songName = JOptionPane.showInputDialog(
                    this,
                    "Enter song name to remove from playlist '" + playlistName + "':",
                    "Remove from Playlist",
                    JOptionPane.QUESTION_MESSAGE);
            
            if (songName != null && !songName.trim().isEmpty()) {
                // Update playlist song count
                playlistModel.setValueAt(Integer.toString(currentCount - 1), selectedRow, 1);
                updateStatusBar("Removed '" + songName + "' from playlist '" + playlistName + "'");
            }
        } catch (Exception e) {
            showErrorMessage("Error removing song from playlist: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 