package com.distributed.chordApp.cooperativemirroring.core.settings;

import com.distributed.chordApp.cooperativemirroring.core.settings.exceptions.ChordNetworkSettingsException;
import com.distributed.chordApp.cooperativemirroring.core.settings.exceptions.codes.ChordNetworkSettingsExceptionCode;
import com.distributed.chordApp.cooperativemirroring.utilities.ChordSettingsLoader;
import com.distributed.chordApp.cooperativemirroring.utilities.SystemUtilities;
import com.distributed.chordLib.Chord;

import java.io.Serializable;

/**
 * Class used for passing Chord network parameters to a Host in order to allow
 * it to join or create (and use) a chord network
 */
public class ChordNetworkSettings implements Serializable {
    //Port of the host that has the following settings
    private Integer associatedPort = null;
    //Parameter used for not allowing any more changes in parameters
    private Boolean changesLocked = false;

    //Boolean flag used to state if the client handlers have to perform a basic lookup
    private Boolean performBasicLookups = false;

    //Boolean used to state if the host should join an existing network or not
    private Boolean joinExistingChordNetwork = null;
    //Bootstrap sever address of the chord network
    private String bootstrapServerAddress = null;
    //Number of fingers of the finger table
    private Integer numberOfFingers = null;
    //Number of successors for each host
    private Integer numberOfSuccessors = null;
    //Size of the module od the chord
    private Integer chordModule = null;

    private ChordNetworkSettings(Integer associatedHostPort,
                                String bootstrapServerAddress,
                                Boolean joinExistingChordNetwork,
                                Integer numberOfFingers,
                                Integer numberOfSuccessors,
                                Integer chordModule,
                                Boolean performBasicLookups
                                )
    {

        this.setAssociatedHostPort(associatedHostPort);
        this.setBootstrapServerAddress(bootstrapServerAddress);
        this.setJoinExistingChordNetwork(joinExistingChordNetwork);
        this.setNumberOfFingers(numberOfFingers);
        this.setNumberOfSuccessors(numberOfSuccessors);
        this.setChordModule(chordModule);
        this.setPerformBasicLookups(performBasicLookups);
    }

    /*Setter methods*/
    private void setAssociatedHostPort(Integer associatedPort){ this.associatedPort = associatedPort; }
    private void setJoinExistingChordNetwork(Boolean joinExistingChordNetwork){this.joinExistingChordNetwork = joinExistingChordNetwork;}
    private void setBootstrapServerAddress(String bootstrapServerAddress){this.bootstrapServerAddress = bootstrapServerAddress;}
    private void setNumberOfFingers(Integer numberOfFingers){this.numberOfFingers = numberOfFingers;}
    public void setPerformBasicLookups(Boolean performBasicLookups){this.performBasicLookups = performBasicLookups;}
    private void setNumberOfSuccessors(Integer numberOfSuccessors){this.numberOfSuccessors = numberOfSuccessors; }
    private void setChordModule(Integer chordModule){this.chordModule = chordModule; }

    /*Getter methods*/
    public Integer getAssociatedPort(){ return this.associatedPort; }
    public Boolean getJoinExistingChordNetwork(){ return this.joinExistingChordNetwork; }
    public String  getBootstrapServerAddress(){ return this.bootstrapServerAddress; }
    public Integer getNumberOfFingers(){ return this.numberOfFingers; }
    public Integer getNumberOfSuccessors(){ return this.numberOfSuccessors; }
    public Integer getChordModule(){return this.chordModule; }
    public Boolean getPerformBasicLookups(){return this.performBasicLookups; }

    @Override
    public String toString(){
        String state = "\n======{ CHORD NETWORK SETTINGS }======\n";

        state += "\nChanges locked: ";
        state += "\nAssociated Host Port: " + this.getAssociatedPort();
        state += "\nJoin an existing chord network: ";
        if(this.getJoinExistingChordNetwork()) state += "<true>\n";
        else state += "<false>\n";
        if(this.getBootstrapServerAddress() != null) state += "\nBootstrap Server address: " + this.getBootstrapServerAddress();
        if(this.getNumberOfFingers() != null) state += "\nNumber of fingers: " + this.getNumberOfFingers();
        if(this.getNumberOfSuccessors() != null) state += "\nNumber of successors: " + this.getNumberOfSuccessors();
        if(this.getChordModule() != null) state += "\nModule: " + this.getChordModule();
        if(this.getPerformBasicLookups()) state += "\nPerform basic lookups";
        else state += "\nPerform optimized lookups";

        return state;
    }

    public static class ChordNetworkSettingsBuilder
    {
        private Integer associatedPort = ChordSettingsLoader.getChordPort();
        private String bootstrapServerAddress = ChordSettingsLoader.getBootstrapServerIP();
        private Integer numberOfFingers = Chord.DEFAULT_NUM_FINGERS;
        private Integer numberOfSuccessors = Chord.DEFAULT_NUM_SUCCESSORS;
        private Integer chordModule = Chord.DEFAULT_CHORD_MODULE;
        private Boolean performBasicLookup = false;
        private Boolean joinExistingChordNetwork = ChordSettingsLoader.getJoinChordNetwork();

