package com.xiebb.chat.huanxin;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.easeui.controller.EaseUI;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.domain.EaseEmojiconGroupEntity;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.model.EaseAtMessageHelper;
import com.hyphenate.easeui.model.EaseNotifier;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.EMLog;
import com.xiebb.chat.ChatModel;
import com.xiebb.chat.R;
import com.xiebb.chat.activity.MainActivity;
import com.xiebb.chat.huanxin.activity.ChatActivity;
//import com.xiebb.chat.huanxin.activity.VideoCallActivity;
//import com.xiebb.chat.huanxin.activity.VoiceCallActivity;
import com.xiebb.chat.huanxin.db.Constant;
import com.xiebb.chat.huanxin.db.InviteMessgeDao;
import com.xiebb.chat.huanxin.db.UserDao;
import com.xiebb.chat.huanxin.domain.EmojiconExampleGroupData;
import com.xiebb.chat.huanxin.domain.RobotUser;
//import com.xiebb.chat.huanxin.parse.UserProfileManager;
//import com.xiebb.chat.huanxin.receiver.CallReceiver;
import com.xiebb.chat.huanxin.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Created by baobiao on 2016/12/4.
 */

public class ChatHelper {
    /**
     * data sync listener
     */
    public interface DataSyncListener {
        /**
         * sync complete
         *
         * @param success true：data sync successful，false: failed to sync data
         */
        void onSyncComplete(boolean success);
    }

    //    private CallReceiver callReceiver;
    private static final String TAG = "ChatHelper";
    private static ChatHelper instance = null;
    private Map<String, EaseUser> contactList;
    //    private DemoModel demoModel = null;
    private Context appContext;
    private String username;
    private ChatModel chatModel = null;

    private DemoModel demoModel = null;
    private EaseUI easeUI;
    //    private UserProfileManager userProManager;
    private List<DataSyncListener> syncContactsListeners;
    /**
     * sync blacklist status listener
     */
    private List<DataSyncListener> syncBlackListListeners;
    /**
     * sync groups status listener
     */
    private List<DataSyncListener> syncGroupsListeners;

    private boolean isSyncingGroupsWithServer = false;
    private boolean isSyncingContactsWithServer = false;
    private boolean isSyncingBlackListWithServer = false;
    private boolean isGroupsSyncedWithServer = false;
    private boolean isContactsSyncedWithServer = false;
    private boolean isBlackListSyncedWithServer = false;
    private InviteMessgeDao inviteMessgeDao;
    private UserDao userDao;
    private Map<String, RobotUser> robotList;
    public boolean isVideoCalling;

    private LocalBroadcastManager broadcastManager;
    public boolean isVoiceCalling;

    public void init(Context context) {
        demoModel = new DemoModel(context);
        this.appContext = context;
//        EMOptions options = initChatOptions();
        EMOptions options = initChatOptions();
        if (EaseUI.getInstance().init(context, options)) {
//            appContext = context;
//
//            //debug mode, you'd better set it to false, if you want release your App officially.
//            EMClient.getInstance().setDebugMode(true);
//            //get easeui instance
//            easeUI = EaseUI.getInstance();
//            //to set user's profile and avatar
////            setEaseUIProviders();
//            //initialize preference manager
//            PreferenceManager.init(context);
//            //initialize profile manager
////            getUserProfileManager().init(context);
//
//            EMClient.getInstance().callManager().getCallOptions().setIsSendPushIfOffline(getModel().isPushCall());
//
//            setGlobalListeners();
//            broadcastManager = LocalBroadcastManager.getInstance(appContext);
//            initDbDao();
        }
    }

    private void initDbDao() {
        inviteMessgeDao = new InviteMessgeDao(appContext);
        userDao = new UserDao(appContext);
    }


