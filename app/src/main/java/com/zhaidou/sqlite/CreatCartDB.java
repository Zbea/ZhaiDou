package com.zhaidou.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by roy on 15/7/29.
 */
public class CreatCartDB extends SQLiteOpenHelper
{
    private static final int version = 1;
    public static final String SqlName="cartItem";

    public CreatCartDB(Context context)
    {
        super(context, SqlName, null, version);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion)
    {

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        StringBuffer stringBufferSql=new StringBuffer();
        stringBufferSql.append(" Create  TABLE cartItem( ");
        stringBufferSql.append(" [id] integer PRIMARY KEY AUTOINCREMENT ");
        stringBufferSql.append(" ,[baseId] ntext ");
        stringBufferSql.append(" ,[title] nvarchar(500) ");
        stringBufferSql.append(" ,[img] ntext ");
        stringBufferSql.append(" ,[currentPrice] ntext ");
        stringBufferSql.append(" ,[formalPrice] ntext ");
        stringBufferSql.append(" ,[saveMoney] ntext ");
        stringBufferSql.append(" ,[saveTotalMoney] ntext ");
        stringBufferSql.append(" ,[totalMoney] ntext ");
        stringBufferSql.append(" ,[num] ntext ");
        stringBufferSql.append(" ,[size] ntext ");
        stringBufferSql.append(" ,[sizeId] ntext ");
        stringBufferSql.append(" ,[isPublish] ntext ");
        stringBufferSql.append(" ,[isCheck] ntext ");
        stringBufferSql.append(" ,[creatTime] ntext ");
        stringBufferSql.append(" ) " );
        sqLiteDatabase.execSQL(stringBufferSql.toString());
    }

}
