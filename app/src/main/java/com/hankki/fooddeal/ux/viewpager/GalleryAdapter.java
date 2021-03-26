package com.hankki.fooddeal.ux.viewpager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.hankki.fooddeal.R;
import com.hankki.fooddeal.amazon.AmazonS3Util;
import com.hankki.fooddeal.data.PostItem;
import com.hankki.fooddeal.ux.dialog.GalleryDialog;

import java.util.ArrayList;

public class GalleryAdapter extends PagerAdapter {

    Context context;
    PostItem item;

    public GalleryAdapter(Context context, PostItem item){
        this.context = context;
        this.item = item;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View page = inflater.inflate(R.layout.gallery_image,null);
        ImageView iv = page.findViewById(R.id.iv_gallary);
        String uri = AmazonS3Util.s3.getUrl("hankki-s3","community/"+
                item.getCategory()+"/"+item.getInsertDate()+item.getBoardTitle()+"/"+position).toString();
        Glide.with(context)
                .load(uri)
                .into(iv);
        container.addView(page,0);

        iv.setOnClickListener(v -> {
            GalleryDialog galleryDialog = new GalleryDialog(context, uri);
            galleryDialog.setCanceledOnTouchOutside(true);
            galleryDialog.getWindow().setBackgroundDrawable(new ColorDrawable(context.getColor(R.color.grey_800)));
            galleryDialog.show();
        });

        return page;
    }

    @Override
    public int getCount() {
        return item.getImgCount();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return (view==object);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}
