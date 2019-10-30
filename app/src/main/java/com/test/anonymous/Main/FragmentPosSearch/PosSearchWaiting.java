package com.test.anonymous.Main.FragmentPosSearch;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.jh.circularlist.CircularListView;
import com.jh.circularlist.CircularTouchListener;
import com.squareup.picasso.Picasso;
import com.test.anonymous.R;
import com.test.anonymous.Tools.CircularListTools.ItemPosUserCircular;
import com.test.anonymous.Tools.CircularListTools.CircularPosUserAdapter;
import com.test.anonymous.Tools.RecyclerViewTools.PosUserList.ItemPosUserComparator;
import com.test.anonymous.Tools.RecyclerViewTools.PosUserList.ItemPosUserRecycler;
import com.test.anonymous.Tools.RecyclerViewTools.PosUserList.RecyclerPosUserAdapter;
import com.test.anonymous.Tools.TextProcessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PosSearchWaiting extends AppCompatActivity implements View.OnClickListener {

    private ImageView backBtn , sortBtn;
    private CardView sortView;
    private RelativeLayout imgSortBtn , listSortBtn;
    private View coverView;
    private ProgressBar waitingBar;
    private TextView waitingTV;
    private Button cancelBtn;//搜尋完成後功能變為重新搜尋
    //CircularListView
    private List<ItemPosUserCircular> posUserCircularList;
    private CircularListView circularList;
    private CircularPosUserAdapter circularPosUserAdapter;
    private CircleImageView selfie;
    //RecyclerView
    private List<ItemPosUserRecycler> posUserRecyclerList;
    private RecyclerView recyclerList;
    private RecyclerPosUserAdapter recyclerPosUserAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private boolean listIsInitialize = false;//清單是否完初始化
    private boolean isDone = false;//搜尋是否完畢

    private double latitude;
    private double longitude;

    //Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_pos_search_waiting);

        backBtn = findViewById(R.id.back_btn);
        sortBtn = findViewById(R.id.sort_btn);
        sortView = findViewById(R.id.sort_view);
        imgSortBtn = findViewById(R.id.img_sort_bnt);
        listSortBtn = findViewById(R.id.list_sort_bnt);
        coverView = findViewById(R.id.cover_view);
        recyclerList = findViewById(R.id.list);
        waitingBar = findViewById(R.id.waiting_bar);
        waitingTV = findViewById(R.id.waiting_TV);
        cancelBtn = findViewById(R.id.cancel_btn);
        circularList = findViewById(R.id.circular_list);
        selfie = findViewById(R.id.selfie);

        backBtn.setOnClickListener(this);
        sortBtn.setOnClickListener(this);
        imgSortBtn.setOnClickListener(this);
        listSortBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);

        latitude = getIntent().getExtras().getDouble("latitude");
        longitude= getIntent().getExtras().getDouble("longitude");

        auth = FirebaseAuth.getInstance();
        firestore =FirebaseFirestore.getInstance();

        search();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back_btn:
                onBackPressed();
                break;
            case R.id.sort_btn:
                if(sortView.getVisibility() == View.GONE){
                     showSortView();
                }else {
                    hideSortView();
                }
                break;
            case R.id.img_sort_bnt:
                hideSortView();
                if(circularList.getVisibility() == View.INVISIBLE){
                    showCircularList();
                }
                break;
            case R.id.list_sort_bnt:
                hideSortView();
                if(circularList.getVisibility() == View.VISIBLE){
                    showRecyclerList();
                }
                break;
            case  R.id.cancel_btn:
                if(!isDone){
                    onBackPressed();
                }else {
                    //重新搜尋
                    search();
                }
                break;
        }
    }

    private void showSortView(){
        sortView.setVisibility(View.VISIBLE);
        Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext() , R.anim.fade_in);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                coverView.setVisibility(View.VISIBLE);
                coverView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        coverView.setVisibility(View.GONE);
                        Animation fadeOut = AnimationUtils.loadAnimation(getApplicationContext() , R.anim.fade_out);
                        fadeOut.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                sortView.setVisibility(View.GONE);
                            }
                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        sortView.startAnimation(fadeOut);
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        sortView.startAnimation(fadeIn);
    }

    private void hideSortView(){
        coverView.setVisibility(View.GONE);
        Animation fadeOut = AnimationUtils.loadAnimation(getApplicationContext() , R.anim.fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation) {
                sortView.setVisibility(View.GONE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        sortView.startAnimation(fadeOut);
    }

    private void showCircularList(){
        Animation fadeOut = AnimationUtils.loadAnimation(getApplicationContext() , R.anim.fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                recyclerList.setVisibility(View.INVISIBLE);
                circularList.setVisibility(View.VISIBLE);
                selfie.setVisibility(View.VISIBLE);
                circularList.startAnimation(AnimationUtils.loadAnimation(getApplicationContext() , R.anim.fade_in));
                selfie.startAnimation(AnimationUtils.loadAnimation(getApplicationContext() , R.anim.fade_in));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        recyclerList.startAnimation(fadeOut);
    }

    private void showRecyclerList(){
        Animation fadeOut = AnimationUtils.loadAnimation(getApplicationContext() , R.anim.fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                circularList.setVisibility(View.INVISIBLE);
                selfie.setVisibility(View.INVISIBLE);
                recyclerList.setVisibility(View.VISIBLE);
                recyclerList.startAnimation(AnimationUtils.loadAnimation(getApplicationContext() , R.anim.fade_in));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        circularList.startAnimation(fadeOut);
        selfie.startAnimation(AnimationUtils.loadAnimation(this , R.anim.fade_out));
    }

    private void search() {
        // hide list
        isDone = false;
        cancelBtn.setText("取消");
        sortBtn.setVisibility(View.INVISIBLE);
        waitingBar.setVisibility(View.VISIBLE);
        waitingTV.setVisibility(View.VISIBLE);
        circularList.setVisibility(View.INVISIBLE);
        recyclerList.setVisibility(View.INVISIBLE);
        selfie.setVisibility(View.INVISIBLE);

        //load selfie
        firestore.collection("User").document(auth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Picasso.get().load(documentSnapshot.getString("selfiePath"))
                                //圖片使用最低分辨率,降低使用空間大小
                                .fit()
                                .centerCrop()
                                .into(selfie);//取得大頭貼
                    }
                });

        //update location
        Map<String, Object> update = new HashMap<>();
        update.put("position", new GeoPoint(latitude, longitude));
        firestore.collection("User").document(auth.getCurrentUser().getUid())
                .update(update);

        //load other location
        posUserCircularList = new ArrayList<>();
        posUserRecyclerList = new ArrayList<>();
        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Pos_Search_Friends").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        //add friendUIDList
                        final List<String> friendUIDList = new ArrayList<>();
                        for (DocumentSnapshot friendDoc : queryDocumentSnapshots) {
                            friendUIDList.add(friendDoc.getId());
                        }

                        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Random_Friends").get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        //add friendUIDList
                                        for (DocumentSnapshot friendDoc : queryDocumentSnapshots) {
                                            friendUIDList.add(friendDoc.getId());
                                        }

                                        firestore.collection("User").document(auth.getCurrentUser().getUid()).collection("Block_List").get()
                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                        //add blockList
                                                        final ArrayList<String> blockList = new ArrayList<>();
                                                        for (DocumentSnapshot blockDoc : queryDocumentSnapshots) {
                                                            blockList.add(blockDoc.getId());
                                                        }

                                                        firestore.collection("User").get()
                                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onSuccess(final QuerySnapshot userQueryDoc) {

                                                                        int i = 0;//查詢次數
                                                                        for (final DocumentSnapshot userDoc : userQueryDoc) {
                                                                            i++;
                                                                            final int finalI = i;
                                                                            if (!userDoc.getId().equals(auth.getCurrentUser().getUid())) {
                                                                                userDoc.getReference().collection("Block_List").get()
                                                                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                                            @Override
                                                                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                                                                                boolean isFriend = false;
                                                                                                boolean isBlocked = false;

                                                                                                //判斷對方是否封鎖自己
                                                                                                for (DocumentSnapshot userBlockDoc : queryDocumentSnapshots) {
                                                                                                    if (userBlockDoc.getId().equals(auth.getCurrentUser().getUid())) {
                                                                                                        isBlocked = true;
                                                                                                        break;
                                                                                                    }
                                                                                                }

                                                                                                //判斷是否已成為朋友
                                                                                                for (String friendUID : friendUIDList) {
                                                                                                    if (friendUID.equals(userDoc.getId())) {
                                                                                                        isFriend = true;
                                                                                                        break;
                                                                                                    }
                                                                                                }

                                                                                                //判斷對方是否已被封鎖
                                                                                                for (String blockID : blockList)
                                                                                                    if (blockID.equals(userDoc.getId())) {
                                                                                                        isBlocked = true;
                                                                                                        break;
                                                                                                    }

                                                                                                if (!isFriend && !isBlocked) {
                                                                                                    //load user data
                                                                                                    try {
                                                                                                        posUserCircularList.add(new ItemPosUserCircular(userDoc.getId(),
                                                                                                                userDoc.getString("name"),
                                                                                                                userDoc.getString("selfiePath"),
                                                                                                                new GeoPoint(latitude, longitude),
                                                                                                                userDoc.get("position", GeoPoint.class)));

                                                                                                        posUserRecyclerList.add(new ItemPosUserRecycler(userDoc.getId(),
                                                                                                                userDoc.getString("name"),
                                                                                                                userDoc.getString("selfiePath"),
                                                                                                                new GeoPoint(latitude, longitude),
                                                                                                                userDoc.get("position", GeoPoint.class)));
                                                                                                        Collections.sort(posUserRecyclerList, new ItemPosUserComparator());
                                                                                                    } catch (Exception e) {
                                                                                                        Log.e("loadPosError", e.toString());
                                                                                                    }
                                                                                                }
                                                                                                //load complete 最多找出8位5公里以內的使用者
                                                                                                if (finalI == userQueryDoc.getDocuments().size()) {
                                                                                                    final List<ItemPosUserCircular> circularResult = new ArrayList<>();
                                                                                                    final List<ItemPosUserRecycler> recyclerResult = new ArrayList<>();
                                                                                                    for (ItemPosUserCircular posUser : posUserCircularList) {
                                                                                                        //找出5公里以內user
                                                                                                        if (posUser.getDistance() <= 5) {
                                                                                                            if (circularResult.size() < 8) {
                                                                                                                circularResult.add(posUser);
                                                                                                            }
                                                                                                        }
                                                                                                    }
                                                                                                    for (ItemPosUserRecycler posUser : posUserRecyclerList) {
                                                                                                        //找出5公里以內user
                                                                                                        if (posUser.getDistance() <= 5) {
                                                                                                            if (recyclerResult.size() < 8) {
                                                                                                                recyclerResult.add(posUser);
                                                                                                            }
                                                                                                        }
                                                                                                    }
                                                                                                    new Handler().postDelayed(new Runnable() {
                                                                                                        @Override
                                                                                                        public void run() {
                                                                                                            showResult(circularResult, recyclerResult);
                                                                                                        }
                                                                                                    }, 2000);
                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    }
                                                                });

                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    private void showResult (final List<ItemPosUserCircular> circularResult , final List<ItemPosUserRecycler> recyclerResult){
        //UI
        waitingBar.setVisibility(View.INVISIBLE);
        waitingTV.setVisibility(View.INVISIBLE);
        cancelBtn.setText("重新搜尋");
        sortBtn.setVisibility(View.VISIBLE);
        circularList.setVisibility(View.VISIBLE);
        selfie.setVisibility(View.VISIBLE);

        if(!listIsInitialize){
            //初次搜尋
            listIsInitialize = true;
            //circularList
            circularPosUserAdapter = new CircularPosUserAdapter(circularResult , getLayoutInflater());
            circularList.setAdapter(circularPosUserAdapter);
            circularList.setOnItemClickListener(new CircularTouchListener.CircularItemClickListener() {
                @Override
                public void onItemClick(View view, int i) {
                    Intent intent = new Intent(getApplicationContext() , InvitationActivity.class);
                    intent.putExtra("UID" , circularResult.get(i).getUID());
                    intent.putExtra("distance" , new TextProcessor().doubleFormat("#.##" , circularResult.get(i).getDistance()));
                    intent.putExtra("type" , "invite");
                    startActivity(intent);
                }
            });
            //recyclerList
            recyclerList.setHasFixedSize(true);
            layoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerPosUserAdapter = new RecyclerPosUserAdapter(recyclerResult);
            recyclerList.setLayoutManager(layoutManager);
            recyclerList.setAdapter(recyclerPosUserAdapter);
            recyclerPosUserAdapter.setOnItemClickListener(new RecyclerPosUserAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    Intent intent = new Intent(getApplicationContext() , InvitationActivity.class);
                    intent.putExtra("UID" , recyclerResult.get(position).getUID());
                    intent.putExtra("distance" , new TextProcessor().doubleFormat("#.##" , recyclerResult.get(position).getDistance()));
                    intent.putExtra("type" , "invite");
                    startActivity(intent);
                }
            });
        }else {
            //重新搜尋
            //add circular list
            circularPosUserAdapter.removeAll();
            for(ItemPosUserCircular item : circularResult){
                View view = getLayoutInflater().inflate(R.layout.item_pos_search_circular, null);
                Picasso.get().load(item.getSelfiePath())
                        //圖片使用最低分辨率,降低使用空間大小
                        .fit()
                        .centerCrop()
                        .into((CircleImageView)view.findViewById(R.id.selfie));//取得大頭貼
                circularPosUserAdapter.addItem(view);
            }
            //add recycler list
            recyclerPosUserAdapter.removeAll();
            for(ItemPosUserRecycler item : recyclerResult){
               recyclerPosUserAdapter.addItem(item);
            }
        }
        isDone = true;
    }
}
