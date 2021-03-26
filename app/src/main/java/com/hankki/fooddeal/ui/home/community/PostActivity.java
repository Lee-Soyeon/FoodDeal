package com.hankki.fooddeal.ui.home.community;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.hankki.fooddeal.R;
import com.hankki.fooddeal.amazon.AmazonS3Util;
import com.hankki.fooddeal.data.PostItem;
import com.hankki.fooddeal.data.PreferenceManager;
import com.hankki.fooddeal.data.retrofit.BoardController;
import com.hankki.fooddeal.image.ImageUtil;
import com.hankki.fooddeal.ui.MainActivity;
import com.hankki.fooddeal.ux.dialog.CustomDialog;
import com.hankki.fooddeal.ux.dialog.CustomPostImageDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


/**게시글 쓰기 액티비티*/
public class PostActivity extends AppCompatActivity {
    EditText et_title;
    EditText et_post;
    LinearLayout select_location,ll_images, ll_choice, ll_post;
    Button btn_write;
    Intent intent;
    TextView select_exchange, select_share, toolbarTextView;
    View toolbarView;
    ImageView backButton;
    int page, order;
    String pageFromTag;

    CustomDialog customDialog;

    String category = ""; // 테스트용/ 교환인지 나눔인지
    String mode = ""; // 수정인지 글쓰기인지


    ArrayList<Bitmap> postImages = new ArrayList<>();
    int[] imageResources = new int[]{R.id.image_1,R.id.image_2,R.id.image_3,R.id.image_4};
    ImageView[] imageViews = new ImageView[4];
    ImageView clear0, clear1, clear2, clear3;
    ImageView[] clearImages = new ImageView[4];

    Context mContext;


    //테스트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_post);
        intent = getIntent();
        page = intent.getIntExtra("page",-1);
        order = intent.getIntExtra("index",-1);
        category = intent.getStringExtra("category");
        mode = intent.getStringExtra("mode");

