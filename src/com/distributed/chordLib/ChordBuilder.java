package com.distributed.chordLib;


import jdk.internal.jline.internal.Nullable;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.function.ToDoubleBiFunction;

/**
 * Factory for ChordClient LIB
 */
public class ChordBuilder {



    /**
     * Join Existing ChordClient network
     * @param bootstrap Bootstrap IP (DEFAULT_SERVER_IP if null)
     * @param port port of serverSocket (try DEFAULT_SERVER_PORT port if not defined)
     * @param callback Optional callback object
     * @return ChordClient object
     * @throws IOException for communication failure
     */
    public static Chord joinChord(@Nullable String bootstrap, @Nullable int port,  @Nullable ChordCallback callback) throws IOException {
        /*TODO:
                -   spawn new thread for chord network [a thread for bootstrap node to accept incoming requests]
                -   contact remote serversocket
                -   build an instance of ChordClient
                -   Return ChordClient instance
         */
        return null;
    }

    /**
     * Create new ChordClient network
     * @param port port of ServerSocket (DEFAULT_SERVER_PORT if null)
     * @param callback Optional callback object
     * @return ChordClient object
     */
    public static Chord createChord(@Nullable int port, @Nullable ChordCallback callback){
        /*TODO:
                -   spawn new thread for chord network [a thread for bootstrap node to accept incoming requests]
                -   create serversocket
                -   build an instance of ChordClient
                -   Return ChordClient instance
                -   Manage node disconnection
         */
        return null;
    }



}
