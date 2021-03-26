package com.hankki.fooddeal.ui.mypage;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.hankki.fooddeal.R;
import com.hankki.fooddeal.data.PreferenceManager;

import java.util.Objects;

public class MySettingActivity extends AppCompatActivity {

    View toolbarView;
    TextView toolbarTextView, tv_logout, tv_bye_bye;
    ImageView iv_logout, iv_bye_bye, back_button;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_setting);
        mContext = this;
        setComponents();
    }

    public void setComponents(){
        toolbarView = findViewById(R.id.top_toolbar);
        toolbarTextView = toolbarView.findViewById(R.id.toolbar_title);
        toolbarTextView.setText("설정");
        back_button = toolbarView.findViewById(R.id.back_button);
        back_button.setOnClickListener(v -> onBackPressed());

        iv_logout = findViewById(R.id.iv_logout);
        tv_logout = findViewById(R.id.tv_logout);
        iv_bye_bye = findViewById(R.id.iv_bye_bye);
        tv_bye_bye = findViewById(R.id.tv_sign_out);

        iv_logout.setOnClickListener(logout);
        tv_logout.setOnClickListener(logout);

        iv_bye_bye.setOnClickListener(signOut);
        tv_bye_bye.setOnClickListener(signOut);
    }

    View.OnClickListener logout = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage("로그아웃 하시겠습니까?");
            builder.setPositiveButton("확인", (dialog, which) -> {
                PreferenceManager.removeKey(mContext,"userToken");
                FirebaseAuth.getInstance().signOut();
                finishAffinity();
            }).setNegativeButton("취소", (dialog, which) -> {
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    };

    View.OnClickListener signOut = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            /**회원탈퇴*/
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage("가지마요...우리 좋았잖아");
            builder.setPositiveButton("잘있어", (dialog, which) -> {
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                PreferenceManager.removeKey(mContext,"userToken");
                Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).delete();
                finishAffinity();
            }).setNegativeButton("안갈게", (dialog, which) -> {
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    };
}