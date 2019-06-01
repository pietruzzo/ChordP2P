package com.distributed.chordLib.chordCore.communication.messages;

import com.distributed.chordLib.chordCore.Node;

import java.io.Serializable;

public class VoluntaryDepartureMessage extends Message implements Serializable {

    private static final long serialVersionUID = 40010L;

    private Node predecessor;
    private Node leaving;
    private Node successor;

    /**
     * Assign an id to a request
     * if null, an id will be automatically assigned
     *
     * @param predecessor predecessor of leaving node
     * @param leavingNode leaving node
     * @param successor successor of leaving node
     */

    public VoluntaryDepartureMessage(Node predecessor, Node leavingNode, Node successor) {
        super(null);
        this.leaving = leavingNode;
        this.predecessor = predecessor;
        this.successor = successor;
    }

    public Node getLeaving() {
        return leaving;
    }

    public Node getPredecessor() {
        return predecessor;
    }

    public Node getSuccessor() {
        return successor;
    }
}
