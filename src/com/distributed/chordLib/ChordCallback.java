package com.distributed.chordLib;

/**
 * Interface that can optionally be implemented by application using ChordLIB that contains callback methods
 * Asynchronous
 */
public interface ChordCallback {

    /**
     * Notify change in set of Keys the application is responsible for
     * @Asynchronous_call
     */
    void notifyResponsabilityChange (String firstKey, String lastKey);
}
