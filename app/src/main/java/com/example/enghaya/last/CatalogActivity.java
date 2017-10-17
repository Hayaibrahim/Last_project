package com.example.enghaya.last;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
 import android.support.v7.app.AppCompatActivity;
 import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.enghaya.last.data.ProductContract;
import com.example.enghaya.last.data.ProductDbHelper;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String LOG_TAG = CatalogActivity.class.getName();
    private static final int PRODUCT_LOADER = 0;
    // Adapter
    ProductCursorAdapter mCursorAdapter;
     private ProductDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        ListView ListView = (ListView) findViewById(R.id.list_item);
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View empty = findViewById(R.id.isempty);
        ListView.setEmptyView(empty);
        mCursorAdapter = new ProductCursorAdapter(this, null);
        ListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific product that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link ProductEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.products/products/2"
                // if the product with ID 2 was clicked on.
                Uri currentProductUri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentProductUri);

                // Launch the {@link EditorActivity} to display the data for the current product.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);

    }
    private void insertProduct(){
        // Gets the database in write mode
        //SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a ContentValues object where column names are the keys,
        // and Toto's product attributes are the values.
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, "Samsung ");
        contentValues.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, 10);
        contentValues.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, 1);
        contentValues.put(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER, "public");
        contentValues.put(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE, getString(R.string.dummy_image_uri));

        // Insert a new row for Toto in the database, returning the ID of that new row.
        // The first argument for db.insert() is the products table name.
        // The second argument provides the name of a column in which the framework
        // can insert NULL in the event that the ContentValues is empty (if
        // this is set to "null", then the framework will not insert a row when
        // there are no values).
        // The third argument is the ContentValues object containing the info for Toto.
        //long newRowId = db.insert(ProductEntry.TABLE_NAME, null, values);

        Uri mUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, contentValues);
        Long id = Long.valueOf(ContentUris.parseId(mUri));
        Log.v(LOG_TAG," ID"+ id);
    }

    /**
     * Helper method to delete all products in the database.
     */
    private void deleteAllProducts() {
        int Delete = getContentResolver().delete(ProductContract.ProductEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", Delete + "  deleted from product database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.insert:
                // Insert dummy_product
                insertProduct();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.delete_all:
                deleteAllProducts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE};

        return new CursorLoader(this,   // Parent activity context
                ProductContract.ProductEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link ProductCursorAdapter} with this new cursor containing update product data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
