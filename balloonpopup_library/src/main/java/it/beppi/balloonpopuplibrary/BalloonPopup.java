package it.beppi.balloonpopuplibrary;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static it.beppi.balloonpopuplibrary.BalloonPopup.BalloonGravity.halftop_halfright;
import static it.beppi.balloonpopuplibrary.R.style.scale;

/**
 * Created by Beppi on 14/12/2016.
 */

public class BalloonPopup {
    private Context ctx;
    private View attachView;
    private BalloonGravity gravity;
    private int offsetX, offsetY;
    private int bgColor, fgColor;
    private int layoutRes;
    private String text;
    private int textSize;
    private Drawable drawable;
    private BalloonAnimation balloonAnimation;
    private PopupWindow popupWindow;
    private int timeToLive;

    private BDelay bDelay;

    public enum BalloonShape {
        oval,
        rounded_square
    }

    public enum BalloonAnimation {
        instantin_scaleout,
        instantin_fadeout,
        instantin_fade_and_scaleout,
        scale,
        fade,
        fade_and_scale
    }

    public enum BalloonGravity {
        alltop_allleft,
        alltop_halfleft,
        alltop_center,
        alltop_halfright,
        alltop_allright,
        halftop_allleft,
        halftop_halfleft,
        halftop_center,
        halftop_halfright,
        halftop_allright,
        center_allleft,
        center_halfleft,
        center,
        center_halfright,
        center_allright,
        halfbottom_allleft,
        halfbottom_halfleft,
        halfbottom_center,
        halfbottom_halfright,
        halfbottom_allright,
        allbottom_allleft,
        allbottom_halfleft,
        allbottom_center,
        allbottom_halfright,
        allbottom_allright,
    }

    public BalloonPopup(Context ctx, View attachView, BalloonGravity gravity, int offsetX, int offsetY, int bgColor, int fgColor, int layoutRes, String text, int textSize, Drawable drawable, BalloonAnimation balloonAnimation, int timeToLive) {
        this.ctx = ctx;
        this.attachView = attachView;
        this.gravity = gravity;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.bgColor = bgColor;
        this.fgColor = fgColor;
        this.layoutRes = layoutRes;
        this.text = text;
        this.textSize = textSize;
        this.drawable = drawable;
        this.balloonAnimation = balloonAnimation;
        this.timeToLive = timeToLive;
    }

    public static Builder Builder(Context ctx, View anchorView) {
        return new Builder(ctx, anchorView);
    }

    private void show() {
        View customView = ((LayoutInflater) ctx.getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(layoutRes, null);

        final TextView tv = (TextView) customView.findViewById(R.id.text_view);
        tv.setText(text);
        tv.setTextColor(fgColor);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float)textSize);

