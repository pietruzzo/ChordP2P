package com.distributed.chordLib.chordCore;

import jdk.internal.jline.internal.Nullable;

public class FingerTable {

    private Node[] fingers;
    private Node[] successors;
    private Node predecessor;

    FingerTable (int nFingers, @Nullable Integer nSuccessors){
        fingers = new Node[nFingers];
        if (nSuccessors != null)
            successors = new Node[nSuccessors];
        else successors = new Node[1];
    }

    /**
     * @return successor of this node
     */
    public Node getSuccessor(){ return fingers[0]; }

    /**
     * Find the Node among Node and its successors most appropriate for key
     * @param key
     * @return Node in FingerTable or Node itself
     */
    public Node[] getNextNode(String key) {
        return null;
    }

    public Node getPredecessor(){ return predecessor; }

    public void setPredecessor(Node predecessor) {this.predecessor = predecessor; }

    public void setSuccessor(Node successor, @Nullable Integer position) {
        if (position == null) position = 0;
        this.successors[position] = successor;
    }


}
