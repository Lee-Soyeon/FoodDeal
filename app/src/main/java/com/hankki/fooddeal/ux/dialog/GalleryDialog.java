package com.hankki.fooddeal.ux.dialog;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hankki.fooddeal.R;

public class GalleryDialog extends Dialog {

    Context context;
    String uri;

    public GalleryDialog(@NonNull Context context, String uri) {
        super(context);
        this.context = context;
        this.uri = uri;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_image);
        ImageView image = findViewById(R.id.iv_gallery);
        image.setScaleType(ImageView.ScaleType.FIT_XY);
        Glide.with(context)
                .load(uri)
                .into(image);
    }
}
