package com.samet.music.model;

import java.io.Serializable;
import java.util.UUID;

public abstract class BaseEntity implements Serializable {
    private String id;
    private String name;

    public BaseEntity(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
    }

    // ID'yi açıkça ayarlama imkanı ekleyelim
    public void setId(String id) {
        if (id != null && !id.isEmpty()) {
            this.id = id;
        }
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