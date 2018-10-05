package sample.ui.vincent.com.recyclerswipetodeletesample

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.MotionEvent
import android.view.View
import java.util.*

class SwipeToDeleteTouchHelper(var context: Context, var recyclerView: RecyclerView, var listener: SwipeToDeleteListener) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
    
    val backgroundColor = ColorDrawable(Color.RED)
    val paint = Paint()
    //queue to unswipe prev swiped
    val queue: Queue<Int> = object : LinkedList<Int>() {
        override fun add(element: Int): Boolean {
            return if (contains(element)) false
            else super.add(element)
        }
    }
    
    init {
        paint.color = Color.WHITE
        paint.textSize = SLUtils.convertDpToPixel(14f, context)
        paint.textAlign = Paint.Align.CENTER
    }
    
    interface SwipeToDeleteListener {
        fun onDeleteClick(pos: Int)
    }
    
    override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
        return false
    }
    
    @SuppressLint("ClickableViewAccessibility")
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
        viewHolder?.run {
            val localAdapterPosition = adapterPosition
            if (localAdapterPosition > -1 && localAdapterPosition != queue.peek()) {
                val prevIndex = queue.poll()
                prevIndex?.let { recyclerView.adapter.notifyItemChanged(it) }
                queue.add(localAdapterPosition)
                val rect = itemView.let {
                    val returnRect = Rect(it.right - (it.right / 4), it.top, it.right, it.bottom)
                    returnRect
                }
                recyclerView.setOnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_DOWN && rect.contains(event.x.toInt(), event.y.toInt())) {
                        listener.onDeleteClick(localAdapterPosition)
                    }
                    false
                }
            } else if (localAdapterPosition == queue.peek() && direction == ItemTouchHelper.RIGHT) {
                queue.poll()
            }
        }
    }
    
    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder?): Float {
        return .85f
    }
    
    override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
        return defaultValue * 2f
    }
    
    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return defaultValue * 2f
    }
    
    override fun onChildDraw(c: Canvas?, recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        viewHolder?.run {
            if (adapterPosition < 0) return
            backgroundColor.run {
                setBounds(itemView.right + (dX.toInt() / 4), itemView.top, itemView.right, itemView.bottom)
                draw(c)
            }
            c?.drawText("Delete", itemView.right + dX / 8, itemView.top + 7f + itemView.height / 2f, paint)
            super.onChildDraw(c, recyclerView, viewHolder, dX / 4, dY, actionState, isCurrentlyActive)
        }
    }
}