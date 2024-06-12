package org.b3log.siyuan

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.tencent.bugly.crashreport.BuglyLog

class NotificationAccessService : NotificationListenerService() {

    companion object {
        private const val TAG = "NotificationAccessService"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let {
            // 当有新通知发布时调用
            logNotification(it, "Posted")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn?.let {
            // 当通知被移除时调用
            logNotification(it, "Removed")
        }
    }
    override fun onListenerConnected() {
        BuglyLog.d(TAG, "onListenerConnected: Connected to client")
    }

    override fun onListenerDisconnected() {
        BuglyLog.d(TAG, "onListenerDisconnected: Disconnected from client")
    }

    private fun logNotification(sbn: StatusBarNotification, action: String) {
        val packageName = sbn.packageName
        val id = sbn.id
        val tag = sbn.tag
        val title = sbn.notification.extras.getString(Notification.EXTRA_TITLE)
        val text = sbn.notification.extras.getString(Notification.EXTRA_TEXT)

        BuglyLog.d(TAG, "$action notification: Package=$packageName, ID=$id, Tag=$tag, Title=$title, Text=$text")
    }
}

