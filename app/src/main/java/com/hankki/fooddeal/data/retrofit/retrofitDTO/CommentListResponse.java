package com.hankki.fooddeal.data.retrofit.retrofitDTO;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class CommentListResponse {
    @SerializedName("responseCode")
    private int responseCode;
    @SerializedName("responseMsg")
    private String responseMsg;
    @SerializedName("boardCommentList")
    List<CommentResponse> boardCommentList;

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    public List<CommentResponse> getBoardCommentList() {
        return boardCommentList;
    }

    public class CommentResponse {
        @SerializedName("BOARD_SEQ")
        private int boardSeq;
        @SerializedName("USER_HASH_ID")
        private String userHashId;
        @SerializedName("COMMENT_CONTENT")
        private String commentContent;
        @SerializedName("INSERT_DATE")
        private String insertDate;
        @SerializedName("PARENT_COMMENT_SEQ")
        private int parentCommentSeq;
        @SerializedName("COMMENT_SEQ")
        private int commentSeq;
        @SerializedName("DEL_YN")
        private String delYN;
        @SerializedName("PARENT_DEL_YN")
        private String parentDelYN;

        public int getBoardSeq() {
            return boardSeq;
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

        public int getParentCommentSeq() {
            return parentCommentSeq;
        }

        public int getCommentSeq() {
            return commentSeq;
        }

        public String getDelYN() {
            return delYN;
        }

        public String getParentDelYN() {
            return parentDelYN;
        }

        public void setBoardSeq(int boardSeq) {
            this.boardSeq = boardSeq;
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

        public void setParentCommentSeq(int parentCommentSeq) {
            this.parentCommentSeq = parentCommentSeq;
        }

        public void setCommentSeq(int commentSeq) {
            this.commentSeq = commentSeq;
        }

        public void setDelYN(String delYN) {
            this.delYN = delYN;
        }
    }
}
