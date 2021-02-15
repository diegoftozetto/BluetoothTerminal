package com.example.chronopassbluetoothterminal.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.chronopassbluetoothterminal.model.Command;
import com.example.chronopassbluetoothterminal.model.Terminal;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "chrono_pass_db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Command.CREATE_TABLE);
        db.execSQL(Terminal.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            db.execSQL("ALTER TABLE " + Command.TABLE_NAME + " ADD COLUMN "
                    + Command.COLUMN_MATRIX_LED + " TEXT");
            db.execSQL(Terminal.CREATE_TABLE);
        } else if (oldVersion == 2) {
            //Nothing
        } else {
            onCreate(db);
        }
    }

    /**
     * TB_COMMAND
     */
    public long insertCommand(String name, String value, String matrixLed, int color) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(Command.COLUMN_NAME, name);
        values.put(Command.COLUMN_VALUE, value);
        values.put(Command.COLUMN_MATRIX_LED, matrixLed);
        values.put(Command.COLUMN_COLOR, color);

        long id = db.insert(Command.TABLE_NAME, null, values);
        db.close();

        return id;
    }

    public Command getCommand(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Command.TABLE_NAME,
                new String[]{Command.COLUMN_ID, Command.COLUMN_NAME, Command.COLUMN_VALUE, Command.COLUMN_MATRIX_LED, Command.COLUMN_COLOR, Command.COLUMN_TIMESTAMP},
                Command.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        Command command = new Command(
                cursor.getLong(cursor.getColumnIndex(Command.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(Command.COLUMN_NAME)),
                cursor.getString(cursor.getColumnIndex(Command.COLUMN_VALUE)),
                cursor.getString(cursor.getColumnIndex(Command.COLUMN_MATRIX_LED)),
                cursor.getInt(cursor.getColumnIndex(Command.COLUMN_COLOR)),
                cursor.getString(cursor.getColumnIndex(Command.COLUMN_TIMESTAMP)));

        cursor.close();

        return command;
    }

    public List<Command> getAllCommands() {
        List<Command> configurations = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + Command.TABLE_NAME + " ORDER BY " +
                Command.COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Command command = new Command();
                command.setId(cursor.getInt(cursor.getColumnIndex(Command.COLUMN_ID)));
                command.setName(cursor.getString(cursor.getColumnIndex(Command.COLUMN_NAME)));
                command.setValue(cursor.getString(cursor.getColumnIndex(Command.COLUMN_VALUE)));
                command.setMatrixLed(cursor.getString(cursor.getColumnIndex(Command.COLUMN_MATRIX_LED)));
                command.setColor(cursor.getInt(cursor.getColumnIndex(Command.COLUMN_COLOR)));
                command.setTimestamp(cursor.getString(cursor.getColumnIndex(Command.COLUMN_TIMESTAMP)));

                configurations.add(command);
            } while (cursor.moveToNext());
        }

        db.close();
        return configurations;
    }

    public int getCommandsCount() {
        String countQuery = "SELECT  * FROM " + Command.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    public boolean updateCommand(Command command) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Command.COLUMN_NAME, command.getName());
        values.put(Command.COLUMN_VALUE, command.getValue());
        values.put(Command.COLUMN_MATRIX_LED, command.getMatrixLed());
        values.put(Command.COLUMN_COLOR, command.getColor());

        int ret = db.update(Command.TABLE_NAME, values, Command.COLUMN_ID + " = ?",
                new String[]{String.valueOf(command.getId())});

        return ret != 0;
    }

    public void deleteCommand(Command config) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Command.TABLE_NAME, Command.COLUMN_ID + " = ?",
                new String[]{String.valueOf(config.getId())});
        db.close();
    }

    /**
     * TB_TERMINAL
     */
    public long insertTerminal(String deviceAddress, String timestamp, String text, int type) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(Terminal.COLUMN_DEVICE_ADDRESS, deviceAddress);
        values.put(Terminal.COLUMN_TIMESTAMP, timestamp);
        values.put(Terminal.COLUMN_TEXT, text);
        values.put(Terminal.COLUMN_TYPE, type);

        long id = db.insert(Terminal.TABLE_NAME, null, values);
        db.close();

        return id;
    }

    public List<Terminal> getAllTextTerminalDeviceAddress(String deviceAddress) {
        List<Terminal> configurations = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + Terminal.TABLE_NAME + " WHERE " +
                Terminal.COLUMN_DEVICE_ADDRESS + "= '" + deviceAddress + "'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Terminal config = new Terminal();
                config.setId(cursor.getInt(cursor.getColumnIndex(Terminal.COLUMN_ID)));
                config.setDeviceAddress(cursor.getString(cursor.getColumnIndex(Terminal.COLUMN_DEVICE_ADDRESS)));
                config.setTimestamp(cursor.getString(cursor.getColumnIndex(Terminal.COLUMN_TIMESTAMP)));
                config.setText(cursor.getString(cursor.getColumnIndex(Terminal.COLUMN_TEXT)));
                config.setType(cursor.getInt(cursor.getColumnIndex(Terminal.COLUMN_TYPE)));

                configurations.add(config);
            } while (cursor.moveToNext());
        }

        db.close();
        return configurations;
    }

    public void deleteTextTerminal(String deviceAddress) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Terminal.TABLE_NAME, Terminal.COLUMN_DEVICE_ADDRESS + " = ?",
                new String[]{deviceAddress});
        db.close();
    }

    public void deleteAllTextTerminal() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Terminal.TABLE_NAME, null,
                null);
        db.close();
    }
}