package com.xiebb.chat;

import android.app.Application;
import android.content.Context;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import com.xiebb.chat.huanxin.ChatHelper;

/**
 * Created by baobiao on 2016/11/27.
 */

public class BaseApplication extends Application {
    public static Context applicationContext;
    private static BaseApplication instance;
    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext=this;
        instance = this;

        EMOptions options = new EMOptions();
        // 默认添加好友时，是不需要验证的，改成需要验证
        options.setAcceptInvitationAlways(false);
        //初始化
        EMClient.getInstance().init(getApplicationContext(), options);
         //在做打包混淆时，关闭debug模式，避免消耗不必要的资源
        EMClient.getInstance().setDebugMode(true);
        ChatHelper.getInstance().init(applicationContext);
    }
    public static BaseApplication getInstance() {
        return instance;
    }
}
