package io.xpush.chat.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.socket.emitter.Emitter;
import io.socket.engineio.client.EngineIOException;
import io.socket.client.Socket;
import io.socket.client.SocketIOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.xpush.chat.R;
import io.xpush.chat.core.CallbackEvent;
import io.xpush.chat.core.ChannelCore;
import io.xpush.chat.core.XPushCore;
import io.xpush.chat.loaders.MessageDataLoader;
import io.xpush.chat.models.XPushChannel;
import io.xpush.chat.models.XPushMessage;
import io.xpush.chat.models.XPushSession;
import io.xpush.chat.models.XPushUser;
import io.xpush.chat.persist.ChannelTable;
import io.xpush.chat.persist.DBHelper;
import io.xpush.chat.persist.MessageTable;
import io.xpush.chat.persist.XPushMessageDataSource;
import io.xpush.chat.persist.XpushContentProvider;
import io.xpush.chat.services.BadgeIntentService;
import io.xpush.chat.services.PushMsgReceiver;
import io.xpush.chat.view.adapters.MessageListAdapter;
import io.xpush.chat.view.listeners.RecyclerOnScrollListener;

public abstract class XPushChatFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<XPushMessage>>{

    public static final String TAG = XPushChatFragment.class.getSimpleName();
    protected static final int INIT_VIEW_COUNT = 16;

    private int mViewCount;

    private RecyclerView mRecyclerView;
    private EditText mInputMessageView;
    protected MessageListAdapter mAdapter;
    private boolean mTyping = false;

    private String mUserId;
    private String mUsername;

    private XPushChannel mXpushChannel;

    protected String mChannel;
    protected Activity mActivity;
    protected ArrayList<String> mUsers = new ArrayList<String>();

    private XPushSession mSession;

    private SQLiteDatabase mDatabase;
    private XPushMessageDataSource mDataSource;
    private DBHelper mDbHelper;

    private List<XPushMessage> mXpushMessages = new ArrayList<XPushMessage>();
    protected ArrayList<XPushUser> mXpushUsers = new ArrayList<XPushUser>();

    private MessageDataLoader mDataLoader;
    private RecyclerOnScrollListener mOnScrollListener;

    private LinearLayoutManager mLayoutManager;

    protected ChannelCore mChannelCore;

    public XPushChatFragment() {
        super();
    }

    private long lastReceiveTime = 0L;

    protected boolean newChannelFlag;
    protected boolean resetChannelFlag;
    protected boolean restartLoader;

    private Bundle mChannelBundle;

    private Bundle mSavedInstanceState;


    private AtomicBoolean isPaused = new AtomicBoolean();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mAdapter = new MessageListAdapter(activity, mXpushMessages);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewCount = INIT_VIEW_COUNT;
        mActivity = getActivity();

        mDbHelper = new DBHelper(mActivity);
        mDatabase = mDbHelper.getWritableDatabase();
        mDataSource = new XPushMessageDataSource(mDatabase, getActivity().getString(R.string.message_table_name), getActivity().getString(R.string.user_table_name));

        mSession = XPushCore.getXpushSession();
        newChannelFlag = mActivity.getIntent().getBooleanExtra("newChannel", false);

        if( savedInstanceState == null ){
            mChannelBundle = mActivity.getIntent().getBundleExtra(XPushChannel.CHANNEL_BUNDLE);

        } else {
            mChannelBundle = savedInstanceState.getBundle(XPushChannel.CHANNEL_BUNDLE);
            mViewCount = savedInstanceState.getInt("mViewCount");
            if( mDataLoader == null  ){

                resetChannelFlag = true;
                restartLoader = true;
            }
        }

        mXpushChannel = new XPushChannel(mChannelBundle);
        mChannel = mXpushChannel.getId();

        if (null == mXpushChannel.getName()) {

            Uri singleUri = Uri.parse(XpushContentProvider.CHANNEL_CONTENT_URI + "/" + mChannel);
            Cursor cursor = mActivity.getContentResolver().query(singleUri, ChannelTable.ALL_PROJECTION, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                mXpushChannel = new XPushChannel(cursor);
                newChannelFlag = false;
            }
        }

        mUserId = mSession.getId();
        mUsername = mSession.getName();
        setHasOptionsMenu(true);

        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        getLoaderManager().initLoader(0, null, this);

