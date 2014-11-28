package com.capstone.potlatch.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;

import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class ImageSampler {
    public static Bitmap sampleBitmapFromResource(Resources res, int source, int desiredWidth, int desiredHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();

        // Calculate the dimensions of the image to properly sub-sample it
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, source, options);

        // Calculate the optimal sample size and eventually decode the bitmap
        options.inSampleSize = calculateInSampleSize(options, desiredWidth, desiredHeight);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeResource(res, source, options);
    }

    public static Bitmap sampleBitmapFromStream(InputStream source, int desiredWidth, int desiredHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        byte[] imageByteArray;

        try {
            // This let us read the source twice (one to get the bounds and other to decode). An InputStream
            // can only be read directly once.
            imageByteArray = IOUtils.toByteArray(source);

            // Calculate the dimensions of the image to properly sub-sample it
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length, options);

            // Calculate the optimal sample size and eventually decode the bitmap
            options.inSampleSize = calculateInSampleSize(options, desiredWidth, desiredHeight);
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length, options);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int desiredWidth, int desiredHeight) {
        final int imgHeight = options.outHeight;
        final int imgWidth = options.outWidth;
        int sampleSize = 1;

        if (imgHeight > desiredHeight || imgWidth > desiredWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) imgHeight / (float) desiredHeight);
            final int widthRatio = Math.round((float) imgWidth / (float) desiredWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            sampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return sampleSize;
    }

    public static class WorkerURI extends AsyncTask<Uri, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private Uri uri;

        public WorkerURI(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Uri... params) {
            uri = params[0];
            Bitmap bitmap = null;
            ImageView iv = imageViewReference.get();
            if (iv != null) {
                try {
                    InputStream is = new FileInputStream(uri.getPath());
                    bitmap = sampleBitmapFromStream(is, iv.getWidth(), iv.getHeight());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            return bitmap;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null && bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}