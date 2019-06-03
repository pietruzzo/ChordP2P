package com.distributed.chordApp.cooperativemirroring.server.core.settings;

import com.distributed.chordApp.cooperativemirroring.server.core.Host;
import com.distributed.chordApp.cooperativemirroring.server.core.managers.ClientHandlerThread;
import com.distributed.chordApp.cooperativemirroring.server.core.settings.exceptions.HostSettingException;
import com.distributed.chordApp.cooperativemirroring.server.core.settings.exceptions.HostSettingsExceptionCode;
import com.distributed.chordApp.cooperativemirroring.server.utilities.ChordSettingsLoader;
import com.distributed.chordApp.cooperativemirroring.common.utilities.SystemUtilities;

import javax.swing.*;
import java.io.Serializable;

/**
 * Class used for storing all the useful settings for a specific host
 *
 * @date 2019-03-27
 * @version 1.0
 */
public class HostSettings implements Serializable
{
    //Possible values for the verboseInfoLog
    public static final String HOST_CALLER = Host.class.getSimpleName();
    public static final String CLIENT_HANDLER_CALLER = ClientHandlerThread.class.getSimpleName();

    //IP address associated to a specific host
    private String hostIP = null;
    //Address associated to a specific host port for the cooperative mirroring service
    private Integer hostPort = null;
    //Boolean flag used to state if the host as to operate in a verbose mode
    private Boolean verboseOperatingMode = null;

    //Object used for setting up the connection to a chord network by a specific host
    private ChordNetworkSettings chordNetworkSettings = null;

    //IP of the host where to send the resources in case of this host exiting the network
    private String shallopHostIP = null;
    //Port of the shallop host
    private Integer shallopHostPort = null;

    //Reference to the out console
    private JTextArea shell = null;

    private HostSettings(String hostIP,
                         Integer hostPort,
                         ChordNetworkSettings chordNetworkSettings,
                         String shallopHostIP,
                         Integer shallopHostPort,
                         Boolean verboseOperatingMode,
                         JTextArea shell) {
        this.setHostIP(hostIP);
        this.setHostPort(hostPort);

        this.setChordNetworkSettings(chordNetworkSettings);

        this.setShallopHostIP(shallopHostIP);
        this.setShallopHostPort(shallopHostPort);

        this.setVerboseOperatingMode(verboseOperatingMode);

        this.setShell(shell);
    }

    /*Setter methods*/
    private void setHostIP(String hostIP){this.hostIP = hostIP; }
    private void setHostPort(Integer hostPort){this.hostPort = hostPort;}
    private void setVerboseOperatingMode(Boolean verboseOperatingMode){this.verboseOperatingMode = verboseOperatingMode; }
    private void setChordNetworkSettings(ChordNetworkSettings chordNetworkSettings){this.chordNetworkSettings = chordNetworkSettings; }
    private void setShallopHostIP(String shallopHostIP){ this.shallopHostIP = shallopHostIP; }
    private void setShallopHostPort(Integer shallopHostPort){this.shallopHostPort = shallopHostPort; }
    private void setShell(JTextArea shell){this.shell = shell;}

    /*Application methods*/

    /**
     * Method used for printing the status of operations for the current host
     * @param info
     * @param caller
     * @param error
     */
    public void verboseInfoLog(String info,String caller,Boolean error) {
        if(!this.verboseOperatingMode)
        {
            return ;
        }

        boolean enabledConsoleOut = (this.getShell() != null);

        String vString = "[Host\\\\" + this.getHostIP() + ":" + this.getHostPort() ;

        vString += " :: " + caller + " > " ;
        vString += info;

        if(error.booleanValue()) {
            if(!enabledConsoleOut) {
                System.err.println(vString);
            }

        }
        else {
            if(!enabledConsoleOut) {
                System.out.println(vString);
            }
        }

        if(enabledConsoleOut) {
            String text = this.shell.getText();
            text += "\n" + vString;

            this.shell.setText(text);
        }
    }

    /**
     * Method used for changing the current shallop host
     * @param newShallopHostIP
     * @param newShallopHostPort
     */
    public void changeShallopHost(String newShallopHostIP,Integer newShallopHostPort) throws HostSettingException {
        if(!SystemUtilities.isValidIP(newShallopHostIP)) {
            throw new HostSettingException(HostSettingsExceptionCode.INVALID_SHALLOP_HOST_IP.getCode());
        }

        if(newShallopHostPort <= 0) {
            throw new HostSettingException(HostSettingsExceptionCode.INVALID_SHALLOP_HOST_PORT.getCode());
        }

        this.setShallopHostIP(shallopHostIP);
        this.setShallopHostPort(shallopHostPort);
    }

    /**
     * Method used to decree if the current host has associated a shallop host
     * or not.
     *
     * @return
     */
    public Boolean hasShallopHost() {
        boolean result = false ;

        if(!this.hostIP.equals(this.shallopHostIP)) {
            result = true;
        }


        return result;
    }

    /**
     * Method used to stop the verbose info log for the current host
     */
    public void mute(){
        this.verboseOperatingMode = false;
    }

    /**
     * Method used for enabling the verbose operating mode for the current host
     */
    public void talkative(){
        this.verboseOperatingMode = true;
    }

