/*
 * Developer email: hiankit.work@gmail.com
 * GitHub: https://github.com/bxute
 */

package com.bxute.custombottomsheet;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

public class CustomBottomSheetBehaviour<V extends View> extends CoordinatorLayout.Behavior<V> {
  public static final int STATE_EXPANDED = 1;
  public static final int STATE_DRAGGING = 2;
  public static final int STATE_SETTLING = 3;
  public static final int STATE_COLLAPSED = 4;
  @State
  private int mState = STATE_COLLAPSED;
  private int mMinOffset;
  private int mMaxOffset;
  private int mParentHeight;
  private int mRowHeight;
  private int mLastNestedScroll;

  private ViewDragHelper mViewDragHelper;
  private boolean touchingScrollingChild;
  private int activePointerId;
  private boolean mIgnoreEvents;
  private WeakReference<V> viewRef;
  private WeakReference<View> nestedScrollingChildRef;
  private ViewDragHelper.Callback mViewDragHelperCallbacks = new ViewDragHelper.Callback() {

    @Override
    public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
      int top;
      @State int targetState;
      if (yvel < 0) { // Moving up
        top = mMinOffset;
        targetState = STATE_EXPANDED;
      } else if (yvel == 0.f || Math.abs(xvel) > Math.abs(yvel)) {
        // If the Y velocity is 0 or the swipe was mostly horizontal indicated by the X velocity
        // being greater than the Y velocity, settle to the nearest correct height.
        int currentTop = releasedChild.getTop();
        if (Math.abs(currentTop - mMinOffset)
         < Math.abs(currentTop - mMaxOffset)) {
          top = mMinOffset;
          targetState = STATE_EXPANDED;
        } else {
          top = mMaxOffset;
          targetState = STATE_COLLAPSED;
        }
      } else {
        top = mMaxOffset;
        targetState = STATE_COLLAPSED;
      }
      if (mViewDragHelper.settleCapturedViewAt(releasedChild.getLeft(), top)) {
        setStateInternal(STATE_SETTLING);
        ViewCompat.postOnAnimation(
         releasedChild, new SettleRunnable(releasedChild, targetState));
      } else {
        setStateInternal(targetState);
      }
    }

    @Override
    public int getViewVerticalDragRange(@NonNull View child) {
      return mMaxOffset - mMinOffset;
    }

    @Override
    public boolean tryCaptureView(@NonNull View child, int pointerId) {
      if (mState == STATE_DRAGGING) {
        return false;
      }
      if (touchingScrollingChild) {
        return false;
      }
      if (mState == STATE_EXPANDED && (activePointerId == pointerId)) {
        View scroll = nestedScrollingChildRef != null ? nestedScrollingChildRef.get() : null;
        if (scroll != null && scroll.canScrollVertically(-1)) {
          // Let the content scroll up
          return false;
        }
      }
      return viewRef != null && viewRef.get() == child;
    }

