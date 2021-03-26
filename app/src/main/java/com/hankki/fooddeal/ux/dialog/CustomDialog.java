package com.hankki.fooddeal.ux.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.hankki.fooddeal.R;

public class CustomDialog extends Dialog {
    TextView alertText;
    Button okButton;
    String text ;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_layout);
        alertText = findViewById(R.id.tv_alert);
        alertText.setText(text);
        okButton = findViewById(R.id.btn_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    public CustomDialog(Context context, String text) {
        super(context);
        this.mContext = context;
        this.text = text;
    }
}
