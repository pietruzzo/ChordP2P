package com.distributed.chordApp.cooperativemirroring.core.model;

//Standard libraries
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//Application libraries
import com.distributed.chordApp.cooperativemirroring.core.model.backend.ClientHandlerThread;
import com.distributed.chordLib.Chord;
import com.distributed.chordLib.ChordBuilder;
import com.distributed.chordApp.cooperativemirroring.interfaces.HostInterface;

/**
 * Class that represents the structure for a Cooperative Mirroring Host
 */

public class Host implements HostInterface {

    //String that represents the IP of the host
    private String IP = "192.168.0.2";
    //Integer that represents the port for the requests
    private Integer port = 9876;
    //Variable that represents the entrypoint for the ChordClient network
    private Chord chordEntryPoint;
    //Socket of the current host
    private ServerSocket serverSocket = null;
    //Set of ProjectsReleases holded by the current host
    private HashSet<Resource> resources ;

    public Host(String IP, Integer port) throws IOException, InterruptedException {
        this.setIP(IP);
        this.setPort(port);
        this.setChordEntryPoint();
        this.setServerSocket();
        this.setResources(new HashSet<>());

        //ExecutorService executor = Executors.newFixedThreadPool(HostInfo.hostAvailableCores());
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        try{

            do{

                Socket clientSocket = null;

                try{

                    //Blocking call, waiting for a new client's connection
                    clientSocket = this.serverSocket.accept();

                    //Creating a new client's handler for managing the resource
                    ClientHandlerThread cht = new ClientHandlerThread(this.getIP(), this.getPort(), clientSocket, this.chordEntryPoint);

                    //Execute in a parallel way the newly created client's handler
                    executor.execute(cht);

                }catch(Exception ex){
                    //In case of failures, I will close the client's connection anyway
                    clientSocket.close();
                    System.err.println(ex.getStackTrace());
                }

            }while(true);

        }finally {
            if(!executor.awaitTermination(800, TimeUnit.MILLISECONDS)){
                executor.shutdownNow();
            }

            while(!executor.isTerminated()){}
        }

        //for(int i = 0; i < HostInfo.hostAvailableCores(); i++){
            /*
             * ResourceManager rm = new ResourceManager(this.chordEntryPoint);
             * executor.execute(rm);
             */
        //}
        /*
         * executor.shutdown();
         *
         * while(!executor.isTerminated()){}
         */
    }

    /*Setters*/
    private void setIP(String IP){ this.IP = IP; }
    private void setPort(Integer port){ this.port = port; }
    private void setResources(HashSet<Resource> resources){ this.resources = resources; }
    private void setServerSocket() throws IOException {
        this.serverSocket = new ServerSocket(this.getPort());
    }

    /**
     * Method used for joining (or creating) a new chord network
     * @throws IOException
     */
    private void setChordEntryPoint() throws IOException {
        Chord ep = (Chord) ChordBuilder.joinChord(this.getIP(), this.getPort(), null);
        if(ep == null) ep = (Chord) ChordBuilder.createChord(this.getPort(), null);

        this.chordEntryPoint = ep;
    }

    /*
     * Method used for retriveing previously saved resources
     */
    private void initResources(){
        //TODO
    }

    /*Getters*/

    @Override
    public synchronized Boolean uploadResource(Resource resource) {
        return null;
    }

    @Override
    public synchronized Boolean downloadResource(String resourceID) {
        return null;
    }

    /**
     * Method that returns the IP address of the Host
     * @return String
     */
    public String getIP(){ return this.IP; }

    /**
     * Method that returns the Port used for the cooperative mirroring requests
     * @return Integer
     */
    public Integer getPort(){ return this.port; }

    /**
     * Method that returns the project releases mantained by a Host
     * @return HashSet<ProjectRelease>
     */
    public synchronized HashSet<Resource> getResources(){ return this.resources; }

    @Override
    public String toString(){
        String stateString = "";

        stateString += "\nIP: " + this.getIP();
        stateString += "\nPort: " + this.getPort();
        stateString += "\nResources: ";
        for(Resource pr : this.getResources()){
            stateString += pr.toString();
        }

        return stateString;
    }
}
