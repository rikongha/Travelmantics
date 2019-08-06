package com.leroi.travelmantics;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.leroi.travelmantics.model.TravelDeal;
import com.leroi.travelmantics.utils.FirebaseUtil;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class DealActivity extends AppCompatActivity {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private EditText textTitle;
    private EditText price;
    private EditText description;
    TravelDeal deal;
    private ImageView dealImage;
    private static final int PICTURE_REQUEST_CODE = 42;
    private Button mBtnUploadImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);
        FirebaseUtil.openFbReference("traveldeals", this);
        mFirebaseDatabase = FirebaseUtil.sFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.sDatabaseReference;
        textTitle = findViewById(R.id.text_insert_title);
        price = findViewById(R.id.text_insert_price);
        description = findViewById(R.id.text_insert_description);
        dealImage = findViewById(R.id.image_deal_photo);
        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");
        if (deal == null) {
            deal = new TravelDeal();
        }
        this.deal = deal;
        textTitle.setText(deal.getTitle());
        description.setText(deal.getDescription());
        price.setText(deal.getPrice());
        showImage(deal.getImageUrl());
        mBtnUploadImage = findViewById(R.id.btn_deal_upload_image);
        mBtnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*"); //accepts any image format
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent,
                        "Upload photo using"), PICTURE_REQUEST_CODE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_deal_activity, menu);
        if (FirebaseUtil.isAdmin) {
            menu.findItem(R.id.menu_item_delete).setVisible(true);
            menu.findItem(R.id.menu_item_save).setVisible(true);
            enableEditDealInfo(true);
        } else {
            menu.findItem(R.id.menu_item_delete).setVisible(false);
            menu.findItem(R.id.menu_item_save).setVisible(false);
            enableEditDealInfo(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_save:
                saveDeals();
                Toast.makeText(this, "Deal Saved", Toast.LENGTH_LONG).show();
                clean();
                return true;
            case R.id.menu_item_delete:
                deleteDeal();
                Toast.makeText(this, "Deal deleted", Toast.LENGTH_SHORT).show();
                backToList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            final StorageReference reference = FirebaseUtil.sStorageReference
                    .child(Objects.requireNonNull(Objects.requireNonNull(imageUri).getLastPathSegment()));
            reference.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    return reference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUrl = task.getResult();
                        String pictureName = Objects.requireNonNull(task.getResult()).getPath();
                        if (downloadUrl != null) deal.setImageUrl(downloadUrl.toString());
                        deal.setImageName(pictureName);
                        showImage(deal.getImageUrl());
                    }
                }
            });
        }
    }

    /*
     * Clears texts fields after push
     */
    private void clean() {
        textTitle.getText().clear();
        price.getText().clear();
        description.getText().clear();
    }

    /*
     * Pushes data to firebase db
     */
    private void saveDeals() {
        deal.setTitle(textTitle.getText().toString());
        deal.setPrice(price.getText().toString());
        deal.setDescription(description.getText().toString());
        if (deal.getId() == null) {
            mDatabaseReference.push().setValue(deal);
        } else {
            mDatabaseReference.child(deal.getId()).setValue(deal);
        }
    }

    private void deleteDeal() {
        if (deal == null) {
            Toast.makeText(this, "Please save the deal before deleting", Toast.LENGTH_SHORT).show();
            return;
        }
        mDatabaseReference.child(deal.getId()).removeValue();

        if (deal.getImageName() != null && !deal.getImageName().isEmpty()) {
            StorageReference picRef = FirebaseUtil.sFirebaseStorage.getReference().child(deal.getImageName());
            picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("deletesuccess", "succesfully deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("deleteFailed", "succesfully deleted");
                }
            });
        }
    }

    private void backToList() {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    private void enableEditDealInfo(boolean isEnabled) {
        textTitle.setEnabled(isEnabled);
        description.setEnabled(isEnabled);
        price.setEnabled(isEnabled);
        mBtnUploadImage.setEnabled(isEnabled);
    }

    private void showImage(String url) {
        if (url != null && !url.isEmpty()) {
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.get()
                    .load(url)
                    .resize(width, width*2/3)
                    .centerCrop()
                    .into(dealImage);
        }
    }
}
