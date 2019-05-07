package com.distributed.chordLib.chordCore;


import java.io.Serializable;

public class Node implements Serializable {

    private final String IP;
    private final HashFunction.Hash key;

    public Node(String IP, HashFunction.Hash key) {
        this.IP = IP;
        this.key = key;
    }

    public String getIP(){return IP;}

    /**
     * @return the hash for the IP
     */
    public HashFunction.Hash getkey(){return key;}

    /**
     * Find if nodes are equals
     * @param node
     * @return comparison response over node's key
     */
    @Override
    public boolean equals(Object node){

        if (node instanceof Node) {
            String nodeIP = ((Node) node).getIP();
            HashFunction.Hash nodeHash = ((Node) node).getkey();

            if (this.IP.equals(nodeIP) && this.key.compareTo(nodeHash) == 0) {
                return true;
            }
        }
        return false;
    }
}
