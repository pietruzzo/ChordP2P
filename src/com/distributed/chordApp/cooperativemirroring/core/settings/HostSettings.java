package com.distributed.chordApp.cooperativemirroring.core.settings;

import java.io.Serializable;

/**
 * Class used for storing all the useful settings for a specific host
 *
 * @date 2019-03-27
 * @version 1.0
 */
public class HostSettings implements Serializable
{
    //IP address associated to a specific host
    private String hostIP = null;
    //Address associated to a specific host port for the cooperative mirroring service
    private Integer hostPort = null;
    //Boolean flag used to state if the host as to operate in a verbose mode
    private Boolean verboseOperatingMode = null;
    //Object used for setting up the connection to a chord network by a specific host
    private ChordNetworkSettings chordNetworkSettings = null;

    public HostSettings(String hostIP,ChordNetworkSettings chordNetworkSettings,Boolean verboseOperatingMode)
    {
        this.setHostIP(hostIP);
        this.setHostPort(chordNetworkSettings.getAssociatedHostPort());
        this.setChordNetworkSettings(chordNetworkSettings);
        this.setVerboseOperatingMode(verboseOperatingMode);
    }

    /*Setter methods*/
    private void setHostIP(String hostIP){this.hostIP = hostIP; }
    private void setHostPort(Integer hostPort){this.hostPort = hostPort;}
    private void setVerboseOperatingMode(Boolean verboseOperatingMode){this.verboseOperatingMode = verboseOperatingMode; }
    private void setChordNetworkSettings(ChordNetworkSettings chordNetworkSettings){this.chordNetworkSettings = chordNetworkSettings; }

    /*Application methods*/
    /**
     * Method used for creating info messages for the verbose mode
     * @param info, clientHandler
     * @return
     */
    public String verboseInfoString(String info,Boolean clientHandler)
    {
        String vString = "[Host\\\\" + this.getHostIP() + ":" + this.getHostPort() ;

        if(clientHandler) vString += " :: ClientHandler> ";
        else vString += "> ";

        vString += info;

        return vString;
    }

    /*Getter methods*/
    public String getHostIP(){return  this.hostIP; }
    public Integer getHostPort(){return this.hostPort; }
    public Boolean getVerboseOperatingMode(){return  this.verboseOperatingMode; }
    public ChordNetworkSettings getChordNetworkSettings(){return this.chordNetworkSettings; }

    @Override
    public String toString()
    {
        String state = "\n======={HOST SETTINGS}======\n";

        state += "\nHost IP: " + this.getHostIP();
        state += "\nHost port: " + this.getHostPort();
        if(this.getVerboseOperatingMode()) state += "\nVerbose operating mode";
        else state += "\nSilent operating mode";
        //state += "\nChord network settings: " + this.getChordNetworkSettings().toString();

        return  state;
    }
}
