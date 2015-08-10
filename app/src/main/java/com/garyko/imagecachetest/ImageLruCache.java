package com.garyko.imagecachetest;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by maydaygjf on 7/27/15.
 */

public class ImageLruCache implements ImageLoader.ImageCache{

    public static final String TAG = ImageLruCache.class.getSimpleName();

    private LruCache<String, Bitmap> mMemoryCache;
    private ImageDiskLruCache mImageDiskLruCache;

    //构造函数
    public ImageLruCache(Context context){

        // 获取到可用内存的最大值，使用内存超出这个值会引起OutOfMemory异常。
        // LruCache通过构造函数传入缓存值，以KB为单位。
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // 使用最大可用内存值的1/8作为缓存的大小。
        int cacheSize = maxMemory / 8;
        Log.i(TAG, "The Size of MemoryCache is " + cacheSize/1024 + " MB");

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // 重写此方法来衡量每张图片的大小，默认返回图片数量。
                if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1 ){
                    return bitmap.getByteCount() / 1024;
                }else{
                    return bitmap.getRowBytes()*bitmap.getHeight() / 1024;
                }
            }

        };
        mImageDiskLruCache = new ImageDiskLruCache(context);
    }

    //添加图片进缓存

    public void putBitmap(final String url, final Bitmap bitmap) {
        final String key = hashKeyForLruCache(url);

        mMemoryCache.put(key, bitmap);

        new Thread(new Runnable() {
            @Override
            public void run() {
                mImageDiskLruCache.addBitmapToDiskCache(url, bitmap);
            }
        }).start();
        return;
    }

    //从缓存中取出图片

    public Bitmap getBitmap(String url) {
        String key = hashKeyForLruCache(url);
        Bitmap bm = mMemoryCache.get(key);

        if(bm != null){
            return bm;
        }
        else
        {
            bm = mImageDiskLruCache.getBitmapFromDiskCache(url);
            if (bm != null) {
                mMemoryCache.put(key, bm);
            }
            return bm;
        }
    }


    //删除缓存文件
    public boolean removeBitmap(String url) {
        String key = hashKeyForLruCache(url);
        mMemoryCache.remove(key);
        return mImageDiskLruCache.removeImageFromDiskCache(url);
    }


    //将key进行MD5编码
    public String hashKeyForLruCache(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}