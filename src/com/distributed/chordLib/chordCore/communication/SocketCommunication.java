package com.distributed.chordLib.chordCore.communication;

import com.distributed.chordLib.chordCore.Node;
import com.distributed.chordLib.chordCore.communication.messages.JoinResponseMessage;
import com.distributed.chordLib.chordCore.communication.messages.ReqResp;

import java.security.Timestamp;
import java.sql.Time;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.TimeoutException;

public class SocketCommunication implements CommCallInterface, CommCallbackInterface {

    public static final int REQUEST_TIMEOUT = 3000;

    //List of SocketNode
    Map<Node, SocketNode> socketNodes;
    //List of threads waiting for a response <RequestID, ComputationState>
    Map<Integer, ComputationState> waitingThreads;

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
     * Send Request, suspend and queue thread, waiting for response
     * @param requestMessage message that will be sent to receiver
     * @param receiver receiver for the message
     * @return Response message
     */
    <T> T waitResponse(T requestMessage, SocketNode receiver){
        //Send message on socket
        //suspend current thread
        //
        return null;
    }
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