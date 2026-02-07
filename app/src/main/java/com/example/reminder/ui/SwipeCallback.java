package com.example.reminder.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.example.reminder.R;

public abstract class SwipeCallback extends ItemTouchHelper.SimpleCallback {

    private final ColorDrawable background;
    private final int deleteColor;
    private final int completeColor;
    private final Drawable deleteIcon;
    private final Drawable completeIcon;

    public SwipeCallback(Context context) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        background = new ColorDrawable();
        deleteColor = context.getColor(R.color.color_delete);
        completeColor = context.getColor(R.color.color_check);
        deleteIcon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_delete);
        completeIcon = ContextCompat.getDrawable(context, R.drawable.ic_tick);

        // Tint icons if needed, or rely on their default/tinted state
        if (deleteIcon != null) {
            deleteIcon.setTint(Color.WHITE);
        }
        if (completeIcon != null) {
            completeIcon.setTint(Color.WHITE);
        }
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
            @NonNull RecyclerView.ViewHolder target) {
        return false; // We don't support drag & drop
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState,
            boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 20; // Slight offset for rounded corners if needed

        if (dX > 0) { // Swiping to the Right (Complete)
            background.setColor(completeColor);
            background.setBounds(itemView.getLeft(), itemView.getTop(),
                    itemView.getLeft() + ((int) dX) + backgroundCornerOffset, itemView.getBottom());

            if (completeIcon != null) {
                int iconMargin = (itemView.getHeight() - completeIcon.getIntrinsicHeight()) / 2;
                int iconTop = itemView.getTop() + (itemView.getHeight() - completeIcon.getIntrinsicHeight()) / 2;
                int iconBottom = iconTop + completeIcon.getIntrinsicHeight();
                int iconLeft = itemView.getLeft() + iconMargin;
                int iconRight = itemView.getLeft() + iconMargin + completeIcon.getIntrinsicWidth();

                completeIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                background.draw(c);
                completeIcon.draw(c);
            } else {
                background.draw(c);
            }

        } else if (dX < 0) { // Swiping to the Left (Delete)
            background.setColor(deleteColor);
            background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                    itemView.getTop(), itemView.getRight(), itemView.getBottom());

            if (deleteIcon != null) {
                int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                int iconTop = itemView.getTop() + (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();
                int iconRight = itemView.getRight() - iconMargin;
                int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();

                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                background.draw(c);
                deleteIcon.draw(c);
            } else {
                background.draw(c);
            }

        } else { // View is unswiped
            background.setBounds(0, 0, 0, 0);
        }
    }
}
