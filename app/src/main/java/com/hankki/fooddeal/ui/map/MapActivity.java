package com.hankki.fooddeal.ui.map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;
import com.hankki.fooddeal.R;
import com.hankki.fooddeal.data.PostItem;
import com.hankki.fooddeal.data.PreferenceManager;
import com.hankki.fooddeal.data.security.AES256Util;
import com.hankki.fooddeal.ui.home.community.Community_detail;
import com.hankki.fooddeal.ui.home.community.PostActivity;
import com.hankki.fooddeal.ux.recyclerview.PostAdapter;
import com.hankki.fooddeal.ux.snaphelper.SnapHelperOneByOne;

import java.util.ArrayList;
import java.util.HashMap;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private ArrayList<PostItem> postItems = new ArrayList<>();
    private ArrayList<PostItem> mapItems = new ArrayList<>();
    private ArrayList<Marker> markers = new ArrayList<>();
    RecyclerView rv_map;
    SnapHelperOneByOne snapHelperOneByOne;
    VerticalSeekBar seekBar;
    TextView tv_100, tv_200, tv_400, tv_all;
    int filterDistance = 1000;
    int zoom = 14;
    Context mContext;
    LatLng currentPostion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mContext = this;

        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        postItems = getIntent().getParcelableArrayListExtra("Items");
        mapItems = postItems;
        findViews();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = (int)(progress/100) * 100;
                seekBar.setProgress(progress);
                switch(progress){
                    case 0:
                        filterDistance = 1000;
                        zoom = 14;
                        tv_all.setTextColor(getResources(). getColor(R.color.original_primary));
                        tv_100.setTextColor(getResources().getColor(R.color.original_black));
                        tv_200.setTextColor(getResources().getColor(R.color.original_black));
                        tv_400.setTextColor(getResources().getColor(R.color.original_black));
                        break;
                    case 100:
                        filterDistance = 400;
                        zoom = 15;
                        tv_all.setTextColor(getResources().getColor(R.color.original_black));
                        tv_100.setTextColor(getResources().getColor(R.color.original_black));
                        tv_200.setTextColor(getResources().getColor(R.color.original_black));
                        tv_400.setTextColor(getResources().getColor(R.color.original_primary));
                        break;
                    case 200:
                        filterDistance = 200;
                        zoom = 16;
                        tv_all.setTextColor(getResources().getColor(R.color.original_black));
                        tv_100.setTextColor(getResources().getColor(R.color.original_black));
                        tv_200.setTextColor(getResources().getColor(R.color.original_primary));
                        tv_400.setTextColor(getResources().getColor(R.color.original_black));
                        break;
                    case 300:
                        filterDistance = 100;
                        zoom = 17;
                        tv_all.setTextColor(getResources().getColor(R.color.original_black));
                        tv_100.setTextColor(getResources().getColor(R.color.original_primary));
                        tv_200.setTextColor(getResources().getColor(R.color.original_black));
                        tv_400.setTextColor(getResources().getColor(R.color.original_black));
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mapItems = filterDistance(postItems, filterDistance);
                setMarkerOption(zoom);
            }
        });
    }

    public void findViews(){
        tv_100 = findViewById(R.id.tv_100);
        tv_200 = findViewById(R.id.tv_200);
        tv_400 = findViewById(R.id.tv_400);
        tv_all = findViewById(R.id.tv_all);
        seekBar = findViewById(R.id.seekbar);
        seekBar.bringToFront();
        seekBar.setMax(300);
        rv_map = findViewById(R.id.rv_map);
        snapHelperOneByOne = new SnapHelperOneByOne();
        snapHelperOneByOne.attachToRecyclerView(rv_map);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        double latitude = Double.parseDouble(AES256Util.aesDecode(PreferenceManager.getString(getApplicationContext(), "Latitude")));
        double longitude = Double.parseDouble(AES256Util.aesDecode(PreferenceManager.getString(getApplicationContext(), "Longitude")));

        currentPostion = new LatLng(latitude, longitude);setMarkerOption(zoom);
        setMarkerOption(zoom);
    }

    public void setMarkerOption(int zoom){
        map.clear();
        markers.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentPostion);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_icon_current));
        markerOptions.title("현재 위치");
        markerOptions.anchor((float)0.5,(float)0.5);
        markerOptions.snippet(PreferenceManager.getString(this, "region1Depth") + " " +
                PreferenceManager.getString(this, "region2Depth") + " " +
                PreferenceManager.getString(this, "region3Depth"));
        map.addMarker(markerOptions);



