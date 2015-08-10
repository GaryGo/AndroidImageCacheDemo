package com.garyko.imagecachetest;

import android.app.Activity;
//import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;


public class MainActivity extends Activity implements View.OnClickListener {

    ImageView imageView;
    RequestQueue mQueue;
    Button button;
    public static MainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                mQueue = Volley.newRequestQueue(this.getApplicationContext());
                ImageLoader imageLoader = new ImageLoader(mQueue, new ImageLruCache(this.getApplicationContext()));
                ImageLoader.ImageListener listener = ImageLoader.getImageListener(imageView,
                        R.drawable.default_image, R.drawable.default_fail);
                imageLoader.get("http://192.168.0.95/~maydaygjf/AdlerOrdering/static/images/no-img.png", listener);
                break;
        }
    }
}
