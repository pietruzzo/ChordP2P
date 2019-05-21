package com.distributed.chordApp.cooperativemirroring.core;

import com.distributed.chordApp.cooperativemirroring.core.backend.SocketManager;
import com.distributed.chordApp.cooperativemirroring.core.backend.exceptions.SocketManagerException;
import com.distributed.chordApp.cooperativemirroring.core.backend.messages.RequestMessage;
import com.distributed.chordApp.cooperativemirroring.core.backend.messages.ResponseMessage;
import com.distributed.chordApp.cooperativemirroring.core.settings.ChordNetworkSettings;
import com.distributed.chordApp.cooperativemirroring.core.backend.ClientHandlerThread;
import com.distributed.chordApp.cooperativemirroring.core.backend.HostHandlerThread;
import com.distributed.chordApp.cooperativemirroring.core.backend.ResourcesManager;
import com.distributed.chordApp.cooperativemirroring.core.settings.HostSettings;
import com.distributed.chordLib.Chord;
import com.distributed.chordLib.ChordBuilder;

import com.distributed.chordLib.ChordCallback;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Class used for representing a Host of the cooperative mirroring system.
 * Each host is used as Server in case of external requests or Peer in case of internal communication among
 * the hosts of the cooperative mirroring server network.
 *
 * @date 2019-03-27
 * @version 2.0
 */
public class Host implements Runnable, ChordCallback
{
    //Settings for the current host
    private HostSettings hostSettings = null;

    //Entry point of the chord network for the cooperative mirroring application
    private Chord chordEntryPoint = null;
    //Manger of the resources of the current host
    private ResourcesManager resourcesManager = null;
    //Handler for changes in the keyset
    private HostHandlerThread hostHandlerThread = null;

    //Boolean flag used for stopping the server (it can be restarted later )
    private Boolean stopHost = false;
    //Boolean flag used for state if we want to definitly shutdown a server (will definitly close the host)
    private Boolean shutdownHost = false;

    public Host(HostSettings hostSettings, @Nullable HashSet<Resource> resources)
    {
        this.setHostSettings(hostSettings);
        this.setResourceManager(resources);


        this.stopHost = false;
        this.shutdownHost = false ;

        this.hostSettings.verboseInfoLog("\n" + this.toString() , HostSettings.HOST_CALLER,false);
    }

    /**
     * Method used for enjoying a chord network
     */
    public void enjoyChordNetwork()
    {
        this.initChordEntryPoint(this.getHostSettings().getChordNetworkSettings());

        this.initHostHandlerThread(new HostHandlerThread(
                this.getHostSettings(),
                this.chordEntryPoint,
                this.resourcesManager,
                true
        ));
    }

    /**
     * Method used for transfering all the resources of the current host to another host
     * in case of network leaving
     *
     * @throws IOException
     */
    public void leaveChordNetwork() throws SocketManagerException {

        this.hostSettings.verboseInfoLog("leaving the chord network ..." , HostSettings.HOST_CALLER,false);

        if(this.chordEntryPoint == null)
        {
            this.hostSettings.verboseInfoLog("no previously instantiated chord network found" , HostSettings.HOST_CALLER,false);
            return ;
        }

        this.chordEntryPoint.closeNetwork();

        this.hostSettings.verboseInfoLog("chord network leaved, sending the host resources to a shallop host ..." , HostSettings.HOST_CALLER,false);

        if(!this.hostSettings.hasShallopHost())
        {
            this.hostSettings.verboseInfoLog("shallop host not present, resources will be lost" , HostSettings.HOST_CALLER,false);
            return ;
        }

        String shallopHostString = this.hostSettings.getShallopHostIP() + " : " + this.hostSettings.getShallopHostPort() ;

        this.hostSettings.verboseInfoLog("trying to open a channel with the shallop host: " + shallopHostString + " ..." , HostSettings.HOST_CALLER,false);

        //Socket destinationHostSocket = null;
        SocketManager destinationHostSocket = null;

        //destinationHostSocket = new Socket();
        destinationHostSocket = new SocketManager(this.hostSettings.getShallopHostIP(),
                                                  this.hostSettings.getShallopHostPort(),
                                                  this.hostSettings.getConnectionTimeout_MS(),
                                                  this.hostSettings.getConnectionRetries());

        destinationHostSocket.connect();

        this.hostSettings.verboseInfoLog("channel opened with the shallop host: " + shallopHostString , HostSettings.HOST_CALLER,false);


        for(Resource resource : this.resourcesManager.getResources())
        {
            RequestMessage request = new RequestMessage(this.hostSettings.getHostIP(), this.hostSettings.getHostPort(), resource, false, false);

            this.hostSettings.verboseInfoLog("created the exchanging request message for the resource: " + resource.getResourceID() + " to be send to the shallop host: " + shallopHostString , HostSettings.HOST_CALLER,false);
            this.hostSettings.verboseInfoLog("sending the resource: " + resource.getResourceID() + " to the shallop host: " + shallopHostString , HostSettings.HOST_CALLER,false);

            ResponseMessage response = (ResponseMessage) destinationHostSocket.post(request, true);

            this.hostSettings.verboseInfoLog("response for the resource: " + resource.getResourceID() + " arrived from the shallop host: " + shallopHostString , HostSettings.HOST_CALLER,false);

            if(response.getRequestPerformedSuccessfully())
            {
                this.hostSettings.verboseInfoLog("resource: " + resource.getResourceID() + " deposited successfully on the shallop host: " + shallopHostString , HostSettings.HOST_CALLER,false);
            }
            else {
                this.hostSettings.verboseInfoLog("resource: " + resource.getResourceID() + " NOT deposited on the shallop host: " + shallopHostString , HostSettings.HOST_CALLER,false);
            }
        }

        destinationHostSocket.disconnect();
    }

