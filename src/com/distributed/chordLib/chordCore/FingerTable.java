package com.distributed.chordLib.chordCore;

import com.distributed.chordLib.Chord;
import com.distributed.chordLib.exceptions.UnableToGetMyIPException;
import jdk.internal.jline.internal.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class FingerTable {

    private Node[] fingers;
    private List<Node> successors;
    private Node predecessor;
    private Node myNode;
    private HashFunction hash;
    private int numSuccessors;

    FingerTable (int nFingers, @Nullable Integer nSuccessors, HashFunction hash) {
        this.hash = hash;
        fingers = new Node[nFingers];
        if (nSuccessors != null)
            this.numSuccessors = nSuccessors;
        else this.numSuccessors = 1;

        successors = new ArrayList<>();
        try {
            String ip;
            if (Chord.USE_PUBLIC_IP) ip = getmyPublicIPAddress();
            else ip = getmyPrivateIPAddress();
            myNode = new Node(ip, hash.getSHA1(ip));
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
        if (predecessor != null && hash.areOrdered(predecessor.getkey(), objectKey, myNode.getkey())){
            return myNode; //Look if I'm responsible for the key
        }

        Node previous = myNode;
        Node next = null;
        //Look into finger Table
        previous = myNode;
        for (int i = 0; i < fingers.length; i++) {
            next= fingers[i];
            if (next != null && hash.areOrdered(next.getkey(), objectKey, previous.getkey())){
                return next; //Look if I'm responsible for the key
            }
            if (next != null) previous = next;
        }
        return next; //Max finger
    }

    public Node getPredecessor(){ return predecessor; }

    public void setPredecessor(Node predecessor) {this.predecessor = predecessor; }

    /**
     * Add successor to successor list and make consistent the latter
     * @param successor
     */
    public void setSuccessor(Node successor) {
        this.successors.add(successor);
        successors.sort((o1, o2) -> hash.compare(o1.getkey(), o2.getkey())); //TODO come effettuare il confronto?
        for (int i = 1; i < successors.size(); i++) {
            if (successors.get(i-1).equals(successors.get(i))){
                successors.remove(i-1);
            }
        }
        successors = successors.subList(0, numSuccessors);
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

    public int getNumSuccessors(){ return this.numSuccessors; }

    public void removeFailedNode(Node node){
        successors.remove(node);
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
