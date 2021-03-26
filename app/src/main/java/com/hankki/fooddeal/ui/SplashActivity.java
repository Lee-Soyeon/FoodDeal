package com.hankki.fooddeal.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hankki.fooddeal.R;
import com.hankki.fooddeal.data.ForcedTerminationService;
import com.hankki.fooddeal.data.PreferenceManager;
import com.hankki.fooddeal.data.retrofit.APIClient;
import com.hankki.fooddeal.data.retrofit.APIInterface;
import com.hankki.fooddeal.data.retrofit.retrofitDTO.MemberResponse;

import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**스플래쉬 화면*/
@RuntimePermissions
public class SplashActivity extends AppCompatActivity {

    Disposable disposable;
    ProgressBar progressBar;

    Intent intent;
    /*
    이현준
    자동 로그인 구현
    */
    private FirebaseAuth firebaseAuth;
    private Activity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View view = getWindow().getDecorView();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(view != null){
                view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                getWindow().setStatusBarColor(getResources().getColor(R.color.original_primary));
            }
        }

        progressBar = findViewById(R.id.customDialog_progressBar);

        disposable = Observable.fromCallable((Callable<Object>) () -> {
            // Firebase 초기 세팅
            firebaseAuth = FirebaseAuth.getInstance();
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(false)
                    .build();
            FirebaseFirestore.getInstance().setFirestoreSettings(settings);

            // 강제종료 알림 서비스
            startService(new Intent(this, ForcedTerminationService.class));

            SplashActivityPermissionsDispatcher.showLocationWithPermissionCheck(this);

            return false;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    disposable.dispose();
                    progressBar.setVisibility(View.VISIBLE);
                });
    }

    // 토큰을 사용한 인증
    private void signInWithCustomToken(String firebaseToken, String userToken) {
        firebaseAuth.signInWithCustomToken(firebaseToken)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        Intent toMainIntent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(toMainIntent);
                        finish();
                    }
                })
                .addOnFailureListener(activity, Throwable::printStackTrace);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        SplashActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        // 이거 때문에 인트로가 두번 실행됨
        /*intent = new Intent(SplashActivity.this, IntroActivity.class);
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            startActivity(intent);
            finish();
        }, 1000);*/

//        return;
    }

    @NeedsPermission({Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
    void showLocation() {
        /* 이현준
        로그인 화면에서 로그인 성공 시, 서버에서 JWT 토큰을 세션 유지용도로 내려줌.
        이것을 SharedPreferences에 저장한 뒤, 어플 종료시 혹은 마이페이지에서 자동 로그인 해제 시 삭제
        자동 로그인 설정 후 어플리케이션 재실행 시, 스플래시에서 SharedPreferences에 토큰이 있는지 판단해서 있으면 바로 홈 액티비티 없으면 인트로 액티비티
        */
        /*
        이현준
        자동 로그인 구현
        */
        if(!PreferenceManager.getString(getApplicationContext(), "userToken").equals("")) {
            String userToken = PreferenceManager.getString(getApplicationContext(), "userToken");
            APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
            Call<MemberResponse> autoLoginCall = apiInterface.autoLogin(userToken);
            autoLoginCall.enqueue(new Callback<MemberResponse>() {
                @Override
                public void onResponse(Call<MemberResponse> call, Response<MemberResponse> response) {
                    MemberResponse memberResponse = response.body();
                    if (memberResponse != null &&
                            memberResponse.getResponseCode() == 500) {
                        signInWithCustomToken(memberResponse.getFirebaseToken(), memberResponse.getUserToken());
                    }
                }

                @Override
                public void onFailure(Call<MemberResponse> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        } else {
            /** SharedPreference 기본틀, key 값이나 변수타입은 추후 수정*/
            /*
            이현준
            Rxjava 내부에서 Looper가 돌아갈 수 없기 때문에 Thread.sleep로 변경
            */
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            intent = new Intent(SplashActivity.this, IntroActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @OnShowRationale({Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
    public void showRationaleForLocation(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage("위치 권한을 허용해 주시기 바랍니다.")
                .setPositiveButton(android.R.string.ok, (dialog, button) -> request.proceed())
                .setNegativeButton(android.R.string.cancel, (dialog, button) -> request.cancel())
                .setCancelable(false)
                .show();
    }

    @OnPermissionDenied({Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
    public void showDeniedForLocation() {
        Toast.makeText(this, "권한을 허용해 주세요.", Toast.LENGTH_SHORT).show();
        finish();
    }

    @OnNeverAskAgain({Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
    public void showNeverAskForLocation() {
        Toast.makeText(this, "권한 허용을 해주지 않으신다면, 서비스 이용이 불가합니다.", Toast.LENGTH_SHORT).show();
        finish();
    }
}
