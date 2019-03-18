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
    private HashFunction hash;

    FingerTable (int nFingers, @Nullable Integer nSuccessors, HashFunction hash) {
        this.hash = hash;
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
        String objectKey = hash.getSHA1(id);
        if (predecessor != null && hash.compare(objectKey,predecessor.getkey())==1 && hash.compare(myNode.getkey(),objectKey)==1){
            return myNode; //Look if I'm responsible for the key
        }

        Node previous = myNode;
        Node next = null;
        //Look into finger Table
        previous = myNode;
        for (int i = 0; i < fingers.length; i++) {
            next= fingers[i];
            if (next != null && hash.compare(objectKey,previous.getkey())==1 && hash.compare(next.getkey(),objectKey)==1){
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

    /**
     * get number of fingers
     */
    public int getNumFingers(){ return fingers.length; }

    public Node getFinger(int position){ return fingers[position]; }

    public void setFinger(Node node, int position){ fingers[position]= node; }


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
