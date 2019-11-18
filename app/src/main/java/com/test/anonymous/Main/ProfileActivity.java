package com.test.anonymous.Main;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.test.anonymous.Main.FragmentSetting.EditProfileActivity;
import com.test.anonymous.R;
import com.test.anonymous.Tools.RecyclerViewTools.HobbyList.HobbyAdapter;
import com.test.anonymous.Tools.RecyclerViewTools.HobbyList.ItemHobby;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView backBtn , editBtn;
    private CircleImageView selfie;
    private TextView nameTV , introTV , greatNum , notBadNum , badNum , infoTV;
    private ConstraintLayout greatBtn , notBadBtn , badBtn;
    //hobby list
    private List<ItemHobby> hobbies;
    private RecyclerView hobbyList;
    private HobbyAdapter hobbyAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private String UID;
    private boolean editable;

    private String COMMENT_TEMP = "";//有三中"GREAT" , "NOT_BAD" , "BAD" 用來儲存原先的評論
    private String COMMENT_RESULT = "";//評論結果最終用來上傳DB
    private int textDefaultColor;
    private String selfieUri;

    //firestore
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_profile);

        backBtn = findViewById(R.id.back_btn);
        editBtn = findViewById(R.id.edit_btn);
        selfie = findViewById(R.id.selfie);
        nameTV = findViewById(R.id.name_TV);
        introTV = findViewById(R.id.intro_TV);
        greatNum = findViewById(R.id.great_num);
        notBadNum = findViewById(R.id.no_bad_num);
        badNum = findViewById(R.id.bad_num);
        infoTV = findViewById(R.id.info);
        hobbyList = findViewById(R.id.hobby_list);
        greatBtn = findViewById(R.id.great);
        notBadBtn = findViewById(R.id.not_bad);
        badBtn = findViewById(R.id.bad);

        backBtn.setOnClickListener(this);
        editBtn.setOnClickListener(this);
        selfie.setOnClickListener(this);
        greatBtn.setOnClickListener(this);
        notBadBtn.setOnClickListener(this);
        badBtn.setOnClickListener(this);

        UID = getIntent().getExtras().getString("UID");
        editable = getIntent().getExtras().getBoolean("editable");

        //firebase初始化
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        setupUI(UID);
        loadCommentRecord();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        updateComment(UID , COMMENT_RESULT, COMMENT_TEMP);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back_btn:
                onBackPressed();
                break;
            case R.id.edit_btn:
                startActivity(new Intent(this , EditProfileActivity.class));
                break;
            case R.id.selfie:
                Intent intent = new Intent(this , BrowseImgActivity.class);
                intent.putExtra("picUri" , selfieUri);
                startActivity(intent);
                break;
            case R.id.great:
                if(!editable){
                    comment("GREAT");
                }
                break;
            case R.id.not_bad:
                if(!editable){
                    comment("NOT_BAD");
                }
                break;
            case R.id.bad:
                if(!editable){
                    comment("BAD");
                }
                break;
        }
    }

    private void setupUI(final String UID) {
        firestore.collection("User").document(UID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if(editable){
                    editBtn.setVisibility(View.VISIBLE);
                }
                //user data
                selfieUri = documentSnapshot.getString("selfiePath");
                Picasso.get().load(selfieUri)
                        //圖片使用最低分辨率,降低使用空間大小
                        .fit()
                        .centerCrop()
                        .into(selfie);//取得大頭貼
                nameTV.setText(documentSnapshot.getString("name"));
                introTV.setText(documentSnapshot.getString("intro"));
                String info = documentSnapshot.get("age" , Integer.TYPE)+" , "+documentSnapshot.getString("gender")+" , "+documentSnapshot.getString("career");
                infoTV.setText(info);
                hobbies = new ArrayList<>();
                for(String hobby : (List<String>) documentSnapshot.get("hobbies")){
                    hobbies.add(new ItemHobby(hobby , false));
                }
                setupRecyclerView();

                //load comment
                documentSnapshot.getReference().collection("Comment").document("comment")
                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        greatNum.setText(String.valueOf(documentSnapshot.get("greatNum" , Integer.TYPE)));
                        notBadNum.setText((String.valueOf(documentSnapshot.get("notBadNum" , Integer.TYPE))));
                        badNum.setText((String.valueOf(documentSnapshot.get("badNum" , Integer.TYPE))));
                    }
                });
            }
        });
    }

    private void setupRecyclerView(){
        hobbyList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this , LinearLayoutManager.HORIZONTAL , false);//水平排版
        hobbyAdapter = new HobbyAdapter(hobbies);
        hobbyList.setLayoutManager(layoutManager);
        hobbyList.setAdapter(hobbyAdapter);
    }

    private void loadCommentRecord(){
        //load comment record
        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Comment_Record")
                .document(UID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            String comment = documentSnapshot.getString("comment");
                            if(comment.equals("GREAT")){
                                highlightTextView(greatNum);
                            }else if(comment.equals("NOT_BAD")){
                                highlightTextView(notBadNum);
                            }else if(comment.equals("BAD")){
                                highlightTextView(badNum);
                            }
                            COMMENT_TEMP = comment;
                            COMMENT_RESULT = comment;
                        }
                    }
                });
    }

    private void comment(String type){
        switch (type){
            case "GREAT":
                if(!COMMENT_RESULT.equals("GREAT")){
                    cancelComment(COMMENT_RESULT);
                    //UI
                    int data = Integer.parseInt(greatNum.getText().toString());
                    data ++;
                    greatNum.setText(String.valueOf(data));
                    highlightTextView(greatNum);

                    COMMENT_RESULT = "GREAT";
                }
                break;
            case "NOT_BAD":
                if(!COMMENT_RESULT.equals("NOT_BAD")){
                    cancelComment(COMMENT_RESULT);
                    //UI
                    int data = Integer.parseInt(notBadNum.getText().toString());
                    data ++;
                    notBadNum.setText(String.valueOf(data));
                    highlightTextView(notBadNum);

                    COMMENT_RESULT = "NOT_BAD";
                }
                break;
            case "BAD":
                if(!COMMENT_RESULT.equals("BAD")){
                    cancelComment(COMMENT_RESULT);
                    //UI
                    int data = Integer.parseInt(badNum.getText().toString());
                    data ++;
                    badNum.setText(String.valueOf(data));
                    highlightTextView(badNum);

                    COMMENT_RESULT = "BAD";
                }
                break;
        }
    }
    //取消原先評論
    private void cancelComment(String data){
        switch (data){
            case  "GREAT":
                //UI
                int greatData = Integer.parseInt(greatNum.getText().toString());
                greatData --;
                greatNum.setText(String.valueOf(greatData));
                initTextView(greatNum);
                break;
            case  "NOT_BAD":
                //UI
                int notBadData = Integer.parseInt(notBadNum.getText().toString());
                notBadData --;
                notBadNum.setText(String.valueOf(notBadData));
                initTextView(notBadNum);
                break;
            case  "BAD":
                //UI
                int badData = Integer.parseInt(badNum.getText().toString());
                badData --;
                badNum.setText(String.valueOf(badData));
                initTextView(badNum);
                break;
        }
    }
    //上傳評論紀錄(新增)
    private void updateComment(String UID , final String result , final String temp){
        //update comment record
        Map<String , Object> update = new HashMap<>();
        update.put("comment" , result);
        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Comment_Record")
                .document(UID)
                .set(update);
        //update friend comment
        //至資料庫取得數字才精確
        if(result.equals(temp)){
            //do nothing
        }else {
            firestore.collection("User").document(UID).collection("Comment").document("comment")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String resultField = getField(result);
                            String tempField = getField(temp);

                            Map<String , Object> resultUpdate = new HashMap<>();
                            resultUpdate.put(resultField , documentSnapshot.get(resultField , Integer.TYPE) + 1);
                            documentSnapshot.getReference().update(resultUpdate);

                            Map<String , Object> tempUpdate = new HashMap<>();
                            tempUpdate.put(tempField , documentSnapshot.get(tempField , Integer.TYPE) - 1);
                            documentSnapshot.getReference().update(tempUpdate);
                        }
                    });
        }
    }

    private String getField(String data){
        if(data.equals("GREAT")){
            return "greatNum";
        }else if(data.equals("NOT_BAD")){
            return "notBadNum";
        }else if(data.equals("BAD")){
            return  "badNum";
        }
        return null;
    }
//    //上傳評論紀錄(刪除)
//    private void updateCommentForCancel(String UID , final String comment){
//        //update friend comment
//        //至資料庫取得數字才精確
//        firestore.collection("User").document(UID).collection("Comment").document("comment")
//                .get()
//                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                        String field = null;
//                        if(comment.equals("GREAT")){
//                            field = "greatNum";
//                        }else if(comment.equals("NOT_BAD")){
//                            field = "notBadNum";
//                        }else if(comment.equals("BAD")){
//                            field = "badNum";
//                        }
//                        Map<String , Object> update = new HashMap<>();
//                        update.put(field , documentSnapshot.get(field , Integer.TYPE) - 1);
//                        documentSnapshot.getReference().update(update);
//                    }
//                });
//    }

    //選擇之後的TextView UI
    private void highlightTextView(TextView textView){
        //set text style
        textDefaultColor = textView.getCurrentTextColor();
        textView.setTextColor(getResources().getColor(R.color.group_setting_color));
        textView.setTypeface(null, Typeface.BOLD);
    }

    //初始化TextView UI
    private void initTextView(TextView textView){
        //set text style
        textView.setTextColor(textDefaultColor);
        textView.setTypeface(null, Typeface.NORMAL);
    }
}
