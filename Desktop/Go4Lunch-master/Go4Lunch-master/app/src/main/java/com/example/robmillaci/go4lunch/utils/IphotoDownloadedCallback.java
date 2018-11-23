package com.example.robmillaci.go4lunch.utils;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;

public interface IphotoDownloadedCallback {
    void photoReady(Bitmap photo, RecyclerView.ViewHolder holder);
}
