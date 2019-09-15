package com.test.anonymous;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import com.test.anonymous.Tools.MyTime;
import com.test.anonymous.Tools.RandomCode;
import com.test.anonymous.Tools.RecyclerViewTools.ChatList.ChatAdapter;
import com.test.anonymous.Tools.RecyclerViewTools.ChatList.ItemChat;
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

    //RecyclerView
    private List<ItemChat> chatList;
    private RecyclerView list;
    private ChatAdapter chatAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private CircleButton chatBtn , sendPicBtn;
    private EditText inputET;

    private String chatRoomID;
    private String mySelfiePath;
    private String otherSelfiePath;

    //監聽對方訊息
    private Task msgSonarTask;

    //camera and Gallery
    private File imgFile;
    private Uri imgUri;
    private final int CAMERA_REQUEST = 1888;
    private final int GALLERY_REQUEST = 1889;

    //firestore
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private StorageReference storageRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_chat_room);

        list = findViewById(R.id.list);
        chatBtn = findViewById(R.id.chat_btn);
        sendPicBtn = findViewById(R.id.send_pic_btn);
        inputET= findViewById(R.id.input_ET);

        chatBtn.setOnClickListener(this);
        sendPicBtn.setOnClickListener(this);

        //firebase初始化
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        setupTopToolBar();
        loadMsg();
        buildMsgSonarTask();;
        msgSonarTask.activateTask(1000 , 1000);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.chat_btn:
                sendMsg();
                break;
            case R.id.send_pic_btn:
                sendPic();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        msgSonarTask.disableTask();
        super.onBackPressed();
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
                                        otherSelfiePath = documentSnapshot.getString("selfiePath");
                                        setTitle(documentSnapshot.getString("name"));//set title
                                        //載入對話
                                        chatList = new ArrayList<>();
                                        firestore.collection("RandomChatRoom").document(chatRoomID).collection("conversation")
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
                                                                new MyTime().getFormatTime(documentSnapshot.get("time" , Timestamp.class) , "hh:mm")));
                                                    }else {
                                                        //圖片訊息
                                                        chatList.add(new ItemChat(documentSnapshot.get("index" , Integer.class) ,
                                                                documentSnapshot.getString("userUID") ,
                                                                mySelfiePath ,
                                                                otherSelfiePath ,
                                                                documentSnapshot.getString("imgUrl"),
                                                                new MyTime().getFormatTime(documentSnapshot.get("time" , Timestamp.class) , "hh:mm") ,
                                                                true));
                                                    }
                                                }
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
//        list.smoothScrollToPosition(chatList.size()-1);

        chatAdapter.setOnItemClickListener(new ChatAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(getApplicationContext() , BrowseImgActivity.class);
                intent.putExtra("picUri" , chatList.get(position).getImgUrl());
                startActivity(intent);
            }
        });
    }

    //更新已讀數
    private void updateReadLine(){
        firestore.collection("RandomChatRoom").document(chatRoomID).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Map<String , Object> update = new HashMap<>();
                        update.put("readLine", documentSnapshot.get("lineNum" , Integer.class));
                        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Random_Friends")
                                .document(getIntent().getExtras().getString("otherUID")).update(update);
                    }
                });
    }

    //資料餵給資料庫都是由此function負責
    private void sendMsg(){

        if(inputET.getText().toString().trim().isEmpty()){//沒打字
            inputET.setError("請輸入想傳送的訊息！");
            inputET.requestFocus();
        }else {
            firestore.collection("RandomChatRoom").document(chatRoomID).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            //lineNum
                            final int lineNum = documentSnapshot.get("lineNum" , Integer.class);
                            //output
                            final ItemChat itemChat = new ItemChat(lineNum ,
                                    auth.getCurrentUser().getUid() ,
                                    mySelfiePath ,
                                    otherSelfiePath ,
                                    inputET.getText().toString(),
                                    new MyTime().getFormatTime(new MyTime().getCurrentTime() , "hh:mm"));
                            chatAdapter.addMsg(itemChat);
                            list.scrollToPosition(chatList.size()-1);//自動滾動到底部
                            //上傳資料庫
                            Map<String , Object> update = new HashMap<>();
                            update.put("index", lineNum);
                            update.put("userUID" , auth.getCurrentUser().getUid());
                            update.put("text" , inputET.getText().toString());
                            update.put("time" , new MyTime().getCurrentTime());
                            inputET.setText("");
                            firestore.collection("RandomChatRoom").document(chatRoomID).collection("conversation")
                                    .document(String.valueOf(lineNum)).set(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //修改lineNum
                                    Map<String , Object> update = new HashMap<>();
                                    update.put("lineNum", (lineNum + 1));
                                    firestore.collection("RandomChatRoom").document(chatRoomID)
                                            .update(update)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //更新已讀數
                                            updateReadLine();
                                        }
                                    });
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
                firestore.collection("RandomChatRoom").document(chatRoomID).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                final int lineNum = documentSnapshot.get("lineNum" , Integer.class);
                                Log.e("listen lineNum", ""+lineNum);
                                firestore.collection("RandomChatRoom").document(chatRoomID).collection("conversation").document(String.valueOf(lineNum))
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
                                                            new MyTime().getFormatTime(documentSnapshot.get("time" , Timestamp.class) , "hh:mm")));
                                                }else {
                                                    //圖片訊息
                                                    chatAdapter.addMsg(new ItemChat(documentSnapshot.get("index" , Integer.class) ,
                                                            documentSnapshot.getString("userUID") ,
                                                            mySelfiePath ,
                                                            otherSelfiePath ,
                                                            documentSnapshot.getString("imgUrl"),
                                                            new MyTime().getFormatTime(documentSnapshot.get("time" , Timestamp.class) , "hh:mm") ,
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

    private void sendPic(){
        final AlertDialog sendPicDialog;
        View view = getLayoutInflater().inflate(R.layout.dialog_send_pic,null);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this)
                .setTitle("請選擇")
                .setView(view);
        sendPicDialog = dialogBuilder.create();
        sendPicDialog.show();

        view.findViewById(R.id.camera_bnt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPicDialog.dismiss();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                imgFile = new File(getExternalCacheDir(),
                        (System.currentTimeMillis()) + ".jpg");
                imgUri = Uri.fromFile(imgFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
                startActivityForResult(intent, CAMERA_REQUEST);
            }
        });
        view.findViewById(R.id.gallery_bnt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPicDialog.dismiss();
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = null;
        if(requestCode == CAMERA_REQUEST && resultCode ==RESULT_OK ){
            uri = imgUri;
        }else if(requestCode == GALLERY_REQUEST && resultCode ==RESULT_OK  && data!=null){
            uri = data.getData();
        }
        if (uri != null) {
            //output
            ItemChat itemChat = new ItemChat(chatList.get(chatList.size() - 1).getIndex(),
                    auth.getCurrentUser().getUid() ,
                    mySelfiePath ,
                    otherSelfiePath ,
                    uri.toString() ,
                    new MyTime().getFormatTime(new MyTime().getCurrentTime() , "hh:mm"),
                    true);
            chatAdapter.addMsg(itemChat);
            list.scrollToPosition(chatList.size()-1);//自動滾動到底部
            //DB
            String fileName = new RandomCode().generateCode(8);
            final StorageReference imgRef = storageRef.child("RandomChatRoom/" +chatRoomID+"/"+auth.getCurrentUser().getUid()+"/"+fileName+".png");//圖片存放路徑
            final Uri finalUri = uri;
            firestore.collection("RandomChatRoom").document(chatRoomID).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(final DocumentSnapshot documentSnapshot) {
                            final int lineNum = documentSnapshot.get("lineNum" , Integer.class);
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
                                            firestore.collection("RandomChatRoom").document(chatRoomID).collection("conversation")
                                                    .document(String.valueOf(lineNum)).set(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    //修改lineNum
                                                    Map<String , Object> update = new HashMap<>();
                                                    update.put("lineNum", (lineNum + 1));
                                                    firestore.collection("RandomChatRoom").document(chatRoomID)
                                                            .update(update)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    //更新已讀數
                                                                    updateReadLine();
                                                                }
                                                            });
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

