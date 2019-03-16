package com.distributed.chordLib.chordCore.communication;

import com.distributed.chordLib.ChordCallback;
import com.distributed.chordLib.chordCore.ChordClient;
import com.distributed.chordLib.chordCore.Node;
import com.distributed.chordLib.exceptions.CommunicationFailureException;
import com.distributed.chordLib.exceptions.TimeoutReachedException;

import java.net.InetAddress;

public class ChordEngine extends ChordClient {

    /**
     * Contructor for chord Network
     *
     * @param numFingers    Number of fingers in finger table
     * @param numSuccessors Number of stored successors
     * @param bootstrapAddr to Join an existing ChordClient Network
     *                      NULL: create a new Network
     * @param callback      Optional Callback for application
     */
    public ChordEngine(int numFingers, int numSuccessors, String bootstrapAddr, int port, ChordCallback callback) {
        super(numFingers, numSuccessors, bootstrapAddr, port, callback);
    }

    @Override
    protected Node findSuccessorB(int id) {
        return null;
    }

    @Override
    protected Node findSuccessor(int id) {
        return null;
    }

    @Override
    protected Node closestPrecedingNode(int id) {
        return null;
    }

    @Override
    protected void stabilize() {

    }

    @Override
    protected void notify(Node predecessor) {

    }

    @Override
    protected void fixFingers() {

    }

    @Override
    protected void checkPredecessor() {

    }

    @Override
    public void close() {

    }

    @Override
    public String lookupKey(String key) throws CommunicationFailureException, TimeoutReachedException {
        return null;
    }

    @Override
    public String lookupKeyBasic(String key) throws CommunicationFailureException, TimeoutReachedException {
        return null;
    }

    @Override
    public InitParameters handleJoinRequest(String IP) {
        return null;
    }

    @Override
    public Node handleLookupB(String key) {
        return null;
    }

    @Override
    public Node handleLookup(String key) {
        return null;
    }

    @Override
    public void notifyIncoming(Node predecessor) {

    }

    @Override
    public void ping() {

    }
}
