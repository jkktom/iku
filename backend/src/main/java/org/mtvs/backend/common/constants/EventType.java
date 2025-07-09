package org.mtvs.backend.common.constants;

/**
 * Event type constants using byte values for optimal storage.
 * Each event type consumes only 1 byte instead of 50+ bytes for string storage.
 */
public final class EventType {
    
    // Event Type Constants (byte values 0-12)
    public static final byte UNKNOWN = 0;
    public static final byte CHAMPION_KILL = 1;
    public static final byte CHAMPION_SPECIAL_KILL = 2;
    public static final byte GAME_END = 3;
    public static final byte ITEM_DESTROYED = 4;
    public static final byte ITEM_PURCHASED = 5;
    public static final byte ITEM_SOLD = 6;
    public static final byte ITEM_UNDO = 7;
    public static final byte LEVEL_UP = 8;
    public static final byte PAUSE_END = 9;
    public static final byte SKILL_LEVEL_UP = 10;
    public static final byte WARD_KILL = 11;
    public static final byte WARD_PLACED = 12;
    
    // Private constructor to prevent instantiation
    private EventType() {
        throw new IllegalStateException("Utility class");
    }
    
    /**
     * Converts byte event type ID to readable string name
     * @param eventTypeId the byte ID of the event type
     * @return the string name of the event type
     */
    public static String getEventTypeName(byte eventTypeId) {
        switch(eventTypeId) {
            case UNKNOWN: return "UNKNOWN";
            case CHAMPION_KILL: return "CHAMPION_KILL";
            case CHAMPION_SPECIAL_KILL: return "CHAMPION_SPECIAL_KILL";
            case GAME_END: return "GAME_END";
            case ITEM_DESTROYED: return "ITEM_DESTROYED";
            case ITEM_PURCHASED: return "ITEM_PURCHASED";
            case ITEM_SOLD: return "ITEM_SOLD";
            case ITEM_UNDO: return "ITEM_UNDO";
            case LEVEL_UP: return "LEVEL_UP";
            case PAUSE_END: return "PAUSE_END";
            case SKILL_LEVEL_UP: return "SKILL_LEVEL_UP";
            case WARD_KILL: return "WARD_KILL";
            case WARD_PLACED: return "WARD_PLACED";
            default: return "UNKNOWN";
        }
    }
    
    /**
     * Converts string event type name to byte ID
     * @param eventTypeName the string name of the event type
     * @return the byte ID of the event type
     * @throws IllegalArgumentException if the event type name is not recognized
     */
    public static byte getEventTypeId(String eventTypeName) {
        if (eventTypeName == null) {
            throw new IllegalArgumentException("Event type name cannot be null");
        }
        
        switch(eventTypeName.toUpperCase()) {
            case "UNKNOWN": return UNKNOWN;
            case "CHAMPION_KILL": return CHAMPION_KILL;
            case "CHAMPION_SPECIAL_KILL": return CHAMPION_SPECIAL_KILL;
            case "GAME_END": return GAME_END;
            case "ITEM_DESTROYED": return ITEM_DESTROYED;
            case "ITEM_PURCHASED": return ITEM_PURCHASED;
            case "ITEM_SOLD": return ITEM_SOLD;
            case "ITEM_UNDO": return ITEM_UNDO;
            case "LEVEL_UP": return LEVEL_UP;
            case "PAUSE_END": return PAUSE_END;
            case "SKILL_LEVEL_UP": return SKILL_LEVEL_UP;
            case "WARD_KILL": return WARD_KILL;
            case "WARD_PLACED": return WARD_PLACED;
            default: return UNKNOWN;
        }
    }
    
    /**
     * Validates if the given byte is a valid event type ID
     * @param eventTypeId the byte to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEventType(byte eventTypeId) {
        return eventTypeId >= UNKNOWN && eventTypeId <= WARD_PLACED;
    }
    
    /**
     * Gets all valid event type IDs
     * @return array of all valid event type byte constants
     */
    public static byte[] getAllEventTypes() {
        return new byte[]{
            UNKNOWN, CHAMPION_KILL, CHAMPION_SPECIAL_KILL, GAME_END, ITEM_DESTROYED,
            ITEM_PURCHASED, ITEM_SOLD, ITEM_UNDO, LEVEL_UP,
            PAUSE_END, SKILL_LEVEL_UP, WARD_KILL, WARD_PLACED
        };
    }
    
    /**
     * Gets all event type names
     * @return array of all event type name strings
     */
    public static String[] getAllEventTypeNames() {
        return new String[]{
            "UNKNOWN", "CHAMPION_KILL", "CHAMPION_SPECIAL_KILL", "GAME_END", "ITEM_DESTROYED",
            "ITEM_PURCHASED", "ITEM_SOLD", "ITEM_UNDO", "LEVEL_UP",
            "PAUSE_END", "SKILL_LEVEL_UP", "WARD_KILL", "WARD_PLACED"
        };
    }
}