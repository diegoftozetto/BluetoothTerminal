package com.example.chronopassbluetoothterminal.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.chronopassbluetoothterminal.model.Command;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "chrono_pass_db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Command.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Command.TABLE_NAME);

        onCreate(db);
    }

    public long insertConfiguration(String name, String value, int color) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(Command.COLUMN_NAME, name);
        values.put(Command.COLUMN_VALUE, value);
        values.put(Command.COLUMN_COLOR, color);

        long id = db.insert(Command.TABLE_NAME, null, values);
        db.close();

        return id;
    }

    public Command getConfiguration(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Command.TABLE_NAME,
                new String[]{Command.COLUMN_ID, Command.COLUMN_NAME, Command.COLUMN_VALUE, Command.COLUMN_COLOR, Command.COLUMN_TIMESTAMP},
                Command.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        Command config = new Command(
                cursor.getLong(cursor.getColumnIndex(Command.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(Command.COLUMN_NAME)),
                cursor.getString(cursor.getColumnIndex(Command.COLUMN_VALUE)),
                cursor.getInt(cursor.getColumnIndex(Command.COLUMN_COLOR)),
                cursor.getString(cursor.getColumnIndex(Command.COLUMN_TIMESTAMP)));

        cursor.close();

        return config;
    }

    public List<Command> getAllConfigurations() {
        List<Command> configurations = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + Command.TABLE_NAME + " ORDER BY " +
                Command.COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Command config = new Command();
                config.setId(cursor.getInt(cursor.getColumnIndex(Command.COLUMN_ID)));
                config.setName(cursor.getString(cursor.getColumnIndex(Command.COLUMN_NAME)));
                config.setValue(cursor.getString(cursor.getColumnIndex(Command.COLUMN_VALUE)));
                config.setColor(cursor.getInt(cursor.getColumnIndex(Command.COLUMN_COLOR)));
                config.setTimestamp(cursor.getString(cursor.getColumnIndex(Command.COLUMN_TIMESTAMP)));

                configurations.add(config);
            } while (cursor.moveToNext());
        }

        db.close();
        return configurations;
    }

    public int getConfigurationsCount() {
        String countQuery = "SELECT  * FROM " + Command.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    public boolean updateConfiguration(Command config) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Command.COLUMN_NAME, config.getName());
        values.put(Command.COLUMN_VALUE, config.getValue());
        values.put(Command.COLUMN_COLOR, config.getColor());

        int ret = db.update(Command.TABLE_NAME, values, Command.COLUMN_ID + " = ?",
                new String[]{String.valueOf(config.getId())});

        if (ret == 0)
            return false;

        return true;
    }

    public void deleteConfiguration(Command config) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Command.TABLE_NAME, Command.COLUMN_ID + " = ?",
                new String[]{String.valueOf(config.getId())});
        db.close();
    }
}