package com.xiebb.chat.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/**
 * Created by baobiao on 2016/11/28.
 */

public class BaseActivity extends Activity implements View.OnClickListener{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        initView();
    }

    public void initData() {
    }

    public void initView() {
    }

    @Override
    public void onClick(View view) {

    }
}
