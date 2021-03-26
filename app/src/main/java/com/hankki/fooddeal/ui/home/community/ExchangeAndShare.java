package com.hankki.fooddeal.ui.home.community;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.hankki.fooddeal.R;
import com.hankki.fooddeal.data.PostItem;
import com.hankki.fooddeal.data.PreferenceManager;
import com.hankki.fooddeal.data.retrofit.BoardController;
import com.hankki.fooddeal.ui.MainActivity;
import com.hankki.fooddeal.ui.address.AddressActivity;
import com.hankki.fooddeal.ux.dialog.CustomAnimationDialog;
import com.hankki.fooddeal.ux.recyclerview.AddressAdapter;
import com.hankki.fooddeal.ux.recyclerview.SetRecyclerViewOption;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;


public class ExchangeAndShare extends Fragment {

    View view;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    CardView cv_postWrite, cv_showExchange, cv_showShare;
    FrameLayout fl_exchange, fl_share;
    Button btn_filter;
    SetRecyclerViewOption setRecyclerViewOption;
    String category = "INGREDIENT EXCHANGE";
    TextView tv_exchange_chip, tv_share_chip;
    ArrayList<PostItem> postItems;

    /**@Enum pageFrom {Main, My, Dib}*/
    String pageFrom = "Main";

    Disposable disposable;

    ProgressBar progressBar;

    public ExchangeAndShare(){}

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState){

        view = inflater.inflate(R.layout.fragment_exchange, container, false);

        progressBar = view.findViewById(R.id.customDialog_progressBar);
        progressBar.setVisibility(View.VISIBLE);

        disposable = Observable.fromCallable(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                updatePostItems();

                return false;
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object result) throws Exception {
                        if(pageFrom.equals("Main")) {
                            setShowLists();
                            setRecyclerView();
                            setPostWrite();
                        } else {
                            setMyPostOption();
                            setShowLists();
                        }
                        setRefresh();
                        filterButtonClickListener();
                        progressBar.setVisibility(View.GONE);

                        disposable.dispose();
                    }
                });

        return view;
    }

    public void setRecyclerView(){
        recyclerView = view.findViewById(R.id.rv_exchange);
        cv_postWrite = view.findViewById(R.id.cv_post);
        if(FirebaseAuth.getInstance().getCurrentUser()==null) {
            cv_postWrite.setClickable(false);
            cv_postWrite.setVisibility(View.INVISIBLE);
        }
        setRecyclerViewOption = new SetRecyclerViewOption(recyclerView, cv_postWrite,view,getContext(),R.layout.community_item);
        setRecyclerViewOption.setPostItems(postItems);
        setRecyclerViewOption.setTag("Main");
        setRecyclerViewOption.build(0);
    }

    public void setShowLists(){
        cv_showExchange = view.findViewById(R.id.cv_exchange);
        cv_showShare = view.findViewById(R.id.cv_share);
        fl_exchange = view.findViewById(R.id.fl_exchange);
        fl_share = view.findViewById(R.id.fl_share);
        tv_exchange_chip = view.findViewById(R.id.tv_exchange_chip);
        tv_share_chip = view.findViewById(R.id.tv_share_chip);
        /**교환 게시글 보이기*/
        cv_showExchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                customAnimationDialog.show();
                progressBar.setVisibility(View.VISIBLE);
                category = "INGREDIENT EXCHANGE";
                fl_exchange.setBackgroundResource(R.drawable.cardview_unselector);
                tv_exchange_chip.setTextColor(getResources().getColor(R.color.original_white));
                fl_share.setBackgroundResource(R.drawable.cardview_selector);
                tv_share_chip.setTextColor(getResources().getColor(R.color.original_black));
                /**교환 게시글 필터링*/
                disposable = Observable.fromCallable(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        updatePostItems();

                        return false;
                    }
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Object>() {
                            @Override
                            public void accept(Object result) throws Exception {
                                setRecyclerViewOption.setPostItems(postItems);
                                setRecyclerViewOption.setTag(pageFrom);
                                setRecyclerViewOption.build(0);
                                progressBar.setVisibility(View.GONE);

                                disposable.dispose();
                            }
                        });

            }
        });

        cv_showShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
