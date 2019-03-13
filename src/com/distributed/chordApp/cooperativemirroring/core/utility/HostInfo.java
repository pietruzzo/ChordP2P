package com.distributed.chordApp.cooperativemirroring.core.utility;

/**
 * Class used for retriveing some informations about the Host in order to provide a better
 * utilization of resources.
 */

public class HostInfo {

    /**
     * Method used to know how many threads are available on a specific host
     * @return Integer
     */
    public static Integer hostAvailableCores(){
        int cores = 0;

        cores = Runtime.getRuntime().availableProcessors();

        return cores;
    }
}
