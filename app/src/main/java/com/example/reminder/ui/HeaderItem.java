package com.example.reminder.ui;

public class HeaderItem implements ListItem {
    private final String title;

    public HeaderItem(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public int getType() {
        return TYPE_HEADER;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        HeaderItem that = (HeaderItem) o;
        return title.equals(that.title);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(title);
    }
}