//                customAnimationDialog.show();
                category = "INGREDIENT SHARE";
                fl_exchange.setBackgroundResource(R.drawable.cardview_selector);
                tv_exchange_chip.setTextColor(getResources().getColor(R.color.original_black));
                fl_share.setBackgroundResource(R.drawable.cardview_unselector);
                tv_share_chip.setTextColor(getResources().getColor(R.color.original_white));
                /**나눔 게시글 필터링*/
                disposable = Observable.fromCallable(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        updatePostItems();

                        return false;
                    }
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Object>() {
                            @Override
                            public void accept(Object result) throws Exception {
                                setRecyclerViewOption.setPostItems(postItems);
                                setRecyclerViewOption.setTag(pageFrom);
                                setRecyclerViewOption.build(0);
                                progressBar.setVisibility(View.GONE);
//                                customAnimationDialog.dismiss();
                                disposable.dispose();
                            }
                        });
            }
        });
    }

    public void setPostWrite(){

        cv_postWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**글 쓰기 버튼 클릭 이벤트*/
                Intent intent = new Intent(getContext(),PostActivity.class);
                intent.putExtra("page",0);
                intent.putExtra("mode","write");
                intent.putExtra("category",category);
                startActivity(intent);
            }
        });
    }

    public void distanceSorting(){
        BoardController.option = "distance";
        setRecyclerViewOption.sortPostItems();
    }

    public void timeSorting(){
        BoardController.option = "time";
        setRecyclerViewOption.sortPostItems();
    }

    public void fromMyPageOption(String tag){
        pageFrom = tag;
    }

    public void setMyPostOption(){
        cv_postWrite = view.findViewById(R.id.cv_post);
        cv_postWrite.setClickable(false);
        cv_postWrite.setVisibility(View.INVISIBLE);

        recyclerView = view.findViewById(R.id.rv_exchange);
        setRecyclerViewOption = new SetRecyclerViewOption(recyclerView, null,view,getContext(),R.layout.community_item);
        setRecyclerViewOption.setPostItems(postItems);
        setRecyclerViewOption.setTag(pageFrom);
        setRecyclerViewOption.build(0);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void filterButtonClickListener(){
        btn_filter = view.findViewById(R.id.btn_filter);
        btn_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu p = new PopupMenu(getContext(),v);
                ((MainActivity)MainActivity.mainContext).getMenuInflater().inflate(R.menu.menu_filter_posts,p.getMenu());
                p.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        String title = item.getTitle().toString();
                        if(title.equals("시간순 정렬")){
                            Toast.makeText(getContext(),"최근의 게시글을 먼저 보여줍니다", Toast.LENGTH_SHORT).show();
                            timeSorting();
                        } else if (title.equals("거리순 정렬")){
                            Toast.makeText(getContext(), "가까운 곳의 게시글을 먼저 보여줍니다", Toast.LENGTH_SHORT).show();
                            distanceSorting();
                        }
                        return true;
                    }
                });
                p.show();
            }
        });
    }

//    @Override
//    public void onResume(){
//        super.onResume();
//        if(pageFrom.equals("Main"))
//            setRecyclerViewOption.update();
//    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        if(pageFrom.equals("Main"))
//            setRecyclerViewOption.update();
//    }

    public ArrayList<PostItem> getPostItems(){
        return postItems;
    }

    public void updatePostItems(){
//        postItems = null;
        if(pageFrom.equals("Main")){
            postItems = BoardController.getBoardList(getContext(),category);
        } else if (pageFrom.equals("My")) {
            postItems = BoardController.getExchangeShareBoardWriteList(getContext(),category);
        } else {
            postItems = BoardController.getExchangeShareBoardLikeList(getContext(),category);
        }
    }

    /*설정한 반경에 따라서 게시글 필터링(100m, 200m 등)*/
    public void getFilteredPostItems(int distance){
        if(pageFrom.equals("Main")){
            postItems = BoardController.getBoardList(getContext(), category, distance);
        }
    }

    public ArrayList<PostItem> getMapPostItems(){
        ArrayList<PostItem> items = new ArrayList<>(postItems);
        if(category.equals("INGREDIENT EXCHANGE")) {
            items.addAll(BoardController.getBoardList(getContext(),"INGREDIENT SHARE"));
        } else {
            items.addAll(BoardController.getBoardList(getContext(),"INGREDIENT EXCHANGE"));
        }
        return items;
    }

    public void setRefresh(){
        swipeRefreshLayout = view.findViewById(R.id.srl_exchange);
        swipeRefreshLayout.setDistanceToTriggerSync(400);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            disposable = Observable.fromCallable(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    updatePostItems();

                    return false;
                }
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Object>() {
                        @Override
                        public void accept(Object result) throws Exception {
                            setRecyclerViewOption.setPostItems(postItems);
                            setRecyclerViewOption.setTag(pageFrom);
                            setRecyclerViewOption.build(0);
                            swipeRefreshLayout.setRefreshing(false);

                            disposable.dispose();
                        }
                    });
        });
    }

}
