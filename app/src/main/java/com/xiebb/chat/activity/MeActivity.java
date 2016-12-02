package com.xiebb.chat.activity;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.xiebb.chat.R;
import com.xiebb.chat.utils.LogUtils;
import com.xiebb.chat.utils.SpUtils;
import com.xiebb.chat.utils.ToastUtils;

/**
 * Created by baobiao on 2016/12/2.
 */

public class MeActivity extends BaseActivity {
    private static final String TAG = "MeActivity";
    private TextView username;
    private Button logout;

    @Nullable
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_me);
        username = (TextView) findViewById(R.id.username);
        logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(this);

        username.setText("用户"+SpUtils.getString(getApplicationContext(), "username"));
    }

    @Override
    public void initData() {
        super.initData();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.logout:
                logout();
                break;
        }
    }

    private void logout() {

        //异步方法
        EMClient.getInstance().logout(true, new EMCallBack() {

            @Override
            public void onSuccess() {
                ToastUtils.showThread(MeActivity.this, "退出成功");
                finish();
                SpUtils.reMove(getApplicationContext(), "username");
                SpUtils.reMove(getApplicationContext(), "password");
                finish();
            }

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(int code, String message) {
                LogUtils.e(TAG, "code:" + code + "message:" + message);
            }
        });
    }
}
