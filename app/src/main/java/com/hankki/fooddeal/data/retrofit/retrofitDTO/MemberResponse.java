package com.hankki.fooddeal.data.retrofit.retrofitDTO;

import com.google.gson.annotations.SerializedName;

public class MemberResponse {
    @SerializedName("responseCode")
    private Integer responseCode;
    @SerializedName("responseMsg")
    private String responseMsg;
    @SerializedName("firebaseToken")
    private String firebaseToken;
    @SerializedName("userToken")
    private String userToken;

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public String getFirebaseToken() { return firebaseToken; }

    public void setFirebaseToken(String firebaseToken) { this.firebaseToken = firebaseToken; }
}
