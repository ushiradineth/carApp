package com.celiao.app.customer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.celiao.app.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;

//this is a custom base adapter for the vehicles list view
public class ViewVehiclesActivityCustomBaseAdapter extends BaseAdapter {
    Context context;
    String listVehicle[];
    String listModel[];
    String listCategory[];
    String listPrice[];
    String listDescription[];
    LayoutInflater inflater;

    public ViewVehiclesActivityCustomBaseAdapter(Context context, String[] listVehicle, String[] listModel, String[] listCategory, String[] listPrice, String[] listDescription) {
        this.context = context;
        this.listVehicle = listVehicle;
        this.listModel = listModel;
        this.listCategory = listCategory;
        this.listPrice = listPrice;
        this.listDescription = listDescription;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listVehicle.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.activity_customer_view_vehicles_custom_list_view, null);
        TextView vehicleNo = (TextView) view.findViewById(R.id.vehicleNo);
        TextView model = (TextView) view.findViewById(R.id.model);
        TextView category = (TextView) view.findViewById(R.id.category);
        TextView price = (TextView) view.findViewById(R.id.price);
        TextView description = (TextView) view.findViewById(R.id.description);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);

        //sets vehicle details
        vehicleNo.setText("vehicle: "+listVehicle[i]);
        model.setText("model: "+listModel[i]);
        category.setText("category: "+listCategory[i]);
        price.setText("price: "+listPrice[i]);
        description.setText("description: "+listDescription[i]);

        //gets vehicle image
        FirebaseStorage.getInstance().getReference().child("images/"+listVehicle[i]).getBytes(1024 * 1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageView.setImageBitmap(bitmap);
            }
        });

        return view;
    }
}
