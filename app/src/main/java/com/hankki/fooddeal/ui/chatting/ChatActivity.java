package com.hankki.fooddeal.ui.chatting;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.hankki.fooddeal.R;
import com.hankki.fooddeal.amazon.AmazonS3Util;
import com.hankki.fooddeal.data.security.AES256Util;
import com.hankki.fooddeal.ui.chatting.chatDTO.ChatModel;
import com.hankki.fooddeal.ui.chatting.chatDTO.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 채팅방 상세화면
 */
// TODO 채팅방 다인 처리 및 URL 처리
public class ChatActivity extends AppCompatActivity {
    private Button sendBtn;
    private EditText msg_input;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter mAdapter;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private SimpleDateFormat dateFormatDay = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat dateFormatHour = new SimpleDateFormat("aa hh:mm");
    private String roomId, roomTitle, uid, otherUserPhotoUrl = null;
    private Integer userTotal;
    private View toolbar;
    private TextView toolbar_title;
    private ImageView back_button;

    private ListenerRegistration messageListenerRegistration;

    private LinearLayoutManager linearLayoutManager;
    private FirebaseFirestore firestore;

    private ArrayList<String> userList = new ArrayList<>();
    private HashMap<String, String> userUrlMap = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        View view = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (view != null) {
                view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                getWindow().setStatusBarColor(getResources().getColor(R.color.grey_100));
            }
        }

        if (getIntent() != null) {
            roomId = getIntent().getStringExtra("roomID");
            roomTitle = getIntent().getStringExtra("roomTitle"); // 채팅방의 이름으로 쓸 게시글 타이틀
//            userTotal = getIntent().getIntExtra("userTotal", -1);
            // 각 채팅마다 안 읽은 사람을 표시하기 위해 필요한 건데 채팅을 하다가 새로운 사람이 들어온 경우를
            // 처리못하기 때문에 메시지 리스너에서 userTotal을 계속 처리해줘야할듯
//            otherUID = getIntent().getStringExtra("otherUID");
            userList.addAll(Objects.requireNonNull(getIntent().getStringArrayListExtra("userList")));
            userTotal = userList.size();
        }

        uid = AES256Util.aesDecode(FirebaseAuth.getInstance().getCurrentUser().getUid());
        firestore = FirebaseFirestore.getInstance();
//        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
//                .setPersistenceEnabled(false)
//                .build();
//        firestore.setFirestoreSettings(settings);

        userList.remove(uid);
        for(int i=0;i<userList.size();i++) {
            otherUserPhotoUrl = AmazonS3Util
                    .s3
                    .getUrl("hankki-s3","profile/"+AES256Util.aesEncode(userList.get(i))).toString();
            userUrlMap.put(userList.get(i), otherUserPhotoUrl);
        }

        msg_input = findViewById(R.id.msg_input);
        sendBtn = findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = msg_input.getText().toString();
                sendMessage(msg, "0", null);
                msg_input.setText("");
            }
        });

        toolbar = findViewById(R.id.toolbar);
        toolbar_title = toolbar.findViewById(R.id.toolbar_title);
        toolbar_title.setText(roomTitle);
        back_button = toolbar.findViewById(R.id.back_button);
        back_button.setOnClickListener(v -> {
            onBackPressed();
        });


        recyclerView = findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v,
                                       int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (mAdapter != null & bottom < oldBottom) {
                    final int lastAdapterItem = mAdapter.getItemCount() - 1;
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            int recyclerViewPositionOffset = -1000000;
                            View bottomView = linearLayoutManager.findViewByPosition(lastAdapterItem);
                            if (bottomView != null) {
                                recyclerViewPositionOffset = -bottomView.getHeight();
                            }
                            linearLayoutManager.scrollToPositionWithOffset(lastAdapterItem, recyclerViewPositionOffset);
                        }
                    });
                }
            }
        });

        mAdapter = new RecyclerViewAdapter();
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    private void sendMessage(final String msg, String msgType, final ChatModel.FileInfo fileInfo) {
//        sendBtn.setEnabled(false);
        Log.d("***************", "SEND");
        Date date = new Date(System.currentTimeMillis());
        /*if(fileInfo != null) {
            messages.put("filename", fileInfo.filename);
            messages.put("filesize", fileInfo.filesize);
        }*/

        final DocumentReference documentReference = firestore.collection("rooms").document(roomId);
        documentReference
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.isSuccessful()) return;

                        WriteBatch batch = firestore.batch();

                        // 내가 읽었다는걸 표시, rooms의 unreadUserCount는 내가 보내면서 읽은것으로 처리되기 때문에 건드릴필요 X
                        List<String> readUserList = new ArrayList<>();
                        readUserList.add(uid);

                        // 메시지 리스트에 들어갈 내용 정리
                        Message newMessage = new Message(uid, msg, date, msgType, readUserList);
