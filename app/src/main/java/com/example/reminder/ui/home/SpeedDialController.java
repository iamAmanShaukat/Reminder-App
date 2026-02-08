package com.example.reminder.ui.home;

import android.view.View;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Controller for Speed Dial FAB functionality.
 * Extracted from HomeFragment to follow Single Responsibility Principle.
 * 
 * Handles:
 * - FAB expand/collapse animations
 * - Scrim visibility
 * - Mini FAB visibility states
 */
public class SpeedDialController {

    public interface OnActionListener {
        void onTemplatesClicked();

        void onCustomReminderClicked();
    }

    private final View scrim;
    private final View templatesLayout;
    private final View customLayout;
    private final FloatingActionButton mainFab;
    private final FloatingActionButton templatesFab;
    private final FloatingActionButton customFab;
    private final OnActionListener listener;

    private boolean isOpen = false;
    private static final int ANIMATION_DURATION = 200;
    private static final int ANIMATION_DELAY = 50;

    public SpeedDialController(
            View scrim,
            View templatesLayout,
            View customLayout,
            FloatingActionButton mainFab,
            FloatingActionButton templatesFab,
            FloatingActionButton customFab,
            OnActionListener listener) {
        this.scrim = scrim;
        this.templatesLayout = templatesLayout;
        this.customLayout = customLayout;
        this.mainFab = mainFab;
        this.templatesFab = templatesFab;
        this.customFab = customFab;
        this.listener = listener;

        setupListeners();
    }

    private void setupListeners() {
        mainFab.setOnClickListener(v -> toggle());
        scrim.setOnClickListener(v -> collapse());

        templatesFab.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTemplatesClicked();
            }
            collapse();
        });

        customFab.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCustomReminderClicked();
            }
            collapse();
        });
    }

    public void toggle() {
        if (isOpen) {
            collapse();
        } else {
            expand();
        }
    }

    public void expand() {
        if (isOpen)
            return;
        isOpen = true;

        // Show scrim
        scrim.setVisibility(View.VISIBLE);
        scrim.setAlpha(0f);
        scrim.animate().alpha(1f).setDuration(ANIMATION_DURATION).start();

        // Rotate main FAB
        mainFab.animate().rotation(45f).setDuration(ANIMATION_DURATION).start();

        // Show and animate mini FABs
        templatesLayout.setVisibility(View.VISIBLE);
        templatesLayout.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(ANIMATION_DURATION)
                .setStartDelay(ANIMATION_DELAY)
                .start();

        customLayout.setVisibility(View.VISIBLE);
        customLayout.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(ANIMATION_DURATION)
                .setStartDelay(ANIMATION_DELAY * 2)
                .start();
    }

    public void collapse() {
        if (!isOpen)
            return;
        isOpen = false;

        // Hide scrim
        scrim.animate().alpha(0f).setDuration(ANIMATION_DURATION)
                .withEndAction(() -> scrim.setVisibility(View.GONE))
                .start();

        // Rotate main FAB back
        mainFab.animate().rotation(0f).setDuration(ANIMATION_DURATION).start();

        // Hide mini FABs
        templatesLayout.animate()
                .alpha(0f)
                .translationY(20f)
                .setDuration(150)
                .withEndAction(() -> templatesLayout.setVisibility(View.GONE))
                .start();

        customLayout.animate()
                .alpha(0f)
                .translationY(20f)
                .setDuration(150)
                .withEndAction(() -> customLayout.setVisibility(View.GONE))
                .start();
    }

    public boolean isOpen() {
        return isOpen;
    }

    /**
     * Call this when the parent view is being destroyed to prevent memory leaks.
     */
    public void cleanup() {
        mainFab.setOnClickListener(null);
        scrim.setOnClickListener(null);
        templatesFab.setOnClickListener(null);
        customFab.setOnClickListener(null);
    }
}
