package com.distributed.chordApp.cooperativemirroring.core.settings.exceptions.codes;

/**
 * Enum of possibles exception codes associated to the HostSettings setups
 */
public enum HostSettingsExceptionCode
{
    //Code associated to an invalid Host IP passed to the HostSettingsBuilder
    INVALID_HOST_IP("INVALID HOST IP"),
    //Code associated to an invalid host port passed to the HostSettingsBuilder
    INVALID_HOST_PORT("INVALID HOST PORT"),
    //Code associated to an invalid verbose mode parameter passed to the HostSettingsBuilder
    INVALID_VERBOSE_OPERATING_MODE("INVALID VERBOSE OPERATING MODE"),
    //Code associated to an invalid ChordNetworkSettings parameter passed to the HostSettingsBuilder
    INVALID_CHORD_NETWORK_SETTINGS("INVALID CHORD NETWORK SETTINGS"),
    //Code associated to an invalid ShallopHostIP parameter passed to the HostSettingsBuilder
    INVALID_SHALLOP_HOST_IP("INVALID SHALLOP HOST IP"),
    //Code associated to an invalid ShallopHostPort parameter passed to the HostSettingsBuilder
    INVALID_SHALLOP_HOST_PORT("INVALID SHALLOP HOST PORT"),
    //Code associated to an invalid ConnectionRetries parameter passed to the HostSettingsBuilder
    INVALID_CONNECTION_RETRIES("INVALID CONNECTION RETRIES"),
    //Code associated to an invalid ConnectionTimeout_MS parameter passed to the HostSettingsBuilder
    INVALID_CONNECTION_TIMEOUT_MS("INVALID CONNECTION TIMEOUT MS");

    private String code ;

    HostSettingsExceptionCode(String code)
    {
        this.setCode(code);
    }

    //Setter
    private void setCode(String code){ this.code = code; }

    //Getter
    public String getCode(){return this.code;}
}
