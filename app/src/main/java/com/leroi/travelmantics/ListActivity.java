package com.leroi.travelmantics;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.leroi.travelmantics.adapter.DealAdapter;
import com.leroi.travelmantics.utils.FirebaseUtil;

import static android.widget.LinearLayout.VERTICAL;

public class ListActivity extends AppCompatActivity {

    public ListActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list_activity, menu);
        MenuItem insertMenuItem = menu.findItem(R.id.menu_list_item_insert);
        if (FirebaseUtil.isAdmin) {
            insertMenuItem.setVisible(true);
        } else {
            insertMenuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_list_item_insert:
                Intent intent = new Intent(this, DealActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_list_item_logout:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d("Logout", "User logged out");
                                FirebaseUtil.attachListener();
                            }
                        });
                FirebaseUtil.detachListener();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUtil.openFbReference("traveldeals", this);
        RecyclerView rvDeals = findViewById(R.id.rv_list_deals);
        final DealAdapter dealsAdapter = new DealAdapter(this);
        rvDeals.setAdapter(dealsAdapter);
        LinearLayoutManager dealsLayoutManager =
                new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        DividerItemDecoration itemDecor = new DividerItemDecoration(this, VERTICAL);
        rvDeals.addItemDecoration(itemDecor);
        rvDeals.setLayoutManager(dealsLayoutManager);
        FirebaseUtil.detachListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUtil.attachListener();
    }

}
