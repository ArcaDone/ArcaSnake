package com.arcadan.arcasnake.snake

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import com.arcadan.arcasnake.R
import kotlin.math.floor

open class TileView : View {
    private val mPaint = Paint()

    private lateinit var mTileArray: Array<Bitmap?>

    private lateinit var mTileGrid: Array<IntArray>

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.TileView)
        mTileSize = a.getDimensionPixelSize(R.styleable.TileView_tileSize, 12)
        a.recycle()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.TileView)
        mTileSize = a.getDimensionPixelSize(R.styleable.TileView_tileSize, 12)
        a.recycle()
    }

    fun clearTiles() {
        for (x in 0 until mXTileCount) {
            for (y in 0 until mYTileCount) {
                setTile(0, x, y)
            }
        }
    }

    fun loadTile(key: Int, tile: Drawable) {
        val bitmap = Bitmap.createBitmap(mTileSize, mTileSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        tile.setBounds(0, 0, mTileSize, mTileSize)
        tile.draw(canvas)
        mTileArray[key] = bitmap
    }

    fun loadTileApple(key: Int, tile: Drawable) {
        val bitmap = Bitmap.createBitmap(mTileSize, mTileSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        tile.setBounds(20, 20, mTileSize, mTileSize)
        tile.draw(canvas)
        mTileArray[key] = bitmap
    }

    public override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var x = 0
        while (x < mXTileCount) {
            var y = 0
            while (y < mYTileCount) {
                if (mTileGrid[x][y] > 0) {
                    canvas.drawBitmap(mTileArray[mTileGrid[x][y]]!!, mXOffset + x * mTileSize.toFloat(),
                            mYOffset + y * mTileSize.toFloat(), mPaint)
                }
                y += 1
            }
            x += 1
        }
    }

    fun resetTiles(tileCount: Int) {
        mTileArray = arrayOfNulls(tileCount)
    }

    fun setTile(tileIndex: Int, x: Int, y: Int) {
        mTileGrid[x][y] = tileIndex
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mXTileCount = floor(w / mTileSize.toDouble()).toInt()
        mYTileCount = floor(h / mTileSize.toDouble()).toInt()
        mXOffset = (w - mTileSize * mXTileCount) / 2
        mYOffset = (h - mTileSize * mYTileCount) / 2
        mTileGrid = Array(mXTileCount) { IntArray(mYTileCount) }
        clearTiles()
    }

    companion object {
        protected var mTileSize: Int = 0

        @JvmField
        var mXTileCount = 0

        @JvmField
        var mYTileCount = 0
        var mXOffset = 0
        var mYOffset = 0
    }
}