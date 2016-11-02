package com.james.felixlauncher.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SquareImageView extends ImageView {

    public SquareImageView(Context context) {
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size = getMeasuredWidth();
        setMeasuredDimension(size, size);
    }

    public void transition(final Activity activity, final Drawable second) {
        if (second == null) return;
        final int size = Math.min(getMeasuredWidth(), getMeasuredHeight());

        new Thread() {
            @Override
            public void run() {
                Bitmap image2 = null;

                try {
                    image2 = drawableToBitmap(second);
                    if (image2 != null) {
                        image2 = ThumbnailUtils.extractThumbnail(image2, size, size);
                    }
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                }

                final Bitmap result = image2;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result == null) {
                            setImageDrawable(second);
                            return;
                        }

                        Animation exitAnim = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
                        exitAnim.setDuration(150);
                        exitAnim.setAnimationListener(new Animation.AnimationListener() {
                            @Override public void onAnimationStart(Animation animation) {

                            }

                            @Override public void onAnimationRepeat(Animation animation) {

                            }

                            @Override public void onAnimationEnd(Animation animation) {
                                setImageBitmap(result);
                                Animation enterAnim = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
                                enterAnim.setDuration(150);
                                startAnimation(enterAnim);
                            }
                        });
                        startAnimation(exitAnim);
                    }
                });
            }
        }.start();
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}