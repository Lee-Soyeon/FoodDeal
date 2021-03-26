package com.hankki.fooddeal.ui.home.community;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hankki.fooddeal.R;
import com.hankki.fooddeal.amazon.AmazonS3Util;
import com.hankki.fooddeal.data.CommentItem;
import com.hankki.fooddeal.data.PostItem;
import com.hankki.fooddeal.data.retrofit.BoardController;
import com.hankki.fooddeal.data.security.AES256Util;
import com.hankki.fooddeal.data.security.HashMsgUtil;
import com.hankki.fooddeal.ui.MainActivity;
import com.hankki.fooddeal.ui.chatting.ChatActivity;
import com.hankki.fooddeal.ui.chatting.chatDTO.ChatRoomModel;
import com.hankki.fooddeal.ux.recyclerview.CommentAdapter;
import com.hankki.fooddeal.ux.viewpager.GalleryAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class Community_detail extends AppCompatActivity implements OnMapReadyCallback {
    ViewPager vp_image; // 이미지 뷰페이저
    TabLayout tl_dots;
    GalleryAdapter galleryAdapter; // 뷰페이저 어댑터
    View topToolbar, myPostBottomToolbar, myCommentBottomToolbar;
    ConstraintLayout post_common, bottomToolbar; // 게시글 몸통, 하단 툴바(채팅 및 댓글창)
    ImageView profile; // 유저 프로필
    TextView userLocation, mapLocation; // 유저 위치, 게시글 위치(교환/나눔)
    TextView userId, postInfo, postText, postLike, postTitle; //아이디, 게시글 정보(시간, 장소 등), 관심도(찜, 좋아요)
    RecyclerView rv_comment; // 댓글 리사이클러 뷰
    Button btn_comment;
    EditText et_comment;
    ArrayList<CommentItem> commentItems; // 댓글 리스트
    PostItem mPost;

    ImageView iv_setting, iv_dot;
    LinearLayout ll_revise, ll_delete;
    FrameLayout fl_bottom;

    CommentAdapter mAdapter;
    Context mContext;
    View trickView;
    NestedScrollView scrollView;

    ArrayList<Bitmap> postImages;

    String uid;
    int order;
    int page; // 교환나눔, 레시피, 자유
    String tag;
    boolean isMyPage = false; // 내 게시글이면 대댓글 처리 따로

    CameraUpdate cameraUpdate;
    GoogleMap mapPost;

    Disposable disposable;

    ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mContext = this;

        postImages = new ArrayList<>();

        if (getIntent() != null) {
            Intent intent = getIntent();
            page = intent.getIntExtra("page", -1); //교환나눔, 레시피, 자유
            order = intent.getIntExtra("index", -1); //몇 번째 게시글인가?
            tag = intent.getStringExtra("Tag");
            mPost = intent.getParcelableExtra("item");
        }
        try {
            FirebaseAuth.getInstance().getCurrentUser().getUid();
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } catch (Exception e) {
            uid = "";
        }

        switch (page) {
            case 0:
                setContentView(R.layout.post_exchange_share);
                setPostCommon();
                setExchangeSharePostDetail();

                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map_post);
                mapFragment.getMapAsync(this);
                break;
            case 1:
            case 2:
            default:
                setContentView(R.layout.post_recipe_free);
                setPostCommon();
                setRecipeFreePostDetail();
                break;
        }

        if (mPost.getUserHashId().equals(uid)) {
            setMyPostBottomToolbar();
            isMyPage = true;
        }
        if (uid.equals(""))
            setGuestBottomToolbarOption();
    }

    @SuppressLint("SetTextI18n")
    public void setExchangeSharePostDetail() {
        /*지도 설정*/
        mapLocation = findViewById(R.id.tv_post_loc);
        String myLocation = mPost.getRegionFirst() + " " + mPost.getRegionSecond() + " " + mPost.getRegionThird();
        mapLocation.setText(myLocation);
        String category;
        switch (mPost.getCategory()) {
            case "INGREDIENT EXCHANGE":
                category = "식재교환";
                break;
            case "INGREDIENT SHARE":
                category = "식재나눔";
                break;
            default:
                category = mPost.getCategory();
        }
        postInfo.setText(category + " ･ " + mPost.getRelativeTime());

        Button btn_chat = bottomToolbar.findViewById(R.id.btn_chatting);
        btn_chat.setOnClickListener(v -> {
            // 1대1 채팅방 생성
            ArrayList<String> roomUserList = new ArrayList<>();
            roomUserList.add(AES256Util.aesDecode(uid));
            roomUserList.add(AES256Util.aesDecode(mPost.getUserHashId()));

            HashMap<String, Integer> unreadUserCountMap = new HashMap<>();
            unreadUserCountMap.put(AES256Util.aesDecode(uid), 0);
            unreadUserCountMap.put(AES256Util.aesDecode(mPost.getUserHashId()), 0);

            // id에 글 등록 시간과 유저리스트가 포함되어 있기 때문에 나중에 해당 게시글의 채팅 참여하기 버튼으로 해당 채팅방 접근 가능
            String newRoomTitle = mPost.getBoardTitle();
            String roomId = HashMsgUtil.getSHARoomID(mPost.getInsertDate(), newRoomTitle);

            DocumentReference docRef = FirebaseFirestore.getInstance().collection("rooms").document(roomId);
            docRef
                    .get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            // 이미 있는 채팅방은 바로 참가
                            if(documentSnapshot.exists()) {
                                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                                intent.putExtra("roomID", roomId);
                                intent.putExtra("roomTitle", newRoomTitle);
//                                intent.putExtra("userTotal", 2);
                                intent.putStringArrayListExtra("userList", roomUserList);
//                                intent.putExtra("otherUID", AES256Util.aesDecode(mPost.getUserHashId()));
                                startActivity(intent);
                            }
                            // 없는 채팅방이면 생성 후 참가
                            else {
                                createChattingRoom(docRef, roomId, newRoomTitle, roomUserList, unreadUserCountMap);
                            }
                        } else {
                            Log.d("##########", "채팅방 생성 에러");
                        }
                    });
        });
    }

    private void createChattingRoom(final DocumentReference room, String roomID, String roomTitle, ArrayList<String> userList, HashMap<String, Integer> unreadUserCountMap) {
        // 첫 방 생성할때는 메시지가 없으므로 타임만 서버에 채팅방이 등록되는 시간으로 설정, unreadUserCountMap들의 값들도 0
        ChatRoomModel chatRoomModel = new ChatRoomModel(roomID, 3, roomTitle, userList, unreadUserCountMap, null, new Date(System.currentTimeMillis()));

        room
                .set(chatRoomModel)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        // 채팅방 참여
                        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                        intent.putExtra("roomID", roomID);
                        intent.putExtra("roomTitle", roomTitle);
//                        intent.putExtra("userTotal", 2);
                        intent.putStringArrayListExtra("userList", userList);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show());
    }

    public void setRecipeFreePostDetail() {
        /*댓글 설정*/
        rv_comment = findViewById(R.id.rv_comment);
        postInfo.setText(mPost.getRelativeTime());
        scrollView = findViewById(R.id.scroll);
        rv_comment.setNestedScrollingEnabled(false);

        et_comment = bottomToolbar.findViewById(R.id.et_comment);
        btn_comment = bottomToolbar.findViewById(R.id.btn_comment);
        defaultWriteComment();
        setCommentList();

        scrollView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                v.postDelayed(() -> {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    et_comment.requestFocus();
                }, 100);
            }
        });
    }

    public void defaultWriteComment() {
        btn_comment.setOnClickListener(v -> {
            String comment = et_comment.getText().toString();
            if (comment.equals(""))
                return;

            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(et_comment.getWindowToken(), 0);

            CommentItem item = new CommentItem();
            item.setBoardSeq(mPost.getBoardSeq());
            item.setCommentContent(comment);
            item.setInsertDate(BoardController.getTime());

            if (BoardController.commentWrite(mContext, item)) {
                setCommentList();
                et_comment.setText(null);
            } else {
                Toast.makeText(mContext, "실패!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void writeChildComment(CommentItem parent) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        assert imm != null;
        et_comment.requestFocus();

        btn_comment.setOnClickListener(v -> {
            String comment = et_comment.getText().toString();
            if (comment.equals(""))
                return;
            imm.hideSoftInputFromWindow(et_comment.getWindowToken(), 0);

            CommentItem item = new CommentItem();
            item.setBoardSeq(mPost.getBoardSeq());
            item.setCommentContent(comment);
            item.setInsertDate(BoardController.getTime());
            item.setParentCommentSeq(parent.getCommentSeq());

            if (BoardController.childCommentWrite(mContext, parent, item)) {
                setCommentList();
                et_comment.setText(null);
            } else {
                Toast.makeText(mContext, "실패!", Toast.LENGTH_SHORT).show();
            }
            defaultWriteComment();
        });
    }

    @SuppressLint("SetTextI18n")
    public void setPostCommon() {
        progressBar = findViewById(R.id.customDialog_progressBar);
        vp_image = findViewById(R.id.vp_image);
        vp_image.setVisibility(View.GONE);
        trickView = findViewById(R.id.trick);
        trickView.getLayoutParams().height = 100;
        tl_dots = findViewById(R.id.tl_dots);

        topToolbar = findViewById(R.id.top_toolbar);
        bottomToolbar = findViewById(R.id.bottom_toolbar);
        CheckBox iv_like = bottomToolbar.findViewById(R.id.iv_like);
        /*내가 이미 찜한 게시글이면, iv_like 는 체크 상태 코드 추가
          if (mPost.isLiked == true) { iv_like.setChecked(true) }*/
        if (BoardController.isLikedBoard(mContext, mPost)) {
            iv_like.setChecked(true);
        }

        iv_like.setOnClickListener(v -> {
            /*유저가 기존에 찜한 게시글이 아닐 경우*/
            if (!(iv_like.isChecked())) {
                if (BoardController.boardLikeMinus(mContext, mPost)) {
                    iv_like.setChecked(false);
                    Toast.makeText(mContext, "게시글 찜을 취소했습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, "실패!", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (BoardController.boardLikePlus(mContext, mPost)) {
                    iv_like.setChecked(true);
                    Toast.makeText(mContext, "이 게시글을 찜했습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, "실패!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        post_common = findViewById(R.id.post_common);

        profile = post_common.findViewById(R.id.iv_user_profile);
        profile.setBackground(new ShapeDrawable(new OvalShape()));
        profile.setImageResource(R.drawable.ic_group_rec_60dp);
        profile.setClipToOutline(true);
        profile.setScaleType(ImageView.ScaleType.CENTER_CROP);

//        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users")
//                .document(mPost.getUserHashId());
//        documentReference
//                .get()
//                .addOnCompleteListener(task -> {
//                    DocumentSnapshot snapshot = task.getResult();
//                    if(!snapshot.get("userPhotoUri").equals("")) {
//
//                        Glide
//                                .with(mContext)
//                                .load(snapshot.get("userPhotoUri"))
//                                .into(profile);
//                    }
//                });
        String userUID = mPost.getUserHashId();
        Glide.with(mContext).load(AmazonS3Util.s3.getUrl("hankki-s3","profile/"+userUID).toString())
                .error(Glide.with(mContext).load(R.drawable.ic_group_60dp))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(profile);

        userId = post_common.findViewById(R.id.tv_user_id);
        userId.setText(AES256Util.aesDecode(userUID));

        userLocation = post_common.findViewById(R.id.tv_user_location);
        userLocation.setText(mPost.getRegionSecond() + " " + mPost.getRegionFirst());

        postTitle = post_common.findViewById(R.id.tv_post_title);
        postTitle.setText(mPost.getBoardTitle());
        postInfo = post_common.findViewById(R.id.tv_post_info);
        postText = post_common.findViewById(R.id.tv_post);
        postText.setText(mPost.getBoardContent());

        postLike = post_common.findViewById(R.id.tv_post_like);
        if (mPost.getLikeCount() > 0) {
            postLike.setText(String.valueOf(mPost.getLikeCount()) + " 명이 찜했어요");
        }

//        setBroadPostImages(mPost.getInsertDate());
        setImageViewPager();
    }

    /*조찬아 @TODO 수정 모드일 때 Null pointer 오류 수정할 것*/
    private void setBroadPostImages(String date) {
//        customAnimationDialog.show();
//        progressBar.setVisibility(View.VISIBLE);
        disposable = Observable.fromCallable((Callable<Object>) () -> {
            for(int i=0;i<4;i++) {
                StorageReference downloadImageRef = FirebaseStorage.getInstance().getReference().child("PostPhotos/" + date + "/" + Integer.toString(i) + ".jpg");

                final long MAX_SIZE = 1024 * 1024 * 15;
                Task<byte[]> task = downloadImageRef.getBytes(MAX_SIZE);
                try {
                    byte[] imageBytes = Tasks.await(task);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    postImages.add(bitmap);
                } catch (Exception e) {
                    Log.e("#########", e.toString());
                }
            }

            return false;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    setImageViewPager();
                });
    }

    public void setMyPostBottomToolbar() {
        fl_bottom = findViewById(R.id.fl_bottom);
        fl_bottom.removeView(bottomToolbar);
        myPostBottomToolbar = View.inflate(this, R.layout.bottom_mypost, null);
        ll_revise = myPostBottomToolbar.findViewById(R.id.ll_revise);
        ll_delete = myPostBottomToolbar.findViewById(R.id.ll_delete);
        iv_dot = myPostBottomToolbar.findViewById(R.id.iv_dot);

        myCommentBottomToolbar = View.inflate(this, R.layout.bottom_my_comment, null);
        iv_setting = myCommentBottomToolbar.findViewById(R.id.iv_setting);
        et_comment = myCommentBottomToolbar.findViewById(R.id.et_comment);
        btn_comment = myCommentBottomToolbar.findViewById(R.id.btn_comment);

        if (page != 0) { // 레시피 자유
            fl_bottom.addView(myCommentBottomToolbar);
            postSetting();
            defaultWriteComment();
            scrollView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                if (bottom < oldBottom) {
                    v.postDelayed(() -> {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        et_comment.requestFocus();
                    }, 100);
                }
            });
        } else {
            fl_bottom.addView(myPostBottomToolbar);
            postRevise();
            postDelete();
        }
    }

    public void postSetting() {
        iv_setting.setOnClickListener(v -> {
            fl_bottom.removeView(myCommentBottomToolbar);
            fl_bottom.addView(myPostBottomToolbar);
        });
        iv_dot.setOnClickListener(v1 -> {
            fl_bottom.removeView(myPostBottomToolbar);
            fl_bottom.addView(myCommentBottomToolbar);
            postSetting();
            defaultWriteComment();
        });
        postRevise();
        postDelete();
    }

    public void postRevise() {
        ll_revise.setOnClickListener(v -> {
            Intent reviseIntent = new Intent(mContext, PostActivity.class);
            reviseIntent.putExtra("mode", "revise");
            reviseIntent.putExtra("page", page);
            reviseIntent.putExtra("item", mPost);
            startActivity(reviseIntent);
            finish();
        });
    }

    public void deletePhotoImage(Context mContext, String insertDate) {
        disposable = Observable.fromCallable((Callable<Object>) () -> {
            for(int i=0;i<postImages.size();i++) {
                StorageReference deleteImageRef = FirebaseStorage.getInstance().getReference().child("PostPhotos/" + insertDate + "/" + Integer.toString(i) + ".jpg");
                deleteImageRef
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            })
                        .addOnFailureListener(e -> {
                            Log.e("###########", e.toString(), e);
                        });
            }
            return false;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    refreshFragmentAndFinish();
                    disposable.dispose();
                });
    }

    public void deleteDownloadUrl(Context mContext, String insertDate) {
        if(postImages != null && postImages.size() > 0) {
            FirebaseFirestore
                    .getInstance()
                    .collection("postPhotos")
                    .document(insertDate)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        deletePhotoImage(mContext, insertDate);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("###########", e.toString(), e);
                    });
        } else  {
            Log.i("#########", "이미지를 가지고 있지 않습니다");
            refreshFragmentAndFinish();
        }
    }

    /*
    이현준
    TODO AWS 서버에 있는 게시글 데이터 삭제와 Firebase에 있는 사진 및 DownloadUrl도 같이 삭제하는 코드 필요
    */
    public void postDelete() {
        ll_delete.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage("글을 삭제 하시겠습니까?");
            builder.setPositiveButton("확인", (dialog, which) -> {

                if (BoardController.boardDelete(mContext, mPost)) {
//                    deleteDownloadUrl(mContext, mPost.getInsertDate());
                    try {
                        AmazonS3Util.deleteImageOfServer(mContext,mPost);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(mContext, "S3 데이터 삭제 실패", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                } else {
                    Toast.makeText(mContext, "AWS 데이터 삭제 실패", Toast.LENGTH_SHORT).show();
                }

            }).setNegativeButton("취소", (dialog, which) -> {});
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });
    }

    public void refreshFragmentAndFinish() {
        if (tag.equals("Main")) {
            /*게시글 리스트로 돌아갈 경우 변경사항 즉각 Update*/
            NavHostFragment navHostFragment = (NavHostFragment) ((MainActivity) MainActivity.mainContext)
                    .getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            assert navHostFragment != null;
            List<Fragment> fragments = navHostFragment.getChildFragmentManager().getFragments().get(0)
                    .getChildFragmentManager().getFragments();

            Fragment fragment = fragments.get(page);
            switch (page) {
                case 0:
                    ((ExchangeAndShare) fragment).updatePostItems();
                    ((ExchangeAndShare) fragment).setRecyclerView();
                    break;
                case 1:
                    ((RecipeShare) fragment).updatePostItems();
                    ((RecipeShare) fragment).setRecyclerView();
                    break;
                case 2:
                    ((FreeCommunity) fragment).updatePostItems();
                    ((FreeCommunity) fragment).setRecyclerView();
                    break;
            }
        }
        finish();
    }

    public void setGuestBottomToolbarOption() {
        FrameLayout fl_bottom = findViewById(R.id.fl_bottom);
        fl_bottom.removeView(bottomToolbar);
    }

    public void setImageViewPager() {
        if(mPost.getImgCount() > 0) {
            if(mPost.getImgCount() == 1){
                TabLayout dots = findViewById(R.id.tl_dots);
                tl_dots.setVisibility(View.INVISIBLE);
            }
            vp_image.setVisibility(View.VISIBLE);
            trickView.setVisibility(View.GONE);
            galleryAdapter = new GalleryAdapter(this, mPost);
            tl_dots.setupWithViewPager(vp_image, true);
            vp_image.setAdapter(galleryAdapter);
        }
    }

    public void setCommentList() {
        commentItems = BoardController.getBoardCommentList(mPost);
        mAdapter = new CommentAdapter(commentItems);
        mAdapter.setChildCommentList();
        mAdapter.setContext(mContext);
        mAdapter.setIsMyPage(isMyPage);
        rv_comment.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        rv_comment.setAdapter(mAdapter);
    }

    /* 이소연
    TODO 게시글 상세 페이지에서 카메라가 줌 된 위치 이외에 다른 곳을 보려고 드래그하면 잘 되지 않는 이슈
    */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        /**지도 설정*/
        mapPost = googleMap;

        double latitude = Double.parseDouble(mPost.getUserLatitude());
        double longitude = Double.parseDouble(mPost.getUserLongitude());

        LatLng latlng = new LatLng(latitude, longitude);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latlng);

        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_icon_home));
        markerOptions.anchor((float)0.5,(float)0.5);

        mapPost.addMarker(markerOptions);

        CircleOptions circle1KM = new CircleOptions().center(latlng) //원점
                .radius(100)      //반지름 단위 : m
                .strokeWidth(0f)  //선너비 0f : 선없음
                .fillColor(Color.parseColor("#88ffb5c5")); //배경색
        mapPost.addCircle(circle1KM);


        cameraUpdate = CameraUpdateFactory.newLatLng(latlng);
        mapPost.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 17));

        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setAllGesturesEnabled(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        postImages = null;
        disposable = null;
    }
}
