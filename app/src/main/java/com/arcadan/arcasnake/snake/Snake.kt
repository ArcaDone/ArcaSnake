package com.arcadan.arcasnake.snake

import android.annotation.SuppressLint
import android.app.Activity
import android.media.MediaPlayer
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import com.arcadan.arcasnake.R

class Snake : Activity() {
    private var mSnakeView: SnakeView? = null
    private var mediaPlayer: MediaPlayer? = null

    @SuppressLint("ClickableViewAccessibility")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.snake_layout)
        mSnakeView = findViewById<View>(R.id.snake) as SnakeView
        mSnakeView!!.setDependentViews(findViewById<View>(R.id.text) as TextView,
                findViewById(R.id.background), findViewById(R.id.background))
        if (savedInstanceState == null) {
            mSnakeView!!.setMode(SnakeView.READY)
        } else {
            val map = savedInstanceState.getBundle(ICICLE_KEY)
            if (map != null) {
                mSnakeView!!.restoreState(map)
            } else {
                mSnakeView!!.setMode(SnakeView.PAUSE)
            }
        }

        mSnakeView!!.setOnTouchListener { v, event ->
            if (mSnakeView!!.gameState == SnakeView.RUNNING) {
                val x = event.x / v.width
                val y = event.y / v.height

                var direction: Int = if (x > y) 1 else 0
                direction += if (x > 1 - y) 2 else 0

                mSnakeView!!.moveSnake(direction)
            } else {

                mSnakeView!!.moveSnake(MOVE_UP)
            }
            false
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.snakemusic)
        mediaPlayer!!.start()
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer!!.stop()
        mSnakeView!!.setMode(SnakeView.PAUSE)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer!!.pause()
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        outState.putBundle(ICICLE_KEY, mSnakeView!!.saveState())
    }

    override fun onKeyDown(keyCode: Int, msg: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_W -> mSnakeView!!.moveSnake(MOVE_UP)
            KeyEvent.KEYCODE_D -> mSnakeView!!.moveSnake(MOVE_RIGHT)
            KeyEvent.KEYCODE_S -> mSnakeView!!.moveSnake(MOVE_DOWN)
            KeyEvent.KEYCODE_A -> mSnakeView!!.moveSnake(MOVE_LEFT)
        }
        return super.onKeyDown(keyCode, msg)
    }

    companion object {
        @JvmField
        var MOVE_LEFT = 0

        @JvmField
        var MOVE_UP = 1

        @JvmField
        var MOVE_DOWN = 2

        @JvmField
        var MOVE_RIGHT = 3
        private const val ICICLE_KEY = "snake-view"
    }
}