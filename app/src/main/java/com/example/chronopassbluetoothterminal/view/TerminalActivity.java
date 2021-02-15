package com.example.chronopassbluetoothterminal.view;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.ClipboardManager;
import android.content.Context;
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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chronopassbluetoothterminal.R;
import com.example.chronopassbluetoothterminal.controller.TerminalController;
import com.example.chronopassbluetoothterminal.utils.AppConstant;
import com.example.chronopassbluetoothterminal.utils.DelimiterReader;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

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
            this.objTC.addTextToTerminal(0, getString(R.string.tv_messages_connecting));
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
        menu.findItem(R.id.ic_menu_disconnect).setVisible(false);
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
            this.objTC.addTextToTerminal(0, getString(R.string.tv_messages_connecting));

            this.menu.findItem(R.id.ic_menu_connect).setVisible(false);
        } else if (item.getItemId() == R.id.ic_menu_disconnect) {
            bluetooth.disconnect();
            this.menu.findItem(R.id.ic_menu_connect).setVisible(true);
        } else if (item.getItemId() == R.id.ic_menu_clean_terminal) {
            this.objTC.cleanTerminal();
            tvMessages.setText("");
        } else if (item.getItemId() == R.id.ic_menu_copy) {
            ClipboardManager clipMan = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipMan.setText(tvMessages.getText().toString().trim());
            Toast.makeText(this, getString(R.string.toast_terminal_copied), Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.ic_menu_save_log) {
            checkPermissions();
        }
        return true;
    }

    void checkPermissions() {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

        Dexter.withActivity(this).withPermissions(
                permissions)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            objTC.saveLog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
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