        setIdComponents();
    }

    public void setIdComponents() {
        ll_post = findViewById(R.id.ll_post);
        et_title = findViewById(R.id.et_post_title);
        et_post = findViewById(R.id.et_post_post);
        select_exchange = findViewById(R.id.select_exchange);
        select_share = findViewById(R.id.select_share);
        select_location = findViewById(R.id.select_location);
        ll_images = findViewById(R.id.ll_images);
        ll_choice = findViewById(R.id.ll_choice);
        toolbarView = findViewById(R.id.post_toolbar);
        toolbarTextView = toolbarView.findViewById(R.id.toolbar_title);
        clear0 = findViewById(R.id.iv_clear0);
        clear1 = findViewById(R.id.iv_clear);
        clear2 = findViewById(R.id.iv_clear2);
        clear3 = findViewById(R.id.iv_clear3);
        clearImages[0] = clear0;
        clearImages[1] = clear1;
        clearImages[2] = clear2;
        clearImages[3] = clear3;
        backButton = toolbarView.findViewById(R.id.back_button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if(page==0){
            if(!mode.equals("revise"))
                setExchangeAndShareComponents();
        } else {
            setRecipeFreeComponents();
        }

        btn_write = findViewById(R.id.btn_post_write);

        for (int i = 0; i < 4; i++) {
            imageViews[i] = findViewById(imageResources[i]);
        }

        if (mode.equals("revise")) {
            toolbarTextView.setText("수정하기");
            setPostRevise();
        } else {
            toolbarTextView.setText("글쓰기");
            setImageWrite();
            setPostWrite();
        }
    }


    public void setExchangeAndShareComponents(){
        if(category.equals("INGREDIENT EXCHANGE")){
            select_exchange.setBackgroundResource(R.drawable.textview_selector);
            select_exchange.setTextColor(getResources().getColor(R.color.original_primary));
            select_share.setBackgroundResource(R.drawable.textview_unselector);
            select_share.setTextColor(getResources().getColor(R.color.original_black));
        } else {
            select_share.setBackgroundResource(R.drawable.textview_selector);
            select_share.setTextColor(getResources().getColor(R.color.original_primary));
            select_exchange.setBackgroundResource(R.drawable.textview_unselector);
            select_exchange.setTextColor(getResources().getColor(R.color.original_black));
        }

        select_exchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_exchange.setBackgroundResource(R.drawable.textview_selector);
                select_exchange.setTextColor(getResources().getColor(R.color.original_primary));
                select_share.setBackgroundResource(R.drawable.textview_unselector);
                select_share.setTextColor(getResources().getColor(R.color.original_black));
                category = "INGREDIENT EXCHANGE";
            }
        });
        select_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_share.setBackgroundResource(R.drawable.textview_selector);
                select_share.setTextColor(getResources().getColor(R.color.original_primary));
                select_exchange.setBackgroundResource(R.drawable.textview_unselector);
                select_exchange.setTextColor(getResources().getColor(R.color.original_black));
                category = "INGREDIENT SHARE";
            }
        });
        setLocation();
    }
    public void setRecipeFreeComponents(){
        ll_post.removeView(ll_choice);
        ll_post.removeView(select_location);
    }

    public void setLocation(){
        /**위치정보 입력*/
        TextView tv_location = findViewById(R.id.tv_location);
        tv_location.setText(PreferenceManager.getString(mContext, "region3Depth"));
    }

    public void setImageWrite(){
        int image_size = postImages.size();

        if(image_size > 0){
            for(int i=0;i<image_size;i++){
                final int finalI = i;
                imageViews[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        postImages.remove(finalI);
                        onImageAttach();
                    }
                });
            }
        }

        if(image_size < 4) {
            imageViews[image_size].setOnClickListener(new View.OnClickListener() {
                @Override
                /**이미지 삽입*/
                public void onClick(View v) {
                    CustomPostImageDialog dialog = new CustomPostImageDialog(mContext);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                }
            });
        }
    }

    public void setPostWrite(){
        btn_write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_write.setVisibility(View.GONE);
                if(page==0&&(postImages.size()==0 || et_title.getText().toString().equals(""))){
                    customDialog = new CustomDialog(mContext,"사진과 제목은 필수 입력 사항입니다!");
                    customDialog.setCanceledOnTouchOutside(false);
                    customDialog.show();
                    btn_write.setVisibility(View.VISIBLE);
                } else if(et_title.getText().toString().equals("")) {
                    customDialog = new CustomDialog(mContext,"제목은 필수 입력 사항입니다!");
                    customDialog.setCanceledOnTouchOutside(false);
                    customDialog.show();
                    btn_write.setVisibility(View.VISIBLE);
                } else {
                    PostItem item = new PostItem();
                    item.setInsertDate(BoardController.getTime());
                    item.setBoardContent(et_post.getText().toString());
                    item.setBoardTitle(et_title.getText().toString());
                    item.setCategory(category);
                    item.setImgCount(postImages.size());

//                    /**테스트*/
//                    PreferenceManager.setString(mContext,"Latitude","37.4758562");
//                    PreferenceManager.setString(mContext,"Longitude","127.1482274");

                    ArrayList<File> files = new ArrayList<>();
                    if(BoardController.boardWrite(mContext,item)) {
                        if (postImages.size() > 0) {
                            for (int i = 0; i < postImages.size(); i++) {
//                            postImages.get(i).compress(Bitmap.CompressFormat.JPEG, 20, baos);
//                            byte[] data = baos.toByteArray();

//                            uploadPostPhoto(data, item.getInsertDate(), Integer.toString(i), postImages.size());

                                File file = ImageUtil.saveBitmapToJpeg(getApplicationContext(), postImages.get(i), "test" + i);
                                files.add(file);
                            }

                            try {
                                AmazonS3Util.uploadImageToServer(mContext, category, item.getInsertDate() + item.getBoardTitle(), files);
                            } catch (ExecutionException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        Toast.makeText(mContext, "실패!", Toast.LENGTH_SHORT).show();
                    }
                    updateRecyclerView();
                    finish();
                }
            }
        });
    }

    public void setPostRevise(){
        PostItem mPost = intent.getParcelableExtra("item");

        btn_write.setText("수정하기");
        et_title.setText(mPost.getBoardTitle());
        et_post.setText(mPost.getBoardContent());
        btn_write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPost.setBoardTitle(et_title.getText().toString());
                mPost.setBoardContent(et_post.getText().toString());

                if(BoardController.boardRevise(mContext, mPost)){
                    Toast.makeText(mContext, "수정을 완료하였습니다", Toast.LENGTH_SHORT).show();
                    /**게시글 수정 후, 해당 커뮤니티에서 즉각적으로 Update*/
                } else {
                    Toast.makeText(mContext, "실패!", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try{
            if (requestCode == 0) {
                if (resultCode == RESULT_OK) {

                    String[] filePath = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(data.getData(), filePath ,null,null,null);
                    cursor.moveToFirst();
                    String imgPath = cursor.getString(cursor.getColumnIndex(filePath[0]));
                    InputStream in = getContentResolver().openInputStream(data.getData());
                    Bitmap img = BitmapFactory.decodeStream(in);
                    Bitmap rotatedImg = ImageUtil.rotateBitmap(imgPath,img);
                    in.close();
                    cursor.close();

                    postImages.add(rotatedImg);
                    /**이미지 Attach*/
                    onImageAttach();

                    Toast.makeText(this,"사진 업로드 완료",Toast.LENGTH_SHORT).show();

                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_LONG).show();
                }
            }

            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
                File file = new File(currentPhotoPath);
                Bitmap img = MediaStore.Images.Media.getBitmap(getContentResolver(),Uri.fromFile(file));
                if(img != null){
                    ExifInterface ei = new ExifInterface(currentPhotoPath);
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED);

                    Bitmap rotatedBitmap = null;
                    switch(orientation) { /**이미지 원본에 맞게 회전변환*/

                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotatedBitmap = rotateImage(img, 90);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotatedBitmap = rotateImage(img, 180);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotatedBitmap = rotateImage(img, 270);
                            break;

                        case ExifInterface.ORIENTATION_NORMAL:
                        default:
                            rotatedBitmap = img;
                    }
                    postImages.add(rotatedBitmap);
                    onImageAttach();
                    Toast.makeText(mContext,"사진 촬영 업로드 완료",Toast.LENGTH_SHORT).show();
//                    galleryAddPic(file);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onImageAttach(){
        for(int i=0;i<4;i++){
            if(i<postImages.size()){
                imageViews[i].setImageBitmap(postImages.get(i));
                clearImages[i].setImageResource(R.drawable.ic_icon_clear);
            }
            else if(i==postImages.size()) {
                imageViews[i].setBackgroundResource(R.drawable.cardview_image_select);
                imageViews[i].setImageResource(R.drawable.ic_icon_camera);
                clearImages[i].setImageBitmap(null);
            }
            else{
                imageViews[i].setBackground(null);
                imageViews[i].setImageBitmap(null);
                clearImages[i].setImageBitmap(null);
            }
        }
        setImageWrite();
    }


    public void tedPermission(){
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Intent intent = new Intent();
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(intent, 0);
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(mContext,"권한이 거부되어 있어요",Toast.LENGTH_SHORT).show();
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("사진을 업로드하기 위하여 권한이 필요합니다.")
                .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    public void dispatchTakePictureIntent() {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    /**Continue only if the File was successfully created*/
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(mContext,
                                "com.hankki.fooddeal.FileProvider",
                                photoFile);

                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(mContext,"권한이 거부되어 있어요",Toast.LENGTH_SHORT).show();
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("사진을 촬영하기 위하여 권한이 필요합니다.")
                .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다")
                .setPermissions(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }

    /**카메로 촬영한 영상 이미지 처리
     * 기존 Bitmap 으로 가져온 이미지는 해상도 낮음
     * -> 원본 파일 접근 및 처리 함수 추가*/
    String currentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /*   prefix   */
                ".jpg",   /*   suffix   */
                storageDir      /*  directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private void galleryAddPic(File file) { /**사진 저장*/
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }






    /*Date date = new Date(System.currentTimeMillis());
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    TimeZone time = TimeZone.getTimeZone("Asia/Seoul");
	    format.setTimeZone(time);

	    bufW.write(format.format(date) + "\n");*/
    /**
     이현준
     Firebase Storage에 등록하고 얻은 Uri들을 FireStore의 PostPhotos 컬렉션에다가 글이 등록된 시간별로 분류된 문서 안에 List를 저장
     (시간을 밀스초단위로 쪼개서 저장하는게 좋을듯)
     각각의 파일마다 이 함수 한번씩 써야함 (Firebase 파일 업로드 기능에 여러개를 보내는게 없음)
     */
    private void uploadPostPhoto(byte[] imageData, String time, String index, Integer size) {
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("PostPhotos/" + time + "/" + index + ".jpg");
        UploadTask uploadTask = storageReference.putBytes(imageData);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()) {
                    Uri downloadUri = task.getResult();

                    setPhotoUrlInFireStore(downloadUri.toString(), time, index, size-1);
                } else {
                    Toast.makeText(getApplicationContext(), "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // postPhotos -> 현재 시각(서울 기준으로 ms 까지) -> Map<String, String> 형식으로 저장
    private void setPhotoUrlInFireStore(String photoUri, String time, String index, Integer size) {
        final DocumentReference documentReference = FirebaseFirestore.getInstance().collection("postPhotos").document(time);

        Map<String, String> photoUriMap = new HashMap<>();
        photoUriMap.put(index, photoUri);

        documentReference
                .set(photoUriMap, SetOptions.merge()) // 병합 옵션
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(index.equals(size.toString())) {
                            updateRecyclerView();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "이미지 URL FireStore 등록 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void updateRecyclerView() {
        /**게시글 추가 후, 해당 커뮤니티에서 즉각적으로 Update*/
        NavHostFragment navHostFragment = (NavHostFragment) ((MainActivity) MainActivity.mainContext)
                .getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
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
        finish();
    }
}