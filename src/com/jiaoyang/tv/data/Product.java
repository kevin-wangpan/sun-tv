package com.jiaoyang.tv.data;

import com.google.gson.annotations.SerializedName;

/**
 * 购买的影片信息
 */
public class Product {
    @SerializedName("productId")
    public int id;   // ID
    public String title;    // 名称
    public int validTime;   // 有效期
    
    public String expireTime;   // 到期时间
    public int onSale;  // 0-已下架
    public int preSale; // 1-预售
    public String saleTime; // 上线日期
    public boolean isViewed;    // 1-已经观看 0-未观看
}
