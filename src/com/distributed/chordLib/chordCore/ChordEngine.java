package com.distributed.chordLib.chordCore;

import com.distributed.chordLib.ChordCallback;
import com.distributed.chordLib.exceptions.CommunicationFailureException;
import com.distributed.chordLib.exceptions.TimeoutReachedException;

import java.util.ArrayList;
import java.util.List;

public class ChordEngine extends ChordClient {

    /**
     * Contructor for chord Network
     * Initialize Chord FingerTable, communication Layer and open connection to bootstrapIP (or create a new Chord Network if bootstrap is null)
     * @param numFingers Number of fingers in finger table (if null, use default)
     * @param numSuccessors Number of stored successors (if null use default)
     * @param bootstrapAddr to Join an existing ChordClient Network
     *                      NULL: create a new Network
     * @param port port for the chord network (if null use default)
     * @param module module of Chord Ring
     * @param callback Optional Callback for application
     */
    public ChordEngine(int numFingers, int numSuccessors, String bootstrapAddr, int port, int module, ChordCallback callback) {
        super(numFingers, numSuccessors, bootstrapAddr, port, module, callback);
    }

    @Override
    protected Node findSuccessorB(int id) {
        Node predecessor = fingerTable.getPredecessor();
        Node myNode = fingerTable.getMyNode();
        String objectKey = hash.getSHA1(String.valueOf(id));
        if (predecessor != null && hash.compare(objectKey,predecessor.getkey())==1 && hash.compare(myNode.getkey(),objectKey)==1){
            return myNode; //Look if I'm responsible for the key
        }
        else return comLayer.findSuccessorB(fingerTable.getSuccessor(), String.valueOf(id));
    }

    @Override
    protected Node findSuccessor(int id) {
        return fingerTable.getNextNode(String.valueOf(id));
    }

    @Override @Deprecated
    protected Node closestPrecedingNode(int id) {
        return null;
    }

    @Override
    protected Node findPredecessor(Node node) {
        return null;
    }

    @Override
    protected void stabilize() {
        Node n = fingerTable.getMyNode();
        Node s = fingerTable.getSuccessor();
        Node x = comLayer.findPredecessor(n);
        if (hash.compare(n.getkey(), s.getkey()) == 1 && hash.compare(s.getkey(), x.getkey()) == 1){
            fingerTable.setSuccessor(x, null);
            notify(x);
        }

        //TODO populate successor list

    }

    @Override
    protected void notify(Node successor) {
        comLayer.notifySuccessor(successor, fingerTable.getMyNode());
    }

    @Override
    protected void fixFingers() {

        int m = hash.getM();
        int next = 0; //Next finger to fix

        for (int i = 0; i < fingerTable.getNumFingers(); i++) {
            next = next + 1;
            if (next > m) next = 1;
            fingerTable.setFinger(findSuccessor(Integer.valueOf(fingerTable.getMyNode().getkey()+ (Math.pow(2, (next-1))))),i);
        }

    }

    @Override
    protected void checkPredecessor() {
        if (!comLayer.isAlive(fingerTable.getPredecessor())) fingerTable.setPredecessor(null);
    }

    @Override
    public void close() {
        List<Node> allNodes = new ArrayList();
        allNodes.add(fingerTable.getPredecessor());
        allNodes.add(fingerTable.getSuccessor());
        for (int i = 0; i < fingerTable.getNumFingers(); i++) {
            allNodes.add(fingerTable.getFinger(i));
        }
        while(allNodes.contains(null)) {
            allNodes.remove(null);
        }
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
    public Node handlePredecessorRequest() {
        return null;
    }
}