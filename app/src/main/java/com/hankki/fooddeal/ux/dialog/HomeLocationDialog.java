package com.hankki.fooddeal.ux.dialog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hankki.fooddeal.R;
import com.hankki.fooddeal.ui.MainActivity;
import com.hankki.fooddeal.ui.address.AddressActivity;

public class HomeLocationDialog extends BottomSheetDialog {
    Context context;
    Button btn_location, btn_cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_location_dialog);
        btn_location = findViewById(R.id.btn_location);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_location.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddressActivity.class);
            context.startActivity(intent);
            dismiss();
        });
        btn_cancel.setOnClickListener(v -> {
            dismiss();
        });
    }

    public HomeLocationDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

}
