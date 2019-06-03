package com.distributed.chordLib;

import com.distributed.chordLib.exceptions.CommunicationFailureException;
import com.distributed.chordLib.exceptions.NoSuccessorsExceptions;
import com.distributed.chordLib.exceptions.TimeoutReachedException;

import java.net.InetAddress;

/**
 * External interface of ChordNetworkInterface Client Library
 * Uses SHA-1 to hash IPs and values
 */
public interface Chord {

    //region: ChordLib parameters

    int DEFAULT_NUM_FINGERS = 19;
    int DEFAULT_NUM_SUCCESSORS = 1;
    String DEFAULT_SERVER_IP = "127.0.0.1";
    int DEFAULT_SERVER_PORT = 1678;
    boolean USE_PUBLIC_IP = false;
    int DEFAULT_RETRY = 5; //number of retrial lookup in case of failures (it can mask transient failures)
    int DEFAULT_CHORD_MODULE = 20; //Expressed in bit length
    int ROUTINE_PERIOD = 20000;

    //endregion

    /**
     * Lookup for key in chord network
     * @return IP address of node responsible for key
     * @throws CommunicationFailureException for socket failures
     * @throws TimeoutReachedException timeout in request handling reached
     * @throws NoSuccessorsExceptions there is no living successor (Ring is unstable, actual ChordNode is Killed)
     */
    String lookupKey(String key) throws CommunicationFailureException, TimeoutReachedException, NoSuccessorsExceptions;

    /**
     * BasicLookup for key in chord network
     * @return IP address of node responsible for key
     * @throws CommunicationFailureException for socket failures
     * @throws TimeoutReachedException timeout in request handling reached
     * @throws NoSuccessorsExceptions there is no living successor (Ring is unstable, actual ChordNode is Killed)
     */
    String lookupKeyBasic(String key) throws CommunicationFailureException, TimeoutReachedException, NoSuccessorsExceptions;

    /**
     * Close ChordLib notifying it to the network
     */
    void closeNetwork();

}
