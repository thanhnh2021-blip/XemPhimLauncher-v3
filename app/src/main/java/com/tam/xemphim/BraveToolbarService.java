package com.tam.xemphim;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class BraveToolbarService extends AccessibilityService {

    private static final String BRAVE_PACKAGE = "com.brave.browser";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private WindowManager windowManager;
    private View toolbarCover;
    private boolean braveWasVisible = false;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        AccessibilityServiceInfo info = getServiceInfo();
        if (info != null) {
            info.flags |= AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
            info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                    | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
                    | AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED;
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
            setServiceInfo(info);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;

        String packageName = event.getPackageName().toString();

        if (BRAVE_PACKAGE.equals(packageName)) {
            braveWasVisible = true;
            handler.removeCallbacksAndMessages(null);
            handler.postDelayed(this::updateToolbarCover, 250);
        } else if (braveWasVisible) {
            braveWasVisible = false;
            hideToolbarCover();
        }
    }

    private void updateToolbarCover() {
        if (!braveWasVisible) return;

        AccessibilityNodeInfo root = getRootInActiveWindow();
        Rect addressBar = findAddressBar(root);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;

        if (addressBar != null && addressBar.width() > 100 && addressBar.height() > 10) {
            int top = addressBar.top;
            int height = addressBar.height();
            int minimumHeight = dp(44);

            if (height < minimumHeight) {
                int extra = minimumHeight - height;
                top -= extra / 2;
                height = minimumHeight;
            }

            showToolbarCover(screenWidth, top, height);
        } else {
            showToolbarCover(screenWidth, getStatusBarHeight(), dp(48));
        }
    }

    private Rect findAddressBar(AccessibilityNodeInfo node) {
        if (node == null) return null;

        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);

        CharSequence text = node.getText();
        CharSequence description = node.getContentDescription();
        String value = "";
        if (text != null) value += text.toString();
        if (description != null) value += " " + description.toString();
        value = value.toLowerCase();

        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;

        if ((value.contains("m.youtube.com") || value.contains("youtube.com"))
                && bounds.top < screenHeight / 3
                && bounds.width() > screenWidth / 3) {
            return bounds;
        }

        String className = node.getClassName() != null
                ? node.getClassName().toString() : "";

        if (className.contains("EditText")
                && bounds.top < screenHeight / 3
                && bounds.width() > screenWidth / 3) {
            return bounds;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            Rect result = findAddressBar(child);
            if (result != null) return result;
        }

        return null;
    }

    private void showToolbarCover(int width, int top, int height) {
        hideToolbarCover();

        toolbarCover = new View(this);
        toolbarCover.setBackgroundColor(Color.rgb(32, 33, 36));

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                width,
                height,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.OPAQUE
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = Math.max(0, top);

        try {
            windowManager.addView(toolbarCover, params);
        } catch (Exception ignored) {
        }
    }

    private void hideToolbarCover() {
        if (toolbarCover == null) return;

        try {
            windowManager.removeView(toolbarCover);
        } catch (Exception ignored) {
        }
        toolbarCover = null;
    }

    private int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier(
                "status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return dp(24);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onInterrupt() {
        hideToolbarCover();
    }

    @Override
    public void onDestroy() {
        hideToolbarCover();
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
