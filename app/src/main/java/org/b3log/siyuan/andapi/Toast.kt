package org.b3log.siyuan.andapi

import android.content.Context
import android.widget.Toast

object Toast {
    fun Show(mContext: Context,mBody: String) {Toast.makeText(mContext, mBody, Toast.LENGTH_LONG).show()}
}