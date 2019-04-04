package com.distributed.chordLib.chordCore;

import com.distributed.chordLib.Chord;
import com.distributed.chordLib.ChordCallback;
import com.distributed.chordLib.exceptions.CommunicationFailureException;
import com.distributed.chordLib.exceptions.NoSuccessorsExceptions;
import com.distributed.chordLib.exceptions.TimeoutReachedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ChordEngine extends ChordClient {

    private volatile boolean stopRoutine = false;
    /**
     * Contructor for chord Network
     * Initialize Chord FingerTable, communication Layer and open connection to bootstrapIP (or create a new Chord Network if bootstrap is null)
     * @param numFingers Number of fingers in finger table (if null, use default)
     * @param numSuccessors Number of stored successors (if null use default)
     * @param bootstrapAddr to Join an existing ChordClient Network
     *                      NULL: create a new Network
     * @param port port for the chord network
     * @param module module of Chord Ring
     * @param callback Optional Callback for application
     */
    public ChordEngine(@Nullable Integer numFingers, @Nullable Integer numSuccessors, @Nullable String bootstrapAddr, int port, @Nullable Integer module, ChordCallback callback) {
        super(numFingers, numSuccessors, bootstrapAddr, port, module, callback);
        stabilize();
        fixFingers();
        Thread routine = new Thread(this::routineActions);
        routine.start();
    }

    @Override
    protected Node findSuccessorB(String id) {
        Node predecessor = fingerTable.getPredecessor();
        Node myNode = fingerTable.getMyNode();
        String objectKey = hash.getSHA1(String.valueOf(id));
        if (predecessor != null && hash.areOrdered(predecessor.getkey(), objectKey, myNode.getkey())){
            return myNode; //Look if I'm responsible for the key
        }
        else {
            try {
                return comLayer.findSuccessorB(fingerTable.getSuccessor(), String.valueOf(id));
            } catch (NoSuccessorsExceptions e){
                this.closeNetwork();
                throw e;
            }

        }
    }

    @Override
    protected Node findSuccessor(String id) {
        try{
            return fingerTable.getNextNode(String.valueOf(id));
        } catch (NoSuccessorsExceptions e){
            this.closeNetwork();
            throw e;
        }

    }

    @Override @Deprecated
    protected Node closestPrecedingNode(int id) {
        return null;
    }

    @Override
    protected Node findPredecessor(Node node) {
        return comLayer.findPredecessor(node);
    }

    @Override
    protected void stabilize() {
        Node myN = fingerTable.getMyNode();
        Node s = fingerTable.getSuccessor();

        if (s != myN) { //If I'm not the only node in the network
            //Get first not failed successor
            while (!comLayer.isAlive(s)) {
                fingerTable.removeFailedNode(s);
                s = fingerTable.getSuccessor();
            }

            Node x = comLayer.findPredecessor(s);
            if (x != null && hash.areOrdered(myN.getkey(), x.getkey(), s.getkey())) {
                fingerTable.setSuccessor(x);
            }
            notify(x);

            Node succ = fingerTable.getSuccessor();
            for (int i = 0; i < fingerTable.getNumSuccessors(); i++) {
                succ = findSuccessor(succ.getkey());
                if (succ.equals(myN)) break; //Stop If number of nodes in network are less than Num of Successors
                fingerTable.setSuccessor(succ);
            }
        }

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
            fingerTable.setFinger(findSuccessor(fingerTable.getMyNode().getkey()+ (Math.pow(2, (next-1)))),i);
        }

    }

    @Override
    protected void checkPredecessor() {
        Node predecessor = fingerTable.getPredecessor();
        if (predecessor!= null && comLayer.isAlive(predecessor)) fingerTable.setPredecessor(null);
    }


    @Override
    public String lookupKey(String key) throws CommunicationFailureException, TimeoutReachedException {
        String response = null;
        for (int i = 0; i < Chord.DEFAULT_RETRY-1; i++) { //Retry lookup Chord.DEFAULT_RETRY times
            try {
                response = this.findSuccessor(key).getIP();
            } catch (TimeoutReachedException | CommunicationFailureException e){
                response = null;
            }
            if(response != null) break;
        }
        if (response == null) response= this.findSuccessor(key).getIP();
        if (response!= null && response.compareTo(fingerTable.getMyNode().getIP())==0) response = "127.0.0.1";
        return response;
    }

    @Override
    public String lookupKeyBasic(String key) throws CommunicationFailureException, TimeoutReachedException {
        String response = null;
        for (int i = 0; i < Chord.DEFAULT_RETRY-1; i++) { //Retry lookup Chord.DEFAULT_RETRY times
            try {
                response = this.findSuccessorB(key).getIP();
            } catch (TimeoutReachedException | CommunicationFailureException e){
                response = null;
            }
            if(response != null) break;
        }
        if (response == null) response= this.findSuccessorB(key).getIP();
        if (response!= null && response.compareTo(fingerTable.getMyNode().getIP())==0) response = "127.0.0.1";
        return response;
    }

    @Override
    public void closeNetwork() {
        stopRoutine = true;
        comLayer.closeChannel(null);
    }

    @Override
    public InitParameters handleJoinRequest(String IP) {
        return new InitParameters(fingerTable.getNumFingers(), fingerTable.getNumSuccessors(), findSuccessor(IP), hash.getM());
    }

    @Override
    public Node handleLookupB(String key) {
        return this.findSuccessorB(key);
    }

    @Override
    public Node handleLookup(String key) {
        return this.findSuccessor(key);
    }

    @Override
    public void notifyIncoming(Node predecessor) {
        String myPredecessorKey = fingerTable.getPredecessor().getkey();
        if (fingerTable.getPredecessor()==null ||
                hash.areOrdered(myPredecessorKey, predecessor.getkey(), fingerTable.getMyNode().getkey())){
            fingerTable.setPredecessor(predecessor);
            if (chordCallback!= null)
                chordCallback.notifyResponsabilityChange(predecessor.getkey(), fingerTable.getMyNode().getkey());
        }
    }

    @Override
    public Node handlePredecessorRequest() {
        return fingerTable.getPredecessor();
    }

    @Override
    public String getkey(String ip) {
        return hash.getSHA1(ip);
    }

    private void routineActions(){
        while (!stopRoutine) {

            try {
                synchronized (this){
                this.wait(2000);
                }
            } catch (InterruptedException e) {
                System.out.println("Routine actions Stopped");
            }
            try {
                fixFingers();
                stabilize();
                checkPredecessor();
            } catch(CommunicationFailureException e){
                System.out.println("Routine action failed, retry in 2 seconds");
            } catch (NoSuccessorsExceptions e){
                this.closeNetwork();
            }
        }
    }
}
