package com.samet.music.repository;

import java.util.List;

/**
 * Interface for collections of music entities
 * @param <T> The type of entity stored in the collection
 */
public interface IMusicCollection<T> {
    void add(T item);
    boolean remove(String id);
    T getById(String id);
    List<T> getAll();
    void clear();
    int size();
    boolean contains(String id);
    boolean saveToFile(String filePath);
    boolean loadFromFile(String filePath);
}