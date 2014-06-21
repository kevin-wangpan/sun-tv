package com.jiaoyang.base.data.local;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jiaoyang.base.app.JiaoyangApplication;
import com.jiaoyang.base.data.local.DbField.DataType;
import com.jiaoyang.base.util.StringEx;
import com.jiaoyang.tv.util.Logger;

public class JiaoyangDatabaseHelper extends SQLiteOpenHelper {
    private static final Logger LOG = Logger.getLogger(JiaoyangDatabaseHelper.class);

    private static final String DATABASE_NAME = "jiaoyang.db";
    private static final int DATABASE_VERSION = 1;

    private static List<Class<? extends BaseInfo>> mTables = new ArrayList<Class<? extends BaseInfo>>();

    private static JiaoyangDatabaseHelper sInstance = null;

    public static void init(Context context) {
        assert (sInstance == null);

        LOG.info("init.");

        registerDatabaseTable(CrashInfo.class);
        sInstance = new JiaoyangDatabaseHelper(context);
    }

    public static void fini() {
        LOG.info("fini.");

        sInstance = null;
    }

    public static JiaoyangDatabaseHelper getInstance() {
        return sInstance;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        LOG.debug("onCreate.");

        JiaoyangApplication.sFirstTimeLaunche = true;
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    private static void registerDatabaseTable(Class<? extends BaseInfo> tableClass) {
        LOG.info("register {}.", tableClass.getName());

        if (!(mTables.contains(tableClass))) {
            mTables.add(tableClass);
        }
    }

    private JiaoyangDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        LOG.debug("construction. database.version={}", DATABASE_VERSION);
    }

    private void dropTable(SQLiteDatabase db, Class<? extends BaseInfo> tableClass) {
        dropTable(db, BaseInfo.getTableName(tableClass));
    }

    private void dropTable(SQLiteDatabase db, String tableName) {
        db.execSQL("DROP TABLE IF EXISTS " + tableName);
    }

    private void createTable(SQLiteDatabase db, Class<? extends BaseInfo> tableClass) {
        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append("CREATE TABLE IF NOT EXISTS " + BaseInfo.getTableName(tableClass) + " ( ");
        Field[] fields = tableClass.getFields();

        if (fields != null && fields.length > 0) {
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                if (field != null) {
                    DbField annoation = field.getAnnotation(DbField.class);
                    if (annoation != null) {
                        String fieldName = annoation.name();
                        DataType tableType = annoation.type();
                        if (StringEx.isNullOrEmpty(fieldName)) {
                            fieldName = field.getName();
                        }
                        sqlBuffer.append("`" + fieldName + "` " + tableType.toString() + " ");
                        if (!annoation.isNull()) {
                            sqlBuffer.append("NOT NULL ");
                        }
                        if (annoation.isPrimaryKey()) {
                            sqlBuffer.append("PRIMARY KEY ");
                        }
                        if (annoation.isAutoIncrement()) {
                            sqlBuffer.append("autoincrement");
                        }
                        sqlBuffer.append(",");
                    }
                }
            }
        }
        // 删除最后一个逗号,
        sqlBuffer.deleteCharAt(sqlBuffer.length() - 1);

        sqlBuffer.append(" )");
        String sql = sqlBuffer.toString();
        LOG.info("create table. sql={}", sql);
        db.execSQL(sql);
    }

    private void createTables(SQLiteDatabase db) {
        for (Class<? extends BaseInfo> tableClass : mTables) {
            createTable(db, tableClass);
        }
    }
    
}
