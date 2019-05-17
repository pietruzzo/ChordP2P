package com.distributed.chordLib.chordCore;

import com.distributed.chordLib.Chord;
import com.distributed.chordLib.ChordCallback;
import com.distributed.chordLib.exceptions.CommunicationFailureException;
import com.distributed.chordLib.exceptions.NoSuccessorsExceptions;
import com.distributed.chordLib.exceptions.TimeoutReachedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.distributed.chordLib.chordCore.HashFunction.*;


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
    protected Node findSuccessorB(Hash hashed_key) {
        Node predecessor = fingerTable.getPredecessor();
        Node myNode = fingerTable.getMyNode();

        if (predecessor != null && hash.areOrdered(predecessor.getkey(), hashed_key, myNode.getkey())){
            return myNode; //Look if I'm responsible for the key
        }
        else {
            Node successor = null;
            try {
                successor = fingerTable.getSuccessor();
                return comLayer.findSuccessorB(successor, hashed_key);
            } catch (NoSuccessorsExceptions e){
                this.closeNetwork();
                throw e;
            }
            catch (CommunicationFailureException e) { //Lookup on failed node, remove node and retry
                fingerTable.removeFailedNode(successor);
                return findSuccessorB(hashed_key);
            }

        }
    }

    @Override
    protected Node findSuccessor(Hash hashed_key) {

        Node nextNode = null;

        if (hashed_key.compareTo(fingerTable.getMyNode().getkey()) == 0) return fingerTable.getMyNode();

        try{
            //If n is responsible, or among n ad s or s, return successor
            if (
                    hash.areOrdered(fingerTable.getMyNode().getkey(), hashed_key, fingerTable.getSuccessor().getkey())
                    || fingerTable.getSuccessor().getkey().compareTo(fingerTable.getMyNode().getkey()) == 0
            ) {
            return fingerTable.getSuccessor(); //Successor is responsible if key inside (n, successor]
            }

            nextNode = closestPrecedingNode(hashed_key);
            if (nextNode == null) nextNode = fingerTable.getSuccessor();
            return comLayer.findSuccessor(nextNode, hashed_key);


        } catch (NoSuccessorsExceptions e){ //Finger table has no successors
            this.closeNetwork();
            throw e;
        } catch (CommunicationFailureException e) { //Lookup on failed node, remove node and retry
            fingerTable.removeFailedNode(nextNode);
            return findSuccessor(hashed_key);
        }
    }

    @Override
    protected Node closestPrecedingNode(Hash hashed_key) {
        //Get preceding finger
        for (int i = fingerTable.getNumFingers() - 1; i >= 0; i--) {
            if (fingerTable.getFinger(i) != null && hash.areOrdered(fingerTable.getMyNode().getkey(), fingerTable.getFinger(i).getkey(), hashed_key))
                return fingerTable.getFinger(i);
        }
        return null;
    }


    @Override
    protected void stabilize() {
        Node myN = fingerTable.getMyNode();
        Node s = fingerTable.getSuccessor();


        //Get first not failed successor that it's not me
        while (!s.equals(myN) && !comLayer.isAlive(s)) {
            if (!s.equals(myN)) {
                fingerTable.removeFailedNode(s);
            }
            s = fingerTable.getSuccessor();
        }

            if (!s.equals(myN)) {
                Node x = comLayer.findPredecessor(s);
                if (x != null) { //If my successor knows a predecessor
                    if (hash.areOrdered(myN.getkey(), x.getkey(), s.getkey())) {
                        fingerTable.setSuccessor(x);
                    }
                }
                notify(fingerTable.getSuccessor());

                Node succ = fingerTable.getSuccessor();
                for (int i = 0; i < fingerTable.getNumSuccessors(); i++) {
                    succ = findSuccessor(hash.moduloSum(succ.getkey(), 1));

                    fingerTable.setSuccessor(succ);

                    //Stop if number of nodes in network are less than Num of Successors
                    if (succ.equals(myN)) break;
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
            fingerTable.setFinger(findSuccessor(hash.moduloSum(fingerTable.getMyNode().getkey(), (long) Math.pow(2, (next-1)))),i);
        }

    }

    @Override
    protected void checkPredecessor() {
        Node predecessor = fingerTable.getPredecessor();
        if (predecessor!= null && !comLayer.isAlive(predecessor)) fingerTable.setPredecessor(null);
    }


    @Override
    public String lookupKey(String key) throws CommunicationFailureException, TimeoutReachedException {
        Hash hashedKey = hash.getSHA1(key); //Get hash of the key for lookup
        String response = null;
        for (int i = 0; i < Chord.DEFAULT_RETRY-1; i++) { //Retry lookup Chord.DEFAULT_RETRY times
            try {
                response = this.findSuccessor(hashedKey).getIP();
            } catch (TimeoutReachedException | CommunicationFailureException e){
                response = null;
            }
            if(response != null) break;
        }
        if (response == null) response= this.findSuccessor(hashedKey).getIP();

        System.out.println("___________________");
        System.out.println("Lookup result: " + response + " (mynode: " + fingerTable.getMyNode().getIP() + ")" );
        System.out.println("myNode hash: " + fingerTable.getMyNode().getkey().toString());
        System.out.println("mySucc hash: " + fingerTable.getSuccessor().getkey().toString());
        System.out.println("given  hash: " + hashedKey);
        System.out.println("my, key, succ: " + hash.areOrdered(fingerTable.getMyNode().getkey(), hashedKey, fingerTable.getSuccessor().getkey()));
        System.out.println("___________________");
        if (response!= null && response.compareTo(fingerTable.getMyNode().getIP())==0) response = "127.0.0.1";
        return response;
    }

    @Override
    public String lookupKeyBasic(String key) throws CommunicationFailureException, TimeoutReachedException {
        Hash hashedKey = hash.getSHA1(key); //Get hash of the key for lookup
        String response = null;
        for (int i = 0; i < Chord.DEFAULT_RETRY-1; i++) { //Retry lookup Chord.DEFAULT_RETRY times
            try {
                response = this.findSuccessorB(hashedKey).getIP();
            } catch (TimeoutReachedException | CommunicationFailureException e){
                response = null;
            }
            if(response != null) break;
        }
        if (response == null) response= this.findSuccessorB(hashedKey).getIP();
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
        return new InitParameters(fingerTable.getNumFingers(), fingerTable.getNumSuccessors(), findSuccessor(hash.getSHA1(IP)), hash.getM());
    }

    @Override
    public Node handleLookupB(Hash key) {
        return this.findSuccessorB(key);
    }

    @Override
    public Node handleLookup(Hash key) {
        return this.findSuccessor(key);
    }

    @Override
    public void notifyIncoming(Node predecessor) {

        if (!fingerTable.successoIsFull()){
            //Add node to successor list
            fingerTable.setSuccessor(predecessor);
        }

        if (fingerTable.getPredecessor()==null ||
                hash.areOrdered(fingerTable.getPredecessor().getkey(), predecessor.getkey(), fingerTable.getMyNode().getkey())){
            fingerTable.setPredecessor(predecessor);

            if (chordCallback!= null)
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        chordCallback.notifyResponsabilityChange();
                    }
                }).start();


        }

    }

    @Override
    public Node handlePredecessorRequest() {
        return fingerTable.getPredecessor();
    }

    @Override
    public Hash getkey(String ip) {
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
