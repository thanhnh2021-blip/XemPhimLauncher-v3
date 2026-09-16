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
import android.view.ViewTreeObserver;
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
    // SERVICE CONNECTED
    // ============================================================

    @Override
    protected void onServiceConnected() {

        super.onServiceConnected();

        Log.d(TAG, "");
        Log.d(TAG, "================================================");
        Log.d(TAG, "=== SERVICE CONNECTED ===");
        Log.d(TAG, "================================================");

        try {

            windowManager =
                    (WindowManager) getSystemService(WINDOW_SERVICE);

            Log.d(TAG,
                    "WindowManager = " + windowManager);

            AccessibilityServiceInfo info =
                    getServiceInfo();

            if (info == null) {

                Log.e(TAG,
                        "!!! getServiceInfo() = NULL !!!");

                return;
            }

            Log.d(TAG,
                    "Initial AccessibilityServiceInfo:");

            Log.d(TAG,
                    "  eventTypes = " + info.eventTypes);

            Log.d(TAG,
                    "  feedbackType = " + info.feedbackType);

            Log.d(TAG,
                    "  flags = " + info.flags);

            Log.d(TAG,
                    "  notificationTimeout = "
                            + info.notificationTimeout);

            Log.d(TAG,
                    "  packageNames = "
                            + (info.packageNames == null
                            ? "NULL / ALL PACKAGES"
                            : Arrays.toString(info.packageNames)));

            Log.d(TAG,
                    "  capabilities = "
                            + info.getCapabilities());

            boolean canRetrieveContent =
                    (info.getCapabilities()
                            & AccessibilityServiceInfo.CAPABILITY_CAN_RETRIEVE_WINDOW_CONTENT)
                            != 0;

            Log.d(TAG,
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

            Log.d(TAG,
                    "ServiceInfo configured successfully");

            Log.d(TAG,
                    "  eventTypes = " + info.eventTypes);

            Log.d(TAG,
                    "  flags = " + info.flags);

            Log.d(TAG,
                    "  packageNames = "
                            + (info.packageNames == null
                            ? "NULL / ALL PACKAGES"
                            : Arrays.toString(info.packageNames)));

            Log.d(TAG,
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

        Log.d(TAG, "");
        Log.d(TAG,
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

        Log.d(TAG,
                "eventType = "
                        + event.getEventType());

        Log.d(TAG,
                "package = "
                        + packageName);

        Log.d(TAG,
                "class = "
                        + className);

        Log.d(TAG,
                "text = "
                        + text);

        Log.d(TAG,
                "contentDescription = "
                        + contentDescription);

        Log.d(TAG,
                "windowId = "
                        + event.getWindowId());

        Log.d(TAG,
                "eventTime = "
                        + event.getEventTime());

        // --------------------------------------------------------
        // Only investigate Brave
        // --------------------------------------------------------

        if (!BRAVE_PACKAGE.equals(packageName)) {

            Log.d(TAG,
                    "NOT BRAVE -> ignore event");

            // IMPORTANT:
            // Không remove overlay ở đây.
            // Android có thể phát rất nhiều event của
            // FreeKiosk / Xem Phim / launcher trong lúc
            // chuyển sang Brave.
            return;
        }

        Log.d(TAG, "");
        Log.d(TAG,
                "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Log.d(TAG,
                ">>> BRAVE DETECTED <<<");
        Log.d(TAG,
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

        Log.d(TAG, "");
        Log.d(TAG,
                "================================================");
        Log.d(TAG,
                "=== inspectBrave() START ===");
        Log.d(TAG,
                "================================================");

        try {

            AccessibilityNodeInfo root =
                    getRootInActiveWindow();

            // ----------------------------------------------------
            // ROOT CHECK
            // ----------------------------------------------------

            if (root == null) {

                Log.e(TAG,
                        "!!! getRootInActiveWindow() = NULL !!!");

                Log.e(TAG,
                        "Accessibility Service cannot access "
                                + "Brave UI tree.");

                Log.d(TAG,
                        "================================================");

                showDiagnosticOverlay();

                return;
            }

            Log.d(TAG,
                    "getRootInActiveWindow() = SUCCESS");

            Log.d(TAG,
                    "root.package = "
                            + root.getPackageName());

            Log.d(TAG,
                    "root.class = "
                            + root.getClassName());

            Log.d(TAG,
                    "root.childCount = "
                            + root.getChildCount());

            Rect rootBounds =
                    new Rect();

            root.getBoundsInScreen(rootBounds);

            Log.d(TAG,
                    "root.bounds = "
                            + rootBounds.left + ","
                            + rootBounds.top + " - "
                            + rootBounds.right + ","
                            + rootBounds.bottom);

            // ----------------------------------------------------
            // NODE TRAVERSAL
            // ----------------------------------------------------

            nodeCounter = 0;

            Log.d(TAG, "");
            Log.d(TAG,
                    "=== START NODE TRAVERSAL ===");

            RectInfo result =
                    findInterestingNode(root, 0);

            Log.d(TAG,
                    "=== NODE TRAVERSAL FINISHED ===");

            Log.d(TAG,
                    "Total nodes visited = "
                            + nodeCounter);

            // ----------------------------------------------------
            // RESULT
            // ----------------------------------------------------

            if (result != null) {

                Log.d(TAG, "");
                Log.d(TAG,
                        "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                Log.d(TAG,
                        ">>> POSSIBLE TOOLBAR / ADDRESS NODE <<<");
                Log.d(TAG,
                        "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

                Log.d(TAG,
                        "class = "
                                + result.className);

                Log.d(TAG,
                        "text = "
                                + result.text);

                Log.d(TAG,
                        "viewId = "
                                + result.viewId);

                Log.d(TAG,
                        "bounds = "
                                + result.left + ","
                                + result.top + " - "
                                + result.right + ","
                                + result.bottom);

                Log.d(TAG,
                        "width = "
                                + (result.right - result.left));

                Log.d(TAG,
                        "height = "
                                + (result.bottom - result.top));

            } else {

                Log.d(TAG,
                        "No obvious toolbar/address node found.");
            }

            // ----------------------------------------------------
            // SHOW OVERLAY
            // ----------------------------------------------------

            showDiagnosticOverlay();

            Log.d(TAG,
                    "=== inspectBrave() END ===");

            Log.d(TAG,
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

            // ----------------------------------------------------
            // Log potentially relevant nodes
            // ----------------------------------------------------

            boolean interesting =
                    combined.contains("address")
                            || combined.contains("url")
                            || combined.contains("omnibox")
                            || combined.contains("toolbar")
                            || combined.contains("location")
                            || combined.contains("search")
                            || combined.contains("webview");

            if (interesting) {

                Log.d(TAG,
                        "NODE #"
                                + nodeCounter
                                + " depth="
                                + depth);

                Log.d(TAG,
                        "  class = "
                                + className);

                Log.d(TAG,
                        "  text = "
                                + text);

                Log.d(TAG,
                        "  contentDescription = "
                                + contentDescription);

                Log.d(TAG,
                        "  viewId = "
                                + viewId);

                Log.d(TAG,
                        "  bounds = "
                                + bounds);

                Log.d(TAG,
                        "  childCount = "
                                + node.getChildCount());
            }

            // ----------------------------------------------------
            // Detect likely address / toolbar node
            // ----------------------------------------------------

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

            // ----------------------------------------------------
            // Traverse children
            // ----------------------------------------------------

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

        Log.d(TAG, "");
        Log.d(TAG,
                "================================================");
        Log.d(TAG,
                "=== showDiagnosticOverlay() START ===");
        Log.d(TAG,
                "================================================");

        // --------------------------------------------------------
        // WindowManager check
        // --------------------------------------------------------

        if (windowManager == null) {

            Log.e(TAG,
                    "!!! windowManager = NULL !!!");

            Log.e(TAG,
                    "Cannot call WindowManager.addView()");

            return;
        }

        Log.d(TAG,
                "windowManager = OK");

        // --------------------------------------------------------
        // Existing overlay check
        // --------------------------------------------------------

        if (overlay != null) {

            Log.d(TAG,
                    "overlay object already exists");

            Log.d(TAG,
                    "overlay.isAttachedToWindow = "
                            + overlay.isAttachedToWindow());

            return;
        }

        try {

            // ----------------------------------------------------
            // Screen information
            // ----------------------------------------------------

            android.util.DisplayMetrics metrics =
                    getResources().getDisplayMetrics();

            Log.d(TAG,
                    "DisplayMetrics:");

            Log.d(TAG,
                    "  widthPixels = "
                            + metrics.widthPixels);

            Log.d(TAG,
                    "  heightPixels = "
                            + metrics.heightPixels);

            Log.d(TAG,
                    "  density = "
                            + metrics.density);

            Log.d(TAG,
                    "  densityDpi = "
                            + metrics.densityDpi);

            // ----------------------------------------------------
            // Create View
            // ----------------------------------------------------

            overlay =
                    new View(this);

            Log.d(TAG,
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

            Log.d(TAG,
                    "overlay background = "
                            + "RED alpha=190");

            // ----------------------------------------------------
            // Position
            // ----------------------------------------------------

            int top =
                    dpToPx(24);

            int height =
                    dpToPx(64);

            Log.d(TAG,
                    "Overlay dimensions:");

            Log.d(TAG,
                    "  width = MATCH_PARENT");

            Log.d(TAG,
                    "  height = "
                            + height
                            + " px");

            Log.d(TAG,
                    "  top = "
                            + top
                            + " px");

            // ----------------------------------------------------
            // Window parameters
            // ----------------------------------------------------

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

            Log.d(TAG,
                    "Window LayoutParams created:");

            Log.d(TAG,
                    "  type = TYPE_ACCESSIBILITY_OVERLAY");

            Log.d(TAG,
                    "  gravity = TOP | LEFT");

            Log.d(TAG,
                    "  flags = "
                            + params.flags);

            Log.d(TAG,
                    "  x = "
                            + params.x);

            Log.d(TAG,
                    "  y = "
                            + params.y);

            Log.d(TAG,
                    "  width = "
                            + params.width);

            Log.d(TAG,
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

                            Log.d(TAG,
                                    "!!! VIEW ATTACHED TO WINDOW !!!");

                            Log.d(TAG,
                                    "isAttachedToWindow = "
                                            + v.isAttachedToWindow());
                        }

                        @Override
                        public void onViewDetachedFromWindow(
                                View v) {

                            Log.d(TAG,
                                    "!!! VIEW DETACHED FROM WINDOW !!!");

                            Log.d(TAG,
                                    "isAttachedToWindow = "
                                            + v.isAttachedToWindow());
                        }
                    }
            );

            // ----------------------------------------------------
            // IMPORTANT:
            // addView()
            // ----------------------------------------------------

            Log.d(TAG, "");
            Log.d(TAG,
                    "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            Log.d(TAG,
                    "CALLING WindowManager.addView()");
            Log.d(TAG,
                    "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

            windowManager.addView(
                    overlay,
                    params
            );

            // ----------------------------------------------------
            // SUCCESS
            // ----------------------------------------------------

            Log.d(TAG, "");
            Log.d(TAG,
                    "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            Log.d(TAG,
                    "!!! OVERLAY ADD SUCCESS !!!");
            Log.d(TAG,
                    "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

            Log.d(TAG,
                    "overlay = "
                            + overlay);

            Log.d(TAG,
                    "isAttachedToWindow = "
                            + overlay.isAttachedToWindow());

            // ----------------------------------------------------
            // Check again after UI thread processes attach
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

                            Log.d(TAG,
                                    "=== OVERLAY POST-CHECK ===");

                            Log.d(TAG,
                                    "overlay = "
                                            + overlay);

                            Log.d(TAG,
                                    "isAttachedToWindow = "
                                            + overlay.isAttachedToWindow());

                            Log.d(TAG,
                                    "visibility = "
                                            + overlay.getVisibility());

                            Log.d(TAG,
                                    "alpha = "
                                            + overlay.getAlpha());

                            Log.d(TAG,
                                    "width = "
                                            + overlay.getWidth());

                            Log.d(TAG,
                                    "height = "
                                            + overlay.getHeight());

                            int[] location =
                                    new int[2];

                            overlay.getLocationOnScreen(
                                    location
                            );

                            Log.d(TAG,
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

        Log.d(TAG,
                "=== showDiagnosticOverlay() END ===");

        Log.d(TAG,
                "================================================");
    }

    // ============================================================
    // REMOVE OVERLAY
    // ============================================================

    private void removeOverlay() {

        Log.d(TAG,
                "=== removeOverlay() ===");

        if (overlay == null) {

            Log.d(TAG,
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

            Log.d(TAG,
                    "Removing overlay...");

            Log.d(TAG,
                    "isAttachedToWindow before remove = "
                            + overlay.isAttachedToWindow());

            windowManager.removeView(
                    overlay
            );

            Log.d(TAG,
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

        Log.d(TAG,
                "================================================");

        Log.d(TAG,
                "=== SERVICE INTERRUPTED ===");

        Log.d(TAG,
                "================================================");

        removeOverlay();
    }

    // ============================================================
    // DESTROY
    // ============================================================

    @Override
    public void onDestroy() {

        Log.d(TAG,
                "================================================");

        Log.d(TAG,
                "=== SERVICE DESTROYED ===");

        Log.d(TAG,
                "================================================");

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
