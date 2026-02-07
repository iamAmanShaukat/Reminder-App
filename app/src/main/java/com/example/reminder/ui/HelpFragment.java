package com.example.reminder.ui;

import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.reminder.R;
import com.example.reminder.widget.StickyNoteWidgetProvider;
import com.google.android.material.snackbar.Snackbar;

public class HelpFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_help, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btnAddWidgetHelp).setOnClickListener(v -> requestPinWidget());
    }

    private void requestPinWidget() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.appwidget.AppWidgetManager appWidgetManager = requireContext()
                    .getSystemService(android.appwidget.AppWidgetManager.class);
            ComponentName myProvider = new ComponentName(requireContext(), StickyNoteWidgetProvider.class);

            if (appWidgetManager.isRequestPinAppWidgetSupported()) {
                // Check if already added
                int[] existingIds = appWidgetManager.getAppWidgetIds(myProvider);
                if (existingIds != null && existingIds.length > 0) {
                    Snackbar.make(requireView(), "Widget is already added to your home screen", Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                appWidgetManager.requestPinAppWidget(myProvider, null, null);
            } else {
                Snackbar.make(requireView(), "Widget pinning not supported on this device/launcher",
                        Snackbar.LENGTH_LONG).show();
            }
        } else {
            Snackbar.make(requireView(), "Widget pinning requires Android 8.0+", Snackbar.LENGTH_LONG).show();
        }
    }
}
