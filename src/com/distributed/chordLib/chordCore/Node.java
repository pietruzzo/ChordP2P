package com.distributed.chordLib.chordCore;


import java.io.Serializable;

public class Node implements Serializable {

    private String IP;


    public Node(String IP) {
        this.IP = IP;
    }

    String getIP(){return IP;}

    /**
     * @return the hash for the IP
     */
    String getkey(){return null;}
}
