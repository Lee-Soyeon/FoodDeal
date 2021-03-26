package com.hankki.fooddeal.ui.chatting;

import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hankki.fooddeal.R;
import com.hankki.fooddeal.amazon.AmazonS3Util;
import com.hankki.fooddeal.data.security.AES256Util;
import com.hankki.fooddeal.data.security.HashMsgUtil;
import com.hankki.fooddeal.ui.chatting.chatDTO.ChatRoomModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * 채팅 화면
 */
// TODO 사용자 추가하기 이벤트 필요
public class ChatRoomFragment extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerViewAdapter mAdapter;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a", Locale.KOREA);
    private String roomId;
    private String sUID;
    private String hostUID;

    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatroom, container, false);

        View toolbar = view.findViewById(R.id.top_toolbar);
        View paddingView1 = view.findViewById(R.id.paddingView1);
        View paddingView2 = view.findViewById(R.id.paddingView2);
        View paddingView3 = view.findViewById(R.id.paddingView3);
        paddingView1.setVisibility(View.GONE);
        paddingView2.setVisibility(View.GONE);
        paddingView3.setVisibility(View.GONE);
        ImageView backButton = toolbar.findViewById(R.id.back_button);
        backButton.setVisibility(View.GONE);
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText("채팅방");

        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            sUID = AES256Util.aesDecode(FirebaseAuth.getInstance().getCurrentUser().getUid());
        } else {
            sUID = "";
        }

        firestore = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.rv_chatroom);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        mAdapter = new RecyclerViewAdapter();
        recyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        final private RequestOptions requestOptions = new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(90));

        private ListenerRegistration chatRoomListenerRegistration;
        private List<ChatRoomModel> roomList = new ArrayList<>();

        RecyclerViewAdapter() {
            // 채팅방 리스트 업데이트
            chatRoomListenerRegistration = firestore
                    .collection("rooms")
                    .whereArrayContains("roomUserList", sUID)
                    .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                    .addSnapshotListener((value, error) -> {
                        // 여기서 다이얼로그 팝업
                        if (error != null) return;

                        roomList.clear();
                        for (final QueryDocumentSnapshot queryDocumentSnapshot : value) {
                            ChatRoomModel chatRoomModel = queryDocumentSnapshot.toObject(ChatRoomModel.class);
                            roomList.add(chatRoomModel);
                        }

                        notifyDataSetChanged();
                        // 여기서 다이얼로그 디스미스
                    });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chatroom, parent, false);
            return new RoomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            RoomViewHolder roomViewHolder = (RoomViewHolder) holder;

            final ChatRoomModel chatRoomModel = roomList.get(position);

            List<String> userList = new ArrayList<String>(chatRoomModel.getRoomUserList());
            userList.remove(sUID);
            String otherUser = userList.get(0);

            if (chatRoomModel.getRoomTitle().length() >= 12) {
                String changedTitle = chatRoomModel.getRoomTitle().substring(0, 12) + "...";
                roomViewHolder.room_title.setText(changedTitle);
            } else {
                roomViewHolder.room_title.setText(chatRoomModel.getRoomTitle());
            }

            if(chatRoomModel.getLastMessageContent() != null) {
                if(chatRoomModel.getLastMessageContent().contains("\n")) {
                    String[] splitArray = chatRoomModel.getLastMessageContent().split("\\n");
                    roomViewHolder.last_msg.setText(splitArray[0] + "...");
                } else {
                    roomViewHolder.last_msg.setText(chatRoomModel.getLastMessageContent());
                }
            }
            // 갓 생성된 채팅방이라서 메시지가 없음
            else {
                roomViewHolder.last_msg.setText("새로운 친구와 채팅을 시작해보세요!");
            }

            roomViewHolder.last_time.setText(simpleDateFormat.format(chatRoomModel.getLastMessageTime()));

            String url = AmazonS3Util.s3.getUrl("hankki-s3","profile/"+AES256Util.aesEncode(otherUser)).toString();
            Glide.with(getContext())
                    .load(url)
                    .error(R.drawable.ic_group_60dp)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(roomViewHolder.room_image);
            roomViewHolder.room_image.setAdjustViewBounds(true);
            roomViewHolder.room_image.setClipToOutline(true);
            roomViewHolder.room_image.setScaleType(ImageView.ScaleType.CENTER_CROP);

            if (chatRoomModel.getRoomUserList().size() > 2) {
                roomViewHolder.room_count.setText(chatRoomModel.getRoomUserList().size());
                roomViewHolder.room_count.setVisibility(View.VISIBLE);
            } else {
                roomViewHolder.room_count.setVisibility(View.INVISIBLE);
            }

            //noinspection ConstantConditions
            if(chatRoomModel.getUnreadMemberCountMap().get(sUID) > 0) {
                roomViewHolder.unread_count.setText(chatRoomModel.getUnreadMemberCountMap().get(sUID).toString());
                roomViewHolder.unread_count.setVisibility(View.VISIBLE);
            } else {
                roomViewHolder.unread_count.setVisibility(View.INVISIBLE);
            }

            roomViewHolder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), ChatActivity.class);
                intent.putExtra("roomID", chatRoomModel.getRoomId());
                intent.putExtra("roomTitle", chatRoomModel.getRoomTitle());
//                intent.putExtra("userTotal", chatRoomModel.getRoomUserList().size());
                intent.putStringArrayListExtra("userList", chatRoomModel.getRoomUserList());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return roomList.size();
        }

        private class RoomViewHolder extends RecyclerView.ViewHolder {
            public ImageView room_image;
            public TextView room_title;
            public TextView last_msg;
            public TextView last_time;
            public TextView room_count;
            public TextView unread_count;

            RoomViewHolder(View view) {
                super(view);
                room_image = view.findViewById(R.id.room_image);
                room_image.setClipToOutline(true);
                room_title = view.findViewById(R.id.room_title);
                last_msg = view.findViewById(R.id.last_msg);
                last_time = view.findViewById(R.id.last_time);
                room_count = view.findViewById(R.id.room_count);
                unread_count = view.findViewById(R.id.unread_count);
            }
        }

        public void stopListening() {
            if (chatRoomListenerRegistration != null) {
                chatRoomListenerRegistration.remove();
                chatRoomListenerRegistration = null;
            }

            roomList.clear();
            notifyDataSetChanged();
        }
    }

    // 이걸 게시판 상세페이지 채팅하기 버튼이나 공동구매 시작하기 버튼 이벤트로 달아주면 됨
//    private void createChattingRoom(final DocumentReference room, String roomID, List<String> userList, HashMap<String, Integer> unreadUserCountMap) {
////        String uid = AES256Util.aesDecode(FirebaseAuth.getInstance().getCurrentUser().getUid());
//
//        String roomTitle = "[공동구매] 감자 2KG 교환허쉴?";
//        // 첫 방 생성할때는 메시지가 없으므로 타임만 현재시간으로 설정, unreadUserCountMap들의 값들도 0
//        ChatRoomModel chatRoomModel = new ChatRoomModel(roomID, 3, roomTitle, userList, unreadUserCountMap, null, new Date(System.currentTimeMillis()));
//
//        room
//                .set(chatRoomModel)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if(task.isSuccessful()) {
//                            mAdapter = new RecyclerViewAdapter();
//                            recyclerView.setAdapter(mAdapter);
//                            Toast.makeText(getContext(), "성공!", Toast.LENGTH_LONG).show();
//                        }
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(getContext(), e.toString(), Toast.LENGTH_LONG).show();
//                    }
//                });
//    }
}
