package com.sokelish.dbhelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @ClassName: DbHelper
 * @Description: 数据库工具类
 * @author lhy
 * @date 2014-10-9 下午2:36:41
 *
 */
public class DbHelper extends SQLiteOpenHelper {
    private static DbHelper dbHelper = null;
//    private OnSqliteUpdateListener onSqliteUpdateListener;
    private static final String dbName = "cdb.db";
    private static final int dbVersion = 1;
    private ReentrantLock lock = new ReentrantLock();//🔒  其实这个锁没必要...
    /**
     * 建表语句列表
     */
    private final String TABLE_NAME = "MulitiTabLe";
    /*id字段*/
    private final String VALUE_ID = "_id";
    private final String VALUE_KEY = "keyStr";
    private final String VALUE_VAL = "valStr";
    private final String VALUE_EXTAR = "extra";
    /*创建表语句 语句对大小写不敏感 create table 表名(字段名 类型，字段名 类型，…)*/
    private final String createTableStr = "create table " + TABLE_NAME + "(" +
            VALUE_ID + " integer primary key," +
            VALUE_KEY + " text ," +
            VALUE_VAL + " text," +
            VALUE_EXTAR + " text" +
            ")";

    private DbHelper(Context context) {
        super(context, dbName, null, dbVersion);
    }


    /**
     * 获取数据库实例
     * @param context
     * @return
     */
    public static DbHelper getInstance(Context context) {
        synchronized (DbHelper.class){
            if (dbHelper == null) {
                dbHelper = new DbHelper(context);
            }
        }
        return dbHelper;
    };

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTableStr);
    }

    public void put(String key,String val){
        try{
            ContentValues contentValues = new ContentValues();
            contentValues.put(VALUE_KEY,key);
            contentValues.put(VALUE_VAL,val);
//            Log.d("put",val+"  锁的hashCode:"+lock.hashCode());
            lock.lock();
//            Log.d("put---进入锁了",val);
            if(get(key)!=null){
                update(TABLE_NAME,contentValues,VALUE_KEY+" = ?",new String[]{key});
            }else {
                insert(TABLE_NAME,contentValues);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
//            Log.d("put---释放了锁",val);
        }
    }


    public String get(String key,String defaultVal){
        String result = get(key);
        if( result == null){
            return defaultVal;
        }else {
            return result;
        }
    }

    public String get(String key){
        try{
            lock.lock();
            Cursor cursor = query(TABLE_NAME,"where "+VALUE_KEY +" = '"+key+"'");
            if(cursor.getCount()==1){
                cursor.moveToFirst();
                int index = cursor.getColumnIndex(VALUE_VAL);
                return cursor.getString(index);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
        return null;
    }

    /**
     *
     * @param key
     * @return -1表示删除失败 1表示成功
     */
    public int remove(String key){
        int result = -1;
        try{
            lock.lock();
            result =  delete(TABLE_NAME,VALUE_KEY+"= ?",new String[]{key});
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
        return result;
    }

    /**
     * Sql写入
     * @param sql
     * @param bindArgs
     */
    private void execSQL(String sql, Object[] bindArgs) {
        synchronized (dbHelper) {
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            database.execSQL(sql, bindArgs);
        }
    }


    /**
     * sql查询
     * @param sql
     * @param bindArgs
     * @return
     */
    private Cursor rawQuery(String sql, String[] bindArgs) {
        synchronized (dbHelper) {
            SQLiteDatabase database = dbHelper.getReadableDatabase();
            Cursor cursor = database.rawQuery(sql, bindArgs);
            return cursor;
        }
    }

    /**
     *
     * @Title: insert
     * @Description: 插入数据
     * @param @param table
     * @param @param contentValues 设定文件
     * @return void 返回类型
     */
    private void insert(String table, ContentValues contentValues) {
        synchronized (dbHelper) {
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            database.insert(table, null, contentValues);
        }
    }

    /**
     *
     * @Title: update
     * @Description: 更新
     * @param @param table
     * @param @param values
     * @param @param whereClause
     * @param @param whereArgs 设定文件
     * @return void 返回类型
     * @throws
     */
    private void update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        synchronized (dbHelper) {
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            database.update(table, values, whereClause, whereArgs);
        }
    }
    /**
     *
     * @Title: delete
     * @Description:删除
     * @param @param table
     * @param @param whereClause
     * @param @param whereArgs
     * @return int 删除的数量， 一般是1，不是1就有问题 --
     */
    private int delete(String table, String whereClause, String[] whereArgs) {
        int result = -1;
        synchronized (dbHelper) {
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            result = database.delete(table, whereClause, whereArgs);
        }
        return result;
    }

    /**
     *
     * @Title: query
     * @Description: 查
     * @param @param table
     * @param @param columns
     * @param @param selection
     * @param @param selectionArgs
     * @param @param groupBy
     * @param @param having
     * @param @param orderBy
     * @return Cursor 一个游标对象
     */
    private Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having,
                        String orderBy) {
        synchronized (dbHelper) {
            SQLiteDatabase database = dbHelper.getReadableDatabase();
            // Cursor cursor = database.rawQuery("select * from "
            // + TableName.TABLE_NAME_USER + " where userId =" + userId, null);
            Cursor cursor = database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
            return cursor;
        }
    }
    /**
     *
     * @Description:查
     * @param table
     * @param columns
     * @param selection
     * @param selectionArgs
     * @param groupBy
     * @param having
     * @param orderBy
     * @param limit
     * @return
     * Cursor
     * @exception:
     * @author: lihy
     * @time:2015-4-3 上午9:37:29
     */
    private Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having,
                        String orderBy, String limit) {
        synchronized (dbHelper) {
            SQLiteDatabase database = dbHelper.getReadableDatabase();
            // Cursor cursor = database.rawQuery("select * from "
            // + TableName.TABLE_NAME_USER + " where userId =" + userId, null);
            Cursor cursor = database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
            return cursor;
        }
    }

    /**
     *
     * @Description 查询，方法重载,table表名，sqlString条件
     * @param @return
     * @return Cursor
     */
    private Cursor query(String tableName, String sqlString) {
        synchronized (dbHelper) {
            SQLiteDatabase database = dbHelper.getReadableDatabase();
            String sqlStr = "select * from " + tableName + " " + sqlString;
            Cursor cursor = database.rawQuery(sqlStr, null);
            return cursor;
        }
    }

    /**
     * 结束的时候请将数据库关闭....
     */
    public void clear() {
        dbHelper.close();
    }

    /**
     * onUpgrade()方法在数据库版本每次发生变化时都会把用户手机上的数据库表删除，然后再重新创建。<br/>
     * 一般在实际项目中是不能这样做的，正确的做法是在更新数据库表结构时，还要考虑用户存放于数据库中的数据不会丢失,从版本几更新到版本几。(非
     * Javadoc)
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
//        if (onSqliteUpdateListener != null) {
//            onSqliteUpdateListener.onSqliteUpdateListener(db, arg1, arg2);
//        }
    }

    //暂时不开放
//    private void setOnSqliteUpdateListener(OnSqliteUpdateListener onSqliteUpdateListener) {
//        this.onSqliteUpdateListener = onSqliteUpdateListener;
//    }
//
//
//    public static interface OnSqliteUpdateListener {
//        public void onSqliteUpdateListener(SQLiteDatabase db, int oldVersion, int newVersion);
//    }
}
