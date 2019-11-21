package com.test.anonymous.Main.FragmentSetting;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.test.anonymous.R;
import com.test.anonymous.Tools.ViewPagerTools.ItemSetting;
import com.test.anonymous.Tools.ViewPagerTools.SettingAdapter;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FragmentSetting extends Fragment {

    private CircleImageView selfie;
    private TextView nameTV;
    private ConstraintLayout botView;
    private ViewPager profileVP , friendListVP , groupVP , topicLibVP , settingVP;
    private ViewPager viewPagerTemp;//VP的暫存 用來指定

    //Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        selfie = view.findViewById(R.id.selfie);
        nameTV = view.findViewById(R.id.name_TV);
        botView = view.findViewById(R.id.bot_view);
        profileVP = view.findViewById(R.id.profile_VP);
        friendListVP = view.findViewById(R.id.friendList_VP);
        groupVP = view.findViewById(R.id.group_VP);
        topicLibVP = view.findViewById(R.id.topic_lib_VP);
        settingVP = view.findViewById(R.id.setting_VP);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        setupUI();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setupUserData();
        scrollVPToOrigin(viewPagerTemp);
    }

    private void setupUI(){

        final List<ItemSetting> profile = new ArrayList<>();
        profile.add(new ItemSetting("" , getResources().getDrawable(R.drawable.transparent_drawable)));
        profile.add(new ItemSetting("個人檔案" , getResources().getDrawable(R.drawable.profile_setting_drawable)));
        List<ItemSetting> friendList = new ArrayList<>();
        friendList.add(new ItemSetting("" , getResources().getDrawable(R.drawable.transparent_drawable)));
        friendList.add(new ItemSetting("朋友清單" , getResources().getDrawable(R.drawable.friend_list_setting_drawable)));
        List<ItemSetting> group = new ArrayList<>();
        group.add(new ItemSetting("" , getResources().getDrawable(R.drawable.transparent_drawable)));
        group.add(new ItemSetting("群組管理" , getResources().getDrawable(R.drawable.group_setting_drawable)));
        List<ItemSetting> topicLib = new ArrayList<>();
        topicLib.add(new ItemSetting("" , getResources().getDrawable(R.drawable.transparent_drawable)));
        topicLib.add(new ItemSetting("話題庫" , getResources().getDrawable(R.drawable.topic_lib_setting_drawable)));
        List<ItemSetting> setting = new ArrayList<>();
        setting.add(new ItemSetting("" , getResources().getDrawable(R.drawable.transparent_drawable)));
        setting.add(new ItemSetting("設定" ,getResources().getDrawable(R.drawable.setting_drawable)));

        SettingAdapter settingAdapter = new SettingAdapter(profile);
        settingAdapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profileVP.setCurrentItem(0 , true);
            }
        });
        profileVP.setAdapter(settingAdapter);
        profileVP.setCurrentItem(1);

        friendListVP.setAdapter(new SettingAdapter(friendList));
        friendListVP.setCurrentItem(1);

        groupVP.setAdapter(new SettingAdapter(group));
        groupVP.setCurrentItem(1);

        settingAdapter = new SettingAdapter(topicLib);
        settingAdapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                topicLibVP.setCurrentItem(0 , true);
            }
        });
        topicLibVP.setAdapter(settingAdapter);
        topicLibVP.setCurrentItem(1);

        settingVP.setAdapter(new SettingAdapter(setting));
        settingVP.setCurrentItem(1);

        profileVP.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                //復原
                if(i == 0){
                    //start activity
                    viewPagerTemp = profileVP;//assign temp
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(getContext() , EditProfileActivity.class));
                        }
                    } , 200);
                }
            }
            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        friendListVP.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {
                switch (i){
                    //拖動、滑動事件
                    case ViewPager.SCROLL_STATE_DRAGGING:

                        break;
                }
            }
        });
        groupVP.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {
                switch (i){
                    //拖動、滑動事件
                    case ViewPager.SCROLL_STATE_DRAGGING:

                        break;
                }
            }
        });
        topicLibVP.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                //復原
                if(i == 0){
                    //start activity
                    viewPagerTemp = topicLibVP;//assign temp
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(getContext() , MyTopicActivity.class));
                        }
                    } , 200);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
//                switch (i){
//                    //拖動、滑動事件
//                    case ViewPager.SCROLL_STATE_DRAGGING:
//
//                        break;
//                }
            }
        });
        settingVP.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {
                switch (i){
                    //拖動、滑動事件
                    case ViewPager.SCROLL_STATE_DRAGGING:

                        break;
                }
            }
        });
    }

    private void setupUserData(){
        firestore.collection("User").document(auth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Picasso.get().load(documentSnapshot.getString("selfiePath"))
                        //圖片使用最低分辨率,降低使用空間大小
                        .fit()
                        .centerCrop()
                        .into(selfie);//取得大頭貼
                nameTV.setText(documentSnapshot.getString("name"));
            }
        });
    }

    //將viewPager回歸到原位
    private void scrollVPToOrigin(final ViewPager viewPager){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try{
                    //scroll to origin position
                    viewPager.setCurrentItem(1);
                }catch (Exception e){
                    Log.e("VP_Error" , e.toString());
                }
            }
        } , 500);
    }
}
