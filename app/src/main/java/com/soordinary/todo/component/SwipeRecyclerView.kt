package com.soordinary.todo.component

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.Scroller
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/**
 * 继承自RecyclerView，可实现子项拖动功能的列表
 *
 * @limitation RecyclerView的子项布局的最大父布局必须有两个子布局
 */
class SwipeRecyclerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : RecyclerView(context, attrs, defStyle) {

    companion object {
        private const val INVALID_POSITION = -1 // 触摸到的点不在子View范围内
        private const val INVALID_CHILD_WIDTH = -1 // 子ItemView不含两个子View
        private const val SNAP_VELOCITY = 600 // 最小滑动速度
    }

    private var velocityTracker: VelocityTracker? = null // 速度追踪器
    private val isScaledMin = ViewConfiguration.get(context).scaledTouchSlop // 认为是滑动的最小距离（一般由系统提供）
    private var childRangeSearchCache: Rect? = null // 子View所在的矩形范围探查器的缓存
    private val scroller = Scroller(context)
    private var lastTouchedX = 0f // 滑动过程中记录上次触碰点X
    private var touchFirstX = 0f
    private var touchFirstY = 0f // 首次触碰的坐标
    private var isSlideChild = false // 是否滑动子View
    private var touchedChild: ViewGroup? = null // 被触碰的子View
    private var touchedChildPosition = 0 // 触碰的view在可见item中的位置
    private var touchedChildMenuWidth = 0 // 触碰到的子View的菜单项的宽度

    private var stateCallback: StateCallback? = null

    var isTouchOpened: Boolean = false