    /**
     * user met some exception: conflict, removed or forbidden
     */
    protected void onUserException(String exception) {
        EMLog.e(TAG, "onUserException: " + exception);
        Intent intent = new Intent(appContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(exception, true);
        appContext.startActivity(intent);
    }

    EMConnectionListener connectionListener;

    /**
     * set global listener
     */
    protected void setGlobalListeners() {
        syncGroupsListeners = new ArrayList<DataSyncListener>();
        syncContactsListeners = new ArrayList<DataSyncListener>();
        syncBlackListListeners = new ArrayList<DataSyncListener>();

        isGroupsSyncedWithServer = demoModel.isGroupsSynced();
        isContactsSyncedWithServer = demoModel.isContactSynced();
        isBlackListSyncedWithServer = demoModel.isBacklistSynced();

        // create the global connection listener
        connectionListener = new EMConnectionListener() {
            @Override
            public void onDisconnected(int error) {
                EMLog.d("global listener", "onDisconnect" + error);
                if (error == EMError.USER_REMOVED) {
                    onUserException(Constant.ACCOUNT_REMOVED);
                } else if (error == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                    onUserException(Constant.ACCOUNT_CONFLICT);
                } else if (error == EMError.SERVER_SERVICE_RESTRICTED) {
                    onUserException(Constant.ACCOUNT_FORBIDDEN);
                }
            }

            @Override
            public void onConnected() {
                // in case group and contact were already synced, we supposed to notify sdk we are ready to receive the events
                if (isGroupsSyncedWithServer && isContactsSyncedWithServer) {
                    EMLog.d(TAG, "group and contact already synced with servre");
                } else {
                    if (!isGroupsSyncedWithServer) {
                        asyncFetchGroupsFromServer(null);
                    }

                    if (!isContactsSyncedWithServer) {
                        asyncFetchContactsFromServer(null);
                    }

                    if (!isBlackListSyncedWithServer) {
                        asyncFetchBlackListFromServer(null);
                    }
                }
            }
        };
    }

    public void asyncFetchBlackListFromServer(final EMValueCallBack<List<String>> callback) {

        if (isSyncingBlackListWithServer) {
            return;
        }

        isSyncingBlackListWithServer = true;

        new Thread() {
            @Override
            public void run() {
                try {
                    List<String> usernames = EMClient.getInstance().contactManager().getBlackListFromServer();

                    // in case that logout already before server returns, we should return immediately
                    if (!isLoggedIn()) {
                        isBlackListSyncedWithServer = false;
                        isSyncingBlackListWithServer = false;
                        notifyBlackListSyncListener(false);
                        return;
                    }

                    demoModel.setBlacklistSynced(true);

                    isBlackListSyncedWithServer = true;
                    isSyncingBlackListWithServer = false;

                    notifyBlackListSyncListener(true);
                    if (callback != null) {
                        callback.onSuccess(usernames);
                    }
                } catch (HyphenateException e) {
                    demoModel.setBlacklistSynced(false);

                    isBlackListSyncedWithServer = false;
                    isSyncingBlackListWithServer = true;
                    e.printStackTrace();

                    if (callback != null) {
                        callback.onError(e.getErrorCode(), e.toString());
                    }
                }

            }
        }.start();
    }

    public void notifyBlackListSyncListener(boolean success) {
        for (DataSyncListener listener : syncBlackListListeners) {
            listener.onSyncComplete(success);
        }
    }

    public void asyncFetchContactsFromServer(final EMValueCallBack<List<String>> callback) {
        if (isSyncingContactsWithServer) {
            return;
        }

        isSyncingContactsWithServer = true;

        new Thread() {
            @Override
            public void run() {
                List<String> usernames = null;
                try {
                    usernames = EMClient.getInstance().contactManager().getAllContactsFromServer();
                    // in case that logout already before server returns, we should return immediately
                    if (!isLoggedIn()) {
                        isContactsSyncedWithServer = false;
                        isSyncingContactsWithServer = false;
                        notifyContactsSyncListener(false);
                        return;
                    }

                    Map<String, EaseUser> userlist = new HashMap<String, EaseUser>();
                    for (String username : usernames) {
                        EaseUser user = new EaseUser(username);
                        EaseCommonUtils.setUserInitialLetter(user);
                        userlist.put(username, user);
                    }
                    // save the contact list to cache
                    getContactList().clear();
                    getContactList().putAll(userlist);
                    // save the contact list to database
                    UserDao dao = new UserDao(appContext);
                    List<EaseUser> users = new ArrayList<EaseUser>(userlist.values());
                    dao.saveContactList(users);

                    demoModel.setContactSynced(true);
                    EMLog.d(TAG, "set contact syn status to true");

                    isContactsSyncedWithServer = true;
                    isSyncingContactsWithServer = false;

                    //notify sync success
                    notifyContactsSyncListener(true);

//                    getUserProfileManager().asyncFetchContactInfosFromServer(usernames, new EMValueCallBack<List<EaseUser>>() {
//
//                        @Override
//                        public void onSuccess(List<EaseUser> uList) {
//                            updateContactList(uList);
//                            getUserProfileManager().notifyContactInfosSyncListener(true);
//                        }
//
//                        @Override
//                        public void onError(int error, String errorMsg) {
//                        }
//                    });
                    if (callback != null) {
                        callback.onSuccess(usernames);
                    }
                } catch (HyphenateException e) {
                    demoModel.setContactSynced(false);
                    isContactsSyncedWithServer = false;
                    isSyncingContactsWithServer = false;
                    notifyContactsSyncListener(false);
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onError(e.getErrorCode(), e.toString());
                    }
                }

            }
        }.start();
    }

