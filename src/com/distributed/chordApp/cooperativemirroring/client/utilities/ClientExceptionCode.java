package com.distributed.chordApp.cooperativemirroring.client.utilities;

/**
 * Enum used for listing all the possible exceptions rised by a client
 */
public enum ClientExceptionCode  {
    INVALID_CLIENT_IP("INVALID CLIENT IP"),
    INVALID_CLIENT_PORT("INVALID CLIENT PORT"),
    INVALID_SERVER_IP("INVALID SERVER IP"),
    INVALID_SERVER_PORT("INVALID SERVER PORT");

    private String code = null;

    ClientExceptionCode(String code){
        this.code = code;
    }

    public String getCode(){return this.code;}
}