        /**
         * Method used for setting the Chord port for a chord network
         * @param associatedPort
         * @return
         * @throws ChordNetworkSettingsException
         */
        public ChordNetworkSettingsBuilder setAssociatedPort(Integer associatedPort) throws ChordNetworkSettingsException
        {
            if(associatedPort.intValue() <= 0)
            {
                throw new ChordNetworkSettingsException(ChordNetworkSettingsExceptionCode.INVALID_CHORD_PORT.getCode());
            }
            else
            {
                this.associatedPort = associatedPort;
            }

            return this;
        }

        /**
         * Method used for setting the joining method for a chord network
         * @param joinExistingChordNetwork
         * @return
         * @throws ChordNetworkSettingsException
         */
        public ChordNetworkSettingsBuilder setJoinExistingChordNetwork(Boolean joinExistingChordNetwork) throws ChordNetworkSettingsException
        {
            if(joinExistingChordNetwork == null)
            {
                throw new ChordNetworkSettingsException(ChordNetworkSettingsExceptionCode.INVALID_CHORD_NETWORK_JOINING_MODE.getCode());
            }
            else
            {
                this.joinExistingChordNetwork = joinExistingChordNetwork;
            }

            return this;
        }

        /**
         * Method used for setting the bootstrap server for a specific chord network
         * @param bootstrapServerAddress
         * @return
         * @throws ChordNetworkSettingsException
         */
        public ChordNetworkSettingsBuilder setBootstrapServerAddress(String bootstrapServerAddress) throws ChordNetworkSettingsException
        {
            if(!SystemUtilities.isValidIP(bootstrapServerAddress))
            {
                throw new ChordNetworkSettingsException(ChordNetworkSettingsExceptionCode.INVALID_CHORD_BOOTSTRAP_SERVER_IP.getCode());
            }
            else
            {
                this.bootstrapServerAddress = bootstrapServerAddress;
            }

            return this;
        }

        /**
         * Method used for setting the number of fingers for a specific chord network
         * @param numberOfFingers
         * @return
         * @throws ChordNetworkSettingsException
         */
        public ChordNetworkSettingsBuilder setNumberOfFingers(Integer numberOfFingers) throws ChordNetworkSettingsException
        {
            if(numberOfFingers <= 0)
            {
                throw new ChordNetworkSettingsException(ChordNetworkSettingsExceptionCode.INVALID_CHORD_NUMBER_OF_FINGERS.getCode());
            }
            else
            {
                this.numberOfFingers = numberOfFingers;
            }

            return this;
        }

        /**
         * Method used for setting the number of successors for a specific chord network
         * @param numberOfSuccessors
         * @return
         * @throws ChordNetworkSettingsException
         */
        public ChordNetworkSettingsBuilder setNumberOfSuccessors(Integer numberOfSuccessors) throws ChordNetworkSettingsException
        {
            if(numberOfSuccessors <= 0)
            {
                throw new ChordNetworkSettingsException(ChordNetworkSettingsExceptionCode.INVALID_CHORD_NUMBER_OF_SUCCESSORS.getCode());
            }
            else
            {
                this.numberOfSuccessors = numberOfSuccessors;
            }

            return this;
        }

        /**
         * Method used for setting the module associated to a specific chord network
         * @param chordModule
         * @return
         * @throws ChordNetworkSettingsException
         */
        public ChordNetworkSettingsBuilder setChordModule(Integer chordModule) throws ChordNetworkSettingsException
        {
            if(chordModule <= 0)
            {
                throw new ChordNetworkSettingsException(ChordNetworkSettingsExceptionCode.INVALID_CHORD_MODULE.getCode());
            }
            else
            {
                this.chordModule = chordModule;
            }

            return this;
        }

        /**
         * Method used for setting the Chord lookup method for a specific chord network
         * @param performBasicLookup
         * @return
         * @throws ChordNetworkSettingsException
         */
        public ChordNetworkSettingsBuilder setPerformBasicLookup(Boolean performBasicLookup) throws ChordNetworkSettingsException
        {
            if(performBasicLookup == null)
            {
                throw new ChordNetworkSettingsException(ChordNetworkSettingsExceptionCode.INVALID_CHORD_LOOKUP_MODE.getCode());
            }
            else
            {
                this.performBasicLookup = performBasicLookup;
            }

            return this;
        }

        /**
         * Method used for creating a ChordSettings instance based on the parameters setted so far
         */
        public ChordNetworkSettings build()
        {
            ChordNetworkSettings instance = null;

            instance = new ChordNetworkSettings(this.associatedPort,
                                                this.bootstrapServerAddress,
                                                this.joinExistingChordNetwork,
                                                this.numberOfFingers,
                                                this.numberOfSuccessors,
                                                this.chordModule,
                                                this.performBasicLookup);

            return instance ;
        }
    }
}
