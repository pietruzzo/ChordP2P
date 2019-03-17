package com.distributed.chordApp.cooperativemirroring.core.utility;

/**
 * Interface used for storing general settings about the system
 */

public interface GeneralApplicationSettings {
    /**
     * Standard port associated to this application
     */
    public static final Integer APPLICATION_PORT = 55;
    /**
     * Location where to store resources
     */
    public static final String DATABASE_PATH = "/resources";
    /**
     * Extension associated to resources
     */
    public static final String DATABASE_RECORD_EXTENSION = ".txt";
    /**
     * Default timeout for requests
     */
    public static final Integer DEFAULT_CONNECTION_TIMEOUT_MS = 5000;
}
