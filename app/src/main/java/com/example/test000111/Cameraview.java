package com.example.test000111;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class Cameraview extends Fragment {
    private View view;
    private ProgressDialog progressDialog;
    private Uri imageUri = null;
    private FirebaseAuth firebaseAuth;
    private String potname = "";
    private String potid = "";
    private String potimage = "";
    Button btn_photo, gallay, savephoto;
    ImageView iv_photo;

    TextView imageText;
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.testaicamera, container, false);

        final Bundle intent = getArguments();

        btn_photo=view.findViewById(R.id.btn_photo);
        gallay=view.findViewById(R.id.gallay);
        savephoto=view.findViewById(R.id.savephoto);
        iv_photo=view.findViewById(R.id.iv_photo);
        imageText=view.findViewById(R.id.imageText);


        firebaseAuth = FirebaseAuth.getInstance(); // Auth???????????????????????? ?????? ????????????//????????????????????? ?????? ???????????? ??????????????? ?????????
        if(imageUri != null) {
            Glide.with(Cameraview.this)
                    .load(imageUri)
                    .placeholder(R.drawable.ic_circle_24)
                    .error(R.drawable.ic_email_24)
                    .centerCrop()
                    .placeholder(R.drawable.ic_florist_24)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(iv_photo);
        }
     //?????? ?????? ???????????? ???????????????//?????? ????????? ??? ????????? ????????????

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("?????? ????????? ????????? ...");
        progressDialog.setCanceledOnTouchOutside(false);
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference temp12 = database.getReference("flowername/");//?????????
        temp12.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                imageText.setText(value);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                imageText.setText("error");
            }
        });


        btn_photo.setOnClickListener(new View.OnClickListener() { //???????????????
            @Override
            public void onClick(View view) {
                pickImageGallery();
            }
        });

        gallay.setOnClickListener(new View.OnClickListener() { //???????????????
            @Override
            public void onClick(View view) {
                pickImageCamera();

            }
        });


        savephoto.setOnClickListener(new View.OnClickListener() {//????????????  ???????????? ????????? ????????? ????????????\
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

        return view;

    }
    private void loadView() {//??? ????????? ???????????? ?????? ????????????? //???????????? ?????? ???????????? ????????? ?????????????????? ?????????????????? ???????????? ??????

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Identify").child("potimage").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String potimage = "" + snapshot.child("potimage").getValue();// ????????? ???????????? ???????????? ????????????.

                Activity activity = getActivity();
                if (activity.isFinishing())
                    return;
//????????? ??????????????? glide??? ????????????. ?????? ????????? ??????????????? ??? ??????????????? ??????????????? ????????????,,?

                Glide.with(Cameraview.this)
                        .load(potimage)
                        .placeholder(R.drawable.ic_circle_24)
                        .error(R.drawable.ic_email_24)
                        .centerCrop()
                        .placeholder(R.drawable.ic_florist_24)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(iv_photo);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void updateProfile(String imageUri){ //????????? ???????????? , ????????????,
        progressDialog.setMessage("Updating user Profile...");
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("potimage","" + imageUri);//????????? ?????? ????????? ????????? ?????? ??????

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users"); //????????? ???????????? ?????? ????????? ????????????
        ref.child(firebaseAuth.getUid()).child("Identify").child("potimage")
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "??????????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "???????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadImage() { //?????? ????????? ??????

        progressDialog.setMessage("updating profile image...");
        progressDialog.show();

        String filePathAndName = ("flower"+(firebaseAuth.getUid())); //????????????????????? Storage??? ?????????
        StorageReference reference = FirebaseStorage.getInstance().getReference(filePathAndName).child("Identify/");

        reference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                        while (!task.isSuccessful());
                        String uploadedImageUri = ""+task.getResult();
                        updateProfile(uploadedImageUri);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "????????? ???????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void pickImageCamera() {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"New Pick");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Sample image description");
        imageUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private void pickImageGallery() { //??????????????? ?????????????????????
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);

    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        iv_photo.setImageURI(imageUri);
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        imageUri = data.getData();
                        iv_photo.setImageURI(imageUri);
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_SHORT).show();
                    }

                }
            }
    );
}
