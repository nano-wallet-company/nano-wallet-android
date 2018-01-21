package co.nano.nanowallet.ui.scan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import co.nano.nanowallet.R;
import co.nano.nanowallet.ui.common.UIUtil;
import me.dm7.barcodescanner.core.DisplayUtils;
import me.dm7.barcodescanner.core.IViewFinder;

public class NanoViewFinderView extends View implements IViewFinder {
    private static final String TAG = "ViewFinderView";
    private Rect mFramingRect;
    private RectF mFramingRectF;
    private static final float PORTRAIT_WIDTH_RATIO = 0.75F;
    private static final float PORTRAIT_WIDTH_HEIGHT_RATIO = 0.75F;
    private static final float LANDSCAPE_HEIGHT_RATIO = 0.625F;
    private static final float LANDSCAPE_WIDTH_HEIGHT_RATIO = 1.4F;
    private static final int MIN_DIMENSION_DIFF = 50;
    private static final float SQUARE_DIMENSION_RATIO = 0.625F;
    private static final int[] SCANNER_ALPHA = new int[]{0, 64, 128, 192, 255, 192, 128, 64};
    private int scannerAlpha;
    private static final int POINT_SIZE = 10;
    private static final long ANIMATION_DELAY = 80L;
    private final int mDefaultMaskColor;
    private final int mDefaultBorderColor;
    private final int mDefaultBorderStrokeWidth;
    private final int mDefaultBorderLineLength;
    private final float mDefaultRoundedRadius;
    protected Paint mFinderMaskPaint;
    protected Paint mBorderPaint;
    protected int mBorderLineLength;
    protected boolean mSquareViewFinder = true;

    public NanoViewFinderView(Context context) {
        super(context);
        this.mDefaultMaskColor = this.getResources().getColor(R.color.semitranslucent_black);
        this.mDefaultBorderColor = this.getResources().getColor(R.color.bright_white);
        this.mDefaultBorderStrokeWidth = this.getResources().getInteger(me.dm7.barcodescanner.core.R.integer.viewfinder_border_width);
        this.mDefaultBorderLineLength = this.getResources().getInteger(me.dm7.barcodescanner.core.R.integer.viewfinder_border_length);
        this.mDefaultRoundedRadius = UIUtil.convertDpToPixel(14f, context);
        this.init();
    }

    public NanoViewFinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mDefaultMaskColor = this.getResources().getColor(R.color.semitranslucent_black);
        this.mDefaultBorderColor = this.getResources().getColor(R.color.bright_white);
        this.mDefaultBorderStrokeWidth = this.getResources().getInteger(me.dm7.barcodescanner.core.R.integer.viewfinder_border_width);
        this.mDefaultBorderLineLength = this.getResources().getInteger(me.dm7.barcodescanner.core.R.integer.viewfinder_border_length);
        this.mDefaultRoundedRadius = UIUtil.convertDpToPixel(14f, context);
        this.init();
    }

    private void init() {
        this.mFinderMaskPaint = new Paint();
        this.mFinderMaskPaint.setColor(this.mDefaultMaskColor);
        this.mBorderPaint = new Paint();
        this.mBorderPaint.setColor(this.mDefaultBorderColor);
        this.mBorderPaint.setStyle(Paint.Style.STROKE);
        this.mBorderPaint.setStrokeWidth((float) this.mDefaultBorderStrokeWidth);
        this.mBorderLineLength = this.mDefaultBorderLineLength;
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void setMaskColor(int maskColor) {
        this.mFinderMaskPaint.setColor(maskColor);
    }

    public void setBorderColor(int borderColor) {
        this.mBorderPaint.setColor(borderColor);
    }

    public void setBorderStrokeWidth(int borderStrokeWidth) {
        this.mBorderPaint.setStrokeWidth((float) borderStrokeWidth);
    }

    public void setBorderLineLength(int borderLineLength) {
        this.mBorderLineLength = borderLineLength;
    }

    public void setSquareViewFinder(boolean set) {
        this.mSquareViewFinder = set;
    }

    public void setupViewFinder() {
        this.updateFramingRect();
        this.invalidate();
    }

    public Rect getFramingRect() {
        return new Rect((int) mFramingRectF.left,
                (int) mFramingRectF.top,
                (int) mFramingRectF.right,
                (int) mFramingRectF.bottom);
    }

    public RectF getFramingRectF() {
        return this.mFramingRectF;
    }

    public void onDraw(Canvas canvas) {
        if (this.getFramingRect() != null) {
            this.drawViewFinderMask(canvas);
            this.drawViewFinderBorder(canvas);
        }
    }

    public void drawViewFinderMask(Canvas canvas) {
        RectF framingRect = this.getFramingRectF();
        Paint paint = this.mFinderMaskPaint;

        // background
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);

        // hole
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawRoundRect(framingRect, this.mDefaultRoundedRadius, this.mDefaultRoundedRadius, paint);
    }

    public void drawViewFinderBorder(Canvas canvas) {
        RectF framingRect = this.getFramingRectF();
        canvas.drawRoundRect(framingRect, this.mDefaultRoundedRadius, this.mDefaultRoundedRadius, this.mBorderPaint);
    }

    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        this.updateFramingRect();
    }

    public synchronized void updateFramingRect() {
        Point viewResolution = new Point(this.getWidth(), this.getHeight());
        int orientation = DisplayUtils.getScreenOrientation(this.getContext());
        int width;
        int height;
        if (this.mSquareViewFinder) {
            if (orientation != 1) {
                height = (int) ((float) this.getHeight() * 0.625F);
                width = height;
            } else {
                width = (int) ((float) this.getWidth() * 0.625F);
                height = width;
            }
        } else if (orientation != 1) {
            height = (int) ((float) this.getHeight() * 0.625F);
            width = (int) (1.4F * (float) height);
        } else {
            width = (int) ((float) this.getWidth() * 0.75F);
            height = (int) (0.75F * (float) width);
        }

        if (width > this.getWidth()) {
            width = this.getWidth() - 50;
        }

        if (height > this.getHeight()) {
            height = this.getHeight() - 50;
        }

        int leftOffset = (viewResolution.x - width) / 2;
        int topOffset = (viewResolution.y - height) / 2;
        this.mFramingRectF = new RectF(leftOffset, topOffset, leftOffset + width, topOffset + height);
    }
}