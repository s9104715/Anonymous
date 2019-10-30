package com.test.anonymous.Login;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.test.anonymous.Main.MainActivity;
import com.test.anonymous.R;
import com.test.anonymous.Tools.LoadingProcessDialog;
import com.test.anonymous.Tools.RecyclerViewTools.CareerList.CareerAdapter;
import com.test.anonymous.Tools.RecyclerViewTools.HobbyList.HobbyAdapter;
import com.test.anonymous.Tools.RecyclerViewTools.CareerList.ItemCareer;
import com.test.anonymous.Tools.RecyclerViewTools.HobbyList.ItemHobby;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.cloudist.acplibrary.ACProgressConstant;
import de.hdodenhof.circleimageview.CircleImageView;

public class InitUserActivity extends AppCompatActivity implements View.OnClickListener {

     private ImageView confirmBtn;
     private CircleImageView selfie;
     private RadioGroup genderRG;
     private EditText ageET , introET;
     //career list
     private List<ItemCareer> careers;
     private RecyclerView careerList;
     private CareerAdapter careerAdapter;
    //hobby list
    private List<ItemHobby> hobbies;
    private RecyclerView hobbyList;
    private HobbyAdapter hobbyAdapter;
    private RecyclerView.LayoutManager layoutManager;

     private AlertDialog confirmAD;

    //Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_init_user);

        confirmBtn = findViewById(R.id.confirm_btn);
        selfie =findViewById(R.id.selfie);
        genderRG = findViewById(R.id.gender_RG);
        ageET = findViewById(R.id.age_ET);
        introET = findViewById(R.id.intro_ET);
        careerList = findViewById(R.id.career_list);
        hobbyList = findViewById(R.id.hobby_list);

        confirmBtn.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        firestore =FirebaseFirestore.getInstance();

