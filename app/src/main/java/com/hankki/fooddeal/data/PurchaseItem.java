package com.hankki.fooddeal.data;

public class PurchaseItem {
    String title;
    String imageUrl;
    String sender;
    String timeToReceive;
    String detailInfo;
    String relativeTime;
    int distance;
    int absoluteTime;
    int originPrice;
    int hotPrice;
    int joinNum;
    int allNum;

    public PurchaseItem(){}

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getOriginPrice() {
        return originPrice;
    }

    public int getHotPrice() {
        return hotPrice;
    }

    public int getJoinNum() {
        return joinNum;
    }

    public int getAllNum() {
        return allNum;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setOriginPrice(int originPrice) {
        this.originPrice = originPrice;
    }

    public void setHotPrice(int hotPrice) {
        this.hotPrice = hotPrice;
    }

    public void setJoinNum(int joinNum) {
        this.joinNum = joinNum;
    }

    public void setAllNum(int allNum) {
        this.allNum = allNum;
    }

    public String getSender() {
        return sender;
    }

    public String getTimeToReceive() {
        return timeToReceive;
    }

    public String getDetailInfo() {
        return detailInfo;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setTimeToReceive(String timeToReceive) {
        this.timeToReceive = timeToReceive;
    }

    public void setDetailInfo(String detailInfo) {
        this.detailInfo = detailInfo;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public void setAbsoluteTime(int absoluteTime) {
        this.absoluteTime = absoluteTime;
    }

    public String getRelativeTime() {
        calculateTime(absoluteTime);
        return relativeTime;
    }

    public void calculateTime(int absoluteTime){
        int time = (int) absoluteTime/1000;
        if(time > 60){
            if(time > 3600){
                if(time > 86400){
                    relativeTime = (int) time/86400 + "일 전";
                } else {
                    relativeTime = String.valueOf((int) time / 3600) + "시간 전";
                }
            } else {
                relativeTime = String.valueOf((int)time/60) + "분 전";
            }
        } else {
            relativeTime = "방금 전";
        }
    }
}
