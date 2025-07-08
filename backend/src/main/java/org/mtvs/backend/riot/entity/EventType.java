package org.mtvs.backend.riot.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "event_types")
public class EventType {
    
    @Id
    private byte id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String name;
    
    @Column(length = 100)
    private String displayName;
    
    // Default constructor required by JPA
    public EventType() {}
    
    public EventType(byte id, String name) {
        this.id = id;
        this.name = name;
        this.displayName = name.replace("_", " "); // Default display name
    }
    
    public EventType(byte id, String name, String displayName) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
    }
    
    // Getters and Setters
    public byte getId() {
        return id;
    }
    
    public void setId(byte id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    @Override
    public String toString() {
        return "EventType{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                '}';
    }
}