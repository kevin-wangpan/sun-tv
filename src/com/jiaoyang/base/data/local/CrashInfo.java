package com.jiaoyang.base.data.local;

import com.jiaoyang.base.data.local.DbField.DataType;

public class CrashInfo extends BaseInfo {
    // 日志的所在路径
    @DbField(name = "", type = DataType.TEXT, isNull = true)
    public String log = "";
    
    @DbField
    public String version = "";
    
    @DbField
    public String time = "";
    
    
}
