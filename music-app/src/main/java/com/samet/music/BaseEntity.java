package com.samet.music;

import java.io.Serializable;
import java.util.UUID;

/**
 * Abstract base class for all music library entities
 */
public abstract class BaseEntity implements Serializable {
    private final String id;
    private String name;

    public BaseEntity(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BaseEntity that = (BaseEntity) obj;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}