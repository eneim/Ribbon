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

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IdRes;
import android.support.annotation.MenuRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import static android.util.Log.INFO;
import static android.util.Log.VERBOSE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static im.ene.ribbon.MiscUtils.log;

public class BottomNavigationView extends FrameLayout implements OnItemClickListener {
  private static final String TAG = BottomNavigationView.class.getSimpleName();

  @SuppressWarnings("checkstyle:staticvariablename") public static boolean DEBUG = false;

  static final int PENDING_ACTION_NONE = 0x0;
  static final int PENDING_ACTION_EXPANDED = 0x1;
  static final int PENDING_ACTION_COLLAPSED = 0x2;
  static final int PENDING_ACTION_ANIMATE_ENABLED = 0x4;

  private static final String WIDGET_PACKAGE_NAME;

  static {
    final Package pkg = BottomNavigationView.class.getPackage();
    WIDGET_PACKAGE_NAME = pkg != null ? pkg.getName() : null;
  }

  static final Class<?>[] CONSTRUCTOR_PARAMS = new Class<?>[] { BottomNavigationView.class };

  /**
   * Current pending action (used inside the BottomBehavior instance)
   */
  private int mPendingAction = PENDING_ACTION_NONE;

  /**
   * This is the amount of space we have to cover in case there's a translucent navigation
   * enabled.
   */
  private int bottomInset;

  /**
   * This is the amount of space we have to cover in case there's a translucent status
   * enabled.
   */
  private int topInset;

  /**
   * This is the current view height. It does take into account the extra space
   * used in case we have to cover the navigation translucent area, and neither the shadow height.
   */
  private int defaultHeight;

  /**
   * Same as defaultHeight, but for tablet mode.
   */
  private int defaultWidth;

  /**
   * Shadow is created above the widget background. It simulates the
   * elevation.
   */
  private int shadowHeight;

  /**
   * Layout container used to create and manage the UI items. It can be either {@link
   * FixedTabLayout} or {@link ShiftingTabLayout}, based on the widget `mode`
   */
  private BottomTabLayout itemsContainer;

  /**
   * This is where the color animation is happening
   */
  private View backgroundOverlay;

  /**
   * current menu
   */
  MenuParser.Menu menu;

  private MenuParser.Menu pendingMenu;

  /**
   * Default selected index.
   * After the items are populated changing this
   * won't have any effect
   */
  private int defaultSelectedIndex = 0;

  /**
   * View visible background color
   */
  private ColorDrawable backgroundDrawable;

  /**
   * Animation duration for the background color change
   */
  private long backgroundColorAnimation;

  /**
   * Optional typeface used for the items' text labels
   */
  SoftReference<Typeface> typeface;

  /**
   * Current BottomBehavior assigned from the CoordinatorLayout
   */
  private CoordinatorLayout.Behavior mBehavior;

  /**
   * Menu selection listener
   */
  private OnTabSelectedListener listener;

  /**
   * The user defined layout_gravity
   */
  private int gravity;

  /**
   * View is attached
   */
  private boolean attached;

  private BadgeProvider badgeProvider;

  public BottomNavigationView(final Context context) {
    this(context, null);
  }

