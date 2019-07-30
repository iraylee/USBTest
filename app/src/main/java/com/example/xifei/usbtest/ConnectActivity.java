package com.example.xifei.usbtest;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.xifei.usbtest.main.Constants;
import com.example.xifei.usbtest.main.InfoActivity;

import java.util.HashMap;

import butterknife.ButterKnife;

public class ConnectActivity extends AppCompatActivity {
    private static final String TAG = "ConnectActivity";

    public static final String DEVICE_EXTRA_KEY = "device";
    private UsbManager mUsbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        Log.e(TAG, "onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");

        final HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();

        if (deviceList == null || deviceList.size() == 0) {
            Log.e(TAG, "onResume: startActivity");

            final Intent intent=new Intent(this,InfoActivity.class);
            startActivity(intent);

            finish();
            return;
        }

        if (searchForUsbAccessory(deviceList)) {
            Log.e(TAG, "onResume: searchForUsbAccessory");
            return;
        }

        for (UsbDevice device:deviceList.values()) {
            Log.e(TAG, "onResume: initAccessory");
            initAccessory(device);
        }

        finish();
    }

    private boolean searchForUsbAccessory(final HashMap<String, UsbDevice> deviceList) {
        Log.e(TAG, "searchForUsbAccessory");
        for (UsbDevice device:deviceList.values()) {
            if (isUsbAccessory(device)) {

                final Intent intent=new Intent(this,ChatActivity.class);
                intent.putExtra(DEVICE_EXTRA_KEY, device);
                startActivity(intent);

                finish();
                return true;
            }
        }

        return false;
    }

    private boolean isUsbAccessory(final UsbDevice device) {

        boolean value = (device.getProductId() == 0x2d00) || (device.getProductId() == 0x2d01);
        Log.e(TAG, "isUsbAccessory:" + value);
        return value;
    }

    private boolean initAccessory(final UsbDevice device) {
        Log.e(TAG, "initAccessory");
        final UsbDeviceConnection connection = mUsbManager.openDevice(device);

        if (connection == null) {
            return false;
        }

        initStringControlTransfer(connection, 0, "quandoo"); // MANUFACTURER
        initStringControlTransfer(connection, 1, "Android2AndroidAccessory"); // MODEL
        initStringControlTransfer(connection, 2, "showcasing android2android USB communication"); // DESCRIPTION
        initStringControlTransfer(connection, 3, "0.1"); // VERSION
        initStringControlTransfer(connection, 4, "http://quandoo.de"); // URI
        initStringControlTransfer(connection, 5, "42"); // SERIAL

        connection.controlTransfer(0x40, 53, 0, 0, new byte[]{}, 0, Constants.USB_TIMEOUT_IN_MS);

        connection.close();

        return true;
    }

    private void initStringControlTransfer(final UsbDeviceConnection deviceConnection,
                                           final int index,
                                           final String string) {
        Log.e(TAG, "initStringControlTransfer");
        deviceConnection.controlTransfer(0x40, 52, 0, index, string.getBytes(), string.length(), Constants.USB_TIMEOUT_IN_MS);
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy: xxx");
        super.onDestroy();
    }
}
