package com.dumbphone.mousetrap;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MainActivity extends Activity {
    private static final Set<String> BUILT_IN_MOUSE_TARGETS = new LinkedHashSet<>(Arrays.asList(
            "com.openbubbles.messaging",
            "com.android.chrome",
            "org.chromium.chrome",
            "com.google.android.apps.mapslite",
            "com.ubercab.uberlite",
            "de.danoeh.antennapod",
            "com.spotify.music",
            "com.apple.android.music"));

    private static final String LAUNCHER = "com.offlineinc.dumbdownlauncher";

    private final List<AppEntry> apps = new ArrayList<>();
    private ListView listView;
    private TextView status;
    private TextView saveAction;
    private AppAdapter adapter;
    private int interceptedKeyCode = -1;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        configureWindow();
        buildUi();
        loadApps();
    }

    private void configureWindow() {
        getWindow().requestFeature(14);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER
                | WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.dimAmount = 0.85f;
        getWindow().setAttributes(attributes);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.TRANSPARENT);

        TextView title = textView("Mouse Trap", 20, Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        root.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));

        status = textView("Loading…", 14, Gravity.CENTER);
        status.setTextColor(Color.WHITE);
        status.setPadding(dp(8), dp(2), dp(8), dp(2));
        status.setBackground(roundedDrawable(0xB0000000, 5));
        LinearLayout.LayoutParams statusLayout = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        statusLayout.gravity = Gravity.CENTER_HORIZONTAL;
        statusLayout.setMargins(0, 0, 0, dp(3));
        root.addView(status, statusLayout);

        listView = new ListView(this);
        listView.setId(View.generateViewId());
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setDivider(null);
        listView.setCacheColorHint(Color.TRANSPARENT);
        listView.setBackgroundColor(Color.TRANSPARENT);
        listView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapter != null) adapter.notifyDataSetChanged();
                updateStatus(countCheckedTargets());
            }
        });
        root.addView(listView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        setContentView(root);
        configureSaveBar();
    }

    private void configureSaveBar() {
        try {
            Object menuBar = Activity.class.getMethod("getMenuBar").invoke(this);
            if (!(menuBar instanceof ViewGroup)) {
                throw new IllegalStateException("TCL menu bar unavailable");
            }
            menuBar.getClass().getMethod(
                    "updateMenuBar",
                    String.class,
                    String.class,
                    String.class,
                    List.class)
                    .invoke(menuBar, "", "Save Mouse Targets", "", null);

            ViewGroup bar = (ViewGroup) menuBar;
            View barBackground = bar.getChildAt(0);
            if (barBackground != null) barBackground.setBackgroundColor(Color.TRANSPARENT);

            int centerId = getResources().getIdentifier("menu_csk", "id", "android");
            saveAction = centerId == 0 ? null : bar.findViewById(centerId);
            if (saveAction == null) {
                throw new IllegalStateException("TCL center menu action unavailable");
            }
            ViewGroup.LayoutParams saveLayout = saveAction.getLayoutParams();
            saveLayout.width = getResources().getDisplayMetrics().widthPixels - dp(16);
            saveAction.setLayoutParams(saveLayout);
            saveAction.setTextSize(15);
            saveAction.setGravity(Gravity.CENTER);
            saveAction.setTextColor(actionTextColors());
            saveAction.setBackground(actionBackground());
            saveAction.setFocusable(true);
            saveAction.setClickable(true);
            saveAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    saveTargets();
                }
            });
        } catch (Exception e) {
            throw new IllegalStateException("TCL menu bar unavailable", e);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (event.getAction() == KeyEvent.ACTION_UP && keyCode == interceptedKeyCode) {
            interceptedKeyCode = -1;
            return true;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
            boolean center = keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                    || keyCode == KeyEvent.KEYCODE_ENTER
                    || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER;
            if (saveAction != null && saveAction.hasFocus()) {
                if (center) {
                    interceptedKeyCode = keyCode;
                    saveAction.performClick();
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    interceptedKeyCode = keyCode;
                    listView.requestFocus();
                    if (!apps.isEmpty()) listView.setSelection(apps.size() - 1);
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    interceptedKeyCode = keyCode;
                    return true;
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                    && listView != null
                    && listView.hasFocus()
                    && !apps.isEmpty()
                    && listView.getSelectedItemPosition() == apps.size() - 1
                    && saveAction != null
                    && saveAction.requestFocus()) {
                interceptedKeyCode = keyCode;
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void loadApps() {
        PackageManager pm = getPackageManager();
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolved = pm.queryIntentActivities(launcherIntent, 0);
        Map<String, AppEntry> unique = new LinkedHashMap<>();
        for (ResolveInfo info : resolved) {
            if (info.activityInfo == null) continue;
            String pkg = info.activityInfo.packageName;
            if (getPackageName().equals(pkg)) continue;
            if (LAUNCHER.equals(pkg) || BUILT_IN_MOUSE_TARGETS.contains(pkg)) continue;
            if (!info.activityInfo.enabled || !info.activityInfo.applicationInfo.enabled) continue;
            if ((info.activityInfo.applicationInfo.flags & (ApplicationInfo.FLAG_SYSTEM
                    | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0) continue;
            if (pm.getLaunchIntentForPackage(pkg) == null) continue;
            CharSequence labelCs = info.loadLabel(pm);
            String label = labelCs == null ? pkg : labelCs.toString();
            unique.put(pkg, new AppEntry(label, pkg, info.loadIcon(pm)));
        }
        apps.clear();
        apps.addAll(unique.values());
        Collections.sort(apps, new Comparator<AppEntry>() {
            @Override
            public int compare(AppEntry left, AppEntry right) {
                return left.label.compareToIgnoreCase(right.label);
            }
        });

        adapter = new AppAdapter();
        listView.setAdapter(adapter);

        Set<String> selected = TargetStore.read(this);
        for (int i = 0; i < apps.size(); i++) {
            listView.setItemChecked(i, selected.contains(apps.get(i).packageName));
        }
        adapter.notifyDataSetChanged();
        if (!apps.isEmpty()) {
            listView.setSelection(0);
            listView.requestFocus();
        }
        updateStatus(countCheckedTargets());
    }

    private void saveTargets() {
        LinkedHashSet<String> selected = new LinkedHashSet<>();
        for (int i = 0; i < apps.size(); i++) {
            if (listView.isItemChecked(i)) selected.add(apps.get(i).packageName);
        }
        try {
            TargetStore.write(this, selected);
            updateStatus(selected.size());
            Toast.makeText(this, "Mouse Targets saved", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            status.setText("Setup required: grant WRITE_SECURE_SETTINGS over ADB.");
            Toast.makeText(this, "Mouse Trap needs its one-time setup permission", Toast.LENGTH_LONG).show();
        } catch (Throwable t) {
            status.setText("Could not save: " + t.getMessage());
        }
    }

    private void updateStatus(int count) {
        boolean granted = checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS)
                == PackageManager.PERMISSION_GRANTED;
        status.setText((granted ? "Ready" : "Setup permission missing")
                + " • " + count + " Custom Target" + (count == 1 ? "" : "s"));
    }

    private int countCheckedTargets() {
        int count = 0;
        for (int i = 0; i < apps.size(); i++) {
            if (listView.isItemChecked(i)) count++;
        }
        return count;
    }

    private TextView textView(String text, int sizeSp, int gravity) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(sizeSp);
        view.setGravity(gravity);
        view.setFontFeatureSettings("kern");
        return view;
    }

    private StateListDrawable rowBackground() {
        StateListDrawable background = new StateListDrawable();
        ColorDrawable selected = new ColorDrawable(Color.WHITE);
        background.addState(new int[]{android.R.attr.state_selected}, selected);
        background.addState(new int[]{android.R.attr.state_pressed}, selected);
        background.addState(new int[]{}, new ColorDrawable(Color.TRANSPARENT));
        return background;
    }

    private StateListDrawable badgeBackground(int alpha) {
        StateListDrawable background = new StateListDrawable();
        ColorDrawable selected = new ColorDrawable(Color.TRANSPARENT);
        background.addState(new int[]{android.R.attr.state_selected}, selected);
        background.addState(new int[]{android.R.attr.state_pressed}, selected);
        background.addState(new int[]{}, roundedDrawable(Color.argb(alpha, 0, 0, 0), 5));
        return background;
    }

    private ColorStateList rowTextColors() {
        return new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_selected},
                        new int[]{android.R.attr.state_pressed},
                        new int[]{}},
                new int[]{Color.BLACK, Color.BLACK, Color.WHITE});
    }

    private ColorStateList checkboxTintColors() {
        return new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_selected},
                        new int[]{android.R.attr.state_pressed},
                        new int[]{}},
                new int[]{Color.BLACK, Color.BLACK, Color.WHITE});
    }

    private StateListDrawable actionBackground() {
        StateListDrawable background = new StateListDrawable();
        ColorDrawable focused = new ColorDrawable(Color.WHITE);
        background.addState(new int[]{android.R.attr.state_focused}, focused);
        background.addState(new int[]{android.R.attr.state_pressed}, focused);
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xAA000000, 0xFF000000});
        gradient.setCornerRadius(dp(5));
        background.addState(new int[]{}, gradient);
        return background;
    }

    private ColorStateList actionTextColors() {
        return new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_focused},
                        new int[]{android.R.attr.state_pressed},
                        new int[]{}},
                new int[]{Color.BLACK, Color.BLACK, Color.WHITE});
    }

    private GradientDrawable roundedDrawable(int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private final class AppAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return apps.size();
        }

        @Override
        public AppEntry getItem(int position) {
            return apps.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CheckableRow row;
            if (convertView instanceof CheckableRow) {
                row = (CheckableRow) convertView;
            } else {
                row = new CheckableRow();
            }
            row.bind(getItem(position));
            row.setChecked(listView.isItemChecked(position));
            return row;
        }
    }

    private final class CheckableRow extends LinearLayout implements Checkable {
        private final ImageView icon;
        private final TextView label;
        private final CheckBox checkbox;
        private boolean checked;

        CheckableRow() {
            super(MainActivity.this);
            setOrientation(HORIZONTAL);
            setGravity(Gravity.CENTER_VERTICAL);
            setPadding(dp(10), dp(4), dp(8), dp(4));
            setMinimumHeight(dp(44));
            setBackground(rowBackground());

            icon = new ImageView(MainActivity.this);
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            addView(icon, new LinearLayout.LayoutParams(dp(36), dp(36)));

            label = textView("", 18, Gravity.CENTER_VERTICAL);
            label.setTextColor(rowTextColors());
            label.setDuplicateParentStateEnabled(true);
            label.setSingleLine(false);
            label.setMaxLines(3);
            label.setHorizontallyScrolling(false);
            label.setMinHeight(dp(28));
            label.setPadding(dp(10), 0, dp(4), 0);
            label.setBackground(badgeBackground(185));
            LinearLayout.LayoutParams labelLayout = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            addView(label, labelLayout);

            checkbox = new CheckBox(MainActivity.this);
            checkbox.setDuplicateParentStateEnabled(true);
            checkbox.setButtonTintList(checkboxTintColors());
            checkbox.setBackground(badgeBackground(210));
            checkbox.setGravity(Gravity.CENTER);
            checkbox.setClickable(false);
            checkbox.setFocusable(false);
            LinearLayout.LayoutParams checkboxLayout = new LinearLayout.LayoutParams(dp(38), dp(32));
            checkboxLayout.setMargins(dp(4), 0, 0, 0);
            addView(checkbox, checkboxLayout);
        }

        void bind(AppEntry app) {
            icon.setImageDrawable(app.icon);
            label.setText(app.label);
            setContentDescription(app.label);
        }

        @Override
        public void setChecked(boolean value) {
            checked = value;
            checkbox.setChecked(value);
        }

        @Override
        public boolean isChecked() {
            return checked;
        }

        @Override
        public void toggle() {
            setChecked(!checked);
        }
    }

    private static final class AppEntry {
        final String label;
        final String packageName;
        final Drawable icon;

        AppEntry(String label, String packageName, Drawable icon) {
            this.label = label;
            this.packageName = packageName;
            this.icon = icon;
        }
    }
}
