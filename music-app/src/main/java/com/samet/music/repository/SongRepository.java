package com.samet.music.repository;

import com.samet.music.model.Song;
import com.samet.music.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Song entity operations
 */
@Repository
public interface SongRepository extends JpaRepository<Song, Integer> {
    
    /**
     * Find all songs by user
     * @param user the user who owns the songs
     * @return a list of songs
     */
    List<Song> findByUser(User user);
    
    /**
     * Find all songs by user with pagination
     * @param user the user who owns the songs
     * @param pageable pagination information
     * @return a page of songs
     */
    Page<Song> findByUser(User user, Pageable pageable);
    
    /**
     * Find songs by title containing the given text
     * @param title the title to search for
     * @param pageable pagination information
     * @return a page of songs
     */
    Page<Song> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    /**
     * Find songs by artist containing the given text
     * @param artist the artist to search for
     * @param pageable pagination information
     * @return a page of songs
     */
    Page<Song> findByArtistContainingIgnoreCase(String artist, Pageable pageable);
    
    /**
     * Find songs by genre
     * @param genre the genre to search for
     * @param pageable pagination information
     * @return a page of songs
     */
    Page<Song> findByGenreIgnoreCase(String genre, Pageable pageable);
    
    /**
     * Find songs by album containing the given text
     * @param album the album to search for
     * @param pageable pagination information
     * @return a page of songs
     */
    Page<Song> findByAlbumContainingIgnoreCase(String album, Pageable pageable);
    
    /**
     * Find songs by year
     * @param year the year to search for
     * @param pageable pagination information
     * @return a page of songs
     */
    Page<Song> findByYear(Integer year, Pageable pageable);
    
    /**
     * Search songs by multiple criteria
     * @param query the search query
     * @param pageable pagination information
     * @return a page of songs
     */
    @Query("SELECT s FROM Song s WHERE " +
           "LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.artist) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.album) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.genre) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Song> search(@Param("query") String query, Pageable pageable);
} 