//                        String messageDocument = date.toString() + " " + AES256Util.aesEncode(uid);
//                        batch.set(documentReference.collection("messages").document(messageDocument), newMessage);
                        batch.set(documentReference.collection("messages").document(), newMessage);
                        batch.commit();

                        // 다른 사람들의 unreadUserCountMap 추가
                        DocumentSnapshot documentSnapshot = task.getResult();
                        // int로 저장했지만 불러올때는 long으로 불러오기때문에 이렇게 처리
                        Map<String, Long> unreadUserCountMap = (Map<String, Long>) documentSnapshot.get("unreadMemberCountMap");

                        for (String key : unreadUserCountMap.keySet()) {
                            if (!uid.equals(key))
                                unreadUserCountMap.put(key, unreadUserCountMap.get(key) + 1);
                        }
                        documentSnapshot.getReference()
                                .update("unreadMemberCountMap", unreadUserCountMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("########", "Send Message Success");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                                    }
                                });

                        documentReference
                                .update("lastMessageContent", msg)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("######", "Last Message Content Update Success");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                        documentReference
                                .update("lastMessageTime", date)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("######", "Last Message Time Update Success");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                });
    }

    // 채팅방 입장시 내가 안 읽은 메시지들 읽음 처리
    private void setUnreadtoRead() {
        firestore
                .collection("rooms")
                .document(roomId)
                .collection("messages")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) return;
                        QuerySnapshot querySnapshot = task.getResult();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : querySnapshot) {
                            Message message = (Message) queryDocumentSnapshot.toObject(Message.class);

                            // 메시지 읽은 사람 리스트에 본인 추가
                            if (!message.getMessageReadUserList().contains(uid)) {
                                List<String> messageReadUserList = message.getMessageReadUserList();
                                messageReadUserList.add(uid);

                                queryDocumentSnapshot.getReference().update("messageReadUserList", messageReadUserList);
                                // unreadUserCountMap에서 이 count 값만큼 제거
                                // TODO 여기서 해당 채팅방의 자기 unreadUserCountMap을 건드리면 될듯
                            }
                        }

                        firestore.collection("rooms")
                                .document(roomId)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        DocumentSnapshot documentSnapshot = task.getResult();
                                        Map<String, Long> unreadUserCountMap = (Map<String, Long>) documentSnapshot.get("unreadMemberCountMap");

                                        for (String key : unreadUserCountMap.keySet()) {
                                            if (uid.equals(key)) unreadUserCountMap.put(key, 0L);
                                        }
                                        documentSnapshot.getReference().update("unreadMemberCountMap", unreadUserCountMap);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("########", e.toString());
                    }
                });

    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        final private RequestOptions requestOptions = new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(90));

        List<Message> messageList;
        String beforeDay = null;
        MessageViewHolder beforeViewHolder;

        // TODO 이걸 어댑터에서 추가를 해줬기 때문에 어뎁터의 내용이 바뀔때마다 setUnreadToRead가 실행되서 내가 채팅방을 보고있을때 안 읽은 사람 수가 실시간으로 바뀌지 않을까 싶은데 테스트 필요
        RecyclerViewAdapter() {
            messageList = new ArrayList<Message>();
            setUnreadtoRead();
            // TODO 여기서 메시지들에 대한 unreadUserCountMap을 봐서 setBadge를 처리해줘야함
            startListening();
        }

        public void startListening() {
            beforeDay = null;
            messageList.clear();

            CollectionReference roomRef = firestore.collection("rooms").document(roomId).collection("messages");
            messageListenerRegistration = roomRef
                    .orderBy("messageTime")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                Toast.makeText(getApplicationContext(), "check query indexing!", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Message message;
                            assert value != null;
                            for (DocumentChange change : value.getDocumentChanges()) {
                                switch (change.getType()) {
                                    case ADDED:
                                        message = change.getDocument().toObject(Message.class);
                                        if (change.getNewIndex() != change.getOldIndex()) {
                                            if (message.getMessageReadUserList().indexOf(uid) == -1) {
                                                message.getMessageReadUserList().add(uid);
                                                change.getDocument().getReference().update("messageReadUserList", message.getMessageReadUserList());
                                            }
                                            messageList.add(message);
                                            notifyItemInserted(change.getNewIndex());
//                                            setUnreadtoRead();
                                        }
                                        break;
                                    case MODIFIED:
                                        message = change.getDocument().toObject(Message.class);
                                        messageList.set(change.getOldIndex(), message);
                                        notifyItemChanged(change.getOldIndex());
//                                        setUnreadtoRead();
                                        break;
                                    case REMOVED:
                                        messageList.remove(change.getOldIndex());
                                        notifyItemRemoved(change.getOldIndex());
                                        break;
                                }
                                setUnreadtoRead();
                                recyclerView.scrollToPosition(messageList.size() - 1);
                            }
                        }
                    });
        }

        public void stopListening() {
            if (messageListenerRegistration != null) {
                messageListenerRegistration.remove();
                messageListenerRegistration = null;
            }

            messageList.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            Message message = messageList.get(position);
            if (uid.equals(message.getMessageSenderUid())) {
                switch (message.getMessageType()) {
                    case "1":
                        return R.layout.item_chatimage_right;
                    case "2":
                        return R.layout.item_chatfile_right;
                    default:
                        return R.layout.item_chatmsg_right;
                }
            } else {
                switch (message.getMessageType()) {
                    case "1":
                        return R.layout.item_chatimage_left;
                    case "2":
                        return R.layout.item_chatfile_left;
                    default:
                        return R.layout.item_chatmsg_left;
                }
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = null;
            view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            // messageType이 String 형식임을 주의!
            final MessageViewHolder messageViewHolder = (MessageViewHolder) holder;
            final Message message = messageList.get(position);

            setReadCounter(message, messageViewHolder.read_counter);


            if ("0".equals(message.getMessageType())) {                                      // text message
                messageViewHolder.msg_item.setText(message.getMessageContent());
            } /*else if ("2".equals(message.getMessageType())) {                                      // file transfer
                messageViewHolder.msg_item.setText(message.getFilename() + "\n" + message.getFilesize());
                messageViewHolder.filename = message.getFilename();
                messageViewHolder.realname = message.getMsg();
                File file = new File(rootPath + message.getFilename());
                if (file.exists()) {
                    messageViewHolder.button_item.setText("Open File");
                } else {
                    messageViewHolder.button_item.setText("Download");
                }
            } else {                                                                // image transfer
                messageViewHolder.realname = message.getMsg();
                Glide.with(getContext())
                        .load(storageReference.child("filesmall/" + message.getMsg()))
                        .apply(new RequestOptions().override(1000, 1000))
                        .into(messageViewHolder.img_item);
            }*/

            if (!uid.equals(message.getMessageSenderUid())) {
                messageViewHolder.msg_name.setText(message.getMessageSenderUid());

                Glide
                        .with(getApplicationContext())
                        .load(userUrlMap.get(message.getMessageSenderUid()))
                        .into(messageViewHolder.user_photo);

                /*if (!otherUserPhotoUrl.equals("")) {
                    Glide
                            .with(getApplicationContext())
                            .load(otherUserPhotoUrl)
                            .into(messageViewHolder.user_photo);
                    messageViewHolder.user_photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    messageViewHolder.user_photo.setClipToOutline(true);
                } else {
                    messageViewHolder.user_photo.setImageResource(R.drawable.ic_group_rec_60dp);
                    messageViewHolder.user_photo.setScaleType(ImageView.ScaleType.FIT_XY);
                    messageViewHolder.user_photo.setClipToOutline(true);
                }*/
            }
            messageViewHolder.divider.setVisibility(View.INVISIBLE);
            messageViewHolder.divider.getLayoutParams().height = 0;
            messageViewHolder.timestamp.setText("");
            if (message.getMessageTime() == null) {
                return;
            }

            String day = dateFormatDay.format(message.getMessageTime());
            String timestamp = dateFormatHour.format(message.getMessageTime());
            messageViewHolder.timestamp.setText(timestamp);

            if (position == 0) {
                messageViewHolder.divider_date.setText(day);
                messageViewHolder.divider.setVisibility(View.VISIBLE);
                messageViewHolder.divider.getLayoutParams().height = 60;
            } else {
                Message beforeMsg = messageList.get(position - 1);
                String beforeDay = dateFormatDay.format(beforeMsg.getMessageTime());

                if (!day.equals(beforeDay) && beforeDay != null) {
                    messageViewHolder.divider_date.setText(day);
                    messageViewHolder.divider.setVisibility(View.VISIBLE);
                    messageViewHolder.divider.getLayoutParams().height = 60;
                }
            }
        }

        void setReadCounter(Message message, final TextView textView) {
            int count = userTotal - message.getMessageReadUserList().size();
            if (count > 0) {
                textView.setVisibility(View.VISIBLE);
                textView.setText(String.valueOf(count));
            } else {
                textView.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return messageList.size();
        }
    }

    private class MessageViewHolder extends RecyclerView.ViewHolder {
        public ImageView user_photo;
        public TextView msg_item;
        //        public ImageView img_item;          // only item_chatimage_
        public TextView msg_name;
        public TextView timestamp;
        public TextView read_counter;
        public LinearLayout divider;
        public TextView divider_date;
//        public TextView button_item;            // only item_chatfile_
//        public LinearLayout msgLine_item;       // only item_chatfile_
        /*public String filename;
        public String realname;*/

        public MessageViewHolder(View view) {
            super(view);
            user_photo = view.findViewById(R.id.user_photo);
            msg_item = view.findViewById(R.id.msg_item);
//            img_item = view.findViewById(R.id.img_item);
            timestamp = view.findViewById(R.id.timestamp);
            msg_name = view.findViewById(R.id.msg_name);
            read_counter = view.findViewById(R.id.read_counter);
            divider = view.findViewById(R.id.divider);
            divider_date = view.findViewById(R.id.divider_date);
            /*button_item = view.findViewById(R.id.button_item);
            msgLine_item = view.findViewById(R.id.msgLine_item);        // for file
            if (msgLine_item != null) {
                msgLine_item.setOnClickListener(downloadClickListener);
            }
            if (img_item != null) {                                       // for image
                img_item.setOnClickListener(imageClickListener);
            }*/
        }

        // file download and open
        /*Button.OnClickListener downloadClickListener = new View.OnClickListener() {
            public void onClick(View view) {
                if ("Download".equals(button_item.getText())) {
                    download();
                } else {
                    openWith();
                }
            }

            public void download() {
                if (!Util9.isPermissionGranted(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    return;
                }
                showProgressDialog("Downloading File.");

                final File localFile = new File(rootPath, filename);

                storageReference.child("files/" + realname).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        button_item.setText("Open File");
                        hideProgressDialog();
                        Log.e("DirectTalk9 ", "local file created " + localFile.toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e("DirectTalk9 ", "local file not created  " + exception.toString());
                    }
                });
            }

            public void openWith() {
                File newFile = new File(rootPath + filename);
                MimeTypeMap mime = MimeTypeMap.getSingleton();
                String ext = newFile.getName().substring(newFile.getName().lastIndexOf(".") + 1);
                String type = mime.getMimeTypeFromExtension(ext);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(getContext(), getActivity().getPackageName() + ".provider", newFile);

                    List<ResolveInfo> resInfoList = getActivity().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        getActivity().grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                } else {
                    uri = Uri.fromFile(newFile);
                }
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(uri, type);//"application/vnd.android.package-archive");
                startActivity(Intent.createChooser(intent, "Your title"));
            }
        };
        // photo view
        Button.OnClickListener imageClickListener = new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ViewPagerActivity.class);
                intent.putExtra("roomID", roomID);
                intent.putExtra("realname", realname);
                startActivity(intent);
            }
        };*/
    }
}
/*


    RecyclerView recyclerView;
    EditText messageEdit;
    Button sendButton;
    ArrayList<MessageItem> messageItems;
    static HashMap<Integer,ArrayList<MessageItem>> messageMap;
    chatAdapter mAdapter;
    int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        index = getIntent().getIntExtra("index",-1);
        setMessageItems();
        bindComponents();
    }

    @Override
    protected void onStop() {
        super.onStop();
        messageMap.put(index, messageItems);
    }

    public void setMessageItems(){
        if(messageMap==null){
            messageMap = new HashMap<>();
            messageItems = new ArrayList<MessageItem>();
            messageMap.put(index,messageItems);
        } else {
            if(messageMap.get(index)==null){
                messageItems = new ArrayList<MessageItem>();
                messageMap.put(index,messageItems);
            } else {
                messageItems = messageMap.get(index);
            }
        }
    }

    public void bindComponents(){
        recyclerView = findViewById(R.id.rv_chat_detail);
        messageEdit = findViewById(R.id.et_chat_message);
        sendButton = findViewById(R.id.btn_chat_message);

        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    v.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.smoothScrollToPosition(messageItems.size());
                        }
                    }, 100);
                }
            }
        });
        */
