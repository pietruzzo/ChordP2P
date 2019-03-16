package com.distributed.chordLib.chordCore.communication;

import com.distributed.chordLib.chordCore.Node;

import java.io.*;
import java.net.Socket;

public class SocketNode {

    private Socket endpoint;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String nodeIP;
    private Thread socketThread;
    private SocketIncomingHandling SocketCommCallback;

    public SocketNode(String IP, Socket endpoint, SocketIncomingHandling callback) {
        this.endpoint = endpoint;
        this.nodeIP = IP;
        this.SocketCommCallback = callback;

        try {
            in = new ObjectInputStream(endpoint.getInputStream());
            out = new ObjectOutputStream(endpoint.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.socketThread = new Thread(new ReadSocketRunnable());
    }



    public void writeSocket(Serializable message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.err.println("Unable to write message on socket " + nodeIP);
            e.printStackTrace();
        }

    }

    public String getNodeIP(){
        return nodeIP;
    }

    public void close() {
        try {
            endpoint.close();
        } catch (IOException e) {
            System.err.println("Unable to close socket " + nodeIP +"...");
            e.printStackTrace();
        }

    }

    public Object readSocket()  {
        try {
            return in.readObject();
        } catch (IOException e) {
            System.err.println("Unable to read message from socket " + nodeIP);
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private class ReadSocketRunnable implements Runnable {

        @Override
        public void run() {
            Object message = readSocket();

        }
    }
}
