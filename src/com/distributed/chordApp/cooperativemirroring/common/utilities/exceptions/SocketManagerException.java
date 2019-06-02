package com.distributed.chordApp.cooperativemirroring.common.utilities.exceptions;

/**
 * Class used for managing exceptions rised from SocketManager
 */
public class SocketManagerException extends Exception{
    private String message = null;

    public SocketManagerException(String message)
    {
        super(message);

        this.setMessage(message);
    }

    //Setter
    private void setMessage(String message){this.message = message;}

    //Getter
    public String getMessage(){return this.message;}
}
