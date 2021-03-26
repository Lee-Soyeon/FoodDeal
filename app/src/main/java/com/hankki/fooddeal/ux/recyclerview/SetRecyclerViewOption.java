package com.hankki.fooddeal.ux.recyclerview;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.hankki.fooddeal.R;
import com.hankki.fooddeal.data.PostItem;
import com.hankki.fooddeal.ui.MainActivity;
import com.hankki.fooddeal.ui.home.HomeFragment;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.Collections;

/**게시판 In Activity Recycler View 옵션 설정 (공통)*/
public class SetRecyclerViewOption {
    RecyclerView mRecyclerView;
    PostAdapter postAdapter;
    View view;
    Context context;
    ArrayList<PostItem> postItems;
    CardView cv = null;
    String tag = "";
    int layout;
    int direction = RecyclerView.VERTICAL;

    public SetRecyclerViewOption(RecyclerView rv, CardView cardView, View v
                                 , Context ct, int layout){
        mRecyclerView = rv;
        cv = cardView;
        view = v;
        context = ct;
        this.layout = layout;
    }


    public void build(int page){
        setMRecyclerView(page);
        if(cv!=null)
            setCardViewAnimation();
    }

    public void setMRecyclerView(int page){
        postAdapter = new PostAdapter(context,postItems,layout);
        postAdapter.setPage(page);
        postAdapter.setTag(tag);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context, direction,false));
        mRecyclerView.setAdapter(postAdapter);
    }

    public void update(){ postAdapter.notifyDataSetChanged(); }

    public void setPostItems(ArrayList<PostItem> items){
        postItems = items;
    }

    public void setTag(String tag){
        this.tag = tag;
    }

    public void sortPostItems(){
            Collections.sort(postItems);
            postAdapter.setPostItems(postItems);
            postAdapter.notifyDataSetChanged();
    }

    public void setCardViewAnimation() {
//        if(!(mRecyclerView.canScrollVertically(-1))) {
//            cv.setVisibility(View.VISIBLE);
//            appBarLayout.setVisibility(View.VISIBLE);
//        }
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && cv.getVisibility() == View.VISIBLE) {
                    cv.setAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out));
                    cv.setVisibility(View.INVISIBLE);
                } else if (dy < 0 && cv.getVisibility() != View.VISIBLE) {
                    cv.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in));
                    cv.setVisibility(View.VISIBLE);
                }
//                if(dy > 0 && homeFragment.getPanelState()== SlidingUpPanelLayout.PanelState.EXPANDED)
//                    homeFragment.panelCollapse();
//                else if (dy < 0 ){
//                    homeFragment.panelExpand();
//                }
            }
        });
    }


    public static void expand(final View v) {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? AppBarLayout.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Expansion speed of 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Collapse speed of 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

}
