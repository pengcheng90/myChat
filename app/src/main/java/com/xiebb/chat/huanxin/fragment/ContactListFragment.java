package com.xiebb.chat.huanxin.fragment;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.ui.EaseContactListFragment;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.exceptions.HyphenateException;
import com.xiebb.chat.R;
import com.xiebb.chat.huanxin.activity.ChatActivity;
import com.xiebb.chat.utils.LogUtils;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Created by baobiao on 2016/12/4.
 */

public class ContactListFragment extends EaseContactListFragment {


    private static final String TAG = "ContactListFragment";
    private Map<String, EaseUser> m = null;

    @Override
    protected void setUpView() {
//        Map<String, EaseUser> m = ChatHelper.getInstance().getContactList();
        if (!isLoggedIn()) {
            LogUtils.i(TAG, "未登录");
            return;
        }
        new Thread(new Runnable() {
            List<String> usernames = null;

            @Override
            public void run() {
                try {
                    usernames = EMClient.getInstance().contactManager().getAllContactsFromServer();
                    if (usernames != null) {
                        setData(usernames);
                    } else {
                        LogUtils.e(TAG, "数据为空");
                    }
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        super.setUpView();
        setOnItemClickListener();
    }

    private void setOnItemClickListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EaseUser user = (EaseUser) listView.getItemAtPosition(position);
                if (user != null) {
                    String username = user.getUsername();
                    // demo中直接进入聊天页面，实际一般是进入用户详情页
                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    intent.putExtra("userId", username);
                    getActivity().startActivity(intent);
                }
            }
        });
    }

    private void setData(List<String> usernames) {
        m = new HashMap<String, EaseUser>();
        for (String username : usernames) {
            EaseUser user = new EaseUser(username);
            EaseCommonUtils.setUserInitialLetter(user);
            m.put(username, user);
        }
        if (m instanceof Hashtable<?, ?>) {

            m = (Map<String, EaseUser>) ((Hashtable<String, EaseUser>) m).clone();
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContactsMap(m);
                ContactListFragment.super.setUpView();
//                setUpView();
            }
        });

    }

    /**
     * if ever logged in
     *
     * @return
     */
    public boolean isLoggedIn() {
        return EMClient.getInstance().isLoggedInBefore();
    }
}
