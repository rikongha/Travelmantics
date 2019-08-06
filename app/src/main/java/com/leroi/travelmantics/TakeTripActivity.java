package com.leroi.travelmantics;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.leroi.travelmantics.model.TravelDeal;
import com.leroi.travelmantics.utils.FirebaseUtil;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.Locale;

public class TakeTripActivity extends AppCompatActivity {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    TravelDeal deal;
    ImageView ivDealImage;
    TextView tvDealTitle;
    TextView tvDealDescription;
    TextView tvDealPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_trip);

        FirebaseUtil.openFbReference("traveldeals", this);
        mFirebaseDatabase = FirebaseUtil.sFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.sDatabaseReference;

        //initialize views
        tvDealTitle = findViewById(R.id.text_trip_title);
        tvDealPrice = findViewById(R.id.text_trip_price);
        tvDealDescription = findViewById(R.id.text_trip_description);
        ivDealImage = findViewById(R.id.image_trip_photo);

        Intent intent = getIntent();
        deal = (TravelDeal) intent.getSerializableExtra("Deal");
        if (deal == null) {
            deal = new TravelDeal();
        }

        tvDealTitle.setText(deal.getTitle());
        tvDealDescription.setText(deal.getDescription());
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        String formattedPrice = format.format(Double.parseDouble(deal.getPrice()));
        tvDealPrice.setText(String.format(getResources().getString(R.string.take_trip_price), formattedPrice));
        showImage(deal.getImageUrl());
        Button btnTakeTrip = findViewById(R.id.btn_trip_take_trip);
        btnTakeTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Your request is processing.. You will be updated via email shortly", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showImage(String url) {
        if (url != null && !url.isEmpty()) {
            Picasso.get()
                    .load(url)
                    .fit()
                    .into(ivDealImage);
        }
    }
}
