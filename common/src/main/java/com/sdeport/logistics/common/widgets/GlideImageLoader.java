package com.sdeport.logistics.common.widgets;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;


public class GlideImageLoader {

    RequestOptions options;

    public GlideImageLoader() {
        options = new RequestOptions()
//                .transform(new RoundedCorners(40))
                .priority(Priority.NORMAL);
    }



    public void displayImage(Context context, Object path, final ImageView imageView) {
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        Glide.with(context.getApplicationContext())
                .load(path)
                .apply(options)
                .into(imageView);
    }
}