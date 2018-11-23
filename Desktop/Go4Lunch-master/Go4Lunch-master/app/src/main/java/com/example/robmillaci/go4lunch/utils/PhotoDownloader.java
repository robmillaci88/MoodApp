package com.example.robmillaci.go4lunch.utils;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * Responsible for taking a picture URL string and returning a bitmap
 */
public class PhotoDownloader {
    private final IphotoDownloadedCallback mPhotoDownloadedCallback;
    private final RecyclerView.ViewHolder mholder;

    public PhotoDownloader(IphotoDownloadedCallback mCallback, RecyclerView.ViewHolder holder) {
        this.mPhotoDownloadedCallback = mCallback;
        this.mholder = holder;
    }


    public void getPhotos(String id, final GeoDataClient mGeoDataClient) {
        final Bitmap[] bitmap = {null};
        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(id);
        photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                // Get the list of photos.
                PlacePhotoMetadataResponse photos = task.getResult();
                // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
                PlacePhotoMetadataBuffer photoMetadataBuffer = photos != null ? photos.getPhotoMetadata() : null;
                // Get the first photo in the list. First check that the buffer contains at value
                if (photoMetadataBuffer != null && photoMetadataBuffer.getCount() > 0) {
                    PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0);
                    // Get a full-size bitmap for the photo.
                    Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
                    photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                            PlacePhotoResponse photo = task.getResult();

                            if (photo != null) {
                                bitmap[0] = photo.getBitmap();
                                if (bitmap[0] != null) {
                                    mPhotoDownloadedCallback.photoReady(bitmap[0], mholder);
                                } else {
                                    mPhotoDownloadedCallback.photoReady(null, mholder);
                                }
                            }
                        }
                    });
                } else {
                    mPhotoDownloadedCallback.photoReady(null, mholder);
                }
            }
        });

    }
}

