package com.hankki.fooddeal.ux.recyclerview;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.hankki.fooddeal.R;
import com.hankki.fooddeal.amazon.AmazonS3Util;
import com.hankki.fooddeal.data.CommentItem;
import com.hankki.fooddeal.data.retrofit.BoardController;
import com.hankki.fooddeal.data.security.AES256Util;
import com.hankki.fooddeal.ui.home.community.Community_detail;


import java.util.ArrayList;
import java.util.HashMap;

public class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder> {
    ArrayList<CommentItem> commentItems;
    ArrayList<CommentItem> parentItems;
    CommentViewHolder viewHolder;
    Context context;
    boolean isMyPage;
    HashMap<Integer, ArrayList<CommentItem>> childCommentList;

    public CommentAdapter(ArrayList<CommentItem> commentItems){
        this.commentItems = commentItems;

    }

    public void setChildCommentList(){
        parentItems = new ArrayList<>();
        childCommentList = new HashMap<>();
        for(CommentItem item : commentItems){
            int parentSeq = item.getParentCommentSeq();
            if(parentSeq != 0){
                if(childCommentList.get(parentSeq) == null){
                    childCommentList.put(parentSeq, new ArrayList<>());
                    childCommentList.get(parentSeq).add(item);
                } else {
                    childCommentList.get(parentSeq).add(item);
                }
            } else {
                parentItems.add(item);
            }
        }
    }

    public void setContext(Context context){
        this.context = context;
    }

    public void setIsMyPage(boolean isMyPage){
        this.isMyPage = isMyPage;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(),R.layout.comment_item,null);
        viewHolder = new CommentViewHolder(view, "Parent");
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentItem item = parentItems.get(position);

        holder.tv_username.setText(AES256Util.aesDecode(item.getUserHashId()));
        holder.tv_message.setText(item.getCommentContent());
        holder.tv_time.setText(item.getRelativeTime());

//        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users")
//                .document(item.getUserHashId());
//        documentReference
//                .get()
//                .addOnCompleteListener(task -> {
//                    DocumentSnapshot snapshot = task.getResult();
//                    if(!snapshot.get("userPhotoUri").equals("")) {
//
//                        Glide
//                                .with(context)
//                                .load(snapshot.get("userPhotoUri"))
//                                .into(holder.iv_profile);
//                    }
//                });
        String uid = item.getUserHashId();
        Glide.with(context).load(AmazonS3Util.s3.getUrl("hankki-s3","profile/"+uid).toString())
                .error(Glide.with(context).load(R.drawable.ic_group_60dp))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(holder.iv_profile);

        holder.tv_reply.setOnClickListener(v -> {

            holder.tv_reply.setTextColor(context.getResources().getColor(R.color.original_primary));
            ((Community_detail) context).writeChildComment(item);

        });

        if(FirebaseAuth.getInstance().getCurrentUser()!=null) {
            if (!item.getUserHashId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                holder.tv_btn_delete.setVisibility(View.GONE);
            } else {
                holder.tv_btn_delete.setOnClickListener(v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("댓글을 삭제하시겠습니까?");
                    builder.setPositiveButton("네", ((dialog, which) -> {
                        if (BoardController.commentDelete(context, item)) {
                            Toast.makeText(context, "댓글을 삭제했습니다.", Toast.LENGTH_SHORT).show();
                            holder.commentView.setVisibility(View.GONE);
                            holder.commentView.getLayoutParams().height = 0;
                        } else {
                            Toast.makeText(context, "실패!", Toast.LENGTH_SHORT).show();
                        }
                    })).setNegativeButton("아니오", (dialog, which) -> {
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                });
            }
        }

        if(childCommentList.get(item.getCommentSeq())!=null &&
                childCommentList.get(item.getCommentSeq()).size()>0){
            ChildCommentAdapter adapter = new ChildCommentAdapter(context, childCommentList.get(item.getCommentSeq()));
            holder.rl_comment.setLayoutManager(new LinearLayoutManager(context){
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            });
            holder.rl_comment.setAdapter(adapter);
        }
    }

    @Override
    public int getItemCount() {
        return parentItems.size();
    }
}
