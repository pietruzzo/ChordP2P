package com.distributed.chordApp.cooperativemirroring.server.core.settings.exceptions;

/**
 * Enum that contains all the exception message codes that ca ChordNetworkSettings class can rise
 */
public enum ChordNetworkSettingsExceptionCode
{
    //Exception code associated to an invalid bootstrap server IP passed as parameter at creation
    INVALID_CHORD_BOOTSTRAP_SERVER_IP("INVALID CHORD BOOTSTRAP SERVER IP"),
    //Exception code associated to an invalid chord port passed as parameter at creation
    INVALID_CHORD_PORT("INVALID CHORD PORT"),
    //Exception code associated to an invalid lookup method passed as parameter at creation
    INVALID_CHORD_LOOKUP_MODE("INVALID CHORD LOOKUP MODE"),
    //Exception code associated to an invalid number of fingers passed as parameter at creation
    INVALID_CHORD_NUMBER_OF_FINGERS("CHORD INVALID NUMBER OF FINGERS"),
    //Exception code associated to an invalid number of successors passed as parameter at creation
    INVALID_CHORD_NUMBER_OF_SUCCESSORS("CHORD INVALID NUMBER OF SUCCESSORS"),
    //Exception code associated to an invalid create/join chord network parameter at creation
    INVALID_CHORD_NETWORK_JOINING_MODE("INVALID CHORD NETWORK JOINING MODE"),
    //Exception code associated to an invalid number used as module parameter at creation
    INVALID_CHORD_MODULE("INVALID CHORD MODULE");

    private String code = null;

    ChordNetworkSettingsExceptionCode(String code)
    {
        this.setCode(code);
    }

    //Setter
    private void setCode(String code){this.code = code;}

    //Getter
    public String getCode(){return this.code;}
}
