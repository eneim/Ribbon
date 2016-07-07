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
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.support.v4.view.ViewCompat;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import proguard.annotation.Keep;

import static android.util.Log.DEBUG;
import static android.util.Log.INFO;
import static im.ene.ribbon.MiscUtils.log;

@SuppressLint("ViewConstructor")  //
public class FixedActionTabView extends ActionTabView {

  private static final String TAG = FixedActionTabView.class.getSimpleName();
  private final int iconSize;
  private int centerY;
  private final Interpolator interpolator = new DecelerateInterpolator();
  private float textWidth;
  private long animationDuration;
  private final int colorActive;
  private final int colorInactive;

  private final int paddingTopActive;
  private final int paddingTopInactive;
  private final int paddingBottom;
  private final int paddingHorizontal;
  private final int textSizeInactive;

  private static final float TEXT_SCALE_ACTIVE = 1.1666666667f;
  private float canvasTextScale;
  private float iconTranslation;
  private int textCenterX;
  private int textCenterY;
  private int centerX;
  private float textX;
  private float textY;

  public FixedActionTabView(final BottomNavigationView parent, boolean expanded,
      final MenuParser.Menu menu) {
    super(parent, expanded, menu);

    final Resources res = getResources();
    this.paddingTopActive = res.getDimensionPixelSize(R.dimen.bbn_fixed_item_padding_top_active);
    this.paddingTopInactive =
        res.getDimensionPixelSize(R.dimen.bbn_fixed_item_padding_top_inactive);
    this.paddingBottom = res.getDimensionPixelSize(R.dimen.bbn_fixed_item_padding_bottom);
    this.paddingHorizontal = res.getDimensionPixelSize(R.dimen.bbn_fixed_item_padding_horizontal);
    this.textSizeInactive = res.getDimensionPixelSize(R.dimen.bbn_fixed_text_size_inactive);
    this.iconSize = res.getDimensionPixelSize(R.dimen.bbn_fixed_item_icon_size);

    this.animationDuration = menu.getItemAnimationDuration();
    this.colorActive = menu.getColorActive();
    this.colorInactive = menu.getColorInactive();
    this.centerY = paddingTopActive;
    this.canvasTextScale = expanded ? TEXT_SCALE_ACTIVE : 1f;
    this.iconTranslation = expanded ? 0 : (paddingTopInactive - paddingTopActive);

    log(TAG, DEBUG, "colors: %x, %x", colorInactive, colorActive);

    this.textPaint.setColor(Color.WHITE);
    this.textPaint.setHinting(Paint.HINTING_ON);
    this.textPaint.setLinearText(true);
    this.textPaint.setSubpixelText(true);
    this.textPaint.setTextSize(textSizeInactive);
    this.textPaint.setColor(expanded ? colorActive : colorInactive);
  }

  @Override
  protected void onStatusChanged(final boolean expanded, final int size, final boolean animate) {
    if (!animate) {
      updateLayoutOnAnimation(1, expanded);
      setIconTranslation(expanded ? 0 : (paddingTopInactive - paddingTopActive));
      return;
    }

    final AnimatorSet set = new AnimatorSet();
    set.setDuration(animationDuration);
    set.setInterpolator(interpolator);

    final ValueAnimator textScaleAnimator =
        ObjectAnimator.ofFloat(this, "textScale", expanded ? TEXT_SCALE_ACTIVE : 1);

    textScaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(final ValueAnimator animation) {
        final float fraction = animation.getAnimatedFraction();
        updateLayoutOnAnimation(fraction, expanded);
      }
    });

    final ValueAnimator iconTranslationAnimator = ObjectAnimator.ofFloat(this, "iconTranslation",
        expanded ? 0 : (paddingTopInactive - paddingTopActive));

    set.playTogether(textScaleAnimator, iconTranslationAnimator);
    set.start();
  }

  private void updateLayoutOnAnimation(final float fraction, final boolean expanded) {
    if (expanded) {
      final int color = (Integer) evaluator.evaluate(fraction, colorInactive, colorActive);
      icon.mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
      textPaint.setColor(color);
      icon.setAlpha(Color.alpha(color));
    } else {
      final int color = (Integer) evaluator.evaluate(fraction, colorActive, colorInactive);
      icon.mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
      textPaint.setColor(color);
      icon.setAlpha(Color.alpha(color));
    }
  }

  final int getCurrentColor() {
    return isExpanded() ? colorActive : colorInactive;
  }

  @Override
  protected void onLayout(final boolean changed, final int left, final int top, final int right,
      final int bottom) {
    log(TAG, INFO, "onLayout(%b)", changed);
    super.onLayout(changed, left, top, right, bottom);

    if (this.icon == null) {
      this.icon = getItem().getIcon(getContext());
      this.icon.mutate().setColorFilter(getCurrentColor(), PorterDuff.Mode.SRC_ATOP);
      this.icon.setAlpha(Color.alpha(getCurrentColor()));
      this.icon.setBounds(0, 0, iconSize, iconSize);
    }

    if (changed) {
      int w = right - left;
      centerX = (w - iconSize) / 2;
      icon.setBounds(centerX, centerY, centerX + iconSize, centerY + iconSize);
    }

    if (textDirty || changed) {
      measureText();
      textDirty = false;
    }
  }

  private void measureText() {
    log(TAG, INFO, "measureText");

    final int width = getWidth();
    final int height = getHeight();

    textWidth = textPaint.measureText(getItem().getTitle());
    textX = paddingHorizontal + (((width - paddingHorizontal * 2) - textWidth) / 2);
    textY = height - paddingBottom;
    textCenterX = width / 2;
    textCenterY = height - paddingBottom;
  }

  @Override protected void onDraw(final Canvas canvas) {
    super.onDraw(canvas);

    canvas.save();
    canvas.translate(0, iconTranslation);
    icon.draw(canvas);
    drawBadge(canvas);
    canvas.restore();

    canvas.save();
    canvas.scale(canvasTextScale, canvasTextScale, textCenterX, textCenterY);
    canvas.drawText(getItem().getTitle(), textX, textY, textPaint);
    canvas.restore();
  }

  @SuppressWarnings("unused") @Keep public int getCenterY() {
    return centerY;
  }

  @SuppressWarnings("unused") @Keep public void setCenterY(int value) {
    centerY = value;
    ViewCompat.postInvalidateOnAnimation(this);
  }

  @SuppressWarnings("unused") @Keep public void setTextScale(final float value) {
    canvasTextScale = value;
    ViewCompat.postInvalidateOnAnimation(this);
  }

  @SuppressWarnings("unused") @Keep public float getTextScale() {
    return canvasTextScale;
  }

  // Keep in proguard for ObjectAnimator
  @Keep @SuppressWarnings("unused") public void setIconTranslation(final float iconTranslation) {
    this.iconTranslation = iconTranslation;
    ViewCompat.postInvalidateOnAnimation(this);
  }

  // Keep in proguard for ObjectAnimator
  @Keep @SuppressWarnings("unused") public float getIconTranslation() {
    return iconTranslation;
  }
}
