package com.distributed.chordApp.cooperativemirroring.common.utilities.exceptions.codes;

/**
 * List of all the exception codes that can be rised by the SocketManager
 */
public enum SocketManagerExceptionCode
{
    //Exception called when a socket on the host reach a timeout
    CONNECTION_TIMEOUT_REACHED("CONNECTION TIMEOUT REACHED"),
    //Exception called when a socket on the host reach the maximum number of retries
    CONNECTION_MAXIMUM_RETRIES_REACHED("CONNECTION MAXIMUM RETRIES REACHED"),
    //Exception code associated to a bad connection parameters
    CONNECTION_BAD_PARAMETERS("BAD CONNECTION PARAMETERS"),
    CONNECTION_INVALID_TIMEOUT("CONNECTION INVALID TIMEOUT"),
    CONNECTION_ALREADY_ESTABLISHED("CONNECTION ALREADY ESTABLISHED"),
    CONNECTION_NOT_ESTABLISHED("CONNECTION NOT ESTABLISHED"),
    UNABLE_TO_OPEN_OUTPUT_STREAM("UNABLE TO OPEN OUTPUT STREAM"),
    OUTPUT_STREAM_NOT_OPENED_YET("OUTPUT STREAM NOT OPENED YET"),
    INPUT_STREAM_NOT_OPENED_YET("INPUT STREAM NOT OPENED YET"),
    UNABLE_TO_WRITE_MESSAGE_ON_OUTPUT_STREAM("UNABLE TO WRITE MESSAGE ON OUTPUT STREAM"),
    UNABLE_TO_READ_OBJECT_FROM_INPUT_STREAM("UNABLE TO READ OBJECT FROM INPUT STREAM"),
    UNABLE_TO_FLUSH_OUTPUT_STREAM("UNABLE TO FLUSH OUTPUT STREAM"),
    UNABLE_TO_CLOSE_OUTPUT_STREAM("UNABLE TO CLOSE OUTPUT STREAM"),
    UNABLE_TO_CLOSE_INPUT_STREAM("UNABLE TO CLOSE INPUT STREAM"),
    UNABLE_TO_OPEN_INPUT_STREAM("UNABLE TO OPEN INPUT STREAM"),
    UNABLE_TO_CLOSE_CONNECTION("UNABLE TO CLOSE CONNECTION"),
    HOST_SHUTTED_DOWN("HOST SHUTTED DOWN");

    private String code = null;

    SocketManagerExceptionCode(String code)
    {
        this.setCode(code);
    }

    //Setter
    private void setCode(String code){this.code = code;}

    //Getter
    public String getCode(){return this.code = code;}
}
