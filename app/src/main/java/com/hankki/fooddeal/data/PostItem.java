package com.hankki.fooddeal.data;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.hankki.fooddeal.amazon.AmazonS3Util;
import com.hankki.fooddeal.data.retrofit.BoardController;
import com.hankki.fooddeal.data.retrofit.retrofitDTO.BoardListResponse;
import com.hankki.fooddeal.data.security.AES256Util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**Post Item
 * 게시글, 채팅, 쿠폰 등
 * Recycler View 구성하는 Item Frame*/
public class PostItem implements Comparable<PostItem>, Parcelable {
    /**DB 데이터에 맞춰서 필드 추가*/

    String boardContent;
    String regionFirst;
    String regionSecond;
    String regionThird;
    String insertDate;
    String boardTitle;
    Bitmap userProfile = null;
    String userLatitude;
    String userLongitude;
    String userToken;
    String userHashId;
    ArrayList<Bitmap> images = new ArrayList<>();
    String thumbnailUrl;
    String category = "";
    int imgCount = 0;
    int likeCount = 0;
    int commentCount = 0;
    int boardSeq;
    int userSeq;
    int distance;
    String delYN;
    String relativeTime;
    long absoluteTime;


    protected PostItem(Parcel in) {
        boardContent = in.readString();
        regionFirst = in.readString();
        regionSecond = in.readString();
        regionThird = in.readString();
        insertDate = in.readString();
        boardTitle = in.readString();
        userProfile = in.readParcelable(Bitmap.class.getClassLoader());
        userLatitude = in.readString();
        userLongitude = in.readString();
        images = in.createTypedArrayList(Bitmap.CREATOR);
        thumbnailUrl = in.readString();
        category = in.readString();
        likeCount = in.readInt();
        commentCount = in.readInt();
        boardSeq = in.readInt();
        userSeq = in.readInt();
        delYN = in.readString();
        distance = in.readInt();
        userHashId = in.readString();
        relativeTime = in.readString();
        imgCount = in.readInt();
    }

    public static final Creator<PostItem> CREATOR = new Creator<PostItem>() {
        @Override
        public PostItem createFromParcel(Parcel in) {
            return new PostItem(in);
        }

        @Override
        public PostItem[] newArray(int size) {
            return new PostItem[size];
        }
    };

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public int getImgCount(){return imgCount;}

    public PostItem(){
    }

    public void setBoardContent(String boardContent) {
        this.boardContent = boardContent;
    }

    public void setBoardTitle(String boardTitle) {
        this.boardTitle = boardTitle;
    }

