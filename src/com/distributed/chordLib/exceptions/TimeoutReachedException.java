package com.distributed.chordLib.exceptions;


/**
 * Timeout is reached
 */
public class TimeoutReachedException extends RuntimeException {

    static final String message = "Waiting too much for responce on message ";
    private String waitingNode;

    public TimeoutReachedException(){
        super();
        System.err.println(message);
    }
    public TimeoutReachedException(String nodeIP){
        super();
        waitingNode = nodeIP;
        System.err.println(message + "node "+ nodeIP);
    }

    public String getWaitingNode() {
        return waitingNode;
    }
}
