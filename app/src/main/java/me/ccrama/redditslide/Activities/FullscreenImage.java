package me.ccrama.redditslide.Activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.util.UUID;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;


/**
 * Created by ccrama on 3/5/2015.
 */
public class FullscreenImage extends FullScreenActivity {


    String toReturn;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getTheme().applyStyle(new ColorPreferences(this).getThemeSubreddit(""), true);

        setContentView(R.layout.activity_image);

        final SubsamplingScaleImageView i = (SubsamplingScaleImageView) findViewById(R.id.submission_image);

        final ProgressBar bar = (ProgressBar) findViewById(R.id.progress);
        bar.setIndeterminate(false);
        bar.setProgress(0);
        String url = getIntent().getExtras().getString("url");
        if (url != null && url.contains("imgur") && (!url.contains(".png") || !url.contains(".jpg") || !url.contains(".jpeg"))) {
            url = url + ".png";
        }
        ImageView fakeImage = new ImageView(FullscreenImage.this);
        fakeImage.setLayoutParams(new LinearLayout.LayoutParams(i.getWidth(), i.getHeight()));
        fakeImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

                ((Reddit) getApplication()).getImageLoader()
                .displayImage(url, new ImageViewAware(fakeImage), options, new ImageLoadingListener() {
                    private View mView;

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        mView = view;
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        Log.v("Slide", "LOADING FAILED");

                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        i.setImage(ImageSource.bitmap(loadedImage));
                        ( findViewById(R.id.progress)).setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        Log.v("Slide", "LOADING CANCELLED");

                    }
                }, new ImageLoadingProgressListener() {
                    @Override
                    public void onProgressUpdate(String imageUri, View view, int current, int total) {
                        ((ProgressBar) findViewById(R.id.progress)).setProgress(Math.round(100.0f * current / total));
                    }
                });

        i.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v2) {
                FullscreenImage.this.finish();
            }
        });


        {
            final ImageView iv = (ImageView) findViewById(R.id.share);
            final String finalUrl = url;
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showShareDialog(finalUrl);
                }
            });
            {
                final String finalUrl1 = url;
                findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v2) {


                        try {
                            ((Reddit) getApplication()).getImageLoader()
                                    .loadImage(finalUrl, new SimpleImageLoadingListener() {
                                        @Override
                                        public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
                                            final String localAbsoluteFilePath = saveImageGallery(loadedImage, finalUrl1);

                                            if (localAbsoluteFilePath != null) {
                                                MediaScannerConnection.scanFile(FullscreenImage.this, new String[]{localAbsoluteFilePath}, null, new MediaScannerConnection.OnScanCompletedListener() {
                                                    public void onScanCompleted(String path, Uri uri) {
                                                        Intent intent = new Intent();
                                                        intent.setAction(android.content.Intent.ACTION_VIEW);
                                                        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(".PNG");

                                                        intent.setDataAndType(Uri.parse(localAbsoluteFilePath), mime);
                                                        PendingIntent contentIntent = PendingIntent.getActivity(FullscreenImage.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);


                                                        Notification notif = new NotificationCompat.Builder(FullscreenImage.this)
                                                                .setContentTitle(getString(R.string.info_photo_saved))
                                                                .setSmallIcon(R.drawable.notif)
                                                                .setLargeIcon(loadedImage)
                                                                .setContentIntent(contentIntent)
                                                                .setStyle(new NotificationCompat.BigPictureStyle()
                                                                        .bigPicture(loadedImage)).build();


                                                        NotificationManager mNotificationManager =
                                                                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                                        mNotificationManager.notify(1, notif);

                                                    }
                                                });
                                            }

                                        }

                                    });
                        } catch (Exception e) {
                            Log.v("RedditSlide", "COULDN'T DOWNLOAD!");
                        }

                    }

                });
            }


        }

    }

    private void showShareDialog(final String url) {
        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialoglayout = inflater.inflate(R.layout.sharemenu, null);

        dialoglayout.findViewById(R.id.share_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareImage(url);
            }
        });

        dialoglayout.findViewById(R.id.share_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reddit.defaultShareText(url, FullscreenImage.this);
            }
        });


        builder.setView(dialoglayout);
        builder.show();
    }


    private void shareImage(String finalUrl) {
        ((Reddit) getApplication()).getImageLoader()
                .loadImage(finalUrl, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        String localAbsoluteFilePath = saveImageLocally(loadedImage);

                        if (!localAbsoluteFilePath.isEmpty() && localAbsoluteFilePath != null) {

                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            Uri phototUri = Uri.parse(localAbsoluteFilePath);

                            File file = new File(phototUri.getPath());

                            Log.d("Slide", "file path: " + file.getPath());

                            if (file.exists()) {
                                shareIntent.setData(phototUri);
                                shareIntent.setType("image/png");
                                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));

                                FullscreenImage.this.startActivity(shareIntent);
                            } else {
                                // file create fail
                            }


                        }
                    }


                });
    }

    private String saveImageGallery(final Bitmap _bitmap, String URL) {

        return MediaStore.Images.Media.insertImage(getContentResolver(), _bitmap, URL, "");


    }

    private String saveImageLocally(final Bitmap _bitmap) {

        return MediaStore.Images.Media.insertImage(getContentResolver(), _bitmap, "SHARED" + UUID.randomUUID().toString(), "");


    }
}