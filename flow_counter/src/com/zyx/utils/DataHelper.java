package com.zyx.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataHelper extends SQLiteOpenHelper {

	private static final int VERSION = 1;
	private static final String DB_NAME = "app_info_db.db";
	private static final String TABLE_NAME = "flow_table";
	private static final String TAG = "DataHelper";

	public DataHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {//除非清除数据，否则只会被调用一次
		// TODO Auto-generated method stub
		Log.e(TAG, "onCreate......");
		String sql = "create table if not exists " + TABLE_NAME + "(_id integer primary key autoincrement,date text,package text,receive long,send log)";
		db.execSQL(sql);
		Log.e(TAG, "onCreate......");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
