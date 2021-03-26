package com.hankki.fooddeal.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hankki.fooddeal.R;
import com.hankki.fooddeal.data.PreferenceManager;

/**메인 화면. 이곳에 5가지 주요 화면들 바텀 네비게이션으로 출력*/
public class MainActivity extends AppCompatActivity {
    BottomNavigationView navView;
    public static Context mainContext;
    long backKeyPressedTime = 0;
    boolean isBackPressed;

    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainContext = this;

        setBottomNavigation();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            String uid = firebaseUser.getUid();
        } else if (!PreferenceManager.getString(getApplicationContext(), "userToken").equals("")) {

        } else {
            Log.d("###########", "파이어베이스 사용자 없음");
//            Toast.makeText(getApplicationContext(), "파이어베이스 사용자 없음", Toast.LENGTH_SHORT).show();
        }
    }

    /**네비게이션 바 세팅*/
    public void setBottomNavigation(){
        navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_grouppurchase,
                R.id.navigation_chatting, R.id.navigation_mypage)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {

        });
        NavigationUI.setupWithNavController(navView, navController);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(isBackPressed) {
            FirebaseAuth.getInstance().signOut();
        }
    }

    @Override
    public void onBackPressed() {
        Toast toast = null;

        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지났으면 Toast Show
        // 2000 milliseconds = 2 seconds
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지나지 않았으면 종료
        // 현재 표시된 Toast 취소
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            isBackPressed = true;
            navView = null;
            this.finishAffinity();
        }
    }


}
