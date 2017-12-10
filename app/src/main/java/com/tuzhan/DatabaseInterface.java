package com.tuzhan;

import android.content.ContentValues;

/**
 * Created by chenchangheng on 10/5/17.
 */
public interface DatabaseInterface {
    String selector();
    ContentValues composeUpdateValues();
}
