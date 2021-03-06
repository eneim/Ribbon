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
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.view.View;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

abstract class VerticalScrollingBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

  private int mTotalDyUnconsumed = 0;
  private int mTotalDy = 0;
  @ScrollDirection private int mOverScrollDirection = ScrollDirection.SCROLL_NONE;
  @ScrollDirection private int mScrollDirection = ScrollDirection.SCROLL_NONE;

  public VerticalScrollingBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public VerticalScrollingBehavior() {
    super();
  }

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({ ScrollDirection.SCROLL_DIRECTION_UP, ScrollDirection.SCROLL_DIRECTION_DOWN })
  public @interface ScrollDirection {
    int SCROLL_DIRECTION_UP = 1;
    int SCROLL_DIRECTION_DOWN = -1;
    int SCROLL_NONE = 0;
  }

  /**
   * @return one of: SCROLL_DIRECTION_UP, SCROLL_DIRECTION_DOWN, SCROLL_NONE as over scroll dir
   */
  @ScrollDirection public int getOverScrollDirection() {
    return mOverScrollDirection;
  }

  /**
   * @return one of: SCROLL_DIRECTION_UP, SCROLL_DIRECTION_DOWN, SCROLL_NONE as scroll dir
   */

  @ScrollDirection public int getScrollDirection() {
    return mScrollDirection;
  }

  /**
   * @param direction Direction of the over scroll: SCROLL_DIRECTION_UP, SCROLL_DIRECTION_DOWN
   * @param currentOverScroll Unconsumed value, negative or positive based on the direction;
   * @param totalOverScroll Cumulative value for current direction
   */
  public abstract void onNestedVerticalOverScroll(CoordinatorLayout coordinatorLayout, V child,
      @ScrollDirection int direction, int currentOverScroll, int totalOverScroll);

  /**
   * @param scrollDirection Direction of the overscroll: SCROLL_DIRECTION_UP, SCROLL_DIRECTION_DOWN
   */
  public abstract void onDirectionNestedPreScroll(CoordinatorLayout coordinatorLayout, V child,
      View target, int dx, int dy, int[] consumed, @ScrollDirection int scrollDirection);

  @Override public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, V child,
      View directTargetChild, View target, int nestedScrollAxes) {
    return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
  }

  @Override public void onNestedScrollAccepted(CoordinatorLayout coordinatorLayout, V child,
      View directTargetChild, View target, int nestedScrollAxes) {
    super.onNestedScrollAccepted(coordinatorLayout, child, directTargetChild, target,
        nestedScrollAxes);
  }

  @Override
  public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, V child, View target) {
    super.onStopNestedScroll(coordinatorLayout, child, target);
  }

  @Override public void onNestedScroll(CoordinatorLayout coordinatorLayout, V child, View target,
      int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
    super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed,
        dyUnconsumed);
    if (dyUnconsumed > 0 && mTotalDyUnconsumed < 0) {
      mTotalDyUnconsumed = 0;
      mOverScrollDirection = ScrollDirection.SCROLL_DIRECTION_UP;
    } else if (dyUnconsumed < 0 && mTotalDyUnconsumed > 0) {
      mTotalDyUnconsumed = 0;
      mOverScrollDirection = ScrollDirection.SCROLL_DIRECTION_DOWN;
    }
    mTotalDyUnconsumed += dyUnconsumed;
    onNestedVerticalOverScroll(coordinatorLayout, child, mOverScrollDirection, dyConsumed,
        mTotalDyUnconsumed);
  }

  @Override
  public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, V child, View target, int dx,
      int dy, int[] consumed) {
    super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
    if (dy > 0 && mTotalDy < 0) {
      mTotalDy = 0;
      mScrollDirection = ScrollDirection.SCROLL_DIRECTION_UP;
    } else if (dy < 0 && mTotalDy > 0) {
      mTotalDy = 0;
      mScrollDirection = ScrollDirection.SCROLL_DIRECTION_DOWN;
    }
    mTotalDy += dy;
    onDirectionNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed,
        mScrollDirection);
  }

  @Override public boolean onNestedFling(CoordinatorLayout coordinatorLayout, V child, View target,
      float velocityX, float velocityY, boolean consumed) {
    super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
    mScrollDirection =
        velocityY > 0 ? ScrollDirection.SCROLL_DIRECTION_UP : ScrollDirection.SCROLL_DIRECTION_DOWN;
    return onNestedDirectionFling(coordinatorLayout, child, target, velocityX, velocityY,
        mScrollDirection);
  }

  protected abstract boolean onNestedDirectionFling(CoordinatorLayout coordinatorLayout, V child,
      View target, float velocityX, float velocityY, @ScrollDirection int scrollDirection);

  @Override
  public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, V child, View target,
      float velocityX, float velocityY) {
    return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY);
  }

  @Override
  public WindowInsetsCompat onApplyWindowInsets(CoordinatorLayout coordinatorLayout, V child,
      WindowInsetsCompat insets) {

    return super.onApplyWindowInsets(coordinatorLayout, child, insets);
  }

  @Override public Parcelable onSaveInstanceState(CoordinatorLayout parent, V child) {
    return super.onSaveInstanceState(parent, child);
  }
}