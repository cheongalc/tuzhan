package com.tuzhan;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public abstract class DB_DataModel implements DB_DataModel_Interface{

    String tableName;

    public void updateDB(SQLiteDatabase  database){
        if(this.isInDatabase(database, tableName)) database.update(tableName, this.composeUpdateValues(), this.selector(), null);
        else database.insert(tableName,null, this.composeUpdateValues());
    }

    public boolean isInDatabase(SQLiteDatabase database, String TABLE_NAME){
        Cursor cursor = database.rawQuery("SELECT * FROM " + tableName + " WHERE " + this.selector(), null);
        if(cursor.getCount() > 0){
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }
}
