package com.example.rob.newsapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class FeedAdapter extends ArrayAdapter {
        private static final String TAG = "FeedAdapter";
        private final int layoutResource;
        private final LayoutInflater layoutInflater;
        private List<FeedItem> newsItems;
        protected ViewHolder viewHolder;
        private Context context;

    public List<FeedItem> getNewsItems() {
        return newsItems;
    }

    public FeedAdapter(@NonNull Context context, int resource, List<FeedItem> newsItems) {

        super(context, resource);
        this.layoutResource = resource;
        this.layoutInflater = LayoutInflater.from(context);
        this.newsItems = newsItems;

    }

    @Override
    public int getCount() {
        return newsItems.size();
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public int getPosition(@Nullable Object item) {
        return super.getPosition(item);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(layoutResource, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        FeedItem currentApp = newsItems.get(position);


        viewHolder.newstitle.setText(currentApp.getTitle());
        viewHolder.newsdescription.setText(currentApp.getDescription());


        Bitmap foundImage = null;
        DownloadImage di = new DownloadImage();
        try {
            foundImage = di.execute(currentApp.getImageURL()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        viewHolder.itemImage.setImageBitmap(foundImage);


        return convertView;
    }

    private class ViewHolder {
        final TextView newstitle;
        final TextView newsdescription;
        final ImageView itemImage;

        public ViewHolder(View v) {

            this.newstitle = (TextView) v.findViewById(R.id.newsTitle);
            this.newsdescription = (TextView) v.findViewById(R.id.newsDesc);
            this.itemImage = v.findViewById(R.id.itemImage);

        }
    }
}



