package com.tam.xemphim;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityEvent;

public class BraveToolbarService extends AccessibilityService {

    private static final String BRAVE_PACKAGE = "com.brave.browser";

    private WindowManager windowManager;
    private View overlay;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        AccessibilityServiceInfo info = getServiceInfo();

        if (info != null) {
            info.eventTypes =
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                    | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
                    | AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED;

            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
            info.notificationTimeout = 100;
            info.flags |= AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;

            setServiceInfo(info);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event == null || event.getPackageName() == null) {
            return;
        }

        String packageName = event.getPackageName().toString();

        if (!BRAVE_PACKAGE.equals(packageName)) {
            removeOverlay();
            return;
        }

        handler.removeCallbacksAndMessages(null);

        // Đợi Brave dựng xong giao diện rồi mới tìm thanh địa chỉ.
        handler.postDelayed(this::showToolbarCover, 350);
    }

    private void showToolbarCover() {

        AccessibilityNodeInfo root = getRootInActiveWindow();

        Rect toolbarRect = new Rect();

        boolean found = findYoutubeAddressBar(root, toolbarRect);

        if (found) {
            int top = toolbarRect.top;
            int height = toolbarRect.height();

            // Nếu node chỉ trả về vùng chữ quá nhỏ,
            // mở rộng tối thiểu để phủ toàn bộ thanh địa chỉ.
            if (height < dp(40)) {
                int center = toolbarRect.centerY();
                height = dp(48);
                top = center - height / 2;
            }

            showOverlay(top, height);

        } else {
            // Fallback: vị trí thanh địa chỉ Brave trên tablet.
            int statusBar = getStatusBarHeight();
            showOverlay(statusBar, dp(48));
        }
    }

    private boolean findYoutubeAddressBar(
            AccessibilityNodeInfo node,
            Rect result) {

        if (node == null) {
            return false;
        }

        CharSequence text = node.getText();
        CharSequence description = node.getContentDescription();

        String value = "";

        if (text != null) {
            value += text.toString() + " ";
        }

        if (description != null) {
            value += description.toString();
        }

        value = value.toLowerCase();

        if (value.contains("m.youtube.com")
                || value.contains("youtube.com")) {

            node.getBoundsInScreen(result);

            // Chỉ chấp nhận node nằm ở vùng trên cùng
            // để tránh nhầm ô tìm kiếm YouTube.
            if (result.top < getScreenHeight() * 0.25f
                    && result.width() > dp(100)) {
                return true;
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {

            AccessibilityNodeInfo child = node.getChild(i);

            if (child != null) {

                if (findYoutubeAddressBar(child, result)) {
                    return true;
                }

                child.recycle();
            }
        }

        return false;
    }

    private void showOverlay(int top, int height) {

        removeOverlay();

        if (windowManager == null) {
            return;
        }

        overlay = new View(this);

        // Màu tối giống thanh toolbar Brave khi dùng Dark Mode.
        overlay.setBackgroundColor(Color.rgb(32, 33, 36));

        WindowManager.LayoutParams params =
                new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        height,
                        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        android.graphics.PixelFormat.OPAQUE
                );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = top;

        try {
            windowManager.addView(overlay, params);
        } catch (Exception ignored) {
        }
    }

    private void removeOverlay() {

        if (overlay != null && windowManager != null) {

            try {
                windowManager.removeView(overlay);
            } catch (Exception ignored) {
            }

            overlay = null;
        }
    }

    private int dp(int value) {
        return Math.round(
                value * getResources().getDisplayMetrics().density
        );
    }

    private int getStatusBarHeight() {

        int resourceId = getResources()
                .getIdentifier(
                        "status_bar_height",
                        "dimen",
                        "android"
                );

        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }

        return dp(24);
    }

    private int getScreenHeight() {
        return getResources()
                .getDisplayMetrics()
                .heightPixels;
    }

    @Override
    public void onInterrupt() {
        removeOverlay();
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        removeOverlay();
        super.onDestroy();
    }
}
