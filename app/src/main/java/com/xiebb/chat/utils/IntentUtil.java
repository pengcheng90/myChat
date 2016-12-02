package com.xiebb.chat.utils;

import android.content.Context;
import android.content.Intent;

/**
 * Created by baobiao on 2016/11/28.
 */

public class IntentUtil {
    public static Intent intent;

    public static Intent getInstance() {
        if (intent == null) {
            intent = new Intent();
            return intent;
        }
        return intent;
    }
}
