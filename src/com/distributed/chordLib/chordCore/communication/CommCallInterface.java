package com.distributed.chordLib.chordCore.communication;

import com.distributed.chordLib.chordCore.HashFunction;
import com.distributed.chordLib.chordCore.Node;
import com.distributed.chordLib.chordCore.communication.messages.JoinResponseMessage;
import org.jetbrains.annotations.Nullable;

import static com.distributed.chordLib.chordCore.HashFunction.*;

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
    JoinResponseMessage join(Node node, int port);

    /**
     * Basic Lookup
     * @param key
     * @param node
     * @return Node responsible for the key
     * @implNote synchronous call
     */
    Node findSuccessorB (Node node, Hash key);

    /**
     * Standard Lookup
     * @param key
     * @param node
     * @return Node responsible for the key
     * @implNote synchronous call
     */
    Node findSuccessor (Node node, Hash key);

    /**
     * get predecessor for node
     * @param node
     */
    Node findPredecessor (Node node);

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
     * Notify all nodes kept alive by lib
     * if nodes is empty or null, the kill all Communication
     * @param nodes node used by application
     */
    void closeChannel(@Nullable Node[] nodes);

    /**
     * Notify successors and predecessor of this node departure and close communication Layer exiting Chord network
     * Only contact nodes that are not null
     * @param predecessor my predecessor
     * @param me my node
     * @param successor my successor
     */
    void closeCommLayer(@Nullable Node predecessor, Node me, @Nullable Node successor);

    /**
     * Close communication Layer
     */
    void closeCommLayer();
}