    /*Getter methods*/
    public String getHostIP(){return  this.hostIP; }
    public Integer getHostPort(){return this.hostPort; }
    public Boolean getVerboseOperatingMode(){return  this.verboseOperatingMode; }
    public ChordNetworkSettings getChordNetworkSettings(){return this.chordNetworkSettings; }
    public String getShallopHostIP(){return  this.shallopHostIP; }
    public Integer getShallopHostPort(){return this.shallopHostPort;}
    public JTextArea getShell(){return this.shell;}

    @Override
    public String toString()
    {
        String state = "";//"\n======={HOST SETTINGS}======\n";

        state += "\nHost IP: " + this.getHostIP();
        state += "\nHost port: " + this.getHostPort();
        //state += "\nChord network settings: " + this.getChordNetworkSettings().toString();
        if(!this.hasShallopHost())
        {
            state += "\n<shallop host not setted>";
        }
        else
        {
            state += "\nShallop host: " + this.getShallopHostIP() + " : " + this.getShallopHostPort() ;
        }
        if(this.getVerboseOperatingMode()){
            state += "\n<verbose operating mode>";
        }
        else{
            state += "\n<silent operating mode>";
        }

        if(this.shell == null)
        {
            state += "\n<local printing>";
        }
        else
        {
            state += "\n<external shell printing>";
        }

        state += "\n";
        //state += "\n=========================\n";

        return  state;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void finalize(){
        if(this.getShell() != null){
            shell = null;
        }
    }

    //Builder for the HostSettings class
    public static class HostSettingsBuilder
    {
        private String hostIP = SystemUtilities.getThisMachineIP();
        private Integer hostPort = ChordSettingsLoader.getApplicationServerPort();
        private Boolean verboseOperatingMode = false;
        private ChordNetworkSettings chordNetworkSettings = null;
        private String shallopHostIP = SystemUtilities.getThisMachineIP();
        private Integer shallopHostPort = ChordSettingsLoader.getApplicationServerPort();
        private JTextArea shell = null;

        /**
         * Method used for setting the current Host IP
         * @param hostIP
         * @return
         */
        public HostSettingsBuilder setHostIP(String hostIP) throws HostSettingException {
            if(!SystemUtilities.isValidIP(hostIP)) {
                throw new HostSettingException(HostSettingsExceptionCode.INVALID_HOST_IP.getCode());
            }
            else {
                this.hostIP = hostIP;
            }

            return this;
        }

        /**
         * Method used for setting the current Host Port
         * @param hostPort
         * @return
         */
        public HostSettingsBuilder setHostPort(Integer hostPort) throws HostSettingException {
            if(hostPort.intValue() <= 0) {
                throw new HostSettingException(HostSettingsExceptionCode.INVALID_HOST_PORT.getCode());
            }
            else {
                this.hostPort = hostPort;
            }

            return this;
        }

        /**
         * Method used for setting the verbose mode for the current Host
         * @param verboseOperatingMode
         * @return
         */
        public HostSettingsBuilder setVerboseOperatingMode(Boolean verboseOperatingMode) throws HostSettingException {
            if(verboseOperatingMode == null) {
                throw new HostSettingException(HostSettingsExceptionCode.INVALID_VERBOSE_OPERATING_MODE.getCode());
            }
            else {
                this.verboseOperatingMode = verboseOperatingMode;
            }

            return this;
        }

        /**
         * Method used for setting the ChordNetworkSetting for the current Host
         * @param chordNetworkSetting
         * @return
         */
        public HostSettingsBuilder setChordNetworkSetting(ChordNetworkSettings chordNetworkSetting) throws HostSettingException {
            if(chordNetworkSetting == null) {
                throw new HostSettingException(HostSettingsExceptionCode.INVALID_CHORD_NETWORK_SETTINGS.getCode());
            }
            else {
                this.chordNetworkSettings = chordNetworkSetting;
            }

            return this;
        }

        /**
         * Method used for setting the ShallopHostIP for the curent Host
         * @param shallopHostIP
         * @return
         */
        public HostSettingsBuilder setShallopHostIP(String shallopHostIP) throws HostSettingException {
            if(!SystemUtilities.isValidIP(shallopHostIP)) {
                throw new HostSettingException(HostSettingsExceptionCode.INVALID_SHALLOP_HOST_PORT.getCode());
            }
            else {
                this.shallopHostIP = shallopHostIP;
            }

            return this;
        }

        /**
         * Method used for setting  the ShallopHostPort for the current host
         * @param shallopHostPort
         * @return
         */
        public HostSettingsBuilder setShallopHostPort(Integer shallopHostPort) throws HostSettingException {
            if(shallopHostPort.intValue() <= 0) {
                throw new HostSettingException(HostSettingsExceptionCode.INVALID_SHALLOP_HOST_PORT.getCode());
            }
            else {
                this.shallopHostPort = shallopHostPort;
            }

            return this;
        }

        /**
         * Method used for setting the output channel for the host's logs
         * @param shell
         * @return
         */
        public HostSettingsBuilder setShell(JTextArea shell) {
            this.shell = shell;

            return this;
        }

        /**
         * Method used for setting the HostSettings
         * @return
         */
        public HostSettings build() throws HostSettingException {
            HostSettings instance = null;

            if(chordNetworkSettings == null) {
                throw new HostSettingException(HostSettingsExceptionCode.INVALID_CHORD_NETWORK_SETTINGS.getCode());
            }

            instance = new HostSettings(
                                            this.hostIP,
                                            this.hostPort,
                                            this.chordNetworkSettings,
                                            this.shallopHostIP,
                                            this.shallopHostPort,
                                            this.verboseOperatingMode,
                                            this.shell);

            return instance;
        }
    }
}


