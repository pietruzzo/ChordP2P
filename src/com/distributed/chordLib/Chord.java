package com.distributed.chordLib;

import com.distributed.chordLib.exceptions.CommunicationFailureException;
import com.distributed.chordLib.exceptions.TimeoutReachedException;

import java.net.InetAddress;

/**
 * External interface of ChordNetworkInterface Client Library
 * Uses SHA-1 to hash IPs and values
 */
public interface Chord {

    int DEFAULT_NUM_FINGERS = 4;
    int DEFAULT_NUM_SUCCESSORS = 4;
    String DEFAULT_SERVER_IP = "192.168.0.1";
    int DEFAULT_SERVER_PORT = 1678;

    /**
     * Lookup for key in chord network
     * @return IP address of node responsible for key
     * @ApiNote Synchronous Call
     * @throws CommunicationFailureException for socket failures
     * @throws TimeoutReachedException timeout in request handling reached
     */
    String lookupKey(String key) throws CommunicationFailureException, TimeoutReachedException;

    /**
     * BasicLookup for key in chord network
     * @return IP address of node responsible for key
     * @ApiNote Synchronous Call
     * @throws CommunicationFailureException for socket failures
     * @throws TimeoutReachedException timeout in request handling reached
     */
    String lookupKeyBasic(String key) throws CommunicationFailureException, TimeoutReachedException;

}
