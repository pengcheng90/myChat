package com.xiebb.chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMChatManager;
import com.hyphenate.chat.EMClient;
import com.hyphenate.util.NetUtils;
import com.xiebb.chat.R;
import com.xiebb.chat.utils.IntentUtil;
import com.xiebb.chat.utils.SpUtils;
import com.xiebb.chat.utils.ToastUtils;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        connectionListener();
    }

    private void initView() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("消息");
        setSupportActionBar(mToolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtils.show(getApplicationContext(), "直播功能正在开发中");
            }
        });

        //toolbar左边菜单按钮
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View view = navigationView.getHeaderView(0);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.message) {
            ToastUtils.show(getApplicationContext(), "点击了message");
        } else if (id == R.id.contanct) {
            ToastUtils.show(getApplicationContext(), "contanct");
        } else if (id == R.id.dynamic) {
            ToastUtils.show(getApplicationContext(), "dynamic");
        } else if (id == R.id.setting) {
            ToastUtils.show(getApplicationContext(), "setting");
        }
        closeDrawer();
        return true;
    }


    @Override
    protected void onStop() {
        super.onStop();
        closeDrawer();
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.imageView:
                if (TextUtils.isEmpty(SpUtils.getString(getApplicationContext(), "username"))) {
                    intent = IntentUtil.getInstance();
                    intent.setClass(getApplicationContext(), LoginActivity.class);
                } else {
                    intent = IntentUtil.getInstance();
                    intent.setClass(getApplicationContext(), MeActivity.class);
                }
                startActivity(intent);
                break;
        }
    }

    private void closeDrawer() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    public void connectionListener() {
        EMClient.getInstance().addConnectionListener(new MyConnectionListener());
    }

    //实现ConnectionListener接口
    private class MyConnectionListener implements EMConnectionListener {
        @Override
        public void onConnected() {
        }

        @Override
        public void onDisconnected(final int error) {
            runOnUiThread(new Runnable() {
                              @Override
                              public void run() {
                                  if (error == EMError.USER_REMOVED) {
                                      // 显示帐号已经被移除
                                  } else if (error == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                                      // 显示帐号在其他设备登录
                                  } else {
                                      if (NetUtils.hasNetwork(MainActivity.this)) {

                                      }
                                      //连接不到聊天服务器
                                      else {
                                          //当前网络不可用，请检查网络设置
                                      }

                                  }
                              }
                          }
            );
        }
    }
}
