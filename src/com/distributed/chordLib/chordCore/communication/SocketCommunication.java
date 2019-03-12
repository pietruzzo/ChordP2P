package com.distributed.chordLib.chordCore.communication;

import com.distributed.chordLib.chordCore.Node;
import com.distributed.chordLib.chordCore.communication.messages.JoinResponseMessage;
import com.distributed.chordLib.chordCore.communication.messages.ReqResp;

import java.util.List;
import java.util.Map;

public class SocketCommunication implements CommCallInterface, CommCallbackInterface {

    public static final int REQUEST_TIMEOUT = 3000;

    //List of SocketNode
    Map<Node, SocketNode> socketNodes;
    //List of threads waiting for a response
    List<ComputationState> waitingThreads;

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
    ReqResp request;
    ReqResp response;

    ComputationState(Thread thread, ReqResp message) {
        this.request = message;
        this.thread = thread;
        response = null;
    }

    //Suspend current thread
    public void sleep(/*TODO: wait until response available or timeout elapse*/){
        if (response == null) this.sleep();
    }

}