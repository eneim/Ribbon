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
import android.widget.Toast;

public class TabletLayout extends ViewGroup implements BottomTabLayout {
  private static final String TAG = TabletLayout.class.getSimpleName();
  private final int itemHeight;
  private final int paddingTop;
  private boolean hasFrame;
  private int selectedIndex;
  OnItemClickListener listener;
  private MenuParser.Menu menu;

  public TabletLayout(final Context context) {
    super(context);
    final Resources res = getResources();
    selectedIndex = 0;
    itemHeight = res.getDimensionPixelSize(R.dimen.ribbon_tablet_item_height);
    paddingTop = res.getDimensionPixelSize(R.dimen.ribbon_tablet_layout_padding_top);
  }

  @Override public void removeAll() {
    removeAllViews();
    selectedIndex = 0;
    menu = null;
  }

  @Override protected void onLayout(final boolean changed, final int l, final int t, final int r,
      final int b) {
    if (!hasFrame || getChildCount() == 0) {
      return;
    }
    int top = paddingTop;

    for (int i = 0; i < getChildCount(); i++) {
      final View child = getChildAt(i);
      final LayoutParams params = child.getLayoutParams();
      setChildFrame(child, 0, top, params.width, params.height);
      top += child.getHeight();
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

    final TabletActionTabView current = (TabletActionTabView) getChildAt(oldSelectedIndex);
    final TabletActionTabView child = (TabletActionTabView) getChildAt(itemIndex);

    current.setExpanded(false, 0, animate);
    child.setExpanded(true, 0, animate);
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

    for (int i = 0; i < menu.getItemCount(); i++) {
      final ActionTab item = menu.getItemAt(i);
      Log.d(TAG, "item: " + item);

      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getWidth(), itemHeight);

      TabletActionTabView view = new TabletActionTabView(parent, i == selectedIndex, menu);
      view.setItem(item);
      view.setLayoutParams(params);
      view.setClickable(true);
      view.setTypeface(parent.typeface);
      final int finalI = i;
      view.setOnClickListener(new OnClickListener() {
        @Override public void onClick(final View v) {
          if (null != listener) {
            listener.onItemClick(TabletLayout.this, v, finalI, true);
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
