package com.example.b_lap.guitarcollector;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class RetrieveDataActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String currentUserId; //sets a variable for the currentUsers id on this activity

    private RecyclerView mPostList;
    private DatabaseReference databaseReference;
    private DatabaseReference mDatabaseReferenceCurrentUser; //for getting the current user to sort with
    private Query mQueryCurrentUser; //for setting a query to sort by current user
    private FirebaseRecyclerAdapter<GuitarPost, PostViewHolder> firebaseRecyclerAdapter;

    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference().child("uploads");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrieve_data);

        mAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("user_post");
        //databaseReference.keepSynced(true);

        //Setting up returning only the current users posts
        currentUserId = mAuth.getCurrentUser().getUid();
        mDatabaseReferenceCurrentUser = FirebaseDatabase.getInstance().getReference().child("user_post");
        mQueryCurrentUser = mDatabaseReferenceCurrentUser.orderByChild("userId").equalTo(currentUserId);

        mPostList = (RecyclerView) findViewById(R.id.retrieve_recyclerview);

        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("user_post");
        Query postQuery = postRef.orderByKey();


        mPostList.setHasFixedSize(true);
        mPostList.setLayoutManager(new LinearLayoutManager(this));

        //The options here sets up the query of where the data will be coming from and what is being looked at, and the data from the class
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<GuitarPost>()
                .setQuery(mQueryCurrentUser, GuitarPost.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<GuitarPost, PostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull GuitarPost model) {

                final String post_key = getRef(position).getKey();

                //This method binds the values that will be displayed to the textviews in the cardview
                holder.setBrand(model.getBrand());
                holder.setModel(model.getModel());
                holder.setSerialNumber(model.getSerialNumber());
                holder.setType(model.getType());
                holder.setTuning(model.getTuning());
                holder.setStringGauge(model.getStringGauge());
                holder.setPostImageView(model.getImage());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent startRUDIntent = new Intent(RetrieveDataActivity.this, SinglePostRUDActivity.class);
                        startRUDIntent.putExtra("post_id", post_key);
                        startActivity(startRUDIntent);
                    }
                });

            }

            @Override
            public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                //Inflates the layout of the recycler views card view for display
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.guitar_post_item, parent, false);

                return new PostViewHolder(view);

            }
        };

        mPostList.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {

        View mView;
        ImageView postImageView;

        public PostViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            postImageView = (ImageView) itemView.findViewById(R.id.card_image_imageview);
        }

        public void setBrand(String brand) {
            TextView brandView = (TextView) mView.findViewById(R.id.card_brand_textview);
            brandView.setText(brand);
        }

        public void setModel(String model) {
            TextView modelView = (TextView) mView.findViewById(R.id.card_model_textview);
            modelView.setText(model);
        }

        public void setType(String type) {
            TextView typeView = (TextView) mView.findViewById(R.id.card_type_textview);
            typeView.setText(type);
        }

        public void setSerialNumber(String serialNumber) {
            TextView serialNumberView = (TextView) mView.findViewById(R.id.card_serialnumber_textview);
            serialNumberView.setText(serialNumber);
        }

        public void setTuning(String tuning) {
            TextView tuningView = (TextView) mView.findViewById(R.id.card_tuning_textview);
            tuningView.setText(tuning);
        }

        public void setStringGauge(String stringGauge) {
            TextView stringGaugeView = (TextView) mView.findViewById(R.id.card_string_gauge_textview);
            stringGaugeView.setText(stringGauge);
        }

        public void setPostImageView(String image) {
            ImageView imageView = (ImageView) mView.findViewById(R.id.card_image_imageview);
            Picasso.get().load(image).into(imageView);

            //Glide.with().load(image).into(imageView);
            //Picasso.get().load(image).into(imageView);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        return super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_post:
                Intent postFormIntent = new Intent(this, PostDataActivity.class);
                startActivity(postFormIntent);
                return true;
            case R.id.menu_item_retrieve:
                Intent retrieveListIntent = new Intent(this,RetrieveDataActivity.class);
                startActivity(retrieveListIntent);
                return true;
            case R.id.menu_item_signout:
                FirebaseAuth.getInstance().signOut();
                Intent signOutIntent = new Intent (this, MainActivity.class);
                startActivity(signOutIntent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
