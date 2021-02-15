package com.example.chronopassbluetoothterminal.controller;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chronopassbluetoothterminal.R;
import com.example.chronopassbluetoothterminal.database.DatabaseHelper;
import com.example.chronopassbluetoothterminal.model.Command;
import com.example.chronopassbluetoothterminal.model.Terminal;
import com.example.chronopassbluetoothterminal.utils.AppConstant;
import com.example.chronopassbluetoothterminal.utils.SharedPreferenceHelper;
import com.example.chronopassbluetoothterminal.view.NavigationDrawerActivity;
import com.example.chronopassbluetoothterminal.view.TerminalActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    private DatabaseHelper db;

    public TerminalController(TerminalActivity objTF) {
        this.objTF = objTF;
        setBluetoothCallbacks();

        this.configurationsList = new ArrayList<>();

        this.db = new DatabaseHelper(objTF);
        this.configurationsList.addAll(db.getAllCommands());

        loadSpinner();
        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(objTF, R.layout.style_spinner, this.spinnerArray);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.objTF.spCommands.setAdapter(mAdapter);

        loadTerminal();

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

    private void loadTerminal() {
        List<Terminal> terminalList = this.db.getAllTextTerminalDeviceAddress(objTF.device.getAddress());

        for (Terminal t : terminalList) {
            addTextToTerminal(t.getType(), t.getText(), t.getTimestamp());
        }
    }

    public void cleanTerminal() {
        this.db.deleteTextTerminal(objTF.device.getAddress());
        objTF.tvMessages.setText("");
    }

    public void onMessageSend(String msg) {
        int position = objTF.spCommands.getSelectedItemPosition();
        if (position == 0) objTF.etMsg.setText("");

        objTF.bluetooth.send(msg + delimiter);
        addTextToTerminal(1, msg);
    }

    public void addTextToTerminal(int type, String msg, String hour) {
        appendToTerminal(type, msg, hour);
    }

    public void addTextToTerminal(int type, String msg) {
        appendToTerminal(type, msg, getHour());
        this.db.insertTerminal(objTF.device.getAddress(), getHour(), msg, type);
    }

    private void appendToTerminal(int type, String msg, String hour) {
        String symbol = (type == 0) ? AppConstant.SYMBOL_TERMINAL_SYSTEM : ((type == 1) ?
                AppConstant.SYMBOL_TERMINAL_SEND : AppConstant.SYMBOL_TERMINAL_RECEIVE);
        int color = (type == 0) ? Color.WHITE : ((type == 1) ? Color.YELLOW : Color.GREEN);

        objTF.tvMessages.append("\n");
        int start = objTF.tvMessages.getText().length();
        String text = symbol + " " + hour + " " + msg;
        objTF.tvMessages.append(text);
        int end = objTF.tvMessages.getText().length();
        Spannable spannableText = (Spannable) objTF.tvMessages.getText();
        spannableText.setSpan(new ForegroundColorSpan(color), start, end, 0);
    }

    private String getHour() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        return dateFormat.format(new Date());
    }

    public String getCurrentTimeStamp() {
        return new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss").format(new Date());
    }

    private final DeviceCallback deviceCallback = new DeviceCallback() {
        @Override
        public void onDeviceConnected(BluetoothDevice device) {
            String msg = objTF.getString(R.string.tv_messages_connected) + "\n" +
                    device.getName() + " (" + device.getAddress() + ")";
            addTextToTerminal(0, msg);

            objTF.btSend.setEnabled(true);

            if (objTF.spCommands.getSelectedItemPosition() == 0) {
                objTF.etMsg.setEnabled(true);
            }

            objTF.menu.findItem(R.id.ic_menu_connect).setVisible(false);
            objTF.menu.findItem(R.id.ic_menu_disconnect).setVisible(true);
        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, String message) {
            String msg = objTF.getString(R.string.tv_messages_disconnected);
            addTextToTerminal(0, msg);

            objTF.btSend.setEnabled(false);
            objTF.etMsg.setEnabled(false);

            objTF.menu.findItem(R.id.ic_menu_connect).setVisible(true);
            objTF.menu.findItem(R.id.ic_menu_disconnect).setVisible(false);
        }

        @Override
        public void onMessage(byte[] message) {
            String str = new String(message);

            String msg = objTF.getString(R.string.tv_messages_command_received) + "\n" + str;
            addTextToTerminal(2, msg);
        }

        @Override
        public void onError(int errorCode) {

        }

        @Override
        public void onConnectError(final BluetoothDevice device, String message) {
            String msg = objTF.getString(R.string.tv_messages_no_connected);

            addTextToTerminal(0, msg);

            objTF.menu.findItem(R.id.ic_menu_connect).setVisible(true);
            objTF.menu.findItem(R.id.ic_menu_disconnect).setVisible(false);

//            new Handler(Looper.getMainLooper()).postDelayed(() -> {
//                if (objTF.bluetooth.isEnabled())
//                    objTF.bluetooth.connectToDevice(device);
//            }, 3000);
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

            objTF.etMsg.setFocusableInTouchMode(true);
            objTF.etMsg.setLongClickable(true);
        } else {
            hideSoftKeyboard(objTF.etMsg);
            objTF.etMsg.setFocusableInTouchMode(false);
            objTF.etMsg.setLongClickable(false);
            objTF.etMsg.clearFocus();

            String value = configurationsList.get(position - 1).getValue();
            String matrixLed = configurationsList.get(position - 1).getMatrixLed();
            int color = configurationsList.get(position - 1).getColor();


            String colorStr = (matrixLed != null) ?
                    (AppConstant.KEY_SEPARATOR_DEFAULT + matrixLed + AppConstant.KEY_SEPARATOR_DEFAULT + (Integer.toHexString(color).substring(2))) :
                    "";
            String text = value + colorStr;

            objTF.etMsg.setText(text);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    protected void hideSoftKeyboard(EditText input) {
        InputMethodManager imm = (InputMethodManager) objTF.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
    }

    public void saveLog() {
        String fileName = "log" + objTF.getString(R.string.app_name).replace(" ", "") + "_" + objTF.device.getAddress() + "_" + getCurrentTimeStamp() + ".txt";
        String text = objTF.tvMessages.getText().toString().trim();
        if (!TextUtils.isEmpty(text)) {
            try {
                File root = new File(Environment.getExternalStorageDirectory(), "");
                if (!root.exists()) {
                    root.mkdirs();
                }
                FileWriter writer = new FileWriter(new File(root, fileName));
                writer.append(text);
                writer.flush();
                writer.close();

                Toast.makeText(objTF, objTF.getString(R.string.toast_log_saved_successfully), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(objTF, objTF.getString(R.string.toast_log_error), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(objTF, objTF.getString(R.string.toast_log_error), Toast.LENGTH_SHORT).show();
        }
    }
}
