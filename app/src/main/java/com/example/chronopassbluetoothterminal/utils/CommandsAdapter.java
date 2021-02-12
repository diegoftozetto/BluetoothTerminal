package com.example.chronopassbluetoothterminal.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.chronopassbluetoothterminal.R;
import com.example.chronopassbluetoothterminal.model.Command;

import java.util.List;

public class CommandsAdapter extends RecyclerView.Adapter<CommandsAdapter.MyViewHolder> {

    private Context context;
    private List<Command> configurationsList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView dot;
        public TextView name;
        public TextView value;

        public MyViewHolder(View view) {
            super(view);
            this.name = view.findViewById(R.id.tv_name);
            this.value = view.findViewById(R.id.tv_value);
            this.dot = view.findViewById(R.id.tv_dot);
        }
    }

    public CommandsAdapter(Context context, List<Command> configurationsList) {
        this.context = context;
        this.configurationsList = configurationsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row_commands, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Command config = configurationsList.get(position);

        holder.name.setText(config.getName());

        String matrixLed = config.getMatrixLed();
        int color = config.getColor();
        String colorStr = "";
        if (matrixLed != null) {
            colorStr = AppConstant.KEY_SEPARATOR_DEFAULT + matrixLed + AppConstant.KEY_SEPARATOR_DEFAULT +
                    (Integer.toHexString(color)).substring(2).toUpperCase();
            holder.dot.setBackgroundColor(color);
        } else {
            holder.dot.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
        }

        String text = config.getValue() + colorStr;
        holder.value.setText(text);
    }

    @Override
    public int getItemCount() {
        return configurationsList.size();
    }
}
