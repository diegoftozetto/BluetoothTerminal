package com.example.chronopassbluetoothterminal.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.chronopassbluetoothterminal.R;
import com.example.chronopassbluetoothterminal.model.Settings;

import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.MyViewHolder> {

    private Context context;
    private List<Settings> settingsList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView description;
        public ImageView icon;

        public MyViewHolder(View view) {
            super(view);
            this.name = view.findViewById(R.id.tv_settings_name);
            this.description = view.findViewById(R.id.tv_settings_description);
            this.icon = view.findViewById(R.id.iv_settings_icon);
        }
    }

    public SettingsAdapter(Context context, List<Settings> settingsList) {
        this.context = context;
        this.settingsList = settingsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row_settings, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Settings settings = settingsList.get(position);

        holder.name.setText(settings.getName());
        holder.description.setText(settings.getDescription());
        holder.icon.setImageResource(settings.getIcon());
    }

    @Override
    public int getItemCount() {
        return settingsList.size();
    }
}
