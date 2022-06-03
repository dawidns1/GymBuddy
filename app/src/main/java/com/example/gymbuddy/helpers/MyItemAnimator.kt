package com.example.gymbuddy.helpers

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

class MyItemAnimator : DefaultItemAnimator() {
    override fun animateChange(oldHolder: RecyclerView.ViewHolder, newHolder: RecyclerView.ViewHolder, fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
        return super.animateChange(oldHolder, newHolder, fromX, fromY, toX, toY)
    }

    override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder, payloads: List<Any>): Boolean {
        return super.canReuseUpdatedViewHolder(viewHolder, payloads)
    }

    override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
//        return super.canReuseUpdatedViewHolder(viewHolder);
        return true
    }
}