package com.distributed.chordLib.chordCore.communication;

import com.distributed.chordLib.chordCore.ChordClient;
import com.distributed.chordLib.chordCore.Node;
import com.distributed.chordLib.chordCore.communication.messages.*;
import com.distributed.chordLib.exceptions.CommunicationFailureException;
import com.distributed.chordLib.exceptions.TimeoutReachedException;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import static com.distributed.chordLib.chordCore.HashFunction.*;

public class SocketCommunication implements CommCallInterface, SocketIncomingHandling {

    public static final int REQUEST_TIMEOUT = 30000;
    private static final int CORE_POOL_SIZE = 2;
    private static final int CORE_MAX_POOL_SIZE = 20;
    private final int socketPort;

    //List of client established connections
    Map<String, SocketNode> socketNodes;
    //List of threads waiting for a response <RequestID, ComputationState>
    Map<Integer, ComputationState> waitingThreads;
    //ChordClient calls from network
    CommCallbackInterface callback;
    //Pool of workers for messageHandling
    ThreadPoolExecutor workers;
    //ServerSocket for incoming connections
    ServerSocket serverSocket;

    volatile boolean closeServerSocket = false;

    /**
     * @param port for socket communication
     * @param callback from communication to chord
     */
    public SocketCommunication(int port, CommCallbackInterface callback){
        this.socketPort = port;
        this.callback = callback;
        this.socketNodes = new HashMap<>();
        this.waitingThreads = new HashMap<>();
        this.workers = new ThreadPoolExecutor(CORE_POOL_SIZE, CORE_MAX_POOL_SIZE, REQUEST_TIMEOUT, TimeUnit.MICROSECONDS, new ArrayBlockingQueue<>(30));
        handleNewIncomingConnections();

    }

    @Override
    public JoinResponseMessage join(Node node, int port) {
        Message message = new JoinRequestMessage();
        waitResponse(message, getSocketNode(node.getIP()));
        return (JoinResponseMessage) getResponseinWaiting(message.getId());
    }

    @Override
    public Node findSuccessorB(Node node, Hash key) {
        Message message = new BasicLookupRequestMessage(key);
        waitResponse(message, getSocketNode(node.getIP()));
        return ((LookupResponseMessage) getResponseinWaiting(message.getId())).node;
    }

    @Override
    public Node findSuccessor(Node node, Hash key) {
        Message message = new LookupRequestMessage(key);
        waitResponse(message, getSocketNode(node.getIP()));
        return ((LookupResponseMessage) getResponseinWaiting(message.getId())).node;
    }

    @Override
    public Node findPredecessor(Node node) {
        Message message = new PredecessorRequestMessage();
        waitResponse(message, getSocketNode(node.getIP()));
        return ((PredecessorResponseMessage) getResponseinWaiting(message.getId())).node;
    }

    @Override
    public void notifySuccessor(Node successor, Node me) {
        SocketNode receiver = getSocketNode(successor.getIP());
        NotifySuccessorMessage message = new NotifySuccessorMessage(me);
        receiver.writeSocket(message);
    }

    @Override
    public boolean isAlive(Node node) {
        Message message = new PingRequestMessage();
        try {
            waitResponse(message, getSocketNode(node.getIP()));
            return true;
        } catch (CommunicationFailureException | TimeoutReachedException e){
            System.out.println("No response to a ping message for " + node.getIP() + " for message" + message.toString());
        }
        return false;
    }

    @Override
    public void closeChannel(Node[] nodes) {

        //If nodes is empty, close all
        if (nodes == null || nodes.length == 0) {
            try {
                serverSocket.close(); //it will throw SocketException on accept that will stop the thread
            } catch (IOException e) {
                System.out.println("Closing socketserver");
            }
            for (SocketNode sn: socketNodes.values()) {
                sn.close();
            }
            socketNodes = new HashMap<>();
        }
        else {
            //Filter out all useless outgoing connections
            String[] engineAlive = (String[]) Arrays.stream(nodes).map(node -> node.getIP()).toArray();
            for (String node : socketNodes.keySet()) {
                boolean kill = false;
                if (!socketNodes.get(node).isIncoming()) {
                    kill = true;
                    for (int i = 0; i < engineAlive.length; i++) {
                        if (node == engineAlive[i]) {
                            kill = false;
                            break;
                        }
                    }
                }
                if (kill) {
                    SocketNode element = socketNodes.get(node);
                    element.close();
                    socketNodes.remove(node);
                }
            }

            //Close all unused incoming channels
            for (SocketNode sn : socketNodes.values()) {
                if (sn.isIncoming() && !isAlive(new Node(sn.getNodeIP(), null))) {
                    socketNodes.remove(sn.getNodeIP());
                    sn.close();
                }
            }
        }
    }

    @Override
    public void closeCommLayer(Node predecessor, Node me, Node successor) {

        //Build message
        VoluntaryDepartureMessage message = new VoluntaryDepartureMessage(predecessor, me, successor);

        //Get predecessor and successo sockets
        SocketNode predecessorSocket = getSocketNode(predecessor.getIP());
        SocketNode successorSocket = getSocketNode(predecessor.getIP());

        //Send message on sockets
        predecessorSocket.writeSocket(message);
        successorSocket.writeSocket(message);

        //Close communication Layer
        this.closeCommLayer();
    }

