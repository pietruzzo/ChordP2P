package com.distributed.chordApp.cooperativemirroring.server.core;

import com.distributed.chordApp.cooperativemirroring.common.Resource;
import com.distributed.chordApp.cooperativemirroring.common.utilities.SocketManager;
import com.distributed.chordApp.cooperativemirroring.common.utilities.exceptions.SocketManagerException;
import com.distributed.chordApp.cooperativemirroring.common.messages.RequestMessage;
import com.distributed.chordApp.cooperativemirroring.common.messages.ResponseMessage;
import com.distributed.chordApp.cooperativemirroring.server.core.settings.ChordNetworkSettings;
import com.distributed.chordApp.cooperativemirroring.server.core.managers.ClientHandlerThread;
import com.distributed.chordApp.cooperativemirroring.server.core.managers.HostHandlerThread;
import com.distributed.chordApp.cooperativemirroring.server.core.managers.ResourcesManager;
import com.distributed.chordApp.cooperativemirroring.server.core.settings.HostSettings;
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
    //Reference to the server socket of the current host
    private ServerSocket serverSocket = null;
    //Boolean flag used to state if we have to shut down the current server
    private boolean hostShuttedDown = false;

    public Host(HostSettings hostSettings, @Nullable HashSet<Resource> resources) {
        this.setHostSettings(hostSettings);
        this.setResourceManager(resources);

        this.hostSettings.verboseInfoLog("\n" + this.toString() , HostSettings.HOST_CALLER,false);
    }

    /**
     * Method used for enjoying a chord network
     */
    public void joinChordNetwork() throws IOException {

        this.initChordEntryPoint(this.getHostSettings().getChordNetworkSettings());

        this.initHostHandlerThread(new HostHandlerThread(
                this.getHostSettings(),
                this.chordEntryPoint,
                this.resourcesManager));
    }

    /**
     * Method used for transfering all the resources of the current host to another host
     * in case of network leaving
     *
     * @throws IOException
     */
    public void leaveChordNetwork() throws SocketManagerException {

        this.hostSettings.verboseInfoLog("leaving the chord network ..." , HostSettings.HOST_CALLER,false);

        if(this.chordEntryPoint == null) {
            this.hostSettings.verboseInfoLog("no previously instantiated chord network found" , HostSettings.HOST_CALLER,false);
            return ;
        }

        this.chordEntryPoint.closeNetwork();

        this.hostSettings.verboseInfoLog("chord network leaved, sending the host resources to a shallop host ..." , HostSettings.HOST_CALLER,false);

        if(!this.hostSettings.hasShallopHost()) {
            this.hostSettings.verboseInfoLog("shallop host not present, resources will be lost" , HostSettings.HOST_CALLER,false);
            return ;
        }

        String shallopHostString = this.hostSettings.getShallopHostIP() + " : " + this.hostSettings.getShallopHostPort() ;

        this.hostSettings.verboseInfoLog("trying to open a channel with the shallop host: " + shallopHostString + " ..." , HostSettings.HOST_CALLER,false);

        //Socket destinationHostSocket = null;
        SocketManager destinationHostSocket = null;

        destinationHostSocket = new SocketManager(this.hostSettings.getShallopHostIP(),
                this.hostSettings.getShallopHostPort(),
                SocketManager.DEFAULT_CONNECTION_TIMEOUT_MS,
                SocketManager.DEFAULT_CONNECTION_RETRIES);

        destinationHostSocket.connect();

        this.hostSettings.verboseInfoLog("channel opened with the shallop host: " + shallopHostString , HostSettings.HOST_CALLER,false);


        for(Resource resource : this.resourcesManager.getResources())
        {
            RequestMessage request = new RequestMessage(this.hostSettings.getHostIP(), this.hostSettings.getHostPort(), resource);

            this.hostSettings.verboseInfoLog("created the exchanging request message for the resource: " + resource.getResourceID() + " to be send to the shallop host: " + shallopHostString , HostSettings.HOST_CALLER,false);
            this.hostSettings.verboseInfoLog("sending the resource: " + resource.getResourceID() + " to the shallop host: " + shallopHostString , HostSettings.HOST_CALLER,false);

            destinationHostSocket.post(request);

            ResponseMessage response = (ResponseMessage) destinationHostSocket.get();

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
    private void initChordEntryPoint(ChordNetworkSettings cns) throws IOException {
        Chord cnep = null;

        if(cns.getJoinExistingChordNetwork())
        {
            this.hostSettings.verboseInfoLog("trying to join an existing chord network ..." , HostSettings.HOST_CALLER,false);
            cnep = ChordBuilder.joinChord(cns.getBootstrapServerAddress(), cns.getAssociatedPort(), this);
            this.hostSettings.verboseInfoLog("joined a chord network" , HostSettings.HOST_CALLER,false);
        }else{
            this.hostSettings.verboseInfoLog("trying to create a chord network ..." , HostSettings.HOST_CALLER,false);
            cnep = ChordBuilder.createChord(cns.getAssociatedPort(), cns.getNumberOfFingers(), cns.getNumberOfSuccessors(), cns.getChordModule(), this);
            this.hostSettings.verboseInfoLog("created a chord network" , HostSettings.HOST_CALLER,false);
        }

        this.chordEntryPoint = cnep;
    }

    @Override
    public void run()
    {
        ThreadPoolExecutor executor = null;

        try {

            this.hostSettings.verboseInfoLog("starting the server socket for the current host" , HostSettings.HOST_CALLER,false);
            this.serverSocket = new ServerSocket(this.getHostSettings().getHostPort().intValue());
            this.hostSettings.verboseInfoLog("server socket started" , HostSettings.HOST_CALLER,false);

        } catch (IOException e) {
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

            while(!this.hostShuttedDown) {

                    this.hostSettings.verboseInfoLog("waiting for a request ..." , HostSettings.HOST_CALLER,false);

                    Socket client = this.serverSocket.accept();

                    this.hostSettings.verboseInfoLog("request detected , instantiating a client handler thread for managing it ..." , HostSettings.HOST_CALLER,false);

                    ClientHandlerThread cht = new ClientHandlerThread( this.getHostSettings(),
                            client,
                            this.resourcesManager,
                            this.chordEntryPoint);

                    this.hostSettings.verboseInfoLog("client handler thread instantiated , trying to execute it ..." , HostSettings.HOST_CALLER,false);

                    executor.execute(cht);

                    this.hostSettings.verboseInfoLog("executing client handler thread" , HostSettings.HOST_CALLER,false);
            }

            this.hostSettings.verboseInfoLog("shutting down the host" , HostSettings.HOST_CALLER,true);


        } catch (IOException e) {
            this.hostSettings.verboseInfoLog("unable to accept the request: \n" + e.getMessage() + "\n" , HostSettings.HOST_CALLER,true);
        } catch (SocketManagerException e) {
            this.hostSettings.verboseInfoLog("unable to handle the request: \n" + e.getMessage() + "\n", HostSettings.HOST_CALLER, true);
        } finally {
            this.hostSettings.verboseInfoLog("shutting down the executor ..." , HostSettings.HOST_CALLER,false);
            executor.shutdown();
            this.hostSettings.verboseInfoLog("executor shutted down, trying to shutting down the ServerSocket ..." , HostSettings.HOST_CALLER,false);

            try {
                this.serverSocket.close();
                this.hostSettings.verboseInfoLog("closed the server socket" , HostSettings.HOST_CALLER,false);
            }
            catch (IOException e) {
                this.hostSettings.verboseInfoLog("unable to shutting down the server socket: \n" + e.getMessage() + "\n" , HostSettings.HOST_CALLER,true);
            }

            this.finalize();
        }
    }

    /**
     * Method used for exiting the blocking condition of the server socket
     */
    private synchronized void snakeOut() throws SocketManagerException {
        this.hostSettings.mute();
        //Sendinding a false request to the server in order to not block it
        SocketManager socket = new SocketManager(this.hostSettings.getHostIP(), this.hostSettings.getHostPort(), SocketManager.DEFAULT_CONNECTION_TIMEOUT_MS, SocketManager.DEFAULT_CONNECTION_RETRIES);
        socket.connect();
        RequestMessage closingRequest = new RequestMessage(this.hostSettings.getHostIP(), this.hostSettings.getHostPort(), Integer.toString(Integer.MIN_VALUE));
        socket.post(closingRequest);
        socket.get();
        socket.disconnect();
        this.hostSettings.talkative();
    }

    /**
     * Method used for permanently shutting down a host
     * (it cannot be restarted anyhow)
     * @return
     */
    public synchronized void shutdownHost() throws SocketManagerException {
        if(this.hostShuttedDown){
            return ;
        }

        this.hostSettings.verboseInfoLog("shutting down the current host (it can't be restarted) ..." , HostSettings.HOST_CALLER,false);
        this.hostShuttedDown = true;
        this.snakeOut();
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
    public boolean isHostShuttedDown(){return this.hostShuttedDown;}

    @Override
    public String toString(){
        String state = "\n============{ HOST }============\n";

        if(this.hostShuttedDown){
            state += "\n<HOST OFF>\n";
        }

        state += this.getHostSettings().toString();


        state += "\n\n==============================\n";

        return state;
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void finalize() {

        this.hostHandlerThread.interrupt();

        try {
            this.leaveChordNetwork();
        } catch (SocketManagerException e) {
            e.printStackTrace();
        }

        this.hostSettings.finalize();

    }
}