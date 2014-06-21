package com.jiaoyang.tv.data;

import com.jiaoyang.tv.data.VipState.Data;

public class VipState extends JsonpResponse<Data> {

    public class Data {
        public String curDate; // 当前时间
        public Beryl beryl;// 手机包月（豪华套餐）
    }
}
