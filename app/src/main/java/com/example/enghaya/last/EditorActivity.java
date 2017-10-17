package com.example.enghaya.last;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.enghaya.last.data.ProductContract;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ENG.HAYA on 10/7/2017 AD.
 */

public class  EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    /** Identifier for the product data loader */
    private static final int EXISTING_PRODUCT_LOADER = 0;

    private static final int PERMISSIONS_REQUEST = 1;

    private static final int IMAGE_REQUEST = 2;



    /** Content URI for the existing product (null if it's a new product) */
    private Uri ProductUri;

    /** EditText field to enter the product's name */
    private EditText mEditTextName;

    /** EditText field to enter the product's price */
    private EditText mEditTextprice;

    /** EditText field to enter the product's quantity */
    private EditText  EditTextmQuantity;

    /** EditText field to enter the product's quantity */
    private EditText  EditTextmSupplier;

    /** ImageView field to enter the product's image */
    private ImageView mImageView;
    private Uri mImageUri;

    private Button mAddButton;
    private Button mSubButton;
    private Button mOrderButton;

    private int quantity;
    private String order =" ";

    /** Boolean flag that keeps track of whether the product has been edited (true) or not (false) */
    private boolean mChanged = false;
    private boolean mSave = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mProductHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one.
        Intent intent = getIntent();
        ProductUri = intent.getData();


        // Find all relevant views that we will need to read user input from
        mEditTextName = (EditText) findViewById(R.id.name);
        mEditTextprice = (EditText) findViewById(R.id.price);
        EditTextmQuantity = (EditText) findViewById(R.id.quantity);
        EditTextmSupplier = (EditText) findViewById(R.id.supplier);
        mImageView = (ImageView) findViewById(R.id.editor);
        mAddButton = (Button) findViewById(R.id.Add);
        mSubButton = (Button) findViewById(R.id.sub);
        mOrderButton = (Button) findViewById(R.id.order);

        // If the intent DOES NOT contain a product content URI, then we know that we are
        // creating a new product.
        if (ProductUri == null) {
            // This is a new product, so change the app bar to say "Add a Product"
            setTitle(getString(R.string.editor_activity_title_new_product));


            mAddButton.setVisibility(View.GONE);
            mSubButton.setVisibility(View.GONE);
            mOrderButton.setVisibility(View.GONE);

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit Product"
            setTitle(getString(R.string.editor_activity_title_edit_product));

            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }



        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mEditTextName.setOnTouchListener(mTouchListener);
        mEditTextprice.setOnTouchListener(mTouchListener);
        EditTextmQuantity.setOnTouchListener(mTouchListener);
        EditTextmSupplier.setOnTouchListener(mTouchListener);
        mImageView.setOnTouchListener(mTouchListener);
        mAddButton.setOnTouchListener(mTouchListener);
        mSubButton.setOnTouchListener(mTouchListener);
        mOrderButton.setOnTouchListener(mTouchListener);

        decreaseQuantity();
        increaseQuantity();
        orderMore();
        addImage();

    }




    /**
     * Get user input from editor and save product into database.
     */
    private boolean saveProduct() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mEditTextName.getText().toString().trim();
        String priceString = mEditTextprice.getText().toString().trim();
        String quantityString = EditTextmQuantity.getText().toString().trim();
        String supplierString = EditTextmSupplier.getText().toString().trim();

        // Check if this is supposed to be a new product
        // and check if all the fields in the editor are blank
        if (ProductUri == null &&
                TextUtils.isEmpty(nameString) ||
                TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(quantityString) ||
                TextUtils.isEmpty(supplierString) ||
                mImageUri == null) {
            // Since no fields were modified, we can return early without creating a new product.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            Toast.makeText(this, getString(R.string.details_required),
                    Toast.LENGTH_SHORT).show();
            mSave = false;
            return mSave;
        }

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();

        if (mImageUri == null) {
            Toast.makeText(this, getString(R.string.image_required),
                    Toast.LENGTH_SHORT).show();
            mSave = false;
            return mSave;
        }
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE, mImageUri.toString());

        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, getString(R.string.name_required),
                    Toast.LENGTH_SHORT).show();
            mSave = false;
            return mSave;
        }

        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, priceString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER, supplierString);
        // If the quantity is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        // Determine if this is a new or existing product by checking if mCurrentProductUri is null or not
        if (ProductUri == null) {
            // This is a NEW product, so insert a new product into the provider,
            // returning the content URI for the new product.
            Uri newUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING product, so update the product with content URI: mCurrentProductUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentProductUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(ProductUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        mSave = true;
        return mSave;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (ProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to database
                saveProduct();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the product table
        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                ProductUri,     // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER);
            int imageColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE);



            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            float price = cursor.getFloat(priceColumnIndex);
            quantity = cursor.getInt(quantityColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            String imageUriString = cursor.getString(imageColumnIndex);


            // Update the views on the screen with the values from the database
            mEditTextName.setText(name);
            mEditTextprice.setText(Float.toString(price));
            EditTextmQuantity.setText(Integer.toString(quantity));
            EditTextmSupplier.setText(supplier);
            mImageUri = Uri.parse(imageUriString);
            mImageView.setImageURI(mImageUri);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mEditTextName.setText("");
        mEditTextprice.setText("");
        EditTextmQuantity.setText("");
        EditTextmSupplier.setText("");

    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (ProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(ProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    private void increaseQuantity() {
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (EditTextmQuantity.getText().toString().equals(null) ||
                        EditTextmQuantity.getText().toString().equals("")) {
                    Toast.makeText(EditorActivity.this, getString(R.string.quantity_null_value),
                            Toast.LENGTH_SHORT).show();
                } else {
                    quantity = Integer.parseInt(EditTextmQuantity.getText().toString());
                    EditTextmQuantity.setText(String.valueOf(quantity + 1));
                }
            }
        });
    }

    private void decreaseQuantity() {
        mSubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (EditTextmQuantity.getText().toString().equals(null) ||
                        EditTextmQuantity.getText().toString().equals("")) {
                    Toast.makeText(EditorActivity.this, getString(R.string.quantity_null_value),
                            Toast.LENGTH_SHORT).show();
                } else if (quantity < 2) {
                    Toast.makeText(EditorActivity.this, getString(R.string.quantity_negative),
                            Toast.LENGTH_SHORT).show();
                } else {
                    quantity = Integer.parseInt(EditTextmQuantity.getText().toString());
                    EditTextmQuantity.setText(String.valueOf(quantity - 1));
                }
            }
        });
    }

    private void orderMore() {
        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                order = mEditTextName.getText().toString();
                Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
                mailIntent.setData(Uri.parse(getString(R.string.supplier_email)));
                mailIntent.putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.supplier_email_subject));
                mailIntent.putExtra(Intent.EXTRA_TEXT,
                        getString(R.string.supplier_email_text)+" "+order+":");
                if (mailIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mailIntent);
                }
            }
        });
    }
    private void addImage() {
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // for opening image folder
                requestPermissions();
                mSave = true;
            }
        });
    }

    public void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST);
            }
        } else {
            mImageView.setEnabled(true);
            openImageSelector();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    openImageSelector();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                mImageUri = data.getData();

                mImageView.setImageURI(mImageUri);
                mImageView.setImageBitmap(getBitmapFromUri(mImageUri));
                mImageView.invalidate();
            }
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {
        if (uri == null || uri.toString().isEmpty()) {
            return null;
        }

        // Get the dimensions of the View
        int targetWidth =  mImageView.getWidth();
        int targetHeight =  mImageView.getHeight();

        InputStream inputStream = null;
        try {
            inputStream = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, bmOptions);
            inputStream.close();

            int photoWidth = bmOptions.outWidth;
            int photoHeight = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoWidth / targetWidth, photoHeight / targetHeight);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            inputStream = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, bmOptions);
            inputStream.close();
            return bitmap;

        } catch (FileNotFoundException noFile) {
            Log.e(LOG_TAG, "Failed to load image.", noFile);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                inputStream.close();
            } catch (IOException ioe) {
            }
        }
    }

    private void openImageSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_REQUEST);
    }
}