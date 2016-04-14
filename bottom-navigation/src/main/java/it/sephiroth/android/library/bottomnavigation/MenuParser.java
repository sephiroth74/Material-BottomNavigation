package it.sephiroth.android.library.bottomnavigation;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
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

    private static final String TAG = MenuParser.class.getSimpleName();

    static class Menu {
        private final Context context;
        private BottomNavigationItem[] items;
        private int colorActive;
        private int background;
        private int rippleColor;
        private int colorInactive;
        private int itemAnimationDuration;
        private boolean shifting;
        private boolean tablet;
        private int badgeColor;

        public Menu(final Context context) {
            this.context = context;
        }

        public int getItemAnimationDuration() {
            return itemAnimationDuration;
        }

        @Override
        public String toString() {
            return String.format("Menu{background:%x, colorActive:%x, colorInactive:%x, shifting:%b, tablet:%b}",
                background, colorActive, colorInactive, shifting, tablet
            );
        }

        public int getBadgeColor() {
            return badgeColor;
        }

        public int getBackground() {
            if (0 == background) {
                if (shifting && !tablet) {
                    return MiscUtils.getColor(context, R.attr.colorPrimary);
                } else {
                    return MiscUtils.getColor(context, android.R.attr.windowBackground);
                }
            }
            return background;
        }

        public int getColorActive() {
            if (0 == colorActive) {
                if (shifting && !tablet) {
                    colorActive = MiscUtils.getColor(context, android.R.attr.colorForegroundInverse);
                } else {
                    colorActive = MiscUtils.getColor(context, android.R.attr.colorForeground);
                }
            }
            return colorActive;
        }

        public int getColorInactive() {
            if (0 == colorInactive) {
                if (shifting && !tablet) {
                    int color = getColorActive();
                    colorInactive = Color.argb(Color.alpha(color) / 2, Color.red(color), Color.green(color), Color.blue(color));
                } else {
                    int color = getColorActive();
                    colorInactive = Color.argb(Color.alpha(color) / 2, Color.red(color), Color.green(color), Color.blue(color));
                }
            }
            return colorInactive;
        }

        public int getRippleColor() {
            if (0 == rippleColor) {
                if (shifting && !tablet) {
                    rippleColor = ContextCompat.getColor(context, R.color.bbn_shifting_item_ripple_color);
                } else {
                    rippleColor = ContextCompat.getColor(context, R.color.bbn_fixed_item_ripple_color);
                }
            }
            return rippleColor;
        }

        public void setItems(final BottomNavigationItem[] items) {
            this.items = items;
            this.shifting = null != items && items.length > 3;
        }

        public boolean isShifting() {
            return shifting;
        }

        public BottomNavigationItem[] getItems() {
            return items;
        }

        public BottomNavigationItem getItemAt(final int index) {
            return items[index];
        }

        public int getItemsCount() {
            if (null != items) {
                return items.length;
            }
            return 0;
        }

        /**
         * Returns true if the first item of the menu
         * has a color defined
         */
        @SuppressWarnings ("unused")
        public boolean hasChangingColor() {
            return items[0].hasColor();
        }

        void setTabletMode(final boolean tablet) {
            this.tablet = tablet;
        }

        public boolean isTablet() {
            return tablet;
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

    private MenuItem item;
    private Menu menu;

    public MenuParser() { }

    private void readMenu(final Context context, final AttributeSet attrs) {
        menu = new Menu(context);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BottomNavigationMenu);

        menu.itemAnimationDuration = a.getInt(
            R.styleable.BottomNavigationMenu_bbn_itemAnimationDuration,
            context.getResources().getInteger(R.integer.bbn_item_animation_duration)
        );
        menu.background = a.getColor(R.styleable.BottomNavigationMenu_android_background, 0);
        menu.rippleColor = a.getColor(R.styleable.BottomNavigationMenu_bbn_rippleColor, 0);
        menu.colorInactive = a.getColor(R.styleable.BottomNavigationMenu_bbn_itemColorInactive, 0);
        menu.colorActive = a.getColor(R.styleable.BottomNavigationMenu_bbn_itemColorActive, 0);
        menu.badgeColor = a.getColor(R.styleable.BottomNavigationMenu_bbn_badgeColor, Color.RED);

        a.recycle();
    }

    @SuppressWarnings ("checkstyle:cyclomaticcomplexity")
    protected static Menu inflateMenu(final Context context, int menuRes) {
        List<BottomNavigationItem> list = new ArrayList<>();

        MenuParser menuParser = new MenuParser();

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
                        menuParser.readMenu(context, attrs);
                        eventType = parser.next();
                        break;
                    }
                    throw new RuntimeException("Expecting menu, got " + tagName);
                }
                eventType = parser.next();
            } while (eventType != XmlPullParser.END_DOCUMENT);

            boolean reachedEndOfMenu = false;

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
                                MenuParser.MenuItem item = menuParser.pullItem();
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

                    default:
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            return null;
        }

        if (menuParser.hasMenu()) {
            Menu menu = menuParser.pullMenu();
            menu.setItems(list.toArray(new BottomNavigationItem[list.size()]));
            return menu;
        }

        return null;
    }

    public MenuItem pullItem() {
        MenuItem current = item;
        item = null;
        return current;
    }

    public boolean hasItem() {
        return null != item;
    }

    public boolean hasMenu() {
        return null != menu;
    }

    private Menu pullMenu() {
        Menu current = menu;
        menu = null;
        return current;
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
