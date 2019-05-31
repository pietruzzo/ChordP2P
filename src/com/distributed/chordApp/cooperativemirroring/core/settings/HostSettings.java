package com.distributed.chordApp.cooperativemirroring.core.settings;

import com.distributed.chordApp.cooperativemirroring.core.Host;
import com.distributed.chordApp.cooperativemirroring.core.backend.ClientHandlerThread;
import com.distributed.chordApp.cooperativemirroring.core.backend.HostHandlerThread;
import com.distributed.chordApp.cooperativemirroring.core.settings.exceptions.HostSettingException;
import com.distributed.chordApp.cooperativemirroring.core.settings.exceptions.codes.HostSettingsExceptionCode;
import com.distributed.chordApp.cooperativemirroring.utilities.ChordSettingsLoader;
import com.distributed.chordApp.cooperativemirroring.utilities.LogShell;
import com.distributed.chordApp.cooperativemirroring.utilities.SystemUtilities;
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
    public static final String HOST_HANDLER_CALLER = HostHandlerThread.class.getSimpleName();
    public static final String CLIENT_HANDLER_CALLER = ClientHandlerThread.class.getSimpleName();

    //IP address associated to a specific host
    private String hostIP = null;
    //Address associated to a specific host port for the cooperative mirroring service
    private Integer hostPort = null;
    //Boolean flag used to state if the host as to operate in a verbose mode
    private Boolean verboseOperatingMode = null;
    //Variable that holds the value of the timeout for the socket requests
    private Integer connectionTimeout_ms = null;
    //Variable that holds the number of message sending retries before giving up
    private Integer connectionRetries = null;

    //Object used for setting up the connection to a chord network by a specific host
    private ChordNetworkSettings chordNetworkSettings = null;

    //IP of the host where to send the resources in case of this host exiting the network
    private String shallopHostIP = null;
    //Port of the shallop host
    private Integer shallopHostPort = null;

    //Reference to the out console
    private LogShell shell = null;

    private HostSettings(String hostIP,
                         Integer hostPort,
                         ChordNetworkSettings chordNetworkSettings,
                         Integer connectionTimeout_ms,
                         Integer connectionRetries,
                         String shallopHostIP,
                         Integer shallopHostPort,
                         Boolean verboseOperatingMode,
                         LogShell shell) {
        this.setHostIP(hostIP);
        this.setHostPort(hostPort);

        this.setChordNetworkSettings(chordNetworkSettings);

        this.setConnectionTimeout_MS(connectionTimeout_ms);
        this.setConnectionRetries(connectionRetries);

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
    private void setConnectionTimeout_MS(Integer connectionTimeout_ms){this.connectionTimeout_ms = connectionTimeout_ms;}
    private void setShallopHostIP(String shallopHostIP){ this.shallopHostIP = shallopHostIP; }
    private void setShallopHostPort(Integer shallopHostPort){this.shallopHostPort = shallopHostPort; }
    private void setConnectionRetries(Integer connectionRetries){this.connectionRetries = connectionRetries; }
    private void setShell(LogShell shell){this.shell = shell;}

    /*Application methods*/

    /**
     * Method used for printing the status of operations for the current host
     * @param info
     * @param caller
     * @param error
     */
    public void verboseInfoLog(String info,String caller,Boolean error)
    {
        if(!this.verboseOperatingMode)
        {
            return ;
        }

        boolean enabledConsoleOut = (this.getShell() != null);

        String vString = "[Host\\\\" + this.getHostIP() + ":" + this.getHostPort() ;

        vString += " :: " + caller + " > " ;
        vString += info;

        if(error.booleanValue())
        {
            if(!enabledConsoleOut)
            {
                System.err.println(vString);
            }

        }
        else
        {
            if(!enabledConsoleOut)
            {
                System.out.println(vString);
            }
        }

        if(enabledConsoleOut)
        {
            this.shell.updateText(vString);
        }
    }

    /**
     * Method used for changing the current shallop host
     * @param newShallopHostIP
     * @param newShallopHostPort
     */
    public void changeShallopHost(String newShallopHostIP,Integer newShallopHostPort) throws HostSettingException
    {
        if(!SystemUtilities.isValidIP(newShallopHostIP))
        {
            throw new HostSettingException(HostSettingsExceptionCode.INVALID_SHALLOP_HOST_IP.getCode());
        }

        if(newShallopHostPort <= 0)
        {
            throw new HostSettingException(HostSettingsExceptionCode.INVALID_SHALLOP_HOST_PORT.getCode());
        }

        this.setShallopHostIP(shallopHostIP);
        this.setShallopHostPort(shallopHostPort);
    }

    /**
     * Method used for changing the connection timeout of the current host
     * @param connectionTimeout_ms
     */
    public void changeConnectionTimeout_ms(Integer connectionTimeout_ms) throws HostSettingException
    {
        if(connectionTimeout_ms.intValue() < 0)
        {
            throw new HostSettingException(HostSettingsExceptionCode.INVALID_CONNECTION_TIMEOUT_MS.getCode());
        }

        this.setConnectionTimeout_MS(connectionTimeout_ms);
    }

    /**
     * Method used for changing the connection retries of the current host
     * @param connectionRetries
     */
    public void changeConnectionRetries(Integer connectionRetries) throws HostSettingException
    {
        if(connectionRetries.intValue() < 0)
        {
            throw new HostSettingException(HostSettingsExceptionCode.INVALID_CONNECTION_RETRIES.getCode());
        }

        this.setConnectionRetries(connectionRetries);
    }

    /**
     * Method used to decree if the current host has associated a shallop host
     * or not.
     *
     * @return
     */
    public Boolean hasShallopHost()
    {
        boolean result = false ;

        if(!this.hostIP.equals(this.shallopHostIP))
        {
            result = true;
        }


        return result;
    }

    /*Getter methods*/
    public String getHostIP(){return  this.hostIP; }
    public Integer getHostPort(){return this.hostPort; }
    public Boolean getVerboseOperatingMode(){return  this.verboseOperatingMode; }
    public ChordNetworkSettings getChordNetworkSettings(){return this.chordNetworkSettings; }
    public Integer getConnectionTimeout_MS(){return this.connectionTimeout_ms; }
    public Integer getConnectionRetries(){return this.connectionRetries; }
    public String getShallopHostIP(){return  this.shallopHostIP; }
    public Integer getShallopHostPort(){return this.shallopHostPort;}
    public LogShell getShell(){return this.shell;}

    @Override
    public String toString()
    {
        String state = "";//"\n======={HOST SETTINGS}======\n";

        state += "\nHost IP: " + this.getHostIP();
        state += "\nHost port: " + this.getHostPort();
        state += "\nConnection retries : " + this.getConnectionRetries() ;
        state += "\nConnection timeout : " + this.getConnectionTimeout_MS() + "[[ms]]";
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

    //Builder for the HostSettings class
    public static class HostSettingsBuilder
    {
        private String hostIP = SystemUtilities.getThisMachineIP();
        private Integer hostPort = ChordSettingsLoader.getApplicationServerPort();
        private Boolean verboseOperatingMode = false;
        private ChordNetworkSettings chordNetworkSettings = null;
        private String shallopHostIP = SystemUtilities.getThisMachineIP();
        private Integer shallopHostPort = ChordSettingsLoader.getApplicationServerPort();
        private Integer connectionRetries = 5;
        private Integer connectionTimeout_ms = 3000;
        private LogShell shell = null;

        /**
         * Method used for setting the current Host IP
         * @param hostIP
         * @return
         */
        public HostSettingsBuilder setHostIP(String hostIP) throws HostSettingException
        {
            if(!SystemUtilities.isValidIP(hostIP))
            {
                throw new HostSettingException(HostSettingsExceptionCode.INVALID_HOST_IP.getCode());
            }
            else
            {
                this.hostIP = hostIP;
            }

            return this;
        }

        /**
         * Method used for setting the current Host Port
         * @param hostPort
         * @return
         */
        public HostSettingsBuilder setHostPort(Integer hostPort) throws HostSettingException
        {
            if(hostPort.intValue() <= 0)
            {
                throw new HostSettingException(HostSettingsExceptionCode.INVALID_HOST_PORT.getCode());
            }
            else
            {
                this.hostPort = hostPort;
            }

            return this;
        }

        /**
         * Method used for setting the verbose mode for the current Host
         * @param verboseOperatingMode
         * @return
         */
        public HostSettingsBuilder setVerboseOperatingMode(Boolean verboseOperatingMode) throws HostSettingException
        {
            if(verboseOperatingMode == null)
            {
                throw new HostSettingException(HostSettingsExceptionCode.INVALID_VERBOSE_OPERATING_MODE.getCode());
            }
            else
            {
                this.verboseOperatingMode = verboseOperatingMode;
            }

            return this;
        }

        /**
         * Method used for setting the ChordNetworkSetting for the current Host
         * @param chordNetworkSetting
         * @return
         */
        public HostSettingsBuilder setChordNetworkSetting(ChordNetworkSettings chordNetworkSetting) throws HostSettingException
        {
            if(chordNetworkSetting == null)
            {
                throw new HostSettingException(HostSettingsExceptionCode.INVALID_CHORD_NETWORK_SETTINGS.getCode());
            }
            else
            {
                this.chordNetworkSettings = chordNetworkSetting;
            }

            return this;
        }

        /**
         * Method used for setting the ShallopHostIP for the curent Host
         * @param shallopHostIP
         * @return
         */
        public HostSettingsBuilder setShallopHostIP(String shallopHostIP) throws HostSettingException
        {
            if(!SystemUtilities.isValidIP(shallopHostIP))
            {
                throw new HostSettingException(HostSettingsExceptionCode.INVALID_SHALLOP_HOST_PORT.getCode());
            }
            else
            {
                this.shallopHostIP = shallopHostIP;
            }

            return this;
        }

        /**
         * Method used for setting  the ShallopHostPort for the current host
         * @param shallopHostPort
         * @return
         */
        public HostSettingsBuilder setShallopHostPort(Integer shallopHostPort) throws HostSettingException
        {
            if(shallopHostPort.intValue() <= 0)
            {
                throw new HostSettingException(HostSettingsExceptionCode.INVALID_SHALLOP_HOST_PORT.getCode());
            }
            else
            {
                this.shallopHostPort = shallopHostPort;
            }

            return this;
        }

        /**
         * Method used for setting the retries in case of communication failure
         * @param connectionRetries
         * @return
         */
        public HostSettingsBuilder setConnectionRetries(Integer connectionRetries) throws HostSettingException
        {
            if(connectionRetries.intValue() < 0)
            {
                throw new HostSettingException(HostSettingsExceptionCode.INVALID_CONNECTION_RETRIES.getCode());
            }
            else
            {
                this.connectionRetries = connectionRetries;
            }

            return this;
        }

        /**
         * Method used for setting the connection timeout in case of communication between
         * hosts
         * @param connectionTimeout_ms
         * @return
         */
        public HostSettingsBuilder setConnectionTimeout_ms(Integer connectionTimeout_ms) throws HostSettingException
        {
            if(connectionTimeout_ms.intValue() < 0)
            {
                throw new HostSettingException(HostSettingsExceptionCode.INVALID_CONNECTION_TIMEOUT_MS.getCode());
            }
            else
            {
                this.connectionTimeout_ms = connectionTimeout_ms;
            }


            return this;
        }

        /**
         * Method used for setting the output channel for the host's logs
         * @param shell
         * @return
         */
        public HostSettingsBuilder setShell(LogShell shell)
        {
            this.shell = shell;

            return this;
        }

        /**
         * Method used for setting the HostSettings
         * @return
         */
        public HostSettings build() throws HostSettingException
        {
            HostSettings instance = null;

            if(chordNetworkSettings == null)
            {
                throw new HostSettingException(HostSettingsExceptionCode.INVALID_CHORD_NETWORK_SETTINGS.getCode());
            }

            instance = new HostSettings(
                                            this.hostIP,
                                            this.hostPort,
                                            this.chordNetworkSettings,
                                            this.connectionTimeout_ms,
                                            this.connectionRetries,
                                            this.shallopHostIP,
                                            this.shallopHostPort,
                                            this.verboseOperatingMode,
                                            this.shell);

            return instance;
        }
    }
}


