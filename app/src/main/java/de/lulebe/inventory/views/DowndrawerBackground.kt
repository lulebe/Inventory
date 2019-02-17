package de.lulebe.inventory.views

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.v4.content.res.ResourcesCompat
import android.util.TypedValue
import de.lulebe.inventory.R

class DowndrawerBackground(ctx: Context) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pathMain = Path()
    private val pathMiddle = Path()
    private var leftSideOffset = 0F
    private var pathMiddleWidth = 0F
    private var bottomOffset = 0F
    private var buttonOffset = 0F

    init {
        paint.color = ResourcesCompat.getColor(ctx.resources, R.color.colorPrimary, null)
        leftSideOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24F, ctx.resources.displayMetrics)
        pathMiddleWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 79.5F, ctx.resources.displayMetrics)
        bottomOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16F, ctx.resources.displayMetrics)
        buttonOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40F, ctx.resources.displayMetrics)
    }

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)
        bounds?.let {
            pathMain.apply {
                reset()
                moveTo(0.0F, 0.0F)
                lineTo(it.width().toFloat(), 0.0F)
                lineTo(it.width().toFloat(), it.height().toFloat() - bottomOffset - buttonOffset)
                lineTo(0.0F, it.height().toFloat() - leftSideOffset - bottomOffset - buttonOffset)
                close()
            }
            pathMiddle.apply {
                reset()
                moveTo((it.width() / 2F - (pathMiddleWidth/2F)), it.height().toFloat() - leftSideOffset - bottomOffset - buttonOffset)
                lineTo((it.width() / 2F + (pathMiddleWidth/2F)), it.height().toFloat() - leftSideOffset - bottomOffset - buttonOffset)
                lineTo((it.width() / 2F + (pathMiddleWidth/2F)), it.height().toFloat() - buttonOffset)
                lineTo((it.width() / 2F - (pathMiddleWidth/2F)), it.height().toFloat() - buttonOffset)
                close()
            }
        }
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPath(pathMain, paint)
        canvas.drawPath(pathMiddle, paint)
    }

    override fun setAlpha(alpha: Int) {}

    override fun getOpacity(): Int {
        return PixelFormat.TRANSPARENT
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {}
}