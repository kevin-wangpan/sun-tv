package com.jiaoyang.base.data.local;

import java.util.Locale;

import com.jiaoyang.base.data.local.DbField.DataType;

public abstract class BaseInfo {

    @DbField(name = "_id", type = DataType.INTEGER, isPrimaryKey = true, isAutoIncrement = true)
    public long id;

    @DbField(name = "created_at", type = DataType.INTEGER, isNull = false)
    public long createdAt;

    @DbField(name = "updated_at", type = DataType.INTEGER, isNull = false)
    public long updatedAt;

    public static String getTableName(Class<?> clazz) {
        String name = clazz.getSimpleName();
        name = name.replaceAll("(?:(?<=[a-z])(?=[A-Z]))|(?:(?<=\\w)(?=[A-Z][a-z]))", "_");

        return name.toLowerCase(Locale.US) + "s";
    }

    public BaseInfo() {
        id = -1;
    }

    public boolean isNewRecord() {
        return id == -1;
    }

    @Override
    public boolean equals(Object o) {
        BaseInfo info = (BaseInfo) o;

        return info != null && info.id == id;
    }
}
