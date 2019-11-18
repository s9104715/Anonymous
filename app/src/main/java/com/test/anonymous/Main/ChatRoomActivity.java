package com.test.anonymous.Main;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.test.anonymous.Tools.Code;
import com.test.anonymous.R;
import com.test.anonymous.Tools.Keyboard;
import com.test.anonymous.Tools.MyTime;
import com.test.anonymous.Tools.RandomCode;
import com.test.anonymous.Tools.RecyclerViewTools.ChatList.ChatAdapter;
import com.test.anonymous.Tools.RecyclerViewTools.ChatList.ItemChat;
import com.test.anonymous.Tools.RecyclerViewTools.TopicList.ItemTopic;
import com.test.anonymous.Tools.RecyclerViewTools.TopicList.SendableTopicAdapter;
import com.test.anonymous.Tools.Task;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import at.markushi.ui.CircleButton;

public class ChatRoomActivity extends AppCompatActivity implements View.OnClickListener {

    private View view;
    //RecyclerView
    private List<ItemChat> chatList;
    private RecyclerView list;
    private ChatAdapter chatAdapter;
    //TopicRecyclerView
    private List<ItemTopic> topics;
    private RecyclerView topicList;
    private SendableTopicAdapter sendableTopicAdapter;
    private ConstraintLayout topicLib;
    private CircleButton hideBtn;
    private RecyclerView.LayoutManager layoutManager;

    private CircleButton chatBtn , optionBtn;
    private EditText inputET;

    //send pic view
    private CardView sendPicView;
    private RelativeLayout cameraBtn  , galleryBtn , topicBtn;
    //cover view
    private View mainCoverView;
    private View botCoverView;

    private String CHAT_ROOM_TYPE;//分為RandomChatRoom , PosSearchChatRoom 兩種
    private String FRIEND_TYPE;//分為Random_Friends , Pos_Search_Friends 兩種
    private String chatRoomID;
    private String mySelfiePath;
    private String otherSelfiePath;

    //監聽對方訊息
    private Task msgSonarTask;

    //camera and Gallery
    private File imgFile;
    private Uri imgUri;