    @Override
    public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
      return child.getLeft();
    }

    @Override
    public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
      //return MathUtils.clamp(top, mMinOffset, mMaxOffset);
      return constrainedVerticalPositionFor(top, mMinOffset, mMaxOffset);
    }

    private int constrainedVerticalPositionFor(int top, int mMinOffset, int mMaxOffset) {
      return Math.max(mMinOffset, Math.min(top, mMaxOffset));
    }
  };
  private boolean nestedScrolled;
  private int lastNestedScrollDy;
  private VelocityTracker velocityTracker;
  private int initialY;

  public CustomBottomSheetBehaviour() {
  }

  public CustomBottomSheetBehaviour(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public static <V extends View> CustomBottomSheetBehaviour<V> from(V view) {
    ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
    if (!(layoutParams instanceof CoordinatorLayout.LayoutParams)) {
      throw new IllegalArgumentException("The view is not a child of coordinatorLayout");
    }
    CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) layoutParams).getBehavior();
    if (behavior instanceof CustomBottomSheetBehaviour) {
      throw new IllegalArgumentException("The view is not associated with BottomSheetBehaviour");
    }
    return (CustomBottomSheetBehaviour<V>) behavior;
  }

  public void configureBehaviour(int minOffset, int maxOffset, int rowHeight){
    this.mMinOffset = minOffset;
    this.mMaxOffset = maxOffset;
    this.mRowHeight = rowHeight;
  }

  @Override
  public boolean onInterceptTouchEvent(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent event) {
    if (!child.isShown()) {
      mIgnoreEvents = true;
      return false;
    }
    int action = event.getActionMasked();
    // Record the velocity
    if (action == MotionEvent.ACTION_DOWN) {
      reset();
    }
    if (velocityTracker == null) {
      velocityTracker = VelocityTracker.obtain();
    }
    velocityTracker.addMovement(event);
    switch (action) {
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        touchingScrollingChild = false;
        activePointerId = MotionEvent.INVALID_POINTER_ID;
        // Reset the ignore flag
        if (mIgnoreEvents) {
          mIgnoreEvents = false;
          return false;
        }
        break;
      case MotionEvent.ACTION_DOWN:
        int initialX = (int) event.getX();
        initialY = (int) event.getY();
        // Only intercept nested scrolling events here if the view not being moved by the
        // ViewDragHelper.
        if (mState != STATE_SETTLING) {
          View scroll = nestedScrollingChildRef != null ? nestedScrollingChildRef.get() : null;
          if (scroll != null && parent.isPointInChildBounds(scroll, initialX, initialY)) {
            activePointerId = event.getPointerId(event.getActionIndex());
            touchingScrollingChild = true;
          }
        }
        mIgnoreEvents =
         activePointerId == MotionEvent.INVALID_POINTER_ID
          && !parent.isPointInChildBounds(child, initialX, initialY);
        break;
      default: // fall out
    }
    if (!mIgnoreEvents
     && mViewDragHelper != null
     && mViewDragHelper.shouldInterceptTouchEvent(event)) {
      return true;
    }
    // We have to handle cases that the ViewDragHelper does not capture the bottom sheet because
    // it is not the top most view of its parent. This is not necessary when the touch event is
    // happening over the scrolling content as nested scrolling logic handles that case.
    View scroll = nestedScrollingChildRef != null ? nestedScrollingChildRef.get() : null;
    return action == MotionEvent.ACTION_MOVE
     && scroll != null
     && !mIgnoreEvents
     && mState != STATE_DRAGGING
     && !parent.isPointInChildBounds(scroll, (int) event.getX(), (int) event.getY())
     && mViewDragHelper != null
     && Math.abs(initialY - event.getY()) > mViewDragHelper.getTouchSlop();
  }

  private void reset() {
    activePointerId = ViewDragHelper.INVALID_POINTER;
    if (velocityTracker != null) {
      velocityTracker.recycle();
      velocityTracker = null;
    }
  }

  @Override
  public boolean onTouchEvent(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent event) {
    if (!child.isShown()) {
      return false;
    }
    int action = event.getActionMasked();
    if (mState == STATE_DRAGGING && action == MotionEvent.ACTION_DOWN) {
      return true;
    }
    if (mViewDragHelper != null) {
      mViewDragHelper.processTouchEvent(event);
    }
    // Record the velocity
    if (action == MotionEvent.ACTION_DOWN) {
      reset();
    }
    if (velocityTracker == null) {
      velocityTracker = VelocityTracker.obtain();
    }
    velocityTracker.addMovement(event);
    // The ViewDragHelper tries to capture only the top-most View. We have to explicitly tell it
    // to capture the bottom sheet in case it is not captured and the touch slop is passed.
    if (action == MotionEvent.ACTION_MOVE && !mIgnoreEvents) {
      if (Math.abs(initialY - event.getY()) > mViewDragHelper.getTouchSlop()) {
        mViewDragHelper.captureChildView(child, event.getPointerId(event.getActionIndex()));
      }
    }
    return !mIgnoreEvents;
  }

  @Override
  public boolean onLayoutChild(@NonNull CoordinatorLayout parent, @NonNull V child, int layoutDirection) {
    //let the parent layout its child
    parent.onLayoutChild(child, layoutDirection);
    mParentHeight = parent.getHeight();
    if (mState == STATE_EXPANDED) {
      ViewCompat.offsetTopAndBottom(child, mMinOffset);
    } else if (mState == STATE_COLLAPSED) {
      ViewCompat.offsetTopAndBottom(child, mMaxOffset);
    }

    if (mViewDragHelper == null) {
      mViewDragHelper = ViewDragHelper.create(parent, mViewDragHelperCallbacks);
    }

    viewRef = new WeakReference<>(child);
    nestedScrollingChildRef = new WeakReference<>(findScrollingChild(child));
    return true;
  }

  @Override
  public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                     @NonNull V child,
                                     @NonNull View directTargetChild,
                                     @NonNull View target,
                                     int axes,
                                     int type) {
    //we are interested in vertical scroll
    mLastNestedScroll = 0;
    nestedScrolled = false;
    return (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
  }

  @Override
  public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                 @NonNull V child,
                                 @NonNull View target,
                                 int type) {
    if (child.getTop() == mMinOffset) {
      setStateInternal(STATE_EXPANDED);
      return;
    }
    if (nestedScrollingChildRef == null
     || target != nestedScrollingChildRef.get()
     || !nestedScrolled) {
      return;
    }
    int top;
    int targetState;
    if (lastNestedScrollDy > 0) {
      top = mMinOffset;
      targetState = STATE_EXPANDED;
    } else {
      top = mMaxOffset;
      targetState = STATE_COLLAPSED;
    }
    if (mViewDragHelper.smoothSlideViewTo(child, child.getLeft(), top)) {
      setStateInternal(STATE_SETTLING);
      ViewCompat.postOnAnimation(child, new SettleRunnable(child, targetState));
    } else {
      setStateInternal(targetState);
    }
    nestedScrolled = false;
  }

  @Override
  public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                @NonNull V child,
                                @NonNull View target,
                                int dx,
                                int dy,
                                @NonNull int[] consumed,
                                int type) {
    int currentTop = child.getTop();
    int newTop = currentTop - dy;
    if (dy > 0) {//scrolling in up direction
      if (newTop < mMinOffset) {
        //consume the difference
        consumed[1] = currentTop - mMinOffset;
        //expand the view
        ViewCompat.offsetTopAndBottom(child, -consumed[1]);
        setStateInternal(STATE_EXPANDED);
      } else {
        consumed[1] = dy;
        ViewCompat.offsetTopAndBottom(child, -consumed[1]);
        setStateInternal(STATE_DRAGGING);
      }
    } else {//scrolling down
      //-1 means scrolling to up
      //If scrolling child finished scrolling to top.
      // Or scrolling target has hit its top and can't scroll further in same direction.
      if (!target.canScrollVertically(-1)) {
        if (newTop > mMaxOffset) {
          //collapse the view
          consumed[1] = currentTop - mMaxOffset;
          ViewCompat.offsetTopAndBottom(child, -consumed[1]);
          setStateInternal(STATE_COLLAPSED);
        } else {
          consumed[1] = dy;
          ViewCompat.offsetTopAndBottom(child, -consumed[1]);
          setStateInternal(STATE_DRAGGING);
        }
      }
    }
    lastNestedScrollDy = dy;
    nestedScrolled = true;
  }

  @Override
  public boolean onNestedPreFling(
   @NonNull CoordinatorLayout coordinatorLayout,
   @NonNull V child,
   @NonNull View target,
   float velocityX,
   float velocityY) {
    if (nestedScrollingChildRef != null) {
      return target == nestedScrollingChildRef.get()
       && (mState != STATE_EXPANDED
       || super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY));
    } else {
      return false;
    }
  }

  private void setStateInternal(@State int state) {
    if (mState == state)
      return;
    mState = state;
  }

  private View findScrollingChild(View view) {
    if (ViewCompat.isNestedScrollingEnabled(view)) {
      return view;
    }
    if (view instanceof ViewGroup) {
      ViewGroup group = (ViewGroup) view;
      for (int i = 0, count = group.getChildCount(); i < count; i++) {
        View scrollingChild = findScrollingChild(group.getChildAt(i));
        if (scrollingChild != null) {
          return scrollingChild;
        }
      }
    }
    return null;
  }

  @IntDef({STATE_COLLAPSED, STATE_DRAGGING, STATE_EXPANDED, STATE_SETTLING})
  @Retention(RetentionPolicy.SOURCE)
  public @interface State {
  }

  public class SettleRunnable implements Runnable {
    View child;
    int targetState;

    public SettleRunnable(View child, int targetState) {
      this.child = child;
      this.targetState = targetState;
    }

    @Override
    public void run() {
      if (mViewDragHelper != null && mViewDragHelper.continueSettling(true)) {
        child.postOnAnimation(this);
      } else {
        if (mState == STATE_SETTLING) {
          setStateInternal(targetState);
        }
      }
    }
  }
}