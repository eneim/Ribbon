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
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import proguard.annotation.Keep;

public class ShiftingTabLayout extends ViewGroup implements BottomTabLayout {
  private static final String TAG = ShiftingTabLayout.class.getSimpleName();
  public static final double ROUND_DECIMALS = 10d;
  public static final float RATIO_MIN_INCREASE = 0.05f;
  private final int maxActiveItemWidth;
  private final int minActiveItemWidth;
  private final int maxInactiveItemWidth;
  private final int minInactiveItemWidth;
  private int totalChildrenSize;
  private int minSize, maxSize;
  private int selectedIndex;
  private boolean hasFrame;
  OnItemClickListener listener;
  private MenuParser.Menu menu;

  public ShiftingTabLayout(final Context context) {
    super(context);
    totalChildrenSize = 0;
    maxActiveItemWidth =
        getResources().getDimensionPixelSize(R.dimen.ribbon_shifting_active_item_max_width);
    minActiveItemWidth =
        getResources().getDimensionPixelSize(R.dimen.ribbon_shifting_active_item_min_width);
    maxInactiveItemWidth =
        getResources().getDimensionPixelSize(R.dimen.ribbon_shifting_inactive_item_max_width);
    minInactiveItemWidth =
        getResources().getDimensionPixelSize(R.dimen.ribbon_shifting_inactive_item_min_width);
  }

  @Override public void removeAll() {
    removeAllViews();
    totalChildrenSize = 0;
    selectedIndex = 0;
    menu = null;
  }

  @Override protected void onLayout(final boolean changed, final int l, final int t, final int r,
      final int b) {
    if (!hasFrame || getChildCount() == 0) {
      return;
    }

    if (totalChildrenSize == 0) {
      totalChildrenSize = minSize * (getChildCount() - 1) + maxSize;
    }

    int width = (r - l);
    int left = (width - totalChildrenSize) / 2;

    for (int i = 0; i < getChildCount(); i++) {
      final View child = getChildAt(i);
      final LayoutParams params = child.getLayoutParams();
      setChildFrame(child, left, 0, params.width, params.height);
      left += child.getWidth();
    }
  }

  @Override protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
    Log.i(TAG, "onSizeChanged(" + w + ", " + h + ")");
    super.onSizeChanged(w, h, oldw, oldh);
    hasFrame = true;

    if (null != menu) {
      populateInternal(menu);
      menu = null;
    }
  }

  @Override public void setOnItemClickListener(OnItemClickListener listener) {
    this.listener = listener;
  }

  private void setChildFrame(View child, int left, int top, int width, int height) {
    // Log.v(TAG, "setChildFrame: " + left + ", " + top + ", " + width + ", " + height);
    child.layout(left, top, left + width, top + height);
  }

  public void setTotalSize(final int minSize, final int maxSize) {
    this.minSize = minSize;
    this.maxSize = maxSize;
  }

  @Override public void setSelectedItem(final int itemIndex, final boolean animate) {
    Log.i(TAG, "setSelectedItem: " + itemIndex);

    if (selectedIndex == itemIndex) {
      return;
    }

    int oldSelectedIndex = this.selectedIndex;
    this.selectedIndex = itemIndex;

    MiscUtils.log(TAG, Log.DEBUG, "change selection: %d --> %d", oldSelectedIndex, selectedIndex);

    if (!hasFrame || getChildCount() == 0) {
      return;
    }

    final ActionTabView current = (ActionTabView) getChildAt(oldSelectedIndex);
    final ActionTabView child = (ActionTabView) getChildAt(itemIndex);

    current.setExpanded(false, minSize, animate);
    child.setExpanded(true, maxSize, animate);
  }

  @Override @Keep @SuppressWarnings("unused") public int getSelectedItem() {
    return selectedIndex;
  }

  @Override public void populate(@NonNull final MenuParser.Menu menu) {
    Log.i(TAG, "populate: " + menu);

    if (hasFrame) {
      populateInternal(menu);
    } else {
      this.menu = menu;
    }
  }

  private void populateInternal(@NonNull final MenuParser.Menu menu) {
    Log.d(TAG, "populateInternal");

    final BottomNavigationView parent = (BottomNavigationView) getParent();
    final float density = getResources().getDisplayMetrics().density;
    final int screenWidth = parent.getWidth();

    Log.v(TAG, "density: " + density);
    Log.v(TAG, "screenWidth(dp): " + (screenWidth / density));

    int itemWidthMin;
    int itemWidthMax;

    final int totalWidth = maxInactiveItemWidth * (menu.getActionCount() - 1) + maxActiveItemWidth;
    Log.v(TAG, "totalWidth(dp): " + totalWidth / density);

    if (totalWidth > screenWidth) {
      float ratio = (float) screenWidth / totalWidth;
      ratio = (float) ((double) Math.round(ratio * ROUND_DECIMALS) / ROUND_DECIMALS)
          + RATIO_MIN_INCREASE;
      Log.v(TAG, "ratio: " + ratio);

      itemWidthMin = (int) Math.max(maxInactiveItemWidth * ratio, minInactiveItemWidth);
      itemWidthMax = (int) (maxActiveItemWidth * ratio);

      Log.d(TAG, "computing sizes...");
      Log.v(TAG, "itemWidthMin(dp): " + itemWidthMin / density);
      Log.v(TAG, "itemWidthMax(dp): " + itemWidthMax / density);
      Log.v(TAG, "total items size(dp): "
          + (itemWidthMin * (menu.getActionCount() - 1) + itemWidthMax) / density);

      if (itemWidthMin * (menu.getActionCount() - 1) + itemWidthMax > screenWidth) {
        itemWidthMax =
            screenWidth - (itemWidthMin * (menu.getActionCount() - 1)); // minActiveItemWidth?
        if (itemWidthMax == itemWidthMin) {
          itemWidthMin = minInactiveItemWidth;
          itemWidthMax = screenWidth - (itemWidthMin * (menu.getActionCount() - 1));
        }
      }
    } else {
      itemWidthMax = maxActiveItemWidth;
      itemWidthMin = maxInactiveItemWidth;
    }

    Log.v(TAG,
        "active size (dp): " + maxActiveItemWidth / density + ", " + minActiveItemWidth / density);
    Log.v(TAG, "inactive size (dp): "
        + maxInactiveItemWidth / density
        + ", "
        + minInactiveItemWidth / density);

    Log.v(TAG, "itemWidth(dp): " + (itemWidthMin / density) + ", " + (itemWidthMax / density));

    setTotalSize(itemWidthMin, itemWidthMax);

    for (int i = 0; i < menu.getActionCount(); i++) {
      final ActionTab item = menu.getActionItemAt(i);
      Log.d(TAG, "item: " + item);

      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(itemWidthMin, getHeight());

      if (i == selectedIndex) {
        params.width = itemWidthMax;
      }

      ActionTabView view = new ShiftingActionTabView(parent, i == selectedIndex, menu);
      view.setAction(item);
      view.setLayoutParams(params);
      view.setClickable(true);
      view.setTypeface(parent.typeface);
      final int finalI = i;
      view.setOnClickListener(new OnClickListener() {
        @Override public void onClick(final View v) {
          if (null != listener) {
            listener.onItemClick(ShiftingTabLayout.this, v, finalI, true);
          }
        }
      });
      view.setOnLongClickListener(new OnLongClickListener() {
        @Override public boolean onLongClick(final View v) {
          Toast.makeText(getContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
          return true;
        }
      });
      addView(view);
    }
  }
}
