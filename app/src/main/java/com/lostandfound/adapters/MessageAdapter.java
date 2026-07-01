package com.lostandfound.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.lostandfound.databinding.ItemMessageReceivedBinding;
import com.lostandfound.databinding.ItemMessageSentBinding;
import com.lostandfound.models.Message;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Two-viewtype adapter for chat bubbles (sent vs. received).
 */
public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT     = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final List<Message> messages;
    private final String        currentUserId;

    public MessageAdapter(List<Message> messages, String currentUserId) {
        this.messages      = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        String senderId = messages.get(position).getSenderId();
        return senderId != null && senderId.equals(currentUserId)
                ? VIEW_TYPE_SENT
                : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_SENT) {
            ItemMessageSentBinding binding =
                    ItemMessageSentBinding.inflate(inflater, parent, false);
            return new SentViewHolder(binding);
        } else {
            ItemMessageReceivedBinding binding =
                    ItemMessageReceivedBinding.inflate(inflater, parent, false);
            return new ReceivedViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = messages.get(position);
        String time = formatTime(msg);

        if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).bind(msg.getText(), time);
        } else {
            ((ReceivedViewHolder) holder).bind(msg.getText(), time);
        }
    }

    @Override
    public int getItemCount() { return messages.size(); }

    private String formatTime(Message msg) {
        if (msg.getTimestamp() == null) return "";
        Date date = msg.getTimestamp().toDate();
        return new SimpleDateFormat("h:mm a", Locale.getDefault()).format(date);
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageSentBinding binding;
        SentViewHolder(ItemMessageSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        void bind(String text, String time) {
            binding.tvMessageSent.setText(text);
            binding.tvTimeSent.setText(time);
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageReceivedBinding binding;
        ReceivedViewHolder(ItemMessageReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        void bind(String text, String time) {
            binding.tvMessageReceived.setText(text);
            binding.tvTimeReceived.setText(time);
        }
    }
}
