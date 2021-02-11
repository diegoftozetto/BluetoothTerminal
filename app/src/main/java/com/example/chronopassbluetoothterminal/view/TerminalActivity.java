package com.example.chronopassbluetoothterminal.view;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chronopassbluetoothterminal.R;
import com.example.chronopassbluetoothterminal.controller.TerminalController;
import com.example.chronopassbluetoothterminal.utils.AppConstant;
import com.example.chronopassbluetoothterminal.utils.DelimiterReader;

import me.aflak.bluetooth.Bluetooth;

public class TerminalActivity extends AppCompatActivity {

    private TerminalController objTC;

    public EditText etMsg;
    public Button btSend;
    public TextView tvMessages;
    public Spinner spCommands;

    public Bluetooth bluetooth;
    public BluetoothDevice device;

    public Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        this.device = getIntent().getParcelableExtra(AppConstant.EXTRA_TERMINAL_DEVICE);

        init();
    }

    private void init() {
        this.etMsg = findViewById(R.id.et_msg);
        this.etMsg.setEnabled(false);

        this.btSend = findViewById(R.id.bt_send);
        this.btSend.setEnabled(false);

        this.tvMessages = findViewById(R.id.tv_messages);
        this.tvMessages.setMovementMethod(new ScrollingMovementMethod());

        this.spCommands = findViewById(R.id.sp_commands);

        this.bluetooth = new Bluetooth(this);
        this.bluetooth.setReader(DelimiterReader.class);
        this.bluetooth.setCallbackOnUI(this);

        this.objTC = new TerminalController(this);
        this.btSend.setOnClickListener(objTC);
        this.spCommands.setOnItemSelectedListener(objTC);
    }

    @Override
    public void onStart() {
        super.onStart();
        bluetooth.onStart();
        if (bluetooth.isEnabled()) {
            bluetooth.connectToDevice(device);

            String msg = getString(R.string.tv_messages_connecting);
            this.objTC.addTextToTerminal(0, msg);
        } else {
            Intent intent = new Intent(this, NavigationDrawerActivity.class);
            this.startActivity(intent);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (bluetooth.isConnected()) {
            bluetooth.disconnect();
        }

        bluetooth.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_terminal, menu);
        menu.findItem(R.id.ic_menu_connect).setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            removeCallbacks();
            startActivity(new Intent(this, NavigationDrawerActivity.class));
            finish();
        } else if (item.getItemId() == R.id.ic_menu_connect) {
            bluetooth.connectToDevice(device);
            this.menu.findItem(R.id.ic_menu_connect).setVisible(false);
        } else if (item.getItemId() == R.id.ic_menu_clean_terminal) {
            this.objTC.cleanTerminal();
            tvMessages.setText("");
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        removeCallbacks();
    }

    private void removeCallbacks() {
        if (!bluetooth.isConnected()) {
            bluetooth.removeDeviceCallback();
            bluetooth.removeBluetoothCallback();
        }
    }
}