/*
 * Copyright 2016 Nam Nguyen, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Alessandro Crugnola
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package im.ene.ribbon;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.support.annotation.MenuRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Xml;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

class MenuParser {

  private static final String TAG = MenuParser.class.getSimpleName();

  static class Menu {
    private final Context context;
    private ActionTab[] items;
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

    @Override public String toString() {
      return String.format(
          "Menu{background:%x, colorActive:%x, colorInactive:%x, shifting:%b, tablet:%b}",
          background, colorActive, colorInactive, shifting, tablet);
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
      if (colorActive == 0) {
        if (shifting && !tablet) {
          colorActive = MiscUtils.getColor(context, android.R.attr.colorForegroundInverse);
        } else {
          colorActive = MiscUtils.getColor(context, R.attr.colorPrimary);
        }
      }
      return colorActive;
    }

    public int getColorInactive() {
      if (colorInactive == 0) {
        if (shifting && !tablet) {
          int color = getColorActive();
          colorInactive =
              Color.argb(Color.alpha(color) * 3 / 4, Color.red(color), Color.green(color),
                  Color.blue(color));
        } else {
          int color = getColorActive();
          colorInactive =
              Color.argb(Color.alpha(color) * 3 / 4, Color.red(color), Color.green(color),
                  Color.blue(color));
        }
      }

      return colorInactive;
    }

    public int getRippleColor() {
      if (rippleColor == 0) {
        if (shifting && !tablet) {
          rippleColor = ContextCompat.getColor(context, R.color.ribbon_shifting_item_ripple_color);
        } else {
          rippleColor = ContextCompat.getColor(context, R.color.ribbon_fixed_item_ripple_color);
        }
      }
      return rippleColor;
    }

    public void setItems(final ActionTab[] items) {
      this.items = items;
      this.shifting = null != items && items.length > 360 / 80;
    }

    boolean isShifting() {
      return shifting;
    }

    ActionTab[] getItems() {
      return items;
    }

    ActionTab getItemAt(final int index) {
      return items[index];
    }

    int getItemCount() {
      if (items != null) {
        return items.length;
      }

      return 0;
    }

    /**
     * Returns true if the first item of the menu
     * has a color defined
     */
    @SuppressWarnings("unused") public boolean hasChangingColor() {
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

  private MenuParser() {
  }

  private void readMenu(final Context context, final AttributeSet attrs) {
    menu = new Menu(context);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BottomNavigationMenu);

    menu.itemAnimationDuration =
        a.getInt(R.styleable.BottomNavigationMenu_bbn_itemAnimationDuration,
            context.getResources().getInteger(R.integer.ribbon_item_animation_duration));
    menu.background = a.getColor(R.styleable.BottomNavigationMenu_android_background, 0);
    menu.rippleColor = a.getColor(R.styleable.BottomNavigationMenu_bbn_rippleColor, 0);
    menu.colorInactive = a.getColor(R.styleable.BottomNavigationMenu_bbn_itemColorInactive, 0);
    menu.colorActive = a.getColor(R.styleable.BottomNavigationMenu_bbn_itemColorActive, 0);
    menu.badgeColor = a.getColor(R.styleable.BottomNavigationMenu_bbn_badgeColor, Color.RED);

    a.recycle();
  }

  @SuppressWarnings("checkstyle:cyclomaticcomplexity")
  protected static Menu inflateMenu(final Context context, @MenuRes int menuRes) {
    List<ActionTab> tabs = new ArrayList<>();

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
                MenuItem item = menuParser.pullItem();
                ActionTab tab = new ActionTab(item.getItemId(), item.getItemIconResId(),
                    String.valueOf(item.getItemTitle()));
                tab.setEnabled(item.isItemEnabled());
                tab.setColor(item.getItemColor());
                tabs.add(tab);
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
      menu.setItems(tabs.toArray(new ActionTab[tabs.size()]));
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
