package com.tuzhan;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public abstract class DB_DataModel implements DB_DataModel_Interface{

    String tableName;

    public void updateDB(SQLiteDatabase  database){
        if(this.isInDatabase(database)) database.update(this.tableName, this.composeUpdateValues(), this.selector(), null);
        else database.insert(this.tableName,null, this.composeUpdateValues());
    }

<<<<<<< HEAD
    public boolean isInDatabase(SQLiteDatabase database, String TABLE_NAME){
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + this.selector(), null);
=======
    public boolean isInDatabase(SQLiteDatabase database){
        Cursor cursor = database.rawQuery("SELECT * FROM " + this.tableName + " WHERE " + this.selector(), null);
>>>>>>> 7ad5dd337aee29671379db1fceb8aa9c6a1237c8
        if(cursor.getCount() > 0){
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }
}
