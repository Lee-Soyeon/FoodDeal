package com.hankki.fooddeal.data.retrofit;

import com.hankki.fooddeal.data.retrofit.retrofitDTO.BoardListResponse;
import com.hankki.fooddeal.data.retrofit.retrofitDTO.CommentListResponse;
import com.hankki.fooddeal.data.retrofit.retrofitDTO.MemberResponse;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface APIInterface {
    @Headers("Content-Type: application/json")
    @GET("member/autoLogin")
    Call<MemberResponse> autoLogin(@Query("USER_TOKEN") String userToken);

    @Headers("Content-Type: application/json")
    @GET("member/login")
    Call<MemberResponse> login(@Query("USER_HASH_ID") String userHashId, @Query("USER_HASH_PW") String userHashPw);

    @Headers("Content-Type: application/json")
    @POST("member/register")
    Call<MemberResponse> register(@Body HashMap<String, String> body);

    @Headers("Content-Type: application/json")
    @POST("member/checkPhoneNo")
    Call<MemberResponse> checkPhoneNo(@Body HashMap<String, String> body);

    @Headers("Content-Type: application/json")
    @POST("member/checkDupID")
    Call<MemberResponse> checkDupID(@Body HashMap<String, String> body);

    @Headers({
            "Content-Type: application/json",
            "Authorization: KakaoAK 5584ccb6bce16722991e3e4d5a0b0dbe"
    })
    @GET("v2/local/search/address")
    Call<ResponseBody> getAddress(@Query("query") String address);

    @Headers({
            "Content-Type: application/json",
            "Authorization: KakaoAK 5584ccb6bce16722991e3e4d5a0b0dbe"
    })
    @GET("v2/local/geo/coord2regioncode")
    Call<ResponseBody> getCurrentAddress(@Query("x") Double x, @Query("y") Double y);


    @Headers("Content-Type: application/json")
    @POST("board/write")
    Call<MemberResponse> boardWrite(@Body HashMap<String, String> body);

    @Headers("Content-Type: application/json")
    @POST("board/address/search")
    Call<BoardListResponse> boardSearch(@Body HashMap<String, String> body);

    @Headers("Content-Type: application/json")
    @PATCH("board/revise")
    Call<MemberResponse> boardRevise(@Body HashMap<String, String> body);

    @Headers("Content-Type: application/json")
    @POST("board/comment/write")
    Call<MemberResponse> commentWrite(@Body HashMap<String, String> body);

    @Headers("Content-Type: application/json")
    @PATCH("board/comment/revise")
    Call<MemberResponse> commentRevise(@Body HashMap<String, String> body);

    @Headers("Content-Type: application/json")
    @GET("board/list")
    Call<BoardListResponse> getBoardList(@Query("REGION_1DEPTH_NAME") String regionFirst,
                                         @Query("REGION_2DEPTH_NAME") String regionSecond,
                                         @Query("REGION_3DEPTH_NAME") String regionThird,
                                         @Query("BOARD_CODE_SORT")String boardCode);

    @Headers("Content-Type: application/json")
    @GET("board/comment/list")
    Call<CommentListResponse> getBoardCommentList(@Query("BOARD_SEQ")int boardSeq);

    @Headers("Content-Type: application/json")
    @PATCH("board/delete")
    Call<MemberResponse> boardDelete(@Body HashMap<String,String> body);

    @Headers("Content-Type: application/json")
    @PATCH("board/comment/delete")
    Call<MemberResponse> commentDelete(@Body HashMap<String, String> body);

    @Headers("Content-Type: application/json")
    @POST("board/like/plus")
    Call<MemberResponse> boardLikePlus(@Body HashMap<String, String> body);

    @Headers("Content-Type: application/json")
    @PATCH("board/like/minus")
    Call<MemberResponse> boardLikeMinus(@Body HashMap<String, String> body);

    @Headers("Content-Type: application/json")
    @GET("board/writeList")
    Call<BoardListResponse> getBoardWriteList(@Query("USER_TOKEN")String userToken);

    @Headers("Content-Type: application/json")
    @GET("board/like/list")
    Call<BoardListResponse> getBoardLikeList(@Query("USER_TOKEN")String userToken);
}