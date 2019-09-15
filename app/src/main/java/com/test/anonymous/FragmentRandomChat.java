package com.test.anonymous;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.test.anonymous.Tools.RecyclerViewTools.FriendsList.FriendsAdapter;
import com.test.anonymous.Tools.RecyclerViewTools.FriendsList.ItemFriends;
import com.test.anonymous.Tools.TextProcessor;
import java.util.ArrayList;
import java.util.List;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

public class FragmentRandomChat extends Fragment implements View.OnClickListener {
    //RecyclerView
    private List<ItemFriends> friends;
    private RecyclerView list;
    private FriendsAdapter friendsAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ACProgressFlower loadingPD;

    private Button chatBtn;

    //firestore
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_random_chat, container, false);

        list = view.findViewById(R.id.list);
        chatBtn = view.findViewById(R.id.chat_btn);

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
        loadFriends();
        super.onResume();
    }

    //載入朋友清單
    private void loadFriends(){

        showLoadingDialog();

        friends = new ArrayList<>();
        //搜尋Random_Friends
        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Random_Friends")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(final QuerySnapshot queryDocumentSnapshots) {
                for (final DocumentSnapshot friendsDocumentSnapshot : queryDocumentSnapshots){
                    //透過查詢到的chatRoomID再到RandomChatRoom找尋lastLine
                    firestore.collection("RandomChatRoom").document(friendsDocumentSnapshot.getString("chatRoomID")).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(final DocumentSnapshot chatRoomDocumentSnapshot) {
                                    //找尋最後一筆對話
                                    final int lineNum= chatRoomDocumentSnapshot.get("lineNum", Integer.class);
                                    chatRoomDocumentSnapshot.getReference().collection("conversation").document(String.valueOf(( lineNum -1)))
                                            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(final DocumentSnapshot conversationDocumentSnapshot) {
                                            //Random_Friends裡的文件id即為朋友UID,再帶入user尋找資料
                                            firestore.collection("User").document(friendsDocumentSnapshot.getId()).get()
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onSuccess(DocumentSnapshot userDocumentSnapshot) {
                                                            friends.add(new ItemFriends(friendsDocumentSnapshot.getId() ,
                                                                                                            userDocumentSnapshot.getString("selfiePath") ,
                                                                                                            userDocumentSnapshot.getString("name") ,
                                                                                                            friendsDocumentSnapshot.getString("chatRoomID") ,
                                                                                                            new TextProcessor().textFormat(conversationDocumentSnapshot.getString("text") , 14) ,
                                                                                                           lineNum - friendsDocumentSnapshot.get("readLine" , Integer.class)));
                                                            //載入完畢
                                                            if(friends.size() == queryDocumentSnapshots.size()){
                                                                setupRecyclerView();
                                                                loadingPD.dismiss();
                                                            }
                                                        }
                                                    });
                                        }
                                    });
                                }
                            });
                }
            }
        });
    }

    private void setupRecyclerView() {

        list.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        friendsAdapter = new FriendsAdapter(friends );
        list.setLayoutManager(layoutManager);
        list.setAdapter(friendsAdapter);

        friendsAdapter.setOnItemClickListener(new FriendsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //click method
                Intent intent = new Intent(getContext() , ChatRoomActivity.class);
                intent.putExtra("chatRoomID" , friends.get(position).getChatRoomID())
                            .putExtra("myUID" , auth.getCurrentUser().getUid())
                            .putExtra("otherUID" , friends.get(position).getUserUID());
                startActivity(intent);
            }
        });
    }

    public void showLoadingDialog(){
         loadingPD = new ACProgressFlower.Builder(getContext())
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .text("Loading......")
                .fadeColor(Color.DKGRAY).build();
         loadingPD.setCancelable(false);
         loadingPD.setCanceledOnTouchOutside(false);
         loadingPD.show();
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
                Intent intent = new Intent(getContext() , RandomChatWaiting.class);
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
}
