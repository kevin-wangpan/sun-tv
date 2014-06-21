package com.jiaoyang.tv.data;

import java.io.Serializable;

public class HomeChannelList implements Serializable {
    private static final long serialVersionUID = 8542460615547534199L;

    public String apiVersion;

    public HomeChannel[] data;
    public Error error;
}
