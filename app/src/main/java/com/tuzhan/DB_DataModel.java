package com.tuzhan;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public abstract class DB_DataModel implements DB_DataModel_Interface{

    public String TABLE_NAME;

    public void updateDB(SQLiteDatabase  database){
        if(this.isInDatabase(database)) database.update(this.TABLE_NAME, this.composeUpdateValues(), this.selector(), null);
        else database.insert(this.TABLE_NAME,null, this.composeUpdateValues());
    }

    public boolean isInDatabase(SQLiteDatabase database){
        Cursor cursor = database.rawQuery("Select * from " + this.TABLE_NAME + " where " + this.selector(), null);
        if(cursor.getCount() > 0){
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }
}
