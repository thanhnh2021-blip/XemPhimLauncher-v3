package com.tam.xemphim;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class BraveToolbarService extends AccessibilityService {

    private static final String TAG = "BraveToolbarService";

    private static final String BRAVE_PACKAGE =
            "com.brave.browser";

    private static final String BRAVE_TOOLBAR_ID =
            "com.brave.browser:id/toolbar_container";

    private WindowManager windowManager;
    private View overlay;

    private int overlayLeft = -1;
    private int overlayTop = -1;
    private int overlayRight = -1;
    private int overlayBottom = -1;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private long lastBraveEventTime = 0;

    // ============================================================
    // LIFECYCLE
    // ============================================================

    @Override
    public void onCreate() {
        super.onCreate();

        Log.e(TAG,
                "BraveToolbarService.onCreate PID="
                        + android.os.Process.myPid());
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        Log.e(TAG,
                "BraveToolbarService.onServiceConnected()");

        try {

            windowManager =
                    (WindowManager) getSystemService(
                            WINDOW_SERVICE);

            AccessibilityServiceInfo info =
                    getServiceInfo();

            if (info == null) {
                Log.e(TAG,
                        "getServiceInfo() = NULL");
                return;
            }

            info.eventTypes =
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                            | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
                            | AccessibilityEvent.TYPE_WINDOWS_CHANGED;

            info.feedbackType =
                    AccessibilityServiceInfo.FEEDBACK_GENERIC;

            info.notificationTimeout = 100;

            info.flags |=
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;

            info.flags |=
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;

            setServiceInfo(info);

            Log.e(TAG,
                    "AccessibilityService configured");

        } catch (Exception e) {

            Log.e(TAG,
                    "onServiceConnected ERROR",
                    e);
        }
    }

    // ============================================================
    // ACCESSIBILITY EVENTS
    // ============================================================

    @Override
    public void onAccessibilityEvent(
            AccessibilityEvent event) {

        if (event == null) {
            return;
        }

        CharSequence pkg =
                event.getPackageName();

        String packageName =
                pkg == null
                        ? ""
                        : pkg.toString();

        // --------------------------------------------------------
        // Brave event
        // --------------------------------------------------------

        if (BRAVE_PACKAGE.equals(packageName)) {

            lastBraveEventTime =
                    System.currentTimeMillis();

            handler.removeCallbacks(
                    foregroundCheckRunnable
            );

            handler.removeCallbacks(
                    toolbarUpdateRunnable
            );

            handler.postDelayed(
                    toolbarUpdateRunnable,
                    100
            );

            return;
        }

        // --------------------------------------------------------
        // Other app/event
        //
        // Do NOT immediately remove the overlay because Lenovo
        // / FreeKiosk can generate accessibility events while
        // Brave is actually the foreground application.
        // --------------------------------------------------------

        handler.removeCallbacks(
                foregroundCheckRunnable
        );

        handler.postDelayed(
                foregroundCheckRunnable,
                250
        );
    }

    // ============================================================
    // FOREGROUND CHECK
    // ============================================================

    private final Runnable foregroundCheckRunnable =
            new Runnable() {

                @Override
                public void run() {

                    try {

                        AccessibilityNodeInfo root =
                                getRootInActiveWindow();

                        if (root == null) {

                            // Temporary transition.
                            // Do not remove immediately.
                            return;
                        }

                        CharSequence pkg =
                                root.getPackageName();

                        String packageName =
                                pkg == null
                                        ? ""
                                        : pkg.toString();

                        if (!BRAVE_PACKAGE.equals(
                                packageName)) {

                            removeToolbarOverlay();
                        }

                    } catch (Exception e) {

                        Log.e(TAG,
                                "Foreground check ERROR",
                                e);
                    }
                }
            };

    // ============================================================
    // UPDATE TOOLBAR
    // ============================================================

    private final Runnable toolbarUpdateRunnable =
            new Runnable() {

                @Override
                public void run() {

                    updateToolbarOverlay();
                }
            };

    private void updateToolbarOverlay() {

        try {

            AccessibilityNodeInfo root =
                    getRootInActiveWindow();

            if (root == null) {
                return;
            }

            CharSequence pkg =
                    root.getPackageName();

            if (pkg == null
                    || !BRAVE_PACKAGE.equals(
                    pkg.toString())) {

                return;
            }

            Rect toolbarBounds =
                    findToolbarBounds(root);

            if (toolbarBounds == null) {

                Log.e(TAG,
                        "Brave toolbar bounds not found");

                return;
            }

            Log.e(TAG,
                    "Brave toolbar bounds = "
                            + toolbarBounds);

            showToolbarOverlay(
                    toolbarBounds
            );

        } catch (Exception e) {

            Log.e(TAG,
                    "updateToolbarOverlay ERROR",
                    e);
        }
    }

    // ============================================================
    // FIND BRAVE TOOLBAR
    // ============================================================

    private Rect findToolbarBounds(
            AccessibilityNodeInfo node) {

        if (node == null) {
            return null;
        }

        try {

            String viewId =
                    node.getViewIdResourceName();

            if (BRAVE_TOOLBAR_ID.equals(viewId)) {

                Rect bounds =
                        new Rect();

                node.getBoundsInScreen(
                        bounds
                );

                if (bounds.width() > 0
                        && bounds.height() > 0) {

                    return bounds;
                }
            }

            int childCount =
                    node.getChildCount();

            for (int i = 0;
                 i < childCount;
                 i++) {

                AccessibilityNodeInfo child =
                        node.getChild(i);

                if (child == null) {
                    continue;
                }

                Rect result =
                        findToolbarBounds(
                                child
                        );

                child.recycle();

                if (result != null) {
                    return result;
                }
            }

        } catch (Exception e) {

            Log.e(TAG,
                    "findToolbarBounds ERROR",
                    e);
        }

        return null;
    }

    // ============================================================
    // CREATE / UPDATE OVERLAY
    // ============================================================

    private void showToolbarOverlay(
            Rect bounds) {

        if (windowManager == null) {
            return;
        }

        int left =
                bounds.left;

        int top =
                bounds.top;

        int width =
                bounds.width();

        int height =
                bounds.height();

        if (width <= 0 || height <= 0) {
            return;
        }

        // --------------------------------------------------------
        // Already exists:
        // update position only when needed.
        // --------------------------------------------------------

        if (overlay != null) {

            boolean samePosition =
                    overlayLeft == left
                            && overlayTop == top
                            && overlayRight
                            == bounds.right
                            && overlayBottom
                            == bounds.bottom;

            if (samePosition) {
                return;
            }

            try {

                WindowManager.LayoutParams params =
                        (WindowManager.LayoutParams)
                                overlay.getLayoutParams();

                params.width =
                        width;

                params.height =
                        height;

                params.x =
                        left;

                params.y =
                        top;

                windowManager.updateViewLayout(
                        overlay,
                        params
                );

                overlayLeft =
                        left;

                overlayTop =
                        top;

                overlayRight =
                        bounds.right;

                overlayBottom =
                        bounds.bottom;

                Log.e(TAG,
                        "Overlay updated = "
                                + bounds);

                return;

            } catch (Exception e) {

                Log.e(TAG,
                        "Overlay update failed; recreating",
                        e);

                removeToolbarOverlay();
            }
        }

        // --------------------------------------------------------
        // Create overlay
        // --------------------------------------------------------

        try {

            overlay =
                    new View(this);

            /*
             * Production cover.
             *
             * The Brave toolbar detected on the tablet is a
             * light toolbar. White gives a clean blank strip
             * without the URL/icons.
             *
             * This does NOT modify Brave itself.
             * Brave and Shields continue running underneath.
             */
            overlay.setBackgroundColor(
                    Color.WHITE
            );

            WindowManager.LayoutParams params =
                    new WindowManager.LayoutParams(
                            width,
                            height,

                            WindowManager.LayoutParams
                                    .TYPE_ACCESSIBILITY_OVERLAY,

                            WindowManager.LayoutParams
                                    .FLAG_NOT_FOCUSABLE
                                    | WindowManager.LayoutParams
                                    .FLAG_NOT_TOUCHABLE
                                    | WindowManager.LayoutParams
                                    .FLAG_LAYOUT_IN_SCREEN,

                            android.graphics.PixelFormat.OPAQUE
                    );

            params.gravity =
                    Gravity.TOP
                            | Gravity.LEFT;

            params.x =
                    left;

            params.y =
                    top;

            windowManager.addView(
                    overlay,
                    params
            );

            overlayLeft =
                    left;

            overlayTop =
                    top;

            overlayRight =
                    bounds.right;

            overlayBottom =
                    bounds.bottom;

            Log.e(TAG,
                    "!!! PRODUCTION OVERLAY ATTACHED !!!");

            Log.e(TAG,
                    "bounds = " + bounds);

        } catch (Exception e) {

            Log.e(TAG,
                    "Production overlay ERROR",
                    e);

            overlay = null;

            overlayLeft = -1;
            overlayTop = -1;
            overlayRight = -1;
            overlayBottom = -1;
        }
    }

    // ============================================================
    // REMOVE OVERLAY
    // ============================================================

    private void removeToolbarOverlay() {

        if (overlay == null) {
            return;
        }

        try {

            if (windowManager != null) {

                windowManager.removeView(
                        overlay
                );
            }

        } catch (Exception e) {

            Log.e(TAG,
                    "removeToolbarOverlay ERROR",
                    e);

        } finally {

            overlay = null;

            overlayLeft = -1;
            overlayTop = -1;
            overlayRight = -1;
            overlayBottom = -1;
        }
    }

    // ============================================================
    // INTERRUPT
    // ============================================================

    @Override
    public void onInterrupt() {

        Log.e(TAG,
                "BraveToolbarService.onInterrupt()");

        removeToolbarOverlay();
    }

    // ============================================================
    // DESTROY
    // ============================================================

    @Override
    public void onDestroy() {

        Log.e(TAG,
                "BraveToolbarService.onDestroy()");

        removeToolbarOverlay();

        super.onDestroy();
    }
}
