package com.micewine.emu.fragments;

import static com.micewine.emu.adapters.AdapterGame.selectedGameName;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.micewine.emu.R;
import com.micewine.emu.core.ShellLoader;

import java.io.FileWriter;
import java.io.IOException;

public class LogViewerFragment extends Fragment implements ShellLoader.LogCallback {
    private TextView logTextView;
    private final StringBuilder logs = new StringBuilder();
    private ScrollView scrollView;
    private FloatingActionButton floatingActionButton;
    private boolean logViewerIsOpened = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_log_viewer, container, false);

        logTextView = rootView.findViewById(R.id.logsTextView);
        scrollView = rootView.findViewById(R.id.scrollView);
        floatingActionButton = rootView.findViewById(R.id.syncLogs);
        MaterialButton exportLogButton = rootView.findViewById(R.id.exportLogButton);

        ShellLoader.connectOutput(this);
        openSessionLog();

        scrollView.setOnScrollChangeListener((view, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            View content = scrollView.getChildAt(0);
            int diff = content.getBottom() - (scrollView.getHeight() + scrollY);
            if (diff > 0) {
                floatingActionButton.setVisibility(View.VISIBLE);
            } else {
                floatingActionButton.setVisibility(View.GONE);
            }
        });

        floatingActionButton.setOnClickListener((view) -> {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            floatingActionButton.setVisibility(View.GONE);
        });

        exportLogButton.setOnClickListener((v) -> {
            // app-scoped external dir: works without storage permissions on every version
            java.io.File dir = new java.io.File(requireContext().getExternalFilesDir(null), "logs");
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
            java.io.File out = new java.io.File(dir, "Cyclone-" + selectedGameName + "-Log-" + System.currentTimeMillis() / 1000 + ".txt");

            try (FileWriter writer = new FileWriter(out)) {
                writer.write(logs.toString());
            } catch (IOException ignored) {
            }

            exportLogButton.post(() -> Toast.makeText(getContext(), "Log exported to " + out.getName(), Toast.LENGTH_SHORT).show());
        });

        MaterialButton shareLogButton = rootView.findViewById(R.id.shareLogButton);
        shareLogButton.setOnClickListener((v) -> {
            try {
                java.io.File f = new java.io.File(requireContext().getCacheDir(), "cyclone-log.txt");
                try (FileWriter writer = new FileWriter(f)) {
                    writer.write(logs.toString());
                }
                android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                        requireContext(), requireContext().getPackageName() + ".fileprovider", f);
                android.content.Intent send = new android.content.Intent(android.content.Intent.ACTION_SEND)
                        .setType("text/plain")
                        .putExtra(android.content.Intent.EXTRA_STREAM, uri)
                        .addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(android.content.Intent.createChooser(send, "Share log"));
            } catch (Exception e) {
                Toast.makeText(getContext(), "Couldn't share log", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    private java.io.Writer sessionWriter;

    // Mirror the session to disk so a crash can still be reported afterwards.
    private void openSessionLog() {
        try {
            java.io.File logDir = new java.io.File(requireContext().getFilesDir(), "logs");
            //noinspection ResultOfMethodCallIgnored
            logDir.mkdirs();
            java.io.File last = new java.io.File(logDir, "last-session.txt");
            java.io.File prev = new java.io.File(logDir, "prev-session.txt");
            if (last.exists()) {
                //noinspection ResultOfMethodCallIgnored
                prev.delete();
                //noinspection ResultOfMethodCallIgnored
                last.renameTo(prev);
            }
            sessionWriter = new java.io.BufferedWriter(new FileWriter(last));
        } catch (IOException ignored) {
        }
    }

    @Override
    public void onDestroyView() {
        if (sessionWriter != null) {
            try {
                sessionWriter.close();
            } catch (IOException ignored) {
            }
            sessionWriter = null;
        }
        super.onDestroyView();
    }

    private String getLastLines(StringBuilder sb) {
        int count = 0;
        int i = sb.length() - 1;

        while (i >= 0 && count < 500) {
            if (sb.charAt(i) == '\n') {
                count++;
            }
            i--;
        }

        int start = Math.max(0, i + 2);

        return sb.substring(start);
    }

    public void populate() {
        logViewerIsOpened = true;
        logTextView.post(() -> logTextView.setText(getLastLines(logs)));
    }

    public void cleanup() {
        logViewerIsOpened = false;
        logTextView.post(() -> logTextView.setText(""));
    }

    @Override
    public void appendLogs(String text) {
        logs.append(text);

        if (sessionWriter != null) {
            try {
                sessionWriter.write(text);
                sessionWriter.flush();
            } catch (IOException ignored) {
            }
        }

        if (!logViewerIsOpened) return;

        requireActivity().runOnUiThread(() -> {
            logTextView.append(text);

            View content = scrollView.getChildAt(0);
            int diff = content.getBottom() - (scrollView.getHeight() + scrollView.getScrollY());
            if (diff == 0) {
                scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
            }
        });
    }
}