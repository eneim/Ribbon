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

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.view.ViewCompat;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

@SuppressLint("ViewConstructor")  //
public class TabletActionTabView extends ActionTabView {
  private static final String TAG = TabletActionTabView.class.getSimpleName();
  private final int iconSize;

  private final Interpolator interpolator = new DecelerateInterpolator();
  private long animationDuration;
  private final int colorActive;
  private final int colorInactive;
  private final ArgbEvaluator evaluator;

  public TabletActionTabView(final BottomNavigationView parent, boolean expanded,
      final MenuParser.Menu menu) {
    super(parent, expanded, menu);
    this.evaluator = new ArgbEvaluator();
    final Resources res = getResources();
    this.iconSize = res.getDimensionPixelSize(R.dimen.ribbon_tablet_item_icon_size);
    this.animationDuration = menu.getItemAnimationDuration();
    this.colorActive = menu.getColorActive();
    this.colorInactive = menu.getColorInactive();
  }

  @Override
  protected void onStatusChanged(final boolean expanded, final int size, final boolean animate) {
    if (!animate) {
      updateLayoutOnAnimation(1, expanded);
      return;
    }

    final ValueAnimator animator = ObjectAnimator.ofFloat(0, 1);
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(final ValueAnimator animation) {
        updateLayoutOnAnimation(animation.getAnimatedFraction(), expanded);
      }
    });
    animator.setDuration(animationDuration);
    animator.setInterpolator(interpolator);
    animator.start();
  }

  private void updateLayoutOnAnimation(final float fraction, final boolean expanded) {
    if (expanded) {
      final int color = (int) evaluator.evaluate(fraction, colorInactive, colorActive);
      icon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
      icon.setAlpha(Color.alpha(color));
    } else {
      int color = (int) evaluator.evaluate(fraction, colorActive, colorInactive);
      icon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
      icon.setAlpha(Color.alpha(color));
    }
    ViewCompat.postInvalidateOnAnimation(this);
  }

  @Override
  protected void onLayout(final boolean changed, final int left, final int top, final int right,
      final int bottom) {
    super.onLayout(changed, left, top, right, bottom);

    if (null == this.icon) {
      this.icon = getItem().getIcon(getContext()).mutate();
      this.icon.setColorFilter(isExpanded() ? colorActive : colorInactive,
          PorterDuff.Mode.SRC_ATOP);
      this.icon.setAlpha(Color.alpha(isExpanded() ? colorActive : colorInactive));
      this.icon.setBounds(0, 0, iconSize, iconSize);
    }

    if (changed) {
      final int w = right - left;
      final int h = bottom - top;
      final int centerX = (w - iconSize) / 2;
      final int centerY = (h - iconSize) / 2;
      icon.setBounds(centerX, centerY, centerX + iconSize, centerY + iconSize);
    }
  }

  @Override protected void onDraw(final Canvas canvas) {
    super.onDraw(canvas);
    icon.draw(canvas);
    drawBadge(canvas);
  }
}
