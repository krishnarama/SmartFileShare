package com.android.BluetoothFTP;

import java.lang.ref.WeakReference;

import android.graphics.Canvas;
import android.graphics.Point;
import android.view.View;

public class DragShadowBuilder {
    private final WeakReference<View> mView;

    /**
     * Constructs a shadow image builder based on a View. By default, the resulting drag
     * shadow will have the same appearance and dimensions as the View, with the touch point
     * over the center of the View.
     * @param view A View. Any View in scope can be used.
     */
    public DragShadowBuilder(View view) {
        mView = new WeakReference<View>(view);
    }

    /**
     * Construct a shadow builder object with no associated View.  This
     * constructor variant is only useful when the {@link #onProvideShadowMetrics(Point, Point)}
     * and {@link #onDrawShadow(Canvas)} methods are also overridden in order
     * to supply the drag shadow's dimensions and appearance without
     * reference to any View object. If they are not overridden, then the result is an
     * invisible drag shadow.
     */
    public DragShadowBuilder() {
        mView = new WeakReference<View>(null);
    }

    /**
     * Returns the View object that had been passed to the
     * {@link #View.DragShadowBuilder(View)}
     * constructor.  If that View parameter was {@code null} or if the
     * {@link #View.DragShadowBuilder()}
     * constructor was used to instantiate the builder object, this method will return
     * null.
     *
     * @return The View object associate with this builder object.
     */
    @SuppressWarnings({"JavadocReference"})
    final public View getView() {
        return mView.get();
    }

    /**
     * Provides the metrics for the shadow image. These include the dimensions of
     * the shadow image, and the point within that shadow that should
     * be centered under the touch location while dragging.
     * <p>
     * The default implementation sets the dimensions of the shadow to be the
     * same as the dimensions of the View itself and centers the shadow under
     * the touch point.
     * </p>
     *
     * @param shadowSize A {@link android.graphics.Point} containing the width and height
     * of the shadow image. Your application must set {@link android.graphics.Point#x} to the
     * desired width and must set {@link android.graphics.Point#y} to the desired height of the
     * image.
     *
     * @param shadowTouchPoint A {@link android.graphics.Point} for the position within the
     * shadow image that should be underneath the touch point during the drag and drop
     * operation. Your application must set {@link android.graphics.Point#x} to the
     * X coordinate and {@link android.graphics.Point#y} to the Y coordinate of this position.
     */
    public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
        final View view = mView.get();
        if (view != null) {
            shadowSize.set(view.getWidth(), view.getHeight());
            shadowTouchPoint.set(shadowSize.x / 2, shadowSize.y / 2);
        } else {
            //Log.e(View.VIEW_LOG_TAG, "Asked for drag thumb metrics but no view");
        }
    }

    /**
     * Draws the shadow image. The system creates the {@link android.graphics.Canvas} object
     * based on the dimensions it received from the
     * {@link #onProvideShadowMetrics(Point, Point)} callback.
     *
     * @param canvas A {@link android.graphics.Canvas} object in which to draw the shadow image.
     */
    public void onDrawShadow(Canvas canvas) {
        final View view = mView.get();
        if (view != null) {
            view.draw(canvas);
        } else {
            //Log.e(View.VIEW_LOG_TAG, "Asked to draw drag shadow but no view");
        }
    }
}
