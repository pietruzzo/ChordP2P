package com.distributed.chordLib.chordCore;

import com.distributed.chordLib.Chord;
import com.distributed.chordLib.exceptions.NoSuccessorsExceptions;
import com.distributed.chordLib.exceptions.UnableToGetMyIPException;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;

/**
 * Datastructures to store and handling all informations about Nodes
 */
public class FingerTable {

    private Node[] fingers;
    private List<Node> successors;
    private Node predecessor;
    private Node myNode;
    private HashFunction hash;
    private int numSuccessors;

    FingerTable(int nFingers, @Nullable Integer nSuccessors, HashFunction hash) {
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
        successors.add(myNode);
    }

    /**
     * @return get first successor
     */
    public Node getSuccessor() {
        if (successors.isEmpty()) throw new NoSuccessorsExceptions();
        return successors.get(0);
    }

    /**
     * Get a clone of successor list
     * @return
     */
    public List<Node> getAllSuccessors(){
        return this.successors.subList(0, this.successors.size());
    }


    public Node getPredecessor() {
        return predecessor;
    }

    public synchronized void setPredecessor(Node predecessor) {
        this.predecessor = predecessor;
    }

    /**
     * Add successor to successor list and make consistent the latter
     *
     * @param successor
     */
    public synchronized void setSuccessor(Node successor) {

        System.out.println("Set Successor "+ successor.getIP());

        if (!this.successors.contains(successor))
            this.successors.add(successor);

        successors.sort(new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                if (o1.equals(getMyNode())) return 1; //Put MyNode always on tail
                else if (o2.equals(getMyNode())) return -1; //Put MyNode always on tail
                else if (hash.areOrdered(myNode.getkey(), o1.getkey(), o2.getkey())) return -1;
                else if (o1.equals(o2)) return 0;
                else return 1;
            }
        });
        for (int i = 1; i < successors.size(); i++) {
            if (successors.get(i - 1).equals(successors.get(i))) {
                successors.remove(i - 1);
            }
        }
        successors = successors.subList(0, Math.min(numSuccessors, successors.size()));

    }


    /**
     * Substitute a node into fingertable with another one
     */
    public synchronized void substitute (Node previous, Node next) {

        //For successors
        for (int i = 0; i < this.successors.size(); i++) {
            if (successors.get(i) != null && previous.equals(successors.get(i))){
                successors.set(i, next);
            }
        }

        //For fingers
        for (int i = 0; i < this.fingers.length; i++) {
            if (fingers[i]!= null && previous.equals(fingers[i])){
                fingers[i] = next;
            }
        }

        if (previous.equals(predecessor)){
            this.predecessor = next;
        }
    }

    public Node getMyNode() {
        return myNode;
    }

    /**
     * get number of fingers
     */
    public int getNumFingers() {
        return fingers.length;
    }

    public Node getFinger(int position) {
        return fingers[position];
    }

    public synchronized void setFinger(Node node, int position) {
        System.out.println("Set finger table");
        fingers[position] = node;
    }

    public int getNumSuccessors() {
        return this.numSuccessors;
    }

    public boolean successoIsFull(){
        if (successors.size()< this.getNumSuccessors())
            return false;
        return true;
    }

    /**
     * Remove failed node from finger Table and Successor List
     * @param node
     */
    public synchronized void removeFailedNode(Node node) {

        //Remove from successor list
        List<Node> tobeRemoved = new ArrayList<>();
        for (Node s: successors) {
            if (s.equals(node)) tobeRemoved.add(s);
        }
        for (Node s: tobeRemoved
             ) {
            successors.remove(s);
        }

        //Remove from fingers
        for (int i = 0; i < fingers.length; i++) {
            if (fingers[i] != null && fingers[i].equals(node)) fingers[i] = null;
        }

        //Remove failed predecessor
        if (this.predecessor != null && this.predecessor.equals(node)){
            this.predecessor = null;
        }
        System.out.println("failed node " + node.getIP() + " is removed, only " + successors.size() + " successors in list");
    }

    /**
     * Debug informtions for Chord Datastructures
     */
    public void printFingerTable(){
        String successorsString ="";
        String fingersString = "";
        String predecessorString = "NOPREDECESSOR";

        for (int i = 0; i < successors.size(); i++) {
            successorsString = successorsString + successors.get(i).getIP() +", ";
        }

        for (int i = 0; i < fingers.length; i++) {
            if (fingers[i]==null) continue;
            fingersString = fingersString + fingers[i].getIP() + ", ";
        }

        if(predecessor != null)  predecessorString = predecessor.getIP();
        System.out.println("___FINGERTABLE____");
        System.out.println("Successors: " + successorsString);
        System.out.println("Fingers: " + fingersString);
        System.out.println("Predecessor: " + predecessorString);
        System.out.println("__________________");
    }

    /**
     * Method invoked to get my public IP using amazonaws
     * @return my public ip
     */
    private String getmyPublicIPAddress() throws IOException {
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                whatismyip.openStream()));

        return in.readLine();
    }

    /**
     * get first occurrence of local IP-4 on network interfaces
     */
    private String getmyPrivateIPAddress() throws IOException {

        String ip;
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            // filters out 127.0.0.1 and inactive interfaces
            if (iface.isLoopback() || !iface.isUp())
                continue;

            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();

                // *EDIT*
                if (addr instanceof Inet6Address) continue;

                ip = addr.getHostAddress();
                System.out.println(iface.getDisplayName() + " " + ip);
                return ip;
            }

        }
        throw new IOException("Local address not found");
    }
}
