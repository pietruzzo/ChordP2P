package com.distributed.chordLib.chordCore;




import com.distributed.chordLib.chordCore.FingerTable;
import com.distributed.chordLib.chordCore.Node;

import java.net.InetAddress;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class ChordNetwork {

    //region Attributes

    private Node myNode;

    private Node[] successor;
    private Node predecessor;
    private FingerTable fingerTable;

    ThreadPoolExecutor threadPool;

    //endregion

    //region Constructors
    ChordNetwork(int numFingers, int numSuccessors){

    }

    ChordNetwork(int numFingers, int numSuccessors, InetAddress bootstrapAddr){

    }
    //endregion

    /**
     * Lookup for key in chord network
     * @return IP address of node responsible for key
     */
    abstract public InetAddress lookupKey(String key);

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
     * Called periodically, checks whether predecessor has faild
     * if(predecessor has failed)
     *  predecessor = NULL
     */
    abstract void fixFingers();

    /**
     * Close network
     */
    abstract public void close();

    /**
     * calculate SHA-1 of a String
     * @implNote with org.apache.commons.codec.digest.DigestUtils
     * @return digested string
     */
    abstract String getSHA1(String inputString);
}
