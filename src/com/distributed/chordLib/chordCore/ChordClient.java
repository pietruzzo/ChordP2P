package com.distributed.chordLib.chordCore;




import com.distributed.chordLib.ChordCallback;
import com.distributed.chordLib.chordCore.communication.CommCallInterface;
import com.distributed.chordLib.chordCore.communication.CommCallbackInterface;
import com.distributed.chordLib.chordCore.communication.SocketCommunication;
import com.distributed.chordLib.chordCore.communication.messages.JoinResponseMessage;
import jdk.internal.jline.internal.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class ChordClient implements com.distributed.chordLib.Chord, CommCallbackInterface {

    //region Attributes


    FingerTable fingerTable;
    private ChordCallback callback;
    CommCallInterface comLayer;
    HashFunction hash;

    ThreadPoolExecutor threadPool;

    //endregion

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
    public ChordClient(@Nullable Integer numFingers, @Nullable Integer numSuccessors, @Nullable String bootstrapAddr, @Nullable Integer port, int module, @Nullable ChordCallback callback){
        this.callback = callback;
        this.hash = new HashFunction(module);
        if (port == null) port= DEFAULT_SERVER_PORT;
        comLayer = new SocketCommunication(port, this);

        //handle parameters
        Node successor;
        Integer nFingers;
        Integer nSucc;
        if (bootstrapAddr != null) { //Join case -> get parameters
            JoinResponseMessage message = comLayer.join(new Node(bootstrapAddr, hash.getSHA1(bootstrapAddr)), port);
            successor = message.successor;
            nFingers = message.numFingers;
            nSucc = message.numSuccessors;
        } else{ //create network
            successor = null; //Current node is the only one
            nFingers = numFingers;
            nSucc = numSuccessors;
        }
        if (nFingers == null) nFingers = DEFAULT_NUM_FINGERS;
        if (nSucc == null) nSucc = DEFAULT_NUM_SUCCESSORS;

        //setup network
        fingerTable = new FingerTable(numFingers, numSuccessors, hash);
        fingerTable.setSuccessor(successor);
    }


    /**
     * ask node n to find the successor of id (B - Basic Lookup)
     * @param id identifier
     * @apiNote
     * if (id ∈ (n,successor])
     *  return successor;
     * else
     * // forward the query around the circle
     * return successor.find successor(id);
     */
    protected abstract Node findSuccessorB(String id);


    /**
     * ask node n to find the successor of id
     * @param id identifier
     *
     * if (id ∈ (n,successor])
     *  return successor;
     * else
     *  n'= closest preceding node(id);
     *  return n'.find successor(id);
     * @return successor Node
     */
    protected abstract Node findSuccessor(String id);

    /**
     * search the local table for the highest predecessor of id
     * @param id identifier
     * for i = m downto 1
     *  if (finger[i] ∈ (n,id))
     *      return finger[i];
     *  return n;
     * @return a preceding node for id
     */
    @Deprecated
    protected abstract Node closestPrecedingNode(int id);

    /**
     * Ask for the predecessor of node
     * @param node
     * @return
     */
    protected abstract Node findPredecessor(Node node);

    /**
     * Called periodically, verify successors and notify them if you are their predecessor
     * x =successor.predecessor
     * if(x in (n, successor))
     *  successor.notify(n);
     */
    protected abstract void stabilize();

    /**
     * notify successor that we are him predecessor
     * if(this.predecessor i NULL or predecessor in (this.predecessor, n))
     *  this.predecessor = predecessor
     * @param successor calling predecessor
     */
    protected abstract void notify(Node successor);

    /**
     * called periodically. refreshes ﬁnger table entries.
     * next stores the index of the next ﬁnger to ﬁx.
     * next = next + 1;
     * if (next > m)
     * next = 1;
     * ﬁnger[next] = ﬁnd successor( n + 2^(next-1) );
     */
    protected abstract void fixFingers();

    /**
     * Called periodically, checks whether predecessor has failed
     * if(predecessor has failed)
     *  predecessor = NULL
     */
    protected abstract void checkPredecessor();

    /**
     * Close network
     */
    abstract public void close();


    /**
     * Datastructure use to give initialization parameters
     */
    public static class InitParameters implements Serializable{
        public final int numFingers;
        public final int numSuccessors;
        public final Node successor;
        public final int module;

        InitParameters(int numFingers, int numSuccessors, Node successor, int moduel){
            this.numFingers = numFingers;
            this.numSuccessors = numSuccessors;
            this.successor = successor;
            this.module = moduel;
        }
    }
}