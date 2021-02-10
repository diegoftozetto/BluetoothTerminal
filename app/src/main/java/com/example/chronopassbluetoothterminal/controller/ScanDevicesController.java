package com.example.chronopassbluetoothterminal.controller;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chronopassbluetoothterminal.R;
import com.example.chronopassbluetoothterminal.model.Device;
import com.example.chronopassbluetoothterminal.utils.AppConstant;
import com.example.chronopassbluetoothterminal.utils.RecyclerTouchListener;
import com.example.chronopassbluetoothterminal.utils.ScanDeviceAdapter;
import com.example.chronopassbluetoothterminal.view.TerminalActivity;
import com.example.chronopassbluetoothterminal.view.ui.ScanDevicesFragment;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;
import java.util.List;

import me.aflak.bluetooth.interfaces.BluetoothCallback;
import me.aflak.bluetooth.interfaces.DiscoveryCallback;

public class ScanDevicesController {

    private final ScanDevicesFragment objSDF;
    private final Context context;

    //Paired Devices
    private final List<Device> pairedDeviceList;
    private List<BluetoothDevice> pairedDevices;

    private ScanDeviceAdapter mAdapterPairedDevices;

    //Unpaired Devices
    private boolean scanning;

    private List<Device> unpairedDeviceList;
    private List<BluetoothDevice> unpairedDevicesBluetooth;

    private ScanDeviceAdapter mAdapterUnpairedDevices;

    private AlertDialog alertDialog;
    private ProgressBar pbScanDevices;
    private ImageView ivUnpairedDevices;
    private TextView tvUnpairedDevices;

    public ScanDevicesController(ScanDevicesFragment objSDF, Context context) {
        this.objSDF = objSDF;
        this.context = context;

        this.pairedDeviceList = new ArrayList<>();
        this.scanning = false;

        this.initRecyclerView();
        this.setBluetoothCallbacks();
    }

    private void initRecyclerView() {
        this.mAdapterPairedDevices = new ScanDeviceAdapter(objSDF.getActivity(), pairedDeviceList);
        this.objSDF.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        this.objSDF.recyclerView.setItemAnimator(new DefaultItemAnimator());
        this.objSDF.recyclerView.addItemDecoration(new DividerItemDecoration(objSDF.recyclerView.getContext(), LinearLayoutManager.VERTICAL));
        this.objSDF.recyclerView.setAdapter(mAdapterPairedDevices);

        this.objSDF.recyclerView.addOnItemTouchListener(new RecyclerTouchListener(objSDF.getActivity(),
                objSDF.recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                if (scanning) {
                    objSDF.bluetooth.stopScanning();
                    setScanning(false);
                }
                startTerminalActivity(pairedDevices.get(position));
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));
    }

    private void setBluetoothCallbacks() {
        this.objSDF.bluetooth.setCallbackOnUI(objSDF.getActivity());
        this.objSDF.bluetooth.setBluetoothCallback(bluetoothCallback);
        this.objSDF.bluetooth.setDiscoveryCallback(discoveryCallback);
    }

    private final BluetoothCallback bluetoothCallback = new BluetoothCallback() {
        @Override
        public void onBluetoothTurningOn() {
        }

        @Override
        public void onBluetoothOn() {
            loadPairedDevices();
            objSDF.menu.findItem(R.id.ic_menu_scan).setVisible(true);
        }

        @Override
        public void onBluetoothTurningOff() {
            objSDF.menu.findItem(R.id.ic_menu_scan).setVisible(false);
        }

        @Override
        public void onBluetoothOff() {
            pairedDeviceList.clear();
            mAdapterPairedDevices.notifyDataSetChanged();
            toggleEmptyDevices();

            if (alertDialog != null) {
                setScanning(false);
                alertDialog.dismiss();
            }

            objSDF.bluetooth.showEnableDialog(objSDF.getActivity());
        }

        @Override
        public void onUserDeniedActivation() {
        }
    };

    private final DiscoveryCallback discoveryCallback = new DiscoveryCallback() {
        @Override
        public void onDiscoveryStarted() {
            unpairedDevicesBluetooth = new ArrayList<>();
            unpairedDeviceList = new ArrayList<>();
            showDevicesDialog();
            pbScanDevices.setVisibility(View.VISIBLE);
            setScanning(true);
        }

        @Override
        public void onDiscoveryFinished() {
            Toast.makeText(context, context.getString(R.string.toast_scanning_finished), Toast.LENGTH_SHORT).show();
            pbScanDevices.setVisibility(View.INVISIBLE);

            setScanning(false);
            if (unpairedDeviceList.isEmpty()) {
                tvUnpairedDevices.setText(context.getString(R.string.tv_unpaired_devices));
                ivUnpairedDevices.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_not_found_unpaired_devices));
                ivUnpairedDevices.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onDeviceFound(BluetoothDevice device) {
            tvUnpairedDevices.setText("");
            ivUnpairedDevices.setVisibility(View.INVISIBLE);
            if (!pairedDevices.contains(device)) {
                unpairedDevicesBluetooth.add(device);
                addListDevices(device, false);
            }
        }

        @Override
        public void onDevicePaired(BluetoothDevice device) {
            Toast.makeText(context, context.getString(R.string.toast_paired), Toast.LENGTH_SHORT).show();

            setScanning(false);
            loadPairedDevices();
            alertDialog.dismiss();
        }

        @Override
        public void onDeviceUnpaired(BluetoothDevice device) {
        }

        @Override
        public void onError(int errorCode) {
        }
    };

