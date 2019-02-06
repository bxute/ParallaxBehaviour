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
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.bxute.custombottomsheet.ParallaxCalendarView.ROW_HEIGHT_DP;

public class BottomSheetBehaviour<V extends View> extends CoordinatorLayout.Behavior<V> {
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
  private ViewDragHelper.Callback mViewDragHelperCallbacks = new ViewDragHelper.Callback() {

    @Override
    public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
      //settle
      int top;
      int targetState;
      if (yvel < 0) {
        //scrolling up
        top = mMinOffset;
        targetState = STATE_EXPANDED;
      } else {
        top = mMaxOffset;
        targetState = STATE_COLLAPSED;
      }
      setStateInternal(STATE_SETTLING);
      if (mViewDragHelper.settleCapturedViewAt(releasedChild.getLeft(), top)) {
        ViewCompat.postOnAnimation(releasedChild, new SettleRunnable(releasedChild, targetState));
      }
    }

    @Override
    public boolean tryCaptureView(@NonNull View view, int i) {
      if (view.getId() == R.id.bottom_sheet) {
        return true;
      }
      return false;
    }

    @Override
    public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
      return child.getLeft();
    }

    @Override
    public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
      return constrainedVerticalPositionFor(top, mMinOffset, mMaxOffset);
    }

    private int constrainedVerticalPositionFor(int top, int mMinOffset, int mMaxOffset) {
      return Math.min(mMinOffset, Math.min(top, mMaxOffset));
    }
  };

  private boolean mIgnoreEvents;
  private ScrollEventListener scrollEventListener;

  public BottomSheetBehaviour() {
  }

  public BottomSheetBehaviour(Context context, AttributeSet attrs) {
    super(context, attrs);
    //mRowHeight = calculateRowHeight(context);
  }

  @Override
  public boolean onInterceptTouchEvent(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent ev) {
    int action = ev.getActionMasked();
    switch (action) {
      case MotionEvent.ACTION_CANCEL:
      case MotionEvent.ACTION_UP:
        if (mIgnoreEvents) {
          mIgnoreEvents = false;
          return false;
        }
        break;
      case MotionEvent.ACTION_DOWN:
        mIgnoreEvents = !parent.isPointInChildBounds(child, (int) ev.getX(), (int) ev.getY());
        break;
    }
    return !mIgnoreEvents && mViewDragHelper.shouldInterceptTouchEvent(ev);
  }

  @Override
  public boolean onTouchEvent(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent ev) {
    if (mViewDragHelper == null)
      return false;
    mViewDragHelper.processTouchEvent(ev);
    return true;
  }

  @Override
  public boolean onLayoutChild(@NonNull CoordinatorLayout parent, @NonNull V child, int layoutDirection) {
    //let the parent layout its child
    parent.onLayoutChild(child, layoutDirection);
    mParentHeight = parent.getHeight();
    if (mRowHeight == 0) {
      mRowHeight = calculateRowHeight(parent.getContext());
      mMinOffset = mRowHeight;
      mMaxOffset = 5 * mRowHeight;
    }

    if (mState == STATE_EXPANDED) {
      ViewCompat.offsetTopAndBottom(child, mMinOffset);
    } else if (mState == STATE_COLLAPSED) {
      ViewCompat.offsetTopAndBottom(child, mMaxOffset);
    }

    if (mViewDragHelper == null) {
      mViewDragHelper = ViewDragHelper.create(parent, mViewDragHelperCallbacks);
    }

    return true;
  }

  private int calculateRowHeight(Context context) {
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
     ROW_HEIGHT_DP,
     context.getResources().getDisplayMetrics());
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
    return (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
  }

  @Override
  public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                 @NonNull V child,
                                 @NonNull View target,
                                 int type) {

    //if user stops scrollig at any point of time,then take appropriate action

    //If there is no nested scroll or child's top is at Min position, simply return
    if (mLastNestedScroll == 0 || child.getTop() == mMinOffset) {
      return;
    }
    int top;
    int targetState;
    //if scrolled in up direction

    if (mLastNestedScroll > 0) {
      //expand the view
      top = mMinOffset;
      targetState = STATE_EXPANDED;
    } else {
      top = mMaxOffset;
      targetState = STATE_COLLAPSED;
    }
    setStateInternal(STATE_SETTLING);
    //with the help of ViewDragHelper, smoothSlide the view to configured position
    //We need to call continueSetting(true) while it returns true.
    // When it returns false, means there is no work left regarding to frame update.
    if (mViewDragHelper.smoothSlideViewTo(child, child.getLeft(), top)) {
      ViewCompat.postOnAnimation(child, new SettleRunnable(child, targetState));
    }
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
    mLastNestedScroll = dy;
  }

  private void setStateInternal(@State int state) {
    if (mState == state)
      return;
    mState = state;
  }

  @IntDef({STATE_COLLAPSED, STATE_DRAGGING, STATE_EXPANDED, STATE_SETTLING})
  @Retention(RetentionPolicy.SOURCE)
  public @interface State {
  }

  public interface ScrollEventListener {
    void onScrolledByRation(float ratio);
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
        setStateInternal(targetState);
      }
    }
  }
}