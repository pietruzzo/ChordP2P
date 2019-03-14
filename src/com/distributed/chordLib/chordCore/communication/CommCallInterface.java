package com.distributed.chordLib.chordCore.communication;

import com.distributed.chordLib.chordCore.Node;
import com.distributed.chordLib.chordCore.communication.messages.JoinResponseMessage;

/**
 * Interface to be implemented by communication Layer
 * Using messages package
 */
public interface CommCallInterface {

    /**
     * Open a connection to specified server
     * @param node server ip
     * @param port port of server;
     * @return parameters and successor
     * @implNote synchronous call
     */
    JoinResponseMessage join(Node node, String port);

    /**
     * Basic Lookup
     * @param key
     * @param node
     * @return Node responsible for the key
     * @implNote synchronous call
     */
    Node findSuccessorB (Node node, String key);

    /**
     * Standard Lookup
     * @param key
     * @param node
     * @return Node responsible for the key
     * @implNote synchronous call
     */
    Node findSuccessor (Node node, String key);

    /**
     * Notify successor that you may be his predecessor
     * @param successor receiver of message
     * @param me Me as a Node
     * @ApiNote Asynchronous
     */
    void notifySuccessor(Node successor, Node me);

    /**
     * Ping node and return true if alive
     * @implNote synchronous call
     */
    boolean isAlive(Node node);

    /**
     * Close communication with node
     * @param node
     * @throws ArrayStoreException Connection not found
     */
    void closeChannel(Node node) throws ArrayStoreException;
}
