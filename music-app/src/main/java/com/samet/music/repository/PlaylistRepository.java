package com.samet.music.repository;

import com.samet.music.model.Playlist;
import com.samet.music.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Playlist entity operations
 */
@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Integer> {
    
    /**
     * Find all playlists by user
     * @param user the user who owns the playlists
     * @return a list of playlists
     */
    List<Playlist> findByUser(User user);
    
    /**
     * Find all playlists by user with pagination
     * @param user the user who owns the playlists
     * @param pageable pagination information
     * @return a page of playlists
     */
    Page<Playlist> findByUser(User user, Pageable pageable);
    
    /**
     * Find playlists by name containing the given text
     * @param name the name to search for
     * @param pageable pagination information
     * @return a page of playlists
     */
    Page<Playlist> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    /**
     * Find playlists by user and name containing the given text
     * @param user the user who owns the playlists
     * @param name the name to search for
     * @param pageable pagination information
     * @return a page of playlists
     */
    Page<Playlist> findByUserAndNameContainingIgnoreCase(User user, String name, Pageable pageable);
    
    /**
     * Search playlists by multiple criteria
     * @param query the search query
     * @param pageable pagination information
     * @return a page of playlists
     */
    @Query("SELECT p FROM Playlist p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Playlist> search(@Param("query") String query, Pageable pageable);
    
    /**
     * Search playlists by user and multiple criteria
     * @param user the user who owns the playlists
     * @param query the search query
     * @param pageable pagination information
     * @return a page of playlists
     */
    @Query("SELECT p FROM Playlist p WHERE p.user = :user AND (" +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Playlist> searchByUser(@Param("user") User user, @Param("query") String query, Pageable pageable);
} 