    public void loadPairedDevices() {
        this.pairedDeviceList.clear();
        this.mAdapterPairedDevices.notifyDataSetChanged();

        this.pairedDevices = objSDF.bluetooth.getPairedDevices();
        for (BluetoothDevice device : pairedDevices) {
            addListDevices(device, true);
        }

        toggleEmptyDevices();
    }

    private void addListDevices(BluetoothDevice device, boolean isPaired) {
        Device currentDevice = new Device();

        String name = device.getName() == null ? context.getString(R.string.tv_unnamed) : device.getName();
        String color = isPaired ? "#FF03DAC5" : "#E91111";

        currentDevice.setName(name);
        currentDevice.setAddress(device.getAddress());
        currentDevice.setColor(color);

        if (isPaired) {
            this.pairedDeviceList.add(currentDevice);
            this.mAdapterPairedDevices.notifyDataSetChanged();
        } else {
            this.unpairedDeviceList.add(currentDevice);
            this.mAdapterUnpairedDevices.notifyDataSetChanged();
        }
    }

    private void toggleEmptyDevices() {
        if (!this.pairedDeviceList.isEmpty()) {
            this.objSDF.linearLayoutNoPairedDevices.setVisibility(View.GONE);
        } else {
            this.objSDF.linearLayoutNoPairedDevices.setVisibility(View.VISIBLE);
        }
    }

    private void setScanning(boolean value) {
        this.scanning = value;
    }

    private void startTerminalActivity(BluetoothDevice device) {
        Intent intent = new Intent(context, TerminalActivity.class);
        intent.putExtra(AppConstant.EXTRA_TERMINAL_DEVICE, device);
        objSDF.startActivity(intent);
    }

    public void showDisplayLocationSettingsRequest() {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        final String TAG = objSDF.getString(R.string.title_activity_scan_device);
        final int REQUEST_CHECK_SETTINGS = 0x1;

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(result1 -> {
            final Status status = result1.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    objSDF.bluetooth.startScanning();
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    try {
                        status.startResolutionForResult(objSDF.getActivity(), REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e) {
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    break;
            }
        });
    }

    public void showDevicesDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
        View view = layoutInflaterAndroid.inflate(R.layout.dialog_scan_unpaired_devices, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(context);
        alertDialogBuilderUserInput.setView(view);
        alertDialogBuilderUserInput.setTitle(context.getString(R.string.alert_dialog_title_scan_device));

        RecyclerView recyclerView = view.findViewById(R.id.rv_unpaired_devices);
        this.pbScanDevices = view.findViewById(R.id.pb_scan_devices);

        this.ivUnpairedDevices = view.findViewById(R.id.iv_unpaired_devices);
        this.ivUnpairedDevices.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_scan_unpaired_devices));
        this.ivUnpairedDevices.setVisibility(View.VISIBLE);

        this.tvUnpairedDevices = view.findViewById(R.id.tv_unpaired_devices);
        this.tvUnpairedDevices.setText(context.getString(R.string.sb_scan_devices));

        this.mAdapterUnpairedDevices = new ScanDeviceAdapter(objSDF.getActivity(), unpairedDeviceList);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(objSDF.recyclerView.getContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapterUnpairedDevices);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(objSDF.getActivity(),
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                if (scanning) {
                    objSDF.bluetooth.stopScanning();
                    setScanning(false);
                }
                pbScanDevices.setVisibility(View.INVISIBLE);
                Toast.makeText(context, context.getString(R.string.toast_pairing), Toast.LENGTH_SHORT).show();

                objSDF.bluetooth.pair(unpairedDevicesBluetooth.get(position));
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));

        alertDialogBuilderUserInput
                .setCancelable(false)
                .setNegativeButton(context.getString(R.string.negative_button_cancel),
                        (dialogBox, id) -> {
                            if (scanning) {
                                objSDF.bluetooth.stopScanning();
                                setScanning(false);
                            }
                            dialogBox.cancel();
                        });

        alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();
    }
}