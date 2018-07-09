package com.android.popularmovies.trailers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.popularmovies.DetailsActivity;
import com.android.popularmovies.R;

public class TrailerRecyclerVIewAdaptor  extends RecyclerView.Adapter<TrailerRecyclerVIewAdaptor.TrailerViewHolder>{

    public static final String VIDEO = "VIDEO";
    private final Context mContext;
    private TrailerItemClickListener showTrailer;

    public TrailerRecyclerVIewAdaptor(Context context, TrailerItemClickListener showTrailer){
        this.mContext = context;
        this.showTrailer = showTrailer;
    }

    @NonNull
    @Override
    public TrailerRecyclerVIewAdaptor.TrailerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.trailers_details, parent, false);

        return new TrailerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TrailerRecyclerVIewAdaptor.TrailerViewHolder holder, final int position) {


        String trailer =DetailsActivity.getJsonFromJsonArray(position, "type");;// holder.mTextView.getContext().getResources().getString(R.string.trailer_hint);
        trailer = VIDEO +" "+(position+1)+": "+trailer;
        holder.mTextView.setText(trailer);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTrailer.showSelectedTrailer(position);
            }
        });
    }

    @Override
    public int getItemCount() {

        if(TrailerQueryTask.trailerJsonArray==null){
            return 0;
        }
        return  TrailerQueryTask.trailerJsonArray.length();
    }

    public class TrailerViewHolder extends RecyclerView.ViewHolder {

        public View view;
        public ImageView mImageView;
        public TextView mTextView;

        public TrailerViewHolder(View itemView) {
            super(itemView);

            this.view = itemView;
            mImageView = itemView.findViewById(R.id.image_play_trailer);
            mTextView = itemView.findViewById(R.id.textView_trailer);
        }
    }
}
