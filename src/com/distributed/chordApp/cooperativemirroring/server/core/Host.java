package com.distributed.chordApp.cooperativemirroring.server.core;

import com.distributed.chordApp.cooperativemirroring.common.Resource;
import com.distributed.chordApp.cooperativemirroring.common.utilities.SocketManager;
import com.distributed.chordApp.cooperativemirroring.common.utilities.exceptions.SocketManagerException;
import com.distributed.chordApp.cooperativemirroring.common.messages.RequestMessage;
import com.distributed.chordApp.cooperativemirroring.common.messages.ResponseMessage;
import com.distributed.chordApp.cooperativemirroring.server.core.settings.ChordNetworkSettings;
import com.distributed.chordApp.cooperativemirroring.server.core.managers.ClientHandlerThread;
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

        if(this.resourcesManager.getResources().isEmpty()){
            this.hostSettings.verboseInfoLog("chord network leaved" , HostSettings.HOST_CALLER,false);
            return ;
        }

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

            if(response.getRequestPerformedSuccessfully()) {
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
     * Method used for permanently shutting down a host
     * (it cannot be restarted anyhow)
     * @return
     */
    public synchronized void shutdownHost() throws IOException {
        if(this.hostShuttedDown){
            return ;
        }

        this.hostSettings.verboseInfoLog("shutting down the current host (it can't be restarted) ..." , HostSettings.HOST_CALLER,false);
        this.hostShuttedDown = true;

        this.serverSocket.close();
    }

    @Override
    public void notifyResponsabilityChange() {
        Boolean thisHost = false ;

        this.hostSettings.verboseInfoLog("notified of responsability changes: verify the current host resources's ownership" , HostSettings.HOST_CALLER,false);

        if(this.resourcesManager.getResources().isEmpty()){
            this.hostSettings.verboseInfoLog("no resource stored on the current host" , HostSettings.HOST_CALLER,false);
        }

        for(Resource resource : this.resourcesManager.getResources()) {
            String destinationAddress = null;
            thisHost = false ;

            this.hostSettings.verboseInfoLog("verifying ownership of resource: " + resource.getResourceID() +" ..." , HostSettings.HOST_CALLER,false);

            if(this.getHostSettings().getChordNetworkSettings().getPerformBasicLookups()) {
                destinationAddress = this.chordEntryPoint.lookupKeyBasic(resource.getResourceID());
            }
            else{
                destinationAddress = this.chordEntryPoint.lookupKey(resource.getResourceID());
            }

            if(destinationAddress.equals(this.getHostSettings().getHostIP()) || destinationAddress.equals("127.0.0.1") || destinationAddress.equals("127.0.1.1")) {
                thisHost = true;
            }

            if(!thisHost) {
                this.hostSettings.verboseInfoLog("need to exchange the resource: " + resource.getResourceID() +" with host: " + destinationAddress , HostSettings.HOST_CALLER,false);

                RequestMessage request = new RequestMessage(
                        this.getHostSettings().getHostIP(),
                        this.getHostSettings().getHostPort(),
                        resource);
                request.setHostDepositRequest(true);

                this.hostSettings.verboseInfoLog("sending request for resource: " + resource.getResourceID() +" to host: " + destinationAddress +" ..." , HostSettings.HOST_CALLER,false);
                this.hostSettings.verboseInfoLog("opening a communication channel with host: " + destinationAddress + " ...", HostSettings.HOST_CALLER,false);

                try {
                    SocketManager destinationSocket = new SocketManager(destinationAddress, this.hostSettings.getHostPort(), SocketManager.DEFAULT_CONNECTION_TIMEOUT_MS, SocketManager.DEFAULT_CONNECTION_RETRIES);
                    destinationSocket.connect();

                    this.hostSettings.verboseInfoLog("communication channel with host: " + destinationAddress + " opened sending the resource: " + resource.getResourceID() + " ..." , HostSettings.HOST_CALLER,false);

                    destinationSocket.post(request);

                    this.hostSettings.verboseInfoLog("request send for resource: " + resource.getResourceID() + " to host: " + destinationAddress + " waiting for response ...", HostSettings.HOST_CALLER,false);

                    ResponseMessage responseMessage = (ResponseMessage) destinationSocket.get();

                    this.hostSettings.verboseInfoLog("response for resource: " + resource.getResourceID() + " arrived from host: " + destinationAddress  ,HostSettings.HOST_CALLER, false);
                    this.hostSettings.verboseInfoLog("response: " + responseMessage.conciseToString(), HostSettings.HOST_CALLER, false);
                    this.hostSettings.verboseInfoLog("disconnecting from destination host ...", HostSettings.HOST_CALLER,false);

                    destinationSocket.disconnect();

                    this.hostSettings.verboseInfoLog("disconnected from host: " + destinationAddress, HostSettings.HOST_CALLER,false);

                    this.resourcesManager.removeResource(resource.getResourceID());

                }catch (SocketManagerException e) {
                    this.hostSettings.verboseInfoLog("Exception rised for resource: " + resource.getResourceID() +" \n" + e.getMessage() + "\n keeping the resource on this host" , HostSettings.HOST_CALLER,true);
                }
            }
            else
            {
                this.hostSettings.verboseInfoLog("the resource: " + resource.getResourceID() +" remains on this host" , HostSettings.HOST_CALLER,false);
            }
        }

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
    public void finalize() { ;

        try {
            this.leaveChordNetwork();
        } catch (SocketManagerException e) {
            e.printStackTrace();
        }

        this.hostSettings.finalize();

    }
}