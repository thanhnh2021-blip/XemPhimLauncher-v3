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

import java.util.Arrays;

public class BraveToolbarService extends AccessibilityService {

    private static final String TAG = "BraveToolbarService";
    private static final String BRAVE_PACKAGE = "com.brave.browser";

    private WindowManager windowManager;
    private View overlay;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private int eventCounter = 0;
    private int nodeCounter = 0;

    // ============================================================
    // SERVICE LIFECYCLE - onCreate
    // ============================================================

    @Override
    public void onCreate() {

        super.onCreate();

        Log.e(TAG, "");
        Log.e(TAG, "################################################");
        Log.e(TAG, "### BraveToolbarService.onCreate() ###");
        Log.e(TAG, "################################################");

        Log.e(TAG,
                "PROCESS ID = " + android.os.Process.myPid());

        Log.e(TAG,
                "PACKAGE = " + getPackageName());

        Log.e(TAG,
                "THREAD = " + Thread.currentThread().getName());

        Log.e(TAG,
                "################################################");
    }

    // ============================================================
    // SERVICE CONNECTED
    // ============================================================

    @Override
    protected void onServiceConnected() {

        super.onServiceConnected();

        Log.e(TAG, "");
        Log.e(TAG, "================================================");
        Log.e(TAG, "=== SERVICE CONNECTED ===");
        Log.e(TAG, "================================================");

        Log.e(TAG,
                "PROCESS ID = " + android.os.Process.myPid());

        Log.e(TAG,
                "PACKAGE = " + getPackageName());

        Log.e(TAG,
                "THREAD = " + Thread.currentThread().getName());

        try {

            windowManager =
                    (WindowManager) getSystemService(WINDOW_SERVICE);

            Log.e(TAG,
                    "WindowManager = " + windowManager);

            if (windowManager == null) {

                Log.e(TAG,
                        "!!! WindowManager = NULL !!!");
            }

            AccessibilityServiceInfo info =
                    getServiceInfo();

            if (info == null) {

                Log.e(TAG,
                        "!!! getServiceInfo() = NULL !!!");

                return;
            }

            Log.e(TAG,
                    "Initial AccessibilityServiceInfo:");

            Log.e(TAG,
                    "  eventTypes = " + info.eventTypes);

            Log.e(TAG,
                    "  feedbackType = " + info.feedbackType);

            Log.e(TAG,
                    "  flags = " + info.flags);

            Log.e(TAG,
                    "  notificationTimeout = "
                            + info.notificationTimeout);

            Log.e(TAG,
                    "  packageNames = "
                            + (info.packageNames == null
                            ? "NULL / ALL PACKAGES"
                            : Arrays.toString(info.packageNames)));

            Log.e(TAG,
                    "  capabilities = "
                            + info.getCapabilities());

            boolean canRetrieveContent =
                    (info.getCapabilities()
                            & AccessibilityServiceInfo.CAPABILITY_CAN_RETRIEVE_WINDOW_CONTENT)
                            != 0;

            Log.e(TAG,
                    "  canRetrieveWindowContent = "
                            + canRetrieveContent);

            // --------------------------------------------------------
            // Configure Accessibility Service
            // --------------------------------------------------------

            info.eventTypes =
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                            | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
                            | AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED;

            info.feedbackType =
                    AccessibilityServiceInfo.FEEDBACK_GENERIC;

            info.notificationTimeout = 100;

            info.flags |=
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;

            info.flags |=
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;

            setServiceInfo(info);

            Log.e(TAG,
                    "ServiceInfo configured successfully");

            Log.e(TAG,
                    "  eventTypes = " + info.eventTypes);

            Log.e(TAG,
                    "  flags = " + info.flags);

            Log.e(TAG,
                    "  packageNames = "
                            + (info.packageNames == null
                            ? "NULL / ALL PACKAGES"
                            : Arrays.toString(info.packageNames)));

            Log.e(TAG,
                    "================================================");

        } catch (Exception e) {

            Log.e(TAG,
                    "!!! ERROR IN onServiceConnected() !!!",
                    e);
        }
    }

    // ============================================================
    // ACCESSIBILITY EVENT
    // ============================================================

