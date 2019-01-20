package com.distributed.chordLib.chordCore;

import javafx.util.Pair;

public class Node extends Pair<String, String> {
    /**
     * Creates a new pair
     *
     * @param key   The key for this pair
     * @param value The value to use for this pair
     */
    public Node(String key, String value) {
        super(key, value);
    }
}
