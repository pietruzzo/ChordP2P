package com.distributed.chordLib.chordCore.communication;

import com.distributed.chordLib.chordCore.Node;
import com.distributed.chordLib.chordCore.communication.messages.Message;

import java.io.*;
import java.net.Socket;

public class SocketNode {

    private Socket endpoint;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String nodeIP;
    private Thread socketThread;
    private volatile boolean killthread = false;
    private SocketIncomingHandling socketCommCallback;
    private boolean incoming;

    public SocketNode(String IP,Socket endpoint, SocketIncomingHandling callback, boolean incoming) {
        this.endpoint = endpoint;
        this.nodeIP = IP;
        this.socketCommCallback = callback;
        this.incoming = incoming;

        try {
            in = new ObjectInputStream(endpoint.getInputStream());
            out = new ObjectOutputStream(endpoint.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.socketThread = new Thread(new ReadSocketRunnable());
        this.socketThread.start();
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
            this.killthread = true;
            endpoint.close();
            socketThread.interrupt();
        } catch (IOException e) {
            System.err.println("Unable to close socket " + nodeIP +"...");
            e.printStackTrace();
        }

    }

    private Object readSocket() throws IOException {
        try {
            return in.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private SocketNode getThis() {return this;}

    public boolean isIncoming() {
        return incoming;
    }

    public void setIncoming(boolean incoming) {
        this.incoming = incoming;
    }

    private class ReadSocketRunnable implements Runnable {

        @Override
        public void run() {
            while(!killthread) {
                Object message = null;
                try {
                    message = readSocket();
                    socketCommCallback.handleNewMessage((Message)message, getThis() );
                } catch (IOException e) {
                    socketCommCallback.handleUnexpectedClosure(nodeIP);
                    System.out.println("Error in reading from socket, probably closed");
                    System.out.println("Closing socket " + nodeIP);
                }

            }
        }
    }
}
