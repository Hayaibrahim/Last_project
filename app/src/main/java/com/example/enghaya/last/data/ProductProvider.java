package com.example.enghaya.last.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by ENG.HAYA on 10/7/2017 AD.
 */

public class ProductProvider extends ContentProvider {

    public static final String LOG_TAG = ProductProvider.class.getSimpleName();

    // Database helpe object
    private ProductDbHelper mDbHelper;

    private static final int PRODUCTS = 100;

    private static final int PRODUCT_ID = 101;

    private static final UriMatcher Uri = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {


        // Add 2 content URIs to URI
        Uri.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS);
        Uri.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }


     // Initialize the provider and the database .

    @Override
    public boolean onCreate() {
        // Create and initialize a ProductDbHelper object to gain access to the products database.
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        mDbHelper = new ProductDbHelper(getContext());

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int many = Uri.match(uri);
        switch (many) {
            case PRODUCTS:
                // For the PRODUCTS code, query the products table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the products table.
                cursor = database.query(ProductContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);

                break;
            case PRODUCT_ID:

                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the products table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(ProductContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    /**
     * Insert new data
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = Uri.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not for " + uri);
        }
    }


    private Uri insertProduct(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException(" requires of name");
        }

        // If the price is provided, check that it's greater than or equal to 0 kg
        Integer price = values.getAsInteger(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("requires of price");
        }

        // If the price is provided, check that it's greater than or equal to 0 kg
        Integer quantity = values.getAsInteger(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("  requires of quantity");
        }


        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert a new pet into the products database table with the given ContentValues
        long mid = database.insert(ProductContract.ProductEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (mid == -1) {
            Log.e(LOG_TAG, "Failed to insert " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, mid);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int more = Uri.match(uri);
        switch (more) {
            case PRODUCTS:
                return updateProduct(uri, values, selection, selectionArgs);
            case PRODUCT_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link ProductEntry#COLUMN_PRODUCT_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME)) {
            String nameval = values.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            if (nameval == null) {
                throw new IllegalArgumentException("Product requires of name");
            }
        }

        if (values.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE)) {
            // Check that the price is greater than or equal to 0 units
            Integer price = values.getAsInteger(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Product requires of price");
            }
        }

        if (values.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY)) {
            // Check that the quantity is greater than or equal to 0 units
            Integer integer = values.getAsInteger(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            if (integer != null && integer < 0) {
                throw new IllegalArgumentException("Product requires of price");
            }
        }


        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int mUpdated = database.update(ProductContract.ProductEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (mUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return  updated
        return mUpdated;
    }



     //Delete
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int Deleted;

        final int match = Uri.match(uri);
        switch (match) {
            case PRODUCTS:
                // Delete all rows that match the selection and selection args
                Deleted = database.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                Deleted = database.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion " + uri);
        }

        if (Deleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return deleted
        return Deleted;

    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = Uri.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductContract.ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductContract.ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException(" I Unknown URI " + uri + " with match " + match);
        }
    }
}

