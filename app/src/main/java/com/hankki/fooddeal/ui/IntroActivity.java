package com.hankki.fooddeal.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hankki.fooddeal.R;
import com.hankki.fooddeal.data.PreferenceManager;
import com.hankki.fooddeal.data.retrofit.APIClient;
import com.hankki.fooddeal.data.retrofit.APIInterface;
import com.hankki.fooddeal.data.security.AES256Util;
import com.hankki.fooddeal.ui.address.GPSTracker;
import com.hankki.fooddeal.ui.login.LoginActivity;
import com.hankki.fooddeal.ui.register.PhoneAuthActivity;
import com.hankki.fooddeal.ui.register.RegisterActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;

/**로그인 유지 상태가 아닐 경우, 로그인 또는 회원가입을 선택하는 화면*/
public class IntroActivity extends AppCompatActivity {

    private GPSTracker gpsTracker;

    private TextView tv_tour;
    private Button btn_register;
    private Button btn_login;

    Disposable disposable;
    APIInterface apiInterface;

    private ArrayList<String> currentAddressList;
    private ArrayList<String> current1depthAddressList;
    private ArrayList<String> current2depthAddressList;
    private ArrayList<String> current3depthAddressList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        apiInterface = APIClient.getKakaoClient().create(APIInterface.class);

        onClickButton();
    }

    private void onClickButton(){
        tv_tour = findViewById(R.id.tv_tour);
        tv_tour.setOnClickListener(v -> {
            gpsTracker = new GPSTracker(IntroActivity.this);

            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();

            PreferenceManager.setString(getApplicationContext(), "Latitude", AES256Util.aesEncode(String.valueOf(latitude)));
            PreferenceManager.setString(getApplicationContext(), "Longitude", AES256Util.aesEncode(String.valueOf(longitude)));

            disposable = Observable.fromCallable((Callable<Object>) () -> {
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
                            currentAddressList.add(jsonObject.getString("address_name"));
                            current1depthAddressList.add(jsonObject.getString("region_1depth_name"));
                            current2depthAddressList.add(jsonObject.getString("region_2depth_name"));
                            current3depthAddressList.add(jsonObject.getString("region_3depth_name"));
                        }

                        PreferenceManager.setString(getApplicationContext(), "region1Depth", current1depthAddressList.get(0));
                        PreferenceManager.setString(getApplicationContext(), "region2Depth", current2depthAddressList.get(0));
                        PreferenceManager.setString(getApplicationContext(), "region3Depth", current3depthAddressList.get(0));
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                return false;
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        Intent main = new Intent(IntroActivity.this, MainActivity.class);
                        startActivity(main);
                    });
        });


        btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(v -> {
            Intent login = new Intent(IntroActivity.this, LoginActivity.class);
            startActivity(login);
        });

        btn_register = findViewById(R.id.btn_register);
        btn_register.setOnClickListener(v -> {
            Intent register = new Intent(IntroActivity.this, PhoneAuthActivity.class);
            startActivity(register);
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        btn_register = null;
        btn_login = null;
    }
}