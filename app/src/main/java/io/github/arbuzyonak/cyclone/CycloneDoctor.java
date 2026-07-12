package io.github.arbuzyonak.cyclone;

import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_BOX64;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_CORE;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_VULKAN_DRIVER;
import static com.micewine.emu.activities.MainActivity.deviceArch;
import static com.micewine.emu.activities.MainActivity.homeDir;
import static com.micewine.emu.activities.MainActivity.ratPackagesDir;
import static com.micewine.emu.activities.MainActivity.winePrefix;
import static com.micewine.emu.activities.MainActivity.winePrefixesDir;
import static com.micewine.emu.adapters.AdapterRatPackage.BOX64;
import static com.micewine.emu.adapters.AdapterRatPackage.CORE;
import static com.micewine.emu.adapters.AdapterRatPackage.DXVK;
import static com.micewine.emu.adapters.AdapterRatPackage.VKD3D;
import static com.micewine.emu.adapters.AdapterRatPackage.VK_DRIVER;
import static com.micewine.emu.adapters.AdapterRatPackage.WINE;
import static com.micewine.emu.adapters.AdapterRatPackage.WINED3D;
import static com.micewine.emu.core.RatPackageManager.haveAnyPackageByCategory;
import static com.micewine.emu.fragments.WinePrefixManagerFragment.getWinePrefixes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.widget.Toast;

import com.micewine.emu.R;
import com.micewine.emu.activities.MainActivity;
import com.micewine.emu.core.ShellLoader;
import com.micewine.emu.fragments.ShortcutsFragment;

import java.io.File;
import java.util.Locale;

/** Health report + repair actions for the embedded runtime. */
public class CycloneDoctor {

    public static void show(Activity a) {
        AlertDialog d = new AlertDialog.Builder(a, R.style.Theme_Cyclone_Dialog)
                .setTitle("Diagnostics")
                .setMessage("Checking…")
                .setPositiveButton("Close", null)
                .setNeutralButton("Share log", (dd, w) -> shareLastLog(a))
                .setNegativeButton("Fix…", (dd, w) -> showFixMenu(a))
                .show();
        new Thread(() -> {
            final String report = buildReport(a);
            a.runOnUiThread(() -> {
                if (d.isShowing()) d.setMessage(report);
            });
        }).start();
    }

    private static String buildReport(Activity a) {
        StringBuilder sb = new StringBuilder();
        sb.append("Runtime\n");
        sb.append(line("Core", haveAnyPackageByCategory(CORE)));
        sb.append(line("Wine", haveAnyPackageByCategory(WINE)));
        sb.append(line("Graphics driver", haveAnyPackageByCategory(VK_DRIVER)));
        sb.append(line("DirectX layers", haveAnyPackageByCategory(DXVK)
                && haveAnyPackageByCategory(VKD3D) && haveAnyPackageByCategory(WINED3D)));
        if (!deviceArch.equals("x86_64")) {
            sb.append(line("Box64", haveAnyPackageByCategory(BOX64)));
        }
        boolean prefixOk = !getWinePrefixes().isEmpty();
        sb.append(line("Game prefix", prefixOk));

        String core = MainActivity.selectedCore;
        if (core != null && !core.isEmpty()) {
            sb.append(line("Vulkan loader", new File(ratPackagesDir, core + "/files/usr/lib/libvulkan.so.1").exists()));
        }

        String prefix = winePrefix != null ? winePrefix : (prefixOk ? getWinePrefixes().get(0) : "default");
        File exe = new File(winePrefixesDir, prefix + "/drive_c/Vortex/Vortex.exe");
        if (exe.exists()) {
            long v = MainActivity.readExeVersion(exe);
            sb.append("• Game client: ").append(v > 0
                    ? "v" + (v / 1_000_000) + "." + (v / 1000 % 1000) + "." + (v % 1000)
                    : "installed").append("\n");
        } else {
            sb.append("• Game client: not installed\n");
        }

        boolean net = false;
        try {
            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
            okhttp3.Request req = new okhttp3.Request.Builder().url("https://playvortex.io").head().build();
            try (okhttp3.Response resp = client.newCall(req).execute()) {
                net = resp.code() < 500;
            }
        } catch (Exception ignored) {
        }
        sb.append(line("playvortex.io", net));

        sb.append("\nStorage\n");
        sb.append("• Runtime: ").append(fmtSize(dirSize(ratPackagesDir))).append("\n");
        sb.append("• Game & prefix: ").append(fmtSize(dirSize(winePrefixesDir))).append("\n");
        long caches = dirSize(new File(homeDir, ".cache")) + dirSize(a.getCacheDir());
        sb.append("• Caches: ").append(fmtSize(caches)).append("\n");
        sb.append("• Logs: ").append(fmtSize(dirSize(new File(a.getFilesDir(), "logs")))).append("\n");

        sb.append("\nCyclone ").append(com.micewine.emu.BuildConfig.VERSION_NAME)
                .append(" (").append(com.micewine.emu.BuildConfig.GIT_SHORT_SHA).append(")");
        return sb.toString();
    }

