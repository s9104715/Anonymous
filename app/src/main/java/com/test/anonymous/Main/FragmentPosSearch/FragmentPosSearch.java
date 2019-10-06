package com.test.anonymous.Main.FragmentPosSearch;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.test.anonymous.R;
import com.test.anonymous.Tools.Code;
import com.test.anonymous.Tools.RecyclerViewTools.FriendsList.FriendsAdapter;
import com.test.anonymous.Tools.RecyclerViewTools.FriendsList.ItemFriends;

import java.util.List;

import at.markushi.ui.CircleButton;

public class FragmentPosSearch extends Fragment implements View.OnClickListener {

    //RecyclerView
    private List<ItemFriends> friends;
    private RecyclerView list;
    private FriendsAdapter friendsAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private CircleButton searchBtn;

    //location
    private FusedLocationProviderClient fusedLocationClient;
//    private double latitude;
//    private double longitude;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pos_search, container, false);

        list = view.findViewById(R.id.list);
        searchBtn = view.findViewById(R.id.search_btn);

        searchBtn.setOnClickListener(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        return view;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.search_btn:
                search();
                break;
        }
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
                    Code.Location_Request_Code);
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

//    private void setLocation(Location location){
//        latitude = location.getLatitude();
//        longitude = location.getLongitude();
//    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == Code.Location_Request_Code && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
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
}
