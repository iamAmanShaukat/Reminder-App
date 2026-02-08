package com.example.reminder.service;

import android.content.Context;
import com.example.reminder.widget.StickyNoteWidgetProvider;
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Centralized service for triggering widget refreshes.
 * Abstracts the widget refresh mechanism from business logic.
 */
@Singleton
public class WidgetRefreshService {

    private final Context context;

    @Inject
    public WidgetRefreshService(@ApplicationContext Context context) {
        this.context = context;
    }

    /**
     * Refresh all reminder widgets.
     */
    public void refresh() {
        StickyNoteWidgetProvider.sendRefreshBroadcast(context);
    }
}
