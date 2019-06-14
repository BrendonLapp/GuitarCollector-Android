package com.example.b_lap.guitarcollector;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Camera;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.net.URI;

public class PostDataActivity extends AppCompatActivity {

    private static final String TAG = "PostDataActivity";

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private String mUserId;

    private Spinner mTypeSpinner;
    private String mTypeValue;
    private Button mSubmitButton;
    private EditText mModelEdittext;
    private EditText mBrandEdittext;
    private EditText mTypeEdittext;
    private EditText mSerialNumberEdittext;
    private EditText mTuningEdittext;
    private EditText mStringGaugeEdittext;

    private ImageView mPickImageVIew;
    private Uri mImageUri;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_data);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = mFirebaseDatabase.getReference();

        //Sets the storage to be looking into the uploads node in the file storage
        mStorage = FirebaseStorage.getInstance().getReference("uploads");

        user = mAuth.getCurrentUser();
        mUserId = user.getUid(); //Allows for the addition of userId in the GuitarPost

        mSubmitButton = (Button) findViewById(R.id.post_activity_submit_button);
        mModelEdittext = (EditText) findViewById(R.id.post_activity_model_edittext);
        mBrandEdittext = (EditText) findViewById(R.id.post_activity_brand_edittext);
        //mTypeEdittext = (EditText) findViewById(R.id.post_activity_type_edittext);
        mSerialNumberEdittext = (EditText) findViewById(R.id.post_activity_serialnumber_edittext);
        mTuningEdittext = (EditText) findViewById(R.id.post_activity_tuning_edittext);
        mStringGaugeEdittext = (EditText) findViewById(R.id.post_activity_stringgauge_edittext);
        mPickImageVIew = (ImageView) findViewById(R.id.post_activity_pick_image_imageview);

        //Set up for the spinner to populate
        mTypeSpinner = (Spinner) findViewById(R.id.post_activity_type_spinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.guitar_types));
        mTypeSpinner.setAdapter(spinnerAdapter);

        //Listens for anything going on with the authentication for the app.
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "Signed in: ");
                    //Toast.makeText(PostDataActivity.this, "User signed in", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(PostDataActivity.this, "User is signed out", Toast.LENGTH_SHORT).show();
                }
            }
        };

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Failed to read the values", databaseError.toException());
            }
        });

        mPickImageVIew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);

                intent.setType("image/*");

                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            mImageUri = data.getData();

            mPickImageVIew.setImageURI(mImageUri);
        }
    }

    private String getFileExtension(Uri uri) {
        //gets the file extension from the file, like .jpg or what the user has
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return  mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    public void SubmitOnClick(View view) {
        final String brand = mBrandEdittext.getText().toString();
        final String model = mModelEdittext.getText().toString();
        //String type = mTypeValue;
        final String serialNumber = mSerialNumberEdittext.getText().toString();
        final String tuning = mTuningEdittext.getText().toString();
        final String stringGauge = mStringGaugeEdittext.getText().toString();
        final String userId = mUserId;
        mTypeValue = mTypeSpinner.getSelectedItem().toString();

        //Sets its storage name to be the current milli second and the file extension from the users uploaded image
        final StorageReference fileRef = mStorage.child(userId + System.currentTimeMillis() + "." + getFileExtension(mImageUri));

        UploadTask uploadTask = fileRef.putFile(mImageUri);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        GuitarPost guitarPost = new GuitarPost(
                            brand,
                            model,
                            mTypeValue,
                            serialNumber,
                            tuning,
                            stringGauge,
                            userId,
                            uri.toString());
                    mDatabase.child("user_post").push().setValue(guitarPost);

                    Toast.makeText(PostDataActivity.this, "Upload successful.", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(PostDataActivity.this, RetrieveDataActivity.class);
                    startActivity(intent);
                    }
                });
            }
        });


        //These two noted sections have non-working uploads for the image uri
//                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                //String uploadUri = taskSnapshot.getUploadSessionUri().toString();
//
//                //fileRef.getDownloadUrl();
//                String uri = fileRef.getDownloadUrl().toString();
//                //The !brand and !model make it so those fields are required
//                if (!brand.equals("") && !model.equals("")) {
//                    //sets up the posting process to the firebase realtime database
//                    GuitarPost guitarPost = new GuitarPost(
//                            brand,
//                            model,
//                            mTypeValue,
//                            serialNumber,
//                            tuning,
//                            stringGauge,
//                            userId,
//                            uri);
//                    mDatabase.child("user_post").push().setValue(guitarPost);
//
//                    Toast.makeText(PostDataActivity.this, "Upload successful.", Toast.LENGTH_SHORT).show();
//
//                    Intent intent = new Intent(PostDataActivity.this, RetrieveDataActivity.class);
//                    startActivity(intent);
//                }
//                else {
//                    Toast.makeText(PostDataActivity.this, "Failed to upload data.", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(PostDataActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
//            }
//        });

//        //The !brand and !model make it so those fields are required
//        if (!brand.equals("") && !model.equals("")) {
//            //sets up the posting process to the firebase realtime database
//            GuitarPost guitarPost = new GuitarPost(brand, model, mTypeValue, serialNumber, tuning, stringGauge, userId, uploadUri);
//            mDatabase.child("user_post").push().setValue(guitarPost);
//
//            Toast.makeText(PostDataActivity.this, "Upload successful.", Toast.LENGTH_SHORT).show();
//
//            Intent intent = new Intent(PostDataActivity.this, RetrieveDataActivity.class);
//            startActivity(intent);
//        }
//        else {
//            Toast.makeText(PostDataActivity.this, "Failed to upload data.", Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
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
