package com.distributed.chordLib.exceptions;

/**
 * Timeout is reached
 */
public class TimeoutReachedException extends RuntimeException {

    static final String message = "Waiting too much for responce on message ";

    public TimeoutReachedException(){
        super();
        System.err.println(message);
    }

}
