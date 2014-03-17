package com.barobot.other;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.barobot.gui.database.DataContract;
import com.barobot.gui.database.DbHelper;

abstract public class DbRow {

	static String[] fields; 
	static Context context = null;
	static SQLiteDatabase dbW = null;
	static SQLiteDatabase dbR = null;

	void getDb( Context context ){
		DbHelper dbHelper = new DbHelper(context);
		dbW = dbHelper.getWritableDatabase();
		dbR = dbHelper.getReadableDatabase();
	}

	long insert(){
		ContentValues cv = new ContentValues();
		cv.put(DataContract.Liquids.COLUMN_NAME_NAME, "");
		cv.put(DataContract.Liquids.COLUMN_NAME_TYPE, "");
		cv.put(DataContract.Liquids.COLUMN_NAME_VOLTAGE, "");
		return dbW.insert(DataContract.Liquids.TABLE_NAME, null, cv);
	}

	public ArrayList<HashMap<String, String>> getAllStudents() {
		ArrayList<HashMap<String, String>> wordList;
		wordList = new ArrayList<HashMap<String, String>>();
		String selectQuery = "SELECT  * FROM Students";
		SQLiteDatabase database = DbRow.dbW;
		Cursor cursor = database.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("StudentId", cursor.getString(0));
				map.put("StudentName", cursor.getString(1));
				wordList.add(map);
			} while (cursor.moveToNext());
		}
		// return contact list
		return wordList;
	}
	public void deleteStudent(String id) {
		SQLiteDatabase database =  DbRow.dbW;  
		String deleteQuery = "DELETE FROM  Students where StudentId='"+ id +"'";  
		database.execSQL(deleteQuery);
	}

	public void insertStudent(HashMap<String, String> queryValues) {
		SQLiteDatabase database = DbRow.dbW;
		ContentValues values = new ContentValues();
		values.put("StudentName", queryValues.get("StudentName"));
		database.insert("Students", null, values);
		database.close();
	}

	public int updateStudent(HashMap<String, String> queryValues) {
		SQLiteDatabase database =  DbRow.dbW;  
		ContentValues values = new ContentValues();
		values.put("StudentName", queryValues.get("StudentName"));
		return database.update("Students", values, "StudentId" + " = ?", new String[] { queryValues.get("StudentId") });
	}

	public HashMap<String, String> getStudentInfo(String id) {
		HashMap<String, String> wordList = new HashMap<String, String>();
		SQLiteDatabase database = DbRow.dbW;
		String selectQuery = "SELECT * FROM Students where StudentId='"+id+"'";
		Cursor cursor = database.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {
				//HashMap<String, String> map = new HashMap<String, String>();
				wordList.put("StudentName", cursor.getString(1));
				//wordList.add(map);
			} while (cursor.moveToNext());
		}                  
		return wordList;
	} 
	
	
	
	
	/*
	
	typeMap.put(String.class, "VARCHAR(767)");
	typeMap.put(Character.TYPE, "CHAR");
	typeMap.put(Short.TYPE, "MEDIUMINT(9)");
	typeMap.put(Integer.TYPE, "INT(11)");
	typeMap.put(Long.TYPE, "BIGINT(20)");
	typeMap.put(Boolean.TYPE, "TINYINT(1)");
	typeMap.put(Double.TYPE, "DOUBLE");
	typeMap.put(Float.TYPE, "FLOAT");
	typeMap.put(Enum.class, "SMALLINT");
	typeMap.put(Date.class, "DATETIME");
	
	    //select the data
    Cursor cursor = db.query(TABLE_STATIONLIST, new String[] {TABLE_FIELD},
                                                    null, null, null, null, null);
    //get it as a ByteArray
    byte[] imageByteArray=cursor.getBlob(1);
    //the cursor is not needed anymore
    cursor.close();
     
    //convert it back to an image
    ByteArrayInputStream imageStream = new ByteArrayInputStream(mybyte);
    Bitmap theImage = BitmapFactory.decodeStream(imageStream));

	
	
	typeMap.put(String.class, "TEXT");
	typeMap.put(Character.TYPE, "TEXT");
	typeMap.put(Short.TYPE, "INTEGER");
	typeMap.put(Integer.TYPE, "INTEGER");
	typeMap.put(Long.TYPE, "INTEGER");
	typeMap.put(Boolean.TYPE, "INTEGER");
	typeMap.put(Double.TYPE, "REAL");
	typeMap.put(Float.TYPE, "REAL");
	typeMap.put(Enum.class, "INTEGER");
	typeMap.put(Date.class, "TEXT"); // as ISO8601 strings ("YYYY-MM-DD HH:MM:SS").
	
	
	*/
	
}

