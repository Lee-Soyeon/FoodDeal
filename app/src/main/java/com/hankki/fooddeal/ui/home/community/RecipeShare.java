package com.hankki.fooddeal.ui.home.community;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.hankki.fooddeal.R;
import com.hankki.fooddeal.data.PostItem;
import com.hankki.fooddeal.data.retrofit.BoardController;
import com.hankki.fooddeal.ux.recyclerview.SetRecyclerViewOption;

import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import java.util.ArrayList;

public class RecipeShare extends Fragment {
    View view;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    CardView cv_post;
    SetRecyclerViewOption setRecyclerViewOption;
    String category = "RECIPE";

    /**@Enum pageFrom {Main, My, Dib}*/
    String pageFrom = "Main";
    ArrayList<PostItem> postItems;

    Disposable disposable;

    ProgressBar progressBar;

    public RecipeShare(){}

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState){

        view = inflater.inflate(R.layout.fragment_recipe, container, false);

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
                            setRecyclerView();
                            setPostWrite();
                        } else {
                            setMyPostOption();
                        }
                        setRefresh();
                        progressBar.setVisibility(View.GONE);
                        disposable.dispose();
                    }
                });

        return view;
    }

    public void setRecyclerView(){
        recyclerView = view.findViewById(R.id.rv_recipe);
        cv_post = view.findViewById(R.id.cv_post);
        if(FirebaseAuth.getInstance().getCurrentUser()==null) {
            cv_post.setClickable(false);
            cv_post.setVisibility(View.INVISIBLE);
        }
        setRecyclerViewOption = new SetRecyclerViewOption(
                recyclerView, cv_post, view, getContext(), R.layout.community_item );
        setRecyclerViewOption.setPostItems(postItems);
        setRecyclerViewOption.setTag("Main");
        setRecyclerViewOption.build(1);
    }
    public void setPostWrite(){
        cv_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**글 쓰기 버튼 클릭 이벤트*/
                Intent intent = new Intent(getContext(),PostActivity.class);
                intent.putExtra("page",1);
                intent.putExtra("mode","write");
                intent.putExtra("category","RECIPE");
                startActivity(intent);
            }
        });
    }
    public void distanceSorting(){
        setRecyclerViewOption.sortPostItems();
    }

    public void fromMyPageOption(String tag){
        pageFrom = tag;
    }

    public void setMyPostOption(){
        cv_post = view.findViewById(R.id.cv_post);
        cv_post.setClickable(false);
        cv_post.setVisibility(View.INVISIBLE);

        recyclerView = view.findViewById(R.id.rv_recipe);
        setRecyclerViewOption = new SetRecyclerViewOption(recyclerView, null,view,getContext(),R.layout.community_item);
        setRecyclerViewOption.setPostItems(postItems);
        setRecyclerViewOption.setTag(pageFrom);
        setRecyclerViewOption.build(1);
    }

    public void updatePostItems(){
        postItems = null;
        if(pageFrom.equals("Main")){
            postItems = BoardController.getBoardList(getContext(), category);
        } else if (pageFrom.equals("My")){
            postItems = BoardController.getRecipeBoardWriteList(getContext());
        } else {
            postItems = BoardController.getRecipeBoardLikeList(getContext());
        }
    }

    public void setRefresh(){
        postItems = null;
        swipeRefreshLayout = view.findViewById(R.id.srl_recipe);
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
                            setRecyclerViewOption.build(1);
                            swipeRefreshLayout.setRefreshing(false);

                            disposable.dispose();
                        }
                    });
        });
    }
}