    /**
     * update user list to cache and database
     *
     * @param contactInfoList
     */
    public void updateContactList(List<EaseUser> contactInfoList) {
        for (EaseUser u : contactInfoList) {
            contactList.put(u.getUsername(), u);
        }
        ArrayList<EaseUser> mList = new ArrayList<EaseUser>();
        mList.addAll(contactList.values());
        demoModel.saveContactList(mList);
    }

    public void notifyContactsSyncListener(boolean success) {
        for (DataSyncListener listener : syncContactsListeners) {
            listener.onSyncComplete(success);
        }
    }

    public void noitifyGroupSyncListeners(boolean success) {
        for (DataSyncListener listener : syncGroupsListeners) {
            listener.onSyncComplete(success);
        }
    }

    /**
     * Get group list from server
     * This method will save the sync state
     *
     * @throws HyphenateException
     */

    public synchronized void asyncFetchGroupsFromServer(final EMCallBack callback) {
        if (isSyncingGroupsWithServer) {
            return;
        }

        isSyncingGroupsWithServer = true;

        new Thread() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().groupManager().getJoinedGroupsFromServer();

                    // in case that logout already before server returns, we should return immediately
                    if (!isLoggedIn()) {
                        isGroupsSyncedWithServer = false;
                        isSyncingGroupsWithServer = false;
                        noitifyGroupSyncListeners(false);
                        return;
                    }

                    demoModel.setGroupsSynced(true);

                    isGroupsSyncedWithServer = true;
                    isSyncingGroupsWithServer = false;

                    //notify sync group list success
                    noitifyGroupSyncListeners(true);

                    if (callback != null) {
                        callback.onSuccess();
                    }
                } catch (HyphenateException e) {
                    demoModel.setGroupsSynced(false);
                    isGroupsSyncedWithServer = false;
                    isSyncingGroupsWithServer = false;
                    noitifyGroupSyncListeners(false);
                    if (callback != null) {
                        callback.onError(e.getErrorCode(), e.toString());
                    }
                }

            }
        }.start();
    }


    /**
     * 获取ChatHelper 实例
     * 单例设计模式，双重锁定
     *
     * @return ChatHelper
     */
    public static ChatHelper getInstance() {
        if (instance == null) {
            synchronized (ChatHelper.class) {
                if (instance == null) {
                    instance = new ChatHelper();
                }
            }
        }
        return instance;
    }

    /**
     * get contact list
     *
     * @return
     */
    public Map<String, EaseUser> getContactList() {
        if (isLoggedIn() && contactList == null) {
            contactList = chatModel.getContactList();
            UserDao userDao = new UserDao(appContext);
            contactList = userDao.getContactList();
        }

        // return a empty non-null object to avoid app crash
        if (contactList == null) {
            return new Hashtable<String, EaseUser>();
        }

        return contactList;
    }

    /**
     * 判断是否登陆
     *
     * @return
     */
    public boolean isLoggedIn() {

        return EMClient.getInstance().isLoggedInBefore();
    }

    /**
     * get current user's id
     */
    public String getCurrentUsernName() {

        if (username == null) {
            username = chatModel.getCurrentUsernName();
        }
        return username;
    }

    private EMOptions initChatOptions() {
        Log.d(TAG, "init HuanXin Options");

        EMOptions options = new EMOptions();
        // set if accept the invitation automatically
        options.setAcceptInvitationAlways(false);
        // set if you need read ack
        options.setRequireAck(true);
        // set if you need delivery ack
        options.setRequireDeliveryAck(false);

        //you need apply & set your own id if you want to use google cloud messaging.
        options.setGCMNumber("324169311137");
        //you need apply & set your own id if you want to use Mi push notification
        options.setMipushConfig("2882303761517426801", "5381742660801");
        //you need apply & set your own id if you want to use Huawei push notification
        options.setHuaweiPushAppId("10492024");

        //set custom servers, commonly used in private deployment
        if (demoModel.isCustomServerEnable() && demoModel.getRestServer() != null && demoModel.getIMServer() != null) {
            options.setRestServer(demoModel.getRestServer());
            options.setIMServer(demoModel.getIMServer());
            if (demoModel.getIMServer().contains(":")) {
                options.setIMServer(demoModel.getIMServer().split(":")[0]);
                options.setImPort(Integer.valueOf(demoModel.getIMServer().split(":")[1]));
            }
        }

        if (demoModel.isCustomAppkeyEnabled() && demoModel.getCutomAppkey() != null && !demoModel.getCutomAppkey().isEmpty()) {
            options.setAppKey(demoModel.getCutomAppkey());
        }

        options.allowChatroomOwnerLeave(getModel().isChatroomOwnerLeaveAllowed());
        options.setDeleteMessagesAsExitGroup(getModel().isDeleteMessagesAsExitGroup());
        options.setAutoAcceptGroupInvitation(getModel().isAutoAcceptGroupInvitation());

        return options;
    }

    public DemoModel getModel() {
        return (DemoModel) demoModel;
    }

