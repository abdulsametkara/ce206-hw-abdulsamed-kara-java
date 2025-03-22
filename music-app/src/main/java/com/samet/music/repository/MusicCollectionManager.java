package com.samet.music.repository;

import java.io.*;
import java.util.*;

/**
 * Abstract base class for music entity collections
 * @param <T> The type of entity stored in the collection
 */
public abstract class MusicCollectionManager<T> implements IMusicCollection<T> {
    protected Map<String, T> items = new HashMap<>();
    // Veritabanı yükleme durumunu takip etmek için flag
    protected boolean isLoaded = false;

    @Override
    public void add(T item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }

        String id = getItemId(item);
        items.put(id, item);
    }

    @Override
    public boolean remove(String id) {
        return items.remove(id) != null;
    }

    @Override
    public T getById(String id) {
        return items.get(id);
    }

    @Override
    public List<T> getAll() {
        // Eğer daha önce yükleme yapılmadıysa, veritabanından yükle
        if (!isLoaded) {
            loadFromDatabase();
            isLoaded = true;
        }
        return new ArrayList<>(items.values());
    }

    @Override
    public void clear() {
        items.clear();
        isLoaded = false;
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean contains(String id) {
        return items.containsKey(id);
    }

    @Override
    public boolean saveToFile(String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(items);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving collection to file: " + e.getMessage());
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean loadFromFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            items = (Map<String, T>) ois.readObject();
            isLoaded = true;
            return true;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading collection from file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Veritabanından yükleme yapar
     * Bu metot alt sınıflar tarafından uygulanmalıdır
     */
    protected abstract void loadFromDatabase();

    protected abstract String getItemId(T item);
}