package com.distributed.chordLib.chordCore;

import com.distributed.chordLib.Chord;
import com.distributed.chordLib.exceptions.NoSuccessorsExceptions;
import com.distributed.chordLib.exceptions.UnableToGetMyIPException;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;


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


    public Node getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(Node predecessor) {
        this.predecessor = predecessor;
    }

    /**
     * Add successor to successor list and make consistent the latter
     *
     * @param successor
     */
    public void setSuccessor(Node successor) {

        System.out.println("Set Successor "+ successor.getIP());

        if (this.successors.contains(myNode)) this.successors.remove(myNode);

        if (!this.successors.contains(successor))
            this.successors.add(successor);

        successors.sort((o1, o2) -> {
            if (hash.areOrdered(myNode.getkey(), o1.getkey(), o2.getkey())) return 1;
            else if (o1.equals(this.getMyNode())) return 1; //Put MyNode always on tail
            else if (o1.equals(o2)) return 0;
            else return -1;
        });
        for (int i = 1; i < successors.size(); i++) {
            if (successors.get(i - 1).equals(successors.get(i))) {
                successors.remove(i - 1);
            }
        }
        successors = successors.subList(0, Math.min(numSuccessors, successors.size()));

        printFingerTable();
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

    public void setFinger(Node node, int position) {
        System.out.println("Set finger table");
        printFingerTable();
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
    public void removeFailedNode(Node node) {
        for (Node s: successors) {
            if (s.equals(node)) successors.remove(s);
        }
        for (int i = 0; i < fingers.length; i++) {
            if (fingers[i] != null && fingers[i].equals(node)) fingers[i] = null;
        }
        if (this.predecessor.equals(node)){
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