    /**
     * Close all remaining connections and communication threads
     */
    @Override
    public void closeCommLayer(){
        closeChannel(null);
        closeServerSocket = true;
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("forcing serversocket closing");
        }

    }

    @Override
    public void handleNewMessage(Message message, SocketNode node) {
        incomingMessageDispatching(node, message);
    }

    @Override
    public void handleUnexpectedClosure(String node) {
        //Close SocketNode and remove any references
        socketNodes.get(node).close();
        socketNodes.remove(node);
    }


    /**
     * Get socketNode corresponding to node or
     * create a new connection to it
     * @param nodeIP
     * @return corresponding SocketNode or null if connection is refused
     */
    private SocketNode getSocketNode(String nodeIP){
        if (socketNodes.containsKey(nodeIP)) return socketNodes.get(nodeIP);
        else{
            try {
                SocketNode newSocketNode =  new SocketNode(nodeIP, new Socket(nodeIP, socketPort), this, false);
                socketNodes.put(nodeIP, newSocketNode);
                return newSocketNode;
            } catch (IOException e) {
                System.err.println("Unable to open connection to " + nodeIP +": " + socketPort);
                throw new CommunicationFailureException();
            }
        }
    }

    /**
     * Handle incoming Connections
     */
    private void handleNewIncomingConnections(){
        SocketIncomingHandling callback = this;
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(socketPort);
            } catch (IOException e) {
                e.printStackTrace();
                closeServerSocket = true;
            }

            while (!closeServerSocket) {
                try {
                    Socket newConnection = serverSocket.accept();
                    String ip = newConnection.getInetAddress().getHostAddress();
                    SocketNode newSocketNode = new SocketNode(ip, newConnection, callback, true);
                    socketNodes.put(ip, newSocketNode);
                } catch (IOException e) {
                    System.err.println("Unable to accept new incoming connection");
                    e.printStackTrace();
                    closeServerSocket = true;
                }
            }
        }).start();
    }

    /**
     * Send Request, suspend and queue thread, waiting for response
     * @param requestMessage message that will be sent to receiver
     * @param receiver receiver for the message
     */
    private void waitResponse(Message requestMessage, SocketNode receiver){
        if (receiver == null) throw new NullPointerException("Receiver SocketNode is NULL");
        ComputationState current = new ComputationState(Thread.currentThread());
        synchronized (this){
            //Send message on socket
            receiver.writeSocket(requestMessage);
            //save this thread in waitingThreads
            this.waitingThreads.put(requestMessage.getId(), current);
        }
        //suspend current thread
        try {
            current.waitResponse();
        } catch (TimeoutReachedException e){
            //Add informations to error
            System.err.println(requestMessage.getId() + " request failed");
            throw new TimeoutReachedException(receiver.getNodeIP());
        }
    }

    //region: SocketMessageReceivingHandling


    private void handleJoinMessage(JoinRequestMessage reqMessage, SocketNode questioner){

        ChordClient.InitParameters initPar = callback.handleJoinRequest(questioner.getNodeIP());
        JoinResponseMessage resMess = new JoinResponseMessage(initPar, reqMessage.getId());
        questioner.writeSocket(resMess);
    }

    private void handleLookupBMessage(BasicLookupRequestMessage reqMessage, SocketNode questioner){
        Node node = callback.handleLookupB(reqMessage.key);
        LookupResponseMessage resMess = new LookupResponseMessage(node, reqMessage.getId());
        questioner.writeSocket(resMess);
    }

    private void handleLookupMessage(LookupRequestMessage reqMessage, SocketNode questioner){
        Node node = callback.handleLookup(reqMessage.key);
        LookupResponseMessage resMess = new LookupResponseMessage(node, reqMessage.getId());
        questioner.writeSocket(resMess);
    }

    private void handleNotifyMessage(NotifySuccessorMessage mess, SocketNode questioner){
        callback.notifyIncoming(new Node(questioner.getNodeIP(), callback.getkey(questioner.getNodeIP())));
    }

    private void handlePingMessage(PingRequestMessage reqMessage, SocketNode node){
        PingResponseMessage resMessage = new PingResponseMessage(reqMessage.getId());
        node.writeSocket(resMessage);
    }

    private void handlePredecessorMessage(PredecessorRequestMessage reqMessage, SocketNode questioner){
        Node predecessor = callback.handlePredecessorRequest();
        PredecessorResponseMessage resMessage = new PredecessorResponseMessage(reqMessage.getId(), predecessor);
        questioner.writeSocket(resMessage);
    }

    private void handleUnrecognizedMessage(Object unrecognized, SocketNode node){
        System.err.println("Unrecognized message of type " + unrecognized.getClass().toString());
    }




    private void incomingMessageDispatching(SocketNode questioner, Message message){
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
    private synchronized void awake (Message responseMessage, @Nullable Integer reqID){
        if (reqID == null) reqID = responseMessage.getId();
        waitingThreads.get(reqID).registerResponse(responseMessage);
    }

    /**
     * get response and deallocate saved computation state
     * @ApiNote it can be invoked only one time on an object
     */
    private ResponseMessage getResponseinWaiting(int id){
        Message message = waitingThreads.get(id).getResponse();
        synchronized (this) {
            waitingThreads.remove(id);
        }
        return (ResponseMessage) message;
    }

}

/**
 * Class to store thread with request
 */
class ComputationState {

    Thread thread;
    Message response;

    private Instant end;

    ComputationState(Thread thread) {
        this.thread = thread;
        response = null;
        end = null;
    }

    /**
     * Suspend current thread until a response is give or timeout expiring
     * @return response, or null is timeout is expired
     */
    public Message waitResponse() {

        end = Instant.now();
        end = end.plusMillis(SocketCommunication.REQUEST_TIMEOUT);
        while (response == null ) {
            if (isTimeElapsed()) throw new TimeoutReachedException();
            try {
                synchronized (thread) {
                    thread.wait(SocketCommunication.REQUEST_TIMEOUT+1);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new CommunicationFailureException();
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
        synchronized (thread) {
            thread.notify();
        }
    }

    public Message getResponse () {
        return response;
    }

    private boolean isTimeElapsed(){
        Instant now = Instant.now();
        return now.isAfter(end);
    }
}