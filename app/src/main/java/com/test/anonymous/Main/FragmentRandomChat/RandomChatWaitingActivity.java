package com.test.anonymous.Main.FragmentRandomChat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.test.anonymous.R;
import com.test.anonymous.Tools.MyTime;
import com.test.anonymous.Tools.RandomCode;
import com.test.anonymous.Tools.Task;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/*使用者依hasMatcher的狀況分為兩角色
        true則為Finder
        false則為Matcher*/
public class RandomChatWaitingActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView backBtn;
    //waitingBar
    private ProgressBar waitingBar;
    private TextView waitingTV;
    private Button cancelBtn;

    //計時器
    private int progress = 30;//允許等待30秒
    private Task countDownTask;
    //match監聽器(有建立match的情況下)
    private  int time = 0;
    private Task matchTask;
    //進入聊天室監聽器(確認聊天室建置完畢才能進入)
    private Task intoChatRoomTask;

    //firestore
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

   @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_random_chat_waiting);

        backBtn = findViewById(R.id.back_btn);
        waitingBar = findViewById(R.id.waiting_bar);
        waitingTV = findViewById(R.id.waiting_TV);
        cancelBtn = findViewById(R.id.cancel_btn);
        waitingBar.startAnimation(AnimationUtils.loadAnimation(this , R.anim.move_upward));
        waitingTV.startAnimation(AnimationUtils.loadAnimation(this , R.anim.move_upward));
        cancelBtn.startAnimation(AnimationUtils.loadAnimation(this , R.anim.move_upward));

        backBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);

       //firebase初始化
       auth = FirebaseAuth.getInstance();
       firestore = FirebaseFirestore.getInstance();

        buildCountDownTask();
        countDownTask.activateTask(0 , 1000);

        if(getIntent().getExtras().getBoolean("hasMatcher")){
            //contact with available matcher
            findMatcher();
        }else {
            //建立matcher同時啟動監聽器
            buildMatchTask();
            createMatcher();
        }
    }

    @Override
    public void onBackPressed() {
        countDownTask.disableTask();
        if(!getIntent().getExtras().getBoolean("hasMatcher")){
            matchTask.disableTask();
            deleteMatcher();
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back_btn:
                onBackPressed();
                break;
            case R.id.cancel_btn:
                onBackPressed();
                break;
        }
    }

    private void buildCountDownTask(){

        countDownTask = new Task(new Timer(), new TimerTask() {
            @Override
            public void run() {
                progress -= 1;
                Log.e("progress" , ""+progress);
                if(progress == 0){
                    countDownTask.disableTask();
                    if(!getIntent().getExtras().getBoolean("hasMatcher")){
                        matchTask.disableTask();
                        deleteMatcher();
                    }
                    finish();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext() ,  "尋找失敗"  , Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void buildMatchTask(){
       matchTask = new Task(new Timer(), new TimerTask() {
           @Override
           public void run() {
               time++;
               Log.e("matching" , ""+time );
                firestore.collection("RandomMatch").document(auth.getCurrentUser().getUid()).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot.getBoolean("contact")!=null){
                                    if(documentSnapshot.getBoolean("contact")){
                                        match(documentSnapshot.getString("contacter"));
                                        matchTask.disableTask();
                                    }
                                }
                            }
                        });
           }
       });
    }

    //matcherUID為聊天室創建者id
    private void buildIntoChatRoomTask(final String matcherUID ){
       intoChatRoomTask = new Task(new Timer(), new TimerTask() {
           @Override
           public void run() {
                firestore.collection("User").document(matcherUID).collection("Random_Friends")
                        .document(auth.getCurrentUser().getUid())
                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot.exists()){
                                    Intent intent = new Intent(RandomChatWaitingActivity.this , ChatRoomActivity.class);
                                    intent.putExtra("chatRoomID" ,  documentSnapshot.getString("chatRoomID"))
                                            .putExtra("myUID" , auth.getCurrentUser().getUid())
                                            .putExtra("otherUID" , matcherUID)
                                            .putExtra("chat_room_type" , "RandomChatRoom")
                                            .putExtra("friend_type" , "Random_Friends");
                                    startActivity(intent);
                                    finish();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "尋找成功", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    countDownTask.disableTask();
                                    intoChatRoomTask.disableTask();
                                }
                            }
                        });
           }
       });
    }

    /*----Finder流程-----
    findMatcher()---->match()---->IntoChatRoomTask---->ChatRoom*/

    //有matcher的情況下找尋matcher
    private void findMatcher() {

        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Random_Friends")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                //add friendUIDList
                final ArrayList<String> friendUIDList = new ArrayList<>();
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                    friendUIDList.add(documentSnapshot.getId());
                }

                firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Pos_Search_Friends").get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                //add friendUIDList
                                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                                    friendUIDList.add(documentSnapshot.getId());
                                }

                                firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Block_List")
                                        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        //add blockList
                                        final ArrayList<String> blockList = new ArrayList<>();
                                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                                            blockList.add(documentSnapshot.getId());
                                        }

                                        firestore.collection("RandomMatch").orderBy("order" , Query.Direction.ASCENDING).get()
                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                                        final boolean[] lock = {false};//媒合成功之後迴圈上鎖

                                                        for (final DocumentSnapshot matchDocumentSnapshot : queryDocumentSnapshots) {
                                                            firestore.collection("User").document(matchDocumentSnapshot.getId()).collection("Block_List")
                                                                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                                                    boolean isFriend = false;
                                                                    boolean isBlocked = false;

                                                                    //判斷對方是否封鎖自己
                                                                    for (DocumentSnapshot matcherDocumentSnapshot : queryDocumentSnapshots){
                                                                        if(matcherDocumentSnapshot.getId().equals(auth.getCurrentUser().getUid())){
                                                                            isBlocked = true;
                                                                            break;
                                                                        }
                                                                    }

                                                                    //判斷是否已成為朋友
                                                                    for(String friendUID : friendUIDList){
                                                                        if(friendUID.equals(matchDocumentSnapshot.getId())){
                                                                            isFriend = true;
                                                                            break;
                                                                        }
                                                                    }

                                                                    //判斷對方是否已被封鎖
                                                                    for(String blockID: blockList){
                                                                        if(blockID.equals(matchDocumentSnapshot.getId())){
                                                                            isBlocked = true;
                                                                            break;
                                                                        }
                                                                    }

                                                                    if(!isFriend && !isBlocked && !lock[0]){
                                                                        if (!matchDocumentSnapshot.getBoolean("contact")) {
                                                                            Map<String, Object> update = new HashMap<>();
                                                                            update.put("contact", true);
                                                                            update.put("contacter" , auth.getCurrentUser().getUid());
                                                                            firestore.collection("RandomMatch").document(matchDocumentSnapshot.getId())
                                                                                    .set(update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    match(matchDocumentSnapshot.getId());
                                                                                }
                                                                            });
                                                                            lock[0] = true;
                                                                        }
                                                                    }

                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                    }
                                });
                            }
                        });
            }
        });
    }

    /*----Matcher流程-----
    createMatcher()---->MatchTask---->match()---->createChatRoom()---->ChatRoom*/

    //沒有matcher的情況下建立matcher同時啟動監聽器
    private void createMatcher(){
        Map<String  , Object> update = new HashMap<>();
        update.put("contact" , false);
        update.put("contacter" , "");
        update.put("order" ,getIntent().getExtras().getInt("order"));
        firestore.collection("RandomMatch").document(auth.getCurrentUser().getUid())
                .set(update).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                matchTask.activateTask(1000 , 2000);
            }
        });
    }

    private void match(final String uid){
       if(!getIntent().getExtras().getBoolean("hasMatcher")){
            //is Matcher
           final String chatRoomID = new RandomCode().generateCode(8);//聊天室id
           final Timestamp lastTime = new MyTime().getCurrentTime();//last time

           firestore.collection("User").document(uid).get()
                   .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                       @Override
                       public void onSuccess(DocumentSnapshot documentSnapshot) {
                           //更新User中的Random_Friends朋友名單(Matcher)
                           Map<String, Object> update = new HashMap<>();
                           update.put("chatRoomID", chatRoomID);
                           update.put("name" , documentSnapshot.getString("name"));
                           update.put("readLine" , 0);//已讀取句數
                           update.put("lastTime" , lastTime);
                           update.put("lastLine" , "");
                           firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Random_Friends")
                                   .document(uid).set(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                               @Override
                               public void onSuccess(Void aVoid) {

                                   firestore.collection("User").document(auth.getCurrentUser().getUid()).get()
                                           .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                               @Override
                                               public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                   //更新User中的Random_Friends朋友名單(Matcher幫Finder更新)
                                                   Map<String, Object> update = new HashMap<>();
                                                   update.put("chatRoomID", chatRoomID);
                                                   update.put("name" , documentSnapshot.getString("name"));
                                                   update.put("readLine" , 0);//已讀取句數
                                                   update.put("lastTime" , lastTime);
                                                   update.put("lastLine" , "");
                                                   firestore.collection("User").document(uid).collection("Random_Friends")
                                                           .document(auth.getCurrentUser().getUid()).set(update)
                                                           .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                               @Override
                                                               public void onSuccess(Void aVoid) {
                                                                   createChatRoom(chatRoomID , auth.getCurrentUser().getUid() , uid);
                                                               }
                                                           });
                                               }
                                           });
                               }
                           });
                       }
                   });
       }else {
           //is Finder
           buildIntoChatRoomTask(uid);
           intoChatRoomTask.activateTask(0 , 1000);
       }
    }

    //建立聊天室(必定由建立matcher的那一方建立)
    //user1為自己 , user2為對方
    private  void createChatRoom(final String chatRoomID , final String user1 , final String user2){
       //由建立matcher的那一方建立
        Map<String, Object> update = new HashMap<>();
        update.put("user1", user1);
        update.put("user2", user2);
        update.put("lineNum" , 0);
        firestore.collection("RandomChatRoom").document(chatRoomID).set(update)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent intent = new Intent(RandomChatWaitingActivity.this , ChatRoomActivity.class);
                        intent.putExtra("chatRoomID" ,  chatRoomID )
                                    .putExtra("myUID" , user1)
                                    .putExtra("otherUID", user2)
                                    .putExtra("chat_room_type" , "RandomChatRoom")
                                    .putExtra("friend_type" , "Random_Friends");
                        startActivity(intent);
                        finish();
                        Toast.makeText(getApplicationContext(), "尋找成功", Toast.LENGTH_LONG).show();
                        countDownTask.disableTask();
                        deleteMatcher();
                    }
                });
    }

    /*有兩種狀況delete matcher
            1.30秒之後尋找失敗
            2.尋找成功*/
    private void deleteMatcher(){
        firestore.collection("RandomMatch").document(auth.getCurrentUser().getUid()).delete();
    }
}
