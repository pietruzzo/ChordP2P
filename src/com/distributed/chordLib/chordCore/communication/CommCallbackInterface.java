package com.distributed.chordLib.chordCore.communication;

import com.distributed.chordLib.chordCore.ChordClient;
import com.distributed.chordLib.chordCore.HashFunction;
import com.distributed.chordLib.chordCore.Node;
import org.jetbrains.annotations.Nullable;

import static com.distributed.chordLib.chordCore.HashFunction.*;

/**
 * Interface implemented by Logic layer to handle incoming requests from communication layer (remote nodes)
 */
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
    Node handleLookupB(Hash key);

    /**
     * Callback for remote Standard Lookup Request
     * @param key
     * @return node responsible for key
     */
    Node handleLookup(Hash key);

    /**
     * a node says to me that he is my predecessor
     */
    void notifyIncoming(Node predecessor);


    /**
     * a node is asking what is my predecessor
     * @return my Predecessor
     */
    Node handlePredecessorRequest();

    /**
     * a node is notifing me its departure from the network
     * and fix my predecessor and successor accordingly
     */
    void handleVolountaryDeparture(Node exitingNode, @Nullable Node predNode, @Nullable Node succNode);

    /**
     * Get hash value of ip
     * @param ip
     * @return
     */
    Hash getkey(String ip);
}
