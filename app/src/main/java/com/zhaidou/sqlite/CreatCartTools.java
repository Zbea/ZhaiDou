package com.zhaidou.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zhaidou.model.CartGoodsItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roy on 15/7/30.
 */
public class CreatCartTools
{
    static List<CartGoodsItem> items=new ArrayList<CartGoodsItem>();

    /**
     * 全查
     */
    public static List<CartGoodsItem> selectByAll(CreatCartDB cartDB,int userId)
    {
        items.removeAll(items);
        List<CartGoodsItem> itemss=new ArrayList<CartGoodsItem>();
        SQLiteDatabase sqLiteDatabase=cartDB.getReadableDatabase();
        sqLiteDatabase.beginTransaction();
        try
        {
            Cursor cursor=sqLiteDatabase.rawQuery("select * from cartItem where userId="+userId,null);
            while (cursor.moveToNext())
            {
                CartGoodsItem item = new CartGoodsItem();
                item.userId = cursor.getInt(cursor.getColumnIndex("userId"));
                item.id = cursor.getInt(cursor.getColumnIndex("baseId"));
                item.name = cursor.getString(cursor.getColumnIndex("title"));
                item.imageUrl = cursor.getString(cursor.getColumnIndex("img"));
                item.currentPrice = cursor.getDouble(cursor.getColumnIndex("currentPrice"));
                item.formalPrice = cursor.getDouble(cursor.getColumnIndex("formalPrice"));
                item.saveMoney = cursor.getDouble(cursor.getColumnIndex("saveMoney"));
                item.saveTotalMoney = cursor.getDouble(cursor.getColumnIndex("saveTotalMoney"));
                item.num = cursor.getInt(cursor.getColumnIndex("num"));
                item.size = cursor.getString(cursor.getColumnIndex("size"));
                item.sizeId = cursor.getInt(cursor.getColumnIndex("sizeId"));
                item.isPublish = cursor.getString(cursor.getColumnIndex("isPublish"));
                item.isOver = cursor.getString(cursor.getColumnIndex("isOver"));
                item.isOSale = cursor.getString(cursor.getColumnIndex("isOSale"));
                item.isCheck = false;
                item.isDate="false";
                item.creatTime = cursor.getLong(cursor.getColumnIndex("creatTime"));
                itemss.add(item);
            }
            sqLiteDatabase.setTransactionSuccessful();
            cursor.close();

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            sqLiteDatabase.endTransaction();
            sqLiteDatabase.close();
        }
        //反序
        for(int i=itemss.size()-1;i>=0;i--)
        {
            items.add(itemss.get(i));
        }
        return items;
    }

    /**
     * 删除数据
     */
    public static void deleteByData(CreatCartDB cartDB, CartGoodsItem item)
    {
        SQLiteDatabase sqLiteDatabase = cartDB.getReadableDatabase();
        sqLiteDatabase.beginTransaction();
        try
        {
            String whereClause = "userId=? and sizeId=?";
            String[] whereArgs = new String[]{String.valueOf(item.userId),String.valueOf(item.sizeId)};
            sqLiteDatabase.delete(CreatCartDB.SqlName, whereClause, whereArgs);
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            sqLiteDatabase.endTransaction();
            sqLiteDatabase.close();
        }
    }

    /**
     * 修改数量数据
     */
    public static void editNumByData(CreatCartDB cartDB,CartGoodsItem itm)
    {
        SQLiteDatabase sqLiteDatabase = cartDB.getReadableDatabase();
        sqLiteDatabase.beginTransaction();
        try
        {
            ContentValues values = new ContentValues();
            values.put("num", itm.num);
            String whereClause = "userId=? and sizeId=?";
            String[] whereArgs = new String[]{String.valueOf(itm.userId),String.valueOf(itm.sizeId)};
            sqLiteDatabase.update(CreatCartDB.SqlName, values, whereClause, whereArgs);
            sqLiteDatabase.setTransactionSuccessful();

        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            sqLiteDatabase.endTransaction();
            sqLiteDatabase.close();
        }
    }

