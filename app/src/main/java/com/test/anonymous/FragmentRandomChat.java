package com.test.anonymous;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentRandomChat extends Fragment implements View.OnClickListener {

    //RecyclerView
    private List<ItemFriends> friends;
    private RecyclerView list;
    private FriendsAdapter friendsAdapter;
    private RecyclerView.LayoutManager layoutManager;

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


        setupRecyclerView();
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

    //載入朋友清單
    private void loadFriends(){

        friends = new ArrayList<>();
        //loadFriends
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

            }
        });
    }

    //開始隨機聊天
    private void randomChat(){

        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Random_Friends").document("base_doc")
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){

                    //search new friends

                }else {
                    //create Random_Friends collection and set base_doc as 1st document
                    Map<String, Object> update = new HashMap<>();
                    update.put("remark", "this is a base document , do not remove");
                    firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Random_Friends").document("base_doc")
                            .set(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Log.e("add collection" , "success");
                            //search new friends

                        }
                    });
                }
            }
        });
    }
}
