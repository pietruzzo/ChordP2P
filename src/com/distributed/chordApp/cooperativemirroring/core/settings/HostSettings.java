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
    //Variable that holds the value of the timeout for the socket requests
    private Integer connectionTimeout_ms = 3000;
    //IP of the host where to send the resources in case of this host exiting the network
    private String shallopHostIP = null;
    //Port of the shallop host
    private Integer shallopHostPort = null;

    public HostSettings(String hostIP,Integer hostPort, ChordNetworkSettings chordNetworkSettings,Boolean verboseOperatingMode)
    {
        this.setHostIP(hostIP);
        this.setHostPort(hostPort);
        this.setChordNetworkSettings(chordNetworkSettings);
        this.setVerboseOperatingMode(verboseOperatingMode);
        this.setConnectionTimeout_MS(connectionTimeout_ms);
        this.setShallopHostIP(shallopHostIP);
        this.setShallopHostPort(shallopHostPort);
    }

    /*Setter methods*/
    private void setHostIP(String hostIP){this.hostIP = hostIP; }
    private void setHostPort(Integer hostPort){this.hostPort = hostPort;}
    private void setVerboseOperatingMode(Boolean verboseOperatingMode){this.verboseOperatingMode = verboseOperatingMode; }
    private void setChordNetworkSettings(ChordNetworkSettings chordNetworkSettings){this.chordNetworkSettings = chordNetworkSettings; }
    private void setConnectionTimeout_MS(Integer connectionTimeout_ms){this.connectionTimeout_ms = connectionTimeout_ms;}
    private void setShallopHostIP(String shallopHostIP){ this.shallopHostIP = shallopHostIP; }
    private void setShallopHostPort(Integer shallopHostPort){this.shallopHostPort = shallopHostPort; }

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

    /**
     * Method used for changing the current shallop host
     * @param newShallopHostIP
     * @param newShallopHostPort
     */
    public void changeShallopHost(String newShallopHostIP,Integer newShallopHostPort)
    {
        this.setShallopHostIP(shallopHostIP);
        this.setShallopHostPort(shallopHostPort);
    }

    /**
     * Method used for changing the connection timeout of the current host
     * @param connectionTimeout_ms
     */
    public void changeConnectionTimeout_ms(Integer connectionTimeout_ms)
    {
        this.setConnectionTimeout_MS(connectionTimeout_ms);
    }

    /**
     * Method used to decree if the current host has associated a shallop host
     * or not.
     *
     * @return
     */
    public Boolean hasShallopHost()
    {
        if(this.hostIP.equals(this.shallopHostIP))
            return false;

        return true;
    }

    /*Getter methods*/
    public String getHostIP(){return  this.hostIP; }
    public Integer getHostPort(){return this.hostPort; }
    public Boolean getVerboseOperatingMode(){return  this.verboseOperatingMode; }
    public ChordNetworkSettings getChordNetworkSettings(){return this.chordNetworkSettings; }
    public Integer getConnectionTimeout_MS(){return this.connectionTimeout_ms; }
    public String getShallopHostIP(){return  this.shallopHostIP; }
    public Integer getShallopHostPort(){return this.shallopHostPort;}

    @Override
    public String toString()
    {
        String state = "\n======={HOST SETTINGS}======\n";

        state += "\nHost IP: " + this.getHostIP();
        state += "\nHost port: " + this.getHostPort();
        if(this.getVerboseOperatingMode()) state += "\nVerbose operating mode";
        else state += "\nSilent operating mode";
        state += "\nConnection timeout : " + this.getConnectionTimeout_MS() + "[[ms]]";
        //state += "\nChord network settings: " + this.getChordNetworkSettings().toString();
        if(this.getHostIP().equals(this.getShallopHostIP()))
        {
            state += "\nShallop host not setted";
        }
        else
        {
            state += "\nShallop host :> IP: " + this.getShallopHostIP() + " port: " + this.getShallopHostPort();
        }

        return  state;
    }
}
