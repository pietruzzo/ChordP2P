package com.distributed.chordLib;


import com.distributed.chordLib.chordCore.ChordClient;
import com.distributed.chordLib.chordCore.ChordEngine;
import com.distributed.chordLib.chordCore.communication.messages.JoinRequestMessage;
import com.distributed.chordLib.chordCore.communication.messages.JoinResponseMessage;
import jdk.internal.jline.internal.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
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
    public static Chord joinChord(@Nullable String bootstrap, @Nullable Integer port,  @Nullable ChordCallback callback) throws IOException {

        Socket endpoint = null;
        if (bootstrap == null) bootstrap = Chord.DEFAULT_SERVER_IP;
        if (port == null) port = Chord.DEFAULT_SERVER_PORT;
        return new ChordEngine(null, null, bootstrap, port, null, callback );
    }

    /**
     * Create new ChordClient network
     * @param port port of ServerSocket (DEFAULT_SERVER_PORT if null)
     * @param numFingers optionally specify number of fingers for network
     * @param numSuccessors optionally specify numberOfSuccessors;
     * @param module optionally specify module for key
     * @param callback Optional callback object
     * @return ChordClient object
     */
    public static Chord createChord(@Nullable Integer port, @Nullable Integer numFingers, @Nullable Integer numSuccessors,@Nullable Integer module, @Nullable ChordCallback callback){

        if (port == null) port = Chord.DEFAULT_SERVER_PORT;
        if (numFingers == null) numFingers = Chord.DEFAULT_NUM_FINGERS;
        if (numSuccessors == null) numSuccessors = Chord.DEFAULT_NUM_SUCCESSORS;
        if (module == null) module = Chord.DEFAULT_CHORD_MODULE;

        return new ChordEngine(numFingers, numSuccessors, null, port, module, callback );
    }



}
