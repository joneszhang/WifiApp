package com.example.zongsizhang.wifidetector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zongsizhang on 3/24/16.
 */
public class DBManager {
    SQLiteDatabase db = null;
    private Context context;

    public String TABLE_NAME_WIFI = "wifirecords";

    public DBManager(Context context){
        this.context = context;
    }

    public void startDB(){
        db = context.openOrCreateDatabase("wifis.db", Context.MODE_PRIVATE, null);
    }

    public List<WifiData> queryWifiRecords(){
        this.startDB();
        List<WifiData> result = new ArrayList<>();

        if(!IsTableExist(TABLE_NAME_WIFI)) this.createTable(TABLE_NAME_WIFI);
        Cursor c = db.rawQuery("SELECT * FROM "+ TABLE_NAME_WIFI + " order by time",null);
        if(c.getCount() > 0){
            while(c.moveToNext()){
                WifiData data = new WifiData();
                data.setTime(c.getString(0));
                data.setSsid(c.getString(1));
                data.setLevel(c.getInt(2));
                data.setMac_adress(c.getString(3));
                result.add(data);
            }
        }
        this.closeDB();
        return result;
    }

    public void pushWifiData(String time, String ssid, int level, String macad){
        this.startDB();
        if(!IsTableExist(TABLE_NAME_WIFI)) createTable(TABLE_NAME_WIFI);
        ContentValues cv = new ContentValues();
        cv.put("time", time);
        cv.put("ssid", ssid);
        cv.put("level", level);
        cv.put("macaddress", macad);
        db.insert(TABLE_NAME_WIFI, null, cv);
        this.closeDB();
    }

    public void clearWifiData(){
        this.startDB();
        if(!IsTableExist(TABLE_NAME_WIFI)) createTable(TABLE_NAME_WIFI);
        db.execSQL("delete from " + TABLE_NAME_WIFI);
        this.closeDB();
    }

    public void createTable(String name){
        db.execSQL("CREATE TABLE " + name +" (time DATETIME CURRENT_TIMESTAMP, ssid VARCHAR(255), level INT, macaddress VARCHAR(255))");
    }

    private boolean IsTableExist(String table_name) {
        boolean isTableExist = true;
        Cursor c = db.rawQuery("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='" + table_name + "'", null);
        if(c.moveToNext()){
            if (c.getInt(0) == 0) {
                isTableExist = false;
            }
        }
        c.close();
        return isTableExist;
    }

    public void closeDB(){
        db.close();
    }

}
