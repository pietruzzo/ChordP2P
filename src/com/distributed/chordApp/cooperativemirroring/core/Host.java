package com.distributed.chordApp.cooperativemirroring.core;

import com.distributed.chordApp.cooperativemirroring.core.backend.ChordNetworkSettings;
import com.distributed.chordApp.cooperativemirroring.core.backend.ClientHandlerThread;
import com.distributed.chordApp.cooperativemirroring.core.backend.ResourcesManager;
import com.distributed.chordLib.Chord;
import com.distributed.chordLib.ChordBuilder;
import com.distributed.chordLib.ChordCallback;
import jdk.internal.jline.internal.Nullable;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Class used for representing a Host of the cooperative mirroring system
 */
public class Host implements Runnable, ChordCallback {
    //IP address of the host
    private String hostIP = null;
    //Port of the host used for the cooperative mirroring application
    private Integer hostPort = null;
    //Entry point of the chord network for the cooperative mirroring application
    private Chord chordEntryPoint = null;
    //Manger of the resources of the current host
    private ResourcesManager resourcesManager = null;
    //Settings of this host for the chord network
    private ChordNetworkSettings chordNetworkSettings = null;


    public Host(String hostIP, Integer hostPort,ChordNetworkSettings chordNetworkSettings, @Nullable HashSet<Resource> resources) throws IOException {
        this.setHostIP(hostIP);
        this.setHostPort(hostPort);
        this.setChordNetworkSettings(chordNetworkSettings);
        this.setResourceManager(resources);
        this.initChordEntryPoint(chordNetworkSettings);
    }

    /*Setter methods*/
    private void setHostIP(String hostIP){this.hostIP = hostIP;}
    private void setHostPort(Integer hostPort){ this.hostPort = hostPort;}
    private void setChordNetworkSettings(ChordNetworkSettings chordNetworkSettings){this.chordNetworkSettings = chordNetworkSettings; }
    private void setResourceManager(@Nullable HashSet<Resource> resources){
        this.resourcesManager = ResourcesManager.getInstance();
        if(resources != null) this.resourcesManager.depositResources(resources);
    }

    /*Application settings*/
    private void initChordEntryPoint(ChordNetworkSettings cns) throws IOException {
        Chord cnep = null;

        if(cns.getJoinExistingChordNetwork()) {
            cnep = (Chord) ChordBuilder.joinChord(cns.getBootstrapServerAddress(), cns.getAssociatedHostPort(), this);
        }
        else {
            cnep = (Chord) ChordBuilder.createChord(cns.getAssociatedHostPort(), cns.getNumberOfFingers(), cns.getNumberOfSuccessors(), cns.getChordModule(), this);
        }

        this.chordEntryPoint = cnep;
    }

    @Override
    public void run()
    {
        ServerSocket server = null;
        ThreadPoolExecutor executor = null;

        try
        {
            server = new ServerSocket(this.getHostPort().intValue());

            /*We used the CachedThreadPool in order to have only as much thread as we
             *need , also according to the processing power of the machine of the host
             */
            executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

            do {
                Socket client = server.accept();

                ClientHandlerThread cht = new ClientHandlerThread(  this.getHostIP(),
                                                                    this.getHostPort(),
                                                                    client,
                                                                    this.resourcesManager,
                                                                    this.chordEntryPoint,
                                                                    this.getChordNetworkSettings().getPerformBasicLookups(),
                                                                    true);

                executor.execute(cht);

            }while(true);

        }
        catch(Exception ex)
        {

        }
        finally
        {
            try
            {
                executor.shutdown();
                server.close();
            }
            //Possible execution rised by the server termination
            catch (IOException e)
            {
                e.printStackTrace();

            }
        }
    }

    /*Getter methods*/
    public String getHostIP(){return this.hostIP;}
    public Integer getHostPort(){return this.hostPort;}
    public ChordNetworkSettings getChordNetworkSettings(){return this.chordNetworkSettings;}

    @Override
    public String toString(){
        String state = "\n======{ HOST }======\n";

        state += "\nIP address: " + this.getHostIP();
        state += "\nPort : " + this.getHostPort();
        state += "\nChord network settings: " + this.getChordNetworkSettings().toString();

        return state;
    }

    @Override
    public void notifyResponsabilityChange(String firstKey, String lastKey) {

    }
}