/**
 * 리사이클러 뷰 어댑터 설정
 *//*

        setAdapter();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageEdit.getText().toString();
                messageEdit.setText("");
                String time = getTime();
                MessageItem item = new MessageItem("User",message,time);
                messageItems.add(item);
                mAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageItems.size());
                new StaticChatRoom().getChatItems().get(index).information = message;
            }
        });
    }

    public String getTime(){
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfnow = new SimpleDateFormat("HH:mm");
        String timeData = sdfnow.format(date);
        return timeData;
    }

    public void setAdapter(){
        mAdapter = new chatAdapter(messageItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));
        recyclerView.setAdapter(mAdapter);
    }


public class chatAdapter extends RecyclerView.Adapter<chatViewHolder>{
    ArrayList<MessageItem> messageItems;

    public chatAdapter(ArrayList<MessageItem> items){
        messageItems = items;
    }

    @NonNull
    @Override
    public chatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(),R.layout.message_item,null);
        chatViewHolder viewHolder = new chatViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull chatViewHolder holder, int position) {
        MessageItem item = messageItems.get(position);
        holder.userName.setText(item.getUserName());
        holder.message.setText(item.getMessage());
        holder.time.setText(item.getTime());
    }

    @Override
    public int getItemCount() {
        return messageItems.size();
    }
}

public class chatViewHolder extends RecyclerView.ViewHolder{
    TextView userName;
    TextView message;
    TextView time;

    public chatViewHolder(@NonNull View itemView) {
        super(itemView);
        userName = itemView.findViewById(R.id.user_name);
        message = itemView.findViewById(R.id.chat_message);
        time = itemView.findViewById(R.id.message_time);
    }
}*/
