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
     * @param ip server ip
     * @param port port of server;
     * @return parameters and successor
     * @implNote synchronous call
     */
    JoinResponseMessage join(String ip, String port);

    /**
     * Basic Lookup
     * @param key
     * @return Node responsible for the key
     * @implNote synchronous call
     */
    Node findSuccessorB (String key);

    /**
     * Standard Lookup
     * @param key
     * @return Node responsible for the key
     * @implNote synchronous call
     */
    Node findSuccessor (String key);

}
