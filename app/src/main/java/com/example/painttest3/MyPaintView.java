package com.example.painttest3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Objects;

public class MyPaintView extends View {
    public static final float TOUCH_TOLERANCE = 10;
    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private Paint paintScreen;
    private Paint paintBrush;
    private HashMap<Integer, Path> hashMapPath;
    private HashMap<Integer, Point> hashMapPreviousPoint;
    private MaskFilter maskFilterEmboss;
    private MaskFilter maskFilterBlur;
    private int numberOfBrushStyleType;

    public void setBitmapCanvas(Canvas bitmapCanvas) {
        this.bitmapCanvas = bitmapCanvas;
    }

    public int getNumberOfBrushStyleType() {
        return numberOfBrushStyleType;
    }

    public MyPaintView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public static Bitmap getResizeBitmap(String path) {
        BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
        bitmapFactoryOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(path, bitmapFactoryOptions);
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public Canvas getBitmapCanvas() {
        return bitmapCanvas;
    }

    public void setBrushStyleNormal() {
        paintBrush.setMaskFilter(null);
        numberOfBrushStyleType = 0;
    }

    public void setBrushStyleEmboss() {
        paintBrush.setMaskFilter(maskFilterEmboss);
        numberOfBrushStyleType = 1;
    }

    public void setBrushStyleBlur() {
        paintBrush.setMaskFilter(maskFilterBlur);
        numberOfBrushStyleType = 2;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        bitmap.eraseColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, paintScreen);

        for (Integer key : hashMapPath.keySet()) {
            canvas.drawPath(Objects.requireNonNull(hashMapPath.get(key)), paintBrush);
        }
    }

    private void initialize() {
        paintScreen = new Paint();
        paintBrush = new Paint();
        paintBrush.setAntiAlias(true);
        paintBrush.setColor(Color.BLACK);
        paintBrush.setStyle(Paint.Style.STROKE);
        paintBrush.setStrokeWidth(5);
        paintBrush.setStrokeCap(Paint.Cap.ROUND);

        hashMapPath = new HashMap<>();
        hashMapPreviousPoint = new HashMap<>();

        maskFilterEmboss = new EmbossMaskFilter(new float[]{1, 1, 1}, 0.5f, 5, 5f);
        maskFilterBlur = new BlurMaskFilter(5, BlurMaskFilter.Blur.NORMAL);

        setBrushStyleNormal();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int motionEventActionIndex = motionEvent.getActionIndex();
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStarted(
                        motionEvent.getX(motionEventActionIndex),
                        motionEvent.getY(motionEventActionIndex),
                        motionEvent.getPointerId(motionEventActionIndex));
                break;
            case MotionEvent.ACTION_UP:
                touchEnded(motionEvent.getPointerId(motionEventActionIndex));
                break;
            case MotionEvent.ACTION_MOVE:
                touchMoved(motionEvent);
                break;
            default:
                break;
        }
        invalidate();
        return true;
    }

    public void setDrawingColor(int color) {
        paintBrush.setColor(color);
    }

    public int getDrawingColor() {
        return paintBrush.getColor();
    }

    public void setBrushSize(int brushSize) {
        paintBrush.setStrokeWidth(brushSize);
    }

    public int getBrushSize() {
        return (int) paintBrush.getStrokeWidth();
    }

    private void touchMoved(MotionEvent event) {
        for (int i = 0; i < event.getPointerCount(); i++) {
            int pointerId = event.getPointerId(i);
            int pointerIndex = event.findPointerIndex(pointerId);

            if (hashMapPath.containsKey(pointerId)) {
                float newX = event.getX(pointerIndex);
                float newY = event.getY(pointerIndex);

                Path path = hashMapPath.get(pointerId);
                Point point = hashMapPreviousPoint.get(pointerId);

                assert point != null;
                float deltaX = Math.abs(newX - point.x);
                float deltaY = Math.abs(newY - point.y);

                if (deltaX >= TOUCH_TOLERANCE || deltaY >= TOUCH_TOLERANCE) {
                    assert path != null;
                    path.quadTo(point.x, point.y, (newX + point.x) / 2, (newY + point.y) / 2);

                    point.x = (int) newX;
                    point.y = (int) newY;
                }
            }
        }
    }

    public void clearCanvas() {
        hashMapPath.clear();
        hashMapPreviousPoint.clear();
        bitmap.eraseColor(Color.WHITE);
        invalidate();
    }

    private void touchEnded(int pointerId) {
        Path path = hashMapPath.get(pointerId);
        if (path != null) {
            bitmapCanvas.drawPath(path, paintBrush);
            path.reset();
        }
    }

    private void touchStarted(float x, float y, int pointerId) {
        Path path;
        Point point;

        if (hashMapPath.containsKey(pointerId)) {
            path = hashMapPath.get(pointerId);
            point = hashMapPreviousPoint.get(pointerId);
        } else {
            path = new Path();
            hashMapPath.put(pointerId, path);
            point = new Point();
            hashMapPreviousPoint.put(pointerId, point);
        }

        assert path != null;
        path.moveTo(x, y);

        assert point != null;
        point.x = (int) x;
        point.y = (int) y;
    }
}