    /*Setter methods*/
    private void setHostSettings(HostSettings hostSettings){this.hostSettings = hostSettings; }
    private void setResourceManager(@Nullable HashSet<Resource> resources){
        this.resourcesManager = ResourcesManager.getInstance();
        if(resources != null) this.resourcesManager.depositResources(resources);
    }

    /*Application settings*/

    /**
     * Method used for instantianting the Handler for the chord callbacks
     * @param hostHandlerThread
     */
    private void initHostHandlerThread(HostHandlerThread hostHandlerThread) {
        this.hostSettings.verboseInfoLog("instantiating a new host handler thread" , HostSettings.HOST_CALLER,false);
        this.hostHandlerThread = hostHandlerThread;

        this.hostSettings.verboseInfoLog("starting the new instantiated host handler thread" , HostSettings.HOST_CALLER,false);
        this.hostHandlerThread.start();

        this.hostSettings.verboseInfoLog("host handler thread started" , HostSettings.HOST_CALLER,false);
    }

    /*
     * Method used for joining or creating a Chord network (depending on the settings of the ChordNetworkSettings passed as parameters)
     */
    private void initChordEntryPoint(ChordNetworkSettings cns)
    {
        Chord cnep = null;

        try
        {
            if(cns.getJoinExistingChordNetwork())
            {
                this.hostSettings.verboseInfoLog("trying to join an existing chord network ..." , HostSettings.HOST_CALLER,false);
                cnep = ChordBuilder.joinChord(cns.getBootstrapServerAddress(), cns.getAssociatedPort(), this);
            }
            else
            {
                this.hostSettings.verboseInfoLog("trying to create a chord network ..." , HostSettings.HOST_CALLER,false);
                cnep = ChordBuilder.createChord(cns.getAssociatedPort(), cns.getNumberOfFingers(), cns.getNumberOfSuccessors(), cns.getChordModule(), this);
            }


        } catch (Exception e)
        {
            this.hostSettings.verboseInfoLog("impossible to join or create a chord network: \n" + e.getMessage() + "\nShutting Down the host.", HostSettings.HOST_CALLER,true);
            //this.finalize();
        }


        if(cns.getJoinExistingChordNetwork())
        {
            this.hostSettings.verboseInfoLog("joined a chord network" , HostSettings.HOST_CALLER,false);
        }
        else {
            this.hostSettings.verboseInfoLog("created a chord network" , HostSettings.HOST_CALLER,false);
        }

        this.chordEntryPoint = cnep;
    }

