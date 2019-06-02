package com.distributed.chordApp.cooperativemirroring.client.utilities;

import com.distributed.chordApp.cooperativemirroring.common.utilities.SystemUtilities;

/**
 * Class that contains several utilities for the current Client
 */
public class ClientSettings {
    //IP address of the client
    private String clientIP = null;
    //Port of the client
    private Integer clientPort = null;
    //Boolean flag used to decide if we have to use the verbose mode
    private boolean verbose = false;
    //Boolean flag used to decide if we have to use the GUI
    private boolean guiMode = false;
    //IP associated to the current server
    private String serverIP = null;
    //Port associated to the reference server
    private Integer serverPort = null;

    private ClientSettings(String clientIP,Integer clientPort,String serverIP,Integer serverPort, boolean guiMode, boolean verbose){
        this.clientIP = clientIP;
        this.clientPort = clientPort;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.verbose = verbose;
        this.guiMode = guiMode;
    }

    /*Application methods*/
    /**
     * Method used for representing a log strng associated to a client
     * @param infoMessage
     * @return
     */
    public String clientInfoString(String infoMessage) {
        String infoString = "[Client\\\\" + this.getClientIP() + "::" + this.getClientPort() + ">";

        infoString += infoMessage;

        return infoString;
    }

    /**
     * Method used for printing log messages associated to a client
     * @param infoMessage
     * @param error
     */
    private void clientVerboseLog(String infoMessage,boolean error) {
        if(!this.verbose) {
            return ;
        }

        String infoString = this.clientInfoString(infoMessage);

        if(error) {
            System.err.println(infoString);
        }
        else {
            System.out.println(infoString);
        }
    }

    /*Getters*/
    public String getClientIP(){return this.clientIP;}
    public Integer getClientPort() {return clientPort; }
    public String getReferenceServerIP(){return this.serverIP;}
    public Integer getReferenceServerPort(){return this.serverPort;}
    public boolean isGuiMode() {return guiMode; }
    public boolean isVerbose() {return verbose; }

    /*toString*/
    @Override
    public String toString() {
        String state = "\n========== Client Settings =========\n";

        state += "\nClient IP = " + this.clientIP;
        state += "\nClient port = " + this.clientPort;
        state += "\nReference server ip = " + this.serverIP;
        state += "\nReference server port = " + this.serverPort;
        if(this.guiMode){
            state += "\n<GUI mode>";
        }
        else{
            state += "\n<TUI mode>";
        }

        if(this.verbose){
            state += "<\nverbose>";
        }

        state += "\n==================================\n";

        return state;
    }

    public static class ClientSettingsBuilder{
        private String clientIP = null;
        private Integer clientPort = null;
        private Boolean verbose = false;
        private Boolean guiMode = false;
        private String serverIP = null;
        private Integer serverPort = null;

        /**
         * Method used for setting the client ip
         * @param clientIP
         * @return
         * @throws ClientException
         */
        public ClientSettingsBuilder setClientIP(String clientIP) throws ClientException {
            if(!SystemUtilities.isValidIP(clientIP)){
                throw new ClientException(ClientExceptionCode.INVALID_CLIENT_IP.getCode());
            }

            this.clientIP = clientIP;

            return this;
        }

        /**
         * Method used for setting the client port of a specific client
         * @param clientPort
         * @return
         * @throws ClientException
         */
        public ClientSettingsBuilder setClientPort(Integer clientPort) throws ClientException {
            if(!SystemUtilities.isValidPort(clientPort)){
                throw new ClientException(ClientExceptionCode.INVALID_CLIENT_PORT.getCode());
            }

            this.clientPort = clientPort;

            return this;
        }

        /**
         * Method used to decide if we have to use the verbose mode
         * @param verbose
         * @return
         */
        public ClientSettingsBuilder setVerbose(boolean verbose){
            this.verbose = verbose;
            return this;
        }

        /**
         * Method used to decide if we have to use the gui mode or not
         * @param guiMode
         * @return
         */
        public ClientSettingsBuilder setGUIMode(boolean guiMode){
            this.guiMode = guiMode;

            return this;
        }

        /**
         * Method used for setting the reference server IP
         * @param serverIP
         * @return
         * @throws ClientException
         */
        public ClientSettingsBuilder setServerIP(String serverIP) throws ClientException {
            if(!SystemUtilities.isValidIP(serverIP)){
                throw new ClientException(ClientExceptionCode.INVALID_SERVER_IP.getCode());
            }

            this.serverIP = serverIP;

            return this;
        }

        /**
         * Method used for setting the reference server port for the current client
         * @param serverPort
         * @return
         * @throws ClientException
         */
        public ClientSettingsBuilder setServerPort(Integer serverPort) throws ClientException {
            if(!SystemUtilities.isValidPort(serverPort)){
                throw new ClientException(ClientExceptionCode.INVALID_SERVER_PORT.getCode());
            }

            this.serverPort = serverPort;

            return this;
        }

        /**
         * Method usef for building a ClientSettings Object
         * @return
         * @throws ClientException
         */
        public ClientSettings build() throws ClientException {
            if(this.clientIP == null){
                throw new ClientException(ClientExceptionCode.INVALID_CLIENT_IP.getCode());
            }

            if(this.clientPort == null){
                throw new ClientException(ClientExceptionCode.INVALID_CLIENT_PORT.getCode());
            }

            if(this.serverIP == null){
                throw new ClientException(ClientExceptionCode.INVALID_SERVER_IP.getCode());
            }

            if(this.serverPort == null){
                throw new ClientException(ClientExceptionCode.INVALID_SERVER_PORT.getCode());
            }

            ClientSettings cSettings = new ClientSettings(this.clientIP, this.clientPort, this.serverIP, this.serverPort, this.guiMode, this.verbose);

            return cSettings;
        }
    }

}
