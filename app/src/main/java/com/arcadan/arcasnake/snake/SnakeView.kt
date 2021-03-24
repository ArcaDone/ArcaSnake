package com.arcadan.arcasnake.snake

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.arcadan.arcasnake.R
import java.util.*

class SnakeView : TileView {

    var gameState = READY
        private set

    private var mDirection = NORTH
    private var mNextDirection = NORTH
    private var mMoveDelay: Long = 0

    private var mLastMove: Long = 0

    private var mStatusText: TextView? = null

    private var mArrowsView: View? = null

    private var mBackgroundView: View? = null

    private var mSnakeTrail = ArrayList<Coordinate?>()

    private var mAppleList: ArrayList<Coordinate?>

    private val mRedrawHandler = RefreshHandler()

    @SuppressLint("HandlerLeak")
    internal inner class RefreshHandler : Handler() {
        override fun handleMessage(msg: Message) {
            update()
            this@SnakeView.invalidate()
        }

        fun sleep(delayMillis: Long) {
            this.removeMessages(0)
            sendMessageDelayed(obtainMessage(0), delayMillis)
        }
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        initSnakeView()
        mAppleList = ArrayList()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context!!, attrs, defStyle) {
        initSnakeView()
        mAppleList = ArrayList()
    }

    private fun initSnakeView() {
        isFocusable = true
        val r = this.context.resources

        resetTiles(8)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            loadTileApple(APPLE, r.getDrawable(R.drawable.pizza, null))
            loadTile(BRICK_STAR, r.getDrawable(R.drawable.brick, null))
            loadTile(PURPLE_STAR, r.getDrawable(R.drawable.purplestar, null))
            loadTile(SNAKE_FACE, r.getDrawable(R.drawable.purplestar, null))
        }
    }

    private fun initNewGame() {
        mSnakeTrail.clear()
        mAppleList.clear()

        mSnakeTrail.add(Coordinate(6, 7))
        mSnakeTrail.add(Coordinate(5, 7))
        mNextDirection = NORTH

        addRandomApple()
        addRandomApple()

        mMoveDelay = 300
        //score
        mScore = 0
        totalScore = 0
    }

    private fun coordArrayListToArray(cvec: ArrayList<Coordinate?>): IntArray {
        val rawArray = IntArray(cvec.size * 2)
        var i = 0
        for (c in cvec) {
            rawArray[i++] = c!!.x
            rawArray[i++] = c.y
        }
        return rawArray
    }

    fun saveState(): Bundle {
        val map = Bundle()
        map.putIntArray("mAppleList", coordArrayListToArray(mAppleList))
        map.putInt("mDirection", mDirection)
        map.putInt("mNextDirection", mNextDirection)
        map.putLong("mMoveDelay", mMoveDelay)
        map.putInt("totalScore", totalScore)
        map.putIntArray("mSnakeTrail", coordArrayListToArray(mSnakeTrail))
        return map
    }

    private fun coordArrayToArrayList(rawArray: IntArray): ArrayList<Coordinate?> {
        val coordArrayList = ArrayList<Coordinate?>()
        val coordCount = rawArray.size
        var index = 0
        while (index < coordCount) {
            val c = Coordinate(rawArray[index], rawArray[index + 1])
            coordArrayList.add(c)
            index += 2
        }
        return coordArrayList
    }

    fun restoreState(icicle: Bundle) {
        setMode(PAUSE)
        mAppleList = coordArrayToArrayList(Objects.requireNonNull(icicle.getIntArray("mAppleList"))!!)
        mDirection = icicle.getInt("mDirection")
        mNextDirection = icicle.getInt("mNextDirection")
        mMoveDelay = icicle.getLong("mMoveDelay")
        totalScore = icicle.getInt("totalScore")
        mSnakeTrail = coordArrayToArrayList(Objects.requireNonNull(icicle.getIntArray("mSnakeTrail"))!!)
    }

    fun moveSnake(direction: Int) {
        if (direction == Snake.MOVE_UP) {
            if (gameState == READY || gameState == LOSE) {

                initNewGame()
                setMode(RUNNING)
                update()
                return
            }
            if (gameState == PAUSE) {

                setMode(RUNNING)
                update()
                return
            }
            if (mDirection != SOUTH) {
                mNextDirection = NORTH
            }
            return
        }
        if (direction == Snake.MOVE_DOWN) {
            if (mDirection != NORTH) {
                mNextDirection = SOUTH
            }
            return
        }
        if (direction == Snake.MOVE_LEFT) {
            if (mDirection != EAST) {
                mNextDirection = WEST
            }
            return
        }
        if (direction == Snake.MOVE_RIGHT) {
            if (mDirection != WEST) {
                mNextDirection = EAST
            }
        }
    }

    fun setDependentViews(msgView: TextView?, arrowView: View?, backgroundView: View?) {
        mStatusText = msgView
        mArrowsView = arrowView
        mBackgroundView = backgroundView
    }

    fun setMode(newMode: Int) {
        val oldMode = gameState
        gameState = newMode
        if (newMode == RUNNING && oldMode != RUNNING) {
            mStatusText!!.visibility = INVISIBLE
            update()
            mArrowsView!!.visibility = VISIBLE
            mBackgroundView!!.visibility = VISIBLE
            return
        }
        val res = context.resources
        var str: CharSequence = ""
        if (newMode == PAUSE) {
            mArrowsView!!.visibility = GONE
            mBackgroundView!!.visibility = GONE
            str = res.getText(R.string.mode_pause)
        }
        if (newMode == READY) {
            mArrowsView!!.visibility = GONE
            mBackgroundView!!.visibility = GONE
            str = res.getText(R.string.mode_ready)
        }
        if (newMode == LOSE) {
            mArrowsView!!.visibility = GONE
            mBackgroundView!!.visibility = GONE
            mAppleList.clear()
            str = res.getString(R.string.mode_lose, totalScore)
        }

        mStatusText!!.text = str
        mStatusText!!.visibility = VISIBLE
    }

    private fun addRandomApple() {
        var newCoord: Coordinate? = null
        var found = false
        while (!found) {
            val newX = 1 + RNG.nextInt(mXTileCount - 2)
            val newY = 1 + RNG.nextInt(mYTileCount - 2)
            newCoord = Coordinate(newX, newY)

            var collision = false
            val snakelength = mSnakeTrail.size
            for (index in 0 until snakelength) {
                if (mSnakeTrail[index]!!.equals(newCoord)) {
                    collision = true
                }
            }

            found = !collision
        }
        mAppleList.add(newCoord)
    }

    fun update() {
        if (gameState == RUNNING) {
            val now = System.currentTimeMillis()
            if (now - mLastMove > mMoveDelay) {
                clearTiles()
                updateWalls()
                updateSnake()
                updateApples()
                mLastMove = now
            }
            mRedrawHandler.sleep(mMoveDelay)
        }
    }

    private fun updateWalls() {
        for (x in 0 until mXTileCount) {
            if (x == mXTileCount / 2) continue
            setTile(BRICK_STAR, x, 0)
            setTile(BRICK_STAR, x, mYTileCount - 1)
        }

        for (y in 0 until mYTileCount) {
            if (y == mYTileCount / 2) continue
            setTile(BRICK_STAR, 0, y)
            setTile(BRICK_STAR, mXTileCount - 1, y)
        }
    }

    private fun updateApples() {
        for (c in mAppleList) {
            setTile(APPLE, c!!.x, c.y)
        }
    }

    private fun loopSnakeX(i: Int): Int {
        var index = i
        while (index < 0) {
            index += mXTileCount
        }
        return index % mXTileCount
    }

    private fun loopSnakeY(i: Int): Int {
        var index = i
        while (index < 0) {
            index += mYTileCount
        }
        return index % mYTileCount
    }

    private fun updateSnake() {
        var growSnake = false

        val head = mSnakeTrail[0]
        var newHead = Coordinate(1, 1)
        mDirection = mNextDirection
        when (mDirection) {
            EAST -> {
                newHead = Coordinate(loopSnakeX(head!!.x + 1), head.y)
            }
            WEST -> {
                newHead = Coordinate(loopSnakeX(head!!.x - 1), head.y)
            }
            NORTH -> {
                newHead = Coordinate(head!!.x, loopSnakeY(head.y - 1))
            }
            SOUTH -> {
                newHead = Coordinate(head!!.x, loopSnakeY(head.y + 1))
            }
        }

        if ((newHead.x == 0 || newHead.x == mXTileCount - 1) &&
                newHead.y > 0 && newHead.y != mYTileCount / 2 ||
                (newHead.y == 0 || newHead.y == mYTileCount - 1) &&
                newHead.x > 0 && newHead.x != mXTileCount / 2) {
            setMode(LOSE)
            return
        }

        val snakeLength = mSnakeTrail.size
        for (snakeIndex in 0 until snakeLength) {
            val c = mSnakeTrail[snakeIndex]
            if (c!!.equals(newHead)) {
                setMode(LOSE)
                return
            }
        }

        val pizzaCount = mAppleList.size
        for (pizzaIndex in 0 until pizzaCount) {
            val c = mAppleList[pizzaIndex]
            if (c!!.equals(newHead)) {
                mAppleList.remove(c)
                addRandomApple()

                mScore++
                totalScore = mScore * 100
                mMoveDelay *= 1

                growSnake = true
            }
        }

        mSnakeTrail.add(0, newHead)
        if (!growSnake) {
            mSnakeTrail.removeAt(mSnakeTrail.size - 1)
        }
        for ((index, c) in mSnakeTrail.withIndex()) {
            if (index == 0) {
                setTile(PURPLE_STAR, c!!.x, c.y)
            } else {
                setTile(PURPLE_STAR, c!!.x, c.y)
            }
        }
    }

    private class Coordinate(var x: Int, var y: Int) {
        fun equals(other: Coordinate): Boolean {
            return x == other.x && y == other.y
        }

        override fun toString(): String {
            return "Coordinate: [$x,$y]"
        }
    }

    companion object {
        const val PAUSE = 0
        const val READY = 1
        const val RUNNING = 2
        const val LOSE = 3
        private const val NORTH = 1
        private const val SOUTH = 2
        private const val EAST = 3
        private const val WEST = 4
        private const val APPLE = 1
        private const val BRICK_STAR = 2
        private const val PURPLE_STAR = 3
        private const val SNAKE_FACE = 4
        private var mScore = 0
        private var totalScore = 0
        private val RNG = Random()
    }
}
