package com.distributed.chordLib.chordCore.communication;

import com.distributed.chordLib.chordCore.Node;
import com.distributed.chordLib.chordCore.communication.messages.JoinResponseMessage;
import com.distributed.chordLib.chordCore.communication.messages.ReqResp;

import java.io.IOException;
import java.net.Socket;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SocketCommunication implements CommCallInterface, CommCallbackInterface {

    public static final int REQUEST_TIMEOUT = 3000;
    private final int socketPort;

    //List of SocketNode
    Map<Node, SocketNode> socketNodes;
    //List of threads waiting for a response <RequestID, ComputationState>
    Map<Integer, ComputationState> waitingThreads;

    /**
     * @param port for socket communication
     */
    SocketCommunication(int port){
        this.socketPort = port;
    }

    @Override
    public JoinResponseMessage join(String ip, String port) {
        return null;
    }

    @Override
    public Node findSuccessorB(String key) {
        return null;
    }

    @Override
    public Node findSuccessor(String key) {
        return null;
    }

    @Override
    public void notifySuccessor() {

    }

    @Override
    public boolean isAlive(Node node) {
        return false;
    }

    @Override
    public void closeChannel(Node node) throws ArrayStoreException {

    }


    @Override
    public void handleJoinRequest(String IP) {

    }

    @Override
    public void handleLookupB(String key) {

    }

    @Override
    public void handleLookup(String key) {

    }

    @Override
    public void notify(Node predecessor) {

    }

    @Override
    public void ping() {

    }

    /**
     * Get socketNode corresponding to node or
     * create a new connection to it
     * @param node
     * @return corresponding SocketNode or null if connection is refused
     */
    private SocketNode getSocketNode(Node node){
        if (socketNodes.containsKey(node)) return socketNodes.get(node);
        else{
            try {
                SocketNode newSocketNode =  new SocketNode(node, new Socket(node.getIP(), socketPort));
                socketNodes.put(node, newSocketNode);
                return newSocketNode;
            } catch (IOException e) {
                System.err.println("Unable to open connection to " + node.getIP() +": " + socketPort);
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * Send Request, suspend and queue thread, waiting for response
     * @param requestMessage message that will be sent to receiver
     * @param receiver receiver for the message
     * @return Response message
     */
    private void waitResponse(ReqResp requestMessage, SocketNode receiver){
        ComputationState current = new ComputationState(Thread.currentThread());
        //Send message on socket
        receiver.writeSocket(requestMessage);
        //save this thread in waitingThreads
        this.waitingThreads.put(requestMessage.getId(), current);
        //suspend current thread
        try {
            current.waitResponse();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Register response and awake sleeping thread
     * @param responseMessage
     * @param reqID
     */
    private void awake (ReqResp responseMessage, int reqID){}
}

/**
 * Datastructure that store thread with request
 */
class ComputationState {

    Thread thread;
    ReqResp response;

    private Date begin;

    ComputationState(Thread thread) {
        this.thread = thread;
        response = null;
        begin = null;
    }

    /**
     * Suspend current thread until a response is give or timeout expiring
     * @return response, or null is timeout is expired
     */
    public ReqResp waitResponse(/*TODO: wait until response available or timeout elapse*/) throws InterruptedException {

        begin = Date.from(Instant.now());
        while (!(response instanceof ReqResp) || isTimeElapsed()) thread.wait();
        return response;
    }

    /**
     * Register a response and resume thread
     * @param response from the network
     */
    public void registerResponse(ReqResp response){
        this.response = response;
        thread.notify();
    }

    private boolean isTimeElapsed(){
        return (Date.from(Instant.now()).getTime() - begin.getTime()) > SocketCommunication.REQUEST_TIMEOUT;
    }

}