package com.distributed.chordApp.cooperativemirroring.app;

/**
 * Class used for instntiating clients for the cooperative mirroring system
 *
 * @date 2019-03-27
 * @version 1.0
 */
public class Client {
    //IP of the client
    private String clientIP = null;
    //Port of the client
    private Integer clientPort = null;
    //Boolean flag used for turn-on the verbose mode for the current client
    private Boolean verbose = null;

    public Client(String clientIP,Integer clientPort,Boolean verbose)
    {

    }

    /*Setters*/
    private void setClientIP(String clientIP){this.clientIP = clientIP; }
    private void setClientPort(Integer clientPort){this.clientPort = clientPort; }
    private void setVerbose(Boolean verbose){this.verbose = verbose; }

    /*Application methods*/

    private void menu()
    {
        Boolean goAhead = true;

        do {
            System.out.println("\n======{Client Menu}=====");
            System.out.println("1)Retrieve a resource");
            System.out.println("2)Deposit a resource");
        }while(goAhead);

    }

    /*Getters*/
    public String getClientIP(){return this.clientIP;}
    public Integer getClientPort(){return this.clientPort;}
    public Boolean getVerbose(){return this.verbose;}

    @Override
    public String toString()
    {
        String state = "\n======{CLIENT}======\n";

        state += "\nIP: " + this.getClientIP();
        state += "\nPort: " + this.getClientPort();
        if(this.getVerbose()) state += "\nverbose mode";
        else state += "\nsilent mode";

        return state ;
    }
}
