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

import android.animation.Animator;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by eneim on 7/7/16.
 */
class AnimUtil {

  private AnimUtil() {
    throw new AssertionError("Illegal initialization!");
  }

  static void animate(BottomNavigationView parent, View view, final View backgroundOverlay,
      final ColorDrawable backgroundDrawable, final int newColor, long duration) {
    int centerX = (int) (ViewCompat.getX(view) + (view.getWidth() / 2));
    int centerY = parent.getPaddingTop() + view.getHeight() / 2;

    backgroundOverlay.clearAnimation();

    final Object animator;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      Animator currentAnimator =
          (Animator) backgroundOverlay.getTag(R.id.ribbon_background_overlay_animator);
      if (currentAnimator != null) {
        //currentAnimator.end();
        currentAnimator.cancel();
      }

      final float startRadius = 1;
      final float finalRadius =
          centerX > parent.getWidth() / 2 ? centerX : parent.getWidth() - centerX;
      animator =
          ViewAnimationUtils.createCircularReveal(backgroundOverlay, centerX, centerY, startRadius,
              finalRadius);
      backgroundOverlay.setTag(R.id.ribbon_background_overlay_animator, animator);
    } else {
      ViewCompat.setAlpha(backgroundOverlay, 0);
      animator = ViewCompat.animate(backgroundOverlay).alpha(1);
    }

    backgroundOverlay.setBackgroundColor(newColor);
    backgroundOverlay.setVisibility(View.VISIBLE);

    if (animator instanceof ViewPropertyAnimatorCompat) {
      ((ViewPropertyAnimatorCompat) animator).setListener(new ViewPropertyAnimatorListener() {
        boolean cancelled;

        @Override public void onAnimationStart(final View view) {
        }

        @Override public void onAnimationEnd(final View view) {
          if (!cancelled) {
            backgroundDrawable.setColor(newColor);
            backgroundOverlay.setVisibility(View.INVISIBLE);
            ViewCompat.setAlpha(backgroundOverlay, 1);
          }
        }

        @Override public void onAnimationCancel(final View view) {
          cancelled = true;
        }
      }).setDuration(duration).start();
    } else {
      Animator animator1 = (Animator) animator;
      animator1.setDuration(duration);
      animator1.setInterpolator(new DecelerateInterpolator());
      animator1.addListener(new Animator.AnimatorListener() {
        boolean cancelled;

        @Override public void onAnimationStart(final Animator animation) {
        }

        @Override public void onAnimationEnd(final Animator animation) {
          if (!cancelled) {
            backgroundDrawable.setColor(newColor);
            backgroundOverlay.setVisibility(View.INVISIBLE);
            ViewCompat.setAlpha(backgroundOverlay, 1);
          }
        }

        @Override public void onAnimationCancel(final Animator animation) {
          cancelled = true;
        }

        @Override public void onAnimationRepeat(final Animator animation) {
        }
      });

      animator1.start();
    }
  }
}
