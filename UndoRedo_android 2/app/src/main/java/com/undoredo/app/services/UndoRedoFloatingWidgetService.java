package com.undoredo.app.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.undoredo.app.R;
import com.undoredo.app.eventbus.FindReplaceEvent;
import com.undoredo.app.eventbus.RedoEvent;
import com.undoredo.app.eventbus.UndoEvent;
import com.undoredo.app.main.MainActivity;

import org.greenrobot.eventbus.EventBus;


/**
 * This service is responsible of displaying the 3 buttons (Undo, Find, Redo ) when
 * an EdiText is focus on the phone.
 * We need and use the android.permission.SYSTEM_ALERT_WINDOW permission to be able to display those
 * buttons on top of other applications.
 * We use the library EventBus to Trigger the Undo, Redo and Find/Replace actions in {@link InputAccessibilityService}
 */
public class UndoRedoFloatingWidgetService extends Service implements View.OnClickListener {


    private WindowManager mWindowManager;
    private View mOverlayView;
    int mWidth;
    private boolean activity_background;
    private ImageView undoImgV;
    private ImageView findImgV;
    private ImageView redoImgV;
    private View.OnTouchListener onMVTouchListerner;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)){
                Toast.makeText(this, R.string.undoredo_cant_work_system_alert_window_disabled, Toast.LENGTH_SHORT).show();
                return super.onStartCommand(intent, flags, startId);
            }
        }

        if (intent != null) {
            activity_background = intent.getBooleanExtra("activity_background", false);
        }

        if (mOverlayView == null) {

            mOverlayView = LayoutInflater.from(this).inflate(R.layout.undo_redo_floating_widget, null);

            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);


            //Specify the view position
            params.gravity = Gravity.TOP | Gravity.LEFT;   //Initially view will be added to top-left corner
            params.x = 0;
            params.y = 100;


            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            mWindowManager.addView(mOverlayView, params);

            Display display = mWindowManager.getDefaultDisplay();
            final Point size = new Point();
            display.getSize(size);

            undoImgV = mOverlayView.findViewById(R.id.undoButton);
            findImgV = mOverlayView.findViewById(R.id.findButton);
            redoImgV = mOverlayView.findViewById(R.id.redoButton);


            final ConstraintLayout layout = mOverlayView.findViewById(R.id.floatingWLayout);
            ViewTreeObserver vto = layout.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int width = layout.getMeasuredWidth();
                    //To get the accurate middle of the screen we subtract the width of the floating widget.
                    mWidth = size.x - width;

                }
            });

            onMVTouchListerner = new View.OnTouchListener() {
                public boolean moving = false;
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:

                            moving = false;

                            //remember the initial position.
                            initialX = params.x;
                            initialY = params.y;


                            //get the touch location
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();


                            return false;
                        case MotionEvent.ACTION_UP:

                            if (!moving){
                                // perform click when it is not after a move action
//                                v.performClick();
                            }else {

                                //Only start the activity if the application is in background. Pass the current badge_count to the activity
                                if (activity_background) {

                                    float xDiff = event.getRawX() - initialTouchX;
                                    float yDiff = event.getRawY() - initialTouchY;

                                    if ((Math.abs(xDiff) < 5) && (Math.abs(yDiff) < 5)) {
                                        startAppActivity();
                                    }

                                }
                                //Logic to auto-position the widget based on where it is positioned currently w.r.t middle of the screen.
                                int middle = mWidth / 2;
                                float nearestXWall = params.x >= middle ? mWidth : 0;
                                params.x = (int) nearestXWall;


                                mWindowManager.updateViewLayout(mOverlayView, params);
                            }

                            moving = false;

                            return false;
                        case MotionEvent.ACTION_MOVE:

                            moving = true;

                            int xDiff = Math.round(event.getRawX() - initialTouchX);
                            int yDiff = Math.round(event.getRawY() - initialTouchY);


                            //Calculate the X and Y coordinates of the view.
                            params.x = initialX + xDiff;
                            params.y = initialY + yDiff;

                            //Update the layout with new X & Y coordinates
                            mWindowManager.updateViewLayout(mOverlayView, params);


                            return false;
                    }
                    return false;
                }
            };


            mOverlayView.setOnTouchListener(onMVTouchListerner);
            undoImgV.setOnTouchListener(onMVTouchListerner);
            findImgV.setOnTouchListener(onMVTouchListerner);
            redoImgV.setOnTouchListener(onMVTouchListerner);

            undoImgV.setOnClickListener(this);
            findImgV.setOnClickListener(this);
            redoImgV.setOnClickListener(this);

            View.OnLongClickListener onMVLongCLickListener = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    startAppActivity();
                    return true;
                }
            };

            undoImgV.setOnLongClickListener(onMVLongCLickListener);
            mOverlayView.setOnLongClickListener(onMVLongCLickListener);
            findImgV.setOnLongClickListener(onMVLongCLickListener);
            redoImgV.setOnLongClickListener(onMVLongCLickListener);
        }



        return super.onStartCommand(intent, flags, startId);
    }





    @Override
    public void onCreate() {
        super.onCreate();
        setTheme(R.style.AppTheme);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOverlayView != null)
            mWindowManager.removeView(mOverlayView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.undoButton:
                EventBus.getDefault().post(new UndoEvent());
                break;

            case R.id.findButton:
                EventBus.getDefault().post(new FindReplaceEvent());
                break;

            case R.id.redoButton:
                EventBus.getDefault().post(new RedoEvent());
                break;
        }
    }

    private void startAppActivity(){
        Intent intent = new Intent(UndoRedoFloatingWidgetService.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showDialog(){

    }
}
