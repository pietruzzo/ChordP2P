package com.distributed.chordLib.chordCore;

import com.distributed.chordLib.Chord;
import com.distributed.chordLib.exceptions.UnableToGetMyIPException;
import jdk.internal.jline.internal.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;


public class FingerTable {

    private Node[] fingers;
    private Node[] successors;
    private Node predecessor;
    private Node myNode;

    FingerTable (int nFingers, @Nullable Integer nSuccessors) {
        fingers = new Node[nFingers];
        if (nSuccessors != null)
            successors = new Node[nSuccessors];
        else successors = new Node[1];

        try {
            if (Chord.USE_PUBLIC_IP) myNode = new Node(getmyPublicIPAddress());
            else myNode = new Node(getmyPrivateIPAddress());
        } catch (IOException e) {
            throw new UnableToGetMyIPException();
        }
    }

    /**
     * @return successor of this node
     */
    public Node getSuccessor(){ return fingers[0]; }

    /**
     * Find the Node among Node, its successors and fingers most appropriate for key
     * @param id (not hashed yet)
     * @return Node in FingerTable or Node itself
     */
    public Node getNextNode(String id) {
        String objectKey = HashFunction.getSHA1(id);
        if (predecessor != null && HashFunction.compare(objectKey,predecessor.getkey())==1 && HashFunction.compare(myNode.getkey(),objectKey)==1){
            return myNode; //Look if I'm responsible for the key
        }
        //Look into my successors
        Node previous = myNode;
        Node next = getPredecessor();
        for (int i = 0; i < successors.length; i++) {
            next= successors[i];
            if (next != null && HashFunction.compare(objectKey,previous.getkey())==1 && HashFunction.compare(next.getkey(),objectKey)==1){
                return next; //Look if I'm responsible for the key
            }
            if (next != null) previous = next;
        }
        //Look into finger Table
        previous = myNode;
        for (int i = 0; i < fingers.length; i++) {
            next= fingers[i];
            if (next != null && HashFunction.compare(objectKey,previous.getkey())==1 && HashFunction.compare(next.getkey(),objectKey)==1){
                return next; //Look if I'm responsible for the key
            }
            if (next != null) previous = next;
        }
        return next; //Max finger
    }

    public Node getPredecessor(){ return predecessor; }

    public void setPredecessor(Node predecessor) {this.predecessor = predecessor; }

    public void setSuccessor(Node successor, @Nullable Integer position) {
        if (position == null) position = 0;
        this.successors[position] = successor;
    }


    public Node getMyNode() {
        return myNode;
    }


    private String getmyPublicIPAddress() throws IOException {
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                whatismyip.openStream()));

        return in.readLine();
    }

    private String getmyPrivateIPAddress() throws IOException {
        InetAddress inetAddress = InetAddress.getLocalHost();
        return inetAddress.getHostAddress();
    }
}
