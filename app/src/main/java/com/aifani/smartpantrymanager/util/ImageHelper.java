package com.aifani.smartpantrymanager.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public final class ImageHelper {

    private static final int MAX_SIZE = 512;

    private ImageHelper() { }

    public static String saveFromUri(Context context, Uri uri) {
        try {
            int rotation = readRotation(context, uri);

            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (InputStream in = context.getContentResolver().openInputStream(uri)) {
                if (in == null) return null;
                BitmapFactory.decodeStream(in, null, bounds);
            }

            int sample = 1;
            while (bounds.outWidth / (sample * 2) >= MAX_SIZE
                    && bounds.outHeight / (sample * 2) >= MAX_SIZE) {
                sample *= 2;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sample;
            Bitmap bitmap;
            try (InputStream in = context.getContentResolver().openInputStream(uri)) {
                if (in == null) return null;
                bitmap = BitmapFactory.decodeStream(in, null, options);
            }
            if (bitmap == null) return null;

            float scale = Math.min(1f,
                    (float) MAX_SIZE / Math.max(bitmap.getWidth(), bitmap.getHeight()));
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            matrix.postRotate(rotation);
            Bitmap result = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            File dir = new File(context.getFilesDir(), "images");
            if (!dir.exists() && !dir.mkdirs()) return null;
            File file = new File(dir, "item_" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream out = new FileOutputStream(file)) {
                result.compress(Bitmap.CompressFormat.JPEG, 85, out);
            }
            return file.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }


    public static Bitmap load(String path) {
        if (path == null) return null;
        File file = new File(path);
        if (!file.exists()) return null;
        return BitmapFactory.decodeFile(path);
    }
    public static void delete(String path) {
        if (path == null) return;
        File file = new File(path);
        if (file.exists()) {

            file.delete();
        }
    }

    private static int readRotation(Context context, Uri uri) {
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in == null) return 0;
            ExifInterface exif = new ExifInterface(in);
            switch (exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                case ExifInterface.ORIENTATION_ROTATE_90:  return 90;
                case ExifInterface.ORIENTATION_ROTATE_180: return 180;
                case ExifInterface.ORIENTATION_ROTATE_270: return 270;
                default: return 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }
}