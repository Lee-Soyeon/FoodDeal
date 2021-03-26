package com.hankki.fooddeal.data.retrofit.retrofitDTO;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


public class BoardListResponse {
    @SerializedName("responseCode")
    private int responseCode;
    @SerializedName("responseMsg")
    private String responseMsg;
    @SerializedName("boardList")
    private List<BoardResponse> boardList = new ArrayList<>();
    @SerializedName("likeList")
    private List<BoardResponse> likeList = new ArrayList<>();

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    public List<BoardResponse> getBoardList() {
        return boardList;
    }

    public List<BoardResponse> getLikeList() {
        return likeList;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }

    public void setBoardList(List<BoardResponse> boardList) {
        this.boardList = boardList;
    }

    public void setLikeList(List<BoardResponse> likeList) {
        this.likeList = likeList;
    }

    public class BoardResponse {
        @SerializedName("BOARD_SEQ")
        private int boardSeq;
        @SerializedName("BOARD_CODE_SEQ")
        private String boardCodeSeq;
        @SerializedName("USER_SEQ")
        private int userSeq;
        @SerializedName("BOARD_TITLE")
        private String boardTitle;
        @SerializedName("BOARD_CONTENT")
        private String boardContent;
        @Nullable
        @SerializedName("INSERT_DATE")
        private String insertDate;
        @SerializedName("USER_LAT")
        private String userLat;
        @SerializedName("USER_LON")
        private String userLon;
        @SerializedName("USER_HASH_ID")
        private String userHashId;
        @SerializedName("REGION_1DEPTH_NAME")
        private String regionFirst;
        @SerializedName("REGION_2DEPTH_NAME")
        private String regionSecond;
        @SerializedName("REGION_3DEPTH_NAME")
        private String regionThird;
        @SerializedName("LIKE_COUNT")
        private int likeCount;
        @SerializedName("DEL_YN")
        private String delYn;
        @SerializedName("COMMENT_COUNT")
        private int commentCount;
        @SerializedName("BOARD_IMAGE_SIZE")
        private int boardImageSize;
        @SerializedName("BOARD_THUMBNAIL")
        private String boardThumbnail;

        public void setBoardSeq(int boardSeq) {
            this.boardSeq = boardSeq;
        }

        public void setBoardCodeSeq(String boardCodeSeq) {
            this.boardCodeSeq = boardCodeSeq;
        }

        public void setUserSeq(int userSeq) {
            this.userSeq = userSeq;
        }

        public void setBoardTitle(String boardTitle) {
            this.boardTitle = boardTitle;
        }

        public void setBoardContent(String boardContent) {
            this.boardContent = boardContent;
        }

        public void setInsertDate(@Nullable String insertDate) {
            this.insertDate = insertDate;
        }

        public void setUserLat(String userLat) {
            this.userLat = userLat;
        }

        public void setUserLon(String userLon) {
            this.userLon = userLon;
        }

        public void setRegionFirst(String regionFirst) {
            this.regionFirst = regionFirst;
        }

        public void setRegionSecond(String regionSecond) {
            this.regionSecond = regionSecond;
        }

        public void setRegionThird(String regionThird) {
            this.regionThird = regionThird;
        }

        public void setLikeCount(int likeCount) {
            this.likeCount = likeCount;
        }

        public void setDelYn(String delYn) {
            this.delYn = delYn;
        }

        public void setCommentCount(int commentCount) {
            this.commentCount = commentCount;
        }

        public void setUserHashId(String userHashId) {
            this.userHashId = userHashId;
        }

        public void setBoardImageSize(int boardImageSize) {
            this.boardImageSize = boardImageSize;
        }

        public int getBoardSeq() {
            return boardSeq;
        }

        public String getBoardCodeSeq() {
            return boardCodeSeq;
        }

        public int getUserSeq() {
            return userSeq;
        }

        public String getBoardTitle() {
            return boardTitle;
        }

        public String getBoardContent() {
            return boardContent;
        }

        @Nullable
        public String getInsertDate() {
            return insertDate;
        }

        public String getUserLat() {
            return userLat;
        }

        public String getUserLon() {
            return userLon;
        }

        public String getUserHashId() {
            return userHashId;
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

        public int getLikeCount() {
            return likeCount;
        }

        public String getDelYn() {
            return delYn;
        }

        public int getCommentCount() {
            return commentCount;
        }

        public int getBoardImageSize() {
            return boardImageSize;
        }

        public String getBoardThumbnail() {
            return boardThumbnail;
        }
    }

}
