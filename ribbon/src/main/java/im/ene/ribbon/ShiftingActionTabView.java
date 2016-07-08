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

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import static android.util.Log.INFO;
import static android.util.Log.VERBOSE;
import static im.ene.ribbon.MiscUtils.log;

@SuppressLint("ViewConstructor")  //
public class ShiftingActionTabView extends ActionTabView {
  private static final String TAG = ShiftingActionTabView.class.getSimpleName();
  private final int paddingTop;
  private final int paddingBottomActive;
  private final int iconSize;
  private final int paddingBottomInactive;
  private final int textSize;

  private int centerY;
  private final float maxAlpha;
  private final float minAlpha;
  private final Interpolator interpolator = new DecelerateInterpolator();
  private float textWidth;
  private long animationDuration;
  private final int colorActive;
  private final int colorInactive;
  private float textX;
  private int textY;

  public ShiftingActionTabView(final BottomNavigationView parent, boolean expanded,
      final MenuParser.Menu menu) {
    super(parent, expanded, menu);

    this.paddingTop = getResources().getDimensionPixelSize(R.dimen.ribbon_shifting_item_padding_top);
    this.paddingBottomActive =
        getResources().getDimensionPixelSize(R.dimen.ribbon_shifting_active_item_padding_bottom);
    this.paddingBottomInactive =
        getResources().getDimensionPixelSize(R.dimen.ribbon_shifting_inactive_item_padding_bottom);
    this.iconSize = getResources().getDimensionPixelSize(R.dimen.ribbon_shifting_item_icon_size);
    this.textSize = getResources().getDimensionPixelSize(R.dimen.ribbon_shifting_text_size);

    this.animationDuration = menu.getItemAnimationDuration();
    this.colorActive = menu.getColorActive();
    this.colorInactive = menu.getColorInactive();
    this.minAlpha = Color.alpha(this.colorInactive) / ALPHA_MAX;
    this.maxAlpha = Math.max((float) Color.alpha(colorActive) / ALPHA_MAX, minAlpha);

    this.centerY = expanded ? paddingTop : paddingBottomInactive;
    this.textPaint.setHinting(Paint.HINTING_ON);
    this.textPaint.setLinearText(true);
    this.textPaint.setSubpixelText(true);
    this.textPaint.setTextSize(textSize);
    this.textPaint.setColor(colorActive);

    if (!expanded) {
      this.textPaint.setAlpha(0);
    }

    if (BottomNavigationView.DEBUG) {
      log(TAG, VERBOSE, "colors: %x, %x", colorInactive, colorActive);
      log(TAG, VERBOSE, "alphas: %g, %g", minAlpha, maxAlpha);
    }
  }

  @Override
  protected void onStatusChanged(final boolean expanded, final int size, final boolean animate) {
    log(TAG, INFO, "onStatusChanged(%b, %d)", expanded, size);

    if (!animate) {
      updateLayoutOnAnimation(size, 1, expanded);
      setCenterY(expanded ? paddingTop : paddingBottomInactive);
      return;
    }

    final AnimatorSet set = new AnimatorSet();
    set.setDuration(animationDuration * 2);
    set.setInterpolator(interpolator);
    final ValueAnimator animator1 = ValueAnimator.ofInt(getLayoutParams().width, size);
    final ValueAnimator animator2 =
        ObjectAnimator.ofInt(this, "centerY", expanded ? paddingBottomInactive : paddingTop,
            expanded ? paddingTop : paddingBottomInactive);

    animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(final ValueAnimator animation) {
        int size = (int) animation.getAnimatedValue();
        final float fraction = animation.getAnimatedFraction();
        updateLayoutOnAnimation(size, fraction, expanded);
      }
    });

    set.playTogether(animator1, animator2);
    set.start();
  }

  private void updateLayoutOnAnimation(final int size, final float fraction,
      final boolean expanded) {
    getLayoutParams().width = size;

    if (expanded) {
      final int color = (Integer) evaluator.evaluate(fraction, colorInactive, colorActive);
      icon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
      icon.setAlpha((int) ((minAlpha + (fraction * (maxAlpha - minAlpha))) * ALPHA_MAX));
      textPaint.setAlpha((int) (((fraction * (maxAlpha))) * ALPHA_MAX));
    } else {
      final int color = (Integer) evaluator.evaluate(fraction, colorActive, colorInactive);
      final float alpha = 1.0F - fraction;
      icon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
      icon.setAlpha((int) ((minAlpha + (alpha * (maxAlpha - minAlpha))) * ALPHA_MAX));
      textPaint.setAlpha((int) (((alpha * (maxAlpha))) * ALPHA_MAX));
    }
  }

  private void measureText() {
    log(TAG, INFO, "measureText");
    this.textWidth = textPaint.measureText(getItem().getTitle());
  }

  @Override
  protected void onLayout(final boolean changed, final int left, final int top, final int right,
      final int bottom) {
    super.onLayout(changed, left, top, right, bottom);

    if (null == this.icon) {
      this.icon = getItem().getIcon(getContext());
      icon.setBounds(0, 0, iconSize, iconSize);
      icon.setColorFilter(isExpanded() ? colorActive : colorInactive, PorterDuff.Mode.SRC_ATOP);
      icon.setAlpha((int) (isExpanded() ? maxAlpha * ALPHA_MAX : minAlpha * ALPHA_MAX));
    }

    if (textDirty) {
      measureText();
      textDirty = false;
    }

    if (changed) {
      int w = right - left;
      int h = bottom - top;
      int centerX = (w - iconSize) / 2;
      this.textY = h - paddingBottomActive;
      this.textX = (w - textWidth) / 2;
      icon.setBounds(centerX, centerY, centerX + iconSize, centerY + iconSize);
    }
  }

  @Override protected void onDraw(final Canvas canvas) {
    super.onDraw(canvas);
    icon.draw(canvas);
    canvas.drawText(getItem().getTitle(), textX, textY, textPaint);
    drawBadge(canvas);
  }

  @SuppressWarnings("unused") @proguard.annotation.Keep public int getCenterY() {
    return centerY;
  }

  @SuppressWarnings("unused") @proguard.annotation.Keep public void setCenterY(int value) {
    centerY = value;
    requestLayout();
  }
}
