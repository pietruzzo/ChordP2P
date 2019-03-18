package com.distributed.chordLib.chordCore;

public class HashFunction {

    /**
     * Module
     */
    private int m;

    public HashFunction(int m){
        this.m = m;
    }
    /**
     * calculate SHA-1 of a String
     * @implNote with org.apache.commons.codec.digest.DigestUtils
     * @return digested string
     */
    public String getSHA1(String inputString){return null;}

    /**
     * Return 1 if hash1 > hash2, -1 if hash1 < hash2, 0 otherwise
     *
     * @param hash1
     * @param hash2
     * @return
     */
    public int compare (String hash1, String hash2){
        return 0;
    }

    public int getM() {
        return m;
    }
}
