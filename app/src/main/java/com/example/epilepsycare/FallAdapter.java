package com.example.epilepsycare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FallAdapter extends RecyclerView.Adapter<FallAdapter.ViewHolder>{
    private OnItemClickListener listener;

    public interface OnItemClickListener{
        void onItemClick(int position);
        void ItemDelete(int position);
        void ItemShare(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    ArrayList<FallEvents> fallEvents;
    public FallAdapter(ArrayList<FallEvents> fallEvents) {
        this.fallEvents = fallEvents;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fall_card,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tv_date_time.setText(fallEvents.get(position).fall_date_time);
        holder.tv_location.setText(fallEvents.get(position).fall_location);
    }

    @Override
    public int getItemCount() {
        return fallEvents.size();
    }

    // for generating viewHolder
    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView tv_date_time,tv_location;
        ImageView img_delete,img_share;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_date_time = itemView.findViewById(R.id.fall_event_datetime);
            tv_location = itemView.findViewById(R.id.fall_event_location);
            img_delete = itemView.findViewById(R.id.img_fall_event_delete);
            img_share = itemView.findViewById(R.id.img_fall_event_share);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener!=null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });

            img_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener!=null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.ItemDelete(position);
                        }
                    }
                }
            });
            img_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener!=null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.ItemShare(position);
                        }
                    }
                }
            });
        }
    }

}
