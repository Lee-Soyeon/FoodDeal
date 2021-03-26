package com.hankki.fooddeal.ux.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.hankki.fooddeal.R;
import com.hankki.fooddeal.data.PurchaseItem;

public class GroupPurchaseDialog extends Dialog {

    TextView sender, duration, detail;
    FrameLayout join;
    ImageView cancel;
    PurchaseItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grouppurchase_popup);
        sender = findViewById(R.id.tv_sender_value);
        sender.setText(item.getSender());
        duration = findViewById(R.id.tv_duration_value);
        duration.setText(item.getTimeToReceive());
        detail = findViewById(R.id.tv_detail_value);
        detail.setText(item.getDetailInfo());
        cancel = findViewById(R.id.iv_cancel);
        join = findViewById(R.id.clickable_join);
        cancel.setOnClickListener(v -> {
            dismiss();
        });

    }

    public GroupPurchaseDialog(@NonNull Context context, PurchaseItem purchaseItem) {
        super(context);
        item = purchaseItem;
    }
}
