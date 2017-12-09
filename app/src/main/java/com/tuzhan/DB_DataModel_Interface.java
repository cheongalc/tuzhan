package com.tuzhan;

import android.content.ContentValues;

/**
 * Created by chenchangheng on 10/5/17.
 */
public interface DB_DataModel_Interface{
    String selector();
    ContentValues composeUpdateValues();
}
