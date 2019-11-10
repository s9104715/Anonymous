package com.test.anonymous.Main.FragmentPosSearch;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.test.anonymous.Main.FragmentRandomChat.ChatRoomActivity;
import com.test.anonymous.R;
import com.test.anonymous.Tools.MyTime;
import com.test.anonymous.Tools.RandomCode;
import com.test.anonymous.Tools.RecyclerViewTools.InvitationList.InvitationAdapter;
import com.test.anonymous.Tools.RecyclerViewTools.InvitationList.ItemInvitation;
import com.test.anonymous.Tools.RecyclerViewTools.InvitationList.ItemInvitationComparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvitationBoxActivity extends AppCompatActivity {

    //RecyclerView
    private List<ItemInvitation> invitations;
    private RecyclerView list;
    private InvitationAdapter invitationAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private ConstraintLayout noInvitation;

    private AlertDialog onLongClickAD;
    private ProgressDialog acceptPD;
    private ProgressDialog refusePD;

    //Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    //this
    public static InvitationBoxActivity instance = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_invitation_box);

        list = findViewById(R.id.list);
        noInvitation = findViewById(R.id.no_invitation);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        instance = this;
        setupTopToolBar();
    }

    @Override
    public void finish() {
        super.finish();
        instance = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInvitation();
    }

    private void setupTopToolBar() {

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.invitation_box_toolbar);
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

    private void loadInvitation() {
        //load Pos_Search_Invitation
        try{
            invitationAdapter.clear();
        }catch (Exception e){
            Log.e("clearError" , e.toString());
        }
        invitations = new ArrayList<>();
        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Pos_Search_Invitation").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.isEmpty()){
                            noInvitation.setVisibility(View.VISIBLE);
                        }else {
                            for (final DocumentSnapshot invitationDoc : queryDocumentSnapshots) {
                                //load inviter data
                                firestore.collection("User").document(invitationDoc.getId()).get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot inviterDoc) {
                                                String info = inviterDoc.get("age" , Integer.TYPE)+" , "+inviterDoc.getString("gender")+" , "+inviterDoc.getString("career");
                                                invitations.add(new ItemInvitation(invitationDoc.getId() ,
                                                        inviterDoc.getString("selfiePath") ,
                                                        inviterDoc.getString("name") ,
                                                        info ,
                                                        invitationDoc.getString("distance") ,
                                                        invitationDoc.getTimestamp("time") ,
                                                        invitationDoc.getBoolean("read")));
                                                Collections.sort(invitations , new ItemInvitationComparator());
                                                if(invitations.size() == getIntent().getExtras().getInt("inviteNum")){
                                                    setupRecyclerView();
                                                    read();
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    private void read(){
        final Map<String , Object> update = new HashMap<>();
        update.put("read" , true);
        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Pos_Search_Invitation").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                            documentSnapshot.getReference().update(update);
                        }
                    }
                });
    }


    private void setupRecyclerView () {

            list.setHasFixedSize(true);
            layoutManager = new LinearLayoutManager(this);
            invitationAdapter = new InvitationAdapter(invitations);
            list.setLayoutManager(layoutManager);
            list.setAdapter(invitationAdapter);

            invitationAdapter.setOnItemClickListener(new InvitationAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    //click method
                    invitationAdapter.read(position);
                    Intent intent = new Intent(getApplicationContext() , InvitationActivity.class);
                    intent.putExtra("UID" , invitations.get(position).getUserUID());
                    intent.putExtra("distance" , invitations.get(position).getDistance());
                    intent.putExtra("type" , "accept");
                    startActivity(intent);
                }
            });
            invitationAdapter.setLongClickListener(new InvitationAdapter.OnItemLongClickListener() {
                @Override
                public void onItemLongClick(final int position) {
                    View view = getLayoutInflater().inflate(R.layout.dialog_invitations_on_long_click_option , null);
                    AlertDialog.Builder ADBuilder = new AlertDialog.Builder(InvitationBoxActivity.this)
                            .setView(view);
                    onLongClickAD = ADBuilder.create();
                    onLongClickAD.show();
                    view.findViewById(R.id.accept_btn).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onLongClickAD.dismiss();
                            showAcceptDialog();
                            accept(invitations.get(position).getUserUID() , invitations.get(position).getName());
                        }
                    });
                    view.findViewById(R.id.refuse_btn).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onLongClickAD.dismiss();
                            refuse(invitations.get(position).getUserUID() , position);
                        }
                    });
                }
            });
        }

    private void accept(final String UID , final String name){
        showAcceptDialog();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //create chat room
                createChatRoom(UID , name);
            }
        } , 2000);
    }

    private void showAcceptDialog() {
        acceptPD = new ProgressDialog(this);
        acceptPD.setCancelable(false);
        acceptPD.setCanceledOnTouchOutside(false);
        acceptPD.setTitle("接受");
        acceptPD.setMessage("處理中.....");
        acceptPD.show();
    }

    private void refuse(final String UID , final int position){
        showRefuseDialog();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Pos_Search_Invitation").document(UID)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                refusePD.dismiss();
                                invitationAdapter.delete(position);
                                if(invitationAdapter.getList().size()==0){
                                    noInvitation.setVisibility(View.VISIBLE);
                                }
                            }
                        });
            }
        } , 2000);
    }

    private void showRefuseDialog() {
        refusePD = new ProgressDialog(this);
        refusePD.setCancelable(false);
        refusePD.setCanceledOnTouchOutside(false);
        refusePD.setTitle("拒絕");
        refusePD.setMessage("處理中.....");
        refusePD.show();
    }

    private void createChatRoom(final String UID , String name){

        final String chatRoomID = new RandomCode().generateCode(8);//聊天室id
        final Timestamp lastTime = new MyTime().getCurrentTime();//last time

        final Map<String, Object> update = new HashMap<>();
        update.put("chatRoomID", chatRoomID);
        update.put("name" , name);
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
