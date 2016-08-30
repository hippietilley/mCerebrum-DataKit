package org.md2k.datakit.logger;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.datatype.DataTypeLong;
import org.md2k.datakitapi.datatype.RowObject;
import org.md2k.datakitapi.status.Status;
import org.md2k.utilities.Report.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/*
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
 * - Timothy W. Hnat <twhnat@memphis.edu>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

public class DatabaseTable_Data {

    private static final String TAG = DatabaseTable_Data.class.getSimpleName();
    public static String TABLE_NAME = "data";
    private static String C_ID = "_id";
    private static String C_CLOUD_SYNC_BIT = "cc_sync";
    private static String C_DATETIME = "datetime";
    private static String C_SAMPLE = "sample";
    private static String C_DATASOURCE_ID = "datasource_id";
    private static final int CVALUE_LIMIT = 250;
    private static final int HFVALUE_LIMIT = 5000;

    private static final String SQL_CREATE_DATA_INDEX = "CREATE INDEX IF NOT EXISTS index_datasource_id on " + TABLE_NAME + " (" + C_DATASOURCE_ID + ");";
    private static final String SQL_CREATE_CC_INDEX = "CREATE INDEX IF NOT EXISTS index_cc_datasource_id on " + TABLE_NAME + " (" + C_DATASOURCE_ID + ", " + C_CLOUD_SYNC_BIT + ");";

    private static final String SQL_CREATE_DATA = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
            C_ID + " INTEGER PRIMARY KEY autoincrement, " +
            C_DATASOURCE_ID + " TEXT not null, " +
            C_CLOUD_SYNC_BIT + " INTEGER DEFAULT 0, " +
            C_DATETIME + " LONG, " +
            C_SAMPLE + " BLOB not null);";

    private static String C_COUNT = "c";
    private static final long WAITTIME = 5 * 1000L; // 5 second;
    private ContentValues[] cValues = new ContentValues[CVALUE_LIMIT];
    private ContentValues[] hfValues = new ContentValues[HFVALUE_LIMIT];
    private int cValueCount = 0;
    private int hfValueCount = 0;
    long lastUnlock = 0;
    Kryo kryo;

    private gzipLogger gzLogger;

    DatabaseTable_Data(SQLiteDatabase db, gzipLogger gzl) {
        kryo = new Kryo();
        createIfNotExists(db);

        gzLogger = gzl;
    }

    public synchronized void removeAll(SQLiteDatabase db) {
        db.execSQL("DROP INDEX index_datasource_id");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public synchronized void createIfNotExists(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_DATA);
        db.execSQL(SQL_CREATE_DATA_INDEX);
        db.execSQL(SQL_CREATE_CC_INDEX);
    }

    private synchronized Status insertDB(SQLiteDatabase db, String tableName) {
        try {

            if (cValueCount == 0)
                return new Status(Status.SUCCESS);


            long st = System.currentTimeMillis();
            db.beginTransaction();

            Log.w("INSERTDB", "Buffer Size: " + cValueCount + " (" + tableName + ")");
            for (int i = 0; i < cValueCount; i++)
                db.insert(tableName, null, cValues[i]);
            cValueCount = 0;
            try {
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                Log.w("INSERTDB", "Transaction Time: " + (System.currentTimeMillis() - st));
            }
        } catch (Exception e) {
            return new Status(Status.INTERNAL_ERROR);
        }
        return new Status(Status.SUCCESS);
    }


    public synchronized Status insert(SQLiteDatabase db, int dataSourceId, DataType dataType) {
        Status status = new Status(Status.SUCCESS);
        if (dataType.getDateTime() - lastUnlock >= WAITTIME || cValueCount >= CVALUE_LIMIT) {
            status = insertDB(db, TABLE_NAME);
            cValueCount=0;
            lastUnlock = dataType.getDateTime();
        }
        ContentValues contentValues = prepareData(dataSourceId, dataType);
        cValues[cValueCount++] = contentValues;
        return status;
    }

    public synchronized Status insertHF(int dataSourceId, DataTypeDoubleArray dataType) {
        Status status = new Status(Status.SUCCESS);
        if (dataType.getDateTime() - lastUnlock >= WAITTIME || hfValueCount >= HFVALUE_LIMIT) {
            status = gzLogger.insert(hfValues, hfValueCount);
            hfValueCount = 0;
            lastUnlock = dataType.getDateTime();
        }
        ContentValues contentValues = prepareDataHF(dataSourceId, dataType);
        hfValues[hfValueCount++] = contentValues;
        return status;
    }


    public synchronized ContentValues prepareDataHF(int dataSourceId, DataTypeDoubleArray dataType) {
        ContentValues contentValues = new ContentValues();
        byte[] dataTypeArray = dataType.toRawBytes();

        contentValues.put(C_DATASOURCE_ID, dataSourceId);
        contentValues.put(C_DATETIME, dataType.getDateTime());
        contentValues.put(C_SAMPLE, dataTypeArray);
        return contentValues;
    }


    private synchronized String[] prepareSelectionArgs(int ds_id, long starttimestamp, long endtimestamp) {
        ArrayList<String> selectionArgs = new ArrayList<>();
        selectionArgs.add(String.valueOf(ds_id));
        selectionArgs.add(String.valueOf(starttimestamp));
        selectionArgs.add(String.valueOf(endtimestamp));
        return selectionArgs.toArray(new String[selectionArgs.size()]);
    }

    private synchronized String[] prepareSelectionArgs(int ds_id) {
        ArrayList<String> selectionArgs = new ArrayList<>();
        selectionArgs.add(String.valueOf(ds_id));
        return selectionArgs.toArray(new String[selectionArgs.size()]);
    }

    private synchronized String prepareSelection() {
        String selection;
        selection = C_DATASOURCE_ID + "=? AND " + C_DATETIME + " >=? AND " + C_DATETIME + " <=?";
        return selection;
    }

    private synchronized String prepareSelectionLastSamples() {
        String selection;
        selection = C_DATASOURCE_ID + "=?";
        return selection;
    }


    public synchronized ArrayList<DataType> query(SQLiteDatabase db, int ds_id, long starttimestamp, long endtimestamp) {
        long totalst = System.currentTimeMillis();
        insertDB(db, TABLE_NAME);
        Cursor mCursor;
        ArrayList<DataType> dataTypes = new ArrayList<>();
        String[] columns = new String[]{C_SAMPLE};
        String selection = prepareSelection();
        String[] selectionArgs = prepareSelectionArgs(ds_id, starttimestamp, endtimestamp);
        mCursor = db.query(TABLE_NAME,
                columns, selection, selectionArgs, null, null, null);
        if (mCursor.moveToFirst()) {
            do {
                try {
                    dataTypes.add(fromBytes(mCursor.getBlob(mCursor.getColumnIndex(C_SAMPLE))));
                } catch (Exception e) {
                    Log.e("DataKit", "Object failed deserialization");
                    Log.e("DataKit", "DataSourceID: " + ds_id + " Row: " + mCursor.getLong(mCursor.getColumnIndex(C_ID)));
                    e.printStackTrace();
                }
            } while (mCursor.moveToNext());
        }
        mCursor.close();
        Log.w(TAG, "Query: starttime="+starttimestamp+" endtime="+ endtimestamp+" Size="+dataTypes.size()+" QueryTime: " + (System.currentTimeMillis() - totalst));

        return dataTypes;
    }

    public synchronized ArrayList<DataType> query(SQLiteDatabase db, int ds_id, int last_n_sample) {
        long totalst = System.currentTimeMillis();
        insertDB(db, TABLE_NAME);
        ArrayList<DataType> dataTypes = new ArrayList<>();
        String[] columns = new String[]{C_SAMPLE};
        String selection = prepareSelectionLastSamples();
        String[] selectionArgs = prepareSelectionArgs(ds_id);
        Cursor mCursor = db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, "_id DESC", String.valueOf(last_n_sample));
        if (mCursor.moveToFirst()) {
            do {
                try {
                    dataTypes.add(fromBytes(mCursor.getBlob(mCursor.getColumnIndex(C_SAMPLE))));
                } catch (Exception e) {
                    Log.e("DataKit", "Object failed deserialization");
                    Log.e("DataKit", "DataSourceID: " + ds_id + " Row: " + mCursor.getLong(mCursor.getColumnIndex(C_ID)));
                    e.printStackTrace();
                }
            } while (mCursor.moveToNext());
        }
        mCursor.close();
        Log.w(TAG, "Query: id="+ds_id+" last_n_sample="+last_n_sample+" Size="+dataTypes.size()+" QueryTime: " + (System.currentTimeMillis() - totalst));
        return dataTypes;
    }


    public synchronized ArrayList<RowObject> queryLastKey(SQLiteDatabase db, int ds_id, int limit) {
        long totalst = System.currentTimeMillis();
        insertDB(db, TABLE_NAME);
        ArrayList<RowObject> rowObjects = new ArrayList<>();
        String sql = "select _id, sample from data where cc_sync = 0 and datasource_id=" + Integer.toString(ds_id) + " LIMIT " + Integer.toString(limit);
        Cursor mCursor = db.rawQuery(sql, null);
        if (mCursor.moveToFirst()) {
            do {
                try {
                    DataType dt = fromBytes(mCursor.getBlob(mCursor.getColumnIndex(C_SAMPLE)));
                    rowObjects.add(new RowObject(mCursor.getLong(mCursor.getColumnIndex(C_ID)), dt));
                } catch (Exception e) {
                    Log.e("DataKit", "Object failed deserialization");
                    Log.e("DataKit", "DataSourceID: " + ds_id + " Row: " + mCursor.getLong(mCursor.getColumnIndex(C_ID)));
                    e.printStackTrace();
                }
            } while (mCursor.moveToNext());
        }
        mCursor.close();

        Log.w(TAG, "QueryLastKey: Size="+rowObjects.size()+" QueryTime: " + (System.currentTimeMillis() - totalst));
        return rowObjects;
    }

    public synchronized ArrayList<RowObject> querySyncedData(SQLiteDatabase db, int ds_id, long ageLimit, int limit) {
        long totalst = System.currentTimeMillis();
        insertDB(db, TABLE_NAME);
        ArrayList<RowObject> rowObjects = new ArrayList<>(limit);
        String sql = "select _id, sample from data where cc_sync = 1 and datasource_id=" + Integer.toString(ds_id) + " and datetime <= " + ageLimit + " LIMIT " + Integer.toString(limit);
        Cursor mCursor = db.rawQuery(sql, null);
        if (mCursor.moveToFirst()) {
            do {
                try {
                    DataType dt = fromBytes(mCursor.getBlob(mCursor.getColumnIndex(C_SAMPLE)));
                    rowObjects.add(new RowObject(mCursor.getLong(mCursor.getColumnIndex(C_ID)), dt));
                } catch (Exception e) {
                    Log.e("DataKit", "Object failed deserialization");
                    Log.e("DataKit", "DataSourceID: " + ds_id + " Row: " + mCursor.getLong(mCursor.getColumnIndex(C_ID)));
                    e.printStackTrace();
                }
            } while (mCursor.moveToNext());
        }
        mCursor.close();
        Log.w(TAG, "QuerySyncedData: Size="+rowObjects.size()+" QueryTime: " + (System.currentTimeMillis() - totalst));
        return rowObjects;
    }


    public synchronized boolean removeSyncedData(SQLiteDatabase db, int dsid, long lastSyncKey) {
        insertDB(db, TABLE_NAME);
        String[] args = new String[]{Long.toString(lastSyncKey), Integer.toString(dsid)};
        db.delete(TABLE_NAME, "cc_sync = 1 AND _id <= ? AND datasource_id = ?", args);
        return true;
    }

    public synchronized boolean setSyncedBit(SQLiteDatabase db, int dsid, long lastSyncKey) {
        insertDB(db, TABLE_NAME);
        ContentValues values = new ContentValues();
        int bit = 1;
        values.put("cc_sync", bit);
        String[] args = new String[]{Long.toString(lastSyncKey), Integer.toString(dsid)};
        db.update(TABLE_NAME, values, "cc_sync = 0 AND _id <= ? AND datasource_id = ?", args);

        return true;
    }


    public synchronized DataTypeLong querySize(SQLiteDatabase db) {
        long totalst = System.currentTimeMillis();
        insertDB(db, TABLE_NAME);
        String sql = "select count(_id)as c from data";
        Cursor mCursor = db.rawQuery(sql, null);
        DataTypeLong count = new DataTypeLong(0L, 0L);
        if (mCursor.moveToFirst()) {
            do {
                count = new DataTypeLong(0L, mCursor.getLong(mCursor.getColumnIndex(C_COUNT)));
            } while (mCursor.moveToNext());
        }
        mCursor.close();
        Log.w(TAG, "QuerySize="+count+" QueryTime: " + (System.currentTimeMillis() - totalst));
        return count;
    }

    public synchronized DataTypeLong queryCount(SQLiteDatabase db, int ds_id, boolean unsynced) {
        long totalst = System.currentTimeMillis();
        String sql = "select count(_id)as c from " + TABLE_NAME + " where " + C_DATASOURCE_ID + " = " + ds_id;
        if (unsynced)
            sql += " and " + C_CLOUD_SYNC_BIT + " = 0";
        Cursor mCursor = db.rawQuery(sql, null);
        DataTypeLong count = new DataTypeLong(0L, 0L);
        if (mCursor.moveToFirst()) {
            do {
                count = new DataTypeLong(0L, mCursor.getLong(mCursor.getColumnIndex(C_COUNT)));
            } while (mCursor.moveToNext());
        }
        mCursor.close();
        Log.w(TAG, "QueryCount="+count+" QueryTime: " + (System.currentTimeMillis() - totalst));
        return count;
    }

    public synchronized ContentValues prepareData(int dataSourceId, DataType dataType) {
        ContentValues contentValues = new ContentValues();

        byte[] dataTypeArray = toBytes(dataType);

        contentValues.put(C_DATASOURCE_ID, dataSourceId);
        contentValues.put(C_DATETIME, dataType.getDateTime());
        contentValues.put(C_SAMPLE, dataTypeArray);
        return contentValues;
    }


    public synchronized void commit(SQLiteDatabase db) {
        insertDB(db, TABLE_NAME);
    }

    private synchronized byte[] toBytes(DataType dataType) {
        byte[] bytes;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(baos);
        kryo.writeClassAndObject(output, dataType);
        output.close();
        bytes = baos.toByteArray();
        return bytes;
    }

    private synchronized DataType fromBytes(byte[] bytes) {
        Input input = new Input(new ByteArrayInputStream(bytes));
        DataType dataType = (DataType) kryo.readClassAndObject(input);
        input.close();
        return dataType;
    }


}
