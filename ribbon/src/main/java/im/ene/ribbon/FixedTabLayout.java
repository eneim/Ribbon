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
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class FixedTabLayout extends ViewGroup implements BottomTabLayout {
  private static final String TAG = FixedTabLayout.class.getSimpleName();
  private final int maxActiveItemWidth;
  private final int minActiveItemWidth;
  private int totalChildrenSize;
  private boolean hasFrame;
  private int selectedIndex;
  OnItemClickListener listener;
  private int itemFinalWidth;
  private MenuParser.Menu menu;

  public FixedTabLayout(final Context context) {
    super(context);
    totalChildrenSize = 0;
    selectedIndex = 0;

    final Resources res = getResources();
    maxActiveItemWidth = res.getDimensionPixelSize(R.dimen.bbn_fixed_maxActiveItemWidth);
    minActiveItemWidth = res.getDimensionPixelSize(R.dimen.bbn_fixed_minActiveItemWidth);
  }

  @Override public void removeAll() {
    removeAllViews();
    totalChildrenSize = 0;
    itemFinalWidth = 0;
    selectedIndex = 0;
    menu = null;
  }

  @Override protected void onLayout(final boolean changed, final int l, final int t, final int r,
      final int b) {
    if (!hasFrame || getChildCount() == 0) {
      return;
    }

    if (totalChildrenSize == 0) {
      totalChildrenSize = itemFinalWidth * (getChildCount() - 1) + itemFinalWidth;
    }

    int width = (r - l);
    int left = (width - totalChildrenSize) / 2;

    Log.v(TAG, "width: " + width);
    Log.v(TAG, "left: " + left);

    for (int i = 0; i < getChildCount(); i++) {
      final View child = getChildAt(i);
      final LayoutParams params = child.getLayoutParams();
      setChildFrame(child, left, 0, params.width, params.height);
      left += child.getWidth();
    }
  }

  @Override protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    hasFrame = true;

    if (null != menu) {
      populateInternal(menu);
      menu = null;
    }
  }

  private void setChildFrame(View child, int left, int top, int width, int height) {
    Log.v(TAG, "setChildFrame: " + left + ", " + top + ", " + width + ", " + height);
    child.layout(left, top, left + width, top + height);
  }

  @Override public void setSelectedItem(final int itemIndex, final boolean animate) {
    Log.i(TAG, "setSelectedItem: " + itemIndex);

    if (selectedIndex == itemIndex) {
      return;
    }

    int oldSelectedIndex = this.selectedIndex;
    this.selectedIndex = itemIndex;

    if (!hasFrame || getChildCount() == 0) {
      return;
    }

    FixedActionTabView current = null;
    if (oldSelectedIndex >= 0) {
      current = (FixedActionTabView) getChildAt(oldSelectedIndex);
    }

    final FixedActionTabView next = (FixedActionTabView) getChildAt(itemIndex);

    if (current != null) {
      current.setExpanded(false, 0, animate);
    }

    next.setExpanded(true, 0, animate);
  }

  @Override public int getSelectedItem() {
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

  @Override public void setOnItemClickListener(OnItemClickListener listener) {
    this.listener = listener;
  }

  private void populateInternal(@NonNull final MenuParser.Menu menu) {
    Log.d(TAG, "populateInternal");

    final BottomNavigationView parent = (BottomNavigationView) getParent();
    final float density = getResources().getDisplayMetrics().density;
    final int screenWidth = parent.getWidth();

    Log.v(TAG, "density: " + density);
    Log.v(TAG, "screenWidth: " + screenWidth);
    Log.v(TAG, "screenWidth(dp): " + (screenWidth / density));

    int proposedWidth = Math.min(Math.max(screenWidth / menu.getItemCount(), minActiveItemWidth),
        maxActiveItemWidth);
    Log.v(TAG, "proposedWidth: " + proposedWidth);
    Log.v(TAG, "proposedWidth(dp): " + proposedWidth / density);

    if (proposedWidth * menu.getItemCount() > screenWidth) {
      proposedWidth = screenWidth / menu.getItemCount();
    }

    Log.v(TAG, "active size: " + maxActiveItemWidth + ", " + minActiveItemWidth);
    Log.v(TAG,
        "active size (dp): " + maxActiveItemWidth / density + ", " + minActiveItemWidth / density);

    this.itemFinalWidth = proposedWidth;

    for (int i = 0; i < menu.getItemCount(); i++) {
      final ActionTab item = menu.getItemAt(i);
      Log.d(TAG, "item: " + item);

      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(proposedWidth, getHeight());

      FixedActionTabView view = new FixedActionTabView(parent, i == selectedIndex, menu);
      view.setItem(item);
      view.setLayoutParams(params);
      view.setClickable(true);
      view.setTypeface(parent.typeface);
      final int index = i;
      view.setOnClickListener(new OnClickListener() {
        @Override public void onClick(final View v) {
          if (null != listener) {
            listener.onItemClick(FixedTabLayout.this, v, index, true);
          }
        }
      });

      view.setOnLongClickListener(new OnLongClickListener() {
        @Override public boolean onLongClick(final View v) {
          // TODO May be get use of long click
          // Toast.makeText(getContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
          return true;
        }
      });
      addView(view);
    }
  }
}
