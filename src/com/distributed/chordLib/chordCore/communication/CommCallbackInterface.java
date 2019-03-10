package com.distributed.chordLib.chordCore.communication;

public interface CommCallbackInterface {

    /**
     * @param IP node asking to join
     * Find successor for node asking to join
     * Send successor to node and configuration parameters (JoinResponseMessage)
     */
    void handleJoinRequest (String IP);

    /**
     * Callback for remote Basic Lookup Request
     * @param key
     */
    void handleLookupB(String key);

    /**
     * Callback for remote Standard Lookup Request
     * @param key
     */
    void handleLookup(String key);
}
