package com.hankki.fooddeal.ui.home;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hankki.fooddeal.R;
import com.hankki.fooddeal.data.PreferenceManager;
import com.hankki.fooddeal.ui.MainActivity;
import com.hankki.fooddeal.ui.address.AddressActivity;
import com.hankki.fooddeal.ui.home.community.ExchangeAndShare;
import com.hankki.fooddeal.ui.home.community.FreeCommunity;
import com.hankki.fooddeal.ui.home.community.RecipeShare;
import com.hankki.fooddeal.ui.map.MapActivity;
import com.hankki.fooddeal.ux.dialog.HomeLocationDialog;
import com.hankki.fooddeal.ux.viewpager.viewPagerAdapter;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.concurrent.Callable;
import java.util.zip.Inflater;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**홈 화면*/
public class HomeFragment extends Fragment {
    ViewPager2 viewpager;
    Fragment[] fragments = new Fragment[3];
    TabLayout tabLayout;
    viewPagerAdapter viewPagerAdapter;
    View view;
    SlidingUpPanelLayout main_panel;

    Button btn_search;
    AppBarLayout ctl_home;
    TextView tv_location;
    Button btn_filter;
    Button btn_location;
    Button btn_map;
    View bottomTrickView;

    Disposable disposable;

    ProgressBar progressBar;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState){

        view = inflater.inflate(R.layout.fragment_home, container, false);
        btn_search = view.findViewById(R.id.btn_search);
        btn_filter = view.findViewById(R.id.btn_filter);
        btn_location = view.findViewById(R.id.btn_location);
        btn_map = view.findViewById(R.id.btn_map);
        tv_location = view.findViewById(R.id.tv_location);
        progressBar = view.findViewById(R.id.customDialog_progressBar);
        main_panel = view.findViewById(R.id.main_panel);
        main_panel.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        bottomTrickView = view.findViewById(R.id.bottomView);
        bottomLayoutTrick();
        setFragments();
        setLocation();
        setViewPager();
        setTabLayout();
        setMapButtonOnClickListener();
//        filterButtonClickListener();

        disposable = Observable.fromCallable(new Callable<Object>() {
            @Override
            public Object call() throws Exception { return false; }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object result) throws Exception {
                        tv_location.setText(PreferenceManager.getString(getContext(),"region3Depth"));
                        Log.d("########", "여기 실행!");
                        Log.d("########", PreferenceManager.getString(getContext(),"region3Depth"));
                        disposable.dispose();
                    }
                });


        /* @TODO 앱 새로 깔았을 때 동이 바로 뜨지 않는 문제 */
        setLocation();

        return view;
    }

    public void bottomLayoutTrick(){
        main_panel.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }
            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if(newState== SlidingUpPanelLayout.PanelState.EXPANDED){
                    bottomTrickView.setVisibility(View.VISIBLE);
                } else {
                    bottomTrickView.setVisibility(View.GONE);
                }
            }
        });
    }

    public void setMapButtonOnClickListener() {
        btn_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Intent intent = new Intent(getActivity(), MapActivity.class);
                progressBar.setVisibility(View.VISIBLE);
                disposable = Observable.fromCallable(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        intent.putParcelableArrayListExtra("Items",
                                ((ExchangeAndShare)getChildFragmentManager().getFragments().get(0)).getMapPostItems());

                        return false;
                    }
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Object>() {
                            @Override
                            public void accept(Object result) throws Exception {
//                                disposable.dispose();
                                startActivity(intent);
                                progressBar.setVisibility(View.GONE);
                                requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            }
                        });
            }
        });
    }

    public void setLocation(){
        tv_location = view.findViewById(R.id.tv_location);

        btn_location.setOnClickListener(v -> {
            HomeLocationDialog dialog = new HomeLocationDialog(getContext());
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        });
        tv_location.setOnClickListener(v -> {
            HomeLocationDialog dialog = new HomeLocationDialog(getContext());
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        });

    }

    /**탭으로 구성할 Fragments 리스트*/
    public void setFragments(){
        fragments[0] = new ExchangeAndShare();
        fragments[1] = new RecipeShare();
        fragments[2] = new FreeCommunity();
    }

    /**View Pager -> ux.viewpager.viewPagerAdapter class*/
    public void setViewPager(){
        viewPagerAdapter = new viewPagerAdapter(this,fragments);
        viewpager = view.findViewById(R.id.vp_home);
        ctl_home = view.findViewById(R.id.ctl_home);
        viewpager.setAdapter(viewPagerAdapter);
    }

    /**상단 탭 바에 나타낼 Title 적용*/
    public void setTabLayout(){
        /**이부분은 따로 String Class 정의해서 사용하는 것이 나을 듯*/
        String[] names = new String[]{"식재공유","레시피","자유"};
        tabLayout = view.findViewById(R.id.tl_home);
        new TabLayoutMediator(tabLayout, viewpager,
                (tab, position) -> tab.setText(names[position])
        ).attach();
    }
}
