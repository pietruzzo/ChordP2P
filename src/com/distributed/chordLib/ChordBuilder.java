package com.distributed.chordLib;


import jdk.internal.jline.internal.Nullable;

import java.net.InetAddress;

/**
 * Factory for Chord LIB
 */
public class ChordBuilder {

    /**
     * Join Existing Chord network
     * @param bootstrap Bootstrap IP
     * @param callback Optional callback object
     * @return Chord object
     */
    static Chord joinChord(InetAddress bootstrap, @Nullable ChordCallback callback){
        return null;
    }

    /**
     * Create new Chord network
     * @param callback Optional callback object
     * @return Chord object
     */
    static Chord createChord(@Nullable ChordCallback callback){
        return null;
    }



}
