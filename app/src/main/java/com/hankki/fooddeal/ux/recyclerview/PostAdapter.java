package com.hankki.fooddeal.ux.recyclerview;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.StorageReference;
import com.hankki.fooddeal.R;
import com.hankki.fooddeal.data.PostItem;
import com.hankki.fooddeal.ui.home.community.Community_detail;

import java.util.ArrayList;
import java.util.Collections;


/**
 * Recycler View Post Adapter
 */
public class PostAdapter extends RecyclerView.Adapter<PostViewHolder> {
    private Context mContext;
    ArrayList<PostItem> postItems;
    private int layout;
    PostViewHolder postViewHolder;
    int page;
    String tag = "Main"; // Main, My, Dib

    StorageReference ref;

    @Override
    public int getItemViewType(int position) {
        PostItem item = postItems.get(position);
        if(item.getImgCount()==0){
            return 0;
        } else {
            return 1;
        }
    }

    public PostAdapter(Context context, ArrayList<PostItem> itemList, int layout) {
        mContext = context;
        postItems = itemList;
        this.layout = layout; // inflate 할 layout 받아와야 함.
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View baseView;
        if(viewType==1) {
            baseView = View.inflate(mContext, layout, null);
        } else {
            baseView = View.inflate(mContext,R.layout.community_item2,null);
        }
        postViewHolder = new PostViewHolder(baseView);

        return postViewHolder;
    }

    /**
     * Layout 과 View Holder Binding.
     * 데이터에 따라 변수 바인딩하는 로직 추가할 것
     */
    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        PostItem item = postItems.get(position);
        setCommunityItem(holder, item);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("#########", "게시글 클릭");
                Intent intent = new Intent(holder.mView.getContext(), Community_detail.class);
                intent.putExtra("page", page);
                intent.putExtra("Tag", tag);
                intent.putExtra("item", item);
                holder.mView.getContext().startActivity(intent);
            }
        });
//        holder.btn_revise.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(holder.mView.getContext(), PostActivity.class);
//                intent.putExtra("page",page);
//                intent.putExtra("mode","revise");
//                intent.putExtra("index",position);
//                holder.mView.getContext().startActivity(intent);
//            }
//        });
    }

    /**
     * 총 게시글/채팅방 수.
     */
    @Override
    public int getItemCount() {
        return postItems.size();
    }

    public void setCommunityItem(PostViewHolder holder, PostItem item) {

        holder.mTitle.setText(item.getBoardTitle()); // 수정해야 함! 테스트용
        if(item.getDistance()<=10){
            holder.mUserLocation.setText("근처");
        } else {
            holder.mUserLocation.setText(item.getDistance() + "m");
        }
        holder.mTime.setText(item.getRelativeTime());

        // 썸네일로 쓸 내용이 있으면 표시 없으면 빈 값
        if (holder.getItemViewType()==1) {
            holder.mImage = holder.itemView.findViewById(R.id.iv_post_image);
            Glide
                    .with(mContext)
                    .load(item.getThumbnailUrl())
                    .thumbnail(0.1f)
                    .into(holder.mImage);
            holder.mImage.setScaleType(ImageView.ScaleType.FIT_XY);
            holder.mImage.setClipToOutline(true);
        }

        if (page == 0) { // 식재 나눔 교환
            /**찜 아이콘을 지우고, 댓글 부분은 찜으로 대체*/
            holder.iv_like.setImageBitmap(null);
            holder.tv_like.setText(null);
            if (item.getLikeCount() == 0) {
                holder.iv_comment.setImageBitmap(null);
                holder.tv_comment.setText(null);
            } else {
                holder.iv_comment.setImageResource(R.drawable.ic_icon_heart_off);
                holder.tv_comment.setText(String.valueOf(item.getLikeCount()));
            }
        } else { // 레시피 자유
            int like = item.getLikeCount();
            if (item.getCommentCount() > 0) {
                int comment = item.getCommentCount();

                if (like == 0 && comment == 0) { // 둘다 0
                    holder.iv_like.setImageBitmap(null);
                    holder.tv_like.setText(null);
                    holder.iv_comment.setImageBitmap(null);
                    holder.tv_comment.setText(null);
                } else if (like == 0) { // like 만 0
                    holder.iv_like.setImageBitmap(null);
                    holder.tv_like.setText(null);
                    holder.iv_comment.setImageResource(R.drawable.ic_icon_chat);
                    holder.tv_comment.setText(String.valueOf(comment));
                } else if (comment == 0) { // comment 만 0
                    holder.iv_like.setImageBitmap(null);
                    holder.tv_like.setText(null);
                    holder.iv_comment.setImageResource(R.drawable.ic_icon_heart_off);
                    holder.tv_comment.setText(String.valueOf(like));
                } else { // 둘다 양수
                    holder.iv_comment.setImageResource(R.drawable.ic_icon_chat);
                    holder.tv_comment.setText(String.valueOf(comment));
                    holder.iv_like.setImageResource(R.drawable.ic_icon_heart_off);
                    holder.tv_like.setText(String.valueOf(like));
                }
            }
        }
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void distanceFiltering(int distance) {

    }

    public void distanceSorting(int distance) {
        Collections.sort(postItems);
    }

    public void setPostItems(ArrayList<PostItem> postItems) {
        this.postItems = postItems;
    }
}
