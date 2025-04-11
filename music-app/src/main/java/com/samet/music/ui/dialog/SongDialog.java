package com.samet.music.ui.dialog;

import com.samet.music.model.Song;
import com.samet.music.model.Artist;
import com.samet.music.model.Album;
import com.samet.music.dao.SongDAO;
import com.samet.music.dao.ArtistDAO;
import com.samet.music.dao.AlbumDAO;
import com.samet.music.dao.DAOFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class SongDialog extends JDialog {
    private JTextField nameField;
    private JComboBox<Artist> artistComboBox;
    private JComboBox<Album> albumComboBox;
    private JSpinner durationSpinner;
    private boolean approved = false;
    private Song song;

    private final SongDAO songDAO;
    private final ArtistDAO artistDAO;
    private final AlbumDAO albumDAO;

    public SongDialog(JFrame parent, String title) {
        super(parent, title, true);
        this.songDAO = DAOFactory.getInstance().getSongDAO();
        this.artistDAO = DAOFactory.getInstance().getArtistDAO();
        this.albumDAO = DAOFactory.getInstance().getAlbumDAO();
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Name field
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Name:"), gbc);

        gbc.gridx = 1;
        nameField = new JTextField(20);
        add(nameField, gbc);

        // Artist combo box
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Artist:"), gbc);

        gbc.gridx = 1;
        artistComboBox = new JComboBox<>();
        loadArtists();
        add(artistComboBox, gbc);

        // Album combo box
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Album:"), gbc);

        gbc.gridx = 1;
        albumComboBox = new JComboBox<>();
        loadAlbums();
        add(albumComboBox, gbc);

        // Duration spinner
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("Duration (seconds):"), gbc);

        gbc.gridx = 1;
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0, 3600, 1);
        durationSpinner = new JSpinner(spinnerModel);
        add(durationSpinner, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> {
            approved = true;
            dispose();
        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        add(buttonPanel, gbc);

        pack();
        setLocationRelativeTo(getParent());
    }

    private void loadArtists() {
        List<Artist> artists = artistDAO.getAll();
        DefaultComboBoxModel<Artist> model = new DefaultComboBoxModel<>();
        for (Artist artist : artists) {
            model.addElement(artist);
        }
        artistComboBox.setModel(model);
    }

    private void loadAlbums() {
        List<Album> albums = albumDAO.getAll();
        DefaultComboBoxModel<Album> model = new DefaultComboBoxModel<>();
        for (Album album : albums) {
            model.addElement(album);
        }
        albumComboBox.setModel(model);
    }

    public boolean showDialog() {
        setVisible(true);
        return approved;
    }

    public Song getSong() {
        if (!approved) return null;
        
        String name = nameField.getText();
        Artist artist = (Artist) artistComboBox.getSelectedItem();
        Album album = (Album) albumComboBox.getSelectedItem();
        int duration = (Integer) durationSpinner.getValue();
        
        song = new Song(name, artist, duration);
        song.setAlbum(album);
        return song;
    }

    public void setSong(Song song) {
        if (song == null) return;
        
        this.song = song;
        nameField.setText(song.getName());
        
        if (song.getArtist() != null) {
            artistComboBox.setSelectedItem(song.getArtist());
        }
        
        if (song.getAlbum() != null) {
            albumComboBox.setSelectedItem(song.getAlbum());
        }
        
        durationSpinner.setValue(song.getDuration());
    }
} 