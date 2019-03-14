package com.distributed.chordLib;

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
     */
    String lookupKey(String key);

    /**
     * BasicLookup for key in chord network
     * @return IP address of node responsible for key
     * @ApiNote Synchronous Call
     */
    String lookupKeyBasic(String key);

}
