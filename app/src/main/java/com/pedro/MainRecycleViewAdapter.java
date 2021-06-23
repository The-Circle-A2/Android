package com.pedro;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pedro.model.Message;
import com.pedro.rtpstreamer.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class MainRecycleViewAdapter extends RecyclerView.Adapter<MainRecycleViewAdapter.ViewHolder> {
    private ArrayList<Message> mMessages;

    public MainRecycleViewAdapter(ArrayList<Message> messages) {
        this.mMessages = messages;
    }

    @NonNull
    @Override
    public MainRecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflator = LayoutInflater.from(context);

        // create a new view
        View activityListItem = inflator.inflate(R.layout.messages, parent, false);
        MainRecycleViewAdapter.ViewHolder viewHolder = new ViewHolder(activityListItem);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MainRecycleViewAdapter.ViewHolder holder, int position) {
        //Retrieve current Feature
        final Message message = mMessages.get(position);

        //Set variables in text views
        holder.userName.setText(message.getText());
        holder.text.setText(message.getText());
        //holder.date.setText(message.getDate());
        Picasso.get().load(Uri.parse(message.getImageUrl())).into(holder.image);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView id;
        private TextView userName;
        private TextView text;
        private TextView date;
        private ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            //Retrieve text views from layout
            id              = itemView.findViewById(R.id.message_id);
            userName        = itemView.findViewById(R.id.message_user_name);
            text            = itemView.findViewById(R.id.message_text);
            //date            = itemView.findViewById(R.id.message_date);
            image           = itemView.findViewById(R.id.message_photo);
        }

        @Override
        public void onClick(View view) {

        }
    }
}