        mSavedInstanceState = savedInstanceState;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG, " ===== resume ===== ");
        isPaused.set(true);

        newChannelFlag = mActivity.getIntent().getBooleanExtra("newChannel", false);
        if( !resetChannelFlag ) {
            resetChannelFlag = mActivity.getIntent().getBooleanExtra("resetChannel", false);
        }

        Log.d(TAG, "resetChannelFlag : " + resetChannelFlag);
        Log.d(TAG, "newChannelFlag : "+newChannelFlag );
        Log.d(TAG, "restartLoader : "+restartLoader );

        if( resetChannelFlag ) {
            mViewCount = INIT_VIEW_COUNT;

            mChannelBundle = mActivity.getIntent().getBundleExtra(XPushChannel.CHANNEL_BUNDLE);
            mXpushChannel = new XPushChannel(mChannelBundle);

            mChannel = mXpushChannel.getId();
            mUsers = mXpushChannel.getUsers();

            mActivity.getIntent().putExtra("resetChannel", false);
            mXpushMessages.clear();

            String selection = MessageTable.KEY_CHANNEL + "='" + mChannel +"'";
            String sortOrder = MessageTable.KEY_UPDATED + " DESC LIMIT " + String.valueOf(mViewCount);

            mDataLoader = new MessageDataLoader(mActivity, mDataSource, selection, null, null, null, sortOrder);

            if( restartLoader ) {
                mXpushMessages.clear();
                mAdapter.notifyDataSetChanged();
                getLoaderManager().restartLoader(0, null, this);
                getChannelAndConnect();
            } else {
                createChannelAndConnect(mXpushChannel);
            }
        } else if( newChannelFlag ){
            createChannelAndConnect(mXpushChannel);
        } else if( mChannelCore == null || !mChannelCore.connected() ) {

            if( mChannelCore != null ) {
                connectChannel();
            } else {
                getChannelAndConnect();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, " ===== pause ===== ");
        isPaused.set(false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.messages);
        mLayoutManager = new LinearLayoutManager(getActivity());

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mInputMessageView = (EditText) view.findViewById(R.id.message_input);
        mInputMessageView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if (id == R.id.send || id == EditorInfo.IME_NULL) {
                    attemptSend();
                    return true;
                }
                return false;
            }
        });

        mInputMessageView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (null == mUsername) return;
                if (mChannelCore == null || !mChannelCore.connected()) return;

                if (!mTyping) {
                    mTyping = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (mInputMessageView.hasFocus() && oldBottom > bottom) {
                    mRecyclerView.scrollBy(0, oldBottom - bottom);
                }
            }
        });

        Button sendButton = (Button) view.findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSend();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_chat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_leave) {

            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle( mActivity.getString(R.string.action_leave_dialog_title))
                    .setMessage( mActivity.getString(R.string.action_leave_dialog_description))
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            channelLeave();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }

                    });

            AlertDialog dialog = builder.create();    // 알림창 객체 생성
            dialog.show();    // 알림창 띄우기

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void refreshContent(){

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                XPushMessage message = mXpushMessages.get(0);

                mViewCount = mViewCount * 2;
                String selection = MessageTable.KEY_CHANNEL + "='" + mChannel + "' and " + MessageTable.KEY_UPDATED + " < " + message.getUpdated();
                String sortOrder = MessageTable.KEY_UPDATED + " DESC LIMIT " + mViewCount;

                mDataLoader.setSelection(selection);
                mDataLoader.setSortOrder(sortOrder);

                List<XPushMessage> messages = mDataLoader.loadInBackground();
                mDataLoader.deliverResult(messages);
            }
        }, 1);
    }

    private void connectChannel() {

        HashMap<String, Emitter.Listener> events = new HashMap<>();

        events.put(Socket.EVENT_CONNECT_ERROR, onConnectError);
        events.put(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        events.put(Socket.EVENT_CONNECT, onConnectSuccess);
        events.put("message", onMessage);

        mChannelCore.connect(events);
    }

    private void addMessage(XPushMessage xpushMessage) {
        mXpushMessages.add(xpushMessage);
        mAdapter.notifyItemInserted(mXpushMessages.size() - 1);
        scrollToBottom();
    }

    private void attemptSend() {
        if (null == mUserId) return;
        if (mChannelCore == null || !mChannelCore.connected()) return;

        mTyping = false;

        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            mInputMessageView.requestFocus();
            return;
        }

        mInputMessageView.setText("");
        mChannelCore.sendMessage(message);
    }

    private void leave() {

        mUsername = null;
        mChannelCore.disconnect();

        Uri channelUri = Uri.parse(XpushContentProvider.CHANNEL_CONTENT_URI + "/" + mChannel );
        mActivity.getContentResolver().delete(channelUri, null, null);

        Uri messageUri = Uri.parse(XpushContentProvider.MESSAGE_CONTENT_URI + "/" + mChannel );
        mActivity.getContentResolver().delete(messageUri, null, null);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mActivity.finish();
            }
        }, 150);
    }

    private Emitter.Listener onConnectSuccess = new Emitter.Listener() {
        @Override

        public void call(Object... args) {
            ContentValues values = new ContentValues();
            values.put(ChannelTable.KEY_ID, mChannel );
            values.put(ChannelTable.KEY_COUNT, 0);

            // Reset Unread Message Count
            Uri singleUri = Uri.parse(XpushContentProvider.CHANNEL_CONTENT_URI + "/" + mChannel );
            mActivity.getContentResolver().update(singleUri, values, null, null);

            // Reset Badge
            Intent intent = new Intent(mActivity,BadgeIntentService.class);
            mActivity.startService(intent);

            mChannelCore.channelGet(new CallbackEvent() {
                @Override
                public void call(Object... args) {
                    if( args != null && args.length > 0 && args[0] != null) {
                        JSONObject response = (JSONObject) args[0];

                        if( response.has("result") ){
                            try {
                                JSONArray dts = response.getJSONObject("result").getJSONArray("UDTS");

                                mXpushUsers.clear();
                                mUsers.clear();
                                for( int inx = 0 ; inx < dts.length() ; inx++ ){
                                    XPushUser tmpUser = new XPushUser(dts.getJSONObject(inx));
                                    mXpushUsers.add(new XPushUser(dts.getJSONObject(inx)));
                                    mUsers.add( tmpUser.getId() );
                                }

                                sendInviteMessage();

                            } catch (Exception e ){
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }
    };

    private void sendInviteMessage(){

        if( (newChannelFlag || resetChannelFlag ) && ( mXpushUsers != null && mXpushUsers.size()  > 2 ) ){

            if( mSavedInstanceState == null || ( mSavedInstanceState != null && !mSavedInstanceState.getBoolean("invitedSuccess", false) ) ) {

                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String message = mUsername + " Invite " + mXpushChannel.getName();
                        mXpushChannel.getUserNames().add(XPushCore.getXpushSession().getName());
                        mChannelCore.sendMessage(message, "IN", mUsers);
                    }
                }, 150);

                if( !newChannelFlag ) {
                    mActivity.getIntent().putExtra("resetChannel", false);
                    resetChannelFlag = false;
                }
            }

        }
    }

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            Log.d(TAG, "error");

            if( args[0] instanceof SocketIOException) {
                SocketIOException e = (SocketIOException) args[0];
                Log.d(TAG, e.getMessage());
            } else if ( args[0] instanceof EngineIOException){
                EngineIOException e = (EngineIOException) args[0];
                Log.d(TAG, e.getMessage());
            }

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mActivity, R.string.error_connect, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            saveMessage(data);
        }
    };

    private void disconnect(){
        if( mChannelCore != null && mChannelCore.connected() ) {
            mChannelCore.disconnect();
        }
    }

    @Override
    public Loader<List<XPushMessage>> onCreateLoader(int id, Bundle args) {

        String selection = MessageTable.KEY_CHANNEL + "='" + mChannel +"'";
        String sortOrder = MessageTable.KEY_UPDATED + " DESC LIMIT " + String.valueOf(mViewCount);

        mDataLoader = new MessageDataLoader(mActivity, mDataSource, selection, null, null, null, sortOrder);
        return mDataLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<XPushMessage>> loader, List<XPushMessage> data) {

        boolean onStarting = false;
        if( mXpushMessages.size() == 0 ){
            onStarting = true;
        }

        Collections.sort(data, new TimestampAscCompare());


        if(  mXpushMessages.size() ==  0 ) {
            mXpushMessages.addAll(0, data);
        } else {

            // Prevent dulplicate
            if( mXpushMessages.size() > 0 && data.size() > 0 && mXpushMessages.get(0).getUpdated() != data.get(0).getUpdated() ){
                mXpushMessages.addAll(0, data);
            }
        }

        mAdapter.notifyDataSetChanged();

        if( onStarting ) {
            mOnScrollListener = new RecyclerOnScrollListener(mLayoutManager, RecyclerOnScrollListener.RecylclerDirection.UP) {
                @Override
                public void onLoadMore(int current_page) {
                    Log.d(TAG, " onLoadMore : "+  current_page);
                    refreshContent();
                }
            };

            scrollToBottom();
            mRecyclerView.addOnScrollListener(mOnScrollListener);

        } else {
            mRecyclerView.scrollToPosition(mXpushMessages.size() - data.size() + 1);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<XPushMessage>> loader) {
        mXpushMessages.clear();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {

        if( mChannelCore != null && mChannelCore.connected() ) {
            disconnect();
        }

        super.onDestroy();
        mDbHelper.close();
        mDatabase.close();
        mDataSource = null;
        mDbHelper = null;
        mDatabase = null;
    }

    private void scrollToBottom() {
        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    static class TimestampAscCompare implements Comparator<XPushMessage> {
        public int compare(XPushMessage arg0, XPushMessage arg1) {
            return arg0.getUpdated() < arg1.getUpdated() ? -1 : arg0.getUpdated() > arg1.getUpdated() ? 1:0;
        }
    }

    private void saveMessage( JSONObject data ){

        Log.d(TAG, "onMessage : " + data.toString());
        final XPushMessage xpushMessage = new XPushMessage( data );

        try {
            ContentValues values = new ContentValues();

            if (xpushMessage.getType() == XPushMessage.TYPE_INVITE) {
                xpushMessage.setType(XPushMessage.TYPE_INVITE);
                values.put(ChannelTable.KEY_USERS, TextUtils.join("@!@", xpushMessage.getUsers()));

                if( newChannelFlag ) {
                    values.put(ChannelTable.KEY_NAME, mXpushChannel.getName());
                }
            } else if ( xpushMessage.getType() == XPushMessage.TYPE_LEAVE){
                xpushMessage.setType(XPushMessage.TYPE_LEAVE);
            } else {
                if (mSession.getId().equals(xpushMessage.getSenderId())) {
                    if( xpushMessage.getType() == XPushMessage.TYPE_IMAGE ) {
                        xpushMessage.setType(XPushMessage.TYPE_SEND_IMAGE);
                    } else {
                        xpushMessage.setType(XPushMessage.TYPE_SEND_MESSAGE);
                    }
                } else {
                    if( xpushMessage.getType() == XPushMessage.TYPE_IMAGE ) {
                        xpushMessage.setType(XPushMessage.TYPE_RECEIVE_IMAGE);
                    } else {
                        xpushMessage.setType(XPushMessage.TYPE_RECEIVE_MESSAGE);
                    }
                }
            }

            if( mUsers != null && mUsers.size() > 2 ) {
                values.put(ChannelTable.KEY_NAME, mXpushChannel.getName());
            } else {
                if (XPushMessage.TYPE_SEND_MESSAGE == xpushMessage.getType() || XPushMessage.TYPE_SEND_IMAGE == xpushMessage.getType() ) {
                    values.put(ChannelTable.KEY_NAME, mXpushChannel.getName());
                    values.put(ChannelTable.KEY_IMAGE, mXpushChannel.getImage());
                } else if (XPushMessage.TYPE_RECEIVE_MESSAGE == xpushMessage.getType() || XPushMessage.TYPE_RECEIVE_IMAGE == xpushMessage.getType() ) {
                    values.put(ChannelTable.KEY_NAME, xpushMessage.getSenderName());
                    values.put(ChannelTable.KEY_IMAGE, xpushMessage.getImage());
                }
            }

            values.put(ChannelTable.KEY_ID, xpushMessage.getChannel());
            values.put(ChannelTable.KEY_MESSAGE, xpushMessage.getMessage());
            values.put(ChannelTable.KEY_MESSAGE_TYPE, xpushMessage.getType());
            values.put(ChannelTable.KEY_UPDATED, xpushMessage.getUpdated());

            PowerManager pm = (PowerManager) mActivity.getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = false;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT_WATCH) {
                isScreenOn = pm.isInteractive();
            } else {
                isScreenOn = pm.isScreenOn();
            }

            // Update channel count
            if( !isScreenOn || !isPaused.get() ) {

                Uri singleUri = Uri.parse(XpushContentProvider.CHANNEL_CONTENT_URI + "/" + xpushMessage.getChannel());
                Cursor cursor = mActivity.getContentResolver().query(singleUri, ChannelTable.ALL_PROJECTION, null, null, null);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    int count = cursor.getInt(cursor.getColumnIndexOrThrow(ChannelTable.KEY_COUNT));
                    values.put(ChannelTable.KEY_COUNT, count + 1);
                } else {
                    values.put(ChannelTable.KEY_COUNT, 1);
                }

            } else {
                values.put(ChannelTable.KEY_COUNT, 0);
            }

            values.put(XpushContentProvider.SQL_INSERT_OR_REPLACE, true);
            mActivity.getContentResolver().insert(XpushContentProvider.CHANNEL_CONTENT_URI, values);

            // INSERT INTO MESSAGE TABLE
            lastReceiveTime = xpushMessage.getUpdated();
            mDataSource.insert(xpushMessage);

            newChannelFlag = false;

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addMessage(xpushMessage);
                }
            });

            if( !isScreenOn || !isPaused.get() ) {
                Intent broadcastIntent = new Intent(mActivity, PushMsgReceiver.class);
                broadcastIntent.setAction("io.xpush.chat.MGRECVD");

                broadcastIntent.putExtra("rcvd.C", xpushMessage.getChannel());
                broadcastIntent.putExtra("rcvd.NM", xpushMessage.getSenderName());
                broadcastIntent.putExtra("rcvd.MG", xpushMessage.getMessage());
                broadcastIntent.putExtra("rcvd.TP", xpushMessage.getType() );

                mActivity.sendBroadcast(broadcastIntent);
            }

        } catch (Exception e ){
            e.printStackTrace();
        }
    }

    private void createChannelAndConnect(XPushChannel xpushChannel){
        XPushCore.createChannel(xpushChannel, new CallbackEvent() {
            @Override
            public void call(Object... args) {
                if (args[0] != null) {
                    mChannelCore = (ChannelCore) args[0];

                    // newChannelFlag 를 false로
                    mActivity.getIntent().putExtra("newChannel", false);
                    connectChannel();
                }
            }
        });
    }

    private void getChannelAndConnect(){

        XPushCore.getChannel(mActivity, mChannel, new CallbackEvent() {
            @Override
            public void call(Object... args) {
                if (args[0] != null) {
                    mChannelCore = (ChannelCore) args[0];
                    connectChannel();
                }
            }
        });
    }

    //add unread message
    private void messageUnread(){
        mChannelCore.getMessageUnread(lastReceiveTime, new CallbackEvent() {
            @Override
            public void call(Object... args) {
                if (args != null && args.length > 0 && args[0] != null) {
                    JSONArray messages = (JSONArray) args[0];
                    try {
                        for (int inx = 0; inx < messages.length(); inx++) {
                            saveMessage(messages.getJSONObject(inx));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    protected void channelLeave(){

        // 1:1 Channel, only delete local data
        if( mUsers != null && mUsers.size() > 2 ){
            mChannelCore.channelLeave(new CallbackEvent() {
                @Override
                public void call(Object... args) {

                    // SEND leave message
                    mChannelCore.sendMessage(mUsername + " leave this chat room", "OUT");
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            leave();
                        }
                    }, 150);
                }
            });
        } else {
            leave();
        }
    }

    protected ArrayList<String> getImageList(){
        String selection = MessageTable.KEY_CHANNEL + "='" + mChannel + "' and " + "a."+MessageTable.KEY_TYPE + " IN ("+XPushMessage.TYPE_RECEIVE_IMAGE+","+XPushMessage.TYPE_SEND_IMAGE+" ) ";
        String sortOrder = MessageTable.KEY_UPDATED;

        ArrayList<String> images = new ArrayList<String>();
        List<XPushMessage> messages = mDataSource.read(selection, null, null, null, sortOrder);
        for( XPushMessage message : messages  ){
            images.add( message.getMessage() );
        }
        return images;
    }


    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);

        savedState.putInt("mViewCount", mViewCount);
        savedState.putBundle(XPushChannel.CHANNEL_BUNDLE, mChannelBundle);
    }
}