//        postItems.addAll(BoardController.getBoardList(mContext, "INGREDIENT EXCHANGE"));
//        postItems.addAll(BoardController.getBoardList(mContext,"INGREDIENT SHARE"));
        HashMap<String, ArrayList<PostItem>> placeMap = new HashMap<>();
        ArrayList<String> placeKey = new ArrayList<>();
        for (PostItem postItem : mapItems) {
            String key = String.valueOf(postItem.getUserLatitude()) + " " + String.valueOf(postItem.getUserLongitude());
            if(placeMap.get(key)==null){
                ArrayList<PostItem> items = new ArrayList<>();
                items.add(postItem);
                placeMap.put(key,items);
            } else {
                ArrayList<PostItem> items = placeMap.get(key);
                items.add(0,postItem);
                placeMap.put(key,items);
            }
            if (!placeKey.contains(key)) {
                placeKey.add(key);
            }
        }

        for(String key : placeKey){
            String[] location = key.split(" ");
            double latitude = Double.parseDouble(location[0]);
            double longitude = Double.parseDouble(location[1]);
            try {
                LatLng position = new LatLng(latitude, longitude);

                markerOptions = new MarkerOptions();
                markerOptions.position(position);

                ArrayList<PostItem> items = placeMap.get(key);
                int size = items.size();
                int markerHeight = 49*2;
                int markerWidth = 32*2;
                if(size <= 3){
                } else if(size <=6){
                    markerHeight += 24;
                    markerWidth += 16;
                } else if(size <=9){
                    markerHeight += 49;
                    markerWidth += 32;
                } else {
                    markerHeight += 73;
                    markerWidth += 48;
                }
                PostItem postItem = items.get(0);

                if(postItem.getCategory().equals("INGREDIENT EXCHANGE")) {
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_icon_marker);
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    Bitmap markerIcon = Bitmap.createScaledBitmap(bitmap,markerWidth,markerHeight,false);
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(markerIcon));
                }
                else {
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_icon_marker_2);
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    Bitmap markerIcon = Bitmap.createScaledBitmap(bitmap,markerWidth,markerHeight,false);
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(markerIcon));
                }


                /*@TODO markerOption.icon 설정*/
                markerOptions.title(size+"개의 게시글이 있어요");
                markers.add(map.addMarker(markerOptions));

            } catch (Exception e){
                Log.d("map",e.getMessage());
            }
        }
        map.setOnMarkerClickListener(marker -> {
            LatLng position = marker.getPosition();
            String key = position.latitude + " " + position.longitude;
            ArrayList<PostItem> items = placeMap.get(key);
            PostAdapter adapter = new PostAdapter(this,items,R.layout.community_item3);
            rv_map.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
            rv_map.setAdapter(adapter);
            rv_map.setVisibility(View.VISIBLE);
            return false;
        });

//        map.setOnInfoWindowClickListener(marker -> {
//            int index = markers.indexOf(marker);
//            Intent intent = new Intent(MapActivity.this, Community_detail.class);
//            intent.putExtra("page",0);
//            intent.putExtra("Tag","Main");
//            intent.putExtra("item", mapItems.get(index));
//            startActivity(intent);
//        });

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPostion, zoom));
        CircleOptions circle1KM = new CircleOptions().center(currentPostion) // 원점
                .radius(filterDistance)      // 반지름 단위 : m
                .strokeWidth(0f)  // 선너비 0f : 선없음
                .fillColor(Color.parseColor("#88ffb5c5")); // 배경색
        map.addCircle(circle1KM);
    }

    public ArrayList<PostItem> filterDistance(ArrayList<PostItem> items, int distance){
        ArrayList<PostItem> filteredItems = new ArrayList<>();
        for(PostItem item: items){
            if(item.getDistance() <= distance){
                filteredItems.add(item);
            }
        }
        return filteredItems;
    }
}
