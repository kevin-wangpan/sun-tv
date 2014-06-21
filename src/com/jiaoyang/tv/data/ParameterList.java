package com.jiaoyang.tv.data;

import java.util.ArrayList;
import java.util.Collection;

import android.util.Pair;

public class ParameterList extends ArrayList<Pair<String, String>> {

    private static final long serialVersionUID = -1854035053887628136L;

    public ParameterList() {
    }

    public ParameterList(int capacity) {
        super(capacity);
    }

    public ParameterList(Collection<? extends Pair<String, String>> collection) {
        super(collection);
    }

    public void add(String key, String value) {
        add(new Pair<String, String>(key, value));
    }

    public void add(String key, int value) {
        add(new Pair<String, String>(key, Integer.toString(value)));
    }
}
