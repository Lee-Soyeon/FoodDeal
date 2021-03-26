package com.hankki.fooddeal.ux.viewpager;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;


/**View Pager2 Adapter. 재활용 가능하도록 할 것
 * 필요 인자
 * Fragment: fragment 와 tab fragment 배열
 * Activity: fragment manager, lifecycle, tab fragment 배열*/
public class viewPagerAdapter extends FragmentStateAdapter {
    Fragment[] fragments;

    /**Fragment 에서 호출 시 */
    public viewPagerAdapter(Fragment fragment, Fragment[] fragments){
        super(fragment);
        this.fragments = fragments;
    }

    /**Activity 에서 호출 시*/
    public viewPagerAdapter(FragmentManager fragmentManager, Lifecycle lifecycle, Fragment[] fragments){
        super(fragmentManager, lifecycle);
        this.fragments = fragments;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position){
        Fragment fragment;
        fragment = fragments[position];
        return fragment;
    }

    @Override
    public int getItemCount(){
        return fragments.length;
    }


}