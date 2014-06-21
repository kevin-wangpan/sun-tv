package com.jiaoyang.tv.update;

public class State {
    public static final int PENDING = 0;
    public static final int START = PENDING + 1;
    public static final int STOP = START + 1;
    public int state = START;
}
