package com.test.anonymous.Main.FragmentRandomChat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.test.anonymous.R;
import com.test.anonymous.Tools.Keyboard;
import com.test.anonymous.Tools.LoadingProcessDialog;
import com.test.anonymous.Tools.RecyclerViewTools.FriendsList.FriendsAdapter;
import com.test.anonymous.Tools.RecyclerViewTools.FriendsList.ItemFriends;
import com.test.anonymous.Tools.RecyclerViewTools.FriendsList.ItemFriendsComparator;
import com.test.anonymous.Tools.Task;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import cc.cloudist.acplibrary.ACProgressConstant;

public class FragmentRandomChat extends Fragment implements View.OnClickListener {

    //RecyclerView
    private List<ItemFriends> friends;
    private RecyclerView list;
    private FriendsAdapter friendsAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ConstraintLayout noFriends;
    public static Button chatBtn;

    private Task msgSonarTask;

    //on long click option
    public static View coverView;
    private AlertDialog friendsOnLongClickAD;
    public static ConstraintLayout editNameView;
    private EditText editNameET;
    private Button editNameBtn;

    //firestore
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_random_chat, container, false);

        list = view.findViewById(R.id.list);
        noFriends = view.findViewById(R.id.no_friends);
        chatBtn = view.findViewById(R.id.chat_btn);
        coverView = view.findViewById(R.id.cover_view);
        editNameView = view.findViewById(R.id.edit_name_view);
        editNameET = view.findViewById(R.id.edit_name_ET);
        editNameBtn = view.findViewById(R.id.edit_name_btn);

        chatBtn.setOnClickListener(this);

        //firebase初始化
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.chat_btn:
                randomChat();
                break;
        }
    }
    //進入此頁面都會呼叫此function
    @Override
    public void onResume() {
        super.onResume();
        loadFriends();
        buildMsgSonarTask();
        msgSonarTask.activateTask(5000 , 5000);
    }

    @Override
    public void onPause() {
        super.onPause();
        msgSonarTask.disableTask();
    }

    //載入朋友清單
    private void loadFriends(){
        //show loadingPD
       final LoadingProcessDialog loadingPD = new LoadingProcessDialog(ACProgressConstant.DIRECT_CLOCKWISE ,
               Color.WHITE , false , false , getContext())
               .show();

        friends = new ArrayList<>();
        //搜尋Random_Friends
        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Random_Friends")
                .orderBy("lastTime" , Query.Direction.DESCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(final QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots.isEmpty()){
                    //no friends
                    loadingPD.dismiss();
                    noFriends.setVisibility(View.VISIBLE);
                }else {
                    noFriends.setVisibility(View.GONE);
                    for (final DocumentSnapshot friendsDocumentSnapshot : queryDocumentSnapshots) {
                        final String friendUID = friendsDocumentSnapshot.getId();
                        final String chatRoomID = friendsDocumentSnapshot.getString("chatRoomID");
                        final String name = friendsDocumentSnapshot.getString("name");
                        final String lastLine = friendsDocumentSnapshot.getString("lastLine");
                        final Timestamp lastTime = friendsDocumentSnapshot.get("lastTime" , Timestamp.class);
                        final int readLine = friendsDocumentSnapshot.get("readLine" , Integer.TYPE);
                        //透過查詢到的chatRoomID再到RandomChatRoom找尋lastLine
                        firestore.collection("RandomChatRoom").document(friendsDocumentSnapshot.getString("chatRoomID")).get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot chatRoomDocumentSnapshot) {
                                        final int lineNum= chatRoomDocumentSnapshot.get("lineNum", Integer.class);
                                        firestore.collection("User").document(friendUID).get()
                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot userDocumentSnapshot) {
                                                        friends.add(new ItemFriends(friendUID ,
                                                                userDocumentSnapshot.getString("selfiePath") ,
                                                                name ,
                                                                chatRoomID ,
                                                                lastLine ,
                                                                lastTime ,
                                                                (lineNum - readLine)));
                                                        Collections.sort(friends , new ItemFriendsComparator());//sort with lastTime
                                                        if(friends.size() == queryDocumentSnapshots.size()){
                                                                setupRecyclerView();
                                                                loadingPD.dismiss();
                                                        }
                                                    }
                                                });
                                    }
                                });
                    }
                }
            }
        });
    }

    private void setupRecyclerView() {

        list.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        friendsAdapter = new FriendsAdapter(friends);
        list.setLayoutManager(layoutManager);
        list.setAdapter(friendsAdapter);

        friendsAdapter.setOnItemClickListener(new FriendsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //click method
                Intent intent = new Intent(getContext() , ChatRoomActivity.class);
                intent.putExtra("chatRoomID" , friends.get(position).getChatRoomID())
                            .putExtra("myUID" , auth.getCurrentUser().getUid())
                            .putExtra("otherUID" , friends.get(position).getUserUID())
                            .putExtra("name" , friends.get(position).getName())
                            .putExtra("chat_room_type" , "RandomChatRoom")
                            .putExtra("friend_type" , "Random_Friends");
                startActivity(intent);
            }
        });

        friendsAdapter.setLongClickListener(new FriendsAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(final int position) {
                View view = getLayoutInflater().inflate(R.layout.dialog_friends_on_long_click_option , null);
                AlertDialog.Builder ADBuilder = new AlertDialog.Builder(getContext())
                        .setTitle(friends.get(position).getName())
                        .setView(view);
                friendsOnLongClickAD = ADBuilder.create();
                friendsOnLongClickAD.show();
                view.findViewById(R.id.edit_name_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        friendsOnLongClickAD.dismiss();
                        editName(friends.get(position) , position);
                    }
                });
                view.findViewById(R.id.block_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        friendsOnLongClickAD.dismiss();
                        block(friends.get(position) , position);
                    }
                });
                view.findViewById(R.id.leave_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        friendsOnLongClickAD.dismiss();
                        leave(friends.get(position) , position);
                    }
                });
            }
        });
    }

    //修改朋友名
    private void editName(final ItemFriends itemFriends , final int position){

        coverView.setVisibility(View.VISIBLE);
        editNameView.setVisibility(View.VISIBLE);
        Animation moveUp = AnimationUtils.loadAnimation(getContext() , R.anim.move_upward);
        moveUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                chatBtn.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        editNameView.startAnimation(moveUp);
        editNameET.setText(itemFriends.getName());
        //outside view surround editNameBtn
        coverView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //close editNameBtn
                chatBtn.setVisibility(View.VISIBLE);
                new Keyboard(getActivity().getSystemService(Context.INPUT_METHOD_SERVICE) , view).close();
                Animation moveDown = AnimationUtils.loadAnimation(getContext() , R.anim.move_down);
                moveDown.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        coverView.setVisibility(View.GONE);
                        editNameView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                editNameView.startAnimation(moveDown);
            }
        });

        editNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!editNameET.getText().toString().equals(itemFriends.getName())){
                    //DB
                    Map<String , Object> update = new HashMap<>();
                    update.put("name" , editNameET.getText().toString());
                    firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Random_Friends")
                            .document(itemFriends.getUserUID()).update(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //change friends list
                            itemFriends.setName(editNameET.getText().toString());
                            friendsAdapter.updateItem(position , itemFriends);
                        }
                    });
                }
                new Keyboard(getActivity().getSystemService(Context.INPUT_METHOD_SERVICE) , view).close();
                chatBtn.setVisibility(View.VISIBLE);
                Animation moveDown = AnimationUtils.loadAnimation(getContext() , R.anim.move_down);
                moveDown.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        coverView.setVisibility(View.GONE);
                        editNameView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                editNameView.startAnimation(moveDown);
            }
        });


    }
    //封鎖朋友
    private void block(final ItemFriends itemFriends , final int position){

        AlertDialog blockAD = new AlertDialog.Builder(getContext())
                .setTitle("封鎖好友")
                .setMessage(R.string.block_msg)
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        firestore.collection("User").document(itemFriends.getUserUID()).get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(final DocumentSnapshot friendsDocumentSnapshot) {
                                        //UI
                                        friendsAdapter.removeItem(position);
                                        //DB add block_list
                                        Map<String , Object> update = new HashMap<>();
                                        update.put("name" , friendsDocumentSnapshot.getString("name"));//get real name
                                        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Block_List")
                                                .document(friendsDocumentSnapshot.getId()).set(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //DB delete Random_Friends
                                                firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Random_Friends")
                                                        .document(friendsDocumentSnapshot.getId()).delete();
                                                //DB chatRoom
                                                firestore.collection("RandomChatRoom").document(itemFriends.getChatRoomID()).get()
                                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                if(documentSnapshot.getBoolean("userHasLeft")!=null){
                                                                    //delete chatRoom
                                                                    firestore.collection("RandomChatRoom").document(itemFriends.getChatRoomID()).collection("conversation")
                                                                            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                        @Override
                                                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                            for (DocumentSnapshot conversationDocumentSnapshot : queryDocumentSnapshots){
                                                                                //delete conversation
                                                                                conversationDocumentSnapshot.getReference().delete();
                                                                            }
                                                                            firestore.collection("RandomChatRoom").document(itemFriends.getChatRoomID()).delete();
                                                                        }
                                                                    });
                                                                }else {
                                                                    //update chatRoom
                                                                    Map<String , Object> update = new HashMap<>();
                                                                    update.put("userHasLeft" , true);
                                                                    firestore.collection("RandomChatRoom").document(itemFriends.getChatRoomID())
                                                                            .update(update);
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
    //刪除朋友
    private void leave(final ItemFriends itemFriends , final int position){

        AlertDialog leaveAD = new AlertDialog.Builder(getContext())
                .setTitle("刪除好友")
                .setMessage(R.string.leave_msg)
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //UI
                        friendsAdapter.removeItem(position);
                        //DB delete Random_Friends
                        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Random_Friends")
                                .document(itemFriends.getUserUID()).delete();
                        //DB chatRoom
                        firestore.collection("RandomChatRoom").document(itemFriends.getChatRoomID()).get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if(documentSnapshot.getBoolean("userHasLeft")!=null){
                                            //delete chatRoom
                                            firestore.collection("RandomChatRoom").document(itemFriends.getChatRoomID()).collection("conversation")
                                                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                    for (DocumentSnapshot conversationDocumentSnapshot : queryDocumentSnapshots){
                                                        //delete conversation
                                                        conversationDocumentSnapshot.getReference().delete();
                                                    }
                                                    firestore.collection("RandomChatRoom").document(itemFriends.getChatRoomID()).delete();
                                                }
                                            });
                                        }else {
                                            //update chatRoom
                                            Map<String , Object> update = new HashMap<>();
                                            update.put("userHasLeft" , true);
                                            firestore.collection("RandomChatRoom").document(itemFriends.getChatRoomID())
                                                    .update(update);
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

    /*開始隨機聊天
         建立match的情況(角色為Matcher)：
             沒有match
             所以match中的contact都為true
         不建立match的情況(角色為Finder)：
              已存在match且有至少一match的contact為false  */
    private void randomChat(){

        firestore.collection("RandomMatch").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Intent intent = new Intent(getContext() , RandomChatWaitingActivity.class);
                if(queryDocumentSnapshots.size() == 0 ){
                    //建立match的狀況(角色為Matcher)
                    intent.putExtra("hasMatcher" , false)
                                .putExtra("order" , 1);
                }else {
                    boolean hasMatcher = false;
                    int order = 0;
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                        if(!documentSnapshot.getBoolean("contact")){
                            hasMatcher = true;
                        }
                        order++;
                    }
                    if (hasMatcher){
                        //不建立match的狀況(角色為Finder)
                        intent.putExtra("hasMatcher" , true);
                    }else {
                        //建立match的狀況(角色為Matcher)
                        intent.putExtra("hasMatcher" , false)
                                    .putExtra("order" , (order + 1));
                    }
                }
               startActivity(intent);
            }
        });
    }

    private void buildMsgSonarTask(){
        msgSonarTask = new Task(new Timer(), new TimerTask() {
            @Override
            public void run() {
                Log.e("MsgSonarTask()" , "isRunning");
                final List<ItemFriends> newFriends = new ArrayList<>();
                for(int i = 0 ; i < friends.size() ; i ++){
                    firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Random_Friends")
                            .document(friends.get(i).getUserUID()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            newFriends.add(new ItemFriends(documentSnapshot.getId() ,
                                    "" ,
                                    "" ,
                                    documentSnapshot.getString("chatRoomID") ,
                                    "" ,
                                    documentSnapshot.get("lastTime" , Timestamp.class) ,
                                    0));
                            Collections.sort(newFriends , new ItemFriendsComparator());
                            if(newFriends.size() == friends.size()){
                                //load complete
                               //compare time between old and new
                                for (int i = 0 ; i <friends.size() ; i ++){
                                    if(!friends.get(i).getLastTime().equals(newFriends.get(i).getLastTime())){
                                        //document has changed
                                        final int finalI = i;
                                        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Random_Friends")
                                                .document(friends.get(i).getUserUID()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot friendsDocumentSnapshot) {
                                                final String friendUID = friendsDocumentSnapshot.getId();
                                                final String chatRoomID = friendsDocumentSnapshot.getString("chatRoomID");
                                                final String lastLine = friendsDocumentSnapshot.getString("lastLine");
                                                final Timestamp lastTime = friendsDocumentSnapshot.get("lastTime" , Timestamp.class);
                                                final int readLine = friendsDocumentSnapshot.get("readLine" , Integer.TYPE);
                                                firestore.collection("RandomChatRoom").document(friendsDocumentSnapshot.getString("chatRoomID")).get()
                                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onSuccess(DocumentSnapshot chatRoomDocumentSnapshot) {
                                                                final int lineNum= chatRoomDocumentSnapshot.get("lineNum", Integer.class);
                                                                firestore.collection("User").document(friendUID).get()
                                                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                            @Override
                                                                            public void onSuccess(DocumentSnapshot userDocumentSnapshot) {
                                                                                friendsAdapter.moveItemToTop(finalI , new ItemFriends(friendUID ,
                                                                                        userDocumentSnapshot.getString("selfiePath") ,
                                                                                        userDocumentSnapshot.getString("name") ,
                                                                                        chatRoomID ,
                                                                                        lastLine ,
                                                                                        lastTime ,
                                                                                        (lineNum - readLine)));
                                                                            }
                                                                        });
                                                            }
                                                        });
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });
    }
}
