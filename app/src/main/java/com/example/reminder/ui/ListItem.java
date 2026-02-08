package com.example.reminder.ui;

public interface ListItem {
    int TYPE_HEADER = 0;
    int TYPE_REMINDER = 1;

    int getType();
}
