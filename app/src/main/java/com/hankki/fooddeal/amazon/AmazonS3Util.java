package com.hankki.fooddeal.amazon;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.hankki.fooddeal.data.PostItem;
import com.hankki.fooddeal.ui.home.community.PostActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class AmazonS3Util {
    public static CognitoCachingCredentialsProvider credentialsProvider;
    public static AmazonS3 s3;
    public static TransferUtility transferUtility;
    public static Disposable disposable;
    public static boolean complete;
    public static boolean flag = true;


    public static void init(Context context){
        credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                "ap-northeast-2:8b88dd46-3819-4e7c-80ce-9f4e4684cd26", // 자격 증명 풀 ID
                Regions.AP_NORTHEAST_2 // 리전
        );
        s3 = new AmazonS3Client(credentialsProvider, Region.getRegion(Regions.AP_NORTHEAST_2));
        transferUtility = TransferUtility.builder().s3Client(s3).context(context).build();
    }

    public static void uploadImageToServer(Context context, String filename, File file){
        TransferNetworkLossHandler.getInstance(context);

        if(credentialsProvider==null) {
            credentialsProvider = new CognitoCachingCredentialsProvider(
                    context,
                    "ap-northeast-2:8b88dd46-3819-4e7c-80ce-9f4e4684cd26", // 자격 증명 풀 ID
                    Regions.AP_NORTHEAST_2 // 리전
            );
            s3 = new AmazonS3Client(credentialsProvider, Region.getRegion(Regions.AP_NORTHEAST_2));
            transferUtility = TransferUtility.builder().s3Client(s3).context(context).build();
        }

        disposable = Observable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                Log.d("amazon", "Rxjava call observe");
                TransferObserver observer = transferUtility.upload(
                        "hankki-s3/profile", /* 업로드 할 버킷 이름 */
                        filename, /* 버킷에 저장할 파일의 이름 */
                        file /* 버킷에 저장할 파일 */
                );
                observe(context, observer);
                return false;
            }

        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object result) throws Exception {
                        Log.d("amazon", String.valueOf(complete));
                        Log.d("amazon", "Rxjava accept");
                    }
                });
    }


    public static void uploadImageToServer(Context context, String category, String boardId, ArrayList<File> files) throws ExecutionException, InterruptedException {

        TransferNetworkLossHandler.getInstance(context);

        if(credentialsProvider==null) {
            credentialsProvider = new CognitoCachingCredentialsProvider(
                    context,
                    "ap-northeast-2:8b88dd46-3819-4e7c-80ce-9f4e4684cd26", // 자격 증명 풀 ID
                    Regions.AP_NORTHEAST_2 // 리전
            );
            s3 = new AmazonS3Client(credentialsProvider, Region.getRegion(Regions.AP_NORTHEAST_2));
            transferUtility = TransferUtility.builder().s3Client(s3).context(context).build();
        }

        disposable = Observable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                Log.d("amazon", "Rxjava call observe");
                uploadFileList(context,category,boardId,files,0);
                return false;
            }

        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object result) throws Exception {
                        Log.d("amazon", String.valueOf(complete));
                        Log.d("amazon", "Rxjava accept");
                    }
                });



//        complete = new AsyncTask<Void, Void, Boolean>() {
//            boolean finalComplete = false;
//            @Override
//            protected Boolean doInBackground(Void... voids) {
//                TransferObserver observer = transferUtility.upload(
//                        "hankki-s3/", /* 업로드 할 버킷 이름 */
//                        filename, /* 버킷에 저장할 파일의 이름 */
//                        file /* 버킷에 저장할 파일 */
//                );
//                observer.setTransferListener(new TransferListener() {
//                    @Override
//                    public void onStateChanged(int id, TransferState state) {
//                        if(state == TransferState.COMPLETED){
//                            finalComplete = true;
//                            Toast.makeText(context, "사진을 성공적으로 업르도", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                    @Override
//                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
//                        int percentage = (int) (bytesCurrent/bytesTotal * 100);
//                    }
//                    @Override
//                    public void onError(int id, Exception ex) {
//                        Toast.makeText(context, "에러가 발생했습니다", Toast.LENGTH_SHORT).show();
//                        Log.d("Amazon S3",ex.getMessage());
//                    }
//
//                });
//                return finalComplete;
//            }
//        }.execute().get();

//
        Log.d("amazon", "Complete is " + String.valueOf(complete));
//        disposable.dispose();
    }

    private static void uploadFileList(Context context, String category, String boardId, ArrayList<File> files, int index){
        TransferObserver observer = transferUtility.upload(
                "hankki-s3/community/" + category + "/" + boardId, /* 업로드 할 버킷 이름 */
                String.valueOf(index), /* 버킷에 저장할 파일의 이름 */
                files.get(index) /* 버킷에 저장할 파일 */
        );
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if(index < files.size()-1){
                    uploadFileList(context,category,boardId,files,index+1);
                } else {
                    Toast.makeText(context, "완료", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

            }

            @Override
            public void onError(int id, Exception ex) {

            }
        });
    }

    public static void setComplete(boolean com){
        complete = com;
    }

    public static void observe(Context context, TransferObserver observer){
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    setComplete(true);
                    flag = false;
                    Toast.makeText(context, "사진을 성공적으로 업르도", Toast.LENGTH_SHORT).show();
                    ((PostActivity)context).updateRecyclerView();
                    ((PostActivity)context).finish();
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            }

            @Override
            public void onError(int id, Exception ex) {
                Toast.makeText(context, "에러가 발생했습니다", Toast.LENGTH_SHORT).show();
                Log.d("Amazon S3", ex.getMessage());
                flag = false;
            }

        });
    }


    public static String getDownloadUrl(){
        return "https://hankki-s3.s3.ap-northeast-2.amazonaws.com/";
    }



    public static void deleteImageOfServer(Context context, PostItem item) throws IOException {
        Regions clientRegion = Regions.DEFAULT_REGION;
        String bucketName = "hankki-s3";
        String keyName = "community/"+item.getCategory()+"/"+item.getInsertDate()+item.getBoardTitle();

        try {
            if(credentialsProvider==null) {
                credentialsProvider = new CognitoCachingCredentialsProvider(
                        context,
                        "ap-northeast-2:8b88dd46-3819-4e7c-80ce-9f4e4684cd26", // 자격 증명 풀 ID
                        Regions.AP_NORTHEAST_2 // 리전
                );
                s3 = new AmazonS3Client(credentialsProvider, Region.getRegion(Regions.AP_NORTHEAST_2));
                transferUtility = TransferUtility.builder().s3Client(s3).context(context).build();
            }
            transferUtility.upload(bucketName,keyName+"/"+0,null);
            s3.deleteObject(new DeleteObjectRequest(bucketName, keyName+"/"+0));
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace();
        } catch (Exception e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }
    }

}
