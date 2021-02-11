package com.example.chronopassbluetoothterminal.controller;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chronopassbluetoothterminal.R;
import com.example.chronopassbluetoothterminal.database.DatabaseHelper;
import com.example.chronopassbluetoothterminal.model.Command;
import com.example.chronopassbluetoothterminal.utils.AppConstant;
import com.example.chronopassbluetoothterminal.utils.CommandsAdapter;
import com.example.chronopassbluetoothterminal.utils.RecyclerTouchListener;
import com.example.chronopassbluetoothterminal.view.ui.CommandsFragment;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.ArrayList;
import java.util.List;

public class CommandsController {

    private final CommandsFragment objCF;
    private final Context context;

    private CommandsAdapter mAdapter;

    private final List<Command> configurationsList;
    private final DatabaseHelper db;

    private String selectedColorPicker;

    public CommandsController(CommandsFragment objCF, Context context) {
        this.objCF = objCF;
        this.context = context;

        this.configurationsList = new ArrayList<>();
        this.selectedColorPicker = null;

        this.db = new DatabaseHelper(context);
        this.configurationsList.addAll(db.getAllConfigurations());

        toggleEmptyConfigurations();
        initRecyclerView();
    }

    private void initRecyclerView() {
        this.mAdapter = new CommandsAdapter(context, configurationsList);
        this.objCF.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        this.objCF.recyclerView.setItemAnimator(new DefaultItemAnimator());
        this.objCF.recyclerView.addItemDecoration(new DividerItemDecoration(objCF.recyclerView.getContext(), LinearLayoutManager.VERTICAL));
        this.objCF.recyclerView.setAdapter(mAdapter);

        this.objCF.recyclerView.addOnItemTouchListener(new RecyclerTouchListener(context,
                objCF.recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
            }

            @Override
            public void onLongClick(View view, int position) {
                showActionsDialog(position);
            }
        }));
    }

    private void toggleEmptyConfigurations() {
        if (!this.configurationsList.isEmpty()) {
            this.objCF.linearLayoutNoCommandConfigured.setVisibility(View.GONE);
        } else {
            this.objCF.linearLayoutNoCommandConfigured.setVisibility(View.VISIBLE);
        }
    }

    public void showConfigCommandDialog(final boolean shouldUpdate, final Command config, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
        View view = layoutInflaterAndroid.inflate(R.layout.dialog_config_command, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(context);
        alertDialogBuilderUserInput.setView(view);
        alertDialogBuilderUserInput.setTitle(!shouldUpdate ? context.getString(R.string.alert_dialog_title_new_command) :
                context.getString(R.string.alert_dialog_title_update_command));
        alertDialogBuilderUserInput.setIcon(R.drawable.ic_drawer_commands);

        final ImageView ivCommandHelpColor = view.findViewById(R.id.iv_command_menu_help);
        final EditText etConfigName = view.findViewById(R.id.et_command_name);
        final EditText etConfigValue = view.findViewById(R.id.et_command_value);
        final TextView tvCommandColor = view.findViewById(R.id.tv_command_color);
        tvCommandColor.setEnabled(false);
        final CheckBox cbCommandColor = view.findViewById(R.id.cb_command_color);
        cbCommandColor.setSelected(false);

        ivCommandHelpColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, context.getString(R.string.toast_selected_color), Toast.LENGTH_LONG).show();
            }
        });

        cbCommandColor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isChecked())
                tvCommandColor.setEnabled(true);
            else
                tvCommandColor.setEnabled(false);
        });

        tvCommandColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ColorPickerDialog colorPickerDialog = ColorPickerDialog.newBuilder()
                        .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                        .setAllowCustom(true)
                        .setAllowPresets(true)
                        .setColor(Color.BLUE)
                        .create();

                colorPickerDialog.setColorPickerDialogListener(new ColorPickerDialogListener() {
                    @Override
                    public void onColorSelected(int dialogId, int color) {
                        selectedColorPicker = String.valueOf(color);
                        tvCommandColor.setBackgroundColor(color);
                    }

                    @Override
                    public void onDialogDismissed(int dialogId) {
                    }
                });
                colorPickerDialog.show(objCF.getActivity().getSupportFragmentManager(), "COLOR_PICKER_EDT");
            }
        });

        if (shouldUpdate && config != null) {
            etConfigName.setText(config.getName());
            etConfigValue.setText(config.getValue());

            if (config.getColor() != AppConstant.COLOR_DEFAULT) {
                cbCommandColor.setChecked(true);
                tvCommandColor.setBackgroundColor(config.getColor());
            }
        }
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(
                        shouldUpdate ? context.getString(R.string.positive_button_update) : context.getString(R.string.positive_button_save),
                        (dialogBox, id) -> {
                        })
                .setNegativeButton(context.getString(R.string.negative_button_cancel),
                        (dialogBox, id) -> dialogBox.cancel());

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = etConfigName.getText().toString().trim();
            String value = etConfigValue.getText().toString().trim();

            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(value)) {
                boolean result;
                String message;

                int selectedColor = AppConstant.COLOR_DEFAULT;
                if (cbCommandColor.isChecked()) {
                    if (selectedColorPicker != null) {
                        selectedColor = Integer.parseInt(selectedColorPicker);
                    } else {
                        selectedColor = context.getResources().getColor(R.color.colorPickerDefault);
                    }
                }

                if (shouldUpdate && config != null) {
                    result = updateConfiguration(name, value, selectedColor, position);
                    message = context.getString(R.string.toast_command_updated_successfully);
                } else {
                    result = createConfiguration(name, value, selectedColor);
                    message = context.getString(R.string.toast_command_added_successfully);
                }

                String text = result ? message : context.getString(R.string.toast_error_adding_command_configuration);
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();

                alertDialog.dismiss();
            } else {
                Toast.makeText(context, context.getString(R.string.toast_type_fields_correctly), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showActionsDialog(final int position) {
        CharSequence[] chooseOptions = new CharSequence[]{context.getString(R.string.choose_option_edit), context.getString(R.string.choose_option_delete)};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.alert_dialog_title_choose_option));
        builder.setItems(chooseOptions, (dialog, which) -> {
            if (which == 0) {
                showConfigCommandDialog(true, configurationsList.get(position), position);
            } else {
                deleteConfiguration(position);
                Toast.makeText(context, context.getString(R.string.toast_command_removed_successfully), Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    private boolean createConfiguration(String name, String value, int color) {
        long id = db.insertConfiguration(name, value, color);
        Command config = db.getConfiguration(id);

        if (config != null) {
            configurationsList.add(0, config);
            mAdapter.notifyDataSetChanged();

            toggleEmptyConfigurations();

            return true;
        }

        return false;
    }

    private boolean updateConfiguration(String name, String value, int color, int position) {
        Command config = configurationsList.get(position);
        config.setName(name);
        config.setValue(value);
        config.setColor(color);

        db.updateConfiguration(config);

        configurationsList.set(position, config);
        mAdapter.notifyItemChanged(position);

        toggleEmptyConfigurations();

        return true;
    }


    private void deleteConfiguration(int position) {
        db.deleteConfiguration(configurationsList.get(position));

        configurationsList.remove(position);
        mAdapter.notifyItemRemoved(position);

        toggleEmptyConfigurations();
    }
}
