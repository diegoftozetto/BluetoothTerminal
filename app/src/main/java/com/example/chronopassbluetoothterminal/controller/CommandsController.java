package com.example.chronopassbluetoothterminal.controller;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
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
import com.example.chronopassbluetoothterminal.utils.MatrixItemView;
import com.example.chronopassbluetoothterminal.utils.MatrixLedAdapter;
import com.example.chronopassbluetoothterminal.utils.RecyclerTouchListener;
import com.example.chronopassbluetoothterminal.view.ui.CommandsFragment;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class CommandsController {

    private final CommandsFragment objCF;
    private final Context context;

    private CommandsAdapter mAdapter;

    private final List<Command> commandsList;
    private final DatabaseHelper db;

    private String gStrMatrixLed;
    private final int ROW = 8;
    private final int COLUMN = 10;

    //ShowConfigCommandDialog
    private EditText etCommandName;
    private EditText etCommandValue;
    private CheckBox cbCommandColor;
    private TextView tvCommandColor;
    private TextView tvCommandTextResult;
    private ImageView ivCommandMatrixLed;

    //showMatrixLedDialog
    private EditText etMatrixLedResult;
    private List<Integer> selectedPositions;
    private int[][] controlMatrixLed;

    public CommandsController(CommandsFragment objCF, Context context) {
        this.objCF = objCF;
        this.context = context;

        this.commandsList = new ArrayList<>();
        this.gStrMatrixLed = initStrMatrixLed();

        this.db = new DatabaseHelper(context);
        this.commandsList.addAll(db.getAllCommands());

        toggleEmptyCommands();
        initRecyclerView();
    }

    private void initRecyclerView() {
        this.mAdapter = new CommandsAdapter(context, commandsList);
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

    private void toggleEmptyCommands() {
        if (!this.commandsList.isEmpty()) {
            this.objCF.linearLayoutNoCommandConfigured.setVisibility(View.GONE);
        } else {
            this.objCF.linearLayoutNoCommandConfigured.setVisibility(View.VISIBLE);
        }
    }

    public void showConfigCommandDialog(final boolean shouldUpdate, final Command command, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
        View view = layoutInflaterAndroid.inflate(R.layout.dialog_config_command, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(context);
        alertDialogBuilderUserInput.setView(view);
        alertDialogBuilderUserInput.setTitle(!shouldUpdate ? context.getString(R.string.alert_dialog_title_new_command) :
                context.getString(R.string.alert_dialog_title_update_command));
        alertDialogBuilderUserInput.setIcon(R.drawable.ic_drawer_commands);

        this.etCommandName = view.findViewById(R.id.et_command_name);
        this.etCommandValue = view.findViewById(R.id.et_command_value);
        this.tvCommandColor = view.findViewById(R.id.tv_command_color);
        this.tvCommandTextResult = view.findViewById(R.id.tv_command_text_result);
        this.tvCommandColor.setEnabled(false);
        this.ivCommandMatrixLed = view.findViewById(R.id.iv_command_menu_help);
        this.ivCommandMatrixLed.setEnabled(false);
        this.cbCommandColor = view.findViewById(R.id.cb_command_color);
        this.cbCommandColor.setSelected(false);

        this.gStrMatrixLed = initStrMatrixLed();

        if (shouldUpdate && command != null) {
            this.etCommandName.setText(command.getName());
            this.etCommandValue.setText(command.getValue());
            if (command.getMatrixLed() != null) {
                this.cbCommandColor.setChecked(true);
                this.tvCommandColor.setEnabled(true);
                this.ivCommandMatrixLed.setEnabled(true);
                this.tvCommandColor.setBackgroundColor(command.getColor());
                this.gStrMatrixLed = command.getMatrixLed();

                setTextResult(command.getValue(), gStrMatrixLed);
            } else {
                setTextResult(command.getValue(), null);
            }
        } else {
            setTextResult(null, null);
        }

        cbCommandColor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isChecked()) {
                tvCommandColor.setEnabled(true);
                ivCommandMatrixLed.setEnabled(true);
            } else {
                tvCommandColor.setEnabled(false);
                ivCommandMatrixLed.setEnabled(false);
            }

            String matrixLed = buttonView.isChecked() ? gStrMatrixLed : null;
            setTextResult(etCommandValue.getText().toString().trim(), matrixLed);
        });

        etCommandValue.addTextChangedListener(new TextWatcher() {

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String matrixLed = cbCommandColor.isChecked() ? gStrMatrixLed : null;
                setTextResult(s.toString(), matrixLed);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        tvCommandColor.setOnClickListener(view12 -> {
            ColorPickerDialog colorPickerDialog = ColorPickerDialog.newBuilder()
                    .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                    .setAllowCustom(true)
                    .setAllowPresets(true)
                    .setColor(getColorId(tvCommandColor))
                    .create();

            colorPickerDialog.setColorPickerDialogListener(new ColorPickerDialogListener() {
                @Override
                public void onColorSelected(int dialogId, int color) {
                    tvCommandColor.setBackgroundColor(color);

                    String matrixLed = cbCommandColor.isChecked() ? gStrMatrixLed : null;
                    setTextResult(etCommandValue.getText().toString().trim(), matrixLed);
                }

                @Override
                public void onDialogDismissed(int dialogId) {
                }
            });
            colorPickerDialog.show(objCF.getActivity().getSupportFragmentManager(), "COLOR_PICKER_EDT");
        });

        ivCommandMatrixLed.setOnClickListener(view1 -> showMatrixLedDialog());

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
            String name = etCommandName.getText().toString().trim();
            String value = etCommandValue.getText().toString().trim();

            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(value)) {
                boolean result;
                String message;

                int selectedColor = AppConstant.COLOR_DEFAULT;
                String matrixLed = null;
                if (cbCommandColor.isChecked()) {
                    selectedColor = getColorId(tvCommandColor);
                    matrixLed = gStrMatrixLed;
                }

                if (shouldUpdate && command != null) {
                    result = updateCommand(name, value, matrixLed, selectedColor, position);
                    message = context.getString(R.string.toast_command_updated_successfully);
                } else {
                    result = createCommand(name, value, matrixLed, selectedColor);
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
                showConfigCommandDialog(true, commandsList.get(position), position);
            } else {
                deleteCommand(position);
                Toast.makeText(context, context.getString(R.string.toast_command_removed_successfully), Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    public void showMatrixLedDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
        View view = layoutInflaterAndroid.inflate(R.layout.dialog_matrix_leds, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(context);
        alertDialogBuilderUserInput.setView(view);
        alertDialogBuilderUserInput.setTitle(context.getString(R.string.alert_dialog_title_matrix_led));
        alertDialogBuilderUserInput.setIcon(R.drawable.ic_matrix_led);

        this.etMatrixLedResult = view.findViewById(R.id.et_matrix_result);
        etMatrixLedResult.setText(gStrMatrixLed);

        this.selectedPositions = new ArrayList<>();
        this.controlMatrixLed = new int[ROW][COLUMN];

        String strBin = convertMatrixLedToBin(gStrMatrixLed);
        loadMatrixUpdate(strBin);

        GridView gvMatrixLed = view.findViewById(R.id.gv_matrix_led);
        final MatrixLedAdapter adapter = new MatrixLedAdapter(context, new String[ROW * COLUMN], selectedPositions, getColorId(tvCommandColor));
        gvMatrixLed.setAdapter(adapter);

        gvMatrixLed.setOnItemClickListener((parent, v, position, id) -> {
            int selectedIndex = adapter.getSelectedPositions().indexOf(position);
            boolean isSelected = selectedIndex <= -1;

            if (!isSelected) {
                adapter.removeSelectedPositions(selectedIndex);
                ((MatrixItemView) v).display(false, context.getResources().getColor(R.color.color_matrix_default));
            } else {
                adapter.addSelectedPositions(position);
                ((MatrixItemView) v).display(true, getColorId(tvCommandColor));
            }

            String positionMatrix = String.valueOf(position);
            int row = (position > COLUMN - 1) ? Integer.parseInt(positionMatrix.substring(0, 1)) : 0;
            int column = (position > COLUMN - 1) ? Integer.parseInt(positionMatrix.substring(1, 2)) : Integer.parseInt(positionMatrix.substring(0, 1));
            controlMatrixLed[row][column] = isSelected ? 1 : 0;
            etMatrixLedResult.setText(convertMatrixLedToHex(controlMatrixLed));
        });

        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(
                        "OK",
                        (dialogBox, id) -> {
                        })
                .setNegativeButton(context.getString(R.string.negative_button_cancel),
                        (dialogBox, id) -> dialogBox.cancel());

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            gStrMatrixLed = etMatrixLedResult.getText().toString().trim();

            String matrixLed = cbCommandColor.isChecked() ? gStrMatrixLed : null;
            setTextResult(etCommandValue.getText().toString().trim(), matrixLed);

            alertDialog.dismiss();
        });
    }

    /**
     * OTHERS
     */
    private String convertMatrixLedToHex(int[][] matrixLed) {
        StringBuilder srtHex = new StringBuilder();
        for (int i = 0; i < ROW; i++) {
            srtHex.append(convertBinToHex(matrixLed[i][0] + "" + matrixLed[i][1], 2));

            int flag = 0;
            StringBuilder concat = new StringBuilder();
            for (int j = 2; j < COLUMN; j++) {
                concat.append(matrixLed[i][j]);
                flag++;

                if (flag == 4) {
                    srtHex.append(convertBinToHex(concat.toString(), 1));
                    concat = new StringBuilder();
                    flag = 0;
                }
            }
        }
        return srtHex.toString();
    }

    private String convertMatrixLedToBin(String str) {
        StringBuilder strBin = new StringBuilder();
        int previousLine = 0;
        int currentLine = 4;
        for (int i = 0; i < ROW; i++) {
            String strHex = str.substring(previousLine, currentLine);
            previousLine = currentLine;
            currentLine += 4;
            strBin.append(convertHexToBin(strHex));
        }
        return strBin.toString();
    }

    private void loadMatrixUpdate(String strBin) {
        int previousLine = 0;
        int currentLine = 1;
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COLUMN; j++) {
                int bit = Integer.parseInt(strBin.substring(previousLine, currentLine));
                this.controlMatrixLed[i][j] = bit;

                if (bit == 1) {
                    int posMatrix = Integer.parseInt(i + "" + j);
                    this.selectedPositions.add(posMatrix);
                }

                previousLine = currentLine;
                currentLine += 1;
            }
        }
    }

    private String convertBinToHex(String str, int bit) {
        int i = Integer.parseInt(str, 2);
        return String.format("%0" + bit + "X", i);
    }

    private String convertHexToBin(String str) {
        String b = new BigInteger(str, 16).toString(2);
        return String.format("%10s", b).replace(' ', '0');
    }

    private String initStrMatrixLed() {
        StringBuilder str = new StringBuilder();
        for (int j = 0; j < 32; j++)
            str.append("0");
        return str.toString();
    }

    private int getColorId(View view) {
        ColorDrawable colorDrawable = (ColorDrawable) view.getBackground();
        return colorDrawable.getColor();
    }

    private void setTextResult(String value, String matrixLed) {
        String currentValue = value;
        if (value != null) {
            if (value.equals("") && matrixLed == null) {
                currentValue = null;
            }
        }

        String textResult;
        if (currentValue == null)
            textResult = context.getString(R.string.no_command_informed);
        else if (matrixLed == null)
            textResult = currentValue;
        else
            textResult = currentValue + AppConstant.KEY_SEPARATOR_DEFAULT + matrixLed + AppConstant.KEY_SEPARATOR_DEFAULT +
                    (Integer.toHexString(getColorId(tvCommandColor))).substring(2).toUpperCase();

        this.tvCommandTextResult.setText(textResult);
    }

    /**
     * DATABASE
     */
    private boolean createCommand(String name, String value, String matrixLed, int color) {
        long id = db.insertCommand(name, value, matrixLed, color);
        Command command = db.getCommand(id);

        if (command != null) {
            commandsList.add(0, command);
            mAdapter.notifyDataSetChanged();

            toggleEmptyCommands();

            return true;
        }

        return false;
    }

    private boolean updateCommand(String name, String value, String matrixLed, int color, int position) {
        Command command = commandsList.get(position);
        command.setName(name);
        command.setValue(value);
        command.setMatrixLed(matrixLed);
        command.setColor(color);

        db.updateCommand(command);

        commandsList.set(position, command);
        mAdapter.notifyItemChanged(position);

        toggleEmptyCommands();

        return true;
    }


    private void deleteCommand(int position) {
        db.deleteCommand(commandsList.get(position));

        commandsList.remove(position);
        mAdapter.notifyItemRemoved(position);

        toggleEmptyCommands();
    }
}
