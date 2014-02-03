package com.barobot.db;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import android.content.Context;

public class ImageSource {

    private static final String SCHEME_DB = "db";
    private static final String DB_URI_PREFIX = SCHEME_DB + "://";

    public ImageSource(Context context) {
    //    super(context);
    }
/*
    protected InputStream getStreamFromOtherSource(String imageUri, Object extra) throws IOException {
        if (imageUri.startsWith(DB_URI_PREFIX)) {
            String path = imageUri.substring(DB_URI_PREFIX.length());

            // Your logic to retreive needed data from DB
            byte[] imageData = null;

            return new ByteArrayInputStream(imageData);
        } else {
            return super.getStreamFromOtherSource(imageUri, extra);
        }
    }*/
}

/*
 * 
 * 
 * ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
        ...
        .imageDownloader(new SqliteImageDownloader(context))
        .build();

ImageLoader.getInstance().init(config);

And then we can do following to display image from DB:

imageLoader.displayImage("db://mytable/13", imageView);
*/
 