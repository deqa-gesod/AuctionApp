package com.mob300.auctionapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import java.util.ArrayList;

public class FullScreenImageActivity extends AppCompatActivity {

    private ViewPager2 viewPagerFullScreen;
    private ArrayList<String> imageUrls;
    private GestureDetector gestureDetector;

    public static void start(Context context, ArrayList<String> imageUrls, int currentPosition, boolean isFullScreen) {
        Intent intent = new Intent(context, FullScreenImageActivity.class);
        intent.putStringArrayListExtra("imageUrls", imageUrls);
        intent.putExtra("currentPosition", currentPosition);
        intent.putExtra("isFullScreen", isFullScreen); // New flag
        context.startActivity(intent);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        viewPagerFullScreen = findViewById(R.id.viewPagerFullScreen);
        imageUrls = getIntent().getStringArrayListExtra("imageUrls");
        int currentPosition = getIntent().getIntExtra("currentPosition", 0);

        boolean isFullScreen = getIntent().getBooleanExtra("isFullScreen", false);

        ImageSliderAdapter adapter = new ImageSliderAdapter(this, imageUrls, isFullScreen);
        viewPagerFullScreen.setAdapter(adapter);
        viewPagerFullScreen.setCurrentItem(currentPosition, false);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true; // Tell the system that we are interested in the rest of the gestures
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float deltaY = e2.getY() - e1.getY();
                if (Math.abs(deltaY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (deltaY > 0) {
                        // Swipe down detected, finish the activity
                        finish();
                        // Apply the custom exit animation
                        overridePendingTransition(0, R.anim.slide_out_down); // Ensure this animation is defined in res/anim
                    }
                    return true;
                }
                return false;
            }

        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // Let the GestureDetector inspect all touch events
        gestureDetector.onTouchEvent(event);
        // Make sure to call the super implementation
        // so that normal touch processing continues.
        return super.dispatchTouchEvent(event);
    }

}


