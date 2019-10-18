package com.test.anonymous.Main.FragmentSetting;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.test.anonymous.R;
import com.test.anonymous.Tools.Code;
import com.test.anonymous.Tools.Keyboard;
import com.test.anonymous.Tools.LoadingProcessDialog;
import com.test.anonymous.Tools.RecyclerViewTools.CareerList.CareerAdapter;
import com.test.anonymous.Tools.RecyclerViewTools.HobbyList.HobbyAdapter;
import com.test.anonymous.Tools.RecyclerViewTools.CareerList.ItemCareer;
import com.test.anonymous.Tools.RecyclerViewTools.HobbyList.ItemHobby;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import at.markushi.ui.CircleButton;
import cc.cloudist.acplibrary.ACProgressConstant;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private CircleImageView selfie;
    private CircleButton editSelfieBtn;
    private EditText nameET , ageET;
    private ImageView editNameBtn , editAgeBtn , editCareerBtn , fixCareerBtn , editHobbyBtn , fixHobbyBtn ,  backBtn;
    private RadioGroup genderRG;
    private View genderCoverView;
    //career list
    private List<ItemCareer> userCareer;
    private RecyclerView careerList;
    private CareerAdapter careerAdapter;
    //fix career list
    private List<ItemCareer> careers;
    private RecyclerView fixCareerList;
    private CareerAdapter fixCareerAdapter;
    //hobby list
    private List<ItemHobby> userHobbies;
    private RecyclerView hobbyList;
    private HobbyAdapter hobbyAdapter;
    //fix hobby list
    private List<ItemHobby> hobbies;
    private RecyclerView fixHobbyList;
    private HobbyAdapter fixHobbyAdapter;
    private RecyclerView.LayoutManager layoutManager;

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
        editCareerBtn = findViewById(R.id.edit_career_btn);
        editHobbyBtn = findViewById(R.id.edit_hobby_btn);
        fixCareerBtn = findViewById(R.id.fix_edit_career_btn);
        fixHobbyBtn = findViewById(R.id.fix_edit_hobby_btn);
        backBtn = findViewById(R.id.back_btn);
        careerList = findViewById(R.id.career_list);
        hobbyList = findViewById(R.id.hobby_list);
        fixCareerList = findViewById(R.id.fix_career_list);
        fixHobbyList = findViewById(R.id.fix_hobby_list);
        genderRG = findViewById(R.id.gender_RG);
        genderCoverView = findViewById(R.id.gender_cover_view);

        editSelfieBtn.setOnClickListener(this);
        editNameBtn.setOnClickListener(this);
        editAgeBtn.setOnClickListener(this);
        editCareerBtn.setOnClickListener(this);
        editHobbyBtn.setOnClickListener(this);
        fixCareerBtn.setOnClickListener(this);
        fixHobbyBtn.setOnClickListener(this);
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
                editName();
                break;
            case R.id.edit_age_btn:
                editAge();
                break;
            case R.id.edit_career_btn:
                editCareer();
                break;
            case R.id.edit_hobby_btn:
                editHobby();
                break;
            case R.id.fix_edit_career_btn:
                doneEditCareer();
                break;
            case R.id.fix_edit_hobby_btn:
                doneEditHobby();
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
        selfie.bringToFront();
        editSelfieBtn.bringToFront();
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
        //career list
        userCareer = new ArrayList<>();
        //load user career data
        firestore.collection("User").document(auth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        userCareer.add(new ItemCareer(documentSnapshot.getString("career") , false));
                        careerAdapter = new CareerAdapter(userCareer);
                        setupCareerRecyclerView(careerList  , careerAdapter  , false);//load
                    }
                });
        //fix career list
        careers = new ArrayList<>();
        firestore.collection("AppData").document("CareerData").collection("Data")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                            //add list
                            careers.add(new ItemCareer(documentSnapshot.getString("name") , false));
                        }
                        careers.add(new ItemCareer("" , false));//其他
                        fixCareerAdapter = new CareerAdapter(careers);
                        setupCareerRecyclerView(fixCareerList , fixCareerAdapter  , true);//load
                    }
                });
        //hobby list
        userHobbies = new ArrayList<>();
        firestore.collection("User").document(auth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        for(String hobby : (List<String>) documentSnapshot.get("hobbies")){
                            userHobbies.add(new ItemHobby(hobby , false));
                        }
                        hobbyAdapter = new HobbyAdapter(userHobbies);
                        setupHobbyRecyclerView(hobbyList , hobbyAdapter  , false);//load
                    }
                });
        //fix hobby list
        hobbies = new ArrayList<>();
        firestore.collection("AppData").document("HobbyData").collection("Data")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                            //add list
                            hobbies.add(new ItemHobby(documentSnapshot.getString("name") , false));
                        }
                        hobbies.add(new ItemHobby("" , false));//其他
                        fixHobbyAdapter = new HobbyAdapter(hobbies);
                        setupHobbyRecyclerView(fixHobbyList , fixHobbyAdapter , true);//load
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

    private void editName(){
        if(!nameET.isEnabled()){
            nameET.setEnabled(true);
            editNameBtn.setImageDrawable(getResources().getDrawable(R.drawable.done));
            new Keyboard(getSystemService(INPUT_METHOD_SERVICE) , nameET).show();
        }else {
            updateName(nameET.getText().toString());
            nameET.setEnabled(false);
            editNameBtn.setImageDrawable(getResources().getDrawable(R.drawable.edit));
        }
    }

    private void updateName(final String name){
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

    private void editAge(){
        if(!ageET.isEnabled()){
            ageET.setEnabled(true);
            editAgeBtn.setImageDrawable(getResources().getDrawable(R.drawable.done));
            new Keyboard(getSystemService(INPUT_METHOD_SERVICE) , ageET).show();
        }else {
            int age = Integer.parseInt(ageET.getText().toString().trim());
            if(age >= 12 && age <= 99){
                updateAge(Integer.parseInt(ageET.getText().toString().trim()));
                ageET.setEnabled(false);
                editAgeBtn.setImageDrawable(getResources().getDrawable(R.drawable.edit));
            }else {
                ageET.setError("年齡須介於12~99");
                ageET.requestFocus();
                return;
            }
        }
    }

    private void updateAge(final int age){
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

    private void editCareer(){
        if(fixCareerList.getVisibility() == View.GONE){
            //visible fix list
            fixCareerList.setVisibility(View.VISIBLE);
            fixCareerBtn.setVisibility(View.VISIBLE);

            for(int i = 0 ; i < careers.size() ; i ++){
                if(careers.get(i).getCareer().equals(userCareer.get(0).getCareer())){
                    //檢查符合的職業
                    fixCareerAdapter.select(i);
                    break;
                }else if(i == careers.size() -1){
                    //檢查到盡頭都沒有符合者
                    //代表該使用者的職業屬於'其他'
                    fixCareerAdapter.selectOther(userCareer.get(0).getCareer());
                }
            }
        }
    }

    private void doneEditCareer(){
        if(fixCareerList.getVisibility() == View.VISIBLE){
            String career;
            if(fixCareerAdapter.getSelectedItemPosition() == fixCareerAdapter.getList().size() -1){
                //選擇其他
                career = fixCareerAdapter.getList().get(fixCareerAdapter.getSelectedItemPosition()).getOtherCareer();
            }else {
                career = fixCareerAdapter.getList().get(fixCareerAdapter.getSelectedItemPosition()).getCareer();
            }
            //比對
            if(!career.isEmpty()){
                //init fix list
                fixCareerAdapter.unSelect(fixCareerAdapter.getSelectedItemPosition());
                //change career list
                careerAdapter.delete(0);
                careerAdapter.add(new ItemCareer(career , false));
                //db
                updateCareer(career);
            }else {
                fixCareerAdapter.unSelect(fixCareerAdapter.getSelectedItemPosition());
                //gone fix list
                fixCareerList.setVisibility(View.GONE);
                fixCareerBtn.setVisibility(View.GONE);
            }
        }
    }

    private void updateCareer(String career){
        Map<String , Object> update = new HashMap<>();
        update.put("career" , career);
        firestore.collection("User").document(auth.getCurrentUser().getUid())
                .update(update)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //gone fix list
                        fixCareerList.setVisibility(View.GONE);
                        fixCareerBtn.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext() , "修改完成", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void editHobby(){
        if(fixHobbyList.getVisibility() == View.GONE){
            fixHobbyList.setVisibility(View.VISIBLE);
            fixHobbyBtn.setVisibility(View.VISIBLE);
            for(int i = 0 ; i < userHobbies.size() ; i ++){
                for(int j = 0 ; j < hobbies.size() ; j ++){
                    if(userHobbies.get(i).getHobby().equals(hobbies.get(j).getHobby())){
                        fixHobbyAdapter.select(j);
                        break;
                    }
                    if(j == hobbies.size() -1){
                        fixHobbyAdapter.selectOther(userHobbies.get(i).getHobby());
                        break;
                    }
                }
            }
        }
    }

    private void doneEditHobby(){
        if(fixHobbyList.getVisibility() == View.VISIBLE){
            List<ItemHobby> selectedHobbyList = new ArrayList<>();
            for(ItemHobby itemHobby : fixHobbyAdapter.getList()){
                if(itemHobby.isSelected()){
                    if(!itemHobby.isOther()){
                        //非其他
                        selectedHobbyList.add(new ItemHobby(itemHobby.getHobby().trim() , false));
                    }else {
                        //其他
                        if(!itemHobby.getOtherHobby().isEmpty()){
                            selectedHobbyList.add(new ItemHobby(itemHobby.getOtherHobby().trim() , false));
                        }
                    }
                }
            }
            if(selectedHobbyList.size() > 5){
                fixHobbyList.requestFocus();
                Toast.makeText(this , "興趣最多只能選五個唷！" , Toast.LENGTH_LONG).show();
                return;
            }
            //比對
            if(selectedHobbyList.size() != 0){
                List<String> data = new ArrayList<>();
                //change career list
                hobbyAdapter.clear();
                for(int i = 0 ; i < hobbies.size() ; i ++){
                    if(hobbies.get(i).isSelected()){
                        fixHobbyAdapter.unSelect(i);//init fix list
                        if(!hobbies.get(i).getHobby().isEmpty()){
                            //hobby
                            hobbyAdapter.add(new ItemHobby(hobbies.get(i).getHobby() , false)); //add to hobby list
                            data.add(hobbies.get(i).getHobby());//add to List<String>
                        }else {
                            //other hobby
                            if(!hobbies.get(i).getOtherHobby().isEmpty()){
                                hobbyAdapter.add(new ItemHobby(hobbies.get(i).getOtherHobby() , false));//add to hobby list
                                data.add(hobbies.get(i).getOtherHobby());//add to List<String>
                            }
                        }
                    }
                }
                //init fix list
                firestore.collection("AppData").document("HobbyData").get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                fixHobbyAdapter.init(documentSnapshot.get("num" , Integer.TYPE));
                            }
                        });
                //db
                updateHobby(data);
            }else {
                fixHobbyList.setVisibility(View.GONE);
                fixHobbyBtn.setVisibility(View.GONE);
            }
        }
    }

    private void updateHobby(List<String> list){
        Map<String , Object> update = new HashMap<>();
        update.put("hobbies" , list);
        firestore.collection("User").document(auth.getCurrentUser().getUid())
                .update(update)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //gone fix list
                        fixHobbyList.setVisibility(View.GONE);
                        fixHobbyBtn.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext() , "修改完成", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setupCareerRecyclerView(RecyclerView recyclerView , CareerAdapter careerAdapter , boolean hasOnClick) {

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this , LinearLayoutManager.HORIZONTAL , false);//水平排版
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(careerAdapter);

        if(hasOnClick){
            final CareerAdapter finalCareerAdapter = careerAdapter;
            careerAdapter.setOnItemClickListener(new CareerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    if(finalCareerAdapter.getSelectedItemPosition() != position){
                        if(finalCareerAdapter.isSelected()){
                            finalCareerAdapter.unSelect(finalCareerAdapter.getSelectedItemPosition());
                        }
                        finalCareerAdapter.select(position);
                    }
                }
            });
        }
    }

    private void setupHobbyRecyclerView(RecyclerView recyclerView  , HobbyAdapter hobbyAdapter , boolean hasOnClick){
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this , LinearLayoutManager.HORIZONTAL , false);//水平排版
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(hobbyAdapter);

        if(hasOnClick){
            final HobbyAdapter finalHobbyAdapter = hobbyAdapter;
            hobbyAdapter.setOnItemClickListener(new HobbyAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(final int position) {
                    if(!finalHobbyAdapter.getList().get(position).isSelected()){
                        //select
                        finalHobbyAdapter.select(position);
                        firestore.collection("AppData").document("HobbyData").get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if(documentSnapshot.exists()){
                                            if(position  > documentSnapshot.get("num" , Integer.TYPE) -1){
                                                //add new other
                                                finalHobbyAdapter.add();
                                                hobbyList.scrollToPosition(finalHobbyAdapter.getItemCount() -1);
                                            }
                                        }
                                    }
                                });
                    }else {
                        //unSelect
                        finalHobbyAdapter.unSelect(position);
                        firestore.collection("AppData").document("HobbyData").get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if(documentSnapshot.exists()){
                                            if(position > documentSnapshot.get("num" , Integer.TYPE) -1){
                                                //delete last other
                                                finalHobbyAdapter.delete(position);
                                            }
                                        }
                                    }
                                });
                    }
                }
            });
        }
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
