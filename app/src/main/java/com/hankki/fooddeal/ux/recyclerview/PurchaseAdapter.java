package com.hankki.fooddeal.ux.recyclerview;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hankki.fooddeal.R;
import com.hankki.fooddeal.data.PurchaseItem;
import com.hankki.fooddeal.ux.dialog.GroupPurchaseDialog;

import java.util.ArrayList;

public class PurchaseAdapter extends RecyclerView.Adapter<PurchaseAdapter.PurchaseViewHolder>{

    ArrayList<PurchaseItem> items;
    Context context;
    PurchaseViewHolder holder;

    public PurchaseAdapter(Context context, ArrayList<PurchaseItem> purchaseItems){
        items = purchaseItems;
        this.context = context;
    }

    @NonNull
    @Override
    public PurchaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.item_grouppurchase, null);
        holder = new PurchaseViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PurchaseViewHolder holder, int position) {
        PurchaseItem item = items.get(position);
        holder.tv_purchase.setText(item.getTitle());
        holder.tv_all_num.setText(String.valueOf(item.getAllNum())+"명 모집");
        holder.tv_join_num.setText(String.valueOf(item.getJoinNum())+"명 참여");
        holder.tv_hot_price.setText("딜가 "+String.valueOf(item.getHotPrice())+"원");
        holder.tv_origin_price.setText(String.valueOf(item.getOriginPrice())+"원");
        holder.time.setText(item.getRelativeTime());
        holder.distance.setText(item.getDistance()+"m");
        Glide.with(context)
                .load(item.getImageUrl())
                .into(holder.iv_purchase);
        holder.iv_purchase.setScaleType(ImageView.ScaleType.FIT_XY);
        holder.iv_purchase.setBackgroundResource(R.drawable.background_card_image);
        holder.iv_purchase.setClipToOutline(true);
        holder.item_view.setOnClickListener(v -> {
            GroupPurchaseDialog dialog = new GroupPurchaseDialog(context, item);
            dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }



    public class PurchaseViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_purchase;
        TextView tv_purchase, tv_hot_price, tv_origin_price, tv_join_num, tv_all_num, time, distance;
        LinearLayout item_view;

        public PurchaseViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_purchase = itemView.findViewById(R.id.iv_purchase);
            tv_purchase = itemView.findViewById(R.id.tv_purchase);
            tv_hot_price = itemView.findViewById(R.id.tv_hotprice);
            tv_origin_price = itemView.findViewById(R.id.tv_originprice);
            tv_join_num = itemView.findViewById(R.id.tv_join_num);
            tv_all_num = itemView.findViewById(R.id.tv_all_num);
            item_view = itemView.findViewById(R.id.item_view);
            time = itemView.findViewById(R.id.tv_purchase_time);
            time.bringToFront();
            distance = itemView.findViewById(R.id.tv_purchase_distance);
            distance.bringToFront();
        }
    }
}