        popupWindow = new PopupWindow(customView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        showingPopup = pw;
        if (Build.VERSION.SDK_INT >= 21) popupWindow.setElevation(5.0f);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(true);
        popupWindow.setClippingEnabled(false);
        if (drawable != null) popupWindow.setBackgroundDrawable(drawable);

        //TODO: manage bgcolor

        switch (balloonAnimation) {
            case instantin_fadeout: popupWindow.setAnimationStyle(R.style.instantin_fadeout); break;
            case instantin_scaleout: popupWindow.setAnimationStyle(R.style.instantin_scaleout); break;
            case instantin_fade_and_scaleout: popupWindow.setAnimationStyle(R.style.instantin_fade_and_scaleout); break;
            case scale: popupWindow.setAnimationStyle(scale); break;
            case fade: popupWindow.setAnimationStyle(R.style.fade); break;
            case fade_and_scale: popupWindow.setAnimationStyle(R.style.fade_and_scale); break;
        }

        if (timeToLive > 0) {
            bDelay = new BDelay((long) timeToLive, new Runnable() {
                @Override public void run() { kill(); }});
        }

        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                kill();
                return false;
            }
        });


        draw();
    }

    private void kill() {
//        if (showingPopup == popupWindow) showingPopup = null;
        if (popupWindow == null) return;
        popupWindow.dismiss();
        popupWindow = null;
    }

    private void draw() {
        // calc position and size, then show

        int[] loc = new int[2];
        attachView.getLocationOnScreen(loc);

        attachView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int widthAttachView = attachView.getMeasuredWidth();
        int heightAttachView = attachView.getMeasuredHeight();

        View hostedView = popupWindow.getContentView();
        hostedView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int widthHostedView = hostedView.getMeasuredWidth();
        int heightHostedView = hostedView.getMeasuredHeight();

        int posX=loc[0] + offsetX;
        switch (gravity) {
            case alltop_allleft: case halftop_allleft: case center_allleft: case halfbottom_allleft: case allbottom_allleft:
                posX -= widthHostedView; break;
            case alltop_halfleft: case halftop_halfleft: case center_halfleft: case halfbottom_halfleft: case allbottom_halfleft:
                posX -= widthHostedView / 2; break;
            case alltop_center: case halftop_center: case center: case halfbottom_center: case allbottom_center:
                posX += widthAttachView / 2 - widthHostedView / 2; break;
            case alltop_halfright: case halftop_halfright: case center_halfright: case halfbottom_halfright: case allbottom_halfright:
                posX += widthAttachView - widthHostedView / 2; break;
            case alltop_allright: case halftop_allright: case center_allright: case halfbottom_allright: case allbottom_allright:
                posX += widthAttachView; break;
        }

        int posY=loc[1] + offsetY;
        switch (gravity) {
            case alltop_allleft: case alltop_halfleft: case alltop_center: case alltop_halfright: case alltop_allright:
                posY -= heightHostedView; break;
            case halftop_allleft: case halftop_halfleft: case halftop_center: case halftop_halfright: case halftop_allright:
                posY -= heightHostedView / 2; break;
            case center_allleft: case center_halfleft: case center: case center_halfright: case center_allright:
                posY += heightAttachView / 2 - heightHostedView / 2; break;
            case halfbottom_allleft: case halfbottom_halfleft: case halfbottom_center: case halfbottom_halfright: case halfbottom_allright:
                posY += heightAttachView - heightHostedView / 2; break;
            case allbottom_allleft: case allbottom_halfleft: case allbottom_center: case allbottom_halfright: case allbottom_allright:
                posY += heightAttachView; break;
        }

        popupWindow.showAtLocation(attachView, Gravity.NO_GRAVITY, posX, posY);
    }

    /**
     * Redraw the current popup. In case of change
     */
    public void redraw() {
        if (bDelay != null) bDelay.setInterval(timeToLive);
        draw();
//        if (showingPopup != null) draw(showingPopup);
    }

    public boolean isShowing() {
        return (popupWindow != null);
    }

    public void changeOffsetAndUpdateTime(int newOffsetX, int newOffsetY) {
        offsetX = newOffsetX;
        offsetY = newOffsetY;
//        redraw();
        if (popupWindow.isShowing()) {
            if (bDelay != null) bDelay.setInterval(timeToLive);
            popupWindow.update();
        }
        else
            redraw();
    }

    public void changeTextAndUpdateTime(String newText) {
        text = newText;
        redraw();
    }

    
    
    
    public static class Builder {
        private Context ctx;
        private View attachView;
        private BalloonGravity gravity = halftop_halfright;
        private int offsetX = 0, offsetY = 0;
        private int bgColor = Color.WHITE, fgColor = Color.BLACK;
        private int layoutRes = R.layout.text_balloon;
        private String text = "";
        private int textSize = 12;
        private Drawable drawable;
        private BalloonAnimation balloonAnimation = BalloonAnimation.scale;
        private int timeToLive = 1500;

        public Builder(Context ctx, View attachView) {
            this.ctx = ctx;
            this.attachView = attachView;
            this.drawable = ctx.getResources().getDrawable(R.drawable.bg_circle);
        }

        public Builder gravity(BalloonGravity gravity) {
            this.gravity = gravity;
            return this;
        }

        public Builder offsetX(int offsetX) {
            this.offsetX = offsetX;
            return this;
        }
        public Builder offsetY(int offsetY) {
            this.offsetY = offsetY;
            return this;
        }
        
        public Builder positionOffset(int offsetX, int offsetY) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            return this;
        }

        public Builder bgColor(int bgColor) {
            this.bgColor = bgColor;
            return this;
        }

        public Builder fgColor(int fgColor) {
            this.fgColor = fgColor;
            return this;
        }

        public Builder layoutRes(int layoutRes) {
            this.layoutRes = layoutRes;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder text(int textRes) {
            this.text = ctx.getResources().getString(textRes);
            return this;
        }

        public Builder textSize(int textSize) {
            this.textSize = textSize;
            return this;
        }

        public Builder shape(BalloonShape balloonShape) {
            switch (balloonShape) {
                case oval: drawable = ctx.getResources().getDrawable(R.drawable.bg_circle); break;
                case rounded_square: drawable = ctx.getResources().getDrawable(R.drawable.bg_rounded_square); break;
            }
            return this;
        }

        public Builder drawable(Drawable drawable) {
            this.drawable = drawable;
            return this;
        }
        public Builder drawable(int drawableRes) {
            this.drawable = ctx.getResources().getDrawable(drawableRes);
            return this;
        }

        public Builder animation(BalloonAnimation balloonAnimation) {
            this.balloonAnimation = balloonAnimation;
            return this;
        }

        public Builder timeToLive(int milliseconds) {
            this.timeToLive = milliseconds;
            return this;
        }

        public BalloonPopup show() {
            BalloonPopup bp = new BalloonPopup(ctx, attachView, gravity, offsetX, offsetY, bgColor, fgColor, layoutRes, text, textSize, drawable, balloonAnimation, timeToLive);
            bp.show();
            return bp;
        }
    }
}