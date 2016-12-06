package com.xiebb.chat.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.xiebb.chat.R;
import com.xiebb.chat.utils.ToastUtils;

/**
 * Created by baobiao on 2016/12/6.
 */

public class AddFriendActivity extends Activity implements View.OnClickListener {
    private EditText username, reason;
    private Button commit;
    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        setContentView(R.layout.activity_addfriend);
        username = (EditText) findViewById(R.id.username);
        reason = (EditText) findViewById(R.id.reason);
        commit = (Button) findViewById(R.id.commit);
        back = (ImageView) findViewById(R.id.back);

        back.setOnClickListener(this);
        commit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.commit:
                commit();
                break;

        }
    }

    private void commit() {

        String name = username.getText().toString().trim();
        String reason1 = reason.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            ToastUtils.show(getApplicationContext(), "请输入用户名");
            return;
        }
        if (TextUtils.isEmpty(reason1)) {
            ToastUtils.show(getApplicationContext(), "请输入添加理由");
            return;
        }
        if (!EMClient.getInstance().isLoggedInBefore()) {
            ToastUtils.show(getApplicationContext(), "您还未登录，请登录后添加");
            return;
        }
        //参数为要添加的好友的username和添加理由
        try {
            EMClient.getInstance().contactManager().addContact(name, reason1);
            ToastUtils.show(getApplicationContext(), "添加成功");
        } catch (HyphenateException e) {
            ToastUtils.show(getApplicationContext(), "添加失败");
            e.printStackTrace();
        }
    }
}
