package com.lostandfound.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.lostandfound.adapters.MessageAdapter;
import com.lostandfound.databinding.ActivityChatBinding;
import com.lostandfound.models.Message;
import com.lostandfound.utils.Constants;
import com.lostandfound.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Real-time chat between two users.
 */
public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding  binding;
    private FirebaseHelper       firebase;
    private MessageAdapter       adapter;
    private ListenerRegistration messagesListener;

    private String chatId;
    private String currentUserId;
    private String otherUserId;

    private final List<Message> messages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding       = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebase      = FirebaseHelper.getInstance();
        currentUserId = firebase.getCurrentUserId();

        chatId      = getIntent().getStringExtra(Constants.EXTRA_CHAT_ID);
        otherUserId = getIntent().getStringExtra(Constants.EXTRA_USER_ID);
        String otherUserName = getIntent().getStringExtra(Constants.EXTRA_USER_NAME);

        if (chatId == null || otherUserId == null) {
            finish();
            return;
        }

        binding.toolbarChat.setTitle(otherUserName != null ? otherUserName : "Chat");
        binding.toolbarChat.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();
        setupMessageInput();
        attachMessagesListener();
    }

    private void setupRecyclerView() {
        adapter = new MessageAdapter(messages, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.recyclerMessages.setLayoutManager(layoutManager);
        binding.recyclerMessages.setAdapter(adapter);
    }

    private void attachMessagesListener() {
        messagesListener = firebase.getDb()
                .collection(Constants.COLLECTION_CHATS)
                .document(chatId)
                .collection(Constants.COLLECTION_MESSAGES)
                .orderBy(Constants.FIELD_TIMESTAMP, Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    messages.clear();
                    for (var doc : snapshots.getDocuments()) {
                        Message msg = doc.toObject(Message.class);
                        if (msg != null) messages.add(msg);
                    }
                    adapter.notifyDataSetChanged();
                    if (!messages.isEmpty()) {
                        binding.recyclerMessages.smoothScrollToPosition(messages.size() - 1);
                    }
                });
    }

    private void setupMessageInput() {
        binding.btnSend.setOnClickListener(v -> sendMessage());

        binding.etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void sendMessage() {
        String text = binding.etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        binding.etMessage.setText("");

        Message message = new Message(chatId, currentUserId, otherUserId, text);

        firebase.getDb()
                .collection(Constants.COLLECTION_CHATS)
                .document(chatId)
                .collection(Constants.COLLECTION_MESSAGES)
                .add(message);

        java.util.Map<String, Object> chatMeta = new java.util.HashMap<>();
        chatMeta.put("chatId",      chatId);
        chatMeta.put("senderId",    currentUserId);
        chatMeta.put("receiverId",  otherUserId);
        chatMeta.put("lastMessage", text);
        chatMeta.put(Constants.FIELD_TIMESTAMP,
                com.google.firebase.firestore.FieldValue.serverTimestamp());

        firebase.getDb()
                .collection(Constants.COLLECTION_CHATS)
                .document(chatId)
                .set(chatMeta,
                     com.google.firebase.firestore.SetOptions.merge());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null) messagesListener.remove();
    }
}
