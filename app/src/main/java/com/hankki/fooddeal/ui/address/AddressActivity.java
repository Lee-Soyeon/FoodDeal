package com.hankki.fooddeal.ui.address;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hankki.fooddeal.R;
import com.hankki.fooddeal.data.PreferenceManager;
import com.hankki.fooddeal.data.retrofit.APIClient;
import com.hankki.fooddeal.data.retrofit.APIInterface;
import com.hankki.fooddeal.data.security.AES256Util;
import com.hankki.fooddeal.ui.MainActivity;
import com.hankki.fooddeal.ux.recyclerview.AddressAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class AddressActivity extends AppCompatActivity {

    private APIInterface apiInterface;
    private EditText et_address;
    private TextView tv_search_result;
    private RecyclerView rv_search_result;
    private AddressAdapter addressAdapter;
    private TextWatcher addressWatcher;
    private Button btn_current_position;

    private Disposable disposable;

    private GPSTracker gpsTracker;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private ArrayList<String> currentAddressList;
    private ArrayList<String> current1depthAddressList;
    private ArrayList<String> current2depthAddressList;
    private ArrayList<String> current3depthAddressList;

    private ArrayList<String> addressList;
    private ArrayList<String> region1depthAddressList;
    private ArrayList<String> region2depthAddressList;
    private ArrayList<String> region3depthAddressList;
    private ArrayList<String> regionLoadNameList;
    private ArrayList<String> longitudeList;
    private ArrayList<String> latitudeList;

    private Timer timer = new Timer();
    private final long DELAY = 500; // in ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        tv_search_result = findViewById(R.id.tv_search_result);
        rv_search_result = (RecyclerView)findViewById(R.id.rv_search_result);
        rv_search_result.addItemDecoration(new DividerItemDecoration(rv_search_result.getContext(), 1));

        apiInterface = APIClient.getKakaoClient().create(APIInterface.class);
        et_address = findViewById(R.id.et_address);

        // ????????? ??? ???????????? ??????????????? ????????? 2?????? ????????? ???, ??? ?????????
        addressWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int starht, int before, int count) {
                if(timer != null)
                    timer.cancel();
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() >= 2) {
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            // TODO: do what you need here (refresh list)
                            // you will probably need to use
                            // runOnUiThread(Runnable action) for some specific
                            // actions
                            searchAddressFromEditText();
                        }

                    }, DELAY);
                }

            }
        };
        et_address.addTextChangedListener(addressWatcher);

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        } else {
            checkRunTimePermission();
        }

        btn_current_position = findViewById(R.id.btn_current_position);
        btn_current_position.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchAddressFromCurrentLocation();
            }
        });
    }

    public void searchAddressFromCurrentLocation() {
        gpsTracker = new GPSTracker(AddressActivity.this);

        double longitude = gpsTracker.getLongitude();
        double latitude = gpsTracker.getLatitude();
        PreferenceManager.setString(getApplicationContext(), "Longitude", AES256Util.aesEncode(String.valueOf(longitude)));
        PreferenceManager.setString(getApplicationContext(), "Latitude", AES256Util.aesEncode(String.valueOf(latitude)));

        tv_search_result.setText("'?????? ??????' ?????? ??????");

        disposable = Observable.fromCallable(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Call<ResponseBody> currentAddressCall = apiInterface.getCurrentAddress(longitude, latitude);
                try {
                    ResponseBody responseBody = currentAddressCall.execute().body();
                    currentAddressList = new ArrayList<>();
                    current1depthAddressList = new ArrayList<>();
                    current2depthAddressList = new ArrayList<>();
                    current3depthAddressList = new ArrayList<>();

                    if (responseBody != null) {
                        JSONObject jsonObject = new JSONObject(responseBody.string());
                        JSONArray jsonArray = jsonObject.getJSONArray("documents");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            jsonObject = jsonArray.getJSONObject(i);

                            if (jsonObject.getString("region_type").equals("B")) {
                                currentAddressList.add(jsonObject.getString("address_name"));
                                current1depthAddressList.add(jsonObject.getString("region_1depth_name"));
                                current2depthAddressList.add(jsonObject.getString("region_2depth_name"));
                                current3depthAddressList.add(jsonObject.getString("region_3depth_name"));
                            }
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                return false;
            }
        })

                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object result) throws Exception {
                        rv_search_result.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        addressAdapter = new AddressAdapter(currentAddressList);

                        addressAdapter.setOnItemClickListener(new AddressAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(View v, int pos) {
                                Intent intent = new Intent(AddressActivity.this, MainActivity.class);

                                String region1Depth = current1depthAddressList.get(pos);
                                if (region1Depth.contains("??????")) {
                                    PreferenceManager.setString(getApplicationContext(), "region1Depth", "?????????");
                                }
                                if (region1Depth.contains("??????")) {
                                    PreferenceManager.setString(getApplicationContext(), "region1Depth", "?????????");
                                }
                                PreferenceManager.setString(getApplicationContext(), "region2Depth", current2depthAddressList.get(pos));
                                PreferenceManager.setString(getApplicationContext(), "region3Depth", current3depthAddressList.get(pos));

                                startActivity(intent);
                                finish();
                            }
                        });

                        rv_search_result.setAdapter(addressAdapter);
                    }
                });

    }

    public void searchAddressFromEditText() {
        disposable = Observable.fromCallable(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Call<ResponseBody> addressSearchCall = apiInterface.getAddress(et_address.getText().toString());

                addressList = new ArrayList<String>();
                region1depthAddressList = new ArrayList<>();
                region2depthAddressList = new ArrayList<>();
                region3depthAddressList = new ArrayList<>();
                regionLoadNameList = new ArrayList<>();
                longitudeList = new ArrayList<>();
                latitudeList = new ArrayList<>();

                try {
                    ResponseBody responseBody = addressSearchCall.execute().body();

                    if(responseBody != null) {
                        JSONObject jsonObject = new JSONObject(responseBody.string());
                        JSONArray jsonArray = jsonObject.getJSONArray("documents");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            jsonObject = jsonArray.getJSONObject(i);
                            addressList.add(jsonObject.getString("address_name"));

                            if (jsonObject.getString("address_type").equals("ROAD")) {
                                region1depthAddressList.add(jsonObject.getJSONObject("road_name").getString("region_1depth_name"));
                                region2depthAddressList.add(jsonObject.getJSONObject("road_name").getString("region_2depth_name"));
                                region3depthAddressList.add(jsonObject.getJSONObject("road_name").getString("region_3depth_name"));
                                regionLoadNameList.add(jsonObject.getJSONObject("road_name").getString("road_name"));

                            } else {
                                region1depthAddressList.add(jsonObject.getJSONObject("address").getString("region_1depth_name"));
                                region2depthAddressList.add(jsonObject.getJSONObject("address").getString("region_2depth_name"));

                                if (jsonObject.getJSONObject("address").getString("region_3depth_name").equals("")) {
                                    region3depthAddressList.add(jsonObject.getJSONObject("address").getString("region_3depth_h_name"));
                                } else {
                                    region3depthAddressList.add(jsonObject.getJSONObject("address").getString("region_3depth_name"));
                                }

                                regionLoadNameList.add(jsonObject.getJSONObject("address").getString("road_name"));


                                longitudeList.add(jsonObject.getJSONObject("address").getString("x"));
                                latitudeList.add(jsonObject.getJSONObject("address").getString("y"));
                            }
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                return false;
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object result) throws Exception {
                        disposable.dispose();
                        rv_search_result.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        addressAdapter = new AddressAdapter(addressList);

                        addressAdapter.setOnItemClickListener(new AddressAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(View v, int pos) {
                                Intent intent = new Intent(AddressActivity.this, MainActivity.class);
                                String region1Depth = region1depthAddressList.get(pos);
                                if (region1Depth.contains("??????")) {
                                    PreferenceManager.setString(getApplicationContext(), "region1Depth", "?????????");
                                }
                                if (region1Depth.contains("??????")) {
                                    PreferenceManager.setString(getApplicationContext(), "region1Depth", "?????????");
                                }
                                PreferenceManager.setString(getApplicationContext(), "region2Depth", region2depthAddressList.get(pos));
                                PreferenceManager.setString(getApplicationContext(), "region3Depth", region3depthAddressList.get(pos));

                                if (regionLoadNameList.get(pos) != null) {
                                    PreferenceManager.setString(getApplicationContext(), "roadName", regionLoadNameList.get(pos));
                                } else {
                                    PreferenceManager.setString(getApplicationContext(), "roadName", "null");
                                }

                                Log.d("######", PreferenceManager.getString(getApplicationContext(), "roadName"));

                                PreferenceManager.setString(getApplicationContext(), "Latitude", AES256Util.aesEncode(latitudeList.get(pos)));
                                PreferenceManager.setString(getApplicationContext(), "Longitude", AES256Util.aesEncode(longitudeList.get(pos)));
                                startActivity(intent);
                                finish();
                            }
                        });

                        rv_search_result.setAdapter(addressAdapter);
                        tv_search_result.setText("'" + et_address.getText().toString() + "' ?????? ??????");
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // ?????? ????????? PERMISSIONS_REQUEST_CODE ??????, ????????? ????????? ???????????? ??????????????????
            boolean check_result = true;

            // ?????? ???????????? ??????????????? ???????????????.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if (check_result) {
                //?????? ?????? ????????? ??? ??????
            }
            else {
                // ????????? ???????????? ????????? ?????? ????????? ??? ?????? ????????? ??????????????? ?????? ???????????????.2 ?????? ????????? ????????????.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(AddressActivity.this, "???????????? ?????????????????????. ?????? ?????? ???????????? ???????????? ??????????????????.", Toast.LENGTH_LONG).show();
                    finish();
                }else {
                    Toast.makeText(AddressActivity.this, "???????????? ?????????????????????. ??????(??? ??????)?????? ???????????? ???????????? ?????????. ", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    void checkRunTimePermission(){
        //????????? ????????? ??????
        // 1. ?????? ???????????? ????????? ????????? ???????????????.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(AddressActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(AddressActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. ?????? ???????????? ????????? ?????????
            // ( ??????????????? 6.0 ?????? ????????? ????????? ???????????? ???????????? ????????? ?????? ????????? ?????? ???????????????.)
            // 3.  ?????? ?????? ????????? ??? ??????
        } else {  //2. ????????? ????????? ????????? ?????? ????????? ????????? ????????? ???????????????. 2?????? ??????(3-1, 4-1)??? ????????????.
            // 3-1. ???????????? ????????? ????????? ??? ?????? ?????? ????????????
            if (ActivityCompat.shouldShowRequestPermissionRationale(AddressActivity.this, REQUIRED_PERMISSIONS[0])) {
                // 3-2. ????????? ???????????? ?????? ?????????????????? ???????????? ????????? ????????? ???????????? ????????? ????????????.
                Toast.makeText(AddressActivity.this, "??? ?????? ??????????????? ?????? ?????? ????????? ???????????????.", Toast.LENGTH_LONG).show();
                // 3-3. ??????????????? ????????? ????????? ?????????. ?????? ????????? onRequestPermissionResult?????? ???????????????.
                ActivityCompat.requestPermissions(AddressActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            } else {
                // 4-1. ???????????? ????????? ????????? ??? ?????? ?????? ???????????? ????????? ????????? ?????? ?????????.
                // ?????? ????????? onRequestPermissionResult?????? ???????????????.
                ActivityCompat.requestPermissions(AddressActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    // ??????????????? GPS ???????????? ?????? ????????????
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddressActivity.this);
        builder.setTitle("?????? ????????? ????????????");
        builder.setMessage("?????? ???????????? ???????????? ?????? ???????????? ???????????????.\n"
                + "?????? ????????? ???????????????????");
        builder.setCancelable(true);
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //???????????? GPS ?????? ???????????? ??????
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("#####", "onActivityResult : GPS ????????? ?????????");
                        checkRunTimePermission();
                        return;
                    }
                }

                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}