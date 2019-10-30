package com.test.anonymous.Main.FragmentPosSearch;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.test.anonymous.Main.FragmentRandomChat.ChatRoomActivity;
import com.test.anonymous.R;
import com.test.anonymous.Tools.RecyclerViewTools.FriendsList.FriendsAdapter;
import com.test.anonymous.Tools.RecyclerViewTools.FriendsList.ItemFriends;
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

    private ConstraintLayout noinvitation;

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
        noinvitation = findViewById(R.id.no_invitation);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        instance = this;
        loadInvitation();
        setupTopToolBar();
    }

    @Override
    public void finish() {
        super.finish();
        instance = null;
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
        if(getIntent().getExtras().getInt("inviteNum") == 0){
            noinvitation.setVisibility(View.VISIBLE);
        }else {
            invitations = new ArrayList<>();
            firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Pos_Search_Invitation").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
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
                    });
        }
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
//
//        friendsAdapter.setLongClickListener(new FriendsAdapter.OnItemLongClickListener() {
//            @Override
//            public void onItemLongClick(final int position) {
//                View view = getLayoutInflater().inflate(R.layout.dialog_friends_on_long_click_option , null);
//                AlertDialog.Builder ADBuilder = new AlertDialog.Builder(getContext())
//                        .setTitle(friends.get(position).getName())
//                        .setView(view);
//                friendsOnLongClickAD = ADBuilder.create();
//                friendsOnLongClickAD.show();
//                view.findViewById(R.id.edit_name_btn).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        friendsOnLongClickAD.dismiss();
//                        editName(friends.get(position) , position);
//                    }
//                });
//                view.findViewById(R.id.block_btn).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        friendsOnLongClickAD.dismiss();
//                        block(friends.get(position) , position);
//                    }
//                });
//                view.findViewById(R.id.leave_btn).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        friendsOnLongClickAD.dismiss();
//                        leave(friends.get(position) , position);
//                    }
//                });
//            }
//        });
//    }
    }
}