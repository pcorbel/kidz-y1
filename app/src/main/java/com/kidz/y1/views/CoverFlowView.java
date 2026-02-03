package com.kidz.y1.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.Scroller;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.kidz.y1.utils.Constants;
import com.kidz.y1.utils.GlideOptionsCache;
import com.kidz.y1.utils.ImageHelper;
import com.kidz.y1.utils.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CoverFlowView extends ViewGroup {
    private List<CoverItem> covers = new ArrayList<>();
    private int selectedIndex = 0;
    private int coverSize;
    private int spacing;
    private Scroller scroller;
    private Interpolator interpolator = new DecelerateInterpolator();
    private OnCoverSelectedListener listener;
    private float scrollOffset = 0f;
    private boolean isScrolling = false;
    private float verticalOffset = 0f;
    private ImageHelper.ImageType imageType;
    private int cornerRadius;
    private RequestOptions cachedRequestOptions;
    private boolean requestOptionsInitialized = false;

    public interface OnCoverSelectedListener {
        void onCoverSelected(int index);
    }

    private static class CoverItem {
        ImageView imageView;
        File dataSource;
        String imagePath;
    }

    public CoverFlowView(Context context) {
        super(context);
        init();
    }

    public CoverFlowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CoverFlowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setFocusable(true);
        scroller = new Scroller(getContext(), interpolator);
        cornerRadius = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                Constants.CORNER_RADIUS_DP,
                getContext().getResources().getDisplayMetrics());
    }

    public void setOnCoverSelectedListener(OnCoverSelectedListener listener) {
        this.listener = listener;
    }

    public void setCoversFromSources(List<File> dataSources, ImageHelper.ImageType imageType, int coverSize) {
        for (CoverItem item : covers) {
            if (item.imageView != null) {
                Glide.with(getContext()).clear(item.imageView);
            }
        }
        removeAllViews();
        covers.clear();
        this.imageType = imageType;
        this.coverSize = coverSize;
        this.spacing = (int) (coverSize * Constants.COVER_SPACING_RATIO);
        requestOptionsInitialized = false;

        if (dataSources == null || dataSources.isEmpty()) {
            return;
        }

        for (File dataSource : dataSources) {
            CoverItem item = new CoverItem();
            item.dataSource = dataSource;
            item.imagePath = ImageHelper.findImagePath(dataSource, imageType);
            ImageView imageView = new ImageView(getContext());
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setBackgroundColor(0xFF000000);
            imageView.setImageDrawable(null);
            imageView.setLayoutParams(new LayoutParams(coverSize, coverSize));
            item.imageView = imageView;
            covers.add(item);
            addView(imageView);
        }

        selectedIndex = Math.max(0, Math.min(selectedIndex, covers.size() - 1));
        scrollOffset = selectedIndex;
        requestLayout();
        invalidate();
        loadVisibleCovers();
    }

    private void loadVisibleCovers() {
        // Load covers that are currently visible (center ± 2 for smooth scrolling)
        int centerIndex = Math.round(scrollOffset);
        int startIndex = Math.max(0, centerIndex - 2);
        int endIndex = Math.min(covers.size() - 1, centerIndex + 2);

        for (int i = startIndex; i <= endIndex; i++) {
            loadCover(i);
        }
    }

    private void loadCover(int index) {
        if (index < 0 || index >= covers.size()) {
            return;
        }

        CoverItem item = covers.get(index);
        if (item.imagePath == null) {
            // Load null to trigger error fallback (ic_no_cover)
            RequestOptions options = getRequestOptions();
            Glide.with(getContext())
                    .load((Object) null)
                    .apply(options)
                    .into(item.imageView);
            return;
        }

        RequestOptions options = getRequestOptions();
        
        RequestListener<android.graphics.drawable.Drawable> listener = new RequestListener<android.graphics.drawable.Drawable>() {
            @Override
            public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model, Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                // Image loaded - request layout to ensure it's displayed
                post(() -> requestLayout());
                return false;
            }
        };

        if (!item.imagePath.startsWith(Constants.ID3_PREFIX)) {
            File imageFile = new File(item.imagePath);
            if (imageFile.exists()) {
                Glide.with(getContext())
                        .load(imageFile)
                        .apply(options)
                        .listener(listener)
                        .into(item.imageView);
            } else {
                // File doesn't exist - load null to trigger error fallback (ic_no_cover)
                Glide.with(getContext())
                        .load((Object) null)
                        .apply(options)
                        .listener(listener)
                        .into(item.imageView);
            }
        } else {
            Glide.with(getContext())
                    .load(item.imagePath)
                    .apply(options)
                    .listener(listener)
                    .into(item.imageView);
        }
    }

    private RequestOptions getRequestOptions() {
        if (!requestOptionsInitialized || cachedRequestOptions == null) {
            RequestOptions baseOptions = GlideOptionsCache.getBaseOptions(
                    getContext(), null, coverSize, coverSize);
            
            cachedRequestOptions = baseOptions
                    .transform(new RoundedCorners(cornerRadius));
            
            requestOptionsInitialized = true;
        }
        return cachedRequestOptions;
    }

    public void setSelectedIndexWithoutAnimation(int index) {
        if (covers.isEmpty()) {
            return;
        }
        selectedIndex = Math.max(0, Math.min(index, covers.size() - 1));
        scrollOffset = selectedIndex;
        loadVisibleCovers();
        requestLayout();
        invalidate();
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setCoverSize(int size) {
        this.coverSize = size;
        this.spacing = (int) (size * 0.1f);
        requestOptionsInitialized = false;
        requestLayout();
        invalidate();
    }

    public void setVerticalOffset(float offset) {
        this.verticalOffset = offset;
        requestLayout();
        invalidate();
    }

    private void smoothScrollToIndex(int targetIndex) {
        float targetOffset = targetIndex;
        float startOffset = scrollOffset;
        scroller.startScroll((int) (startOffset * 1000), 0, (int) ((targetOffset - startOffset) * 1000), 0,
                Constants.SCROLL_ANIMATION_DURATION_MS);
        isScrolling = true;
        postInvalidateOnAnimation();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (coverSize == 0) {
            coverSize = Math.min(width / 2, height / 2);
            spacing = (int) (coverSize * 0.1f);
        }
        setMeasuredDimension(width, height);
    }

    /**
     * Layouts cover items in a 3D carousel view.
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (covers.isEmpty() || coverSize == 0) {
            return;
        }
        
        // Load covers when layout changes (scroll offset changed)
        loadVisibleCovers();
        
        int centerX = getWidth() / 2;
        int centerY = (int) (getHeight() / 2 + verticalOffset);

        layoutVisibleCovers(centerX, centerY);
    }

    /**
     * Layouts visible cover items with 3D transformations.
     * @param centerX center X coordinate
     * @param centerY center Y coordinate
     */
    private void layoutVisibleCovers(int centerX, int centerY) {
        for (int i = 0; i < covers.size(); i++) {
            CoverItem item = covers.get(i);
            float position = i - scrollOffset;
            float absPosition = Math.abs(position);

            if (absPosition > Constants.COVER_VISIBILITY_THRESHOLD + 0.5f) {
                item.imageView.setVisibility(View.INVISIBLE);
                continue;
            }

            CoverLayout layout = calculateCoverLayout(position, absPosition, centerX, centerY);
            layoutCoverItem(item, layout);
            applyTransformations(item.imageView, position, layout.scale, absPosition);
        }
    }

    /**
     * Calculates layout parameters for a cover item.
     * @param position relative position from center
     * @param absPosition absolute position
     * @param centerX center X coordinate
     * @param centerY center Y coordinate
     * @return layout parameters
     */
    private CoverLayout calculateCoverLayout(float position, float absPosition, int centerX, int centerY) {
        float scale;
        float x;

        if (absPosition <= 1.0f) {
            scale = 1.0f - (absPosition * (1.0f - Constants.COVER_SCALE_MIN));
            x = centerX + (position * (coverSize + spacing));
        } else {
            scale = Constants.COVER_SCALE_MIN * (1.0f - (absPosition - 1.0f) * 0.5f);
            x = centerX + (position * (coverSize + spacing));
        }

        int scaledSize = (int) (coverSize * scale);
        int left = (int) (x - scaledSize / 2);
        int top = (int) (centerY - scaledSize / 2);

        return new CoverLayout(left, top, scaledSize, scale);
    }

    /**
     * Layouts a single cover item.
     * @param item the cover item to layout
     * @param layout layout parameters
     */
    private void layoutCoverItem(CoverItem item, CoverLayout layout) {
        item.imageView.layout(
                layout.left,
                layout.top,
                layout.left + layout.size,
                layout.top + layout.size
        );
        item.imageView.setVisibility(View.VISIBLE);
    }

    /**
     * Helper class for cover layout parameters.
     */
    private static class CoverLayout {
        final int left;
        final int top;
        final int size;
        final float scale;

        CoverLayout(int left, int top, int size, float scale) {
            this.left = left;
            this.top = top;
            this.size = size;
            this.scale = scale;
        }
    }

    private void applyTransformations(View view, float position, float scale, float absPosition) {
        view.setScaleX(scale);
        view.setScaleY(scale);
        float rotation = position * Constants.COVER_ROTATION_MULTIPLIER * (1.0f - absPosition * 0.3f);
        view.setRotationY(rotation);
        float alpha = 1.0f - (absPosition * 0.5f);
        alpha = Math.max(Constants.COVER_ALPHA_MIN, Math.min(1.0f, alpha));
        view.setAlpha(alpha);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isScrolling && scroller.computeScrollOffset()) {
            float newOffset = scroller.getCurrX() / (float) 1000;
            if (Math.abs(newOffset - scrollOffset) > 0.1f) {
                scrollOffset = newOffset;
                requestLayout();
                postInvalidateOnAnimation();
            } else {
                requestLayout();
                postInvalidateOnAnimation();
            }
        } else if (isScrolling) {
            isScrolling = false;
            scrollOffset = selectedIndex;
            loadVisibleCovers();
            requestLayout();
        }
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollOffset = scroller.getCurrX() / (float) 1000;
            requestLayout();
            invalidate();
        }
    }

    public void scrollToNext() {
        if (covers.isEmpty()) {
            return;
        }
        if (selectedIndex < covers.size() - 1) {
            int newIndex = selectedIndex + 1;
            selectedIndex = newIndex;
            if (listener != null) {
                listener.onCoverSelected(newIndex);
            }
            smoothScrollToIndex(newIndex);
        }
    }

    public void scrollToPrevious() {
        if (covers.isEmpty()) {
            return;
        }
        if (selectedIndex > 0) {
            int newIndex = selectedIndex - 1;
            selectedIndex = newIndex;
            if (listener != null) {
                listener.onCoverSelected(newIndex);
            }
            smoothScrollToIndex(newIndex);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            for (CoverItem item : covers) {
                if (item != null && item.imageView != null) {
                    Glide.with(getContext()).clear(item.imageView);
                }
            }
        } catch (IllegalArgumentException e) {
            Logger.w("CoverFlowView", "Error clearing Glide images", e);
        } catch (Exception e) {
            Logger.w("CoverFlowView", "Unexpected error during cleanup", e);
        }
    }
}