    private static String line(String label, boolean ok) {
        return "• " + label + ": " + (ok ? "OK" : "MISSING") + "\n";
    }

    // manual walk, skipping symlinks so the usr->Core link doesn't double-count
    private static long dirSize(File dir) {
        if (dir == null || !dir.exists()) return 0;
        try {
            if (java.nio.file.Files.isSymbolicLink(dir.toPath())) return 0;
        } catch (Exception e) {
            return 0;
        }
        if (dir.isFile()) return dir.length();
        long total = 0;
        File[] files = dir.listFiles();
        if (files == null) return 0;
        for (File f : files) total += dirSize(f);
        return total;
    }

    private static String fmtSize(long bytes) {
        if (bytes >= 1L << 30) return String.format(Locale.US, "%.1f GB", bytes / (double) (1L << 30));
        if (bytes >= 1L << 20) return String.format(Locale.US, "%.0f MB", bytes / (double) (1L << 20));
        return String.format(Locale.US, "%.0f KB", Math.max(1, bytes) / (double) (1L << 10));
    }

    private static void shareLastLog(Activity a) {
        File log = new File(a.getFilesDir(), "logs/last-session.txt");
        if (!log.exists() || log.length() == 0) {
            log = new File(a.getFilesDir(), "logs/prev-session.txt");
        }
        if (!log.exists() || log.length() == 0) {
            Toast.makeText(a, "No session log yet — start the game once first", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                    a, a.getPackageName() + ".fileprovider", log);
            Intent send = new Intent(Intent.ACTION_SEND)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_STREAM, uri)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            a.startActivity(Intent.createChooser(send, "Share log"));
        } catch (Exception e) {
            Toast.makeText(a, "Couldn't share log", Toast.LENGTH_SHORT).show();
        }
    }

    private static void showFixMenu(Activity a) {
        String[] items = {"Clear caches", "Reinstall runtime"};
        new AlertDialog.Builder(a, R.style.Theme_Cyclone_Dialog)
                .setTitle("Fix")
                .setItems(items, (d, which) -> {
                    if (which == 0) confirmClearCaches(a);
                    else confirmReinstallRuntime(a);
                })
                .show();
    }

    private static void confirmClearCaches(Activity a) {
        new AlertDialog.Builder(a, R.style.Theme_Cyclone_Dialog)
                .setTitle("Clear caches")
                .setMessage("Frees space used by shader caches and logs. The game may stutter "
                        + "for a bit while the shader cache rebuilds.")
                .setPositiveButton("Clear", (d, w) -> new Thread(() -> {
                    long freed = dirSize(new File(homeDir, ".cache"))
                            + dirSize(a.getCacheDir())
                            + dirSize(new File(a.getFilesDir(), "logs"));
                    ShellLoader.runCommand("rm -rf " + homeDir + "/.cache " + a.getCacheDir()
                            + "/* " + new File(a.getFilesDir(), "logs"), false);
                    final String msg = "Freed " + fmtSize(freed);
                    a.runOnUiThread(() -> Toast.makeText(a, msg, Toast.LENGTH_LONG).show());
                }).start())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static void confirmReinstallRuntime(Activity a) {
        new AlertDialog.Builder(a, R.style.Theme_Cyclone_Dialog)
                .setTitle("Reinstall runtime")
                .setMessage("Removes the downloaded runtime and the game installation. "
                        + "Everything is set up again automatically the next time you tap Play "
                        + "(about a 400 MB download). Your account is not affected.")
                .setPositiveButton("Reinstall", (d, w) -> new Thread(() -> {
                    ShellLoader.runCommand("rm -rf " + ratPackagesDir + " " + winePrefixesDir, false);
                    if (MainActivity.preferences != null) {
                        MainActivity.preferences.edit()
                                .remove(SELECTED_CORE)
                                .remove(SELECTED_BOX64)
                                .remove(SELECTED_VULKAN_DRIVER)
                                .apply();
                    }
                    ShortcutsFragment.gameList.clear();
                    ShortcutsFragment.saveShortcuts();
                    a.runOnUiThread(() -> Toast.makeText(a,
                            "Runtime removed — it reinstalls on the next Play", Toast.LENGTH_LONG).show());
                }).start())
                .setNegativeButton("Cancel", null)
                .show();
    }
}
