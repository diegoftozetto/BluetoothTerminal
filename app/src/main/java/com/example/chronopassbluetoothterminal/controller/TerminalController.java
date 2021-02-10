package com.example.chronopassbluetoothterminal.controller;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.example.chronopassbluetoothterminal.R;
import com.example.chronopassbluetoothterminal.database.DatabaseHelper;
import com.example.chronopassbluetoothterminal.model.Command;
import com.example.chronopassbluetoothterminal.utils.AppConstant;
import com.example.chronopassbluetoothterminal.utils.SharedPreferenceHelper;
import com.example.chronopassbluetoothterminal.view.NavigationDrawerActivity;
import com.example.chronopassbluetoothterminal.view.TerminalActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.aflak.bluetooth.interfaces.BluetoothCallback;
import me.aflak.bluetooth.interfaces.DeviceCallback;

public class TerminalController implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private final TerminalActivity objTF;

    private ArrayList<String> spinnerArray;

    private final List<Command> configurationsList;

    private final String delimiter;

    public TerminalController(TerminalActivity objTF) {
        this.objTF = objTF;
        setBluetoothCallbacks();

        this.configurationsList = new ArrayList<>();

        DatabaseHelper db = new DatabaseHelper(objTF);
        this.configurationsList.addAll(db.getAllConfigurations());

        loadSpinner();
        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(objTF, R.layout.style_spinner, this.spinnerArray);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.objTF.spCommands.setAdapter(mAdapter);

        this.delimiter = SharedPreferenceHelper.getSharedPreferenceString(objTF, AppConstant.KEY_DELIMITER_SEND, AppConstant.KEY_DELIMITER_DEFAULT);
    }


    private void setBluetoothCallbacks() {
        this.objTF.bluetooth.setDeviceCallback(deviceCallback);
        this.objTF.bluetooth.setBluetoothCallback(bluetoothCallback);
    }

    private void loadSpinner() {
        this.spinnerArray = new ArrayList<>();

        this.spinnerArray.add(objTF.getString(R.string.sp_item_terminal));
        for (int i = 0; i < configurationsList.size(); i++) {
            this.spinnerArray.add(configurationsList.get(i).getName());
        }
    }

    public void onMessageSend(String msg) {
        int position = objTF.spCommands.getSelectedItemPosition();
        if (position == 0) objTF.etMsg.setText("");

        objTF.bluetooth.send(msg + delimiter);
        appendToChat(">", msg, Color.YELLOW);
    }

    public void appendToChat(String symbol, String msg, int color) {
        objTF.tvMessages.append("\n");
        int start = objTF.tvMessages.getText().length();
        String text = symbol + " " + getHour() + " " + msg;
        objTF.tvMessages.append(text);
        int end = objTF.tvMessages.getText().length();
        Spannable spannableText = (Spannable) objTF.tvMessages.getText();
        spannableText.setSpan(new ForegroundColorSpan(color), start, end, 0);
    }

    private String getHour() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        return dateFormat.format(new Date());
    }

    private final DeviceCallback deviceCallback = new DeviceCallback() {
        @Override
        public void onDeviceConnected(BluetoothDevice device) {
            String msg = objTF.getString(R.string.tv_messages_connected) + "\n" +
                    device.getName() + " (" + device.getAddress() + ")";
            appendToChat("$", msg, Color.WHITE);

            objTF.btSend.setEnabled(true);

            if (objTF.spCommands.getSelectedItemPosition() == 0)
                objTF.etMsg.setEnabled(true);
        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, String message) {
            String msg = objTF.getString(R.string.tv_messages_disconnected);
            appendToChat("$", msg, Color.WHITE);

            objTF.btSend.setEnabled(false);
            objTF.etMsg.setEnabled(false);

            MenuItem item = objTF.menu.findItem(R.id.ic_menu_connect);
            item.setVisible(true);
        }

        @Override
        public void onMessage(byte[] message) {
            String str = new String(message);

            String msg = objTF.getString(R.string.tv_messages_command_received) + "\n" + str;
            appendToChat("<", msg, Color.GREEN);
        }

        @Override
        public void onError(int errorCode) {

        }

        @Override
        public void onConnectError(final BluetoothDevice device, String message) {
            String msg = objTF.getString(R.string.tv_messages_no_connected);

            appendToChat("$", msg, Color.WHITE);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (objTF.bluetooth.isEnabled())
                    objTF.bluetooth.connectToDevice(device);
            }, 3000);
        }
    };

    private final BluetoothCallback bluetoothCallback = new BluetoothCallback() {
        @Override
        public void onBluetoothTurningOn() {
        }

        @Override
        public void onBluetoothOn() {
        }

        @Override
        public void onBluetoothTurningOff() {
            startNavigationDrawerActivity();
        }

        @Override
        public void onBluetoothOff() {
            startNavigationDrawerActivity();
        }

        @Override
        public void onUserDeniedActivation() {
        }
    };

    private void startNavigationDrawerActivity() {
        Intent intent = new Intent(objTF, NavigationDrawerActivity.class);
        objTF.startActivity(intent);
        objTF.finish();
    }

    @Override
    public void onClick(View v) {
        String msg = objTF.etMsg.getText().toString().trim();
        if (!msg.isEmpty())
            onMessageSend(msg);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            objTF.etMsg.setEnabled(objTF.bluetooth.isConnected());
            objTF.etMsg.setText("");
        } else {
            objTF.etMsg.setEnabled(false);

            String value = configurationsList.get(position - 1).getValue();
            int color = configurationsList.get(position - 1).getColor();

            String colorStr = (color != -1) ? (AppConstant.KEY_SEPARATOR_DEFAULT + Integer.toHexString(color)) : "";
            String text = value + colorStr;

            objTF.etMsg.setText(text);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}