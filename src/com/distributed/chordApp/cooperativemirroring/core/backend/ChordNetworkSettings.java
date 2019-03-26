package com.distributed.chordApp.cooperativemirroring.core.backend;

import java.io.Serializable;

/**
 * Class used for passing Chord network parameters to a Host in order to allow
 * it to join or create (and use) a chord network
 */
public class ChordNetworkSettings implements Serializable {
    //IP of the host that has the following settings
    private String associatedHostIP = null;
    //Port of the host that has the following settings
    private Integer associatedHostPort = null;
    //Parameter used for not allowing any more changes in parameters
    private Boolean changesLocked = false;

    //Boolean flag used to state if the client handlers have to perform a basic lookup
    private Boolean performBasicLookups = null;

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

    public ChordNetworkSettings(String associatedHostIP,Integer associatedHostPort){
       this.setAssociatedHostIP(associatedHostIP);
       this.setAssociatedHostPort(associatedHostPort);
    }

    /*Setter methods*/
    private void setAssociatedHostIP(String associatedHostIP){this.associatedHostIP = associatedHostIP; }
    private void setAssociatedHostPort(Integer associatedHostPort){ this.associatedHostPort = associatedHostPort; }

    /**
     * Method used to state if the Host will join or create a chord network
     * @param joinExistingChordNetwork
     * @return
     */
    public Boolean setJoinExistingChordNetwork(Boolean joinExistingChordNetwork){
        if(this.changesLocked) return false;
        this.joinExistingChordNetwork = joinExistingChordNetwork;

        return true;
    }

    /**
     * Method used for setting the bootstrap server address for the chord network
     * @param bootstrapServerAddress
     * @return
     */
    public Boolean setBootstrapServerAddress(String bootstrapServerAddress){
        if(this.changesLocked) return false ;
        this.bootstrapServerAddress = bootstrapServerAddress;

        return true;
    }

    /**
     * Method used for setting the number of fingers of each finger table
     * @param numberOfFingers
     * @return
     */
    public Boolean setNumberOfFingers(Integer numberOfFingers){
        if(this.changesLocked) return false ;

        this.numberOfFingers = numberOfFingers;

        return true;
    }

    /**
     * Method used for setting if the client handlers have to perform basic lookups
     * @param performBasicLookups
     * @return
     */
    public Boolean setPerformBasicLookups(Boolean performBasicLookups){
        if(this.changesLocked) return false;

        this.performBasicLookups = performBasicLookups;

        return true;
    }

    /**
     * Method used for setting the number of successors
     * @param numberOfSuccessors
     * @return
     */
    public Boolean setNumberOfSuccessors(Integer numberOfSuccessors){
        if(this.changesLocked) return false;

        this.numberOfSuccessors = numberOfSuccessors;

        return true;
    }

    /**
     * Method used for setting the module
     * @param chordModule
     * @return
     */
    public Boolean setChordModule(Integer chordModule){
        if(this.changesLocked) return false ;

        this.chordModule = chordModule;

        return true;
    }

    /**
     * Method used for locking the setting and not allowing any more changes
     * to the chord settings
     * @return
     */
    public Boolean lockChanges(){
        this.changesLocked = true;

        return changesLocked;
    }

    /*Getter methods*/
    public String getAssociatedHostIP(){ return this.associatedHostIP; }
    public Integer getAssociatedHostPort(){ return this.associatedHostPort; }
    public Boolean getChangesLocked(){ return this.changesLocked; }
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
        if(this.getChangesLocked()) state += "<true>\n";
        else state += "<false>\n";
        state += "\nAssociated Host IP: " + this.getAssociatedHostIP();
        state += "\nAssociated Host Port: " + this.getAssociatedHostPort();
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
}
