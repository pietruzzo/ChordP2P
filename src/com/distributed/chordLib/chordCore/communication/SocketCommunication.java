package com.distributed.chordLib.chordCore.communication;

import com.distributed.chordLib.chordCore.Node;
import com.distributed.chordLib.chordCore.communication.messages.JoinResponseMessage;

public class SocketCommunication implements CommCallInterface, CommCallbackInterface {


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
     * Queue
     */
    <T> T waitResponse(T responseMessage){
        return null;
    }
}
