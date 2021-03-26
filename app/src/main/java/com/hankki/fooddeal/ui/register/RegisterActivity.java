package com.hankki.fooddeal.ui.register;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Debug;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hankki.fooddeal.R;
import com.hankki.fooddeal.data.PreferenceManager;
import com.hankki.fooddeal.data.RegularCheck;
import com.hankki.fooddeal.data.retrofit.APIClient;
import com.hankki.fooddeal.data.retrofit.APIInterface;
import com.hankki.fooddeal.data.retrofit.retrofitDTO.MemberResponse;
import com.hankki.fooddeal.data.security.AES256Util;
import com.hankki.fooddeal.data.security.HashMsgUtil;
import com.hankki.fooddeal.ui.login.LoginActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** 회원가입 회면
 *  소비자용, 사업자용 버튼 이용해서 선택*/
// TODO 패스워드 입력 텍스트를 rightDrawable 대신 Toggle로 해야지 InputMethodManager 자원 해제 가능
public class RegisterActivity extends AppCompatActivity {

    private static final int SEARCH_ADDRESS_ACTIVITY = 10000;

    private APIInterface apiInterface;
    private APIInterface kakaoApiInterface;

    String phoneNo, userID, userPassword, userEmail;

    Animation animAppearHint;

    View toolbarView;

    TextView idHintTextView, passwordHintTextView, emailHintTextView, toolbarTextView;
    EditText idEditText, passwordEditText, emailEditText, registerAddressEditText_1, registerAddressEditText_2;
    ImageView backButton;
    Button dupIDCheckButton, postButton;
    ImageView registerAddressButton;

    TextWatcher passwordTextWatcher, emailTextWatcher;

    boolean isFirstExecuted = true, isBackPressed, isPasswordVisible, isNewID, isRegularPassword, isRegularEmail;

    Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        kakaoApiInterface = APIClient.getKakaoClient().create(APIInterface.class);

