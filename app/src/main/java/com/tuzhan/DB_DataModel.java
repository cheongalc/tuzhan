package com.tuzhan;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public abstract class DB_DataModel implements DB_DataModel_Interface{

    static String TABLE_NAME = "CARDS";

    public void updateDB(SQLiteDatabase  database){
        if(this.isInDatabase(database)) database.update(TABLE_NAME, this.composeUpdateValues(), this.selector(), null);
        else database.insert(TABLE_NAME,null, this.composeUpdateValues());
    }

    public boolean isInDatabase(SQLiteDatabase database){
        Cursor cursor = database.rawQuery("Select * from " + TABLE_NAME + " where " + this.selector(), null);
        if(cursor.getCount() > 0){
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }
}
