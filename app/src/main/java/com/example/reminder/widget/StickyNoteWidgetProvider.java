package com.example.reminder.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.example.reminder.R;
import com.example.reminder.ui.MainActivity;

public class StickyNoteWidgetProvider extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        android.util.Log.d("StickyWidget", "onReceive: " + intent.getAction());
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        android.util.Log.d("StickyWidget",
                "onUpdate called for " + appWidgetIds.length + " widgets: " + java.util.Arrays.toString(appWidgetIds));
        for (int appWidgetId : appWidgetIds) {
            try {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            } catch (Exception e) {
                android.util.Log.e("StickyWidget", "Error updating widget " + appWidgetId, e);
            }
        }
    }

    @Override
    public void onEnabled(Context context) {
        android.util.Log.d("StickyWidget", "onEnabled");
        super.onEnabled(context);
        // Schedule midnight updates when first widget is added
        com.example.reminder.receiver.MidnightWidgetUpdateReceiver.scheduleMidnightUpdate(context);
    }

    @Override
    public void onDisabled(Context context) {
        android.util.Log.d("StickyWidget", "onDisabled");
        super.onDisabled(context);
        // Cancel midnight updates when last widget is removed
        com.example.reminder.receiver.MidnightWidgetUpdateReceiver.cancelMidnightUpdate(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        android.util.Log.d("StickyWidget", "onDeleted: " + java.util.Arrays.toString(appWidgetIds));
        super.onDeleted(context, appWidgetIds);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        android.util.Log.d("StickyWidget", "Updating widget " + appWidgetId);
        try {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_sticky_note);
            Intent intent = new Intent(context, WidgetRemoteViewsService.class);
            views.setRemoteAdapter(R.id.widget_list_view, intent);
            views.setEmptyView(R.id.widget_list_view, R.id.empty_view);

            Intent clickIntent = new Intent(context, MainActivity.class);
            PendingIntent clickPendingIntent = PendingIntent.getActivity(context, 0, clickIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setPendingIntentTemplate(R.id.widget_list_view, clickPendingIntent);

            // Add Button PendingIntent
            Intent addIntent = new Intent(context, MainActivity.class);
            addIntent.putExtra("NAVIGATE_TO_ADD", true);
            PendingIntent addPendingIntent = PendingIntent.getActivity(context, 1, addIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_add_button, addPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
            android.util.Log.d("StickyWidget", "Widget " + appWidgetId + " updated successfully");
        } catch (Exception e) {
            android.util.Log.e("StickyWidget", "Exception in updateAppWidget for " + appWidgetId, e);
        }
    }

    public static void sendRefreshBroadcast(Context context) {
        try {
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.setComponent(new android.content.ComponentName(context, StickyNoteWidgetProvider.class));
            context.sendBroadcast(intent);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager
                    .getAppWidgetIds(new android.content.ComponentName(context, StickyNoteWidgetProvider.class));

            android.util.Log.d("StickyWidget", "sendRefreshBroadcast found " + appWidgetIds.length + " widgets: "
                    + java.util.Arrays.toString(appWidgetIds));

            if (appWidgetIds.length > 0) {
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view);
                android.util.Log.d("StickyWidget", "notifyAppWidgetViewDataChanged triggered");
            } else {
                android.util.Log.w("StickyWidget",
                        "No widgets found to refresh! Is the widget added to the home screen?");
            }
        } catch (Exception e) {
            android.util.Log.e("StickyWidget", "Error in sendRefreshBroadcast", e);
        }
    }
}
