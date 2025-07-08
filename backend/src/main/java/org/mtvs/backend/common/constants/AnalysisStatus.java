package org.mtvs.backend.common.constants;

/**
 * Analysis status constants to replace enum usage and eliminate type compatibility issues.
 * Using string constants provides flexibility and database/JSON compatibility.
 */
public final class AnalysisStatus {
    
    /**
     * Analysis has been requested but not yet started
     */
    public static final String REQUESTED = "REQUESTED";
    
    /**
     * Analysis is currently being processed
     */
    public static final String PROCESSING = "PROCESSING";
    
    /**
     * Analysis has completed successfully
     */
    public static final String COMPLETED = "COMPLETED";
    
    /**
     * Analysis failed due to an error
     */
    public static final String FAILED = "FAILED";
    
    // Private constructor to prevent instantiation
    private AnalysisStatus() {
        throw new IllegalStateException("Utility class");
    }
    
    /**
     * Validates if the given status is a valid analysis status
     * @param status the status string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidStatus(String status) {
        return REQUESTED.equals(status) || 
               PROCESSING.equals(status) || 
               COMPLETED.equals(status) || 
               FAILED.equals(status);
    }
    
    /**
     * Gets all valid status values
     * @return array of all valid status constants
     */
    public static String[] getAllStatuses() {
        return new String[]{REQUESTED, PROCESSING, COMPLETED, FAILED};
    }
}