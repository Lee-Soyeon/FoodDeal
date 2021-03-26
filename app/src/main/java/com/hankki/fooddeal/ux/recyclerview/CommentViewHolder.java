package com.hankki.fooddeal.ux.recyclerview;

import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hankki.fooddeal.R;

public class CommentViewHolder extends RecyclerView.ViewHolder{
    public View commentView;
    public TextView tv_username;
    public TextView tv_message;
    public TextView tv_time;
    public TextView tv_reply, tv_btn_delete;
    public ImageView iv_profile;
    public RecyclerView rl_comment;

    public CommentViewHolder(@NonNull View itemView, String type) {
        super(itemView);
        tv_message = itemView.findViewById(R.id.tv_comment_message);
        tv_username = itemView.findViewById(R.id.tv_comment_user_name);
        tv_time = itemView.findViewById(R.id.tv_comment_time);
        iv_profile = itemView.findViewById(R.id.iv_comment_user_profile);
        tv_btn_delete = itemView.findViewById(R.id.tv_btn_delete);
        commentView = itemView.findViewById(R.id.comment_item);
        iv_profile.setBackground(new ShapeDrawable(new OvalShape()));
        iv_profile.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv_profile.setImageResource(R.drawable.ic_group_rec_60dp);
        iv_profile.setClipToOutline(true);
        iv_profile.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if(type.equals("Parent")){
            tv_reply = itemView.findViewById(R.id.tv_reply);
            rl_comment = itemView.findViewById(R.id.rl_comment_comment);
        }
    }
}
