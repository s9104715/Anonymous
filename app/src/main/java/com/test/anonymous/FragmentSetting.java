package com.test.anonymous;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class FragmentSetting extends Fragment {

    private RelativeLayout profileBtn , settingBtn;
    private ConstraintLayout profileOption;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        profileBtn = view.findViewById(R.id.profile_btn);
        settingBtn = view.findViewById(R.id.setting_btn);
        profileOption = view.findViewById(R.id.profile_option);

        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profileOption.setVisibility(View.VISIBLE);
                profileOption.setAnimation(AnimationUtils.loadAnimation(getContext() , R.anim.fade_in));
                ConstraintSet set = new ConstraintSet();
                set.clear(R.id.setting_btn , ConstraintSet.TOP);
                set.connect(R.id.setting_btn , ConstraintSet.TOP , R.id.profile_option , ConstraintSet.BOTTOM , 5);
            }
        });
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext() , "wefwef" , Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }
}
