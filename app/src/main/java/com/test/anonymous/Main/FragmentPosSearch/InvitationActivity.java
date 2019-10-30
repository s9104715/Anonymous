package com.test.anonymous.Main.FragmentPosSearch;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.test.anonymous.Login.LoginActivity;
import com.test.anonymous.Main.FragmentRandomChat.ChatRoomActivity;
import com.test.anonymous.Main.FragmentRandomChat.RandomChatWaiting;
import com.test.anonymous.R;
import com.test.anonymous.Tools.MyTime;
import com.test.anonymous.Tools.RandomCode;
import com.test.anonymous.Tools.RecyclerViewTools.HobbyList.HobbyAdapter;
import com.test.anonymous.Tools.RecyclerViewTools.HobbyList.ItemHobby;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class InvitationActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView backBtn , reportBtn;
    private CircleImageView selfie;
    private TextView nameTV , distanceData , introTV , greatNum , notBadNum , badNum , infoTV;
    private Button inviteBtn , acceptBtn;
    //hobby list
    private List<ItemHobby> hobbies;
    private RecyclerView hobbyList;
    private HobbyAdapter hobbyAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private ProgressDialog invitePD;
    private ProgressDialog acceptPD;

    //Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    //activity data
    private String UID;
    private String distance;
    private String ACTIVITY_TYPE;//有invite及accept兩種

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_invitation);

        backBtn = findViewById(R.id.back_btn);
        reportBtn = findViewById(R.id.report_btn);
        selfie = findViewById(R.id.selfie);
        nameTV = findViewById(R.id.name_TV);
        distanceData = findViewById(R.id.distance_data);
        introTV = findViewById(R.id.intro_TV);
        greatNum = findViewById(R.id.great_num);
        notBadNum = findViewById(R.id.no_bad_num);
        badNum = findViewById(R.id.bad_num);
        infoTV = findViewById(R.id.info);
        hobbyList = findViewById(R.id.hobby_list);
        inviteBtn = findViewById(R.id.invite_btn);
        acceptBtn = findViewById(R.id.accept_btn);

        backBtn.setOnClickListener(this);
        reportBtn.setOnClickListener(this);
        inviteBtn.setOnClickListener(this);
        acceptBtn.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        UID = getIntent().getExtras().getString("UID");
        distance = getIntent().getExtras().getString("distance");
        ACTIVITY_TYPE = getIntent().getExtras().getString("type");
        setupUI(UID);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back_btn:
                onBackPressed();
                break;
            case R.id.report_btn:
                report();
                break;
            case R.id.invite_btn:
                if(ACTIVITY_TYPE.equals("invite")){
                    invite();
                }
                break;
            case R.id.accept_btn:
                if(ACTIVITY_TYPE.equals("accept")){
                    accept();
                }
                break;
        }
    }

    private void setupUI(String UID) {
        firestore.collection("User").document(UID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        //user data
                        Picasso.get().load(documentSnapshot.getString("selfiePath"))
                                //圖片使用最低分辨率,降低使用空間大小
                                .fit()
                                .centerCrop()
                                .into(selfie);//取得大頭貼
                        nameTV.setText(documentSnapshot.getString("name"));
                        distanceData.setText(distance);
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

                        //activity type
                        if(ACTIVITY_TYPE.equals("accept")){
                            inviteBtn.setVisibility(View.GONE);
                            acceptBtn.setVisibility(View.VISIBLE);
                        }

                        //invite btn 如果已經寄出邀請則disable
                        documentSnapshot.getReference().collection("Pos_Search_Invitation").document(auth.getCurrentUser().getUid())
                                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot.exists()){
                                    inviteBtn.setText("已寄出邀請");
                                    inviteBtn.setBackgroundResource(R.drawable.chat_edit_text_style);
                                    inviteBtn.setEnabled(false);
                                }
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

    private void report(){

    }

    private void invite(){
        showInviteDialog();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //upload invitation to db
                invitePD.dismiss();
                Map<String , Object> update = new HashMap<>();
                update.put("time" , new MyTime().getCurrentTime());
                update.put("distance" , getIntent().getExtras().getString("distance"));
                update.put("read" , false);
                firestore.collection("User").document(getIntent().getExtras().getString("UID")).collection("Pos_Search_Invitation")
                        .document(auth.getCurrentUser().getUid())
                        .set(update)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext() , "已送出邀請！" , Toast.LENGTH_LONG).show();
                                finish();
                            }
                        });
            }
        }, 2000);
    }

    private void showInviteDialog() {
        invitePD = new ProgressDialog(this);
        invitePD.setCancelable(false);
        invitePD.setCanceledOnTouchOutside(false);
        invitePD.setTitle("邀請");
        invitePD.setMessage("邀請中.....");
        invitePD.show();
    }

    private void accept(){
        showAcceptDialog();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //create chat room
                createChatRoom();
            }
        } , 2000);
    }

    private void showAcceptDialog() {
        acceptPD = new ProgressDialog(this);
        acceptPD.setCancelable(false);
        acceptPD.setCanceledOnTouchOutside(false);
        acceptPD.setTitle("接受");
        acceptPD.setMessage("接受中.....");
        acceptPD.show();
    }

    private void createChatRoom(){

        final String chatRoomID = new RandomCode().generateCode(8);//聊天室id
        final Timestamp lastTime = new MyTime().getCurrentTime();//last time

        final Map<String, Object> update = new HashMap<>();
        update.put("chatRoomID", chatRoomID);
        update.put("name" , nameTV.getText().toString());
        update.put("readLine" , 0);//已讀取句數
        update.put("lastTime" , lastTime);
        update.put("lastLine" , "");
        //更新自己的朋友名單Pos_Search_Friends
        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Pos_Search_Friends")
                .document(UID)
                .set(update)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        firestore.collection("User").document(auth.getCurrentUser().getUid()).get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        final Map<String, Object> update = new HashMap<>();
                                        update.put("chatRoomID", chatRoomID);
                                        update.put("name" , documentSnapshot.getString("name"));
                                        update.put("readLine" , 0);//已讀取句數
                                        update.put("lastTime" , lastTime);
                                        update.put("lastLine" , "");
                                        //更新對方的朋友名單Pos_Search_Friends
                                        firestore.collection("User").document(UID).collection("Pos_Search_Friends")
                                                .document(auth.getCurrentUser().getUid())
                                                .set(update)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        //createChatRoom
                                                        //user1為自己 , user2為對方
                                                        Map<String, Object> update = new HashMap<>();
                                                        update.put("user1", auth.getCurrentUser().getUid());
                                                        update.put("user2", UID);
                                                        update.put("lineNum" , 0);
                                                        firestore.collection("PosSearchChatRoom").document(chatRoomID).set(update)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        acceptPD.dismiss();
                                                                        Intent intent = new Intent(getApplicationContext() , ChatRoomActivity.class);
                                                                        intent.putExtra("chatRoomID" ,  chatRoomID )
                                                                                .putExtra("myUID" , auth.getCurrentUser().getUid())
                                                                                .putExtra("otherUID", UID)
                                                                                .putExtra("chat_room_type" , "PosSearchChatRoom")
                                                                                .putExtra("friend_type" , "Pos_Search_Friends");
                                                                        startActivity(intent);
                                                                        finish();
                                                                        //finish box activity
                                                                        try{
                                                                            InvitationBoxActivity.instance.finish();
                                                                        }catch (Exception e){
                                                                            Log.e("finishError", e.toString());
                                                                        }
                                                                        Toast.makeText(getApplicationContext(), "接受成功", Toast.LENGTH_LONG).show();
                                                                    }
                                                                });
                                                        //delete invitation
                                                        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Pos_Search_Invitation")
                                                                .document(UID).delete();
                                                    }
                                                });
                                    }
                                });
                    }
                });

    }
}
