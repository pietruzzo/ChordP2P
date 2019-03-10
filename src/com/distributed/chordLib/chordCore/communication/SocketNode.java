package com.distributed.chordLib.chordCore.communication;

import com.distributed.chordLib.chordCore.Node;

import java.io.*;
import java.net.Socket;

public class SocketNode {

    private Socket endpoint;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Node node;


    public SocketNode(String IP, Socket endpoint) {
        Node node = new Node(IP);
        new SocketNode(node, endpoint);
    }

    public SocketNode(Node node, Socket endpoint){
        this.endpoint = endpoint;

        try {
            in = new ObjectInputStream(endpoint.getInputStream());
            out = new ObjectOutputStream(endpoint.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object readSocket()  {
        try {
            return in.readObject();
        } catch (IOException e) {
            System.err.println("Unable to read message from socket " + node.getIP());
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void writeSocket(Serializable message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.err.println("Unable to write message on socket " + node.getIP());
            e.printStackTrace();
        }

    }

    public Node getNode(){
        return node;
    }

    public void close() {
        try {
            endpoint.close();
        } catch (IOException e) {
            System.err.println("Unable to close socket " + node.getIP() +"...");
            e.printStackTrace();
        }

    }
}
