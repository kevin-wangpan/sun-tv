package com.jiaoyang.base.data.local;

import java.util.List;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class BaseRecordDao<T extends BaseRecord> extends BaseDao<T> {

    private Class<T> mClazz;
    public BaseRecordDao(Context context, Class<T> clazz) {
        super(context, clazz);
        mClazz = clazz;
    }

    public BaseRecordDao(Context context, Class<T> clazz, int maxRowCount) {
        super(context, clazz, maxRowCount);
        mClazz = clazz;
    }

    public boolean exist(int movieId, boolean isOnline) {
        return findByMovieId(movieId, isOnline) != null;
    }


    public T getBaseRecord(int movieId, boolean isOnline) {
        T record = findByMovieId(movieId, isOnline);
        if (record == null) {
            try {
                record = mClazz.newInstance();
                record.movieid = movieId;
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return record;
    }


    public T getBaseRecord(int movieId, int index, boolean isOnline) {
        T record = getBaseRecord(movieId, isOnline);

        if (record.isNewRecord()) {
            record.index = index;
        } else if (record.index != index) {
            record.index = index;
            record.movietiming = 0;
        }

        return record;
    }

    public T getBaseRecord(int movieId, int index, int partIndex, boolean isOnline) {
        T record = getBaseRecord(movieId, index, isOnline);

        if (record.isNewRecord()) {
            record.partindex = partIndex;
        } else if (record.partindex != partIndex) {
            record.partindex = partIndex;
            record.movietiming = 0;
        }

        return record;
    }

    public T save(T record) {
        return save(record, null);
    }

    public void deleteByMovieId(long movieId, boolean isOnline) {
        deleteBy(new String[]{BaseRecord.COLUMN_MOVIE_ID, BaseRecord.COLUMN_IS_ONLINE},
                new String[]{String.valueOf(movieId), isOnline ? "1" : "0"});
    }

    public void deleteAllOnline() {
        deleteBy(BaseRecord.COLUMN_IS_ONLINE, String.valueOf(1));
    }

    public void deleteAllLocal() {
        deleteBy(BaseRecord.COLUMN_IS_ONLINE, String.valueOf(0));
    }

    public T findByMovieId(int movieId , boolean isOnline) {
        return findBy(new String[]{BaseRecord.COLUMN_MOVIE_ID, BaseRecord.COLUMN_IS_ONLINE},
                new String[]{Integer.toString(movieId), isOnline ? "1" : "0"});
    }

    public T save(T record, SQLiteDatabase db) {
        if (record.isNewRecord()) {
            record.id = insert(record, db);
        } else {
            update(record);
        }

        return record;
    }

    public List<T> getAllRecordsOrdered(boolean online) {
        String sort = null;
        return find(BaseRecord.COLUMN_IS_ONLINE + "=?",
                new String[]{online ? "1" : "0"}, null, null,
                sort == null ? null : "`" + sort + "` DESC");
    }

}
