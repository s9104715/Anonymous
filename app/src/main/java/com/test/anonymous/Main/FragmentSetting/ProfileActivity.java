package com.test.anonymous.Main.FragmentSetting;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.test.anonymous.R;
import com.test.anonymous.Tools.Code;
import com.test.anonymous.Tools.Keyboard;
import com.test.anonymous.Tools.LoadingProcessDialog;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import at.markushi.ui.CircleButton;
import cc.cloudist.acplibrary.ACProgressConstant;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private CircleImageView selfie;
    private CircleButton editSelfieBtn;
    private EditText nameET , ageET;
    private ImageView editNameBtn , editAgeBtn , backBtn;
    private RadioGroup genderRG;
    private View genderCoverView;

    //change selfie
    private AlertDialog changeSelfieAD;
    private File selfieFile;
    private Uri selfieUri;

    //Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private StorageReference storageRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_profile);

        selfie = findViewById(R.id.selfie);
        editSelfieBtn = findViewById(R.id.edit_selfie_btn);
        nameET = findViewById(R.id.name_ET);
        ageET = findViewById(R.id.age_ET);
        editNameBtn = findViewById(R.id.edit_name_btn);
        editAgeBtn = findViewById(R.id.edit_age_btn);
        backBtn = findViewById(R.id.back_btn);
        genderRG = findViewById(R.id.gender_RG);
        genderCoverView = findViewById(R.id.gender_cover_view);

        editSelfieBtn.setOnClickListener(this);
        editNameBtn.setOnClickListener(this);
        editAgeBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);
        genderCoverView.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();//storage初始化

        setupUI();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.edit_selfie_btn:
                editSelfie();
                break;
            case R.id.edit_name_btn:

                if(!nameET.isEnabled()){
                    nameET.setEnabled(true);
                    editNameBtn.setImageDrawable(getResources().getDrawable(R.drawable.done));
                    new Keyboard(getSystemService(INPUT_METHOD_SERVICE) , nameET).show();
                }else {
                    editName(nameET.getText().toString());
                    nameET.setEnabled(false);
                    editNameBtn.setImageDrawable(getResources().getDrawable(R.drawable.edit));
                }
                break;
            case R.id.edit_age_btn:
                if(!ageET.isEnabled()){
                    ageET.setEnabled(true);
                    editAgeBtn.setImageDrawable(getResources().getDrawable(R.drawable.done));
                    new Keyboard(getSystemService(INPUT_METHOD_SERVICE) , ageET).show();
                }else {
                    int age = Integer.parseInt(ageET.getText().toString().trim());
                    if(age >= 12 && age <= 99){
                        editAge(Integer.parseInt(ageET.getText().toString().trim()));
                        ageET.setEnabled(false);
                        editAgeBtn.setImageDrawable(getResources().getDrawable(R.drawable.edit));
                    }else {
                        ageET.setError("年齡須介於12~99");
                        ageET.requestFocus();
                        return;
                    }
                }
                break;
            case R.id.back_btn:
                onBackPressed();
                break;
            case R.id.gender_cover_view:
                Toast.makeText(this , getString(R.string.gender_setting_msg) , Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void setupUI(){
        firestore.collection("User").document(auth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Picasso.get().load(documentSnapshot.getString("selfiePath"))
                        //圖片使用最低分辨率,降低使用空間大小
                        .fit()
                        .centerCrop()
                        .into(selfie);//取得大頭貼
                nameET.setText(documentSnapshot.getString("name"));
                ageET.setText(String.valueOf(documentSnapshot.get("age", Integer.TYPE)));
                if(documentSnapshot.getString("gender").equals("男")){
                    genderRG.check(R.id.male_RB);
                }else {
                    genderRG.check(R.id.female_RB);
                }
                nameET.setEnabled(false);
                ageET.setEnabled(false);
                genderRG.setEnabled(false);
            }
        });
    }

    private void editSelfie(){

        View v = getLayoutInflater().inflate(R.layout.dialog_change_selfie,null);
        AlertDialog.Builder ADBuider = new AlertDialog.Builder(this)
                .setTitle("請選擇")
                .setView(v);
        changeSelfieAD = ADBuider.create();
        changeSelfieAD.show();

        v.findViewById(R.id.camera_bnt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSelfieAD.dismiss();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                selfieFile = new File(getExternalCacheDir(),
                        String.valueOf(System.currentTimeMillis()) + ".jpg");
                selfieUri = Uri.fromFile(selfieFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, selfieUri);
                startActivityForResult(intent, Code.CAMERA_REQUEST);
            }
        });

        v.findViewById(R.id.gallery_bnt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSelfieAD.dismiss();
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), Code.GALLERY_REQUEST);
            }
        });

        v.findViewById(R.id.delete_bnt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                changeSelfieAD.dismiss();
                //show loadingPD
                final LoadingProcessDialog loadingPD = new LoadingProcessDialog(ACProgressConstant.DIRECT_CLOCKWISE ,
                        Color.WHITE , false , false , ProfileActivity.this)
                        .show();

                final String defaultSelfieUrl = getResources().getString(R.string.user_default_selfie_url);
                //照片還原成default
                Map<String,Object> update = new HashMap<>();
                update.put("selfiePath" , defaultSelfieUrl);
                firestore.collection("User").document(auth.getCurrentUser().getUid())
                        .set(update , SetOptions.mergeFields("selfiePath"))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //delete selfie storage
                                storageRef.child("UserSelfie/" + auth.getCurrentUser().getUid() + "/selfie.png")
                                        .delete();
                                //renew selfie
                                firestore.collection("User").document(auth.getCurrentUser().getUid()).get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if(documentSnapshot.exists()){
                                                    loadingPD.dismiss();
                                                    //firebase設置大頭貼
                                                    Picasso.get()
                                                            .load(documentSnapshot.getString("selfiePath"))
                                                            //圖片使用最低分辨率,降低使用空間大小
                                                            .fit()
                                                            .centerCrop()
                                                            .into(selfie);
                                                    Toast.makeText(getApplicationContext() , "大頭貼已經刪除" , Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        });
            }
        });
        v.findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSelfieAD.dismiss();
            }
        });
    }

    private void editName(final String name){

        final Map<String , Object> update = new HashMap<>();
        update.put("name" , name);
        firestore.collection("User").document(auth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        //姓名經過更動才需寫入
                        if(!documentSnapshot.getString("name").equals(name)){
                            documentSnapshot.getReference().update(update);
                            Toast.makeText(getApplicationContext() , "修改完成", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void editAge(final int age){

        final Map<String , Object> update = new HashMap<>();
        update.put("age" , age);
        firestore.collection("User").document(auth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        //姓名經過更動才需寫入
                        if(documentSnapshot.get("age" , Integer.TYPE) != age){
                            documentSnapshot.getReference().update(update);
                            Toast.makeText(getApplicationContext() , "修改完成", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = null;
        if(requestCode == Code.CAMERA_REQUEST  && resultCode ==RESULT_OK ){
            uri = selfieUri;
        }else if(requestCode == Code.GALLERY_REQUEST && resultCode ==RESULT_OK  && data!=null){
            uri = data.getData();
        }
        if (uri != null) {

            //show loadingPD
            final LoadingProcessDialog loadingPD = new LoadingProcessDialog(ACProgressConstant.DIRECT_CLOCKWISE ,
                    Color.WHITE , false , false , ProfileActivity.this)
                    .show();

            //upload file
            storageRef.child("UserSelfie/" + auth.getCurrentUser().getUid() + "/selfie.png")
                    .putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            //get download url
                            taskSnapshot.getStorage().getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(final Uri uri) {

                                            //update user document
                                            final Map<String , Object> update = new HashMap<>();
                                            update.put("selfiePath" , uri.toString());
                                            firestore.collection("User").document(auth.getCurrentUser().getUid())
                                                    .update(update);

                                            loadingPD.dismiss();

                                            //load selfie
                                            Picasso.get().load(uri.toString())
                                                    //圖片使用最低分辨率,降低使用空間大小
                                                    .fit()
                                                    .centerCrop()
                                                    .into(selfie);//取得大頭貼

                                            Toast.makeText(ProfileActivity.this , "修改大頭貼成功！" , Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    });
        }
    }
}
