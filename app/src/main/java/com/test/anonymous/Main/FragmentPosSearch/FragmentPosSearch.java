package com.test.anonymous.Main.FragmentPosSearch;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.test.anonymous.Main.FragmentRandomChat.ChatRoomActivity;
import com.test.anonymous.R;
import com.test.anonymous.Tools.Code;
import com.test.anonymous.Tools.LoadingProcessDialog;
import com.test.anonymous.Tools.RecyclerViewTools.FriendsList.FriendsAdapter;
import com.test.anonymous.Tools.RecyclerViewTools.FriendsList.ItemFriends;
import com.test.anonymous.Tools.RecyclerViewTools.FriendsList.ItemFriendsComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.markushi.ui.CircleButton;
import cc.cloudist.acplibrary.ACProgressConstant;

public class FragmentPosSearch extends Fragment implements View.OnClickListener {

    //RecyclerView
    private List<ItemFriends> friends;
    private RecyclerView list;
    private FriendsAdapter friendsAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private Button searchBtn;
    private CircleButton invitationBtn;
    private RelativeLayout unReadNum;
    private TextView unReadNumTV;
    private ConstraintLayout noFriends;
    private int inviteNum = 0;//邀請數

    //location
    private FusedLocationProviderClient fusedLocationClient;

    //Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pos_search, container, false);

        list = view.findViewById(R.id.list);
        noFriends = view.findViewById(R.id.no_friends);
        searchBtn = view.findViewById(R.id.search_btn);
        invitationBtn = view.findViewById(R.id.invitation_btn);
        unReadNum = view.findViewById(R.id.unRead_line_num);
        unReadNumTV = view.findViewById(R.id.unRead_line_num_TV);

        searchBtn.setOnClickListener(this);
        invitationBtn.setOnClickListener(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.search_btn:
                search();
                break;
            case R.id.invitation_btn:
                Intent intent = new Intent(getContext() , InvitationBoxActivity.class);
                intent.putExtra("inviteNum" , inviteNum);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        inviteNum = 0;
        setupUI();
        loadFriends();
    }

    private void setupUI(){
        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Pos_Search_Invitation")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int unReadNum = 0;
                for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                    inviteNum++;
                    if(!documentSnapshot.getBoolean("read")){
                        unReadNum ++;
                    }
                }
                if(unReadNum > 0){
                    FragmentPosSearch.this.unReadNum.setVisibility(View.VISIBLE);
                    unReadNumTV.setText(String.valueOf(unReadNum));
                }else {
                    FragmentPosSearch.this.unReadNum.setVisibility(View.GONE);
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void search(){

        //permission check
        if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Code.LOCATION_REQUEST);
            return;
        }
        //granted
//        //locationRequest
//        LocationRequest locationRequest = new LocationRequest();
//        locationRequest.setInterval(1000); // two minute interval
//        locationRequest.setFastestInterval(120000);
//        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//        //locationCallback
//        LocationCallback locationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                List<Location> locationList = locationResult.getLocations();
//                if(locationList.size() > 0 ){
//                    setLocation(locationList.get(0));
//                }
//            }
//        };
//        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        //start waiting
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                try {
                    Intent intent = new Intent( getContext() , PosSearchWaiting.class);
                    intent.putExtra("latitude" , location.getLatitude())
                            .putExtra("longitude" , location.getLongitude());
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("GetLocationError" , e.toString());
                    Toast.makeText(getContext() , "GPS尚未定位" , Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == Code.LOCATION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            //granted
            //start waiting
            fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    try {
                        Intent intent = new Intent( getContext() , PosSearchWaiting.class);
                        intent.putExtra("latitude" , location.getLatitude())
                                .putExtra("longitude" , location.getLongitude());
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e("GetLocationError" , e.toString());
                        Toast.makeText(getContext() , "GPS尚未定位" , Toast.LENGTH_LONG).show();
                    }
                }
            });
        }else {
            //權限不被允許
            Toast.makeText(getContext(), "權限不被允許", Toast.LENGTH_LONG).show();
        }
    }

    //載入朋友清單
    private void loadFriends(){
        //show loadingPD
        final LoadingProcessDialog loadingPD = new LoadingProcessDialog(ACProgressConstant.DIRECT_CLOCKWISE ,
                Color.WHITE , false , false , getContext())
                .show();

        friends = new ArrayList<>();
        //搜尋Pos_Search_Friends
        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Pos_Search_Friends")
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
                        //透過查詢到的chatRoomID再到PosSearchChatRoom找尋lastLine
                        firestore.collection("PosSearchChatRoom").document(friendsDocumentSnapshot.getString("chatRoomID")).get()
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
                        .putExtra("chat_room_type" , "PosSearchChatRoom")
                        .putExtra("friend_type" , "Pos_Search_Friends");
                startActivity(intent);
            }
        });

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
    }
}
