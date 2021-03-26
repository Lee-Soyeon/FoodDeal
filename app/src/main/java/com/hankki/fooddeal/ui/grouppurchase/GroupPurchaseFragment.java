package com.hankki.fooddeal.ui.grouppurchase;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hankki.fooddeal.R;
import com.hankki.fooddeal.data.PreferenceManager;
import com.hankki.fooddeal.data.PurchaseItem;
import com.hankki.fooddeal.ux.recyclerview.PurchaseAdapter;

import java.util.ArrayList;

/**공동구매 화면*/
public class GroupPurchaseFragment extends Fragment {

    TextView tv_location, tv_all, tv_join, tv_my;
    Button btn_location, btn_search, btn_map, btn_filter;
    CardView cv_all, cv_join, cv_my, cv_write;
    FrameLayout fl_all, fl_join, fl_my;
    RecyclerView rv_purchase;
    PurchaseAdapter mAdapter;
    ArrayList<PurchaseItem> purchaseItems = new ArrayList<>();

    View view;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_grouppurchase, container, false);

        setContentView();
        setChipView();
        setRecyclerView();

        return view;
    }

    public void setContentView(){
        tv_location = view.findViewById(R.id.tv_location);
        tv_location.setText(PreferenceManager.getString(getContext(),"region3Depth"));
        btn_location = view.findViewById(R.id.btn_location);
        btn_search = view.findViewById(R.id.btn_search);
        btn_map = view.findViewById(R.id.btn_map);
        btn_filter = view.findViewById(R.id.btn_filter);
        cv_all = view.findViewById(R.id.cv_all);
        cv_join = view.findViewById(R.id.cv_join);
        cv_my = view.findViewById(R.id.cv_my);
        cv_write = view.findViewById(R.id.cv_post);
        rv_purchase = view.findViewById(R.id.rv_purchase);
        fl_all = view.findViewById(R.id.fl_all);
        fl_join = view.findViewById(R.id.fl_join);
        fl_my = view.findViewById(R.id.fl_my);
        tv_all = view.findViewById(R.id.tv_all);
        tv_join = view.findViewById(R.id.tv_join);
        tv_my = view.findViewById(R.id.tv_my);
    }

    public void setChipView(){
        cv_all.setOnClickListener(v -> {
            fl_all.setBackgroundResource(R.drawable.cardview_selector_2);
            tv_all.setTextColor(getResources().getColor(R.color.original_white));
            fl_join.setBackgroundResource(R.drawable.cardview_selector);
            tv_join.setTextColor(getResources().getColor(R.color.original_black));
            fl_my.setBackgroundResource(R.drawable.cardview_selector);
            tv_my.setTextColor(getResources().getColor(R.color.original_black));
        });
        cv_join.setOnClickListener(v -> {
            fl_join.setBackgroundResource(R.drawable.cardview_selector_2);
            tv_join.setTextColor(getResources().getColor(R.color.original_white));
            fl_all.setBackgroundResource(R.drawable.cardview_selector);
            tv_all.setTextColor(getResources().getColor(R.color.original_black));
            fl_my.setBackgroundResource(R.drawable.cardview_selector);
            tv_my.setTextColor(getResources().getColor(R.color.original_black));
        });
        cv_my.setOnClickListener(v -> {
            fl_my.setBackgroundResource(R.drawable.cardview_selector_2);
            tv_my.setTextColor(getResources().getColor(R.color.original_white));
            fl_join.setBackgroundResource(R.drawable.cardview_selector);
            tv_join.setTextColor(getResources().getColor(R.color.original_black));
            fl_all.setBackgroundResource(R.drawable.cardview_selector);
            tv_all.setTextColor(getResources().getColor(R.color.original_black));
        });
    }

    public void setRecyclerView(){
        makeItems();
        mAdapter = new PurchaseAdapter(getContext(), purchaseItems);
        rv_purchase.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL,false));
        rv_purchase.setAdapter(mAdapter);
        rv_purchase.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && cv_write.getVisibility() == View.VISIBLE) {
                    cv_write.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_out));
                    cv_write.setVisibility(View.INVISIBLE);
                } else if (dy < 0 && cv_write.getVisibility() != View.VISIBLE) {
                    cv_write.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in));
                    cv_write.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void makeItems(){
        /**테스트용 데이터*/
        PurchaseItem item1 = new PurchaseItem();
        item1.setImageUrl("https://thumbnail7.coupangcdn.com/thumbnails/remote/492x492ex/image/vendor_inventory/8476/861b7193b031ffe0a838618a4e9f1cd615ee68aec8c7d91da57cae42b068.jpg");
        item1.setAllNum(4);
        item1.setHotPrice(6900);
        item1.setJoinNum(3);
        item1.setOriginPrice(15000);
        item1.setTitle("[보배농장] 꿀맛 청송사과 2KG 5KG 10KG 공동구매");
        item1.setDetailInfo("중량 2 ~ 10 KG");
        item1.setSender("대한통운");
        item1.setTimeToReceive("3일 내 배송완료");
        item1.setAbsoluteTime(180000);
        item1.setDistance(100);

        PurchaseItem item2 = new PurchaseItem();
        item2.setImageUrl("https://thumbnail9.coupangcdn.com/thumbnails/remote/492x492ex/image/vendor_inventory/9d69/4be6b630db857401a52a9e0ada1a006d5b45aa317fcf37acf0a30b00c235.jpg");
        item2.setAllNum(4);
        item2.setHotPrice(11980);
        item2.setJoinNum(2);
        item2.setOriginPrice(21500);
        item2.setTitle("켈로그 콘푸로스트 30g X 25개 아침 식사 대용 시리얼 콘프로스트 대용량");
        item2.setDetailInfo("중량 30g X 25");
        item2.setSender("쿠팡 로켓배송");
        item2.setTimeToReceive("1일 내 배송완료");
        item2.setAbsoluteTime(360000);
        item2.setDistance(20);

        PurchaseItem item3 = new PurchaseItem();
        item3.setImageUrl("https://thumbnail6.coupangcdn.com/thumbnails/remote/492x492ex/image/product/image/vendoritem/2019/03/22/3081976223/98fb84c9-1d8a-461a-bbce-34042c4a1ed9.jpg");
        item3.setAllNum(6);
        item3.setHotPrice(19600);
        item3.setJoinNum(4);
        item3.setOriginPrice(60000);
        item3.setTitle("크라운 츄러스 스낵");
        item3.setDetailInfo("중량 2KG");
        item3.setSender("한진택배");
        item3.setTimeToReceive("2~3일 내 배송완료");
        item3.setAbsoluteTime(1581000);
        item3.setDistance(120);

        purchaseItems.add(item1);
        purchaseItems.add(item2);
        purchaseItems.add(item3);
    }
}
