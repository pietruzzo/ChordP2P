package com.distributed.chordLib.chordCore;

import com.google.common.collect.Lists;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;


public class HashFunction {

    /**
     * Module in bits
     */
    private int m;

    MessageDigest md;

    public HashFunction(int m){
        this.m = m;
        try {
            this.md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    /**
     * calculate SHA-1 of a String
     * @implNote with org.apache.commons.codec.digest.DigestUtils
     * @return digested string
     */
    public Hash getSHA1(String inputString){
        byte[] digest = md.digest(inputString.getBytes());
        Hash h = new Hash(digest, m);
        System.out.println("Hash for " + inputString + " is: " + h.toString());
        return h;
    }


    /**
     * 2 < 1 && 3 > 2 || 1 > 3 && 3 > 2 || 2 > 1 && 1 > 3
     * @return true if hash2 is inside range [in module] (hash1, hash3) extremes excluded
     */
    public boolean areOrdered(Hash hash1, Hash hash2, Hash hash3){

        if (hash2.compareTo(hash1) == 1 && (hash3.compareTo(hash2) == 1 || hash1.compareTo(hash3) == 1)) return true;
        if (hash1.compareTo(hash3) == 1 && hash3.compareTo(hash2) == 1) return true;
        return false;
    }

    /**
     * Sum given num to hash and perform module operation
     * @return new hash
     */
    public Hash moduloSum(Hash hash, long num){
        boolean[] digest = hash.getDigest();
        List<Boolean> result = new ArrayList<>();
        List<Boolean> binaryNum = new ArrayList<>();

        //Tranform int into binary rep
        while (num == 0){
            int bit = Math.floorMod(num, 2);
            if (bit == 1) binaryNum.add(true);
            else binaryNum.add(false);
            num = Math.floorDiv(num, 2);
        }
        binaryNum = Lists.reverse(binaryNum);

        //Sum bit a bit with rest
        int rest = 0, first = 0, second = 0;
        int size = Math.max(binaryNum.size(), digest.length);
        for (int i = 1; i <= size; i++) {
            if (size-i < digest.length)  first = 1;
            else first = 0;
            if (size-i < binaryNum.size()) second = 1;
            else second = 0;

            //bit sum logic
            int localSum = rest + first + second;
            if (localSum > 0){
                rest = localSum - 1;
                result.add(true);
            } else if (localSum == 0){
                result.add(false);
            }
        }
        while (rest != 0){
            result.add(true);
            rest = rest - 1;
        }

        //TIf result exceedes ring, MODULO
        if (result.size() > m) {
            result = result.subList(0, m);
        }

        result = Lists.reverse(result);



        //Boolean -> boolean
        boolean newDigest[] = new boolean[result.size()];
        for (int i = 0; i < result.size(); i++) {
            newDigest[i] = result.get(i).booleanValue();
        }


        //return
        return new Hash(newDigest);
    }

    public int getM() {
        return m;
    }
    
    static public class Hash implements Serializable {

        private boolean[] digest;

        public Hash(byte[] digest, int length){

            if (length> digest.length*8) length = digest.length*8; //if length exceeds digest dimension, limit length

            this.digest = new boolean[length];
            for (int i = 0; i < digest.length; i++) {
                int byteIndex = digest.length - i - 1;

                boolean[] currByte = byteToBooleans(digest[byteIndex]);
                for (int j = 0; j < currByte.length; j++) {
                    int bitIndex = this.digest.length - j -1;

                    if ((i+i)*8 + j > length) return;
                    else this.digest[length- (i+1) * 8 + j] = currByte[j];
                }
            }

        }


        public Hash(boolean[] newDigest) {
            this.digest = newDigest;
        }

        public boolean[] getDigest() {
            return digest;
        }

        /**
         * @param hash
         * @return 1 if this.digest > hash.digest, -1 if this.digest < hash.digest, 0 otherwise
         */
        public int compareTo(Hash hash){

            boolean[] otherDigest = hash.getDigest();

            int maxLength = Math.max(this.digest.length, otherDigest.length);
            int minLength = Math.min(this.digest.length, otherDigest.length);

            if (minLength!=maxLength) { //one vector is longer than another one

                if (otherDigest.length == maxLength) {
                    for (int i = 0; i < maxLength - minLength; i++) {
                        if (otherDigest[i]) return -1;
                    }
                } else {
                    for (int i = 0; i < maxLength - minLength; i++) {
                        if (digest[i]) return 1;
                    }
                }
            }

            for (int i = 0; i < minLength; i++) {
                boolean thisElement = digest[digest.length-minLength+i];
                boolean otherElement= otherDigest[otherDigest.length-minLength+i];
                if (thisElement && !otherElement) return 1;
                else if (!thisElement && otherElement) return -1;
            }
            return 0; //equal digests
        }


        @Override
        public String toString() {
            String res = "";
            for (int i = 0; i < this.digest.length; i++) {
                if (digest[i]) res = res + "1";
                else res = res + "0";
            }
            return res;
        }

        /**
         * Convert a byte to array of bits (length of array is 8)
         * @param e
         * @return
         */
        private boolean[] byteToBooleans (byte e) {

            boolean[] result = new boolean[8];

            String s = String.format("%8s", Integer.toBinaryString((e & 0xFF))).replace(' ', '0');

            for (int i = 0; i < result.length; i++) {
                if (s.charAt(i) == '1') result[i] = true;
            }
            return result;
        }
    }

}
