package com.example.enghaya.last;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.enghaya.last.data.ProductContract;

import static com.example.enghaya.last.data.ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE;
import static com.example.enghaya.last.data.ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY;

/**
 * Created by ENG.HAYA on 10/7/2017 AD.
 */

public class ProductCursorAdapter extends CursorAdapter {


    private static final Uri DUMMY_IMAGE_URI = Uri.parse(
            "android.resource://com.example.android.inventory/drawable/ic_empty_shelter");

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }


    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name_text_view);
        TextView priceTextView = (TextView) view.findViewById(R.id.price_text_view);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity_text_view);
        ImageView imageView = (ImageView) view.findViewById(R.id.image_view);

        Button sellButton = (Button) view.findViewById(R.id.sell_button);


        // Find the columns of product attributes that we're interested in
        final int idColumnIndex = cursor.getInt(cursor.getColumnIndex(ProductContract.ProductEntry._ID));
        int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(COLUMN_PRODUCT_QUANTITY);
        int imageColumnIndex = cursor.getColumnIndex(COLUMN_PRODUCT_IMAGE);

        // Read the product attributes from the Cursor for the current product
        String productName = cursor.getString(nameColumnIndex);
        float productPrice = cursor.getFloat(priceColumnIndex);
        final int productQuantity = cursor.getInt(quantityColumnIndex);
        //byte[] productImage = cursor.getBlob(imageColumnIndex);
        String imageString = cursor.getString(imageColumnIndex);
        Uri imageUri = Uri.parse(imageString);

        // Update the TextViews with the attributes for the current product
        nameTextView.setText(productName);
        priceTextView.setText(Float.toString(productPrice));
        quantityTextView.setText(Integer.toString(productQuantity));

        imageView.setImageURI(imageUri);
        imageView.invalidate();
        if (imageView == null) {
            imageView.setImageURI(DUMMY_IMAGE_URI);
        }

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (productQuantity > 0) {
                    int newProductQuantity = productQuantity - 1;

                    Uri currentProductUri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, idColumnIndex);

                    ContentValues values = new ContentValues();
                    values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, newProductQuantity);
                    view.getContext().getContentResolver().update(currentProductUri, values, null, null);

                    quantityTextView.setText(Integer.toString(newProductQuantity));
                }


            }
        });

    }


}
