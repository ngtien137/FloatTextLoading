package com.luza.floattextloading

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import kotlin.math.roundToInt

class FloatTextLoading @JvmOverloads constructor(
    context: Context, var attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var viewWidth = 0
    private var viewHeight = 0
    private var numberLoadingItem = 10
    private var itemWidth = 0
    private var itemSpacing = 0

    var progress = 0
        private set
    var max = 100
        private set

    private var progressPerItem = max / numberLoadingItem

    /**
     * Progress
     */
    private var rectProgress = RectF()
    private val paintProgress = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    }

    /**
     * Text Progress
     */
    private var progressTextFormat = "%s"
    private val paintProgressText = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    }
    private var progressTextBottom = 0
    private val rectTextProgress = Rect()

    /**
     * Indicator Progress
     */
    private var indicator: Drawable? = null
    private var indicatorWidth = 0
    private var indicatorHeight = 0
    private var indicatorBottom = 0
    private val rectIndicator = Rect()

    /**
     * Background
     */
    private var loadingBarHeight = 0
    private val paintBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
    }
    private val rectBackground = RectF()

    /**
     * Border
     */
    private val DEF_BORDER_COLOR = Color.parseColor("#44ffffff")
    private val paintBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = DEF_BORDER_COLOR
        style = Paint.Style.STROKE
    }
    private val rectBorder = RectF()
    private var borderCorner = 0f

    init {
        attrs?.let {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.FloatTextLoading)
            numberLoadingItem = ta.getInt(R.styleable.FloatTextLoading_ftl_progress_item_count, 10)
            itemSpacing =
                ta.getDimensionPixelSize(R.styleable.FloatTextLoading_ftl_progress_item_spacing, 0)

            //Text
            progressTextFormat =
                ta.getString(R.styleable.FloatTextLoading_ftl_progress_text_format) ?: "%s"
            paintProgressText.color =
                ta.getColor(R.styleable.FloatTextLoading_ftl_progress_text_color, Color.WHITE)
            paintProgressText.textSize =
                ta.getDimensionPixelSize(R.styleable.FloatTextLoading_ftl_progress_text_size, 0)
                    .toFloat()
            progressTextBottom =
                ta.getDimensionPixelSize(R.styleable.FloatTextLoading_ftl_progress_text_bottom, 0)

            //Progress
            paintProgress.color =
                ta.getColor(R.styleable.FloatTextLoading_ftl_progress_color, Color.WHITE)

            //Indicator size
            indicator = ta.getDrawable(R.styleable.FloatTextLoading_ftl_progress_indicator_src)
            indicatorWidth = ta.getDimensionPixelSize(
                R.styleable.FloatTextLoading_ftl_progress_indicator_width,
                0
            )
            indicatorHeight = ta.getDimensionPixelSize(
                R.styleable.FloatTextLoading_ftl_progress_indicator_height,
                0
            )
            indicatorBottom = ta.getDimensionPixelSize(
                R.styleable.FloatTextLoading_ftl_progress_indicator_bottom,
                0
            )

            //Background
            loadingBarHeight =
                ta.getDimensionPixelSize(R.styleable.FloatTextLoading_ftl_loading_bar_height, 0)
            paintBackground.color =
                ta.getColor(R.styleable.FloatTextLoading_ftl_background_color, Color.BLACK)

            borderCorner =
                ta.getDimensionPixelSize(R.styleable.FloatTextLoading_ftl_border_corner, 0)
                    .toFloat()
            paintBorder.strokeWidth =
                ta.getDimensionPixelSize(R.styleable.FloatTextLoading_ftl_border_size, 0).toFloat()
            paintBorder.color =
                ta.getColor(R.styleable.FloatTextLoading_ftl_border_color, DEF_BORDER_COLOR)

            progress = ta.getInt(R.styleable.FloatTextLoading_ftl_progress, 0)
            max = ta.getInt(R.styleable.FloatTextLoading_ftl_max, 100)

            ta.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        viewWidth = widthSize
        viewHeight = heightSize
        log("Height mode: ${MeasureSpec.getMode(heightMeasureSpec)==MeasureSpec.UNSPECIFIED}, ${MeasureSpec.getMode(heightMeasureSpec)==MeasureSpec.AT_MOST}")
        rectBackground.set(0f, viewHeight - loadingBarHeight, viewWidth, viewHeight)
        rectBorder.set(
            0f + paintBorder.strokeWidth / 2,
            viewHeight - loadingBarHeight + paintBorder.strokeWidth / 2,
            viewWidth - paintBorder.strokeWidth / 2,
            viewHeight - paintBorder.strokeWidth / 2
        )
        calculateItem()
        setMeasuredDimension(viewWidth,viewHeight)
    }

    private fun calculateItem() {
        progressPerItem = max / numberLoadingItem
        itemWidth =
            ((viewWidth - paddingLeft - paddingRight - itemSpacing * (numberLoadingItem - 1) - paintBorder.strokeWidth * 2) / numberLoadingItem).roundToInt()
    }

    fun setProgress(progress: Int) {
        var p = progress
        if (p > max)
            p = max
        else if (p < 0)
            p = 0
        this.progress = p
        invalidate()
    }

    fun setMax(max: Int) {
        this.max = max
        if (max < 0)
            this.max = 100
        progress = 0
        calculateItem()
        invalidate()
    }

    private fun RectF.set(left: Number, top: Number, right: Number, bottom: Number) {
        this.set(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
    }

    private fun Rect.set(left: Number, top: Number, right: Number, bottom: Number) {
        this.set(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let { canvas ->
            canvas.drawRoundRect(rectBackground, borderCorner, borderCorner, paintBackground)
            canvas.drawRoundRect(rectBorder, borderCorner, borderCorner, paintBorder)
            drawProgress(canvas)
            drawIndicatorProgress(canvas)
            drawTextProgress(canvas)
        }
    }

    private fun drawProgress(canvas: Canvas) {
        var tempProgress = progress
        val itemStart = paddingStart + paintBorder.strokeWidth
        val itemTop = viewHeight - loadingBarHeight + paddingTop + paintBorder.strokeWidth
        val itemBottom = viewHeight - paddingBottom - paintBorder.strokeWidth
        rectProgress.set(itemStart, itemTop, itemStart + itemWidth, itemBottom)
        do {
            if (tempProgress > progressPerItem) {
                canvas.drawRect(rectProgress, paintProgress)
                rectProgress.left += itemWidth + itemSpacing
                rectProgress.right = rectProgress.left + itemWidth
                tempProgress -= progressPerItem
            } else {
                val width = tempProgress.toDouble() / progressPerItem * itemWidth
                rectProgress.right = (rectProgress.left + width).toFloat()
                canvas.drawRect(rectProgress, paintProgress)
                tempProgress = 0
            }
            if (tempProgress == 0)
                break
        } while (true)
    }

    private fun drawIndicatorProgress(canvas: Canvas) {
        val centerX = rectProgress.centerX()
        rectIndicator.set(
            centerX - indicatorWidth / 2,
            rectBackground.top - indicatorBottom - indicatorHeight,
            centerX + indicatorWidth / 2,
            rectBackground.top - indicatorBottom
        )
        indicator?.let {
            it.bounds.set(rectIndicator)
            it.draw(canvas)
        }
    }

    private fun drawTextProgress(canvas: Canvas) {
        val centerX = rectProgress.centerX()
        val sProgress = String.format(progressTextFormat, progress.toString())
        val xText = when {
            centerX-rectTextProgress.width()/2<0 -> {
                0f
            }
            centerX+rectTextProgress.width()/2>viewWidth -> viewWidth-rectTextProgress.width().toFloat()
            else -> centerX-rectTextProgress.width()/2f
        }
        paintProgressText.getTextBounds(sProgress, 0, sProgress.length, rectTextProgress)
        canvas.drawText(
            sProgress,
            xText,
            rectIndicator.top - progressTextBottom - rectTextProgress.height() / 2f,
            paintProgressText
        )
    }
}