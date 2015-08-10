package com.garyko.imagecachetest;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by maydaygjf on 7/27/15.
 */
public class ImageDiskLruCache {

    public static final String TAG = ImageDiskLruCache.class.getSimpleName();

    private DiskLruCache mDiskLruCache;
    private int mDiskCacheSize = 1024 * 1024 * 100; // 100MB


    //初始化函数
    ImageDiskLruCache(Context context){
        try {

            File cacheDir = context.getCacheDir();
            File imageCacheDir = new File(cacheDir + "/image");
            //如果文件夹不存在则创建
            if (!imageCacheDir.exists()) {
                imageCacheDir.mkdirs();
            }

            //第一个参数指定的是数据的缓存地址，第二个参数指定当前应用程序的版本号，
            //第三个参数指定同一个key可以对应多少个缓存文件，基本都是传1，第四个参数指定最多可以缓存多少字节的数据
            mDiskLruCache = DiskLruCache.open(imageCacheDir, getAppVersion(context), 1, mDiskCacheSize);
        }
        catch (IOException e) {
            Log.i(TAG, "fail to open cache");
        }

    }

    //获取应用的版本号
    public int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        }
        catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        return 1;
    }


    //添加图片到缓存
    public void addBitmapToDiskCache(final String data, final Bitmap bitmap) {
        if (data == null || bitmap == null) {
            return;
        }

        if (mDiskLruCache != null) {
            final String key = hashKeyForDisk(data);
            OutputStream out = null;
            try {
                final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                if (snapshot == null) {
                    final DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                    if (editor != null) {
                        out = editor.newOutputStream(0);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                        editor.commit();
                        out.close();
                        mDiskLruCache.flush();
                    }
                } else {
                    snapshot.getInputStream(0).close();
                }
            } catch (final IOException e) {

            } finally {
                try {
                    if (out != null) {
                        out.close();
                        out = null;
                    }
                } catch (final IOException e) {
                } catch (final IllegalStateException e) {
                }
            }
        }
    }


    //从缓存中取出图片
    public final Bitmap getBitmapFromDiskCache(final String data) {
        if (data == null) {
            return null;
        }

        final String key = hashKeyForDisk(data);//md5生成key
        if (mDiskLruCache != null) {
            InputStream inputStream = null;
            try {
                final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                if (snapshot != null) {
                    inputStream = snapshot.getInputStream(0);
                    if (inputStream != null) {
                        final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        if (bitmap != null) {
                            return bitmap;
                        }
                    }
                }
            } catch (final IOException e) {

            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (final IOException e) {
                }
            }
        }
        return null;
    }

    //删除缓存文件
    public boolean removeImageFromDiskCache(final String data) {
        if (data == null) {
            return false;
        }

        final String key = hashKeyForDisk(data);//md5生成key
        if (mDiskLruCache != null) {
            try {
                boolean remove =  mDiskLruCache.remove(key);
                return remove;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    //将key进行MD5编码
    public String hashKeyForDisk(String key) {
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