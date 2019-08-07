package io.xpush.sampleChat.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import io.xpush.chat.models.XPushChannel;
import io.xpush.sampleChat.R;
import io.xpush.sampleChat.fragments.ChatFragment;

public class ChatActivity extends AppCompatActivity{

    public static final String TAG = ChatActivity.class.getSimpleName();
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ChatFragment f = new ChatFragment();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.list, f, TAG).commit();
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        Bundle bundle = getIntent().getBundleExtra(XPushChannel.CHANNEL_BUNDLE);
        XPushChannel xpushChannel = new XPushChannel(bundle);

        if( xpushChannel.getUsers() != null ){
            if ( xpushChannel.getUsers().size() > 5 ) {
                mToolbar.setTitle( getString(R.string.title_text_group_chatting) + " " + xpushChannel.getUsers().size());
            } else if( xpushChannel.getUsers().size() > 2 ) {
                mToolbar.setTitle(xpushChannel.getName() + " (" + xpushChannel.getUsers().size() + ")");
            } else {
                mToolbar.setTitle(xpushChannel.getName()) ;
            }
        } else {
            mToolbar.setTitle(xpushChannel.getName()) ;
        }
        setSupportActionBar(mToolbar);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (null != intent) {
            Log.d(TAG, intent.toString());

            Bundle bundle = intent.getBundleExtra(XPushChannel.CHANNEL_BUNDLE);
            XPushChannel xpushChannel = new XPushChannel(bundle);
            Log.d(TAG, xpushChannel.toString());

            mToolbar.setTitle(xpushChannel.getName()+" (" + xpushChannel.getUsers().size() + ")") ;

            setIntent(intent);
        }
    }
}