package com.distributed.chordLib.chordCore;




import com.distributed.chordLib.ChordCallback;
import com.distributed.chordLib.chordCore.communication.CommCallbackInterface;
import jdk.internal.jline.internal.Nullable;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class ChordClient implements com.distributed.chordLib.Chord, CommCallbackInterface {

    //region Attributes

    private Node myNode;

    private FingerTable fingerTable;
    private ChordCallback callback;

    ThreadPoolExecutor threadPool;

    //endregion

    /**
     * Contructor for chord Network
     * @param numFingers Number of fingers in finger table
     * @param numSuccessors Number of stored successors
     * @param bootstrapAddr to Join an existing ChordClient Network
     *                      NULL: create a new Network
     * @param callback Optional Callback for application
     */
    ChordClient(int numFingers, int numSuccessors, @Nullable InetAddress bootstrapAddr, @Nullable ChordCallback callback){

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
    abstract Node findSuccessorB(int id);


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
    abstract Node findSuccessor(int id);

    /**
     * search the local table for the highest predecessor of id
     * @param id identifier
     * for i = m downto 1
     *  if (finger[i] ∈ (n,id))
     *      return finger[i];
     *  return n;
     * @return a preceding node for id
     */
    abstract Node closestPrecedingNode(int id);

    /**
     * Called periodically, verify n successor
     * x =successor.predecessor
     * if(x in (n, successor))
     *  successor.notify(n);
     */
    abstract void stabilize();

    /**
     * predecessor thinks it might be our predecessor
     * if(this.predecessor i NULL or predecessor in (this.predecessor, n))
     *  this.predecessor = predecessor
     * @param predecessor calling predecessor
     */
    abstract void notify(Node predecessor);

    /**
     * called periodically. refreshes ﬁnger table entries.
     * next stores the index of the next ﬁnger to ﬁx.
     * next = next + 1;
     * if (next > m)
     * next = 1;
     * ﬁnger[next] = ﬁnd successor( n + 2^(next-1) );
     */
    abstract void fixFingers();

    /**
     * Called periodically, checks whether predecessor has failed
     * if(predecessor has failed)
     *  predecessor = NULL
     */
    abstract void checkPredecessor();

    /**
     * Close network
     */
    abstract public void close();


    public static class InitParameters implements Serializable{
        public final int numFingers;
        public final int numSuccessors;
        public final Node successor;

        InitParameters(int numFingers, int numSuccessors, Node successor){
            this.numFingers = numFingers;
            this.numSuccessors = numSuccessors;
            this.successor = successor;
        }
    }
}