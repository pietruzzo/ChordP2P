package com.distributed.chordLib;


import jdk.internal.jline.internal.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.function.ToDoubleBiFunction;

/**
 * Factory for Chord LIB
 */
public class ChordBuilder {

    private final static String DEFAULT_SERVER_IP = "192.168.0.1";
    private final static int DEFAULT_SERVER_PORT = 1678;

    /**
     * Join Existing Chord network
     * @param bootstrap Bootstrap IP (DEFAULT_SERVER_IP if null)
     * @param port port of serverSocket (try DEFAULT_SERVER_PORT port if not defined)
     * @param callback Optional callback object
     * @return Chord object
     * @throws IOException for communication failure
     */
    static Chord joinChord(@Nullable InetAddress bootstrap, @Nullable int port,  @Nullable ChordCallback callback) throws IOException {
        /*TODO:
                -   spawn new thread for chord network [a thread for bootstrap node to accept incoming requests]
                -   contact remote serversocket
                -   build an instance of Chord
                -   Return Chord instance
         */
        return null;
    }

    /**
     * Create new Chord network
     * @param port port of ServerSocket (DEFAULT_SERVER_PORT if null)
     * @param callback Optional callback object
     * @return Chord object
     */
    static Chord createChord(@Nullable int port, @Nullable ChordCallback callback){
        /*TODO:
                -   spawn new thread for chord network [a thread for bootstrap node to accept incoming requests]
                -   create serversocket
                -   build an instance of Chord
                -   Return Chord instance
                -   Manage node disconnection
         */
        return null;
    }



}
