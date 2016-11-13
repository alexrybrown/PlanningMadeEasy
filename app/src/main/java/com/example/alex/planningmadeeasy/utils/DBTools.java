package com.example.alex.planningmadeeasy.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageStats;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by noahblumenfeld on 11/12/16.
 */

public class DBTools extends SQLiteOpenHelper {

    private final static String DATABASE_NAME = "PlanningMadeEasy10.db";
    private final static int DATABSAE_VERSION = 1;

    public static final String TABLE_GOALS = "goals";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_GOALKEY = "GoalKey";
    public static final String COLUMN_PARENTKEY = "ParentKey";

    private static final String TABLE_GOALS_CREATE =
            "CREATE TABLE " + TABLE_GOALS + " (" + COLUMN_GOALKEY + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_MESSAGE + " TEXT, " + COLUMN_PARENTKEY + " INTEGER, FOREIGN KEY(" + COLUMN_PARENTKEY +
                    ") REFERENCES "+ TABLE_GOALS + "("+COLUMN_GOALKEY+"))";

    public static final String TABLE_USER = "user";
    public static final String COLUMN_F_NAME = "F_Name";
    public static final String COLUMN_L_NAME = "L_Name";
    public static final String COLUMN_USERNAME = "Username";
    public static final String COLUMN_EMAIL = "Email";
    public static final String COLUMN_PASSWORD = "Password";
    public static final String COLUMN_ID = "userID";

    private static final String TABLE_USER_CREATE =
            "CREATE TABLE " + TABLE_USER + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_F_NAME + " TEXT, " + COLUMN_L_NAME + " TEXT, "+ COLUMN_USERNAME + " TEXT UNIQUE, "
                    + COLUMN_EMAIL + " TEXT, " + COLUMN_PASSWORD + " TEXT)";


    public static final String TABLE_USER_HAS_GOALS = "User_has_Goals";
    public static final String COLUMN_USERID = "ID";
    public static final String COLUMN_GOAL = "GoalKey";

    private static final String TABLE_USER_HAS_GOALS_CREATE =
            "CREATE TABLE " + TABLE_USER_HAS_GOALS + " (" + COLUMN_USERID + " INTEGER REFERENCES " + TABLE_USER + "(" + COLUMN_ID + "), "
                    + COLUMN_GOAL + " INTEGER REFERENCES " + TABLE_GOALS + "(" + COLUMN_GOALKEY + "))";

    public DBTools(Context context) {
        super(context, DATABASE_NAME, null, DATABSAE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(TABLE_GOALS_CREATE);
        sqLiteDatabase.execSQL(TABLE_USER_CREATE);
        sqLiteDatabase.execSQL(TABLE_USER_HAS_GOALS_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS" + TABLE_GOALS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS" + TABLE_USER);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS" + TABLE_USER_HAS_GOALS);
        onCreate(sqLiteDatabase);
    }

    public User createUser(User queryValues) throws SQLiteConstraintException{
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("F_Name", queryValues.F_Name);
        values.put("L_Name", queryValues.L_Name);
        values.put("Username", queryValues.Username);
        values.put("Email", queryValues.Email);
        values.put("Password", queryValues.Password);
        queryValues.userID = database.insertOrThrow("user", null, values);
        database.close();
        return queryValues;
    }

    public User getUser (String username){
        String query = "Select * from user where Username ='"+username+"'";
        User myUser = new User(0,"","","",username,"");
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(query, null);
        if (cursor.moveToFirst()){
            do {
                myUser.userID=cursor.getLong(0);
                myUser.F_Name=cursor.getString(1);
                myUser.L_Name=cursor.getString(2);
                myUser.Username=cursor.getString(3);
                myUser.Email=cursor.getString(4);
                myUser.Password=cursor.getString(5);
            } while (cursor.moveToNext());
        }
        return myUser;
    }

    public Goal insertGoal(Goal queryValues){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("message", queryValues.message);
        values.put(COLUMN_PARENTKEY, queryValues.parentKey);
        queryValues.goalKey = database.insert("goals", null, values);
        ContentValues user_goals = new ContentValues();
        user_goals.put(COLUMN_USERID, queryValues.user.userID);
        user_goals.put(COLUMN_GOAL, queryValues.goalKey);
        database.insert(TABLE_USER_HAS_GOALS, null, user_goals);
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_USER_HAS_GOALS, null);
        database.close();
        return queryValues;
    }

    public ArrayList<Goal> getGoals(String username) {
        User user = this.getUser(username);
        String query = "SELECT " + COLUMN_GOAL + " FROM " + TABLE_USER_HAS_GOALS + " WHERE "
                + COLUMN_USERID + "=" + user.userID;
        ArrayList<Goal> goals = new ArrayList<Goal>();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(query, null);
        if(cursor.moveToFirst()) {
            do {
                Goal goal = getGoal(cursor.getLong(0), user);
                if (goal.parentKey == 0) {
                    goals.add(goal);
                }
            } while (cursor.moveToNext());
        }
        return goals;
    }

    public ArrayList<Goal> getGoals(Goal goal) {
        String query = "SELECT " + COLUMN_GOALKEY + " FROM " + TABLE_GOALS + " WHERE "
                + COLUMN_PARENTKEY + "=" + goal.goalKey;
        ArrayList<Goal> goals = new ArrayList<Goal>();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(query, null);
        if(cursor.moveToFirst()) {
            do {
                Goal childGoal = getGoal(cursor.getLong(0), goal.user);
                goals.add(childGoal);
            } while (cursor.moveToNext());
        }
        return goals;
    }

    public Goal getGoal(long goalKey, User user) {
        String query = "Select * from " + TABLE_GOALS + " where " + COLUMN_GOALKEY + "=" + goalKey;
        Goal myGoal = new Goal(goalKey, "", 0, user);
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(query, null);
        if (cursor.moveToFirst()){
            do {
                myGoal.goalKey=cursor.getLong(0);
                myGoal.message=cursor.getString(1);
                myGoal.parentKey=cursor.getLong(2);
            } while (cursor.moveToNext());
        }
        return myGoal;
    }

    public Goal getParentGoal(long goalKey, User user) {
        Goal goal = this.getGoal(goalKey, user);
        if(goal.parentKey != 0) {
            String query = "Select " + COLUMN_PARENTKEY + " from " + TABLE_GOALS + " where " + COLUMN_GOALKEY + "=" + goal.goalKey;
            Goal myGoal = new Goal(goalKey, "", 0, user);
            SQLiteDatabase database = this.getReadableDatabase();
            Cursor cursor = database.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {
                    long key = cursor.getLong(0);
                    myGoal = this.getGoal(key, user);
                } while (cursor.moveToNext());
            }
            return myGoal;
        } else {
            return goal;
        }
    }

    public ArrayList<Goal> getPrimaryGoals(User user) {
        String query = "SELECT " + COLUMN_GOALKEY + " FROM " + TABLE_GOALS + " WHERE "
                + COLUMN_PARENTKEY + "=0";
        ArrayList<Goal> goals = new ArrayList<Goal>();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(query, null);
        if(cursor.moveToFirst()) {
            do {
                Goal childGoal = getGoal(cursor.getLong(0), user);
                goals.add(childGoal);
            } while (cursor.moveToNext());
        }
        return goals;
    }
}