  public BottomNavigationView(final Context context, final AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public BottomNavigationView(final Context context, final AttributeSet attrs,
      final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize(context, attrs, defStyleAttr, 0);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public BottomNavigationView(final Context context, final AttributeSet attrs,
      final int defStyleAttr, final int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override protected Parcelable onSaveInstanceState() {
    log(TAG, INFO, "onSaveInstanceState");
    Parcelable parcelable = super.onSaveInstanceState();
    SavedState savedState = new SavedState(parcelable);

    if (null == menu) {
      savedState.selectedIndex = 0;
    } else {
      savedState.selectedIndex = Math.max(0, Math.min(getSelectedItem(), menu.getActionCount() - 1));
    }

    if (badgeProvider != null) {
      savedState.badgeBundle = badgeProvider.save();
    }

    return savedState;
  }

  @Override protected void onRestoreInstanceState(final Parcelable state) {
    log(TAG, INFO, "onRestoreInstanceState");
    SavedState savedState = (SavedState) state;
    super.onRestoreInstanceState(savedState.getSuperState());

    defaultSelectedIndex = savedState.selectedIndex;
    log(TAG, Log.DEBUG, "defaultSelectedIndex: %d", defaultSelectedIndex);

    if (badgeProvider != null && savedState.badgeBundle != null) {
      badgeProvider.restore(savedState.badgeBundle);
    }
  }

  public final BadgeProvider getBadgeProvider() {
    return badgeProvider;
  }

  private void initialize(final Context context, final AttributeSet attrs, final int defStyleAttr,
      final int defStyleRes) {
    typeface = new SoftReference<>(Typeface.DEFAULT);

    TypedArray array =
        context.obtainStyledAttributes(attrs, R.styleable.BottomNavigationView, defStyleAttr,
            defStyleRes);
    final int menuResId = array.getResourceId(R.styleable.BottomNavigationView_ribbon_menu, 0);
    if (menuResId == 0) {
      throw new IllegalArgumentException("A valid menu must be set in xml");
    }

    pendingMenu = MenuParser.inflateMenu(context, menuResId);
    badgeProvider = parseBadgeProvider(this, context,
        array.getString(R.styleable.BottomNavigationView_ribbon_badgeProvider));
    array.recycle();

    backgroundColorAnimation =
        getResources().getInteger(R.integer.ribbon_background_animation_duration);

    defaultSelectedIndex = 0;

    defaultHeight = getResources().getDimensionPixelSize(R.dimen.ribbon_bottom_navigation_height);
    defaultWidth = getResources().getDimensionPixelSize(R.dimen.ribbon_bottom_navigation_width);
    shadowHeight = getResources().getDimensionPixelOffset(R.dimen.ribbon_top_shadow_height);

    // check if the bottom navigation is translucent
    if (!isInEditMode()) {
      final Activity activity = MiscUtils.getActivity(context);
      if (activity != null) {
        final SystemBarTintManager systemBarTintManager = new SystemBarTintManager(activity);
        if (MiscUtils.hasTranslucentNavigationBar(activity) &&  //
            systemBarTintManager.getConfig().isNavigationAtBottom() &&  //
            systemBarTintManager.getConfig().hasNavigtionBar()) {
          bottomInset = systemBarTintManager.getConfig().getNavigationBarHeight();
        } else {
          bottomInset = 0;
        }
        topInset = systemBarTintManager.getConfig().getStatusBarHeight();
      }
    }

    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
    params.topMargin = shadowHeight;
    backgroundOverlay = new View(getContext());
    backgroundOverlay.setLayoutParams(params);
    addView(backgroundOverlay);
  }

  /* package */ int getPendingAction() {
    return mPendingAction;
  }

  /* package */ void resetPendingAction() {
    mPendingAction = PENDING_ACTION_NONE;
  }

  @Override public void setLayoutParams(final ViewGroup.LayoutParams params) {
    log(TAG, INFO, "setLayoutParams: %s", params);
    super.setLayoutParams(params);
  }

  private boolean isTablet(final int gravity) {
    return MiscUtils.isGravityLeft(gravity) || MiscUtils.isGravityRight(gravity);
  }

  @SuppressWarnings("unused")
  public void setSelectedItem(final int position, final boolean animate) {
    if (itemsContainer != null) {
      setSelectedItemInternal(itemsContainer, ((ViewGroup) itemsContainer).getChildAt(position),
          position, animate, false);
    } else {
      defaultSelectedIndex = position;
    }
  }

  @SuppressWarnings("unused") public int getSelectedItem() {
    if (itemsContainer != null) {
      return itemsContainer.getSelectedItem();
    }

    return -1;
  }

  @SuppressWarnings("unused") public void setExpanded(boolean expanded, boolean animate) {
    log(TAG, INFO, "setExpanded(%b, %b)", expanded, animate);
    mPendingAction = (expanded ? PENDING_ACTION_EXPANDED : PENDING_ACTION_COLLAPSED) | (animate
        ? PENDING_ACTION_ANIMATE_ENABLED : 0);
    requestLayout();
  }

  public boolean isExpanded() {
    return mBehavior != null
        && mBehavior instanceof BottomNavigationBehavior
        && ((BottomNavigationBehavior) mBehavior).isExpanded();
  }

  @SuppressWarnings("unused")
  public void setOnActionClickListener(final OnTabSelectedListener listener) {
    this.listener = listener;
  }

  @SuppressWarnings("unused") public void setMenuItems(@MenuRes final int menuResId) {
    defaultSelectedIndex = 0;
    if (isAttachedToWindow()) {
      setMenu(MenuParser.inflateMenu(getContext(), menuResId));
      pendingMenu = null;
    } else {
      pendingMenu = MenuParser.inflateMenu(getContext(), menuResId);
    }
  }

  /**
   * Returns the current menu items count
   *
   * @return number of items in the current menu
   */
  public int getMenuItemCount() {
    if (null != menu) {
      return menu.getActionCount();
    }
    return 0;
  }

  /**
   * Returns the id of the item at the specified position
   *
   * @param position the position inside the menu
   * @return the item ID
   */
  @IdRes public int getMenuItemId(final int position) {
    if (null != menu) {
      return menu.getActionItemAt(position).getItemId();
    }
    return 0;
  }

  @Override protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    log(TAG, INFO, "onMeasure: %d", gravity);
    if (MiscUtils.isGravityBottom(gravity)) {
      final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
      final int widthSize = MeasureSpec.getSize(widthMeasureSpec);

      if (widthMode == MeasureSpec.AT_MOST) {
        throw new IllegalArgumentException("layout_width must be equal to `match_parent`");
      }
      setMeasuredDimension(widthSize, defaultHeight + bottomInset + shadowHeight);
    } else if (MiscUtils.isGravityLeft(gravity) || MiscUtils.isGravityRight(gravity)) {
      final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
      final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

      if (heightMode == MeasureSpec.AT_MOST) {
        throw new IllegalArgumentException("layout_height must be equal to `match_parent`");
      }
      setMeasuredDimension(defaultWidth, heightSize);
    } else {
      throw new IllegalArgumentException(
          "invalid layout_gravity. Only one start, end, left, right or bottom is allowed");
    }
  }

  @SuppressWarnings("unused") public int getNavigationHeight() {
    return defaultHeight;
  }

  @SuppressWarnings("unused") public int getNavigationWidth() {
    return defaultWidth;
  }

  @Override protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
    log(TAG, INFO, "onSizeChanged(%d, %d)", w, h);
    super.onSizeChanged(w, h, oldw, oldh);
    MarginLayoutParams marginLayoutParams = (MarginLayoutParams) getLayoutParams();
    marginLayoutParams.bottomMargin = -bottomInset;
  }

  public boolean isAttachedToWindow() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      return super.isAttachedToWindow();
    }
    return attached;
  }

  private void resolveGravity(ViewGroup.LayoutParams params) {
    if (params instanceof FrameLayout.LayoutParams) {
      this.gravity = GravityCompat.getAbsoluteGravity(((LayoutParams) params).gravity,
          ViewCompat.getLayoutDirection(this));
    } else if (params instanceof CoordinatorLayout.LayoutParams) {
      this.gravity =
          GravityCompat.getAbsoluteGravity(((CoordinatorLayout.LayoutParams) params).gravity,
              ViewCompat.getLayoutDirection(this));
    }

    initializeUI(gravity);
  }

  @Override protected void onAttachedToWindow() {
    log(TAG, INFO, "onAttachedToWindow");
    super.onAttachedToWindow();
    attached = true;

    final ViewGroup.LayoutParams params = getLayoutParams();
    resolveGravity(params);

    if (null != pendingMenu) {
      setMenu(pendingMenu);
      pendingMenu = null;
    }

    if (mBehavior == null) {
      if (params instanceof CoordinatorLayout.LayoutParams) {
        mBehavior = ((CoordinatorLayout.LayoutParams) params).getBehavior();

        if (isInEditMode()) {
          return;
        }

        if (mBehavior instanceof BottomNavigationBehavior) {
          ((BottomNavigationBehavior) mBehavior).setLayoutValues(defaultHeight, bottomInset);
        } else if (mBehavior instanceof TabletBehavior) {
          final Activity activity = MiscUtils.getActivity(getContext());
          boolean translucentStatus = MiscUtils.hasTranslucentStatusBar(activity);
          ((TabletBehavior) mBehavior).setLayoutValues(defaultWidth, topInset, translucentStatus);
        }
      }
    }
  }

  @SuppressWarnings("unused") public CoordinatorLayout.Behavior getBehavior() {
    if (mBehavior == null) {
      if (getLayoutParams() instanceof CoordinatorLayout.LayoutParams) {
        return ((CoordinatorLayout.LayoutParams) getLayoutParams()).getBehavior();
      }
    }
    return mBehavior;
  }

  private void setMenu(MenuParser.Menu menu) {
    log(TAG, INFO, "setMenu: %s", menu);
    this.menu = menu;
    if (menu != null) {
      if (menu.getActionCount() < 3 || menu.getActionCount() > 5) {
        throw new IllegalArgumentException(
            "BottomNavigation expects 3 to 5 items. " + menu.getActionCount() + " found");
      }

      menu.setTabletMode(isTablet(gravity));

      initializeBackgroundColor(menu);
      initializeContainer(menu);
      initializeItems(menu);
    }

    requestLayout();
  }

  private void initializeUI(final int gravity) {
    log(TAG, INFO, "initializeUI(%d)", gravity);
    final LayerDrawable layerDrawable;

    final boolean tablet = isTablet(gravity);
    final int elevation = getResources().getDimensionPixelSize(
        !tablet ? R.dimen.ribbon_elevation : R.dimen.ribbon_elevation_tablet);
    final int backgroundResId = !tablet ? R.drawable.ribbon_background
        : (MiscUtils.isGravityRight(gravity) ? R.drawable.ribbon_background_tablet_right
            : R.drawable.ribbon_background_tablet_left);
    final int paddingTop = !tablet ? shadowHeight : 0;

    // View elevation
    ViewCompat.setElevation(this, elevation);

    // Main background
    layerDrawable = (LayerDrawable) ContextCompat.getDrawable(getContext(), backgroundResId);
    // layerDrawable.mutate();
    backgroundDrawable =
        (ColorDrawable) ((LayerDrawable) layerDrawable.mutate()).findDrawableByLayerId(
            R.id.bbn_background);
    setBackground(layerDrawable);

    // Padding bottom
    setPadding(0, paddingTop, 0, 0);
  }

  private void initializeBackgroundColor(final MenuParser.Menu menu) {
    log(TAG, INFO, "initializeBackgroundColor");

    final int color = menu.getBackground();
    log(TAG, VERBOSE, "background: %x", color);
    backgroundDrawable.setColor(color);
  }

  private void initializeContainer(final MenuParser.Menu menu) {
    log(TAG, INFO, "initializeContainer");
    if (itemsContainer != null) {
      if (menu.isTablet() && !(itemsContainer instanceof TabletLayout)) {
        removeView((View) itemsContainer);
        itemsContainer = null;
      } else if ((menu.isShifting() && !(itemsContainer instanceof ShiftingTabLayout))
          || (!menu.isShifting() && !(itemsContainer instanceof FixedTabLayout))) {
        removeView((View) itemsContainer);
        itemsContainer = null;
      } else {
        itemsContainer.removeAll();
      }
    }

    if (itemsContainer == null) {
      LinearLayout.LayoutParams params =
          new LinearLayout.LayoutParams(menu.isTablet() ? defaultWidth : MATCH_PARENT,
              menu.isTablet() ? MATCH_PARENT : defaultHeight);

      if (menu.isTablet()) {
        itemsContainer = new TabletLayout(getContext());
      } else if (menu.isShifting()) {
        itemsContainer = new ShiftingTabLayout(getContext());
      } else {
        itemsContainer = new FixedTabLayout(getContext());
      }

      // force the layout manager ID
      itemsContainer.setId(R.id.ribbon_container);
      itemsContainer.setLayoutParams(params);
      addView((View) itemsContainer);
    }
  }

  private void initializeItems(final MenuParser.Menu menu) {
    log(TAG, INFO, "initializeItems(%d)", defaultSelectedIndex);

    itemsContainer.setSelectedItem(defaultSelectedIndex, false);
    itemsContainer.populate(menu);
    itemsContainer.setOnItemClickListener(this);

    if (menu.getActionItemAt(defaultSelectedIndex).hasColor()) {
      backgroundDrawable.setColor(menu.getActionItemAt(defaultSelectedIndex).getColor());
    }
  }

  @Override public void onItemClick(final BottomTabLayout parent, final View view, final int index,
      boolean animate) {
    log(TAG, INFO, "onItemClick: %d", index);
    setSelectedItemInternal(parent, view, index, animate, true);
  }

  private void setSelectedItemInternal(final BottomTabLayout container, final View view,
      final int index, final boolean animate, final boolean fromUser) {

    final ActionTab item = menu.getActionItemAt(index);

    if (container.getSelectedItem() != index) {
      container.setSelectedItem(index, animate);

      if (!menu.isTablet() && item.hasColor()) {
        if (animate) {
          AnimUtil.animate(this, view, backgroundOverlay, backgroundDrawable, item.getColor(),
              backgroundColorAnimation);
        } else {
          MiscUtils.switchColor(this, view, backgroundOverlay, backgroundDrawable, item.getColor());
        }
      }

      if (null != listener && fromUser) {
        listener.onTabSelected(item.getItemId(), index);
      }
    } else {
      if (listener != null && fromUser) {
        listener.onTabReselected(item.getItemId(), index);
      }
    }
  }

  public void setDefaultTypeface(final Typeface typeface) {
    this.typeface = new SoftReference<>(typeface);
  }

  public void setDefaultSelectedIndex(final int defaultSelectedIndex) {
    this.defaultSelectedIndex = defaultSelectedIndex;
  }

  public void invalidateBadge(final int itemId) {
    log(TAG, INFO, "invalidateBadge: %d", itemId);
    if (null != itemsContainer) {
      final ActionTabView actionTabView = (ActionTabView) itemsContainer.findViewById(itemId);
      if (null != actionTabView) {
        actionTabView.invalidateBadge();
      }
    }
  }

  static final ThreadLocal<Map<String, Constructor<BadgeProvider>>> S_CONSTRUCTORS =
      new ThreadLocal<>();

  static BadgeProvider parseBadgeProvider(final BottomNavigationView navigation,
      final Context context, final String name) {
    log(TAG, INFO, "parseBadgeProvider: %s", name);

    if (TextUtils.isEmpty(name)) {
      return new BadgeProvider(navigation);
    }

    final String fullName;
    if (name.startsWith(".")) {
      fullName = context.getPackageName() + name;
    } else if (name.indexOf('.') >= 0) {
      fullName = name;
    } else {
      // Assume stock behavior in this package (if we have one)
      fullName =
          !TextUtils.isEmpty(WIDGET_PACKAGE_NAME) ? (WIDGET_PACKAGE_NAME + '.' + name) : name;
    }

    try {
      Map<String, Constructor<BadgeProvider>> constructors = S_CONSTRUCTORS.get();
      if (constructors == null) {
        constructors = new HashMap<>();
        S_CONSTRUCTORS.set(constructors);
      }
      Constructor<BadgeProvider> c = constructors.get(fullName);
      if (c == null) {
        final Class<BadgeProvider> clazz =
            (Class<BadgeProvider>) Class.forName(fullName, true, context.getClassLoader());
        c = clazz.getConstructor(CONSTRUCTOR_PARAMS);
        c.setAccessible(true);
        constructors.put(fullName, c);
      }
      return c.newInstance(navigation);
    } catch (Exception e) {
      throw new RuntimeException("Could not inflate Behavior subclass " + fullName, e);
    }
  }

  public interface OnTabSelectedListener {

    void onTabSelected(@IdRes final int itemId, final int position);

    void onTabReselected(@IdRes final int itemId, final int position);
  }

  static class SavedState extends BaseSavedState {
    int selectedIndex;
    Bundle badgeBundle;

    public SavedState(Parcel in) {
      super(in);
      selectedIndex = in.readInt();
      badgeBundle = in.readBundle();
    }

    public SavedState(final Parcelable superState) {
      super(superState);
    }

    @Override public void writeToParcel(final Parcel out, final int flags) {
      super.writeToParcel(out, flags);
      out.writeInt(selectedIndex);
      out.writeBundle(badgeBundle);
    }

    @Override public int describeContents() {
      return super.describeContents();
    }

    public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
      public SavedState createFromParcel(Parcel in) {
        return new SavedState(in);
      }

      public SavedState[] newArray(int size) {
        return new SavedState[size];
      }
    };
  }
}
