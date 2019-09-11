package com.test.anonymous;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ChatRoomActivity extends AppCompatActivity {

    //RecyclerView
    private List<ItemFriends> friends;
    private RecyclerView list;
    private FriendsAdapter friendsAdapter;
    private RecyclerView.LayoutManager layoutManager;

    //firestore
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_chat_room);

        //firebase初始化
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        Toast.makeText(this , getIntent().getExtras().getString("chatRoomID") , Toast.LENGTH_LONG).show();
    }
}
