package com.distributed.chordLib.chordCore.communication;

import com.distributed.chordLib.chordCore.ChordClient;
import com.distributed.chordLib.chordCore.Node;

public interface CommCallbackInterface {

    /**
     * @param IP node asking to join
     * Find successor for node asking to join
     * Send successor to node and configuration parameters (JoinResponseMessage)
     * @return Initialization parameters
     */
    ChordClient.InitParameters handleJoinRequest (String IP);

    /**
     * Callback for remote Basic Lookup Request
     * @param key
     * @return node responsible for key
     */
    Node handleLookupB(String key);

    /**
     * Callback for remote Standard Lookup Request
     * @param key
     * @return node responsible for key
     */
    Node handleLookup(String key);

    /**
     * a node says to me that he is my predecessor
     */
    void notifyIncoming(Node predecessor);

    /**
     * Return Ping
     */
    void ping();
}
