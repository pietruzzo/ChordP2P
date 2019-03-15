package com.distributed.chordLib.chordCore.communication;

import com.distributed.chordLib.chordCore.ChordClient;
import com.distributed.chordLib.chordCore.Node;
import com.distributed.chordLib.chordCore.communication.messages.*;
import jdk.internal.jline.internal.Nullable;

import java.io.IOException;
import java.net.Socket;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

public class SocketCommunication implements CommCallInterface {

    public static final int REQUEST_TIMEOUT = 3000;
    private final int socketPort;

    //List of SocketNode
    Map<Node, SocketNode> socketNodes;
    //List of threads waiting for a response <RequestID, ComputationState>
    Map<Integer, ComputationState> waitingThreads;
    //ChordClient calls from network
    CommCallbackInterface callback;

    /**
     * @param port for socket communication
     * @param callback from communication to chord
     */
    SocketCommunication(int port, CommCallbackInterface callback){
        this.socketPort = port;
        this.callback = callback;
    }

    @Override
    public JoinResponseMessage join(Node node, String port) {
        Message message = new JoinRequestMessage();
        waitResponse(message, getSocketNode(node));
        return (JoinResponseMessage) waitingThreads.get(message.getId()).getResponse();
    }

    @Override
    public Node findSuccessorB(Node node, String key) {
        Message message = new BasicLookupRequestMessage(key);
        waitResponse(message, getSocketNode(node));
        return ((LookupResponseMessage) waitingThreads.get(message.getId()).getResponse()).node;
    }

    @Override
    public Node findSuccessor(Node node, String key) {
        Message message = new LookupRequestMessage(key);
        waitResponse(message, getSocketNode(node));
        return ((LookupResponseMessage) waitingThreads.get(message.getId()).getResponse()).node;
    }

    @Override
    public void notifySuccessor(Node successor, Node me) {
        SocketNode receiver = getSocketNode(successor);
        NotifySuccessorMessage message = new NotifySuccessorMessage(me);
        receiver.writeSocket(message);
    }

    @Override
    public boolean isAlive(Node node) {
        Message message = new PingMessage();
        waitResponse(message, getSocketNode(node));
        if (waitingThreads.get(message.getId()).getResponse() instanceof PingMessage)
            return true;
        return false;
    }

    @Override
    public void closeChannel(Node node) throws ArrayStoreException {
        SocketNode socketNode = socketNodes.get(node);
        if (socketNode == null) System.err.println("SocketNode not found");
        else {
            socketNode.close();
            socketNodes.remove(socketNode);
        }
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
     */
    private void waitResponse(Message requestMessage, SocketNode receiver){
        if (receiver == null) throw new NullPointerException("Receiver Socket Node is NULL");
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

    //region: SocketMessageReceivingHandling


    void handleJoinMessage (JoinRequestMessage reqMessage, SocketNode questioner){

        ChordClient.InitParameters initPar = callback.handleJoinRequest(questioner.getNode().getIP());
        JoinResponseMessage resMess = new JoinResponseMessage(initPar, reqMessage.getId());
        questioner.writeSocket(resMess);
    }

    void handleLookupBMessage(BasicLookupRequestMessage reqMessage, SocketNode questioner){
        Node node = callback.handleLookupB(reqMessage.key);
        LookupResponseMessage resMess = new LookupResponseMessage(node, reqMessage.getId());
        questioner.writeSocket(resMess);
    }

    void handleLookupMessage (LookupRequestMessage reqMessage, SocketNode questioner){
        Node node = callback.handleLookup(reqMessage.key);
        LookupResponseMessage resMess = new LookupResponseMessage(node, reqMessage.getId());
        questioner.writeSocket(resMess);
    }

    void handleNotifyMessage(NotifySuccessorMessage mess, SocketNode questioner){
        callback.notifyIncoming(questioner.getNode());
    }

    void handlePingMessage(PingMessage reqMessage, SocketNode node){
        node.writeSocket(reqMessage);
    }

    void handleUnrecognizedMessage(Object unrecognized, SocketNode node){
        System.err.println("Unrecognized message of type " + unrecognized.getClass().toString());
    }


    public void incomingMessageDispatching(SocketNode questioner, Message message){
        if (message instanceof JoinRequestMessage) handleJoinMessage((JoinRequestMessage) message, questioner);
        else if (message instanceof BasicLookupRequestMessage) handleLookupBMessage((BasicLookupRequestMessage) message, questioner);
        else if (message instanceof LookupRequestMessage) handleLookupMessage((LookupRequestMessage)message, questioner);
        else if (message instanceof NotifySuccessorMessage) handleNotifyMessage((NotifySuccessorMessage) message, questioner);
        else if (message instanceof PingMessage) handlePingMessage((PingMessage) message, questioner);
        else handleUnrecognizedMessage(message, questioner);

    }


    //endregion

    /**
     * Register response and awake sleeping thread
     * @param responseMessage
     * @param reqID if null, use responseMessage' id
     */
    private void awake (Message responseMessage, @Nullable Integer reqID){
        if (reqID == null) reqID = responseMessage.getId();
        waitingThreads.get(reqID).registerResponse(responseMessage);

    }
}

/**
 * Class to store thread with request
 */
class ComputationState {

    Thread thread;
    Message response;

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
    public Message waitResponse(/*TODO: wait until response available or timeout elapse*/) throws InterruptedException {

        begin = Date.from(Instant.now());
        while (!(response instanceof Message) || isTimeElapsed()) thread.wait();
        return response;
    }

    /**
     * Register a response and resume thread
     * @param response from the network
     */
    public void registerResponse(Message response){
        this.response = response;
        thread.notify();
    }

    public Message getResponse () { return response; }

    private boolean isTimeElapsed(){
        return (Date.from(Instant.now()).getTime() - begin.getTime()) > SocketCommunication.REQUEST_TIMEOUT;
    }
}