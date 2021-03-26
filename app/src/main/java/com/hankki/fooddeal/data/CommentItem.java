package com.hankki.fooddeal.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.hankki.fooddeal.data.retrofit.retrofitDTO.CommentListResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class CommentItem implements Parcelable {
    String userName;
    Bitmap userProfile;
    String message;
    String date;

    /**DB*/
    int commentSeq;
    int boardSeq;
    int parentCommentSeq;
    String userHashId;
    String commentContent;
    String insertDate;
    String delYn;
    String parentDelYn;
    String relativeTime;
    ArrayList<CommentItem> commentCommentList = new ArrayList<>();

    public ArrayList<CommentItem> getCommentCommentList() {
        return commentCommentList;
    }

    public int getCommentSeq() {
        return commentSeq;
    }

    public int getBoardSeq() {
        return boardSeq;
    }

    public int getParentCommentSeq() {
        return parentCommentSeq;
    }

    public String getUserHashId() {
        return userHashId;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public String getInsertDate() {
        return insertDate;
    }

    public String getDelYn() {
        return delYn;
    }

    public String getRelativeTime() {
        return relativeTime;
    }

    public String getParentDelYn() {
        return parentDelYn;
    }

    public void setCommentSeq(int commentSeq) {
        this.commentSeq = commentSeq;
    }

    public void setBoardSeq(int boardSeq) {
        this.boardSeq = boardSeq;
    }

    public void setParentCommentSeq(int parentCommentSeq) {
        this.parentCommentSeq = parentCommentSeq;
    }

    public void setUserHashId(String userHashId) {
        this.userHashId = userHashId;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public void setInsertDate(String insertDate) {
        this.insertDate = insertDate;
    }

    public void setDelYn(String delYn) {
        this.delYn = delYn;
    }

    public void setParentDelYn(String parentDelYn) {
        this.parentDelYn = parentDelYn;
    }

    public void setCommentCommentList(ArrayList<CommentItem> commentCommentList) {
        this.commentCommentList = commentCommentList;
    }

    public void addChildCommentList(CommentItem child){
        this.commentCommentList.add(child);
    }

    public CommentItem(String userName, String message, String date, @Nullable Bitmap profile){
        this.userName = userName;
        this.message = message;
        this.date = date;
        this.userProfile = profile;
    }

    public CommentItem(){

    }

    public void onBindCommentApi(CommentListResponse.CommentResponse response) throws ParseException {
        commentSeq = response.getCommentSeq();
        boardSeq = response.getBoardSeq();
        parentCommentSeq = response.getParentCommentSeq();
        userHashId = response.getUserHashId();
        commentContent = response.getCommentContent();
        insertDate = response.getInsertDate();
        delYn = response.getDelYN();
        parentDelYn = response.getParentDelYN();
        try {
            relativeTime = calculateTime(response.getInsertDate());
        } catch (Exception e){
            relativeTime = "예전";
        }
    }

    public HashMap<String, String> onBindBodyApi(Context context){
        HashMap<String, String> body = new HashMap<>();
        body.put("BOARD_SEQ",String.valueOf(getBoardSeq()));
        body.put("USER_TOKEN",PreferenceManager.getString(context,"userToken"));
        body.put("COMMENT_CONTENT",getCommentContent());
        body.put("INSERT_DATE",getInsertDate());
        return body;
    }

    protected CommentItem(Parcel in) {
        userName = in.readString();
        message = in.readString();
        date = in.readString();
        relativeTime = in.readString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUserName() {
        return userName;
    }

    public String getMessage() {
        return message;
    }

    public String getDate(){return date;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userName);
        dest.writeString(message);
        dest.writeString(date);
        dest.writeString(relativeTime);
    }

    public String calculateTime(String date) throws ParseException {
        String relativeTime;
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",Locale.KOREA);
        Date date1 = format.parse(date);
        long time1 = date1.getTime();
        int second = (int) (System.currentTimeMillis() - time1)/1000;
        if(second > 60){
            if(second > 3600){
                if(second > 86400){
                    relativeTime = (int) second/86400 + "일 전";
                } else {
                    relativeTime = String.valueOf((int) second / 3600) + "시간 전";
                }
            } else {
                relativeTime = String.valueOf((int)second/60) + "분 전";
            }
        } else {
            relativeTime = "방금 전";
        }
        return relativeTime;
    }

    public static final Creator<CommentItem> CREATOR = new Creator<CommentItem>() {
        @Override
        public CommentItem createFromParcel(Parcel in) {
            return new CommentItem(in);
        }

        @Override
        public CommentItem[] newArray(int size) {
            return new CommentItem[size];
        }
    };
}

