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

        // 함수를 두 번씩이나 불러오는데 적어도 2글자 이상일 때, 초 넣어서
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

        tv_search_result.setText("'현재 위치' 검색 결과");

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
                                if (region1Depth.contains("서울")) {
                                    PreferenceManager.setString(getApplicationContext(), "region1Depth", "서울시");
                                }
                                if (region1Depth.contains("경기")) {
                                    PreferenceManager.setString(getApplicationContext(), "region1Depth", "경기도");
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
                                if (region1Depth.contains("서울")) {
                                    PreferenceManager.setString(getApplicationContext(), "region1Depth", "서울시");
                                }
                                if (region1Depth.contains("경기")) {
                                    PreferenceManager.setString(getApplicationContext(), "region1Depth", "경기도");
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
                        tv_search_result.setText("'" + et_address.getText().toString() + "' 검색 결과");
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if (check_result) {
                //위치 값을 가져올 수 있음
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(AddressActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();
                }else {
                    Toast.makeText(AddressActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    void checkRunTimePermission(){
        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(AddressActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(AddressActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
            // 3.  위치 값을 가져올 수 있음
        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(AddressActivity.this, REQUIRED_PERMISSIONS[0])) {
                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(AddressActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(AddressActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(AddressActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    // 여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddressActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
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

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("#####", "onActivityResult : GPS 활성화 되있음");
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