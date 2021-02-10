package com.example.chronopassbluetoothterminal.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.chronopassbluetoothterminal.R;
import com.example.chronopassbluetoothterminal.model.Device;

import java.util.List;

public class ScanDeviceAdapter extends RecyclerView.Adapter<ScanDeviceAdapter.MyViewHolder> {

    private Context context;
    private List<Device> devicesList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView dot;
        public TextView address;

        public MyViewHolder(View view) {
            super(view);
            this.address = view.findViewById(R.id.tv_address);
            this.name = view.findViewById(R.id.tv_name);
            this.dot = view.findViewById(R.id.tv_dot);
        }
    }

    public ScanDeviceAdapter(Context context, List<Device> devicesList) {
        this.context = context;
        this.devicesList = devicesList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row_scan_devices, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Device device = devicesList.get(position);

        holder.address.setText(device.getAddress());
        holder.name.setText(device.getName());
        holder.dot.setTextColor(Color.parseColor(device.getColor()));
    }

    @Override
    public int getItemCount() {
        return devicesList.size();
    }
}
