package com.tuzhan;

import android.support.annotation.Nullable;

/**
 * Created by chenchangheng on 7/12/17.
 */

public interface DataFetchedCallback<T>{
    void fetched(@Nullable T data);
}
