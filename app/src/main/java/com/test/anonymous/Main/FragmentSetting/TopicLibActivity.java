package com.test.anonymous.Main.FragmentSetting;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.test.anonymous.Login.LoginActivity;
import com.test.anonymous.R;
import com.test.anonymous.Tools.MyTime;
import com.test.anonymous.Tools.RandomCode;
import com.test.anonymous.Tools.RecyclerViewTools.TopicLibList.ItemTopicLib;
import com.test.anonymous.Tools.RecyclerViewTools.TopicLibList.TopicLibAdapter;
import com.test.anonymous.Tools.RecyclerViewTools.TopicList.ItemTopic;
import com.test.anonymous.Tools.RecyclerViewTools.TopicList.TopicAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopicLibActivity extends AppCompatActivity {

    //topic lib RecyclerView
    private List<ItemTopicLib> topicLibs;
    private RecyclerView topicLibList;
    private TopicLibAdapter topicLibAdapter;
    private RecyclerView.LayoutManager layoutManager;
    //topic RecyclerView
    private List<ItemTopic> topics;
    private RecyclerView topicList;
    private TopicAdapter topicAdapter;

    private AlertDialog uploadTopicAD;
    private ProgressDialog downloadPD;;

    //Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_topic_lib);

        topicLibList = findViewById(R.id.list);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        loadTopicLib();
        setupTopToolBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topic_lib_option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.upload_topic:
                upload();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupTopToolBar() {

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.topic_lib_toolbar);
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

    private void loadTopicLib() {
        topicLibs = new ArrayList<>();
        firestore.collection("Topic_Lib").orderBy("downloadTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(final QuerySnapshot queryDocumentSnapshots) {
                        for (final DocumentSnapshot topicLibDoc : queryDocumentSnapshots) {
                            //get user data
                            firestore.collection("User").document(topicLibDoc.getString("UID")).get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot userDoc) {
                                            topicLibs.add(new ItemTopicLib(topicLibDoc.getId(),
                                                    topicLibDoc.getString("UID"),
                                                    userDoc.getString("name"),
                                                    userDoc.getString("selfiePath"),
                                                    topicLibDoc.getString("topic"),
                                                    topicLibDoc.get("downloadTime", Integer.TYPE)));
                                            //load complete
                                            if (topicLibs.size() == queryDocumentSnapshots.size()) {
                                                setupTopicLibRecyclerView();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void setupTopicLibRecyclerView() {

        filterMyTopic(topicLibs);//過濾屬於自己的topic

        topicLibList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        topicLibAdapter = new TopicLibAdapter(topicLibs , getResources().getColor(R.color.group_setting_color));
        topicLibList.setLayoutManager(layoutManager);
        topicLibList.setAdapter(topicLibAdapter);
        //判斷是否已下載
        setHasDownloaded();

        topicLibAdapter.setOnItemClickListener(new TopicLibAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                downloadTopic(position , topicLibs.get(position).getId()  , topicLibs.get(position).getTopic() , topicLibs.get(position).getDownloadTime());
            }
        });
    }

    //過濾屬於自己的topic
    private void filterMyTopic(List<ItemTopicLib> list){
        for(int i = list.size() -1 ; i >=0  ; i --){
            if(list.get(i).getUID().equals(auth.getCurrentUser().getUid())){
                list.remove(i);
            }
        }
    }

    //判斷是否已下載
    private void setHasDownloaded(){
        ArrayList<String> downloadId = getIntent().getExtras().getStringArrayList("downloadId");
        for(int i = 0 ; i  < topicLibAdapter.getList().size() ; i ++){
            for(String id : downloadId){
                if(topicLibAdapter.getList().get(i).getId().equals(id)){
                    topicLibAdapter.setHasDownloaded(i);
                }
            }
        }
    }

    //show upload AD
    private void upload(){
        View view = getLayoutInflater().inflate(R.layout.dialog_upload_topic_option, null);
        AlertDialog.Builder ADBuilder = new AlertDialog.Builder(this)
                .setTitle("選擇話題")
                .setView(view);
        uploadTopicAD = ADBuilder.create();
        uploadTopicAD.show();

        topicList= view.findViewById(R.id.list);
        ConstraintLayout noTopics = view.findViewById(R.id.no_topics);

        loadTopic(noTopics);
    }

    private void loadTopic(final ConstraintLayout view){
        topics = new ArrayList<>();
        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Topic_Lib")
                .orderBy("time" , Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                            //過濾掉isUploaded 和 isDownload
                            if(documentSnapshot.getString("downloadFromId") == null && documentSnapshot.getBoolean("isUploaded") == null){
                                topics.add(new ItemTopic(documentSnapshot.getId() , documentSnapshot.getString("topic")));
                            }
                        }
                        if(topics.size() == 0){
                            view.setVisibility(View.VISIBLE);//show no topics view
                        }
                        setupTopicRecyclerView();
                    }
                });
    }

    private void setupTopicRecyclerView(){
        topicList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        topicAdapter = new TopicAdapter(topics);
        topicList.setLayoutManager(layoutManager);
        topicList.setAdapter(topicAdapter);

        topicAdapter.setOnItemClickListener(new TopicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                uploadTopic(topics.get(position).getId() , topics.get(position).getTopic());
                uploadTopicAD.dismiss();
            }
        });
    }

    private void uploadTopic(final String id , String topic){
        Map<String , Object> update = new HashMap<>();
        update.put("UID" , auth.getCurrentUser().getUid());
        update.put("topic" , topic);
        update.put("downloadTime" , 0);
        firestore.collection("Topic_Lib").document(new RandomCode().generateCode(8))
                .set(update)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //set topic isUploaded
                        Map<String , Object> update = new HashMap<>();
                        update.put("isUploaded" , true);
                        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Topic_Lib").document(id)
                                .update(update);
                        Toast.makeText(getApplicationContext() , "已上傳，等待審核中" , Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void downloadTopic(final int position , final String id , final String topic , final int downloadTime){

        if(topicLibAdapter.getList().get(position).isHasDownloaded()){
            Toast.makeText(getApplicationContext() , "已經下載過了！" , Toast.LENGTH_LONG).show();
        }else {
            firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Topic_Lib")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            //get topic num
                            if(queryDocumentSnapshots.size() >=10){
                                Toast.makeText(getApplicationContext() , "您的話題數已經超過上限！" , Toast.LENGTH_LONG).show();
                            }else {
                                showDownloadDialog();
                                //download topic
                                Map<String , Object> update = new HashMap<>();
                                update.put("topic" , topic);
                                update.put("time" , new MyTime().getCurrentTime());
                                update.put("downloadFromId"  , id);//判別是否為下載的
                                firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Topic_Lib").document(  new RandomCode().generateCode(4))
                                        .set(update)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                //add downloadTime to topic
                                                Map<String , Object> update = new HashMap<>();
                                                update.put("downloadTime" , (downloadTime+1));
                                                firestore.collection("Topic_Lib").document(id).update(update)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                //UI
                                                                topicLibAdapter.download(position);
                                                                topicLibAdapter.setHasDownloaded(position);

                                                                downloadPD.dismiss();
                                                                Toast.makeText(getApplicationContext() , "下載完成！" , Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                            }
                                        });
                            }
                        }
                    });
        }
    }

    private void showDownloadDialog() {
        downloadPD = new ProgressDialog(TopicLibActivity.this);
        downloadPD.setCancelable(false);
        downloadPD.setCanceledOnTouchOutside(false);
        downloadPD.setTitle("下載話題");
        downloadPD.setMessage("下載中.....");
        downloadPD.show();
    }
}
