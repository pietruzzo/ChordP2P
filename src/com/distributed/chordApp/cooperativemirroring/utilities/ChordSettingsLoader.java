package com.distributed.chordApp.cooperativemirroring.utilities;

/**
 * Class used for retriving some specific elements from the configuration file of chord
 *
 */
public class ChordSettingsLoader {

    public static final String CHORD_SETTINGS_FILE = "chordsettings.xml";
    public static final String HIDDEN_SETTINGS_FILE = "hiddensettings.xml";
    public static final String BOOTSTRAP_SERVER_TAG = "BootstrapServer";
    public static final String APPLICATION_SERVER_PORT_TAG = "ApplicationServerPort";
    public static final String APPLICATION_CLIENT_PORT_TAG = "ApplicationClientPort";
    public static final String CHORD_PORT_TAG = "ChordPort";
    public static final String JOIN_CHORD_NETWORK_TAG = "JoinChordNetwork";
    public static final String VERBOSE_OPERATING_MODE_TAG = "VerboseOperatingMode";
    public static final String ENABLE_LOG_SHELL_GUI_TAG = "EnableLogShellGUI";

    /**
     * Method used for retriving the bootstrap server IP of the application
     * @return
     */
    public static synchronized String getBootstrapServerIP()
    {
        return XMLParser.getXMLElement(CHORD_SETTINGS_FILE, BOOTSTRAP_SERVER_TAG, 0);
    }

    /**
     * Method used for retriving the bootstrap server Port of the application
     * @return
     */
    public static synchronized Integer getApplicationServerPort()
    {
        String tmp = XMLParser.getXMLElement(HIDDEN_SETTINGS_FILE, APPLICATION_SERVER_PORT_TAG, 0);

        return Integer.parseInt(tmp);
    }

    /**
     * Method used for retriving the client port of the application
     * @return
     */
    public static synchronized Integer getApplicationClientPort()
    {
        String tmp = XMLParser.getXMLElement(HIDDEN_SETTINGS_FILE, APPLICATION_CLIENT_PORT_TAG, 0);

        return Integer.parseInt(tmp);
    }

    /**
     * Method used for retriving thr port associated to the chord object
     * @return
     */
    public static synchronized Integer getChordPort()
    {
        String tmp = XMLParser.getXMLElement(HIDDEN_SETTINGS_FILE, CHORD_PORT_TAG, 0);

        return Integer.parseInt(tmp);
    }

    /**
     * Method used for retriving if we have to use the log shell gui or not on the server side
     * @return
     */
    public static synchronized Boolean getEnableLogShellGUI(){
        String tmp = XMLParser.getXMLElement(HIDDEN_SETTINGS_FILE, ENABLE_LOG_SHELL_GUI_TAG, 0);

        return Boolean.parseBoolean(tmp);
    }

    /**
     * Method used for retriving if we have to use the verbose operating mode server side
     * @return
     */
    public static synchronized Boolean getVerboseOperatingMode(){
        String tmp = XMLParser.getXMLElement(HIDDEN_SETTINGS_FILE, VERBOSE_OPERATING_MODE_TAG, 0);

        return Boolean.parseBoolean(tmp);
    }



    /**
     * Method used by servers that are not the bootstrap server if they have to
     * create or not new chord networks.
     * @return
     */
    public static synchronized Boolean getJoinChordNetwork()
    {
        String tmp = XMLParser.getXMLElement(HIDDEN_SETTINGS_FILE, JOIN_CHORD_NETWORK_TAG, 0);

        return Boolean.parseBoolean(tmp);
    }
}
