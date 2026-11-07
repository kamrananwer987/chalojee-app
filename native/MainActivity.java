package com.chaloje.app;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.getcapacitor.BridgeActivity;
import com.getcapacitor.BridgeWebChromeClient;
import com.getcapacitor.WebViewListener;

/**
 * Chaloje app shell: the live website plus a native bottom tab bar,
 * pull-to-refresh, a top loading bar and double-back-to-exit.
 */
public class MainActivity extends BridgeActivity {

    private static final String BASE = "https://chaloje.com";
    private static final int INDIGO = Color.parseColor("#4338CA");
    private static final int GREY = Color.parseColor("#6B7280");

    private static final String[] LABELS = {"Flights", "Packages", "My Trips", "Support", "Account"};
    private static final String[] PATHS = {"/", "/?product=package", "/flights/manage-booking", "/contact", "/customer/login"};
    private static final int[] ICONS = {
            R.drawable.ic_tab_flight, R.drawable.ic_tab_packages, R.drawable.ic_tab_trips,
            R.drawable.ic_tab_support, R.drawable.ic_tab_account
    };

    private WebView webView;
    private SwipeRefreshLayout swipe;
    private ProgressBar progress;
    private LinearLayout tabBar;
    private final ImageView[] tabIcons = new ImageView[LABELS.length];
    private final TextView[] tabLabels = new TextView[LABELS.length];
    private long lastBackPress = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = bridge.getWebView();
        buildLayout();
        hookWebView();
        hookBackButton();
    }

    private int dp(float v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    private void buildLayout() {
        ViewGroup oldParent = (ViewGroup) webView.getParent();
        if (oldParent != null) oldParent.removeView(webView);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        // Web content with pull-to-refresh and a thin loading bar on top
        FrameLayout content = new FrameLayout(this);
        swipe = new SwipeRefreshLayout(this);
        swipe.setColorSchemeColors(INDIGO, Color.parseColor("#F97316"));
        swipe.setDistanceToTriggerSync(dp(120));
        swipe.addView(webView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        swipe.setOnChildScrollUpCallback((parent, child) -> webView.getScrollY() > 0);
        swipe.setOnRefreshListener(() -> webView.reload());
        content.addView(swipe, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setIndeterminate(false);
        progress.setProgressTintList(ColorStateList.valueOf(INDIGO));
        progress.setProgressBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        progress.setVisibility(View.GONE);
        FrameLayout.LayoutParams plp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(3));
        plp.gravity = Gravity.TOP;
        content.addView(progress, plp);

        root.addView(content, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // Divider + tab bar
        View divider = new View(this);
        divider.setBackgroundColor(Color.parseColor("#E5E7EB"));
        root.addView(divider, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));

        tabBar = new LinearLayout(this);
        tabBar.setOrientation(LinearLayout.HORIZONTAL);
        tabBar.setBackgroundColor(Color.WHITE);
        for (int i = 0; i < LABELS.length; i++) {
            tabBar.addView(makeTab(i), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        }
        root.addView(tabBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(60)));

        setContentView(root);

        // Edge-to-edge: pad for status bar / nav bar; hide tabs while the keyboard is open
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat ctl = new WindowInsetsControllerCompat(getWindow(), root);
        ctl.setAppearanceLightStatusBars(true);
        ctl.setAppearanceLightNavigationBars(true);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            boolean keyboard = ime.bottom > bars.bottom;
            tabBar.setVisibility(keyboard ? View.GONE : View.VISIBLE);
            divider.setVisibility(keyboard ? View.GONE : View.VISIBLE);
            v.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, ime.bottom));
            return WindowInsetsCompat.CONSUMED;
        });

        selectTab(0);
    }

    private View makeTab(final int i) {
        LinearLayout tab = new LinearLayout(this);
        tab.setOrientation(LinearLayout.VERTICAL);
        tab.setGravity(Gravity.CENTER);
        TypedValue ripple = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, ripple, true);
        tab.setBackgroundResource(ripple.resourceId);
        tab.setContentDescription(LABELS[i]);

        ImageView icon = new ImageView(this);
        icon.setImageResource(ICONS[i]);
        tab.addView(icon, new LinearLayout.LayoutParams(dp(24), dp(24)));

        TextView label = new TextView(this);
        label.setText(LABELS[i]);
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        label.setGravity(Gravity.CENTER);
        label.setPadding(0, dp(2), 0, 0);
        tab.addView(label, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        tabIcons[i] = icon;
        tabLabels[i] = label;
        tab.setOnClickListener(v -> {
            selectTab(i);
            webView.loadUrl(BASE + PATHS[i]);
        });
        return tab;
    }

    private void selectTab(int selected) {
        for (int i = 0; i < LABELS.length; i++) {
            int c = (i == selected) ? INDIGO : GREY;
            tabIcons[i].setColorFilter(c);
            tabLabels[i].setTextColor(c);
            tabLabels[i].setTypeface(null, i == selected ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    private int tabForUrl(String url) {
        if (url == null) return -1;
        Uri u = Uri.parse(url);
        String host = u.getHost();
        if (host == null || !host.endsWith("chaloje.com")) return -1;
        String path = u.getPath() == null ? "/" : u.getPath();
        String query = u.getQuery() == null ? "" : u.getQuery();
        if (path.startsWith("/flights/manage-booking")) return 2;
        if (path.startsWith("/customer")) return 4;
        if (path.startsWith("/contact")) return 3;
        if (path.startsWith("/travel-packages") || query.contains("product=package")) return 1;
        if (path.equals("/") || path.isEmpty() || path.startsWith("/flights")) return 0;
        return -1;
    }

    private void hookWebView() {
        bridge.setWebChromeClient(new BridgeWebChromeClient(bridge) {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progress.setProgress(newProgress);
                progress.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
            }
        });

        bridge.addWebViewListener(new WebViewListener() {
            @Override
            public void onPageLoaded(WebView view) {
                swipe.setRefreshing(false);
                progress.setVisibility(View.GONE);
                selectTab(tabForUrl(view.getUrl()));
            }
        });
    }

    private void hookBackButton() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack();
                    return;
                }
                long now = System.currentTimeMillis();
                if (now - lastBackPress < 2000) {
                    finish();
                } else {
                    lastBackPress = now;
                    Toast.makeText(MainActivity.this, "Press back again to exit", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