    public void setUserProfile(Bitmap userProfile) {
        this.userProfile = userProfile;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public void setImgCount(int imgCount) {this.imgCount = imgCount; }

    public String getBoardContent() {
        return boardContent;
    }

    public String getBoardTitle(){
        return boardTitle;
    }

    public int getDistance() {
        return distance;
    }

    public String getInsertDate(){ return insertDate; }

    public Bitmap getUserProfile() {
        return userProfile;
    }

    public String getRegionFirst() {
        return regionFirst;
    }

    public String getRegionSecond() {
        return regionSecond;
    }

    public String getRegionThird() {
        return regionThird;
    }

    public String getUserLatitude() {
        return userLatitude;
    }

    public String getUserLongitude() {
        return userLongitude;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public int getBoardSeq() {
        return boardSeq;
    }

    public int getUserSeq() {
        return userSeq;
    }

    public String getUserHashId() {
        return userHashId;
    }

    public String getDelYN() {
        return delYN;
    }

    public void addPostImage(Bitmap image){
        images.add(image);
    }

    public ArrayList<Bitmap> getImages() {
        return images;
    }

    public void setImages(ArrayList<Bitmap> images) {
        this.images = images;
    }

    public void setInsertDate(String insertDate) {
        this.insertDate = insertDate;
    }

    public void setUserLatitude(String userLatitude) {
        this.userLatitude = userLatitude;
    }

    public void setUserLongitude(String userLongitude) {
        this.userLongitude = userLongitude;
    }

    public String getThumbnailUrl() { return thumbnailUrl; }

    public String getRelativeTime(){ return relativeTime; }

    public long getAbsoluteTime() {
        return absoluteTime;
    }

    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public void setBoardSeq(int boardSeq) {
        this.boardSeq = boardSeq;
    }

    public void setUserSeq(int userSeq) {
        this.userSeq = userSeq;
    }

    public void setDelYN(String delYN) {
        this.delYN = delYN;
    }

    @Override
    public int compareTo(PostItem o) {
        if(BoardController.option.equals("distance")) {
            return this.getDistance() - o.getDistance();
        } else {
            return (int)this.getAbsoluteTime() - (int)o.getAbsoluteTime();
        }
    }


    public void onBindBoardApi(Context context, BoardListResponse.BoardResponse boardResponse) throws ParseException {
        boardSeq = boardResponse.getBoardSeq();
        boardTitle = boardResponse.getBoardTitle();
        Log.d("bind",boardTitle);
        boardContent = boardResponse.getBoardContent();
        Log.d("bind",boardResponse.getUserLat());
        Log.d("bind",boardResponse.getUserLon());
        try {
            userLatitude = AES256Util.aesDecode(boardResponse.getUserLat());
            Log.d("bind", userLatitude);
            userLongitude = AES256Util.aesDecode(boardResponse.getUserLon());
            Log.d("bind", userLongitude);
        } catch (Exception e) {
            Log.d("bind","Location Failed");
        }
        userHashId = boardResponse.getUserHashId();
        regionFirst = boardResponse.getRegionFirst();
        regionSecond = boardResponse.getRegionSecond();
        regionThird = boardResponse.getRegionThird();

        switch(boardResponse.getBoardCodeSeq()){
            case "IN01":
                category = "INGREDIENT EXCHANGE";
                break;
            case "IN02":
                category = "INGREDIENT SHARE";
                break;
            case "FR01":
                category = "FREE";
                break;
            case "RE01":
                category = "RECIPE";
                break;
            default:
                category = boardResponse.getBoardCodeSeq();
        }
        imgCount = boardResponse.getBoardImageSize();
        insertDate = boardResponse.getInsertDate();
        likeCount = boardResponse.getLikeCount();
        delYN = boardResponse.getDelYn();
        commentCount = boardResponse.getCommentCount();
        thumbnailUrl = boardResponse.getBoardThumbnail();
        relativeTime = calculateTime(insertDate);
        distance = calculateDistance(context, Double.parseDouble(userLatitude),Double.parseDouble(userLongitude));
    }

    public HashMap<String, String> onBindBodyApi(Context context){
        HashMap<String, String> body = new HashMap<>();
        body.put("BOARD_CODE_SORT", this.getCategory());
        body.put("USER_TOKEN", PreferenceManager.getString(context, "userToken"));
        body.put("BOARD_TITLE",this.getBoardTitle());
        body.put("BOARD_CONTENT",this.getBoardContent());
        body.put("INSERT_DATE",this.getInsertDate());
        body.put("USER_LAT",PreferenceManager.getString(context, "Latitude"));
        body.put("USER_LON",PreferenceManager.getString(context,"Longitude"));
        body.put("REGION_1DEPTH_NAME",PreferenceManager.getString(context, "region1Depth"));
        body.put("REGION_2DEPTH_NAME",PreferenceManager.getString(context, "region2Depth"));
        body.put("REGION_3DEPTH_NAME",PreferenceManager.getString(context, "region3Depth"));
        body.put("ROAD_NAME", PreferenceManager.getString(context, "roadName"));
        body.put("BOARD_IMAGE_SIZE", String.valueOf(this.getImgCount()));
        if(imgCount>0){
            body.put("BOARD_THUMBNAIL", AmazonS3Util.s3.getUrl("hankki-s3","community/"+category+"/"+
                    insertDate+boardTitle+"/"+0).toString());
        }
        return body;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(boardContent);
        dest.writeString(regionFirst);
        dest.writeString(regionSecond);
        dest.writeString(regionThird);
        dest.writeString(insertDate);
        dest.writeString(boardTitle);
        dest.writeParcelable(userProfile, flags);
        dest.writeString(userLatitude);
        dest.writeString(userLongitude);
        dest.writeTypedList(images);
        dest.writeString(thumbnailUrl);
        dest.writeString(category);
        dest.writeInt(likeCount);
        dest.writeInt(commentCount);
        dest.writeInt(boardSeq);
        dest.writeInt(userSeq);
        dest.writeString(delYN);
        dest.writeInt(distance);
        dest.writeString(userHashId);
        dest.writeString(relativeTime);
        dest.writeInt(imgCount);
    }

    public String calculateTime(String date) throws ParseException {
        String relativeTime;
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        Date date1 = format.parse(date);
        long time1 = date1.getTime();
        absoluteTime = System.currentTimeMillis() - time1;
        int second = (int) (absoluteTime)/1000;
        if(second > 60){
            if(second > 3600){
                if(second > 86400){
                    relativeTime = (int) second / 86400 + "일 전";
                } else {
                    relativeTime = String.valueOf((int) second / 3600) + "시간 전";
                }
            } else {
                relativeTime = String.valueOf((int) second / 60) + "분 전";
            }
        } else {
            relativeTime = "방금 전";
        }
        return relativeTime;
    }

    public int calculateDistance(Context context, double lat, double lon){
        Location myLoc = new Location("myLoc");
        myLoc.setLatitude(Double.parseDouble(AES256Util.aesDecode(PreferenceManager.getString(context, "Latitude"))));
        myLoc.setLongitude(Double.parseDouble(AES256Util.aesDecode(PreferenceManager.getString(context, "Longitude"))));

        Location targetLoc = new Location("targetLoc");
        targetLoc.setLatitude(lat);
        targetLoc.setLongitude(lon);

        float distance = myLoc.distanceTo(targetLoc);
        return (int) distance;
    }
}
