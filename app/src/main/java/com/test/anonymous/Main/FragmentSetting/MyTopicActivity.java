package com.test.anonymous.Main.FragmentSetting;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.test.anonymous.R;
import com.test.anonymous.Tools.MyTime;
import com.test.anonymous.Tools.RandomCode;
import com.test.anonymous.Tools.RecyclerViewTools.TopicList.ItemTopic;
import com.test.anonymous.Tools.RecyclerViewTools.TopicList.TopicAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import at.markushi.ui.CircleButton;

public class MyTopicActivity extends AppCompatActivity implements View.OnClickListener {

    private CircleButton addBtn;
    //RecyclerView
    private List<ItemTopic> topics;
    private RecyclerView list;
    private TopicAdapter topicAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private ConstraintLayout noTopic;
    private AlertDialog addAD;
    private AlertDialog addTopicAD;
    private ArrayList<String> downloadId;//儲存下載得到的topic id

    //Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_my_topic);

        addBtn = findViewById(R.id.add_btn);
        list = findViewById(R.id.list);
        noTopic = findViewById(R.id.no_topic);

        addBtn.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        setupTopToolBar();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.add_btn:
                add();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTopic();
    }

    private void setupTopToolBar() {

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.my_topic_toolbar);
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

    private void loadTopic(){
        topics = new ArrayList<>();
        downloadId = new ArrayList<>();
        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Topic_Lib")
                .orderBy("time" , Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.isEmpty()){
                            noTopic.setVisibility(View.VISIBLE);
                        }else {
                            for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                                topics.add(new ItemTopic(documentSnapshot.getId() , documentSnapshot.getString("topic")));
                                if(documentSnapshot.getString("downloadFromId")!=null){
                                    //屬於download type
                                    topics.get(topics.size()-1).setDownloadType(true);//將最後一筆改成download type
                                    downloadId.add(documentSnapshot.getString("downloadFromId"));//收集下載得到的topic id
                                }
                            }
                        }
                        setupRecyclerView();
                    }
                });
    }

    private void setupRecyclerView() {

        list.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        topicAdapter = new TopicAdapter(topics);
        list.setLayoutManager(layoutManager);
        list.setAdapter(topicAdapter);

        topicAdapter.setOnItemClickListener(new TopicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                topicAdapter.unSelect(position);
            }
        });

        topicAdapter.setLongClickListener(new TopicAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position) {
                topicAdapter.select(position);
            }
        });

        topicAdapter.setDeleteListener(new TopicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                deleteTopic(position);
            }
        });
    }

    private void add(){
        View view = getLayoutInflater().inflate(R.layout.dialog_topic_option, null);
        AlertDialog.Builder ADBuilder = new AlertDialog.Builder(this)
                .setTitle("話題庫")
                .setView(view);
        addAD = ADBuilder.create();
        addAD.show();

        view.findViewById(R.id.add_topic_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAD.dismiss();
                final View view = getLayoutInflater().inflate(R.layout.dialog_add_topic, null);
                AlertDialog.Builder ADBuilder = new AlertDialog.Builder(MyTopicActivity.this)
                        .setTitle("新增話題庫")
                        .setView(view);
                addTopicAD = ADBuilder.create();
                addTopicAD.show();

                view.findViewById(R.id.confirm_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText topicET =  view.findViewById(R.id.topic_ET);
                        String topic = topicET.getText().toString().trim();
                        if(topic.isEmpty()){
                            topicET.setError("此欄位不能為空");
                            topicET.requestFocus();
                            return;
                        }
                        addTopic(topic);
                        addTopicAD.dismiss();
                    }
                });
            }
        });

        view.findViewById(R.id.browse_topic_lib_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext() ,  TopicLibActivity.class);
                intent.putExtra("downloadId" , downloadId);
                startActivity(intent);
                addAD.dismiss();
            }
        });
    }

    private void addTopic(String topic){
        if(topics.size() < 10){
            String id = new RandomCode().generateCode(4);
            //UI
            topicAdapter.add(new ItemTopic(id , topic));
            noTopic.setVisibility(View.INVISIBLE);
            //DB
            Map<String , Object> update = new HashMap<>();
            update.put("topic" , topic);
            update.put("time" , new MyTime().getCurrentTime());
            firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Topic_Lib").document(id)
                    .set(update);
        }else {
            Toast.makeText(this , "儲存的話題不能超過10個" , Toast.LENGTH_LONG).show();
        }
    }

    private void deleteTopic(final int position){
        ItemTopic topic = topics.get(position);
        //UI
        topicAdapter.delete(position);
        if(topics.size() == 0){
            noTopic.setVisibility(View.VISIBLE);
        }
        //DB
        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Topic_Lib").document(topic.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.getString("downloadFromId")!=null){
                            unDownload(documentSnapshot.getString("downloadFromId"));
                        }
                        documentSnapshot.getReference().delete();
                    }
                });
    }

    //downloadTime -1
    private void unDownload(String id){
        downloadId.remove(id);
        firestore.collection("Topic_Lib").document(id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Map<String , Object> update = new HashMap<>();
                            update.put("downloadTime" , documentSnapshot.get("downloadTime" , Integer.TYPE) -1);
                            documentSnapshot.getReference().update(update);
                        }
                });
    }
}
