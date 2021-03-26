package com.hankki.fooddeal.data.retrofit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.common.base.Ascii;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hankki.fooddeal.amazon.AmazonS3Util;
import com.hankki.fooddeal.data.CommentItem;
import com.hankki.fooddeal.data.PostItem;
import com.hankki.fooddeal.data.PreferenceManager;
import com.hankki.fooddeal.data.retrofit.retrofitDTO.BoardListResponse;
import com.hankki.fooddeal.data.retrofit.retrofitDTO.CommentListResponse;
import com.hankki.fooddeal.data.retrofit.retrofitDTO.MemberResponse;
import com.hankki.fooddeal.ux.dialog.CustomAnimationDialog;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.http.Url;

public class BoardController {
    public static APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
    public static String option = "distance";

    public static ArrayList<PostItem> getBoardList(Context context, String boardCode) {
        if(AmazonS3Util.transferUtility==null) {
            AmazonS3Util.init(context);
        }
        Log.d("Timecheck", "getBoardList Start");
        ArrayList<PostItem> items = new ArrayList<>();
        Log.d("Timecheck", "Preference Start");
        String regionFirst = PreferenceManager.getString(context, "region1Depth");
        String regionSecond = PreferenceManager.getString(context, "region2Depth");
        String regionThird = PreferenceManager.getString(context, "region3Depth");
        Log.d("Timecheck", "Preference End");

        Log.d("Timecheck", "Call Start");
        Call<BoardListResponse> boardListCall = apiInterface.getBoardList(regionFirst, regionSecond, regionThird, boardCode);
        try {
            items = new AsyncTask<Void, Void, ArrayList<PostItem>>() {

                @Override
                protected ArrayList<PostItem> doInBackground(Void... voids) {
                    final ArrayList<PostItem> postItems = new ArrayList<>();
                    try {
                        BoardListResponse boardListResponse = boardListCall.execute().body();
                        assert boardListResponse != null;
                        List<BoardListResponse.BoardResponse> boardResponses = boardListResponse.getBoardList();
                        for (BoardListResponse.BoardResponse boardResponse : boardResponses) {
                            try {
                                PostItem item = new PostItem();
                                Log.d("getBoardList", "line 76");
                                item.onBindBoardApi(context, boardResponse);
                                Log.d("getBoardList", "line 77");
                                postItems.add(0, item);
                                Log.d("getBoardList", "line 78");
                                Log.d("Timecheck", "Call End");
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        Log.d("getBoardList",e.getMessage());
                        e.printStackTrace();
                    }
                    return postItems;
                }

            }.execute().get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("############", "getBoardList End");
        return items;
    }

    public static ArrayList<PostItem> getBoardList(Context context, String boardCode, int distance) {
        Log.d("############", "getBoardList Start");
        ArrayList<PostItem> items = new ArrayList<>();

        String regionFirst = PreferenceManager.getString(context, "region1Depth");
        String regionSecond = PreferenceManager.getString(context, "region2Depth");
        String regionThird = PreferenceManager.getString(context, "region3Depth");

        Call<BoardListResponse> boardListCall = apiInterface.getBoardList(regionFirst, regionSecond, regionThird, boardCode);
        try {
            items = new AsyncTask<Void, Void, ArrayList<PostItem>>() {

                @Override
                protected ArrayList<PostItem> doInBackground(Void... voids) {
                    final ArrayList<PostItem> postItems = new ArrayList<>();
                    try {
                        BoardListResponse boardListResponse = boardListCall.execute().body();
                        assert boardListResponse != null;
                        List<BoardListResponse.BoardResponse> boardResponses = boardListResponse.getBoardList();
                        for (BoardListResponse.BoardResponse boardResponse : boardResponses) {
                            PostItem item = new PostItem();

                            item.onBindBoardApi(context, boardResponse);
                            if(item.getDistance()<=distance)
                                postItems.add(0,item);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return postItems;
                }

            }.execute().get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("############", "getBoardList End");
        return items;
    }

    public static boolean boardWrite(Context context, PostItem item) {
        boolean complete = false;
        HashMap<String, String> body = item.onBindBodyApi(context);

        Call<MemberResponse> boardWrite = apiInterface.boardWrite(body);

        try {
            complete = new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... voids) {
                    boolean finalComplete = false;
                    try {
                        MemberResponse response = boardWrite.execute().body();
                        if (response != null && response.getResponseCode() == 400) {
                            finalComplete = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return finalComplete;
                }
            }.execute().get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return complete;
    }

    public static boolean boardRevise(Context context, PostItem item) {
        boolean complete = false;
        HashMap<String, String> body = item.onBindBodyApi(context);
        body.put("BOARD_SEQ", String.valueOf(item.getBoardSeq()));
        Call<MemberResponse> responseCall = apiInterface.boardRevise(body);
        try {
            complete = new AsyncTask<Void, Void, Boolean>() {
                boolean finalComplete = false;
                @Override
                protected Boolean doInBackground(Void... voids) {
                    try {
                        MemberResponse response = responseCall.execute().body();
                        if (response != null && response.getResponseCode() == 402) {
                            finalComplete = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return finalComplete;
                }
            }.execute().get();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return complete;
    }

    public static boolean boardDelete(Context context, PostItem item) {
        boolean complete = false;
        HashMap<String, String> body = new HashMap<>();
        body.put("BOARD_SEQ", String.valueOf(item.getBoardSeq()));
        body.put("USER_TOKEN", PreferenceManager.getString(context, "userToken"));
        Call<MemberResponse> responseCall = apiInterface.boardDelete(body);
        try {
            complete = new AsyncTask<Void, Void, Boolean>() {
                boolean finalComplete = false;
                @Override
                protected Boolean doInBackground(Void... voids) {
                    try {
                        MemberResponse response = responseCall.execute().body();
                        if (response != null && response.getResponseCode() == 420) {
                            finalComplete = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return finalComplete;
                }
            }.execute().get();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return complete;
    }


    public static ArrayList<CommentItem> getBoardCommentList(PostItem postItem) {
        ArrayList<CommentItem> commentItems = new ArrayList<>();
        Call<CommentListResponse> commentListResponseCall = apiInterface.getBoardCommentList(postItem.getBoardSeq());
        try {
            commentItems = new AsyncTask<Void, Void, ArrayList<CommentItem>>() {
                final ArrayList<CommentItem> items = new ArrayList<>();
                @Override
                protected ArrayList<CommentItem> doInBackground(Void... voids) {
                    try {
                        CommentListResponse commentListResponse = commentListResponseCall.execute().body();
                        assert commentListResponse != null;
                        List<CommentListResponse.CommentResponse> commentResponses = commentListResponse.getBoardCommentList();
                        for (CommentListResponse.CommentResponse comment : commentResponses) {
                            CommentItem item = new CommentItem();
                            item.onBindCommentApi(comment);
                            items.add(item);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return items;
                }
            }.execute().get();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return commentItems;
    }

    public static boolean commentWrite(Context context, CommentItem comment) {
        boolean complete = false;
        HashMap<String, String> body = comment.onBindBodyApi(context);
        Call<MemberResponse> responseCall = apiInterface.commentWrite(body);
        try {
            complete = new AsyncTask<Void, Void, Boolean>() {
                boolean finalComplete = false;
                @Override
                protected Boolean doInBackground(Void... voids) {
                    try {
                        MemberResponse response = responseCall.execute().body();
                        if (response != null && response.getResponseCode() == 800) {
                            finalComplete = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return finalComplete;
                }
            }.execute().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return complete;
    }

    public static boolean commentDelete(Context context, CommentItem item){
        boolean complete = false;
        HashMap<String, String> body = new HashMap<>();
        body.put("COMMENT_SEQ",String.valueOf(item.getCommentSeq()));
        body.put("USER_TOKEN",PreferenceManager.getString(context,"userToken"));
        body.put("BOARD_SEQ",String.valueOf(item.getBoardSeq()));
        Call<MemberResponse> responseCall = apiInterface.commentDelete(body);
        try{
            complete = new AsyncTask<Void, Void, Boolean>() {
                boolean finalComplete = false;
                @Override
                protected Boolean doInBackground(Void... voids) {
                    try{
                        MemberResponse response = responseCall.execute().body();
                        if(response != null && response.getResponseCode()==820){
                            finalComplete = true;
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    return finalComplete;
                }
            }.execute().get();
        } catch (Exception e){
            e.printStackTrace();
        }
        return complete;
    }

    public static boolean childCommentWrite(Context context,CommentItem parentComment, CommentItem comment) {
        boolean complete = false;
        HashMap<String, String> body = comment.onBindBodyApi(context);
        body.put("PARENT_COMMENT_SEQ",String.valueOf(parentComment.getCommentSeq()));
        Call<MemberResponse> responseCall = apiInterface.commentWrite(body);
        try {
            complete = new AsyncTask<Void, Void, Boolean>() {
                boolean finalComplete = false;
                @Override
                protected Boolean doInBackground(Void... voids) {
                    try {
                        MemberResponse response = responseCall.execute().body();
                        if(response != null && response.getResponseCode()==800){
                            finalComplete = true;
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    return finalComplete;
                }
            }.execute().get();
        } catch (Exception e){
            e.printStackTrace();
        }
        return complete;
    }

    public static boolean boardLikePlus(Context context, PostItem item) {
        boolean complete = false;
        HashMap<String, String> body = new HashMap<>();
        body.put("BOARD_SEQ", String.valueOf(item.getBoardSeq()));
        body.put("USER_TOKEN", PreferenceManager.getString(context, "userToken"));
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdfnow = new SimpleDateFormat("YYYY/MM/dd HH:mm:ss");
        String timeData = sdfnow.format(date);
        body.put("LIKE_DATE", timeData);

        Call<MemberResponse> responseCall = apiInterface.boardLikePlus(body);
        try {
            complete = new AsyncTask<Void, Void, Boolean>() {
                boolean finalComplete = false;
                @Override
                protected Boolean doInBackground(Void... voids) {
                    try {
                        MemberResponse response = responseCall.execute().body();
                        if (response != null && response.getResponseCode() == 100) {
                            finalComplete = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return finalComplete;
                }
            }.execute().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return complete;
    }

    public static boolean boardLikeMinus(Context context, PostItem item) {
        boolean complete = false;
        HashMap<String, String> body = new HashMap<>();
        body.put("BOARD_SEQ", String.valueOf(item.getBoardSeq()));
        body.put("USER_TOKEN", PreferenceManager.getString(context, "userToken"));

        Call<MemberResponse> responseCall = apiInterface.boardLikeMinus(body);
        try {
            complete = new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... voids) {
                    boolean finalComplete = false;
                    try {
                        MemberResponse response = responseCall.execute().body();
                        if (response != null && response.getResponseCode() == 102) {
                            finalComplete = true;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return finalComplete;
                }
            }.execute().get();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return complete;
    }

    public static ArrayList<PostItem> getBoardWriteList(Context context) {
        ArrayList<PostItem> myBoardList = new ArrayList<>();
        Call<BoardListResponse> responseCall = apiInterface.getBoardWriteList(PreferenceManager.getString(context, "userToken"));
        try {
            myBoardList = new AsyncTask<Void, Void, ArrayList<PostItem>>() {
                ArrayList<PostItem> items = new ArrayList<>();

                @Override
                protected ArrayList<PostItem> doInBackground(Void... voids) {
                    try {
                        BoardListResponse response = responseCall.execute().body();
                        if (response != null && response.getResponseCode() == 410) {
                            List<BoardListResponse.BoardResponse> boardResponses = response.getBoardList();
                            for (BoardListResponse.BoardResponse boardResponse : boardResponses) {
                                PostItem item = new PostItem();

                                item.onBindBoardApi(context, boardResponse);
                                items.add(item);
                            }
                        }
                    } catch (IOException | ParseException e) {
                        e.printStackTrace();
                    }
                    return items;
                }
            }.execute().get();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return myBoardList;
    }

    public static ArrayList<PostItem> getExchangeShareBoardWriteList(Context context, String category){
        ArrayList<PostItem> myBoardList = new ArrayList<>();
        Call<BoardListResponse> responseCall = apiInterface.getBoardWriteList(PreferenceManager.getString(context, "userToken"));
        try {
            myBoardList = new AsyncTask<Void, Void, ArrayList<PostItem>>() {
                ArrayList<PostItem> items = new ArrayList<>();

                @Override
                protected ArrayList<PostItem> doInBackground(Void... voids) {
                    try {
                        BoardListResponse response = responseCall.execute().body();
                        if (response != null && response.getResponseCode() == 410) {
                            List<BoardListResponse.BoardResponse> boardResponses = response.getBoardList();
                            for (BoardListResponse.BoardResponse boardResponse : boardResponses) {
                                PostItem item = new PostItem();

                                item.onBindBoardApi(context, boardResponse);
                                if (item.getCategory().equals(category))
                                    items.add(0,item);
                            }
                        }
                    } catch (IOException | ParseException e) {
                        e.printStackTrace();
                    }
                    return items;
                }
            }.execute().get();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return myBoardList;
    }

    public static ArrayList<PostItem> getRecipeBoardWriteList(Context context) {
        ArrayList<PostItem> myBoardList = new ArrayList<>();
        Call<BoardListResponse> responseCall = apiInterface.getBoardWriteList(PreferenceManager.getString(context, "userToken"));
        try {
            myBoardList = new AsyncTask<Void, Void, ArrayList<PostItem>>() {
                ArrayList<PostItem> items = new ArrayList<>();

                @Override
                protected ArrayList<PostItem> doInBackground(Void... voids) {
                    try {
                        BoardListResponse response = responseCall.execute().body();
                        if (response != null && response.getResponseCode() == 410) {
                            List<BoardListResponse.BoardResponse> boardResponses = response.getBoardList();
                            for (BoardListResponse.BoardResponse boardResponse : boardResponses) {
                                PostItem item = new PostItem();

                                item.onBindBoardApi(context, boardResponse);
                                if (item.getCategory().equals("RECIPE")) {
                                    items.add(0,item);
                                }
                            }
                        }
                    } catch (IOException | ParseException e) {
                        e.printStackTrace();
                    }
                    return items;
                }
            }.execute().get();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return myBoardList;
    }

    public static ArrayList<PostItem> getFreeBoardWriteList(Context context) {
        ArrayList<PostItem> myBoardList = new ArrayList<>();
        Call<BoardListResponse> responseCall = apiInterface.getBoardWriteList(PreferenceManager.getString(context, "userToken"));
        try {
            myBoardList = new AsyncTask<Void, Void, ArrayList<PostItem>>() {
                ArrayList<PostItem> items = new ArrayList<>();

                @Override
                protected ArrayList<PostItem> doInBackground(Void... voids) {
                    try {
                        BoardListResponse response = responseCall.execute().body();
                        if (response != null && response.getResponseCode() == 410) {
                            List<BoardListResponse.BoardResponse> boardResponses = response.getBoardList();
                            for (BoardListResponse.BoardResponse boardResponse : boardResponses) {
                                PostItem item = new PostItem();

                                item.onBindBoardApi(context, boardResponse);
                                if (item.getCategory().equals("FREE")) {
                                    items.add(0,item);
                                }
                            }
                        }
                    } catch (IOException | ParseException e) {
                        e.printStackTrace();
                    }
                    return items;
                }
            }.execute().get();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return myBoardList;
    }

    public static ArrayList<PostItem> getBoardLikeList(Context context) {
        ArrayList<PostItem> likeBoardList = new ArrayList<>();
        Call<BoardListResponse> responseCall = apiInterface.getBoardLikeList(PreferenceManager.getString(context, "userToken"));
        try {
            likeBoardList = new AsyncTask<Void, Void, ArrayList<PostItem>>() {
                ArrayList<PostItem> items = new ArrayList<>();

                @Override
                protected ArrayList<PostItem> doInBackground(Void... voids) {
                    try {
                        BoardListResponse response = responseCall.execute().body();
                        if (response != null && response.getResponseCode() == 110) {
                            List<BoardListResponse.BoardResponse> boardResponses = response.getLikeList();
                            for (BoardListResponse.BoardResponse boardResponse : boardResponses) {
                                PostItem item = new PostItem();

                                item.onBindBoardApi(context, boardResponse);
                                items.add(0,item);
                            }
                        }
                    } catch (IOException | ParseException e) {
                        e.printStackTrace();
                    }
                    return items;
                }
            }.execute().get();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return likeBoardList;
    }

    public static ArrayList<PostItem> getExchangeShareBoardLikeList(Context context, String category){
        ArrayList<PostItem> likeBoardList = new ArrayList<>();
        Call<BoardListResponse> responseCall = apiInterface.getBoardLikeList(PreferenceManager.getString(context, "userToken"));
        try {
            likeBoardList = new AsyncTask<Void, Void, ArrayList<PostItem>>() {
                ArrayList<PostItem> items = new ArrayList<>();

                @Override
                protected ArrayList<PostItem> doInBackground(Void... voids) {
                    try {
                        BoardListResponse response = responseCall.execute().body();
                        if (response != null && response.getResponseCode() == 110) {
                            List<BoardListResponse.BoardResponse> boardResponses = response.getLikeList();
                            for (BoardListResponse.BoardResponse boardResponse : boardResponses) {
                                PostItem item = new PostItem();

                                item.onBindBoardApi(context, boardResponse);
                                if (item.getCategory().equals(category))
                                    items.add(0,item);
                            }
                        }
                    } catch (IOException | ParseException e) {
                        e.printStackTrace();
                    }
                    return items;
                }
            }.execute().get();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return likeBoardList;
    }

    public static ArrayList<PostItem> getRecipeBoardLikeList(Context context) {
        ArrayList<PostItem> likeBoardList = new ArrayList<>();
        Call<BoardListResponse> responseCall = apiInterface.getBoardLikeList(PreferenceManager.getString(context, "userToken"));
        try {
            likeBoardList = new AsyncTask<Void, Void, ArrayList<PostItem>>() {
                ArrayList<PostItem> items = new ArrayList<>();

                @Override
                protected ArrayList<PostItem> doInBackground(Void... voids) {
                    try {
                        BoardListResponse response = responseCall.execute().body();
                        if (response != null && response.getResponseCode() == 110) {
                            List<BoardListResponse.BoardResponse> boardResponses = response.getLikeList();
                            for (BoardListResponse.BoardResponse boardResponse : boardResponses) {
                                PostItem item = new PostItem();

                                item.onBindBoardApi(context, boardResponse);
                                if (item.getCategory().equals("RECIPE")) {
                                    items.add(0,item);
                                }
                            }
                        }
                    } catch (IOException | ParseException e) {
                        e.printStackTrace();
                    }
                    return items;
                }
            }.execute().get();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return likeBoardList;
    }

    public static ArrayList<PostItem> getFreeBoardLikeList(Context context) {
        ArrayList<PostItem> likeBoardList = new ArrayList<>();
        Call<BoardListResponse> responseCall = apiInterface.getBoardLikeList(PreferenceManager.getString(context, "userToken"));
        try {
            likeBoardList = new AsyncTask<Void, Void, ArrayList<PostItem>>() {
                ArrayList<PostItem> items = new ArrayList<>();

                @Override
                protected ArrayList<PostItem> doInBackground(Void... voids) {
                    try {
                        BoardListResponse response = responseCall.execute().body();
                        if (response != null && response.getResponseCode() == 110) {
                            List<BoardListResponse.BoardResponse> boardResponses = response.getLikeList();
                            for (BoardListResponse.BoardResponse boardResponse : boardResponses) {
                                PostItem item = new PostItem();

                                item.onBindBoardApi(context, boardResponse);
                                if (item.getCategory().equals("FREE")) {
                                    items.add(0,item);
                                }
                            }
                        }
                    } catch (IOException | ParseException e) {
                        e.printStackTrace();
                    }
                    return items;
                }
            }.execute().get();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return likeBoardList;
    }

    public static boolean isLikedBoard(Context context, PostItem item) {
        ArrayList<PostItem> likedPosts = getBoardLikeList(context);
        if (likedPosts != null && likedPosts.size() > 0) {
            for (PostItem postItem : likedPosts) {
                if (postItem.getBoardSeq() == item.getBoardSeq() &&
                        postItem.getCategory().equals(item.getCategory())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getTime() {
        Date date = new Date(System.currentTimeMillis());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        TimeZone time = TimeZone.getTimeZone("Asia/Seoul");
        format.setTimeZone(time);
        return format.format(date);
    }

    private static String getThumbnailUrl(String title, String date, String category) {
        String realCategory;
        if(category.equals("FR01")) {
            realCategory = "FREE";
        } else if(category.equals("IN01")){
            realCategory = "INGREDIENT EXCHANGE";
        } else if(category.equals("IN02")){
            realCategory = "INGREDIENT SHARE";
        } else {
            realCategory = "RECIPE";
        }
        String path = AmazonS3Util.s3.getUrl("hankki-s3","community/"+realCategory+"/"+date+title+"/"+"0").toString();




//        final DocumentReference documentReference = FirebaseFirestore.getInstance().collection("postPhotos").document(timestamp);
//        Task<DocumentSnapshot> task = documentReference.get();
//        try {
//            DocumentSnapshot documentSnapshot = Tasks.await(task);
//            result = documentSnapshot.getString("0");
//            Log.w("Thumnail", result + " " + timestamp);
//        } catch (Exception e) {
//            result = "";
//        }

        return path;
    }

    public static String stringToHex(String s) {
        String result = "";

        for (int i = 0; i < s.length(); i++) {
            result += String.format("%02X ", (int) s.charAt(i));
        }

        return result;
    }

    public static ArrayList<PostItem> boardDistanceFiltering(ArrayList<PostItem> items, int distance){
        for(PostItem item: items){
            if(item.getDistance() > distance){
                items.remove(item);
            }
        }
        return items;
    }
}
