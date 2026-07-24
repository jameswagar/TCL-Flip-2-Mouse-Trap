package com.dumbphone.mousetrap;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

    private final List<AppEntry> apps = new ArrayList<>();
    private ListView listView;
    private TextView status;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        buildUi();
        loadApps();
    }

    private void buildUi() {
        int pad = dp(6);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("Mouse Trap");
        title.setTextSize(22);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        status = new TextView(this);
        status.setTextSize(14);
        status.setGravity(Gravity.CENTER_HORIZONTAL);
        status.setPadding(0, 0, 0, dp(3));
        root.addView(status);

        listView = new ListView(this);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        root.addView(listView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        Button save = new Button(this);
        save.setText("Save mouse targets");
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveTargets();
            }
        });
        root.addView(save, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        setContentView(root);
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
            unique.put(pkg, new AppEntry(label, pkg));
        }
        apps.clear();
        apps.addAll(unique.values());
        Collections.sort(apps, new Comparator<AppEntry>() {
            @Override
            public int compare(AppEntry left, AppEntry right) {
                return left.label.compareToIgnoreCase(right.label);
            }
        });

        List<String> rows = new ArrayList<>();
        for (AppEntry app : apps) rows.add(app.label);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_multiple_choice, rows) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    TextView text = (TextView) view;
                    text.setTextSize(17);
                    text.setSingleLine(false);
                    text.setMaxLines(3);
                    text.setEllipsize(null);
                    text.setHorizontallyScrolling(false);
                    text.setMinHeight(dp(48));
                    text.setGravity(Gravity.CENTER_VERTICAL);
                }
                return view;
            }
        };
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateStatus(countCheckedTargets());
            }
        });

        Set<String> selected = TargetStore.read(this);
        for (int i = 0; i < apps.size(); i++) {
            listView.setItemChecked(i, selected.contains(apps.get(i).packageName));
        }
        listView.setSelection(0);
        updateStatus(selected.size());
    }

    private void saveTargets() {
        LinkedHashSet<String> selected = new LinkedHashSet<>();
        for (int i = 0; i < apps.size(); i++) {
            if (listView.isItemChecked(i)) selected.add(apps.get(i).packageName);
        }
        try {
            TargetStore.write(this, selected);
            updateStatus(selected.size());
            Toast.makeText(this, "Mouse targets saved", Toast.LENGTH_SHORT).show();
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
                + " • " + count + " custom target" + (count == 1 ? "" : "s"));
    }

    private int countCheckedTargets() {
        int count = 0;
        for (int i = 0; i < apps.size(); i++) {
            if (listView.isItemChecked(i)) count++;
        }
        return count;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static final class AppEntry {
        final String label;
        final String packageName;
        AppEntry(String label, String packageName) {
            this.label = label;
            this.packageName = packageName;
        }
    }

    private static final String LAUNCHER = "com.offlineinc.dumbdownlauncher";
}
