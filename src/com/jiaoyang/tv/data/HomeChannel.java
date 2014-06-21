package com.jiaoyang.tv.data;

import java.io.Serializable;

import android.text.TextUtils;

public class HomeChannel implements Serializable {
    private static final long serialVersionUID = 9154486533142743992L;
    public int index;
    public String layoutType;//0布局为一个单元格，1布局为横向占两个单元格
    public String title;
    public String icon;
    public String poster;
    public String kktv_icon;
    public String url;
    
    public String getChannelType() {
        return parseChannelTypeFromUrl(url);
    }
    
    private String parseChannelTypeFromUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        String [] infos = url.split("/");
        if (infos == null || infos.length == 0) {
            return null;
        }
        for (int i = 0; i < infos.length; i++) {
            if (infos[i].startsWith("type")) {
                return infos[i].split(",")[1];
            }
        }
        return null;
    }
}
