package com.leroi.travelmantics.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.leroi.travelmantics.DealActivity;
import com.leroi.travelmantics.R;
import com.leroi.travelmantics.TakeTripActivity;
import com.leroi.travelmantics.model.TravelDeal;
import com.leroi.travelmantics.utils.FirebaseUtil;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class DealAdapter extends RecyclerView.Adapter<DealAdapter.DealViewHolder> {

    ArrayList<TravelDeal> deals;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildListener;
    private ImageView dealImage;
    private Context mContext;
    private String userId;

    public DealAdapter(Context context) {
        mFirebaseDatabase = FirebaseUtil.sFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.sDatabaseReference;
        deals = FirebaseUtil.mDeals;
        this.mContext = context;
        userId = FirebaseUtil.sFirebaseAuth.getUid();
        mChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                TravelDeal travelDeal = dataSnapshot.getValue(TravelDeal.class);
                Log.d("Deal: ", Objects.requireNonNull(travelDeal).getTitle());
                travelDeal.setId(dataSnapshot.getKey());
                deals.add(travelDeal);
                notifyItemInserted(deals.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mDatabaseReference.addChildEventListener(mChildListener);
    }

    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_deals_row, parent, false);
        return new DealViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DealViewHolder holder, int position) {
        TravelDeal deal = deals.get(position);
        holder.bind(deal);
    }

    @Override
    public int getItemCount() {
        return deals.size();
    }

    public class DealViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTitle;
        TextView tvDescription;
        TextView tvPrice;

        public DealViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_dealsRow_title);
            tvDescription = itemView.findViewById(R.id.tv_dealsRow_description);
            tvPrice = itemView.findViewById(R.id.tv_dealsRow_price);
            dealImage = itemView.findViewById(R.id.image_dealsRow_deal);
            itemView.setOnClickListener(this);
        }

        public void bind(TravelDeal deal) {
            tvTitle.setText(deal.getTitle());
            tvDescription.setText(deal.getDescription());
            NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
            String formattedPrice = format.format(Double.parseDouble(deal.getPrice()));
            tvPrice.setText(String.format(mContext.getResources().getString(R.string.price_value), formattedPrice));
            showImage(deal.getImageUrl());
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Log.d("Click", String.valueOf(position));
            TravelDeal selectedDeal = deals.get(position);
            if (FirebaseUtil.isAdmin) {
                Intent intent = new Intent(view.getContext(), DealActivity.class);
                intent.putExtra("Deal", selectedDeal);
                view.getContext().startActivity(intent);
            } else {
                Intent intent = new Intent(view.getContext(), TakeTripActivity.class);
                intent.putExtra("Deal", selectedDeal);
                view.getContext().startActivity(intent);
            }
        }

        private void showImage(String url) {
            if (url != null && !url.isEmpty()) {
                int widthHeight = 160;
                Picasso.get()
                        .load(url)
                        .placeholder(R.drawable.ic_placeholder_photo)
                        .resize(widthHeight, widthHeight)
                        .centerCrop()
                        .into(dealImage);
            }
        }
    }
}
