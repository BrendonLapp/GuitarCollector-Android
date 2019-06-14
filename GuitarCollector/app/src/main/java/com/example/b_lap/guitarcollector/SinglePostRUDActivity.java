package com.example.b_lap.guitarcollector;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class SinglePostRUDActivity extends AppCompatActivity {
    //RUD, not CRUD as this will only Read Update and Delete

    private String mPost_key = null;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private StorageReference storageReference;
    private StorageReference deleteReference;

    private Spinner mTypeSpinner;
    private String mTypeValue;
    private ArrayAdapter<String> spinnerAdapter;

    private EditText mBrandEditText;
    private EditText mModelEditText;
    private EditText mSerialNumberEditText;
    private EditText mStringGaugeEditText;
    private EditText mTuningEditText;
    //private EditText mTypeEditText;

    private String postUid;
    private String mImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_post_rud);

        storageReference = FirebaseStorage.getInstance().getReference();

        mPost_key = getIntent().getExtras().getString("post_id");

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("user_post");

        mBrandEditText = (EditText) findViewById(R.id.single_post_activity_brand_edittext);
        mModelEditText = (EditText) findViewById(R.id.single_post_activity_model_edittext);
        mSerialNumberEditText = (EditText) findViewById(R.id.single_post_activity_serialnumber_edittext);
        mStringGaugeEditText = (EditText) findViewById(R.id.single_post_activity_stringgauge_edittext);
        mTuningEditText = (EditText) findViewById(R.id.single_post_activity_tuning_edittext);
        //mTypeEditText = (EditText) findViewById(R.id.single_post_activity_type_edittext);

        //Set up for the spinner to populate
        mTypeSpinner = (Spinner) findViewById(R.id.single_post_activity_type_spinner);
        spinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.guitar_types));
        mTypeSpinner.setAdapter(spinnerAdapter);
        mTypeValue = mTypeSpinner.getSelectedItem().toString();


        mDatabase.child(mPost_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String postBrand = (String) dataSnapshot.child("brand").getValue();
                String postModel = (String) dataSnapshot.child("model").getValue();
                String postSerialNumber = (String) dataSnapshot.child("serialNumber").getValue();
                String postStringGauge = (String) dataSnapshot.child("stringGauge").getValue();
                String postTuning = (String) dataSnapshot.child("tuning").getValue();
                String postType = (String) dataSnapshot.child("type").getValue();
                postUid = (String) dataSnapshot.child("userId").getValue();
                mImageUrl = (String) dataSnapshot.child("image").getValue();

                mBrandEditText.setText(postBrand);
                mModelEditText.setText(postModel);
                mSerialNumberEditText.setText(postSerialNumber);
                mStringGaugeEditText.setText(postStringGauge);
                mTuningEditText.setText(postTuning);
                //mTypeEditText.setText(postType);
                //Find the value of the spinners selected value
                int spinnerPosition = spinnerAdapter.getPosition(postType);
                mTypeSpinner.setSelection(spinnerPosition);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void onDeleteClick(View view) {
        if (mPost_key != null) {

            mDatabase.child(mPost_key).removeValue();

            Toast.makeText(SinglePostRUDActivity.this, "Deleted", Toast.LENGTH_SHORT).show();

            Intent sendBackIntent = new Intent(SinglePostRUDActivity.this, RetrieveDataActivity.class);
            startActivity(sendBackIntent);
        }
        else {
            Toast.makeText(SinglePostRUDActivity.this, "Lel nope", Toast.LENGTH_SHORT).show();
        }
    }

    public void onUpdateClick (View view) {
        String brand = mBrandEditText.getText().toString();
        String model = mModelEditText.getText().toString();
        String serialNumber = mSerialNumberEditText.getText().toString();
        String stringGauge = mStringGaugeEditText.getText().toString();
        String tuning = mTuningEditText.getText().toString();
        //String type = mTypeEditText.getText().toString();
        mTypeValue = mTypeSpinner.getSelectedItem().toString();

        if (!brand.equals("") && !model.equals("")) {
            //sets up the posting process to the firebase realtime database
            GuitarPost guitarPost = new GuitarPost(brand, model, mTypeValue, serialNumber, tuning, stringGauge, postUid, "something");
            mDatabase.child("user_post").child(mPost_key).setValue(guitarPost);

            Toast.makeText(SinglePostRUDActivity.this, "Upload successful.", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(SinglePostRUDActivity.this, RetrieveDataActivity.class);
            startActivity(intent);
        }
        else {
            Toast.makeText(SinglePostRUDActivity.this, "Failed to upload data.", Toast.LENGTH_SHORT).show();
        }
    }

    public void onCancleClick (View view) {

        Toast.makeText(SinglePostRUDActivity.this, "Canacled updating or deleting post.", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(SinglePostRUDActivity.this, RetrieveDataActivity.class);
        startActivity(intent);
    }
}