        // 암호화 된 전화번호가 넘어왔음
        if(getIntent() != null) phoneNo = getIntent().getStringExtra("phoneNo");
    }

    // 자원할당 및 이벤트 설정
    @SuppressLint("ClickableViewAccessibility")
    private void initFindViewById() {
        apiInterface = APIClient.getClient().create(APIInterface.class);

        // 패스워드의 정규성을 실시간으로 파악
        passwordTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String inputPassword = passwordEditText.getText().toString();
                if(RegularCheck.isRegularPassword(inputPassword)) {
                    passwordHintTextView.setTextColor(Color.BLUE);
                    passwordHintTextView.setText("올바른 비밀번호 형식입니다");
                    isRegularPassword = true;
                    userPassword = HashMsgUtil.getSHA256(inputPassword);
                }
                else {
                    passwordHintTextView.setTextColor(Color.RED);
                    passwordHintTextView.setText("올바르지 않은 비밀번호 형식입니다");
                    isRegularPassword = false;
                }
            }
        };
        // 이메일의 정규성을 실시간으로 파악
        emailTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String inputEmail = emailEditText.getText().toString();
                if(RegularCheck.isRegularEmail(inputEmail)) {
                    emailHintTextView.setTextColor(Color.BLUE);
                    emailHintTextView.setText("올바른 이메일 형식입니다");
                    isRegularEmail = true;
                    userEmail = inputEmail;
                }
                else {
                    emailHintTextView.setTextColor(Color.RED);
                    emailHintTextView.setText("올바르지 않은 이메일 형식입니다");
                    isRegularEmail = false;
                }
            }
        };

        toolbarView = findViewById(R.id.login_toolbar);

        toolbarTextView = toolbarView.findViewById(R.id.toolbar_title);
        toolbarTextView.setText(getString(R.string.activity_register_toolbar));

        idHintTextView = findViewById(R.id.register_id_dup_hint_textView);
        passwordHintTextView = findViewById(R.id.register_password_hint_textView);
        emailHintTextView = findViewById(R.id.register_email_hint_textView);

        // 힌트 텍스트 애니메이션
        animAppearHint = AnimationUtils.loadAnimation(this, R.anim.anim_appear_hint_text);
        animAppearHint.setInterpolator(AnimationUtils.loadInterpolator(this, android.R.anim.decelerate_interpolator));

        // 툴바 이미지 클릭 이벤트
        backButton = toolbarView.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onBackPressed(); }
        });

        dupIDCheckButton = findViewById(R.id.register_dup_check_button);
        dupIDCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputID = idEditText.getText().toString();
                checkDupID(inputID);
            }
        });

        postButton = findViewById(R.id.register_post_button);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNewID && isRegularEmail && isRegularPassword) { register(phoneNo, userID, userPassword, userEmail); }
                else { Toast.makeText(getApplicationContext(), "입력값을 다시 한번 확인해주세요", Toast.LENGTH_SHORT).show(); }
            }
        });

        idEditText = findViewById(R.id.register_id_editText);
        idEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    idHintTextView.setTextColor(getResources().getColor(R.color.original_black));
                    idHintTextView.setText(getString(R.string.activity_register_id_hint));
                    idHintTextView.startAnimation(animAppearHint);
                } else { idHintTextView.setText(""); }
            }
        });

        passwordEditText = findViewById(R.id.register_password_editText);
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
                        } else  {
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
        passwordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    passwordHintTextView.setTextColor(getResources().getColor(R.color.original_black));
                    passwordHintTextView.setText(getString(R.string.activity_register_password_hint));
                    passwordHintTextView.startAnimation(animAppearHint);
                } else { passwordHintTextView.setText(""); }
            }
        });

        emailEditText = findViewById(R.id.register_email_editText);
        emailEditText.addTextChangedListener(emailTextWatcher);
        emailEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    emailHintTextView.setTextColor(getColor(R.color.original_black));
                    emailHintTextView.setText(getString(R.string.activity_register_email_hint));
                    emailHintTextView.startAnimation(animAppearHint);
                } else { emailHintTextView.setText(""); }
            }
        });

        registerAddressButton = findViewById(R.id.register_address_button);
        registerAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, WebViewActivity.class);
                startActivityForResult(intent, SEARCH_ADDRESS_ACTIVITY);
            }
        });

        registerAddressEditText_1 = findViewById(R.id.register_address_editText_1);
        registerAddressEditText_2 = findViewById(R.id.register_address_editText_2);
    }

    // 자원할당 해제
    @SuppressLint("ClickableViewAccessibility")
    private void releaseResource() {
        apiInterface = null;

        animAppearHint = null;

        toolbarView = null;

        idHintTextView = null;
        passwordHintTextView = null;
        emailHintTextView = null;

        idEditText = null;
        emailEditText = null;
        passwordEditText = null;

        dupIDCheckButton = null;
        postButton = null;

        emailTextWatcher = null;
        passwordTextWatcher = null;

        backButton = null;
    }

    @SuppressWarnings("NullableProblems")
    private void register(String phone, String id, String password, String email) {
        HashMap<String, String> body = new HashMap<>();
        body.put("USER_PHONE", phone);
        body.put("USER_HASH_ID", id);
        body.put("USER_HASH_PW", password);
        body.put("USER_EMAIL", email);

        Call<MemberResponse> registerCall = apiInterface.register(body);
        registerCall.enqueue(new Callback<MemberResponse>() {
            @Override
            public void onResponse(Call<MemberResponse> call, Response<MemberResponse> response) {
                MemberResponse memberResponse = response.body();
                if (memberResponse != null &&
                        memberResponse.getResponseCode() == 600) {
                    setUserProfiles(id);

                    body.clear();
                } else { Toast.makeText(getApplicationContext(), "서버와의 연결이 불안정합니다", Toast.LENGTH_SHORT).show(); }
            }

            @Override
            public void onFailure(Call<MemberResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @SuppressWarnings("NullableProblems")
    private void checkDupID(String userId) {
        HashMap<String, String> body = new HashMap<>();
        body.put("USER_HASH_ID", AES256Util.aesEncode(userId));

        Call<MemberResponse> checkDupIDCall = apiInterface.checkDupID(body);
        checkDupIDCall.enqueue(new Callback<MemberResponse>() {
            @Override
            public void onResponse(Call<MemberResponse> call, Response<MemberResponse> response) {
                MemberResponse memberResponse = response.body();
                if (memberResponse != null &&
                        memberResponse.getResponseCode() == 605) {
                    idHintTextView.setTextColor(Color.BLUE);
                    idHintTextView.setText("사용가능한 아이디입니다");
                    isNewID = true;
                    userID = AES256Util.aesEncode(userId);

                    body.clear();
                } else {
                    idHintTextView.setTextColor(Color.RED);
                    idHintTextView.setText("이미 존재하는 아이디입니다");
                }
            }

            @Override
            public void onFailure(Call<MemberResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void setUserProfiles(String uid) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        final HashMap<String, Object> userProfileMap = new HashMap<>();
        userProfileMap.put("userNickname", "");
        userProfileMap.put("userPhotoUri", "");

        firebaseFirestore
                .collection("users")
                .document(uid)
                .set(userProfileMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent toMainIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(toMainIntent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void searchLatLngFromAddress(String address) {
        disposable = Observable.fromCallable(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Call<ResponseBody> addressSearchCall = kakaoApiInterface.getAddress(address);
                try {
                    ResponseBody responseBody = addressSearchCall.execute().body();

                    if(responseBody != null) {
                        JSONObject jsonObject = new JSONObject(responseBody.string());
                        JSONArray jsonArray = jsonObject.getJSONArray("documents");

                        jsonObject = jsonArray.getJSONObject(0);
                        String longitude = jsonObject.getJSONObject("address").getString("x");
                        String latitude = jsonObject.getJSONObject("address").getString("y");

                        PreferenceManager.setString(getApplicationContext(), "Latitude", AES256Util.aesEncode(latitude));
                        PreferenceManager.setString(getApplicationContext(), "Longitude", AES256Util.aesEncode(longitude));
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

                    }
                });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent){

        super.onActivityResult(requestCode, resultCode, intent);

        switch(requestCode){

            case SEARCH_ADDRESS_ACTIVITY:

                if(resultCode == RESULT_OK){

                    String data = intent.getExtras().getString("data");
                    String[] address = data.split(",");
                    String[] region = address[1].split(" ");

                    PreferenceManager.setString(this, "address", address[1]);
                    PreferenceManager.setString(this, "region1Depth", region[1]);
                    PreferenceManager.setString(this, "region2Depth", region[2]);
                    PreferenceManager.setString(this, "region3Depth", region[5]);

                    searchLatLngFromAddress(address[1]);

                    if (data != null) {
                        // 우편번호
                        registerAddressEditText_1.setText(address[0]);
                        // 상세 주소
                        registerAddressEditText_2.setText(address[1]);
                    }

                }
                break;

        }

    }

    // 자원 할당
    @Override
    protected void onStart() {
        super.onStart();
        if(isFirstExecuted) {
            initFindViewById();
            isFirstExecuted = false;
        }
    }

    // 자원 할당 해제
    @Override
    protected void onStop() {
        super.onStop();
        if(isBackPressed) {
            releaseResource();
            isBackPressed = false;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isBackPressed = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseResource();
        Debug.stopMethodTracing();
    }
}
