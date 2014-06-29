package com.barobot.gui.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper{
	public static int SCHEMA_VERSION = 2;
	public static String DATABASE_NAME = "Barobot.db";
	public DbHelper(Context context)
	{
		super(context, DATABASE_NAME, null, SCHEMA_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DataContract.SQL_CREATE_LIQUIDS);
		db.execSQL(DataContract.SQL_CREATE_SLOTS);
		db.execSQL(DataContract.SQL_CREATE_RECIPES);
		db.execSQL(DataContract.SQL_CREATE_INGREDIENTS);
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(DataContract.SQL_DELETE_INGREDIENTS);
		db.execSQL(DataContract.SQL_DELETE_RECIPES);
		db.execSQL(DataContract.SQL_DELETE_SLOTS);
		db.execSQL(DataContract.SQL_DELETE_LIQUIDS);
		onCreate(db);
	}
}