    // 判断滑动事件是否是用户要滑动子View的意图
    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        val x = e.x.toInt()
        val y = e.y.toInt()
        obtainVelocity(e)
        when (e.action) {
            ACTION_DOWN -> {
                if (!scroller.isFinished) {  // 如果动画还没停止，则立即终止动画
                    scroller.abortAnimation()
                }
                run {
                    lastTouchedX = x.toFloat()
                    touchFirstX = lastTouchedX
                }
                touchFirstY = y.toFloat()
                touchedChildPosition = pointToPosition(x, y) // 获取触碰点所在的position
                if (touchedChildPosition != INVALID_POSITION) {
                    stateCallback?.dragEnable(false)
                    val lastTouchedView = touchedChild
                    // 获取触碰点所在的view
                    touchedChild = getChildAt(touchedChildPosition) as ViewGroup
                    // 点击的是否是上次点击并且被打开的View
                    isTouchOpened = (touchedChild === lastTouchedView) && (touchedChild!!.scrollX != 0)
                    // 如果上次一打开的View还没有关闭，先关闭
                    if (lastTouchedView != null && touchedChild !== lastTouchedView && lastTouchedView.scrollX != 0) {
                        lastTouchedView.scrollTo(0, 0)
                    }
                    // 强制的要求RecyclerView的子ViewGroup必须要有2个子view,这样菜单按钮才会有值
                    // 注意:如果不定制RecyclerView的子View，则要求子View必须要有固定的width。
                    touchedChildMenuWidth = if (touchedChild!!.childCount == 2) {
                        touchedChild!!.getChildAt(1).width
                    } else {
                        INVALID_CHILD_WIDTH
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                // 判断是在上下移动整个RecycleView还是左右移动子项
                velocityTracker!!.computeCurrentVelocity(1000)
                // 此处有俩判断，满足其一则认为是侧滑：
                // 1.如果x方向速度大于y方向速度，且大于最小速度限制；
                // 2.如果x方向的侧滑距离大于y方向滑动距离，且x方向达到最小滑动距离；
                val xVelocity = velocityTracker!!.xVelocity
                val yVelocity = velocityTracker!!.yVelocity
                if (abs(xVelocity.toDouble()) > SNAP_VELOCITY && abs(xVelocity.toDouble()) > abs(yVelocity.toDouble())
                    || abs((x - touchFirstX).toDouble()) >= isScaledMin
                    && abs((x - touchFirstX).toDouble()) > abs((y - touchFirstY).toDouble())
                ) {
                    isSlideChild = true
                    stateCallback?.dragEnable(false)
                    return true
                } else {
                    if (!isTouchOpened) {
                        stateCallback?.dragEnable(true)
                    }
                }
            }

            MotionEvent.ACTION_UP -> releaseVelocity()
        }
        return super.onInterceptTouchEvent(e)
    }

    // 寻找触摸的子View
    private fun pointToPosition(x: Int, y: Int): Int {
        var frame = childRangeSearchCache
        if (childRangeSearchCache == null) {
            childRangeSearchCache = Rect()
            frame = childRangeSearchCache
        }

        val count = childCount
        for (i in count - 1 downTo 0) {
            val child = getChildAt(i)
            if (child.visibility == VISIBLE) {
                child.getHitRect(frame)
                if (frame!!.contains(x, y)) {
                    return i
                }
            }
        }
        return INVALID_POSITION
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (isSlideChild && touchedChildPosition != INVALID_POSITION) {
            val x = e.x
            obtainVelocity(e)
            when (e.action) {
                ACTION_DOWN -> {}
                // 随手指滑动
                MotionEvent.ACTION_MOVE -> {
                    if (touchedChildMenuWidth != INVALID_CHILD_WIDTH) {
                        val dx = lastTouchedX - x
                        if (touchedChild!!.scrollX + dx <= touchedChildMenuWidth
                            && touchedChild!!.scrollX + dx > 0
                        ) {
                            touchedChild!!.scrollBy(dx.toInt(), 0)
                        }
                        lastTouchedX = x
                    }
                }
                // 松手时
                MotionEvent.ACTION_UP -> {
                    if (touchedChildMenuWidth != INVALID_CHILD_WIDTH) {
                        val scrollX = touchedChild!!.scrollX
                        velocityTracker!!.computeCurrentVelocity(1000)
                        // 此处有两个原因决定是否打开菜单：
                        // 1.菜单被拉出宽度大于菜单宽度一半；
                        // 2.横向滑动速度大于最小滑动速度；
                        // 注意：之所以要小于负值，是因为向左滑则速度为负值
                        if (velocityTracker!!.xVelocity < -SNAP_VELOCITY) {    // 向左侧滑达到侧滑最低速度，则打开
                            val delt = abs((touchedChildMenuWidth - scrollX).toDouble()).toInt()
                            val t = (delt / velocityTracker!!.xVelocity * 1000).toInt()
                            scroller.startScroll(scrollX, 0, touchedChildMenuWidth - scrollX, 0, abs(t.toDouble()).toInt())
                        } else if (velocityTracker!!.xVelocity >= SNAP_VELOCITY) {  // 向右侧滑达到侧滑最低速度，则关闭
                            scroller.startScroll(scrollX, 0, -scrollX, 0, abs(scrollX.toDouble()).toInt())
                        } else if (scrollX >= touchedChildMenuWidth / 2) { // 如果超过删除按钮一半，则打开
                            scroller.startScroll(scrollX, 0, touchedChildMenuWidth - scrollX, 0, abs((touchedChildMenuWidth - scrollX).toDouble()).toInt())
                        } else {    // 其他情况则关闭
                            scroller.startScroll(scrollX, 0, -scrollX, 0, abs(scrollX.toDouble()).toInt())
                        }
                        invalidate()
                    }
                    touchedChildMenuWidth = INVALID_CHILD_WIDTH
                    isSlideChild = false
                    touchedChildPosition = INVALID_POSITION
                    releaseVelocity() // 这里之所以会调用，是因为如果前面拦截了，就不会执行ACTION_UP,需要在这里释放追踪
                }
            }
            return true
        } else {
            // 此处防止RecyclerView正常滑动时，还有菜单未关闭，因为有视图重用
            closeMenu()
            // Velocity，这里的释放是防止RecyclerView正常拦截了，但是在onTouchEvent中却没有被释放；
            // 有三种情况：1.onInterceptTouchEvent并未拦截，在onInterceptTouchEvent方法中，DOWN和UP一对获取和释放；
            // 2.onInterceptTouchEvent拦截，DOWN获取，但事件不是被侧滑处理，需要在这里进行释放；
            // 3.onInterceptTouchEvent拦截，DOWN获取，事件被侧滑处理，则在onTouchEvent的UP中释放。
            releaseVelocity()
        }
        return super.onTouchEvent(e)
    }

    private fun releaseVelocity() {
        if (velocityTracker != null) {
            velocityTracker!!.clear()
            velocityTracker!!.recycle()
            velocityTracker = null
        }
    }

    private fun obtainVelocity(event: MotionEvent) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker!!.addMovement(event)
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            touchedChild!!.scrollTo(scroller.currX, scroller.currY)
            invalidate()
        }
    }

    /**
     * 将显示子菜单的子view关闭
     * 这里本身是要自己来实现的，但是由于不定制item，因此不好监听器点击事件，因此需要调用者手动的关闭
     */
    fun closeMenu() {
        if (touchedChild != null && touchedChild!!.scrollX != 0) {
            touchedChild!!.scrollTo(0, 0)
        }
    }

    fun setStateCallback(stateCallback: StateCallback) {
        this.stateCallback = stateCallback
    }

    interface StateCallback {
        fun dragEnable(enable: Boolean)
    }
}
