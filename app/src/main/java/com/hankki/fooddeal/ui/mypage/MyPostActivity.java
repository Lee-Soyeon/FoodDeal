package com.hankki.fooddeal.ui.mypage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hankki.fooddeal.R;
import com.hankki.fooddeal.ui.home.community.ExchangeAndShare;
import com.hankki.fooddeal.ui.home.community.FreeCommunity;
import com.hankki.fooddeal.ui.home.community.RecipeShare;
import com.hankki.fooddeal.ux.viewpager.viewPagerAdapter;

import java.util.List;

public class MyPostActivity extends AppCompatActivity {
    ViewPager2 viewpager;
    Fragment[] fragments = new Fragment[3];
    TabLayout tabLayout;
    viewPagerAdapter viewPagerAdapter;
    View toolbarView;
    TextView toolbarTextView;
    ImageView back_button;
    Intent intent;
    String tag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_post);
        intent = getIntent();
        setMyPosts();
        setFragments();
        setViewPager();
        setTabLayout();
        setFragmentOption();
    }

    public void setMyPosts(){
        toolbarView = findViewById(R.id.post_toolbar);
        toolbarTextView = toolbarView.findViewById(R.id.toolbar_title);
        back_button = toolbarView.findViewById(R.id.back_button);
        back_button.setOnClickListener(v -> {
            onBackPressed();
        });

        String mode = intent.getStringExtra("Mode");
        if(mode.equals("my_post")) {
            toolbarTextView.setText("내가 쓴 글");
            tag = "My";
        } else if (mode.equals("like")){
            toolbarTextView.setText("찜");
            tag = "Dib";
        }
    }

    public void setFragments(){
        fragments[0] = new ExchangeAndShare();
        ((ExchangeAndShare)fragments[0]).fromMyPageOption(tag);
        fragments[1] = new RecipeShare();
        ((RecipeShare)fragments[1]).fromMyPageOption(tag);
        fragments[2] = new FreeCommunity();
        ((FreeCommunity)fragments[2]).fromMyPageOption(tag);
    }

    public void setViewPager(){
        viewPagerAdapter = new viewPagerAdapter(getSupportFragmentManager(),getLifecycle(),fragments);
        viewpager = findViewById(R.id.vp_home);
        viewpager.setAdapter(viewPagerAdapter);
    }

    public void setTabLayout(){
        /**이부분은 따로 String Class 정의해서 사용하는 것이 나을 듯*/
        String[] names = new String[]{"식재공유","레시피","자유"};
        tabLayout = findViewById(R.id.tl_home);
        new TabLayoutMediator(tabLayout, viewpager,
                (tab, position) -> tab.setText(names[position])
        ).attach();
    }

    public void setFragmentOption(){

    }
}