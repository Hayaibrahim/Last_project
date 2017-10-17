package com.example.enghaya.last.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ENG.HAYA on 10/17/2017 AD.
 */

public class ProductDbHelper extends SQLiteOpenHelper {


    public static final String LOG_TAG = ProductDbHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "products.db";

    private static final int DATABASE_VERSION = 1;

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Create a String that contains the SQL statement to create the pets table
        String TABLE =  "CREATE TABLE " + ProductContract.ProductEntry.TABLE_NAME + " ("
                + ProductContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProductContract.ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE + " REAL NOT NULL DEFAULT 0, "
                + ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER + " TEXT NOT NULL, "
                + ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE + " BLOB);";
        // Execute the SQL statement
        db.execSQL(TABLE);
    }
// This is called  upgraded.

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //there's nothing to do be done here.
    }
}
