package com.example.pisspal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private List<ReviewModel> reviewList;
    private Context context;

    public ReviewAdapter(Context context, List<ReviewModel> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.review_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReviewModel ReviewModel = reviewList.get(position);
        String formattedRating = String.format("%.1f", ReviewModel.getAverageRating());

        holder.locationNameTextView.setText(ReviewModel.getLocationName());
        holder.averageratingBarStar.setRating((float) ReviewModel.getAverageRating());

        if (ReviewModel.getOtherComments()=="") {
            holder.otherCommentsTextView.setText("No Comments");}
        else {
        holder.otherCommentsTextView.setText('"' + ReviewModel.getOtherComments() + '"');}

        holder.averageratingBarNumber.setText(formattedRating);
        holder.latNlon.setText(String.valueOf(ReviewModel.getLatitude()) + "°N " + String.valueOf(ReviewModel.getLongitude()) + "°E");
        Glide.with(context).load(ReviewModel.getImageUrl()).into(holder.reviewImage);

    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView locationNameTextView, otherCommentsTextView, averageratingBarNumber, latNlon;
        RatingBar averageratingBarStar;
        ImageView reviewImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            locationNameTextView = itemView.findViewById(R.id.reviewLocationName);
            averageratingBarStar = itemView.findViewById(R.id.ratingBarStar);
            averageratingBarNumber = itemView.findViewById(R.id.ratingBarNumber);
            otherCommentsTextView = itemView.findViewById(R.id.reviewOtherComments);
            reviewImage = itemView.findViewById(R.id.reviewtoiletimg);
            latNlon = itemView.findViewById(R.id.reviewlatlon);
        }
    }
}
