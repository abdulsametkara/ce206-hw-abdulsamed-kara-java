package com.samet.music.gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JButton;
import com.samet.music.dao.SongDAO;
import com.samet.music.dao.AlbumDAO;
import com.samet.music.dao.ArtistDAO;
import com.samet.music.dao.PlaylistDAO;
import com.samet.music.util.DatabaseUtil;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MusicLibraryGUI extends JFrame {

	private static final long serialVersionUID = 1L;

	private JTable allSongsTable;
	private javax.swing.JLabel statusBar;
	private JTable playlistsTable;
	private JTable artistsTable;
	private JButton btnAdd;
	private JButton btnEditAllSongs;
	private JButton btnDeleteAllSongs;
	private JButton btnCreatePlaylist;
	private JButton btnMetadata;
	private JButton btnAddToPlaylist;
	private JButton btnRemoveFromPlaylist;
	private JButton btnAddArtist;
	private JButton btnEditPlaylists;
	private JButton btnDeletePlaylists;
	private JButton btnEditArtists;
	private JButton btnDeleteArtists;
	private SongDAO songDAO;
	private AlbumDAO albumDAO;
	private ArtistDAO artistDAO;
	private PlaylistDAO playlistDAO;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					// Initialize database first
					DatabaseUtil.initializeDatabase();
					
					MusicLibraryGUI frame = new MusicLibraryGUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MusicLibraryGUI() {
		// Initialize database connection
		DatabaseUtil.initializeDatabase();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1000, 700);
		setLocationRelativeTo(null);
		
		// Add window closing listener to ensure database connection is closed properly
		addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DatabaseUtil.closeConnection();
                System.out.println("Database connection closed on window closing");
            }
        });
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnNewMenu = new JMenu("File");
		menuBar.add(mnNewMenu);
		
		JMenuItem mntmNewMenuItem = new JMenuItem("Import");
		mnNewMenu.add(mntmNewMenuItem);
		
		JMenuItem mntmNewMenuItem_1 = new JMenuItem("Export");
		mnNewMenu.add(mntmNewMenuItem_1);
		
		JMenuItem mntmNewMenuItem_2 = new JMenuItem("Exit");
		mnNewMenu.add(mntmNewMenuItem_2);
		
		JMenu mnNewMenu_1 = new JMenu("Edit");
		menuBar.add(mnNewMenu_1);
		
		JMenuItem mntmNewMenuItem_3 = new JMenuItem("Preferences");
		mnNewMenu_1.add(mntmNewMenuItem_3);
		
		JMenuItem mntmNewMenuItem_4 = new JMenuItem("User Settings");
		mnNewMenu_1.add(mntmNewMenuItem_4);
		
		JMenu mnNewMenu_2 = new JMenu("View");
		menuBar.add(mnNewMenu_2);
		
		JMenuItem mntmNewMenuItem_7 = new JMenuItem("All Songs");
		mnNewMenu_2.add(mntmNewMenuItem_7);
		
		JMenuItem mntmNewMenuItem_8 = new JMenuItem("Albums");
		mnNewMenu_2.add(mntmNewMenuItem_8);
		
		JMenuItem mntmNewMenuItem_9 = new JMenuItem("Artists");
		mnNewMenu_2.add(mntmNewMenuItem_9);
		
		JMenuItem mntmNewMenuItem_10 = new JMenuItem("Playlists");
		mnNewMenu_2.add(mntmNewMenuItem_10);
		
		JMenuItem mntmNewMenuItem_11 = new JMenuItem("Recommendations");
		mnNewMenu_2.add(mntmNewMenuItem_11);
		
		JMenuItem mntmNewMenuItem_12 = new JMenuItem("Theme (Açık/Koyu)");
		mnNewMenu_2.add(mntmNewMenuItem_12);
		
		JMenu mnNewMenu_3 = new JMenu("Playlists");
		menuBar.add(mnNewMenu_3);
		
		JMenuItem mntmNewMenuItem_13 = new JMenuItem("Create Playlist");
		mnNewMenu_3.add(mntmNewMenuItem_13);
		
		JMenuItem mntmNewMenuItem_14 = new JMenuItem("Delete Playlist");
		mnNewMenu_3.add(mntmNewMenuItem_14);
		
		JMenuItem mntmNewMenuItem_15 = new JMenuItem("Add Song to Playlist");
		mnNewMenu_3.add(mntmNewMenuItem_15);
		
		JMenuItem mntmNewMenuItem_16 = new JMenuItem("Remove Song from Playlist");
		mnNewMenu_3.add(mntmNewMenuItem_16);
		
		JMenu mnNewMenu_4 = new JMenu("Help");
		menuBar.add(mnNewMenu_4);
		
		JMenuItem mntmNewMenuItem_5 = new JMenuItem("About");
		mnNewMenu_4.add(mntmNewMenuItem_5);
		
		JMenuItem mntmNewMenuItem_6 = new JMenuItem("Documentation");
		mnNewMenu_4.add(mntmNewMenuItem_6);
		
		JSplitPane splitPane = new JSplitPane();
		getContentPane().add(splitPane, BorderLayout.CENTER);
		
		// Sol panel: Navigasyon
		JPanel panel = new JPanel(new BorderLayout());
		splitPane.setLeftComponent(panel);
		
		JList<String> list = new JList<>(new String[] {
			"All Songs", "Albums", "Artists", "Playlists", "Recommendations"
		});
		panel.add(list, BorderLayout.CENTER);
		
		// Sağ panel: İçerik + araç çubuğu
		JPanel panel_1 = new JPanel(new BorderLayout());
		splitPane.setRightComponent(panel_1);
		
		// Sağ üst: Kullanıcı adı ve Logout
		JPanel userPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
		javax.swing.JLabel userLabel = new javax.swing.JLabel("User: samet");
		javax.swing.JButton btnLogout = new javax.swing.JButton("Logout");
		userPanel.add(userLabel);
		userPanel.add(btnLogout);
		getContentPane().add(userPanel, BorderLayout.NORTH);

		// Sağ panel: CardLayout ile içerik panelleri
		JPanel cardPanel = new JPanel(new java.awt.CardLayout());
		// All Songs Panel
		JPanel allSongsPanel = new JPanel(new BorderLayout());
		allSongsTable = new JTable();
		allSongsTable.setModel(new javax.swing.table.DefaultTableModel(
			new Object[][] {},
			new String[] {"Title", "Artist", "Album", "Genre"}
		));
		allSongsPanel.add(new javax.swing.JScrollPane(allSongsTable), BorderLayout.CENTER);
		// Albums Panel
		JPanel albumsPanel = new JPanel(new BorderLayout());
		JTable albumsTable = new JTable();
		albumsTable.setModel(new javax.swing.table.DefaultTableModel(
			new Object[][] {},
			new String[] {"Album Title", "Artist", "Year", "Genre"}
		));
		albumsPanel.add(new javax.swing.JScrollPane(albumsTable), BorderLayout.CENTER);
		// Artists Panel
		JPanel artistsPanel = new JPanel(new BorderLayout());
		artistsTable = new JTable();
		artistsTable.setModel(new javax.swing.table.DefaultTableModel(
			new Object[][] {},
			new String[] {"Artist Name", "Country", "Genre"}
		));
		artistsPanel.add(new javax.swing.JScrollPane(artistsTable), BorderLayout.CENTER);
		// Playlists Panel
		JPanel playlistsPanel = new JPanel(new BorderLayout());
		playlistsTable = new JTable();
		playlistsTable.setModel(new javax.swing.table.DefaultTableModel(
			new Object[][] {},
			new String[] {"Playlist Name", "Song Count", "Created"}
		));
		playlistsPanel.add(new javax.swing.JScrollPane(playlistsTable), BorderLayout.CENTER);
		// Recommendations Panel
		JPanel recommendationsPanel = new JPanel(new BorderLayout());
		JTable recommendationsTable = new JTable();
		recommendationsTable.setModel(new javax.swing.table.DefaultTableModel(
			new Object[][] {},
			new String[] {"Title", "Artist", "Reason"}
		));
		recommendationsPanel.add(new javax.swing.JScrollPane(recommendationsTable), BorderLayout.CENTER);
		// CardPanel'a ekle
		cardPanel.add(allSongsPanel, "All Songs");
		cardPanel.add(albumsPanel, "Albums");
		cardPanel.add(artistsPanel, "Artists");
		cardPanel.add(playlistsPanel, "Playlists");
		cardPanel.add(recommendationsPanel, "Recommendations");
		// Sağ panelin ortasına CardPanel ekle
		panel_1.add(cardPanel, BorderLayout.CENTER);
		
		// Araç çubuğu (butonlar)
		JPanel toolBarPanel = new JPanel();
		btnAdd = new JButton("Add Song");
		btnEditAllSongs = new JButton("Edit");
		btnDeleteAllSongs = new JButton("Delete");
		btnCreatePlaylist = new JButton("Create Playlist");
		btnMetadata = new JButton("Metadata");
		btnAddToPlaylist = new JButton("Add Song to Playlist");
		btnRemoveFromPlaylist = new JButton("Remove Song from Playlist");
		btnAddArtist = new JButton("Add Artist");
		// Eksik olan butonları oluştur
		btnEditPlaylists = new JButton("Edit");
		btnDeletePlaylists = new JButton("Delete");
		btnEditArtists = new JButton("Edit");
		btnDeleteArtists = new JButton("Delete");
		
		// Butonları her panel için ayrı panellere ekle
		JPanel allSongsButtonPanel = new JPanel();
		allSongsButtonPanel.add(btnAdd);
		allSongsButtonPanel.add(btnEditAllSongs);
		allSongsButtonPanel.add(btnDeleteAllSongs);
		allSongsButtonPanel.add(btnMetadata);
		
		JPanel playlistsButtonPanel = new JPanel();
		playlistsButtonPanel.add(btnCreatePlaylist);
		playlistsButtonPanel.add(btnEditPlaylists);
		playlistsButtonPanel.add(btnDeletePlaylists);
		playlistsButtonPanel.add(btnAddToPlaylist);
		playlistsButtonPanel.add(btnRemoveFromPlaylist);
		
		JPanel artistsButtonPanel = new JPanel();
		artistsButtonPanel.add(btnAddArtist);
		artistsButtonPanel.add(btnEditArtists);
		artistsButtonPanel.add(btnDeleteArtists);
		
		// Albums için panel ve butonlar
		JPanel albumsButtonPanel = new JPanel();
		JButton btnAddAlbum = new JButton("Add Album");
		JButton btnEditAlbum = new JButton("Edit Album");
		JButton btnDeleteAlbum = new JButton("Delete Album");
		JButton btnViewSongs = new JButton("View Songs");
		albumsButtonPanel.add(btnAddAlbum);
		albumsButtonPanel.add(btnEditAlbum);
		albumsButtonPanel.add(btnDeleteAlbum);
		albumsButtonPanel.add(btnViewSongs);
		
		// Recommendations için panel
		JPanel recommendationsButtonPanel = new JPanel();
		JButton btnAddToFavorites = new JButton("Add Song");
		JButton btnRefreshRecommendations = new JButton("Refresh");
		recommendationsButtonPanel.add(btnAddToFavorites);
		recommendationsButtonPanel.add(btnRefreshRecommendations);
		
		// CardLayout için buton paneli oluştur
		JPanel buttonCardPanel = new JPanel(new java.awt.CardLayout());
		buttonCardPanel.add(allSongsButtonPanel, "All Songs");
		buttonCardPanel.add(albumsButtonPanel, "Albums");
		buttonCardPanel.add(artistsButtonPanel, "Artists");
		buttonCardPanel.add(playlistsButtonPanel, "Playlists");
		buttonCardPanel.add(recommendationsButtonPanel, "Recommendations");
		
		// Buton panelini ekle
		panel_1.add(buttonCardPanel, BorderLayout.SOUTH);
		
		// Durum çubuğu (en alt)
		statusBar = new javax.swing.JLabel("Status: Ready");
		getContentPane().add(statusBar, BorderLayout.SOUTH);

		// JList seçim değişikliği ile CardLayout panelini değiştir - MOVED AFTER BUTTONS ARE INITIALIZED
		list.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				String selected = list.getSelectedValue();
				// İçerik panelini değiştir
				java.awt.CardLayout cl = (java.awt.CardLayout)(cardPanel.getLayout());
				cl.show(cardPanel, selected);
				
				// Buton panelini değiştir
				java.awt.CardLayout buttonCL = (java.awt.CardLayout)(buttonCardPanel.getLayout());
				buttonCL.show(buttonCardPanel, selected);
				
				// Recommendations sayfasındaysa örnek tavsiyeler KALDIRDIK
			}
		});
		
		// Başlangıçta All Songs seçili olsun
		list.setSelectedIndex(0);

		// Modern ve profesyonel font ve renkler
		java.awt.Font mainFont = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14);
		java.awt.Color bgColor = new java.awt.Color(245, 246, 250); // Açık gri
		java.awt.Color accentColor = new java.awt.Color(52, 152, 219); // Mavi
		java.awt.Color buttonColor = new java.awt.Color(41, 128, 185); // Koyu mavi
		java.awt.Color buttonTextColor = java.awt.Color.WHITE;
		java.awt.Color listSelectionColor = new java.awt.Color(230, 240, 255);

		// Menü bar ve menüler
		menuBar.setFont(mainFont);
		for (int i = 0; i < menuBar.getMenuCount(); i++) {
			JMenu menu = menuBar.getMenu(i);
			if (menu != null) {
				menu.setFont(mainFont);
				for (int j = 0; j < menu.getItemCount(); j++) {
					JMenuItem item = menu.getItem(j);
					if (item != null) item.setFont(mainFont);
				}
			}
		}
		menuBar.setBackground(bgColor);

		// Sol panel ve JList
		panel.setBackground(bgColor);
		list.setFont(mainFont);
		list.setBackground(bgColor);
		list.setSelectionBackground(listSelectionColor);
		list.setSelectionForeground(accentColor.darker());
		list.setFixedCellHeight(32);
		list.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Sağ üst kullanıcı paneli
		userPanel.setBackground(bgColor);
		userLabel.setFont(mainFont.deriveFont(java.awt.Font.BOLD, 15f));
		btnLogout.setFont(mainFont);
		btnLogout.setBackground(buttonColor);
		btnLogout.setForeground(buttonTextColor);
		btnLogout.setFocusPainted(false);
		btnLogout.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 15, 5, 15));

		// CardPanel ve içerik panelleri arka plan
		cardPanel.setBackground(bgColor);
		allSongsPanel.setBackground(bgColor);
		albumsPanel.setBackground(bgColor);
		artistsPanel.setBackground(bgColor);
		playlistsPanel.setBackground(bgColor);
		recommendationsPanel.setBackground(bgColor);

		// Tablolar için font ve renkler
		javax.swing.JTable[] tables = {allSongsTable, albumsTable, artistsTable, playlistsTable, recommendationsTable};
		for (javax.swing.JTable t : tables) {
			t.setFont(mainFont);
			t.setRowHeight(28);
			t.getTableHeader().setFont(mainFont.deriveFont(java.awt.Font.BOLD, 14f));
			t.setSelectionBackground(listSelectionColor);
			t.setSelectionForeground(accentColor.darker());
			t.setGridColor(new java.awt.Color(220, 220, 220));
			t.setShowGrid(true);
			t.setBackground(java.awt.Color.WHITE);
		}

		// Araç çubuğu (butonlar)
		toolBarPanel.setBackground(bgColor);
		javax.swing.JButton[] toolButtons = {btnAdd, btnEditAllSongs, btnDeleteAllSongs, btnCreatePlaylist, btnMetadata, btnAddToPlaylist, btnRemoveFromPlaylist, btnAddArtist, 
		                                    btnAddAlbum, btnEditAlbum, btnDeleteAlbum, btnViewSongs, btnAddToFavorites, btnRefreshRecommendations};
		for (javax.swing.JButton b : toolButtons) {
			b.setFont(mainFont);
			b.setBackground(buttonColor);
			b.setForeground(buttonTextColor);
			b.setFocusPainted(false);
			b.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 18, 6, 18));
		}

		// Durum çubuğu
		statusBar.setFont(mainFont.deriveFont(java.awt.Font.ITALIC, 13f));
		statusBar.setBackground(bgColor);
		statusBar.setOpaque(true);
		statusBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10));
		statusBar.setForeground(new java.awt.Color(100, 100, 100));

		// SongDAO ile SQLite bağlantısı
		songDAO = new SongDAO();
		albumDAO = new AlbumDAO();
		artistDAO = new ArtistDAO();
		playlistDAO = new PlaylistDAO();

		// All Songs tablosunu başlatırken veritabanından yükle
		javax.swing.table.DefaultTableModel allSongsModel = (javax.swing.table.DefaultTableModel) allSongsTable.getModel();
		allSongsModel.setRowCount(0);
		for (String[] song : songDAO.getAllSongs()) {
			allSongsModel.addRow(song);
		}
		
		// Albums tablosunu yükle
		javax.swing.table.DefaultTableModel albumsTableModel = (javax.swing.table.DefaultTableModel) albumsTable.getModel();
		albumsTableModel.setRowCount(0);
		for (String[] album : albumDAO.getAllAlbums()) {
			albumsTableModel.addRow(album);
		}
		
		// Artists tablosunu yükle
		javax.swing.table.DefaultTableModel artistsTableModel = (javax.swing.table.DefaultTableModel) artistsTable.getModel();
		artistsTableModel.setRowCount(0);
		for (String[] artist : artistDAO.getAllArtists()) {
			artistsTableModel.addRow(artist);
		}
		
		// Playlists tablosunu yükle
		javax.swing.table.DefaultTableModel playlistsTableModel = (javax.swing.table.DefaultTableModel) playlistsTable.getModel();
		playlistsTableModel.setRowCount(0);
		for (String[] playlist : playlistDAO.getAllPlaylists()) {
			playlistsTableModel.addRow(playlist);
		}

		// Add Song butonuna tıklanınca şarkı ekleme dialogu aç
		btnAdd.addActionListener(e -> {
			javax.swing.JTextField titleField = new javax.swing.JTextField();
			javax.swing.JTextField artistField = new javax.swing.JTextField();
			javax.swing.JTextField albumField = new javax.swing.JTextField();
			javax.swing.JTextField genreField = new javax.swing.JTextField();
			javax.swing.JTextField durationField = new javax.swing.JTextField();
			Object[] message = {
				"Title:", titleField,
				"Artist:", artistField,
				"Album:", albumField,
				"Genre:", genreField,
				"Duration (sec):", durationField
			};
			int option = javax.swing.JOptionPane.showConfirmDialog(
				this, message, "Add Song", javax.swing.JOptionPane.OK_CANCEL_OPTION);
			if (option == javax.swing.JOptionPane.OK_OPTION) {
				String title = titleField.getText().trim();
				String artist = artistField.getText().trim();
				String album = albumField.getText().trim();
				String genre = genreField.getText().trim();
				String duration = durationField.getText().trim();
				if (title.isEmpty() || artist.isEmpty() || album.isEmpty() || genre.isEmpty() || duration.isEmpty()) {
					javax.swing.JOptionPane.showMessageDialog(this, "All fields are required!", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					Integer.parseInt(duration);
				} catch (NumberFormatException ex) {
					javax.swing.JOptionPane.showMessageDialog(this, "Duration must be a number!", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				try {
					// Şarkıyı veritabanına ekle - ensure this uses the instance variable
					songDAO.addSong(title, artist, album, genre);
					
					// Şarkıyı tabloya ekle
					javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) allSongsTable.getModel();
					model.addRow(new Object[] {title, artist, album, genre});
					
					// Artist'i kontrol et ve ekle
					boolean artistExists = false;
					javax.swing.table.DefaultTableModel artistModel = (javax.swing.table.DefaultTableModel) artistsTable.getModel();
					for (int i = 0; i < artistModel.getRowCount(); i++) {
						if (artistModel.getValueAt(i, 0).equals(artist)) {
							artistExists = true;
							break;
						}
					}
					
					if (!artistExists) {
						artistModel.addRow(new Object[] {artist, "Unknown", genre});
					}
					
					// Album'ü kontrol et ve ekle
					boolean albumExists = false;
					javax.swing.table.DefaultTableModel albumModel = (javax.swing.table.DefaultTableModel) albumsTable.getModel();
					for (int i = 0; i < albumModel.getRowCount(); i++) {
						if (albumModel.getValueAt(i, 0).equals(album) && albumModel.getValueAt(i, 1).equals(artist)) {
							albumExists = true;
							break;
						}
					}
					
					if (!albumExists) {
						java.util.Calendar now = java.util.Calendar.getInstance();
						albumModel.addRow(new Object[] {album, artist, String.valueOf(now.get(java.util.Calendar.YEAR)), genre});
					}
					
					statusBar.setText("Status: Song added successfully and saved to database.");
				} catch (Exception ex) {
					statusBar.setText("Status: Error saving song - " + ex.getMessage());
					ex.printStackTrace();
				}
			}
		});

		// Edit butonuna tıklanınca seçili satırı düzenle
		btnEditAllSongs.addActionListener(e -> {
				int row = allSongsTable.getSelectedRow();
				if (row == -1) {
					javax.swing.JOptionPane.showMessageDialog(null, "Please select a song to edit.", "Warning", javax.swing.JOptionPane.WARNING_MESSAGE);
					return;
				}
				javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) allSongsTable.getModel();
				String oldTitle = (String) model.getValueAt(row, 0);
				String oldArtist = (String) model.getValueAt(row, 1);
				String oldAlbum = (String) model.getValueAt(row, 2);
				String oldGenre = (String) model.getValueAt(row, 3);
				
				javax.swing.JTextField titleField = new javax.swing.JTextField(oldTitle);
				javax.swing.JTextField artistField = new javax.swing.JTextField(oldArtist);
				javax.swing.JTextField albumField = new javax.swing.JTextField(oldAlbum);
				javax.swing.JTextField genreField = new javax.swing.JTextField(oldGenre);
				
				Object[] message = {
					"Title:", titleField,
					"Artist:", artistField,
					"Album:", albumField,
					"Genre:", genreField
				};
				
				int option = javax.swing.JOptionPane.showConfirmDialog(null, message, "Edit Song", javax.swing.JOptionPane.OK_CANCEL_OPTION);
				if (option == javax.swing.JOptionPane.OK_OPTION) {
				    String newTitle = titleField.getText().trim();
				    String newArtist = artistField.getText().trim();
				    String newAlbum = albumField.getText().trim();
				    String newGenre = genreField.getText().trim();
				    
				    try {
				        // Update the database
				        boolean updateSuccessful = songDAO.updateSong(
				            oldTitle, oldArtist, oldAlbum, 
				            newTitle, newArtist, newAlbum, newGenre
				        );
				        
				        if (updateSuccessful) {
				            // Update the table if database update was successful
				            model.setValueAt(newTitle, row, 0);
				            model.setValueAt(newArtist, row, 1);
				            model.setValueAt(newAlbum, row, 2);
				            model.setValueAt(newGenre, row, 3);
				            
				            statusBar.setText("Status: Song edited successfully and database updated.");
				        } else {
				            statusBar.setText("Status: Error updating song in database.");
				        }
				    } catch (Exception ex) {
				        statusBar.setText("Status: Error editing song - " + ex.getMessage());
				        ex.printStackTrace();
				    }
			    }
		});

		// Delete butonuna tıklanınca seçili satırı sil
		btnDeleteAllSongs.addActionListener(e -> {
				int row = allSongsTable.getSelectedRow();
				if (row == -1) {
					javax.swing.JOptionPane.showMessageDialog(null, "Please select a song to delete.", "Warning", javax.swing.JOptionPane.WARNING_MESSAGE);
					return;
				}
				int confirm = javax.swing.JOptionPane.showConfirmDialog(null, "Are you sure you want to delete the selected song?", "Confirm Delete", javax.swing.JOptionPane.YES_NO_OPTION);
				if (confirm == javax.swing.JOptionPane.YES_OPTION) {
					javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) allSongsTable.getModel();
                    String title = (String) model.getValueAt(row, 0);
                    String artist = (String) model.getValueAt(row, 1);
                    String album = (String) model.getValueAt(row, 2);
                    
                    try {
                        // Veritabanından sil
                        songDAO.deleteSong(title, artist, album);
                        
                        // Tabloyu güncelle
                        model.removeRow(row);
                        
                        statusBar.setText("Status: Song deleted successfully and database updated.");
                    } catch (Exception ex) {
                        statusBar.setText("Status: Error deleting song - " + ex.getMessage());
                        ex.printStackTrace();
                    }
				}
		});

		// Create Playlist butonuna tıklanınca yeni playlist ekle
		btnCreatePlaylist.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				javax.swing.JTextField nameField = new javax.swing.JTextField();
				Object[] message = {"Playlist Name:", nameField};
				int option = javax.swing.JOptionPane.showConfirmDialog(null, message, "Create Playlist", javax.swing.JOptionPane.OK_CANCEL_OPTION);
				if (option == javax.swing.JOptionPane.OK_OPTION) {
					String name = nameField.getText().trim();
					if (name.isEmpty()) {
						javax.swing.JOptionPane.showMessageDialog(null, "Playlist name cannot be empty!", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					try {
						// Veritabanına playlist ekle
						boolean success = playlistDAO.addPlaylist(name, "", 1);
						
						if (success) {
							// UI'yi güncelle
							javax.swing.table.DefaultTableModel playlistsTableModel = (javax.swing.table.DefaultTableModel) playlistsTable.getModel();
							playlistsTableModel.addRow(new Object[] {name, 0, java.time.LocalDate.now().toString()});
							statusBar.setText("Status: Playlist created successfully.");
						} else {
							statusBar.setText("Status: Error creating playlist in database.");
						}
					} catch (Exception ex) {
						javax.swing.JOptionPane.showMessageDialog(null, "Error creating playlist: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
						ex.printStackTrace();
					}
				}
			}
		});

		// Metadata butonuna tıklanınca seçili şarkının bilgilerini göster
		btnMetadata.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				int row = allSongsTable.getSelectedRow();
				if (row == -1) {
					javax.swing.JOptionPane.showMessageDialog(null, "Please select a song to view metadata.", "Warning", javax.swing.JOptionPane.WARNING_MESSAGE);
					return;
				}
				javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) allSongsTable.getModel();
				String title = (String) model.getValueAt(row, 0);
				String artist = (String) model.getValueAt(row, 1);
				String album = (String) model.getValueAt(row, 2);
				String genre = (String) model.getValueAt(row, 3);
				String message = String.format("Title: %s\nArtist: %s\nAlbum: %s\nGenre: %s", title, artist, album, genre);
				javax.swing.JOptionPane.showMessageDialog(null, message, "Song Metadata", javax.swing.JOptionPane.INFORMATION_MESSAGE);
			}
		});

		// Add Song to Playlist butonuna tıklanınca şarkı ekleme dialogu aç
		btnAddToPlaylist.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				int playlistRow = playlistsTable.getSelectedRow();
				if (playlistRow == -1) {
					javax.swing.JOptionPane.showMessageDialog(null, "Please select a playlist to add a song.", "Warning", javax.swing.JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				// Mevcut şarkıları al
				javax.swing.table.DefaultTableModel allSongsModel = (javax.swing.table.DefaultTableModel) allSongsTable.getModel();
				int rowCount = allSongsModel.getRowCount();
				
				if (rowCount == 0) {
					javax.swing.JOptionPane.showMessageDialog(null, "No songs available to add.", "Warning", javax.swing.JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				// Şarkı listesini oluştur
				String[] songs = new String[rowCount];
				for (int i = 0; i < rowCount; i++) {
					String title = (String) allSongsModel.getValueAt(i, 0);
					String artist = (String) allSongsModel.getValueAt(i, 1);
					songs[i] = title + " - " + artist;
				}
				
				// Şarkı seç
				String selectedSong = (String) javax.swing.JOptionPane.showInputDialog(null, 
					"Select a song to add:", "Add Song to Playlist", 
					javax.swing.JOptionPane.QUESTION_MESSAGE, null, songs, songs[0]);
				
				if (selectedSong != null) {
					// Playlist'in song count'unu güncelle
					javax.swing.table.DefaultTableModel playlistsModel = (javax.swing.table.DefaultTableModel) playlistsTable.getModel();
					int currentCount = Integer.parseInt(playlistsModel.getValueAt(playlistRow, 1).toString());
					playlistsModel.setValueAt(currentCount + 1, playlistRow, 1);
					
					statusBar.setText("Status: Song '" + selectedSong + "' added to playlist.");
				}
			}
		});

		// Remove Song from Playlist butonuna tıklanınca şarkı silme dialogu aç
		btnRemoveFromPlaylist.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				int playlistRow = playlistsTable.getSelectedRow();
				if (playlistRow == -1) {
					javax.swing.JOptionPane.showMessageDialog(null, "Please select a playlist to remove a song.", "Warning", javax.swing.JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				// Mevcut şarkı sayısını kontrol et
				javax.swing.table.DefaultTableModel playlistsModel = (javax.swing.table.DefaultTableModel) playlistsTable.getModel();
				int currentCount = Integer.parseInt(playlistsModel.getValueAt(playlistRow, 1).toString());
				
				if (currentCount <= 0) {
					javax.swing.JOptionPane.showMessageDialog(null, "This playlist has no songs to remove.", "Warning", javax.swing.JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				// Şarkıyı sil
				String songTitle = (String) javax.swing.JOptionPane.showInputDialog(null, "Enter the song title to remove:", "Remove Song from Playlist", javax.swing.JOptionPane.QUESTION_MESSAGE);
				
				if (songTitle != null && !songTitle.trim().isEmpty()) {
					// Playlist song count'u güncelle
					playlistsModel.setValueAt(currentCount - 1, playlistRow, 1);
					statusBar.setText("Status: Song '" + songTitle + "' removed from playlist.");
				}
			}
		});

		// Add Artist butonuna tıklanınca yeni artist ekleme dialogu aç
		btnAddArtist.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				javax.swing.JTextField nameField = new javax.swing.JTextField();
				javax.swing.JTextField countryField = new javax.swing.JTextField();
				javax.swing.JTextField genreField = new javax.swing.JTextField();
				Object[] message = {"Artist Name:", nameField, "Country:", countryField, "Genre:", genreField};
				int option = javax.swing.JOptionPane.showConfirmDialog(null, message, "Add Artist", javax.swing.JOptionPane.OK_CANCEL_OPTION);
				if (option == javax.swing.JOptionPane.OK_OPTION) {
					String name = nameField.getText().trim();
					String country = countryField.getText().trim();
					String genre = genreField.getText().trim();
					
					if (name.isEmpty() || country.isEmpty() || genre.isEmpty()) {
						javax.swing.JOptionPane.showMessageDialog(null, "All fields are required!", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					try {
						// Veritabanına artist ekle
						boolean success = artistDAO.addArtist(name, country, genre, 1);
						
						if (success) {
							// UI'yi güncelle
							javax.swing.table.DefaultTableModel artistsTableModel = (javax.swing.table.DefaultTableModel) artistsTable.getModel();
							artistsTableModel.addRow(new Object[] {name, country, genre});
							statusBar.setText("Status: Artist added successfully.");
						} else {
							statusBar.setText("Status: Error adding artist to database.");
						}
					} catch (Exception ex) {
						javax.swing.JOptionPane.showMessageDialog(null, "Error adding artist: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
						ex.printStackTrace();
					}
				}
			}
		});

		// Edit butonuna tıklanınca seçili satırı düzenle
		btnEditArtists.addActionListener(e -> {
			int row = artistsTable.getSelectedRow();
			if (row == -1) {
				javax.swing.JOptionPane.showMessageDialog(null, "Please select an artist to edit.", "Warning", javax.swing.JOptionPane.WARNING_MESSAGE);
				return;
			}
			javax.swing.table.DefaultTableModel artistTableModel = (javax.swing.table.DefaultTableModel) artistsTable.getModel();
			String oldName = (String) artistTableModel.getValueAt(row, 0);
			String oldCountry = (String) artistTableModel.getValueAt(row, 1);
			String oldGenre = (String) artistTableModel.getValueAt(row, 2);
			
			javax.swing.JTextField nameField = new javax.swing.JTextField(oldName);
			javax.swing.JTextField countryField = new javax.swing.JTextField(oldCountry);
			javax.swing.JTextField genreField = new javax.swing.JTextField(oldGenre);
			Object[] message = {
				"Artist Name:", nameField,
				"Country:", countryField,
				"Genre:", genreField
			};
			int option = javax.swing.JOptionPane.showConfirmDialog(null, message, "Edit Artist", javax.swing.JOptionPane.OK_CANCEL_OPTION);
			if (option == javax.swing.JOptionPane.OK_OPTION) {
				String newName = nameField.getText().trim();
				String newCountry = countryField.getText().trim();
				String newGenre = genreField.getText().trim();
				
				try {
					// Veritabanında artist güncelle
					boolean success = artistDAO.updateArtist(oldName, newName, newCountry, newGenre);
					
					if (success) {
						// UI'yi güncelle
						artistTableModel.setValueAt(newName, row, 0);
						artistTableModel.setValueAt(newCountry, row, 1);
						artistTableModel.setValueAt(newGenre, row, 2);
						statusBar.setText("Status: Artist edited successfully.");
					} else {
						statusBar.setText("Status: Error updating artist in database.");
					}
				} catch (Exception ex) {
					javax.swing.JOptionPane.showMessageDialog(null, "Error updating artist: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
				}
			}
		});

		// Delete butonuna tıklanınca seçili satırı sil
		btnDeleteArtists.addActionListener(e -> {
			int row = artistsTable.getSelectedRow();
			if (row == -1) {
				javax.swing.JOptionPane.showMessageDialog(null, "Please select an artist to delete.", "Warning", javax.swing.JOptionPane.WARNING_MESSAGE);
				return;
			}
			int confirm = javax.swing.JOptionPane.showConfirmDialog(null, "Are you sure you want to delete the selected artist?", "Confirm Delete", javax.swing.JOptionPane.YES_NO_OPTION);
			if (confirm == javax.swing.JOptionPane.YES_OPTION) {
				javax.swing.table.DefaultTableModel artistTableModel = (javax.swing.table.DefaultTableModel) artistsTable.getModel();
				String name = (String) artistTableModel.getValueAt(row, 0);
				
				try {
					// Veritabanından artist sil
					boolean success = artistDAO.deleteArtist(name);
					
					if (success) {
						// UI'yi güncelle
						artistTableModel.removeRow(row);
						statusBar.setText("Status: Artist deleted successfully.");
					} else {
						statusBar.setText("Status: Error deleting artist from database.");
					}
				} catch (Exception ex) {
					javax.swing.JOptionPane.showMessageDialog(null, "Error deleting artist: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
				}
			}
		});

		// Albums için butonlar
		btnAddAlbum.addActionListener(e -> {
			javax.swing.JTextField titleField = new javax.swing.JTextField();
			javax.swing.JTextField artistField = new javax.swing.JTextField();
			javax.swing.JTextField yearField = new javax.swing.JTextField(String.valueOf(java.time.LocalDate.now().getYear()));
			javax.swing.JTextField genreField = new javax.swing.JTextField();
			
			Object[] message = {
				"Album Title:", titleField,
				"Artist:", artistField,
				"Year:", yearField,
				"Genre:", genreField
			};
			
			int option = javax.swing.JOptionPane.showConfirmDialog(null, message, "Add Album", javax.swing.JOptionPane.OK_CANCEL_OPTION);
			if (option == javax.swing.JOptionPane.OK_OPTION) {
				String title = titleField.getText().trim();
				String artist = artistField.getText().trim();
				String year = yearField.getText().trim();
				String genre = genreField.getText().trim();
				
				if (title.isEmpty() || artist.isEmpty() || year.isEmpty() || genre.isEmpty()) {
					javax.swing.JOptionPane.showMessageDialog(null, "All fields are required!", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				try {
					// Veritabanına album ekle
					boolean success = albumDAO.addAlbum(title, artist, year, genre, 1);
					
					if (success) {
						// UI'yi güncelle
						javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) albumsTable.getModel();
						model.addRow(new Object[] {title, artist, year, genre});
						statusBar.setText("Status: Album added successfully.");
					} else {
						statusBar.setText("Status: Error adding album to database.");
					}
				} catch (Exception ex) {
					javax.swing.JOptionPane.showMessageDialog(null, "Error adding album: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
				}
			}
		});
		
		btnEditAlbum.addActionListener(e -> {
			int row = albumsTable.getSelectedRow();
			if (row == -1) {
				javax.swing.JOptionPane.showMessageDialog(null, "Please select an album to edit.", "Warning", javax.swing.JOptionPane.WARNING_MESSAGE);
				return;
			}
			
			javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) albumsTable.getModel();
			String oldTitle = (String) model.getValueAt(row, 0);
			String oldArtist = (String) model.getValueAt(row, 1);
			String oldYear = (String) model.getValueAt(row, 2);
			String oldGenre = (String) model.getValueAt(row, 3);
			
			javax.swing.JTextField titleField = new javax.swing.JTextField(oldTitle);
			javax.swing.JTextField artistField = new javax.swing.JTextField(oldArtist);
			javax.swing.JTextField yearField = new javax.swing.JTextField(oldYear);
			javax.swing.JTextField genreField = new javax.swing.JTextField(oldGenre);
			
			Object[] message = {
				"Album Title:", titleField,
				"Artist:", artistField,
				"Year:", yearField,
				"Genre:", genreField
			};
			
			int option = javax.swing.JOptionPane.showConfirmDialog(null, message, "Edit Album", javax.swing.JOptionPane.OK_CANCEL_OPTION);
			if (option == javax.swing.JOptionPane.OK_OPTION) {
				String newTitle = titleField.getText().trim();
				String newArtist = artistField.getText().trim();
				String newYear = yearField.getText().trim();
				String newGenre = genreField.getText().trim();
				
				try {
					// Veritabanında album güncelle
					boolean success = albumDAO.updateAlbum(oldTitle, oldArtist, newTitle, newArtist, newYear, newGenre);
					
					if (success) {
						// UI'yi güncelle
						model.setValueAt(newTitle, row, 0);
						model.setValueAt(newArtist, row, 1);
						model.setValueAt(newYear, row, 2);
						model.setValueAt(newGenre, row, 3);
						statusBar.setText("Status: Album edited successfully.");
					} else {
						statusBar.setText("Status: Error updating album in database.");
					}
				} catch (Exception ex) {
					javax.swing.JOptionPane.showMessageDialog(null, "Error updating album: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
				}
			}
		});
		
		btnDeleteAlbum.addActionListener(e -> {
			int row = albumsTable.getSelectedRow();
			if (row == -1) {
				javax.swing.JOptionPane.showMessageDialog(null, "Please select an album to delete.", "Warning", javax.swing.JOptionPane.WARNING_MESSAGE);
				return;
			}
			
			int confirm = javax.swing.JOptionPane.showConfirmDialog(null, "Are you sure you want to delete the selected album?", "Confirm Delete", javax.swing.JOptionPane.YES_NO_OPTION);
			if (confirm == javax.swing.JOptionPane.YES_OPTION) {
				javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) albumsTable.getModel();
				String title = (String) model.getValueAt(row, 0);
				String artist = (String) model.getValueAt(row, 1);
				
				try {
					// Veritabanından album sil
					boolean success = albumDAO.deleteAlbum(title, artist);
					
					if (success) {
						// UI'yi güncelle
						model.removeRow(row);
						statusBar.setText("Status: Album deleted successfully.");
					} else {
						statusBar.setText("Status: Error deleting album from database.");
					}
				} catch (Exception ex) {
					javax.swing.JOptionPane.showMessageDialog(null, "Error deleting album: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
				}
			}
		});
		
		btnViewSongs.addActionListener(e -> {
			int row = albumsTable.getSelectedRow();
			if (row == -1) {
				javax.swing.JOptionPane.showMessageDialog(null, "Please select an album to view metadata.", "Warning", javax.swing.JOptionPane.WARNING_MESSAGE);
				return;
			}
			
			javax.swing.table.DefaultTableModel albumModel = (javax.swing.table.DefaultTableModel) albumsTable.getModel();
			String albumTitle = (String) albumModel.getValueAt(row, 0);
			String albumArtist = (String) albumModel.getValueAt(row, 1);
			String albumYear = (String) albumModel.getValueAt(row, 2);
			String albumGenre = (String) albumModel.getValueAt(row, 3);
			
			String message = String.format("Album: %s\nArtist: %s\nYear: %s\nGenre: %s", 
				albumTitle, albumArtist, albumYear, albumGenre);
				
			javax.swing.JOptionPane.showMessageDialog(null, message, "Album Metadata", javax.swing.JOptionPane.INFORMATION_MESSAGE);
		});

		// Recommendations için butonlar
		btnRefreshRecommendations.addActionListener(e -> {
			javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) recommendationsTable.getModel();
			model.setRowCount(0); // Mevcut tavsiyeleri temizle
			model.addRow(new Object[] {"Uptown Funk", "Mark Ronson ft. Bruno Mars", "New recommendation"});
			model.addRow(new Object[] {"Believer", "Imagine Dragons", "You might like this"});
			model.addRow(new Object[] {"Bad Guy", "Billie Eilish", "Popular among similar users"});
			statusBar.setText("Status: Recommendations refreshed.");
		});
		
		btnAddToFavorites.addActionListener(e -> {
			int row = recommendationsTable.getSelectedRow();
			if (row == -1) {
				javax.swing.JOptionPane.showMessageDialog(null, "Please select a song to add to your library.", "Warning", javax.swing.JOptionPane.WARNING_MESSAGE);
				return;
			}
			
			javax.swing.table.DefaultTableModel recModel = (javax.swing.table.DefaultTableModel) recommendationsTable.getModel();
			String title = (String) recModel.getValueAt(row, 0);
			String artist = (String) recModel.getValueAt(row, 1);
			
			// All Songs tablosuna ekle
			javax.swing.table.DefaultTableModel songsModel = (javax.swing.table.DefaultTableModel) allSongsTable.getModel();
			songsModel.addRow(new Object[] {title, artist, "Unknown", "Unknown"});
			
			statusBar.setText("Status: Song '" + title + "' added to your library.");
		});

		// Edit butonuna tıklanınca seçili satırı düzenle
		btnEditPlaylists.addActionListener(e -> {
			int row = playlistsTable.getSelectedRow();
			if (row == -1) {
				javax.swing.JOptionPane.showMessageDialog(null, "Please select a playlist to edit.", "Warning", javax.swing.JOptionPane.WARNING_MESSAGE);
				return;
			}
			javax.swing.table.DefaultTableModel playlistTableModel = (javax.swing.table.DefaultTableModel) playlistsTable.getModel();
			String oldName = (String) playlistTableModel.getValueAt(row, 0);
			javax.swing.JTextField nameField = new javax.swing.JTextField(oldName);
			Object[] message = {"Playlist Name:", nameField};
			int option = javax.swing.JOptionPane.showConfirmDialog(null, message, "Edit Playlist", javax.swing.JOptionPane.OK_CANCEL_OPTION);
			if (option == javax.swing.JOptionPane.OK_OPTION) {
				String newName = nameField.getText().trim();
				
				try {
					// Veritabanında playlist güncelle
					boolean success = playlistDAO.updatePlaylist(oldName, newName);
					
					if (success) {
						// UI'yi güncelle
						playlistTableModel.setValueAt(newName, row, 0);
						statusBar.setText("Status: Playlist edited successfully.");
					} else {
						statusBar.setText("Status: Error updating playlist in database.");
					}
				} catch (Exception ex) {
					javax.swing.JOptionPane.showMessageDialog(null, "Error updating playlist: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
				}
			}
		});

		// Delete butonuna tıklanınca seçili satırı sil
		btnDeletePlaylists.addActionListener(e -> {
			int row = playlistsTable.getSelectedRow();
			if (row == -1) {
				javax.swing.JOptionPane.showMessageDialog(null, "Please select a playlist to delete.", "Warning", javax.swing.JOptionPane.WARNING_MESSAGE);
				return;
			}
			int confirm = javax.swing.JOptionPane.showConfirmDialog(null, "Are you sure you want to delete the selected playlist?", "Confirm Delete", javax.swing.JOptionPane.YES_NO_OPTION);
			if (confirm == javax.swing.JOptionPane.YES_OPTION) {
				javax.swing.table.DefaultTableModel playlistTableModel = (javax.swing.table.DefaultTableModel) playlistsTable.getModel();
				String name = (String) playlistTableModel.getValueAt(row, 0);
				
				try {
					// Veritabanından playlist sil
					boolean success = playlistDAO.deletePlaylist(name);
					
					if (success) {
						// UI'yi güncelle
						playlistTableModel.removeRow(row);
						statusBar.setText("Status: Playlist deleted successfully.");
					} else {
						statusBar.setText("Status: Error deleting playlist from database.");
					}
				} catch (Exception ex) {
					javax.swing.JOptionPane.showMessageDialog(null, "Error deleting playlist: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
				}
			}
		});
	}

}
