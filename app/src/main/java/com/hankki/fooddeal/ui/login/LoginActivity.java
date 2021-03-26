package com.hankki.fooddeal.ui.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.hankki.fooddeal.R;
import com.hankki.fooddeal.data.PreferenceManager;
import com.hankki.fooddeal.data.RegularCheck;
import com.hankki.fooddeal.data.retrofit.APIClient;
import com.hankki.fooddeal.data.retrofit.APIInterface;
import com.hankki.fooddeal.data.retrofit.retrofitDTO.MemberResponse;
import com.hankki.fooddeal.data.security.AES256Util;
import com.hankki.fooddeal.data.security.HashMsgUtil;
import com.hankki.fooddeal.ui.MainActivity;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * 소비자용, 사업자용 2개 탭뷰 구성
 */

/**
 * 로그인 액티비티
 * 로그인 유지 상태일 경우, 이 화면은 건너뜀.
 * 로그인 유지 정보는 앱 내에서 기억해야 하므로 Preference Manager 이용할 듯.
 */
/* 이현준 (2020.07.13)
사업자 탭이 없어졌으므로 탭 부분 삭제
*/
// TODO 패스워드 입력 텍스트를 rightDrawable 대신 Toggle로 해야지 InputMethodManager 자원 해제 가능
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "*************";

    private APIInterface apiInterface;

    Activity activity;

    View toolbarView;

    TextView missingPasswordTextView, loginErrorHintTextView, toolbarTextView;
    EditText idEditText, passwordEditText;
    Button loginButton;
    ImageView backButton;
    ProgressBar progressBar;

    TextWatcher passwordTextWatcher;

    Animation animAppearHint;

    boolean isFirstExecuted = true, isBackPressed, isPasswordVisible;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        activity = this;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initFindViewById() {
        firebaseAuth = FirebaseAuth.getInstance();

        apiInterface = APIClient.getClient().create(APIInterface.class);

        passwordTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String inputPassword = passwordEditText.getText().toString();
                if (RegularCheck.isRegularPassword(inputPassword)) loginButton.setEnabled(true);
                else loginButton.setEnabled(false);
            }
        };

        progressBar = findViewById(R.id.customDialog_progressBar);

        toolbarView = findViewById(R.id.login_toolbar);

        toolbarTextView = toolbarView.findViewById(R.id.toolbar_title);
        toolbarTextView.setText(getString(R.string.activity_login_login_button));

        backButton = toolbarView.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.setEnabled(false);
                loginButton.setText("");
                progressBar.setVisibility(View.VISIBLE);

                String inputID = idEditText.getText().toString();
                String inputPassword = passwordEditText.getText().toString();

                login(AES256Util.aesEncode(inputID), HashMsgUtil.getSHA256(inputPassword));
            }
        });

        missingPasswordTextView = findViewById(R.id.missing_password_text);
        loginErrorHintTextView = findViewById(R.id.id_password_error_hint);

        animAppearHint = AnimationUtils.loadAnimation(this, R.anim.anim_appear_hint_text);
        animAppearHint.setInterpolator(AnimationUtils.loadInterpolator(this, android.R.anim.decelerate_interpolator));

        passwordEditText = findViewById(R.id.user_password_edittext);
        passwordEditText.addTextChangedListener(passwordTextWatcher);
        passwordEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int RIGHT = 2;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (passwordEditText.getRight() - passwordEditText.getCompoundDrawables()[RIGHT].getBounds().width())) {
                        int selection = passwordEditText.getSelectionEnd();
                        if (isPasswordVisible) {
                            // set drawable image
                            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_icon_visibility_off, 0);
                            // hide Password
                            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            isPasswordVisible = false;
                        } else {
                            // set drawable image
                            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_icon_visibility_on, 0);
                            // show Password
                            passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            isPasswordVisible = true;
                        }
                        passwordEditText.setSelection(selection);
                        return true;
                    }
                }
                return false;
            }
        });
        idEditText = findViewById(R.id.user_id_edittext);
        idEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) loginErrorHintTextView.setText("");
            }
        });
    }

    private void releaseResource() {
        activity = null;
        apiInterface = null;

        firebaseAuth = null;

        progressBar = null;

        toolbarView = null;

        missingPasswordTextView = null;
        loginErrorHintTextView = null;
        toolbarTextView = null;
        idEditText = null;
        passwordEditText = null;
        loginButton = null;
        backButton = null;

        passwordTextWatcher = null;

        animAppearHint = null;
    }

    @SuppressWarnings("NullableProblems")
    private void login(String userHashID, String userHashPw) {
        Call<MemberResponse> loginCall = apiInterface.login(userHashID, userHashPw);
        loginCall.enqueue(new Callback<MemberResponse>() {
            @Override
            public void onResponse(Call<MemberResponse> call, Response<MemberResponse> response) {
                MemberResponse memberResponse = response.body();
                if (memberResponse != null &&
                        memberResponse.getResponseCode() == 500) {
                    signInWithCustomToken(memberResponse.getFirebaseToken(), memberResponse.getUserToken());
                } else {
                    loginErrorHintTextView.setText(getString(R.string.activity_login_error_input));
                    loginButton.setEnabled(true);
                    loginButton.setText(getString(R.string.activity_login_login_button));
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<MemberResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    // 토큰을 사용한 인증
    private void signInWithCustomToken(String firebaseToken, String userToken) {
        firebaseAuth.signInWithCustomToken(firebaseToken)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            PreferenceManager.setString(getApplicationContext(), "userToken", userToken);
                            Intent toMainIntent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(toMainIntent);
                            loginButton.setEnabled(true);
                            loginButton.setText(getString(R.string.activity_login_login_button));
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("#############", Objects.requireNonNull(e.getMessage()));
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isFirstExecuted) {
            initFindViewById();
            isFirstExecuted = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBackPressed) {
            releaseResource();
            isBackPressed = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseResource();
        Debug.stopMethodTracing();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isBackPressed = true;
    }
}












/*    *//**
 * 탭으로 구성할 Fragments 리스트View Pager -> ux.viewpager.viewPagerAdapter class상단 탭 바에 나타낼 Title 적용
 *//*
    public void setFragments(){
        fragments[0] = new ConsumerFragment();
        fragments[1] = new ProducerFragment();
    }

    *//**View Pager -> ux.viewpager.viewPagerAdapter class*//*
    public void setViewPager(){
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        Lifecycle lifecycle = this.getLifecycle();
        viewPagerAdapter = new viewPagerAdapter(fragmentManager,lifecycle,fragments);
        viewpager = findViewById(R.id.vp_login);
        viewpager.setAdapter(viewPagerAdapter);
    }

    *//**상단 탭 바에 나타낼 Title 적용*//*
    public void setTabLayout(){
        String[] names = new String[]{"개인","사업자"};
        tabLayout = findViewById(R.id.tl_login);
        new TabLayoutMediator(tabLayout, viewpager,
                (tab, position) -> tab.setText(names[position])
        ).attach();
    }*/