package com.distributed.chordLib.chordCore;

import com.distributed.chordLib.Chord;
import com.distributed.chordLib.ChordCallback;
import com.distributed.chordLib.exceptions.CommunicationFailureException;
import com.distributed.chordLib.exceptions.NoSuccessorsExceptions;
import com.distributed.chordLib.exceptions.TimeoutReachedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


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
            Node successor = null;
            try {
                successor = fingerTable.getSuccessor();
                return comLayer.findSuccessorB(successor, String.valueOf(id));
            } catch (NoSuccessorsExceptions e){
                this.closeNetwork();
                throw e;
            }
            catch (CommunicationFailureException e) { //Lookup on failed node, remove node and retry
                fingerTable.removeFailedNode(successor);
                return findSuccessorB(id);
            }

        }
    }

    @Override
    protected Node findSuccessor(String id) {

        Node nextNode = null;

        try{
            if (id == fingerTable.getMyNode().getIP()) {//I'm looking for myself
            return fingerTable.getMyNode();
            }
            if (hash.areOrdered(fingerTable.getMyNode().getkey(), hash.getSHA1(id), fingerTable.getSuccessor().getkey())){
            return fingerTable.getSuccessor(); //Successor is responsible
            }

            nextNode = closestPrecedingNode(id);
            return comLayer.findSuccessor(nextNode, id);


        } catch (NoSuccessorsExceptions e){ //Finger table has no successors
            this.closeNetwork();
            throw e;
        } catch (CommunicationFailureException e) { //Lookup on failed node, remove node and retry
            fingerTable.removeFailedNode(nextNode);
            return findSuccessor(id);
        }
    }

    @Override
    protected Node closestPrecedingNode(String id) {
        //Get preceding finger
        for (int i = fingerTable.getNumFingers() - 1; i >= 0; i--) {
            if (fingerTable.getFinger(i) != null && hash.areOrdered(fingerTable.getMyNode().getkey(), fingerTable.getFinger(i).getkey(), hash.getSHA1(id)))
                return fingerTable.getFinger(i);
        }
        return null;
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
            if (x != null) { //If my successor knows a predecessor
                if (hash.areOrdered(myN.getkey(), x.getkey(), s.getkey())) {
                    fingerTable.setSuccessor(x);
                }
            }
            notify(fingerTable.getSuccessor());

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
        if (predecessor!= null && !comLayer.isAlive(predecessor)) fingerTable.setPredecessor(null);
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
        comLayer.closeCommLayer();
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
        if (fingerTable.getPredecessor()==null ||
                hash.areOrdered(fingerTable.getPredecessor().getkey(), predecessor.getkey(), fingerTable.getMyNode().getkey())){
            fingerTable.setPredecessor(predecessor);
            if (chordCallback!= null)
                chordCallback.notifyResponsabilityChange();
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

    /**
     * Method that executes all routines actions every n milliseconds
     */
    private void routineActions(){
        while (!stopRoutine) {

            try {
                synchronized (this){
                this.wait(ROUTINE_PERIOD);
                }
            } catch (InterruptedException e) {
                System.out.println("Routine actions Stopped");
            }
            try {
                System.out.println("Fix Fingers:---");
                fixFingers();
                System.out.println("Stabilize:---");
                stabilize();
                System.out.println("Check predecessor:---");
                checkPredecessor();
                System.out.println("End Routine:---");
                fingerTable.printFingerTable();
            } catch(CommunicationFailureException e){
                System.out.println("Routine action failed, retry in 2 seconds");
            } catch (TimeoutReachedException e) {
                //Consider as failed node
                fingerTable.removeFailedNode(new Node(e.getWaitingNode(), hash.getSHA1(e.getWaitingNode())));
            }
            catch (NoSuccessorsExceptions e){
                this.closeNetwork();
            }
        }
    }
}
