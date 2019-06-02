package com.distributed.chordApp.cooperativemirroring.client.utilities;

/**
 * Class used for managing exceptions coming from the client
 */
public class ClientException extends Exception {

    //Message associated to the current exception
    private String message = null;

    public ClientException(String message){
        super(message);

        this.message = message;
    }

    public String getMessage(){return this.message;}
}
