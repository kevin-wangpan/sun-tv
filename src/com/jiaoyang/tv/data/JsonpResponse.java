package com.jiaoyang.tv.data;

import com.google.gson.annotations.SerializedName;

public class JsonpResponse<T> {

    @SerializedName("rtn")
    public int returnCode;

    public T data;
}
