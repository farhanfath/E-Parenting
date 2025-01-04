package com.jamali.eparenting.utils

import android.view.View

object RecyclerViewUtils {
    fun setEmptyState(isEmpty: Boolean, emptyView: View, recyclerView: View) {
        if (isEmpty) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}