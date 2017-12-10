package com.tuzhan;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public abstract class DatabaseModel implements DatabaseInterface {

    String tableName;

    public void updateDB(SQLiteDatabase  database){
        if(this.isInDatabase(database)) database.update(this.tableName, this.composeUpdateValues(), this.selector(), null);
        else database.insert(this.tableName,null, this.composeUpdateValues());
    }

    public boolean isInDatabase(SQLiteDatabase database){
        Cursor cursor = database.rawQuery("SELECT * FROM " + this.tableName + " WHERE " + this.selector(), null);
        if(cursor.getCount() > 0){
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }
}
