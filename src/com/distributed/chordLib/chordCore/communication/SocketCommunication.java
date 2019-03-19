package com.distributed.chordLib.chordCore.communication;

import com.distributed.chordLib.chordCore.ChordClient;
import com.distributed.chordLib.chordCore.HashFunction;
import com.distributed.chordLib.chordCore.Node;
import com.distributed.chordLib.chordCore.communication.messages.*;
import com.distributed.chordLib.exceptions.CommunicationFailureException;
import com.distributed.chordLib.exceptions.TimeoutReachedException;
import jdk.internal.jline.internal.Nullable;

import java.io.IOException;
import java.net.Socket;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Stream;

public class SocketCommunication implements CommCallInterface, SocketIncomingHandling {

    public static final int REQUEST_TIMEOUT = 3000;
    private static final int CORE_POOL_SIZE = 2;
    private static final int CORE_MAX_POOL_SIZE = 20;
    private final int socketPort;

    //List of SocketNode
    Map<String, SocketNode> socketNodes;
    //List of threads waiting for a response <RequestID, ComputationState>
    Map<Integer, ComputationState> waitingThreads;
    //ChordClient calls from network
    CommCallbackInterface callback;
    //Pool of workers for messageHandling
    ThreadPoolExecutor workers;

    /**
     * @param port for socket communication
     * @param callback from communication to chord
     */
    public SocketCommunication(int port, CommCallbackInterface callback){
        this.socketPort = port;
        this.callback = callback;
        this.workers = new ThreadPoolExecutor(CORE_POOL_SIZE, CORE_MAX_POOL_SIZE, REQUEST_TIMEOUT, TimeUnit.MICROSECONDS, new ArrayBlockingQueue<>(30));
    }

    @Override
    public JoinResponseMessage join(Node node, int port) {
        Message message = new JoinRequestMessage();
        waitResponse(message, getSocketNode(node));
        return (JoinResponseMessage) getResponseinWaiting(message.getId());
    }

    @Override
    public Node findSuccessorB(Node node, String key) {
        Message message = new BasicLookupRequestMessage(key);
        waitResponse(message, getSocketNode(node));
        return ((LookupResponseMessage) getResponseinWaiting(message.getId())).node;
    }

    @Override
    public Node findSuccessor(Node node, String key) {
        Message message = new LookupRequestMessage(key);
        waitResponse(message, getSocketNode(node));
        return ((LookupResponseMessage) getResponseinWaiting(message.getId())).node;
    }

    @Override
    public Node findPredecessor(Node node) {
        Message message = new PredecessorRequestMessage();
        waitResponse(message, getSocketNode(node));
        return ((PredecessorResponseMessage) getResponseinWaiting(message.getId())).node;
    }

    @Override
    public void notifySuccessor(Node successor, Node me) {
        SocketNode receiver = getSocketNode(successor);
        NotifySuccessorMessage message = new NotifySuccessorMessage(me);
        receiver.writeSocket(message);
    }

    @Override
    public boolean isAlive(Node node) {
        Message message = new PingRequestMessage();
        waitResponse(message, getSocketNode(node));
        if (getResponseinWaiting(message.getId())!= null && getResponseinWaiting(message.getId()) instanceof PingResponseMessage)
            return true;
        return false;
    }

    @Override
    public void closeChannel(Node[] nodes) {
        String[] engineAlive =(String[]) Arrays.stream(nodes).map(node -> node.getIP()).toArray();
        for (String node:socketNodes.keySet()) {
            boolean kill = true;
            for (int i = 0; i < engineAlive.length; i++) {
                if (node == engineAlive[i]) {
                    kill = false;
                    break;
                }
            }
            if (kill) {
                SocketNode element = socketNodes.get(node);
                element.close();
                socketNodes.remove(node);
            }
        }
    }

    @Override
    public void handleNewMessage(Message message, SocketNode node) {
        incomingMessageDispatching(node, message);
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
                SocketNode newSocketNode =  new SocketNode(node.getIP(), new Socket(node.getIP(), socketPort), this);
                socketNodes.put(node.getIP(), newSocketNode);
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
        if (receiver == null) throw new NullPointerException("Receiver SocketNode is NULL");
        ComputationState current = new ComputationState(Thread.currentThread());
        //Send message on socket
        receiver.writeSocket(requestMessage);
        //save this thread in waitingThreads
        this.waitingThreads.put(requestMessage.getId(), current);
        //suspend current thread
            current.waitResponse();
    }

    //region: SocketMessageReceivingHandling


    void handleJoinMessage (JoinRequestMessage reqMessage, SocketNode questioner){

        ChordClient.InitParameters initPar = callback.handleJoinRequest(questioner.getNodeIP());
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
        callback.notifyIncoming(new Node(questioner.getNodeIP(), callback.getkey(questioner.getNodeIP())));
    }

    void handlePingMessage(PingRequestMessage reqMessage, SocketNode node){
        node.writeSocket(reqMessage);
    }

    void handlePredecessorMessage(PredecessorRequestMessage reqMessage, SocketNode questioner){
        Node predecessor = callback.handlePredecessorRequest();
        PredecessorResponseMessage resMessage = new PredecessorResponseMessage(reqMessage.getId(), predecessor);
        questioner.writeSocket(resMessage);
    }

    void handleUnrecognizedMessage(Object unrecognized, SocketNode node){
        System.err.println("Unrecognized message of type " + unrecognized.getClass().toString());
    }




    public void incomingMessageDispatching(SocketNode questioner, Message message){
        if (message instanceof JoinRequestMessage) handleJoinMessage((JoinRequestMessage) message, questioner);
        else if (message instanceof BasicLookupRequestMessage) handleLookupBMessage((BasicLookupRequestMessage) message, questioner);
        else if (message instanceof LookupRequestMessage) handleLookupMessage((LookupRequestMessage)message, questioner);
        else if (message instanceof NotifySuccessorMessage) handleNotifyMessage((NotifySuccessorMessage) message, questioner);
        else if (message instanceof PingRequestMessage) handlePingMessage((PingRequestMessage) message, questioner);
        else if (message instanceof PredecessorRequestMessage) handlePredecessorMessage((PredecessorRequestMessage) message, questioner);
        else if (message instanceof ResponseMessage) awake(message, null);
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

    private ResponseMessage getResponseinWaiting(int id){
        Message message = waitingThreads.get(id).getResponse();
        waitingThreads.remove(id);
        return (ResponseMessage) message;
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
    public Message waitResponse()  {

        begin = Date.from(Instant.now());
        while (response == null ) {
            if (isTimeElapsed()) throw new TimeoutReachedException();
            try {
                thread.wait();
            } catch (InterruptedException e) {
                throw new CommunicationFailureException(e);
            }
        }
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

    public Message getResponse () {
        return response;
    }

    private boolean isTimeElapsed(){
        return (Date.from(Instant.now()).getTime() - begin.getTime()) > SocketCommunication.REQUEST_TIMEOUT;
    }
}