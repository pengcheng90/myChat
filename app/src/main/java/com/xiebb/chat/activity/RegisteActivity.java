package com.xiebb.chat.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.xiebb.chat.R;
import com.xiebb.chat.utils.ToastUtils;

/**
 * Created by baobiao on 2016/11/28.
 */
public class RegisteActivity extends BaseActivity {
    private static final String TAG = "RegisteActivity";
    private Button register;
    private EditText username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initData() {
        super.initData();
    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_register);
        register = (Button) findViewById(R.id.register);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);

        register.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        final String name = username.getText().toString().trim();
        final String pwd = password.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            ToastUtils.show(getApplicationContext(), "请输入用户名");
        } else if (TextUtils.isEmpty(pwd)) {
            ToastUtils.show(getApplicationContext(), "请输入密码");
        }
        switch (view.getId()) {
            case R.id.register:
                //注册失败会抛出HyphenateException
                try {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                EMClient.getInstance().createAccount(name, pwd);//同步方法
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ToastUtils.show(getApplicationContext(), "注册成功");
                                    }
                                });
                            } catch (HyphenateException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ToastUtils.show(getApplicationContext(), "注册失败");
                                    }
                                });
                                e.printStackTrace();
                            }


                        }
                    }).start();

                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }
}
