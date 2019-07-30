package com.example.xifei.usbtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.xifei.usbtest.main.BaseChatActivity;
import com.example.xifei.usbtest.main.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class ChatActivity extends BaseChatActivity {

    private static final String TAG = "TV";

    private final AtomicBoolean keepThreadAlive = new AtomicBoolean(true);
    private final List<String> sendBuffer = new ArrayList<>();

    UsbInterface usbInterface;
    UsbDeviceConnection connection;

    @Override
    public void sendString(final String string) {
        sendBuffer.add(string);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(new CommunicationRunnable()).start();
        mButton.setText("Tv");
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String inputString = input.getText().toString();
                if (inputString.length() == 0) {
                    return;
                }
                sendString(inputString);
                printLineToUI(getString(R.string.local_prompt) + inputString);
                input.setText("");
            }
        });
    }

    // @Override
    // protected void onStart() {
    //     super.onStart();
    //     IntentFilter it = new IntentFilter();
    //     it.addAction("android.hardware.usb.action.USB_ACCESSORY_DETACHED");
    //     registerReceiver(mUsbReceiver, it);
    // }

    private class CommunicationRunnable implements Runnable {

        @Override
        public void run() {
            final UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

            final UsbDevice device=getIntent().getParcelableExtra(ConnectActivity.DEVICE_EXTRA_KEY);

            UsbEndpoint endpointIn = null;
            UsbEndpoint endpointOut = null;

            UsbInterface usbInterface = device.getInterface(0);

            for (int i = 0; i < device.getInterface(0).getEndpointCount(); i++) {

                final UsbEndpoint endpoint = device.getInterface(0).getEndpoint(i);
                if (endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                    endpointIn = endpoint;
                }
                if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                    endpointOut = endpoint;
                }

            }

            if (endpointIn == null) {
                printLineToUI("Input Endpoint not found");
                return;
            }

            if (endpointOut == null) {
                printLineToUI("Output Endpoint not found");
                return;
            }

            connection = usbManager.openDevice(device);

            if (connection == null) {
                printLineToUI("Could not open device");
                return;
            }

            final boolean claimResult = connection.claimInterface(usbInterface, true);

            if (!claimResult) {
                printLineToUI("Could not claim device");
            } else {
                final byte buff[] = new byte[Constants.BUFFER_SIZE_IN_BYTES];
                printLineToUI("Claimed interface - ready to communicate");

                while (keepThreadAlive.get()) {
                    final int bytesTransferred = connection.bulkTransfer(endpointIn, buff, buff.length, Constants.USB_TIMEOUT_IN_MS);
                    if (bytesTransferred > 0) {
                        printLineToUI("device> "+new String(buff, 0, bytesTransferred));
                    }

                    synchronized (sendBuffer) {
                        if (sendBuffer.size()>0) {
                            // final byte[] sendBuff=sendBuffer.get(0).toString().getBytes();
                            final byte[] sendBuff=sendBuffer.get(0).getBytes();
                            connection.bulkTransfer(endpointOut, sendBuff, sendBuff.length, Constants.USB_TIMEOUT_IN_MS);
                            sendBuffer.remove(0);
                        }
                    }
                }
            }

            connection.releaseInterface(usbInterface);
            connection.close();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        keepThreadAlive.set(true);
    }

    @Override
    protected void onDestroy() {
        Toast.makeText(this, "应用终止", Toast.LENGTH_LONG).show();
        Log.e(TAG, "onDestroy");
        super.onDestroy();
    }

    // BroadcastReceiver mUsbReceiver=new BroadcastReceiver(){
    //     public void onReceive (Context context, Intent intent) {
    //         String action=intent.getAction ();
    //         if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals (action)) {
    //             UsbAccessory accessory= (UsbAccessory) intent.getParcelableExtra (UsbManager.EXTRA_DEVICE);
    //             if (accessory !=null) {
    //                 keepThreadAlive.set(false);
    //                 Log.e(TAG, "onReceive: 收到断开广播");
    //                 finish();
    //             }
    //             finish();
    //         }
    //     }
    // };
}