//    public UserProfileManager getUserProfileManager() {
//        if (userProManager == null) {
//            userProManager = new UserProfileManager();
//        }
//        return userProManager;
//    }

    //    private EaseUser getUserInfo(String username) {
//        // To get instance of EaseUser, here we get it from the user list in memory
//        // You'd better cache it if you get it from your server
//        EaseUser user = null;
//        if (username.equals(EMClient.getInstance().getCurrentUser()))
//            return getUserProfileManager().getCurrentUserInfo();
//        user = getContactList().get(username);
//        if (user == null && getRobotList() != null) {
//            user = getRobotList().get(username);
//        }
//
//        // if user is not in your contacts, set inital letter for him/her
//        if (user == null) {
//            user = new EaseUser(username);
//            EaseCommonUtils.setUserInitialLetter(user);
//        }
//        return user;
//
//    }
    public Map<String, RobotUser> getRobotList() {
        if (isLoggedIn() && robotList == null) {
            robotList = demoModel.getRobotList();
        }
        return robotList;
    }

//    protected void setEaseUIProviders() {
//        // set profile provider if you want easeUI to handle avatar and nickname
//        easeUI.setUserProfileProvider(new EaseUI.EaseUserProfileProvider() {
//
//            @Override
//            public EaseUser getUser(String username) {
//                return getUserInfo(username);
//            }
//        });
//
//        //set options
//        easeUI.setSettingsProvider(new EaseUI.EaseSettingsProvider() {
//
//            @Override
//            public boolean isSpeakerOpened() {
//                return demoModel.getSettingMsgSpeaker();
//            }
//
//            @Override
//            public boolean isMsgVibrateAllowed(EMMessage message) {
//                return demoModel.getSettingMsgVibrate();
//            }
//
//            @Override
//            public boolean isMsgSoundAllowed(EMMessage message) {
//                return demoModel.getSettingMsgSound();
//            }
//
//            @Override
//            public boolean isMsgNotifyAllowed(EMMessage message) {
//                if (message == null) {
//                    return demoModel.getSettingMsgNotification();
//                }
//                if (!demoModel.getSettingMsgNotification()) {
//                    return false;
//                } else {
//                    String chatUsename = null;
//                    List<String> notNotifyIds = null;
//                    // get user or group id which was blocked to show message notifications
//                    if (message.getChatType() == EMMessage.ChatType.Chat) {
//                        chatUsename = message.getFrom();
//                        notNotifyIds = demoModel.getDisabledIds();
//                    } else {
//                        chatUsename = message.getTo();
//                        notNotifyIds = demoModel.getDisabledGroups();
//                    }
//
//                    if (notNotifyIds == null || !notNotifyIds.contains(chatUsename)) {
//                        return true;
//                    } else {
//                        return false;
//                    }
//                }
//            }
//        });
//        //set emoji icon provider
//        easeUI.setEmojiconInfoProvider(new EaseUI.EaseEmojiconInfoProvider() {
//
//            @Override
//            public EaseEmojicon getEmojiconInfo(String emojiconIdentityCode) {
//                EaseEmojiconGroupEntity data = EmojiconExampleGroupData.getData();
//                for (EaseEmojicon emojicon : data.getEmojiconList()) {
//                    if (emojicon.getIdentityCode().equals(emojiconIdentityCode)) {
//                        return emojicon;
//                    }
//                }
//                return null;
//            }
//
//            @Override
//            public Map<String, Object> getTextEmojiconMapping() {
//                return null;
//            }
//        });
//
////        //set notification options, will use default if you don't set it
////        easeUI.getNotifier().setNotificationInfoProvider(new EaseNotifier.EaseNotificationInfoProvider() {
////
////            @Override
////            public String getTitle(EMMessage message) {
////                //you can update title here
////                return null;
////            }
////
////            @Override
////            public int getSmallIcon(EMMessage message) {
////                //you can update icon here
////                return 0;
////            }
////
////            @Override
////            public String getDisplayedText(EMMessage message) {
////                // be used on notification bar, different text according the message type.
////                String ticker = EaseCommonUtils.getMessageDigest(message, appContext);
////                if (message.getType() == EMMessage.Type.TXT) {
////                    ticker = ticker.replaceAll("\\[.{2,3}\\]", "[表情]");
////                }
////                EaseUser user = getUserInfo(message.getFrom());
////                if (user != null) {
////                    if (EaseAtMessageHelper.get().isAtMeMsg(message)) {
////                        return String.format(appContext.getString(R.string.at_your_in_group), user.getNick());
////                    }
////                    return user.getNick() + ": " + ticker;
////                } else {
////                    if (EaseAtMessageHelper.get().isAtMeMsg(message)) {
////                        return String.format(appContext.getString(R.string.at_your_in_group), message.getFrom());
////                    }
////                    return message.getFrom() + ": " + ticker;
////                }
////            }
////
////            @Override
////            public String getLatestText(EMMessage message, int fromUsersNum, int messageNum) {
////                // here you can customize the text.
////                // return fromUsersNum + "contacts send " + messageNum + "messages to you";
////                return null;
////            }
////
////            @Override
////            public Intent getLaunchIntent(EMMessage message) {
////                // you can set what activity you want display when user click the notification
////                Intent intent = new Intent(appContext, ChatActivity.class);
////                // open calling activity if there is call
////                if (isVideoCalling) {
////                    intent = new Intent(appContext, VideoCallActivity.class);
////                } else if (isVoiceCalling) {
////                    intent = new Intent(appContext, VoiceCallActivity.class);
////                } else {
////                    EMMessage.ChatType chatType = message.getChatType();
////                    if (chatType == EMMessage.ChatType.Chat) { // single chat message
////                        intent.putExtra("userId", message.getFrom());
////                        intent.putExtra("chatType", Constant.CHATTYPE_SINGLE);
////                    } else { // group chat message
////                        // message.getTo() is the group id
////                        intent.putExtra("userId", message.getTo());
////                        if (chatType == EMMessage.ChatType.GroupChat) {
////                            intent.putExtra("chatType", Constant.CHATTYPE_GROUP);
////                        } else {
////                            intent.putExtra("chatType", Constant.CHATTYPE_CHATROOM);
////                        }
////
////                    }
////                }
////                return intent;
////            }
////        });
//    }
}

