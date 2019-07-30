package com.example.xifei.usbtest.device;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.xifei.usbtest.R;
import com.example.xifei.usbtest.main.BaseChatActivity;

public class ChatActivity extends BaseChatActivity {
    private static final String TAG = "PAD";

    private AccessoryCommunicator communicator;
    // private Button mButton;
    // private EditText mEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        communicator = new AccessoryCommunicator(this) {

            @Override
            public void onReceive(byte[] payload, int length) {
                printLineToUI("host> " + new String(payload, 0, length));
            }

            @Override
            public void onError(String msg) {
                printLineToUI("notify" + msg);
            }

            @Override
            public void onConnected() {
                printLineToUI("connected");
            }

            @Override
            public void onDisconnected() {
                printLineToUI("disconnected");
            }
        };
        Log.e(TAG, "onCreate");
        mButton.setText("pad");
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String inputString = input.getText().toString();
                if (inputString.length() == 0) {
                    return;
                }
                sendString(inputString);
                printLineToUI(getString(R.string.local_prompt_device) + inputString);
                input.setText("");
            }
        });
    }


    @Override
    public void sendString(String string) {
        communicator.send(string.getBytes());
    }
}