    @Override
    public void onAccessibilityEvent(
            AccessibilityEvent event) {

        eventCounter++;

        Log.e(TAG,
                "---------------- EVENT #"
                        + eventCounter
                        + " ----------------");

        if (event == null) {

            Log.e(TAG,
                    "EVENT = NULL");

            return;
        }

        CharSequence packageNameCs =
                event.getPackageName();

        String packageName =
                packageNameCs == null
                        ? "NULL"
                        : packageNameCs.toString();

        String className =
                event.getClassName() == null
                        ? "NULL"
                        : event.getClassName().toString();

        String text =
                event.getText() == null
                        ? "NULL"
                        : event.getText().toString();

        String contentDescription =
                event.getContentDescription() == null
                        ? "NULL"
                        : event.getContentDescription().toString();

        Log.e(TAG,
                "eventType = "
                        + event.getEventType());

        Log.e(TAG,
                "package = "
                        + packageName);

        Log.e(TAG,
                "class = "
                        + className);

        Log.e(TAG,
                "text = "
                        + text);

        Log.e(TAG,
                "contentDescription = "
                        + contentDescription);

        Log.e(TAG,
                "windowId = "
                        + event.getWindowId());

        Log.e(TAG,
                "eventTime = "
                        + event.getEventTime());

        // --------------------------------------------------------
        // Only investigate Brave
        // --------------------------------------------------------

        if (!BRAVE_PACKAGE.equals(packageName)) {

            Log.e(TAG,
                    "NOT BRAVE -> ignore event");

            return;
        }

        Log.e(TAG, "");
        Log.e(TAG,
                "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Log.e(TAG,
                ">>> BRAVE DETECTED <<<");
        Log.e(TAG,
                "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        handler.removeCallbacksAndMessages(null);

        handler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        inspectBrave();
                    }
                },
                200
        );
    }

    // ============================================================
    // INSPECT BRAVE WINDOW
    // ============================================================

    private void inspectBrave() {

        Log.e(TAG, "");
        Log.e(TAG,
                "================================================");
        Log.e(TAG,
                "=== inspectBrave() START ===");
        Log.e(TAG,
                "================================================");

        try {

            AccessibilityNodeInfo root =
                    getRootInActiveWindow();

            if (root == null) {

                Log.e(TAG,
                        "!!! getRootInActiveWindow() = NULL !!!");

                Log.e(TAG,
                        "Accessibility Service cannot access Brave UI tree.");

                showDiagnosticOverlay();

                return;
            }

            Log.e(TAG,
                    "getRootInActiveWindow() = SUCCESS");

            Log.e(TAG,
                    "root.package = "
                            + root.getPackageName());

            Log.e(TAG,
                    "root.class = "
                            + root.getClassName());

            Log.e(TAG,
                    "root.childCount = "
                            + root.getChildCount());

            Rect rootBounds =
                    new Rect();

            root.getBoundsInScreen(rootBounds);

            Log.e(TAG,
                    "root.bounds = "
                            + rootBounds.left + ","
                            + rootBounds.top
                            + " - "
                            + rootBounds.right + ","
                            + rootBounds.bottom);

            // ----------------------------------------------------
            // NODE TRAVERSAL
            // ----------------------------------------------------

            nodeCounter = 0;

            Log.e(TAG, "");
            Log.e(TAG,
                    "=== START NODE TRAVERSAL ===");

            RectInfo result =
                    findInterestingNode(root, 0);

            Log.e(TAG,
                    "=== NODE TRAVERSAL FINISHED ===");

            Log.e(TAG,
                    "Total nodes visited = "
                            + nodeCounter);

            if (result != null) {

                Log.e(TAG, "");
                Log.e(TAG,
                        "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                Log.e(TAG,
                        ">>> POSSIBLE TOOLBAR / ADDRESS NODE <<<");
                Log.e(TAG,
                        "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

                Log.e(TAG,
                        "class = "
                                + result.className);

                Log.e(TAG,
                        "text = "
                                + result.text);

                Log.e(TAG,
                        "contentDescription = "
                                + result.contentDescription);

                Log.e(TAG,
                        "viewId = "
                                + result.viewId);

                Log.e(TAG,
                        "bounds = "
                                + result.left + ","
                                + result.top
                                + " - "
                                + result.right + ","
                                + result.bottom);

                Log.e(TAG,
                        "width = "
                                + (result.right - result.left));

                Log.e(TAG,
                        "height = "
                                + (result.bottom - result.top));

            } else {

                Log.e(TAG,
                        "No obvious toolbar/address node found.");
            }

            // ----------------------------------------------------
            // SHOW OVERLAY
            // ----------------------------------------------------

            showDiagnosticOverlay();

            Log.e(TAG,
                    "=== inspectBrave() END ===");

            Log.e(TAG,
                    "================================================");

        } catch (Exception e) {

            Log.e(TAG,
                    "!!! ERROR IN inspectBrave() !!!",
                    e);
        }
    }

    // ============================================================
    // ACCESSIBILITY NODE TRAVERSAL
    // ============================================================

    private RectInfo findInterestingNode(
            AccessibilityNodeInfo node,
            int depth) {

        if (node == null) {
            return null;
        }

        nodeCounter++;

        try {

            String className =
                    node.getClassName() == null
                            ? ""
                            : node.getClassName().toString();

            String text =
                    node.getText() == null
                            ? ""
                            : node.getText().toString();

            String contentDescription =
                    node.getContentDescription() == null
                            ? ""
                            : node.getContentDescription().toString();

            String viewId =
                    node.getViewIdResourceName() == null
                            ? ""
                            : node.getViewIdResourceName();

            Rect bounds =
                    new Rect();

            node.getBoundsInScreen(bounds);

            String combined =
                    (
                            className
                                    + " "
                                    + text
                                    + " "
                                    + contentDescription
                                    + " "
                                    + viewId
                    ).toLowerCase();

            boolean interesting =
                    combined.contains("address")
                            || combined.contains("url")
                            || combined.contains("omnibox")
                            || combined.contains("toolbar")
                            || combined.contains("location")
                            || combined.contains("search")
                            || combined.contains("webview");

            if (interesting) {

                Log.e(TAG,
                        "NODE #"
                                + nodeCounter
                                + " depth="
                                + depth);

                Log.e(TAG,
                        "  class = "
                                + className);

                Log.e(TAG,
                        "  text = "
                                + text);

                Log.e(TAG,
                        "  contentDescription = "
                                + contentDescription);

                Log.e(TAG,
                        "  viewId = "
                                + viewId);

                Log.e(TAG,
                        "  bounds = "
                                + bounds);

                Log.e(TAG,
                        "  childCount = "
                                + node.getChildCount());
            }

            if (combined.contains("address")
                    || combined.contains("omnibox")
                    || combined.contains("toolbar")
                    || combined.contains("location")) {

                return new RectInfo(
                        className,
                        text,
                        contentDescription,
                        viewId,
                        bounds.left,
                        bounds.top,
                        bounds.right,
                        bounds.bottom
                );
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

                RectInfo result =
                        findInterestingNode(
                                child,
                                depth + 1
                        );

                if (result != null) {

                    child.recycle();

                    return result;
                }

                child.recycle();
            }

        } catch (Exception e) {

            Log.e(TAG,
                    "ERROR traversing node #"
                            + nodeCounter,
                    e);
        }

        return null;
    }

    // ============================================================
    // CREATE DIAGNOSTIC OVERLAY
    // ============================================================

    private void showDiagnosticOverlay() {

        Log.e(TAG, "");
        Log.e(TAG,
                "================================================");
        Log.e(TAG,
                "=== showDiagnosticOverlay() START ===");
        Log.e(TAG,
                "================================================");

        if (windowManager == null) {

            Log.e(TAG,
                    "!!! windowManager = NULL !!!");

            return;
        }

        Log.e(TAG,
                "windowManager = OK");

        if (overlay != null) {

            Log.e(TAG,
                    "overlay object already exists");

            Log.e(TAG,
                    "overlay.isAttachedToWindow = "
                            + overlay.isAttachedToWindow());

            return;
        }

        try {

            android.util.DisplayMetrics metrics =
                    getResources().getDisplayMetrics();

            Log.e(TAG,
                    "DisplayMetrics:");

            Log.e(TAG,
                    "  widthPixels = "
                            + metrics.widthPixels);

            Log.e(TAG,
                    "  heightPixels = "
                            + metrics.heightPixels);

            Log.e(TAG,
                    "  density = "
                            + metrics.density);

            Log.e(TAG,
                    "  densityDpi = "
                            + metrics.densityDpi);

            // ----------------------------------------------------
            // Create View
            // ----------------------------------------------------

            overlay =
                    new View(this);

            Log.e(TAG,
                    "overlay View object created");

            // ----------------------------------------------------
            // RED DIAGNOSTIC COLOR
            // ----------------------------------------------------

            overlay.setBackgroundColor(
                    Color.argb(
                            190,
                            255,
                            0,
                            0
                    )
            );

            Log.e(TAG,
                    "overlay background = RED alpha=190");

            int top =
                    dpToPx(24);

            int height =
                    dpToPx(64);

            Log.e(TAG,
                    "Overlay dimensions:");

            Log.e(TAG,
                    "  width = MATCH_PARENT");

            Log.e(TAG,
                    "  height = "
                            + height
                            + " px");

            Log.e(TAG,
                    "  top = "
                            + top
                            + " px");

            WindowManager.LayoutParams params =
                    new WindowManager.LayoutParams(
                            WindowManager.LayoutParams.MATCH_PARENT,
                            height,
                            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                            android.graphics.PixelFormat.TRANSLUCENT
                    );

            params.gravity =
                    Gravity.TOP | Gravity.LEFT;

            params.x = 0;
            params.y = top;

            Log.e(TAG,
                    "Window LayoutParams created:");

            Log.e(TAG,
                    "  type = TYPE_ACCESSIBILITY_OVERLAY");

            Log.e(TAG,
                    "  gravity = TOP | LEFT");

            Log.e(TAG,
                    "  flags = "
                            + params.flags);

            Log.e(TAG,
                    "  x = "
                            + params.x);

            Log.e(TAG,
                    "  y = "
                            + params.y);

            Log.e(TAG,
                    "  width = "
                            + params.width);

            Log.e(TAG,
                    "  height = "
                            + params.height);

            // ----------------------------------------------------
            // Attach listener
            // ----------------------------------------------------

            overlay.addOnAttachStateChangeListener(
                    new View.OnAttachStateChangeListener() {

                        @Override
                        public void onViewAttachedToWindow(
                                View v) {

                            Log.e(TAG,
                                    "!!! VIEW ATTACHED TO WINDOW !!!");

                            Log.e(TAG,
                                    "isAttachedToWindow = "
                                            + v.isAttachedToWindow());
                        }

                        @Override
                        public void onViewDetachedFromWindow(
                                View v) {

                            Log.e(TAG,
                                    "!!! VIEW DETACHED FROM WINDOW !!!");

                            Log.e(TAG,
                                    "isAttachedToWindow = "
                                            + v.isAttachedToWindow());
                        }
                    }
            );

            // ----------------------------------------------------
            // ADD VIEW
            // ----------------------------------------------------

            Log.e(TAG, "");
            Log.e(TAG,
                    "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            Log.e(TAG,
                    "CALLING WindowManager.addView()");
            Log.e(TAG,
                    "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

            windowManager.addView(
                    overlay,
                    params
            );

            // ----------------------------------------------------
            // SUCCESS
            // ----------------------------------------------------

            Log.e(TAG, "");
            Log.e(TAG,
                    "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            Log.e(TAG,
                    "!!! OVERLAY ADD SUCCESS !!!");
            Log.e(TAG,
                    "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

            Log.e(TAG,
                    "overlay = "
                            + overlay);

            Log.e(TAG,
                    "isAttachedToWindow = "
                            + overlay.isAttachedToWindow());

            // ----------------------------------------------------
            // POST CHECK
            // ----------------------------------------------------

            handler.postDelayed(
                    new Runnable() {

                        @Override
                        public void run() {

                            if (overlay == null) {

                                Log.e(TAG,
                                        "POST-CHECK: overlay = NULL");

                                return;
                            }

                            Log.e(TAG,
                                    "=== OVERLAY POST-CHECK ===");

                            Log.e(TAG,
                                    "overlay = "
                                            + overlay);

                            Log.e(TAG,
                                    "isAttachedToWindow = "
                                            + overlay.isAttachedToWindow());

                            Log.e(TAG,
                                    "visibility = "
                                            + overlay.getVisibility());

                            Log.e(TAG,
                                    "alpha = "
                                            + overlay.getAlpha());

                            Log.e(TAG,
                                    "width = "
                                            + overlay.getWidth());

                            Log.e(TAG,
                                    "height = "
                                            + overlay.getHeight());

                            int[] location =
                                    new int[2];

                            overlay.getLocationOnScreen(
                                    location
                            );

                            Log.e(TAG,
                                    "screen location = "
                                            + location[0]
                                            + ","
                                            + location[1]);
                        }
                    },
                    300
            );

        } catch (WindowManager.BadTokenException e) {

            Log.e(TAG,
                    "!!! OVERLAY ERROR: BadTokenException !!!",
                    e);

            overlay = null;

        } catch (WindowManager.InvalidDisplayException e) {

            Log.e(TAG,
                    "!!! OVERLAY ERROR: InvalidDisplayException !!!",
                    e);

            overlay = null;

        } catch (SecurityException e) {

            Log.e(TAG,
                    "!!! OVERLAY ERROR: SecurityException !!!",
                    e);

            overlay = null;

        } catch (IllegalArgumentException e) {

            Log.e(TAG,
                    "!!! OVERLAY ERROR: IllegalArgumentException !!!",
                    e);

            overlay = null;

        } catch (Exception e) {

            Log.e(TAG,
                    "!!! OVERLAY ERROR: UNKNOWN EXCEPTION !!!",
                    e);

            overlay = null;
        }

        Log.e(TAG,
                "=== showDiagnosticOverlay() END ===");

        Log.e(TAG,
                "================================================");
    }

    // ============================================================
    // REMOVE OVERLAY
    // ============================================================

    private void removeOverlay() {

        Log.e(TAG,
                "=== removeOverlay() ===");

        if (overlay == null) {

            Log.e(TAG,
                    "overlay = NULL -> nothing to remove");

            return;
        }

        if (windowManager == null) {

            Log.e(TAG,
                    "windowManager = NULL");

            overlay = null;

            return;
        }

        try {

            Log.e(TAG,
                    "Removing overlay...");

            Log.e(TAG,
                    "isAttachedToWindow before remove = "
                            + overlay.isAttachedToWindow());

            windowManager.removeView(
                    overlay
            );

            Log.e(TAG,
                    "Overlay removed successfully");

        } catch (Exception e) {

            Log.e(TAG,
                    "ERROR removing overlay",
                    e);

        } finally {

            overlay = null;
        }
    }

    // ============================================================
    // DP -> PX
    // ============================================================

    private int dpToPx(int dp) {

        float density =
                getResources()
                        .getDisplayMetrics()
                        .density;

        return Math.round(
                dp * density
        );
    }

    // ============================================================
    // INTERRUPT
    // ============================================================

    @Override
    public void onInterrupt() {

        Log.e(TAG, "");
        Log.e(TAG,
                "================================================");
        Log.e(TAG,
                "=== SERVICE INTERRUPTED ===");
        Log.e(TAG,
                "================================================");

        removeOverlay();
    }

    // ============================================================
    // DESTROY
    // ============================================================

    @Override
    public void onDestroy() {

        Log.e(TAG, "");
        Log.e(TAG,
                "================================================");
        Log.e(TAG,
                "=== SERVICE DESTROYED ===");
        Log.e(TAG,
                "================================================");

        Log.e(TAG,
                "PROCESS ID = " + android.os.Process.myPid());

        removeOverlay();

        super.onDestroy();
    }

    // ============================================================
    // RECT INFO
    // ============================================================

    private static class RectInfo {

        String className;
        String text;
        String contentDescription;
        String viewId;

        int left;
        int top;
        int right;
        int bottom;

        RectInfo(
                String className,
                String text,
                String contentDescription,
                String viewId,
                int left,
                int top,
                int right,
                int bottom) {

            this.className = className;
            this.text = text;
            this.contentDescription =
                    contentDescription;

            this.viewId = viewId;

            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }
    }
}
