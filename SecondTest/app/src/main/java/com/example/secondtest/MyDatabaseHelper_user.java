package com.example.secondtest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class MyDatabaseHelper_user extends SQLiteOpenHelper {

    //建立一个note的表
    public static final String CREATE_BOOK = "create table note("
            +"id integer primary key autoincrement,"
            + "date text ,"
            + "name text,"
            + "data text,"
            + "picture text,"
            + "theme text)";
    private Context mContext;
    public MyDatabaseHelper_user(Context context, String name,
                                 SQLiteDatabase.CursorFactory
                                         factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BOOK);
        Toast.makeText(mContext, "用户数据库创建成功", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
