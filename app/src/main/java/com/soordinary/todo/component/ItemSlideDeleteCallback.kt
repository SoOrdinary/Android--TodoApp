package com.soordinary.todo.component

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/**
 * RecycleItem拖动扩展类
 *
 * @role1 实现Item的拖动变小效果，并通过RecyclerView的Adapter回调其他操作
 */
class ItemSlideDeleteCallback(var slideDeleteListener: SlideDeleteListener) : ItemTouchHelper.Callback() {


    interface SlideDeleteListener {
        fun onSwipedItem(position: Int)
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) = makeMovementFlags(0, (ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT))

    // 是否启用
    override fun isLongPressDragEnabled() = true

    // 上下顺序移动
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

    // 侧滑删除回调
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = slideDeleteListener.onSwipedItem(viewHolder.getAdapterPosition())

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        //侧滑状态
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            // 侧滑让item有个变小的动画
            val alpha = 1 - abs(dX) / (viewHolder.itemView.width.toFloat())
            viewHolder.itemView.setAlpha(alpha)
            viewHolder.itemView.scaleX = Math.max(0.7.toFloat(), alpha)
            viewHolder.itemView.scaleX = Math.max(0.7.toFloat(), alpha)
            if (alpha <= 0) {//让属性动画回复原状
                viewHolder.itemView.setAlpha(1F)
                viewHolder.itemView.scaleX = 1F
                viewHolder.itemView.scaleY = 1F
            }
        } else {
            //让属性动画回复原状
            viewHolder.itemView.setAlpha(1F);
            viewHolder.itemView.scaleX = 1F;
            viewHolder.itemView.scaleY = 1F;
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}