    @Override
    public void run()
    {
        ServerSocket server = null;
        ThreadPoolExecutor executor = null;

        try {

            this.hostSettings.verboseInfoLog("starting the server socket for the current host" , HostSettings.HOST_CALLER,false);

            server = new ServerSocket(this.getHostSettings().getHostPort().intValue());

            this.hostSettings.verboseInfoLog("server socket started" , HostSettings.HOST_CALLER,false);

        } catch (IOException e)
        {
            this.hostSettings.verboseInfoLog("cannot instantiate a server socket : \n" + e.getMessage() + "\nShutting down the current host\n" , HostSettings.HOST_CALLER,false);
            //this.finalize();
            System.exit(1);
        }

        try
        {
            this.hostSettings.verboseInfoLog("instantiating an executor ..." , HostSettings.HOST_CALLER,false);

            /*We used the CachedThreadPool in order to have only as much thread as we
             *need , also according to the processing power of the machine of the host
             */
            executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

            this.hostSettings.verboseInfoLog("executor instantiated" , HostSettings.HOST_CALLER,false);

            while(!this.shutdownHost)
            {
                while(!this.stopHost)
                {
                    this.hostSettings.verboseInfoLog("waiting for a request ..." , HostSettings.HOST_CALLER,false);

                    Socket client = server.accept();

                    this.hostSettings.verboseInfoLog("request detected , instantiating a client handler thread for managing it ..." , HostSettings.HOST_CALLER,false);

                    ClientHandlerThread cht = new ClientHandlerThread(  this.getHostSettings(),
                                                                        client,
                                                                        this.resourcesManager,
                                                                        this.chordEntryPoint,
                                                                        true);

                    this.hostSettings.verboseInfoLog("client handler thread instantiated , trying to execute it ..." , HostSettings.HOST_CALLER,false);

                    executor.execute(cht);

                    this.hostSettings.verboseInfoLog("executing client handler thread" , HostSettings.HOST_CALLER,false);
                }

            }


        } catch (IOException e) {
            this.hostSettings.verboseInfoLog("unable to accept the request: \n" + e.getMessage() + "\n" , HostSettings.HOST_CALLER,true);
        } catch (SocketManagerException e) {
            this.hostSettings.verboseInfoLog("unable to handle the request: \n" + e.getMessage() + "\n", HostSettings.HOST_CALLER, true);
        } finally
        {
            this.hostSettings.verboseInfoLog("shutting down the executor ..." , HostSettings.HOST_CALLER,false);
            executor.shutdown();
            this.hostSettings.verboseInfoLog("executor shutted down, trying to shutting down the ServerSocket ..." , HostSettings.HOST_CALLER,false);

            try
            {
                server.close();
                this.hostSettings.verboseInfoLog("closed the server socket" , HostSettings.HOST_CALLER,false);
            }
            catch (IOException e)
            {
                this.hostSettings.verboseInfoLog("unable to shutting down the server socket: \n" + e.getMessage() + "\n" , HostSettings.HOST_CALLER,true);
            }

            this.finalize();
        }
    }

    /**
     * Method used for stopping momentanously the host
     * (it could be restarted later)
     * @return Boolean value that represent if the host is in the stop state
     */
    public synchronized Boolean stopHost(Boolean verbose)
    {
        this.hostSettings.verboseInfoLog("stopping the current host (it could be restarted later) ..." , HostSettings.HOST_CALLER,false);

        this.stopHost = true;

        this.hostSettings.verboseInfoLog("current host stopped" , HostSettings.HOST_CALLER,false);

        return this.getHostStopped();
    }

    /**
     * Method used for restarting a host after it has been stopped
     *
     * @return Boolean value that represents if the host is stopped or not
     */
    public synchronized Boolean startHost(Boolean verbose)
    {
        this.hostSettings.verboseInfoLog("restarting the current host ..." , HostSettings.HOST_CALLER,false);

        this.stopHost = false;

        this.hostSettings.verboseInfoLog("current host started" , HostSettings.HOST_CALLER,false);

        return this.getHostStopped();
    }

    /**
     * Method used for permanently shutting down a host
     * (it cannot be restarted anyhow)
     * @return
     */
    public synchronized Boolean shutdownHost(Boolean verbose)
    {
        this.hostSettings.verboseInfoLog("shutting down the current host (it could't be restarted) ..." , HostSettings.HOST_CALLER,false);

        this.shutdownHost = true;

        this.hostSettings.verboseInfoLog("current host shutted down" , HostSettings.HOST_CALLER,false);

        return this.getHostPoweredOff();
    }

    /**
     * Method used for changing a resource placement
     */
    @Override
    public void notifyResponsabilityChange()
    {
        this.hostHandlerThread.notifyResponsabilityChange();
    }

    /*Getter methods*/
    public HostSettings getHostSettings(){return this.hostSettings; }
    public Boolean getHostStopped(){return this.stopHost; }
    public Boolean getHostPoweredOff(){ return this.shutdownHost; }

    @Override
    public String toString(){
        String state = "\n============{ HOST }============\n";

        state += this.getHostSettings().toString();

        state += "\nHost state: " ;

        if(this.getHostStopped()){
            state += "<STOPPED>";
        }
        else{
            state += "<RUNNING>";
        }
        if(this.getHostPoweredOff()){
            state += "\nHost <SHUTTED DOWN>";
        }


        state += "\n\n================================\n";

        return state;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void finalize() {

        this.hostSettings.verboseInfoLog("trying to close the HostHandlerThread ..." , HostSettings.HOST_CALLER,false);

        this.hostHandlerThread.stop();

        this.hostSettings.verboseInfoLog("HostHandlerThread closed" , HostSettings.HOST_CALLER,false);

        try {
            this.leaveChordNetwork();
        } catch (SocketManagerException e) {
            e.printStackTrace();
        }
    }
}