        setupUI();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.confirm_btn:
                initUser();
                break;
        }
    }

    private void setupUI(){
        Toast.makeText(this , "請先設定您的資料！", Toast.LENGTH_LONG).show();
        //load selfie
        selfie.bringToFront();
        firestore.collection("User").document(auth.getCurrentUser().getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Picasso.get().load(documentSnapshot.getString("selfiePath"))
                        //圖片使用最低分辨率,降低使用空間大小
                        .fit()
                        .centerCrop()
                        .into(selfie);//取得大頭貼
            }
        });
        //load career
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
                        careers.add(new ItemCareer("", false));//其他
                        setupCareerRecyclerView();
                    }
                });
        //load hobby
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
                        hobbies.add(new ItemHobby("", false));//其他
                        setupHobbyRecyclerView();
                    }
                });
    }

    private void setupCareerRecyclerView() {

        careerList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this , LinearLayoutManager.HORIZONTAL , false);//水平排版
        careerAdapter = new CareerAdapter(careers);
        careerList.setLayoutManager(layoutManager);
        careerList.setAdapter(careerAdapter);

        careerAdapter.setOnItemClickListener(new CareerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if(careerAdapter.isSelected()){
                    careerAdapter.unSelect(careerAdapter.getSelectedItemPosition());
                }
                careerAdapter.select(position);
            }
        });
    }

    private void setupHobbyRecyclerView(){
        hobbyList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this , LinearLayoutManager.HORIZONTAL , false);//水平排版
        hobbyAdapter = new HobbyAdapter(hobbies);
        hobbyList.setLayoutManager(layoutManager);
        hobbyList.setAdapter(hobbyAdapter);

        hobbyAdapter.setOnItemClickListener(new HobbyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final int position) {
                if(!hobbyAdapter.getList().get(position).isSelected()){
                    //select
                    hobbyAdapter.select(position);
                    firestore.collection("AppData").document("HobbyData").get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(documentSnapshot.exists()){
                                        if(position  > documentSnapshot.get("num" , Integer.TYPE) -1){
                                            //add new other
                                            hobbyAdapter.add();
                                            hobbyList.scrollToPosition(hobbyAdapter.getItemCount() -1);
                                        }
                                    }
                                }
                            });
                }else {
                    //unSelect
                    hobbyAdapter.unSelect(position);
                    firestore.collection("AppData").document("HobbyData").get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(documentSnapshot.exists()){
                                        if(position > documentSnapshot.get("num" , Integer.TYPE) -1){
                                            //delete last other
                                            hobbyAdapter.delete(position);
                                        }
                                    }
                                }
                            });
                }
            }
        });
    }

    private void initUser(){
        RadioButton radioButton =  findViewById(genderRG.getCheckedRadioButtonId());
        String gender = (String)radioButton.getText();
        String age = ageET.getText().toString().trim();
        String career;
        if(careerAdapter.getSelectedItemPosition() == careerAdapter.getList().size() -1){
            //選擇其他
            career = careerAdapter.getList().get(careerAdapter.getSelectedItemPosition()).getOtherCareer();
        }else if(careerAdapter.getSelectedItemPosition() == -1){
            //無選擇
            career = "";
        }else {
            career = careerAdapter.getList().get(careerAdapter.getSelectedItemPosition()).getCareer();
        }
        List<String> selectedHobbyList = new ArrayList<>();
        for(ItemHobby itemHobby : hobbyAdapter.getList()){
            if(itemHobby.isSelected()){
                if(!itemHobby.isOther()){
                    //非其他
                    selectedHobbyList.add(itemHobby.getHobby().trim());
                }else {
                    //其他
                    if(!itemHobby.getOtherHobby().isEmpty()){
                        selectedHobbyList.add(itemHobby.getOtherHobby().trim());
                    }
                }
            }
        }
        String intro = introET.getText().toString().trim();
        if(age.isEmpty()){
            ageET.setError("年齡不能為空");
            ageET.requestFocus();
            selectedHobbyList.clear();
            return;
        }
        if(Integer.parseInt(age) < 12 || Integer.parseInt(age) > 99){
            ageET.setError("年齡須介於12~99");
            ageET.requestFocus();
            selectedHobbyList.clear();
            return;
        }
        if(career.isEmpty()){
            Toast.makeText(this , "必須選擇一項職業" , Toast.LENGTH_LONG).show();
            careerList.requestFocus();
            selectedHobbyList.clear();
            return;
        }
        if(selectedHobbyList.size() < 2){
            Toast.makeText(this , "必須選擇只少兩個興趣" , Toast.LENGTH_LONG).show();
            hobbyList.requestFocus();
            selectedHobbyList.clear();
            return;
        }
        if(selectedHobbyList.size() > 5){
            Toast.makeText(this , "興趣最多只能選五個唷！" , Toast.LENGTH_LONG).show();
            hobbyList.requestFocus();
            selectedHobbyList.clear();
            return;
        }
        uploadUserData(gender , age , career , selectedHobbyList ,intro);
    }

    private void uploadUserData(final String gender , final String age , final  String career , final List<String> hobbyList , final String intro){
        AlertDialog.Builder ADBuilder = new AlertDialog.Builder(this)
                .setTitle("提醒")
                .setMessage(getString(R.string.gender_setting_msg))
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        confirmAD.dismiss();
                        //upload data
                        //show loadingPD
                        final LoadingProcessDialog loadingPD = new LoadingProcessDialog(ACProgressConstant.DIRECT_CLOCKWISE ,
                                Color.WHITE , false , false , InitUserActivity.this)
                                .show();
                        //初始化使用者評論資料
                        initUserCommentData();
                        //更新使用者資料
                        Map<String  , Object> update = new HashMap<>();
                        update.put("gender" , gender);
                        update.put("age" , Integer.parseInt(age));
                        update.put("career" , career);
                        update.put("hobbies" , hobbyList);
                        update.put("intro" , intro);
                        firestore.collection("User").document(auth.getCurrentUser().getUid())
                                .update(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                loadingPD.dismiss();
                                //start main
                                startActivity(new Intent(InitUserActivity.this , MainActivity.class));
                                Toast.makeText(InitUserActivity.this , "設定完成" , Toast.LENGTH_LONG).show();
                                finish();
                            }
                        });
                    }
                })
                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        confirmAD.dismiss();
                    }
                });
        confirmAD = ADBuilder.create();
        confirmAD.show();
    }
    //init user comment
    private void initUserCommentData(){
        Map<String  , Object> update = new HashMap<>();
        update.put("greatNum" , 0);
        update.put("notBadNum" , 0);
        update.put("badNum" , 0);
        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Comment")
                .document("comment").set(update);
    }
}
