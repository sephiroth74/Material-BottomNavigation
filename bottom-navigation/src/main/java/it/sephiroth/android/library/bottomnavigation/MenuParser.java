package it.sephiroth.android.library.bottomnavigation;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;

import it.sephiroth.android.library.bottonnavigation.R;

/**
 * Created by alessandro crugnola on 4/3/16 at 7:59 PM.
 * Project: MaterialBottomNavigation
 */
class MenuParser {
    private MenuItem item;

    public MenuParser() { }

    protected static BottomNavigationItem[] inflateMenu(final Context context, int menuRes) {
        List<BottomNavigationItem> list = new ArrayList<>();

        try {
            final XmlResourceParser parser = context.getResources().getLayout(menuRes);
            AttributeSet attrs = Xml.asAttributeSet(parser);

            String tagName;
            int eventType = parser.getEventType();
            boolean lookingForEndOfUnknownTag = false;
            String unknownTagName = null;

            do {
                if (eventType == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
                    if (tagName.equals("menu")) {
                        eventType = parser.next();
                        break;
                    }
                    throw new RuntimeException("Expecting menu, got " + tagName);
                }
                eventType = parser.next();
            } while (eventType != XmlPullParser.END_DOCUMENT);

            boolean reachedEndOfMenu = false;
            MenuParser menuParser = new MenuParser();

            while (!reachedEndOfMenu) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (lookingForEndOfUnknownTag) {
                            break;
                        }
                        tagName = parser.getName();
                        if (tagName.equals("item")) {
                            menuParser.readItem(context, attrs);
                        } else {
                            lookingForEndOfUnknownTag = true;
                            unknownTagName = tagName;
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        tagName = parser.getName();
                        if (lookingForEndOfUnknownTag && tagName.equals(unknownTagName)) {
                            lookingForEndOfUnknownTag = false;
                            unknownTagName = null;
                        } else if (tagName.equals("item")) {
                            if (menuParser.hasItem()) {
                                MenuParser.MenuItem item = menuParser.pull();
                                BottomNavigationItem tab = new BottomNavigationItem(item.getItemId(), item.getItemIconResId(),
                                    String.valueOf(item.getItemTitle())
                                );
                                tab.setEnabled(item.isItemEnabled());
                                tab.setColor(item.getItemColor());
                                list.add(tab);
                            }
                        } else if (tagName.equals("menu")) {
                            reachedEndOfMenu = true;
                        }
                        break;

                    case XmlPullParser.END_DOCUMENT:
                        throw new RuntimeException("Unexpected end of document");
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            return null;
        }

        if (list.size() > 0) {
            return list.toArray(new BottomNavigationItem[list.size()]);
        } else {
            return new BottomNavigationItem[0];
        }
    }

    static class MenuItem {
        private int itemId;
        private CharSequence itemTitle;
        private int itemIconResId;
        private boolean itemEnabled;
        private int itemColor;

        public int getItemId() {
            return itemId;
        }

        public CharSequence getItemTitle() {
            return itemTitle;
        }

        public int getItemIconResId() {
            return itemIconResId;
        }

        public boolean isItemEnabled() {
            return itemEnabled;
        }

        public int getItemColor() {
            return itemColor;
        }
    }

    public MenuItem pull() {
        MenuItem current = item;
        item = null;
        return current;
    }

    public boolean hasItem() {
        return null != item;
    }

    /**
     * Called when the parser is pointing to an item tag.
     */
    public void readItem(Context mContext, AttributeSet attrs) {
        TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.BottomNavigationMenuItem);
        item = new MenuItem();
        item.itemId = a.getResourceId(R.styleable.BottomNavigationMenuItem_android_id, 0);
        item.itemTitle = a.getText(R.styleable.BottomNavigationMenuItem_android_title);
        item.itemIconResId = a.getResourceId(R.styleable.BottomNavigationMenuItem_android_icon, 0);
        item.itemEnabled = a.getBoolean(R.styleable.BottomNavigationMenuItem_android_enabled, true);
        item.itemColor = a.getColor(R.styleable.BottomNavigationMenuItem_android_color, 0);
        a.recycle();
    }
}
