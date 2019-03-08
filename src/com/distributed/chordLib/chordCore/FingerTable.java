package com.distributed.chordLib.chordCore;

public class FingerTable {

    private Node[] fingers;

    FingerTable (int nFingers){
            fingers = new Node[nFingers];
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

}
