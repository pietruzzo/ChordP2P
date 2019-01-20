package com.distributed.chordLib;

import com.distributed.chordLib.chordCore.ChordNetwork;

import java.net.InetAddress;

/**
 * External interface of Chord Client Library
 * Uses SHA-1 to hash IPs and values
 */
public interface Chord {

    int DEFAULT_NUM_FINGERS = 4;
    int DEFAULT_NUM_SUCCESSORS = 4;

    /**
     * join a Chord network given a bootstrap node IP
     * @param bootstrapNodeIp
     * @return new ChordNetwork
     */

    ChordNetwork join(InetAddress bootstrapNodeIp);

    /**
     * Create a new Chord Network
     * @return joined ChordNetwork
     */
    ChordNetwork create();

}
