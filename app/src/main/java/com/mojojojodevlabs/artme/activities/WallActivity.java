package com.mojojojodevlabs.artme.activities;

import android.Manifest;
import android.app.DownloadManager;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.mojojojodevlabs.artme.R;
import com.mojojojodevlabs.artme.Supplier;
import com.mojojojodevlabs.artme.Utils;
import com.mojojojodevlabs.artme.data.WallData;

import java.io.IOException;


public class WallActivity extends AppCompatActivity {

    WallData data;
    BroadcastReceiver downloadReceiver;
    Supplier supplier;

    Toolbar toolbar;
    ImageView imageee;
    TextView wall, auth;
    LinearLayout bg;
    FloatingActionButton fab;
    View download, share, apply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper);

        supplier = (Supplier) getApplicationContext();

        data = getIntent().getParcelableExtra("wall");
        setTitle(data.name);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        imageee = (ImageView) findViewById(R.id.imageee);
        wall = (TextView) findViewById(R.id.wall);
        auth = (TextView) findViewById(R.id.auth);
        bg = (LinearLayout) findViewById(R.id.back);
        download = findViewById(R.id.download);
        share = findViewById(R.id.share);
        apply = findViewById(R.id.apply);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        byte[] b = getIntent().getByteArrayExtra("preload");
        if (b != null) imageee.setImageBitmap(BitmapFactory.decodeByteArray(b, 0, b.length));

        wall.setText(getTitle());
        auth.setText(data.authorName);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        Glide.with(this).load(data.url).into(new SimpleTarget<GlideDrawable>(metrics.widthPixels, metrics.heightPixels) {
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                imageee.setImageDrawable(resource);

                Palette.from(Utils.drawableToBitmap(resource)).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        fab.setBackgroundTintList(ColorStateList.valueOf(palette.getDarkVibrantColor(Color.DKGRAY)));
                        bg.setBackgroundColor(palette.getVibrantColor(Color.DKGRAY));
                    }
                });

            }
        });

        fab.setImageDrawable(VectorDrawableCompat.create(getResources(), data.favorite ? R.drawable.fav_added : R.drawable.fav_add, getTheme()));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.setImageDrawable(VectorDrawableCompat.create(getResources(), data.favorite ? R.drawable.fav_add : R.drawable.fav_added, getTheme()));
                data.setFavorite(WallActivity.this, !data.favorite);
            }
        });

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.credit) {
                    supplier.getCreditDialog(WallActivity.this, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            registerReceiver(downloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

                            if (ContextCompat.checkSelfPermission(WallActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                                supplier.downloadWallpaper(WallActivity.this, data);
                            else
                                ActivityCompat.requestPermissions(WallActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 8027);

                            dialog.dismiss();
                        }
                    }).show();
                } else {
                    registerReceiver(downloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

                    if (ContextCompat.checkSelfPermission(WallActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                        supplier.downloadWallpaper(WallActivity.this, data);
                    else
                        ActivityCompat.requestPermissions(WallActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 8027);
                }
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.credit) {
                    supplier.getCreditDialog(WallActivity.this, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            supplier.shareWallpaper(WallActivity.this, data);
                            dialog.dismiss();
                        }
                    }).show();
                }
                else supplier.shareWallpaper(WallActivity.this, data);
            }
        });

        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Glide.with(WallActivity.this).load(data.url).into(new SimpleTarget<GlideDrawable>() {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        if (ContextCompat.checkSelfPermission(WallActivity.this, Manifest.permission.SET_WALLPAPER) == PackageManager.PERMISSION_GRANTED) {
                            try {
                                WallpaperManager.getInstance(WallActivity.this).setBitmap(Utils.drawableToBitmap(resource));
                                Toast.makeText(WallActivity.this, R.string.set_wallpaper_success, Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(WallActivity.this, R.string.set_wallpaper_failed, Toast.LENGTH_SHORT).show();
                            }
                        } else
                            ActivityCompat.requestPermissions(WallActivity.this, new String[]{Manifest.permission.SET_WALLPAPER}, 9274);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        Toast.makeText(WallActivity.this, R.string.download_failed, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        downloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                supplier.getDownloadedDialog(WallActivity.this, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        WallActivity.this.startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
                    }
                }).show();
                unregisterReceiver(this);
            }
        };
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(downloadReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) return;
        }

        switch (requestCode) {
            case 8027:
                download.callOnClick();
                break;
            case 9274:
                apply.callOnClick();
                break;
        }
    }
}
