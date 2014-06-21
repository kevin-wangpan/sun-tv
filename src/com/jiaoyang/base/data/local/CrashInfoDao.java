package com.jiaoyang.base.data.local;

import android.content.Context;

public class CrashInfoDao extends BaseDao<CrashInfo> {

    public CrashInfoDao(Context context){
        super(context, CrashInfo.class);
    }
}
