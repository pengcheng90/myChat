package com.xiebb.chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.xiebb.chat.R;
import com.xiebb.chat.utils.LogUtils;
import com.xiebb.chat.utils.SpUtils;
import com.xiebb.chat.utils.ToastUtils;

/**
 * Created by baobiao on 2016/11/28.
 */

public class LoginActivity extends BaseActivity {

    public static final String TAG = "LoginActivity";
    private ImageView back;
    private TextView register;
    private Button login;
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
        super.initView();
        setContentView(R.layout.activity_login);

        back = (ImageView) findViewById(R.id.back);
        register = (TextView) findViewById(R.id.register);
        login = (Button) findViewById(R.id.login);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);

        login.setOnClickListener(this);
        register.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {


        Intent intent;
        switch (view.getId()) {
            case R.id.login:
                login();
                break;
            case R.id.back:
                finish();
                break;
            case R.id.register:
                intent = new Intent(getApplicationContext(), RegisteActivity.class);
                startActivity(intent);
                break;


        }
    }

    private void login() {
        final String name = username.getText().toString().trim();
        final String pwd = password.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            ToastUtils.show(getApplicationContext(), "请输入用户名");
            return;
        } else if (TextUtils.isEmpty(pwd)) {
            ToastUtils.show(getApplicationContext(), "请输入密码");
            return;
        }
        EMClient.getInstance().login(name, pwd, new EMCallBack() {
            @Override
            public void onSuccess() {
                EMClient.getInstance().groupManager().loadAllGroups();
                EMClient.getInstance().chatManager().loadAllConversations();
                SpUtils.putString(getApplicationContext(), "username", name);
                SpUtils.putString(getApplicationContext(), "password", pwd);
                ToastUtils.showThread(LoginActivity.this, "登陆成功，返回");
                finish();
            }

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(int code, String message) {
                if (code == 200) {
                    ToastUtils.showThread(LoginActivity.this, "用户已登陆");
                }
                LogUtils.e(TAG, "code:" + code + "message:" + message);
            }
        });
    }
}
