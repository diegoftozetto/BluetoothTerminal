package com.example.chronopassbluetoothterminal.view.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chronopassbluetoothterminal.R;
import com.example.chronopassbluetoothterminal.controller.ScanDevicesController;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

import me.aflak.bluetooth.Bluetooth;

public class ScanDevicesFragment extends Fragment {

    private ScanDevicesController objSDC;

    public RecyclerView recyclerView;
    public LinearLayout linearLayoutNoPairedDevices;

    public Bluetooth bluetooth;

    public Menu menu;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_scan_devices, container, false);
        init(root);

        return root;
    }

    private void init(View root) {
        this.recyclerView = root.findViewById(R.id.rv_paired_devices);
        this.linearLayoutNoPairedDevices = root.findViewById(R.id.ll_no_paired_devices);

        this.bluetooth = new Bluetooth(root.getContext());

        this.objSDC = new ScanDevicesController(this, root.getContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        bluetooth.onStart();
        if (bluetooth.isEnabled()) {
            this.objSDC.loadPairedDevices();
        } else {
            bluetooth.showEnableDialog(getActivity());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        bluetooth.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        bluetooth.onActivityResult(requestCode, resultCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        this.menu = menu;

        menu.findItem(R.id.ic_menu_add).setVisible(false);
        menu.findItem(R.id.ic_menu_scan).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.ic_menu_scan) {
            checkPermissions();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void checkPermissions() {
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

        Dexter.withActivity(getActivity()).withPermissions(
                permissions)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            objSDC.showDisplayLocationSettingsRequest();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }
}