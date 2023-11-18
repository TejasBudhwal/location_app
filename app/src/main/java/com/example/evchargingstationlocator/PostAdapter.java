package com.example.evchargingstationlocator;

import android.content.Context;
import android.media.Image;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>{
    private static final String Tag = "RecyclerView";
    private Context mContext;
    private ArrayList<Posts> postsList;

    public PostAdapter(Context mContext, ArrayList<Posts> postsList) {
        this.mContext = mContext;
        this.postsList = postsList;
    }

    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.all_posts_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView1.setText(postsList.get(position).getImageName());
        holder.textView3.setText(postsList.get(position).getLocation());
        holder.textView4.setText(postsList.get(position).getMessage());
        holder.textView5.setText(postsList.get(position).getDate());
        holder.textView6.setText(postsList.get(position).getTime());
        Glide.with(mContext)
                .load(postsList.get(position).getPostImage())
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView1, textView2, textView3, textView4, textView5, textView6;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
            textView1 = itemView.findViewById(R.id.user_name);
            textView3 = itemView.findViewById(R.id.location);
            textView4 = itemView.findViewById(R.id.description);
            textView5 = itemView.findViewById(R.id.date);
            textView6 = itemView.findViewById(R.id.time);
        }
    }
}