    //firestore
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private StorageReference storageRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_chat_room);

        view = this.getCurrentFocus();
        list = findViewById(R.id.list);
        topicList = findViewById(R.id.topic_list);
        topicLib = findViewById(R.id.topic_lib);
        hideBtn = findViewById(R.id.hide_btn);
        chatBtn = findViewById(R.id.chat_btn);
        optionBtn = findViewById(R.id.option_btn);
        inputET= findViewById(R.id.input_ET);
        sendPicView = findViewById(R.id.send_pic_view);
        cameraBtn = findViewById(R.id.camera_bnt);
        galleryBtn = findViewById(R.id.gallery_bnt);
        topicBtn = findViewById(R.id.topic_btn);
        mainCoverView = findViewById(R.id.main_cover_view);
        botCoverView = findViewById(R.id.bot_cover_view);

        hideBtn.setOnClickListener(this);
        chatBtn.setOnClickListener(this);
        optionBtn.setOnClickListener(this);
        cameraBtn.setOnClickListener(this);
        galleryBtn.setOnClickListener(this);
        topicBtn.setOnClickListener(this);

        //firebase初始化
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        CHAT_ROOM_TYPE = getIntent().getExtras().getString("chat_room_type");
        FRIEND_TYPE = getIntent().getExtras().getString("friend_type");
        loadMsg();
        loadTopic();
        buildMsgSonarTask();
        msgSonarTask.activateTask(1000 , 1000);
        setupTopToolBar();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.hide_btn:
                hideTopicLib();
                break;
            case R.id.chat_btn:
                sendMsg();
                break;
            case R.id.option_btn:
                callOption();
                break;
            case R.id.camera_bnt:
                cameraOnClick();
                break;
            case R.id.gallery_bnt:
                galleryOnClick();
                break;
            case R.id.topic_btn:
                showTopicLib();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if(sendPicView.getVisibility() == View.VISIBLE){
            mainCoverView.setVisibility(View.GONE);
            botCoverView.setVisibility(View.GONE);
            Animation fadeOut = AnimationUtils.loadAnimation(this , R.anim.fade_out);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    sendPicView.setVisibility(View.INVISIBLE);
                }
                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            sendPicView.startAnimation(fadeOut);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public void finish() {
        msgSonarTask.disableTask();
        super.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_room_option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.block:
                block();
                break;
            case R.id.leave:
                leave();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setupTopToolBar(){

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.chat_room_toolbar);
        setSupportActionBar(toolbar);
        //toolBar返回鍵
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void loadMsg(){

        //載入聊天室ID
        chatRoomID = getIntent().getExtras().getString("chatRoomID");
        //載入自己大頭貼路徑
        firestore.collection("User").document(getIntent().getExtras().getString("myUID")).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        mySelfiePath = documentSnapshot.getString("selfiePath");
                        //載入對方大頭貼路徑
                        firestore.collection("User").document(getIntent().getExtras().getString("otherUID")).get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        //set title
                                        if(getIntent().getExtras().getString("name")!=null){
                                            setTitle(getIntent().getExtras().getString("name"));
                                        }else {
                                            setTitle(documentSnapshot.getString("name"));
                                        }
                                        otherSelfiePath = documentSnapshot.getString("selfiePath");
                                        //載入對話
                                        chatList = new ArrayList<>();
                                        firestore.collection(CHAT_ROOM_TYPE).document(chatRoomID).collection("conversation")
                                                .orderBy("index" , Query.Direction.ASCENDING)
                                                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                                                    if(!documentSnapshot.getString("text").equals("")){
                                                        //文字訊息
                                                        chatList.add(new ItemChat(documentSnapshot.get("index" , Integer.class) ,
                                                                documentSnapshot.getString("userUID") ,
                                                                mySelfiePath ,
                                                                otherSelfiePath ,
                                                                documentSnapshot.getString("text") ,
                                                                documentSnapshot.get("time" , Timestamp.class)));
                                                    }else {
                                                        //圖片訊息
                                                        chatList.add(new ItemChat(documentSnapshot.get("index" , Integer.class) ,
                                                                documentSnapshot.getString("userUID") ,
                                                                mySelfiePath ,
                                                                otherSelfiePath ,
                                                                documentSnapshot.getString("imgUrl"),
                                                                documentSnapshot.get("time" , Timestamp.class) ,
                                                                true));
                                                    }
                                                }
                                                //userHasLeft
                                                firestore.collection(CHAT_ROOM_TYPE).document(chatRoomID).get()
                                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                if(documentSnapshot.getBoolean("userHasLeft")!=null){
                                                                    if(documentSnapshot.getBoolean("userHasLeft")){
                                                                        inputET.setText("對方已離開聊天室");
                                                                        inputET.setEnabled(false);
                                                                    }
                                                                }
                                                            }
                                                        });
                                                setupRecyclerView();
                                                updateReadLine();//更新已讀數
                                            }
                                        });
                                    }
                                });
                    }
                });
    }

    private void setupRecyclerView(){
        list.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        chatAdapter = new ChatAdapter(chatList);
        list.setLayoutManager(layoutManager);
        list.setAdapter(chatAdapter);
        list.getRecycledViewPool().setMaxRecycledViews(0 ,0);//防止丟失數據
        list.scrollToPosition(chatList.size()-1);//自動滾動到底部
        //RecyclerView下拉至底部(載入圖片需耗時，等待載入完成所用)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                list.scrollToPosition(chatList.size()-1);//自動滾動到底部
            }
        } , 2500);
        //照片點擊方法
        chatAdapter.setImgClickListener(new ChatAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(getApplicationContext() , BrowseImgActivity.class);
                intent.putExtra("picUri" , chatList.get(position).getImgUrl());
                startActivity(intent);
            }
        });
        //大頭貼點擊方法
        chatAdapter.setSelfieClickListener(new ChatAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //如果進入的是自己的檔案則editable = true
                String UID = chatList.get(position).getUserUID();
                boolean editable = false;
                if(UID.equals(auth.getCurrentUser().getUid())){
                    editable = true;
                }
                Intent intent = new Intent(getApplicationContext() , ProfileActivity.class);
                intent.putExtra("UID" , chatList.get(position).getUserUID());
                intent.putExtra("editable" , editable);
                startActivity(intent);
            }
        });
    }

    //更新已讀數(更新自己的就好)
    private void updateReadLine(){
        firestore.collection(CHAT_ROOM_TYPE).document(chatRoomID).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Map<String , Object> update = new HashMap<>();
                        update.put("readLine", documentSnapshot.get("lineNum" , Integer.class));
                        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection(FRIEND_TYPE)
                                .document(getIntent().getExtras().getString("otherUID")).update(update);
                    }
                });
    }

    //更新last time(雙方皆要更新)
    private void updateLastTime(Timestamp timestamp){
        String otherUID = getIntent().getExtras().getString("otherUID");
        Map<String , Object> update = new HashMap<>();
        update.put("lastTime", timestamp);
        //update my Random_Friends lastTime
        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection(FRIEND_TYPE)
                .document(otherUID).update(update);
        //update other Random_Friends lastTime
        firestore.collection("User").document(otherUID).collection(FRIEND_TYPE)
                .document(auth.getCurrentUser().getUid()).update(update);
    }

    //更新lastLine(雙方皆要更新)
    private void updateLastLine(String lastLine){
        String otherUID = getIntent().getExtras().getString("otherUID");
        Map<String , Object> update = new HashMap<>();
        update.put("lastLine", lastLine);
        //update my Random_Friends lastLine
        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection(FRIEND_TYPE)
                .document(otherUID).update(update);
        //update other Random_Friends lastLine
        firestore.collection("User").document(otherUID).collection(FRIEND_TYPE)
                .document(auth.getCurrentUser().getUid()).update(update);
    }

    private void loadTopic(){
        topics = new ArrayList<>();
        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Topic_Lib")
                .orderBy("time" , Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                            topics.add(new ItemTopic(documentSnapshot.getId() , documentSnapshot.getString("topic")));
                        }
                        setupTopicRecyclerView();
                    }
                });
    }

    private void setupTopicRecyclerView() {
        topicList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this , LinearLayoutManager.HORIZONTAL , false);//水平排版
        sendableTopicAdapter = new SendableTopicAdapter(topics);
        topicList.setLayoutManager(layoutManager);
        topicList.setAdapter(sendableTopicAdapter);

        sendableTopicAdapter.setOnItemClickListener(new SendableTopicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                inputET.setText(topics.get(position).getTopic());
                hideTopicLib();
            }
        });
    }

    //資料餵給資料庫都是由此function負責
    private void sendMsg(){

        if(inputET.getText().toString().trim().isEmpty()){//沒打字
            inputET.setError("請輸入想傳送的訊息！");
            inputET.requestFocus();
        }else {
            firestore.collection(CHAT_ROOM_TYPE).document(chatRoomID).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            //lineNum
                            final int lineNum = documentSnapshot.get("lineNum" , Integer.class);
                            final String lastLine =  inputET.getText().toString();
                            final Timestamp lastTime = new MyTime().getCurrentTime();
                            //output
                            final ItemChat itemChat = new ItemChat(lineNum ,
                                    auth.getCurrentUser().getUid() ,
                                    mySelfiePath ,
                                    otherSelfiePath ,
                                    inputET.getText().toString(),
                                    new MyTime().getCurrentTime());
                            chatAdapter.addMsg(itemChat);
                            inputET.setText("");
                            list.scrollToPosition(chatList.size()-1);//自動滾動到底部
                            new Keyboard(getSystemService(INPUT_METHOD_SERVICE) , inputET.getRootView()).close();
                            //上傳資料庫
                            Map<String , Object> update = new HashMap<>();
                            update.put("index", lineNum);
                            update.put("userUID" , auth.getCurrentUser().getUid());
                            update.put("text" , lastLine);
                            update.put("time" , lastTime);
                            firestore.collection(CHAT_ROOM_TYPE).document(chatRoomID).collection("conversation")
                                    .document(String.valueOf(lineNum)).set(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //修改lineNum
                                    Map<String , Object> update = new HashMap<>();
                                    update.put("lineNum", (lineNum + 1));
                                    firestore.collection(CHAT_ROOM_TYPE).document(chatRoomID)
                                            .update(update);
                                    //更新已讀數
                                    updateReadLine();
                                    //更新last time
                                    updateLastTime(lastTime);
                                    //更新lastLine
                                    updateLastLine(lastLine);
                                }
                            });
                        }
                    });
        }
    }

    //並不負責資料庫
    private void buildMsgSonarTask(){
        msgSonarTask = new Task(new Timer(), new TimerTask() {
            @Override
            public void run() {
                firestore.collection(CHAT_ROOM_TYPE).document(chatRoomID).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                final int lineNum = documentSnapshot.get("lineNum" , Integer.class);
                                Log.e("listen lineNum", ""+lineNum);
                                firestore.collection(CHAT_ROOM_TYPE).document(chatRoomID).collection("conversation").document(String.valueOf(lineNum))
                                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if(documentSnapshot.exists()){
                                            if(!documentSnapshot.getString("userUID").equals(auth.getCurrentUser().getUid())){
                                                if(!documentSnapshot.getString("text").equals("")){
                                                    //文字訊息
                                                    chatAdapter.addMsg(new ItemChat(documentSnapshot.get("index" , Integer.class) ,
                                                            documentSnapshot.getString("userUID") ,
                                                            mySelfiePath ,
                                                            otherSelfiePath ,
                                                            documentSnapshot.getString("text") ,
                                                            documentSnapshot.get("time" , Timestamp.class)));
                                                }else {
                                                    //圖片訊息
                                                    chatAdapter.addMsg(new ItemChat(documentSnapshot.get("index" , Integer.class) ,
                                                            documentSnapshot.getString("userUID") ,
                                                            mySelfiePath ,
                                                            otherSelfiePath ,
                                                            documentSnapshot.getString("imgUrl"),
                                                            documentSnapshot.get("time" , Timestamp.class) ,
                                                            true));
                                                }
                                                list.scrollToPosition(chatList.size()-1);//自動滾動到底部
                                                updateReadLine();
                                            }
                                        }
                                    }
                                });
                            }
                        });
            }
        });
    }

    private void block(){
        AlertDialog blockAD = new AlertDialog.Builder(this)
                .setTitle("封鎖好友")
                .setMessage(R.string.block_msg)
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        msgSonarTask.disableTask();
                        firestore.collection("User").document(getIntent().getExtras().getString("otherUID")).get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(final DocumentSnapshot friendsDocumentSnapshot) {
                                        //DB add block_list
                                        Map<String , Object> update = new HashMap<>();
                                        update.put("name" , friendsDocumentSnapshot.getString("name"));//get real name
                                        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Block_List")
                                                .document(friendsDocumentSnapshot.getId()).set(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //DB delete Random_Friends
                                                firestore.collection("User").document(auth.getCurrentUser().getUid()).collection(FRIEND_TYPE)
                                                        .document(friendsDocumentSnapshot.getId()).delete();
                                                //DB chatRoom
                                                firestore.collection(CHAT_ROOM_TYPE).document(chatRoomID).get()
                                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                if(documentSnapshot.getBoolean("userHasLeft")!=null){
                                                                    firestore.collection(CHAT_ROOM_TYPE).document(chatRoomID).collection("conversation")
                                                                            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                        @Override
                                                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                            for(DocumentSnapshot conversationDocumentSnapshot :queryDocumentSnapshots){
                                                                                //delete conversation
                                                                                conversationDocumentSnapshot.getReference().delete();
                                                                            }
                                                                            //delete chatRoom
                                                                            firestore.collection(CHAT_ROOM_TYPE).document(chatRoomID).delete()
                                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void aVoid) {
                                                                                            ChatRoomActivity.super.finish();
                                                                                        }
                                                                                    });
                                                                        }
                                                                    });
                                                                }else {
                                                                    //update chatRoom
                                                                    Map<String , Object> update = new HashMap<>();
                                                                    update.put("userHasLeft" , true);
                                                                    firestore.collection(CHAT_ROOM_TYPE).document(chatRoomID)
                                                                            .update(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            ChatRoomActivity.super.finish();
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
                })
                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
        blockAD.show();
    }

    private void leave(){
        AlertDialog leaveAD = new AlertDialog.Builder(this)
                .setTitle("刪除好友")
                .setMessage(R.string.leave_msg)
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        msgSonarTask.disableTask();
                        //DB delete Random_Friends
                        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection(FRIEND_TYPE)
                                .document(getIntent().getExtras().getString("otherUID")).delete();
                        //DB chatRoom
                        firestore.collection(CHAT_ROOM_TYPE).document(chatRoomID).get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if(documentSnapshot.getBoolean("userHasLeft")!=null){
                                            //delete chatRoom
                                            firestore.collection(CHAT_ROOM_TYPE).document(chatRoomID).collection("conversation")
                                                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                    for(DocumentSnapshot conversationDocumentSnapshot :queryDocumentSnapshots){
                                                        //delete conversation
                                                        conversationDocumentSnapshot.getReference().delete();
                                                    }
                                                    //delete chatRoom
                                                    firestore.collection(CHAT_ROOM_TYPE).document(chatRoomID).delete()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    ChatRoomActivity.super.finish();
                                                                }
                                                            });
                                                }
                                            });
                                        }else {
                                            //update chatRoom
                                            Map<String , Object> update = new HashMap<>();
                                            update.put("userHasLeft" , true);
                                            firestore.collection(CHAT_ROOM_TYPE).document(chatRoomID)
                                                    .update(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    ChatRoomActivity.super.finish();
                                                }
                                            });
                                        }
                                    }
                                });
                    }
                })
                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
        leaveAD.show();
    }

    private void callOption(){
        if(sendPicView.getVisibility() == View.INVISIBLE ){
            sendPicView.setVisibility(View.VISIBLE);
            Animation fadeIn = AnimationUtils.loadAnimation(this , R.anim.fade_in);
            fadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mainCoverView.setVisibility(View.VISIBLE);
                    botCoverView.setVisibility(View.VISIBLE);
                    View.OnClickListener clickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mainCoverView.setVisibility(View.GONE);
                            botCoverView.setVisibility(View.GONE);
                            Animation fadeOut = AnimationUtils.loadAnimation(getApplicationContext() , R.anim.fade_out);
                            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }
                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    sendPicView.setVisibility(View.INVISIBLE);
                                }
                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                            sendPicView.startAnimation(fadeOut);
                        }
                    };
                    mainCoverView.setOnClickListener(clickListener);
                    botCoverView.setOnClickListener(clickListener);
                }
                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            sendPicView.setAnimation(fadeIn);
        }else {
            mainCoverView.setVisibility(View.GONE);
            botCoverView.setVisibility(View.GONE);
            Animation fadeOut = AnimationUtils.loadAnimation(this , R.anim.fade_out);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    sendPicView.setVisibility(View.INVISIBLE);
                }
                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            sendPicView.startAnimation(fadeOut);
        }
    }

    private void cameraOnClick(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imgFile = new File(getExternalCacheDir(),
                (System.currentTimeMillis()) + ".jpg");
        imgUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", imgFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        startActivityForResult(intent, Code.CAMERA_REQUEST);
    }

    private void galleryOnClick(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Code.GALLERY_REQUEST);
    }

    private void showTopicLib(){
        if(topics.size() == 0){
            Toast.makeText(this , "您尚未儲存任何話題", Toast.LENGTH_LONG).show();
        }else {
            if(topicLib.getVisibility() == View.INVISIBLE){
                //hide send pic view
                mainCoverView.setVisibility(View.GONE);
                botCoverView.setVisibility(View.GONE);
                Animation fadeOut = AnimationUtils.loadAnimation(getApplicationContext() , R.anim.fade_out);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        sendPicView.setVisibility(View.INVISIBLE);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                sendPicView.startAnimation(fadeOut);
                //show topic list
                topicLib.setVisibility(View.VISIBLE);
                hideBtn.setVisibility(View.VISIBLE);
                topicLib.startAnimation(AnimationUtils.loadAnimation(this , R.anim.fade_in));
                hideBtn.startAnimation(AnimationUtils.loadAnimation(this , R.anim.fade_in));
            }
        }
    }

    private void hideTopicLib(){
        Animation fadeOut = AnimationUtils.loadAnimation(getApplicationContext() , R.anim.fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation) {
                topicLib.setVisibility(View.INVISIBLE);
                hideBtn.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        topicLib.startAnimation(fadeOut);
        hideBtn.startAnimation(fadeOut);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mainCoverView.setVisibility(View.GONE);
        botCoverView.setVisibility(View.GONE);
        sendPicView.setVisibility(View.INVISIBLE);
        Uri uri = null;
        if(requestCode == Code.CAMERA_REQUEST  && resultCode ==RESULT_OK ){
            uri = imgUri;
        }else if(requestCode == Code.GALLERY_REQUEST && resultCode ==RESULT_OK  && data!=null){
            uri = data.getData();
        }
        if (uri != null) {
            //output
            ItemChat itemChat = new ItemChat(chatList.size(),
                    auth.getCurrentUser().getUid() ,
                    mySelfiePath ,
                    otherSelfiePath ,
                    uri.toString() ,
                    new MyTime().getCurrentTime(),
                    true);
            chatAdapter.addMsg(itemChat);
            list.scrollToPosition(chatList.size()-1);//自動滾動到底部
            //DB
            String fileName = new RandomCode().generateCode(8);
            final StorageReference imgRef = storageRef.child("RandomChatRoom/" +chatRoomID+"/"+auth.getCurrentUser().getUid()+"/"+fileName+".png");//圖片存放路徑
            final Uri finalUri = uri;
            firestore.collection(CHAT_ROOM_TYPE).document(chatRoomID).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(final DocumentSnapshot documentSnapshot) {
                            final int lineNum = documentSnapshot.get("lineNum" , Integer.class);
                            final String lastLine = "(照片)";
                            final Timestamp lastTime = new MyTime().getCurrentTime();
                            //upload file
                            imgRef.putFile(finalUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            //上傳資料庫
                                            Map<String , Object> update = new HashMap<>();
                                            update.put("index", lineNum);
                                            update.put("userUID" , auth.getCurrentUser().getUid());
                                            update.put("text" , "");
                                            update.put("imgUrl" , uri.toString());
                                            update.put("time" , new MyTime().getCurrentTime());
                                            firestore.collection(CHAT_ROOM_TYPE).document(chatRoomID).collection("conversation")
                                                    .document(String.valueOf(lineNum)).set(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    //修改lineNum
                                                    Map<String , Object> update = new HashMap<>();
                                                    update.put("lineNum", (lineNum + 1));
                                                    firestore.collection(CHAT_ROOM_TYPE).document(chatRoomID)
                                                            .update(update);
                                                    //更新已讀數
                                                    updateReadLine();
                                                    //更新last time
                                                    updateLastTime(lastTime);
                                                    //更新lastLine
                                                    updateLastLine(lastLine);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
        }
    }
}