    /**
     * 修改数据为已卖完
     */
    public static void editIsOverByData(CreatCartDB cartDB,CartGoodsItem itm)
    {
        SQLiteDatabase sqLiteDatabase = cartDB.getReadableDatabase();
        sqLiteDatabase.beginTransaction();
        try
        {
            ContentValues values = new ContentValues();
            values.put("isOver", itm.isOver);
            String whereClause = "userId=? and sizeId=?";
            String[] whereArgs = new String[]{String.valueOf(itm.userId),String.valueOf(itm.sizeId)};
            sqLiteDatabase.update(CreatCartDB.SqlName, values, whereClause, whereArgs);
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            sqLiteDatabase.endTransaction();
            sqLiteDatabase.close();
        }
    }

    /**
     * 修改数据为已下架
     */
    public static void editIsLoseByData(CreatCartDB cartDB,CartGoodsItem itm)
    {
        SQLiteDatabase sqLiteDatabase = cartDB.getReadableDatabase();
        sqLiteDatabase.beginTransaction();
        try
        {
            ContentValues values = new ContentValues();
            values.put("isPublish", itm.isPublish);
            String whereClause = "userId=? and sizeId=?";
            String[] whereArgs = new String[]{String.valueOf(itm.userId),String.valueOf(itm.sizeId)};
            sqLiteDatabase.update(CreatCartDB.SqlName, values, whereClause, whereArgs);
            sqLiteDatabase.setTransactionSuccessful();

        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            sqLiteDatabase.endTransaction();
            sqLiteDatabase.close();
        }
    }

    /**
     * 加入数据
     */
    public static void insertByData(CreatCartDB cartDB,List<CartGoodsItem> cartGoodsItems,CartGoodsItem itm)
    {
        SQLiteDatabase sqLiteDatabase=cartDB.getReadableDatabase();
        sqLiteDatabase.beginTransaction();
        try
        {
            Cursor cursor=sqLiteDatabase.rawQuery("select * from cartItem where userId="+itm.userId+" and sizeId="+itm.sizeId,null);
            if (!cursor.moveToLast())
            {
                ContentValues values = new ContentValues();
                values.put("userId", itm.userId);
                values.put("baseId", itm.id);
                values.put("title", itm.name);
                values.put("img", itm.imageUrl);
                values.put("currentPrice", itm.currentPrice);
                values.put("formalPrice", itm.formalPrice);
                values.put("saveMoney", itm.saveMoney);
                values.put("saveTotalMoney", itm.saveTotalMoney);
                values.put("num", itm.num);
                values.put("size", itm.size);
                values.put("sizeId", itm.sizeId);
                values.put("isPublish", itm.isPublish);
                values.put("isOver", itm.isOver);
                values.put("isOSale", itm.isOSale);
                values.put("creatTime", itm.creatTime);
                sqLiteDatabase.insert(CreatCartDB.SqlName, null, values);
            }
            else
            {
                for (int i = 0; i < cartGoodsItems.size() ; i++)
                {
                    if (cartGoodsItems.get(i).sizeId==itm.sizeId)
                    {
                        ContentValues values = new ContentValues();
                        values.put("num", cartGoodsItems.get(i).num+1);
                        String whereClause = "sizeId=?";
                        String[] whereArgs = new String[] { String.valueOf(itm.sizeId) };
                        sqLiteDatabase.update(CreatCartDB.SqlName,values,whereClause,whereArgs);
                    }
                }
            }
            sqLiteDatabase.setTransactionSuccessful();
            cursor.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            sqLiteDatabase.endTransaction();
            sqLiteDatabase.close();
        }
    }

    /**
     * 清空数据库
     */
    public static void clearAll(CreatCartDB cartDB)
    {
        SQLiteDatabase sqLiteDatabase = cartDB.getReadableDatabase();
        sqLiteDatabase.beginTransaction();
        sqLiteDatabase.execSQL("delete * from cartItem");
        sqLiteDatabase.setTransactionSuccessful();
        sqLiteDatabase.endTransaction();
        sqLiteDatabase.close();

    }

}
