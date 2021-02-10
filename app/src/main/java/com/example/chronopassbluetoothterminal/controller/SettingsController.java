package com.example.chronopassbluetoothterminal.controller;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chronopassbluetoothterminal.R;
import com.example.chronopassbluetoothterminal.model.Settings;
import com.example.chronopassbluetoothterminal.utils.AppConstant;
import com.example.chronopassbluetoothterminal.utils.RecyclerTouchListener;
import com.example.chronopassbluetoothterminal.utils.SettingsAdapter;
import com.example.chronopassbluetoothterminal.utils.SharedPreferenceHelper;
import com.example.chronopassbluetoothterminal.view.ui.SettingsFragment;

import java.util.ArrayList;
import java.util.List;

public class SettingsController {

    private SettingsFragment objSF;
    private Context context;

    private SettingsAdapter mAdapter;

    private List<Settings> settingsList;

    public SettingsController(SettingsFragment objSF, Context context) {
        this.objSF = objSF;
        this.context = context;

        this.settingsList = new ArrayList<>();

        Settings delimiter = new Settings(context.getString(R.string.settings_delimiter),
                context.getString(R.string.settings_delimiter_description), R.drawable.ic_seetings_delimiter);
        Settings about = new Settings(context.getString(R.string.settings_about),
                context.getString(R.string.settings_about_description), R.drawable.ic_settings_about);

        this.settingsList.add(delimiter);
        this.settingsList.add(about);

        initRecyclerView();
    }

    private void initRecyclerView() {
        this.mAdapter = new SettingsAdapter(context, settingsList);
        this.objSF.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        this.objSF.recyclerView.setItemAnimator(new DefaultItemAnimator());
        this.objSF.recyclerView.addItemDecoration(new DividerItemDecoration(objSF.recyclerView.getContext(), LinearLayoutManager.VERTICAL));
        this.objSF.recyclerView.setAdapter(mAdapter);

        this.objSF.recyclerView.addOnItemTouchListener(new RecyclerTouchListener(context,
                objSF.recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                if (position == 0) {
                    showEditDelimiterDialog();
                } else {
                    showAboutDialog();
                }
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));
    }

    public void showEditDelimiterDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
        View view = layoutInflaterAndroid.inflate(R.layout.dialog_edit_delimiter, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(context);
        alertDialogBuilderUserInput.setView(view);
        alertDialogBuilderUserInput.setTitle(context.getString(R.string.alert_dialog_title_configure_delimiter));
        alertDialogBuilderUserInput.setIcon(R.drawable.ic_seetings_delimiter);

        String delimiterSend = SharedPreferenceHelper.getSharedPreferenceString(context, AppConstant.KEY_DELIMITER_SEND, null);
        String delimiterReceive = SharedPreferenceHelper.getSharedPreferenceString(context, AppConstant.KEY_DELIMITER_RECEIVE, null);

        final EditText etDelimiterSend = view.findViewById(R.id.et_delimiter_send);
        etDelimiterSend.setText(delimiterSend);

        final EditText etDelimiterReceive = view.findViewById(R.id.et_delimiter_receive);
        etDelimiterReceive.setText(delimiterReceive);

        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(context.getString(R.string.positive_button_save), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                    }
                })
                .setNegativeButton(context.getString(R.string.negative_button_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentDelimiterSend = etDelimiterSend.getText().toString().trim();
                String currentDelimiterReceive = etDelimiterReceive.getText().toString().trim();

                if (TextUtils.isEmpty(currentDelimiterSend) || currentDelimiterSend.length() != 1 ||
                        TextUtils.isEmpty(currentDelimiterReceive) || currentDelimiterReceive.length() != 1) {
                    Toast.makeText(context, context.getString(R.string.toast_type_field_correctly), Toast.LENGTH_SHORT).show();
                } else {
                    AppConstant.CURRENT_DELIMITER_RECEIVE = currentDelimiterReceive;
                    SharedPreferenceHelper.setSharedPreferenceString(context, AppConstant.KEY_DELIMITER_SEND, currentDelimiterSend);
                    SharedPreferenceHelper.setSharedPreferenceString(context, AppConstant.KEY_DELIMITER_RECEIVE, currentDelimiterReceive);

                    Toast.makeText(context, context.getString(R.string.toast_delimiter_added_successfully), Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                }
            }
        });
    }

    public void showAboutDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
        View view = layoutInflaterAndroid.inflate(R.layout.dialog_settings_about, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(context);
        alertDialogBuilderUserInput.setView(view);

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();
    }
}
