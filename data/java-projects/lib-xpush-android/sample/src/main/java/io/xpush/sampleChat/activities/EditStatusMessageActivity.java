package io.xpush.sampleChat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import io.xpush.chat.core.XPushCore;
import io.xpush.chat.util.ContentUtils;
import io.xpush.chat.util.XPushUtils;
import io.xpush.sampleChat.R;

public class EditStatusMessageActivity extends FragmentActivity implements TextWatcher{

    private EditText mStatusMessage = null;
    private TextView mTextCount;

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_edit_status_message);

        mStatusMessage = ((EditText)findViewById(R.id.status_message));
        mTextCount = ((TextView)findViewById(R.id.text_count));

        mStatusMessage.setText(XPushCore.getXpushSession().getMessage() );
        mTextCount.setText( ContentUtils.getInputStringLength(XPushCore.getXpushSession().getMessage(), 20) );
        mStatusMessage.setSelection(mStatusMessage.getText().length());

        mStatusMessage.addTextChangedListener(this);
        mStatusMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int paramInt, KeyEvent paramKeyEvent) {

                if ((paramInt == EditorInfo.IME_ACTION_DONE ) || ((paramKeyEvent != null) && (paramKeyEvent.getKeyCode() == 66))) {
                    Intent intent = new Intent();
                    intent.putExtra("statusMessage", mStatusMessage.getText().toString());
                    setResult(RESULT_OK, intent);
                    finish();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        String strLen = ContentUtils.getInputStringLength(editable.toString(), 20);
        mTextCount.setText( strLen );
    }
}
