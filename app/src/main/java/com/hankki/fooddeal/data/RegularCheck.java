package com.hankki.fooddeal.data;

public class RegularCheck {
    // 입력받은 휴대폰번호의 정규식 여부 체크
    public static boolean isRegularPhoneNo(String phoneNo) {
        String regExp = "^01(?:0|1|[6-9])[.-]?(\\d{3}|\\d{4})[.-]?(\\d{4})$";
        return phoneNo.matches(regExp);
    }
    // 입력받은 비밀번호 안정성 체크
    // 8~20 자리의 최소 1개의 숫자, 특수문자, 영문자를 포함하고 공백 미포함
    public static boolean isRegularPassword(String password) {
        String regExp = "^(?=.*[0-9])(?=.*[a-zA-z])(?=.*\\W)(?=\\S+$).{8,20}$";
        return password.matches(regExp);
    }
    // 입력받은 이메일 주소의 정규식 여부 체크
    public static boolean isRegularEmail(String email) {
        String regExp = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
        return email.matches(regExp);
    }
}