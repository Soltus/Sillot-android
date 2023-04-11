package org.b3log.siyuan.permission

import android.Manifest

object Ps { // 权限组
    val useLocation = mutableListOf(
        Manifest.permission.ACCESS_BACKGROUND_LOCATION, // 允许应用访问后台位置。
        Manifest.permission.ACCESS_COARSE_LOCATION, // 允许应用访问大概位置。
        Manifest.permission.ACCESS_FINE_LOCATION, // 允许应用访问精确位置。
        Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,  // 允许应用程序访问其他位置提供程序命令。
        Manifest.permission.ACCESS_MEDIA_LOCATION, // 允许应用访问用户共享集合中保留的任何地理位置。
    )
    val useNet = mutableListOf(
        Manifest.permission.ACCESS_NETWORK_STATE, // 允许获取网络信息状态
        Manifest.permission.ACCESS_WIFI_STATE, // 允许获取当前WiFi接入的状态以及WLAN热点的信息
        Manifest.permission.BLUETOOTH, // 允许程序连接到已配对的蓝牙设备
        Manifest.permission.BLUETOOTH_ADMIN, // 允许应用程序发现和配对蓝牙设备
        Manifest.permission.CHANGE_NETWORK_STATE, // 允许程序改变网络连接状态
        Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,  // 允许应用程序进入Wi-Fi组播模式。
        Manifest.permission.CHANGE_WIFI_STATE, // 允许程序改变Wi-Fi连接状态
        Manifest.permission.INTERNET,  // 允许程序访问网络连接
    )
    val useNotification = mutableListOf(
        Manifest.permission.ACCESS_NOTIFICATION_POLICY, // 希望访问通知策略的应用程序的标记权限。
    )
    val useCallable = mutableListOf(
        Manifest.permission.ANSWER_PHONE_CALLS, // 允许该应用接听来电。
        Manifest.permission.CALL_PHONE, // 允许应用程序在不通过Dialer用户界面的情况下发起电话呼叫，以使用户确认呼叫。
        Manifest.permission.READ_CONTACTS, // 允许应用程序读取用户的联系人数据。
    )
    val useMedia = mutableListOf(
        Manifest.permission.CAMERA, // 允许程序访问摄像头进行拍照
        Manifest.permission.RECORD_AUDIO, // 允许程序录制音频，通过手机或耳机的麦克
    )
    val useSensor = mutableListOf(
        Manifest.permission.ACTIVITY_RECOGNITION,  // 允许应用程序识别身体活动。
        Manifest.permission.BODY_SENSORS, // 允许应用访问来自传感器的数据，用户可使用传感器来测量他/她体内发生的事情，例如心律。
        Manifest.permission.NFC, // 允许应用程序通过NFC执行I / O操作。
        Manifest.permission.VIBRATE, // 允许访问振动设备
    )
    val useCom = mutableListOf(
        Manifest.permission.WAKE_LOCK, // 允许程序在手机屏幕关闭后后台进程仍然运行（唤醒锁定）? / 允许使用PowerManager WakeLocks保持处理器休眠或屏幕变暗?
        Manifest.permission.SYSTEM_ALERT_WINDOW, // 允许应用程序使用在WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY所有其他应用程序顶部显示的类型创建窗口 。
        Manifest.permission.REQUEST_COMPANION_RUN_IN_BACKGROUND, // 允许伴随应用在后台运行。
        Manifest.permission.REQUEST_COMPANION_USE_DATA_IN_BACKGROUND, // 允许同伴应用在后台使用数据。
        Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, // 忽略电池优化,应用程序必须拥有才能使用的许可 Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS。
        Manifest.permission.KILL_BACKGROUND_PROCESSES,  // 允许应用程序调用 ActivityManager.killBackgroundProcesses(String)。
        Manifest.permission.FOREGROUND_SERVICE, // 允许使用常规应用程序Service.startForeground。
        Manifest.permission.BROADCAST_STICKY, // 允许应用程序广播粘性意图。
        Manifest.permission.CALL_COMPANION_APP, // 允许实现InCallServiceAPI 的应用 有资格作为呼叫伴随应用启用。
    )
    val useData = mutableListOf(
        Manifest.permission.GET_PACKAGE_SIZE, // 允许应用程序找出任何包使用的空间。
    )
    val useConvenient = mutableListOf(
        Manifest.permission.EXPAND_STATUS_BAR, // 允许程序扩展或收缩状态栏
        Manifest.permission.INSTALL_SHORTCUT, // 允许应用程序创建快捷方式
        Manifest.permission.READ_CALENDAR, // 允许程序读取用户日历数据
        Manifest.permission.WRITE_CALENDAR, // 允许一个程序写入但不读取用户日历数据
        Manifest.permission.REQUEST_INSTALL_PACKAGES, // 允许应用程序请求安装软件包。
        Manifest.permission.SET_ALARM, // 允许程序设置闹铃提醒? / 允许应用程序广播意图为用户设置警报?
        Manifest.permission.RECEIVE_BOOT_COMPLETED, // 允许应用程序接收 Intent.ACTION_BOOT_COMPLETED系统完成引导后广播的消息。
    )
    val useSafe = mutableListOf(
        Manifest.permission.REQUEST_PASSWORD_COMPLEXITY, // 允许应用程序请求屏幕锁定复杂性，并提示用户将屏幕锁定更新到特定的复杂性级别。
    )
    val useAPI30 = mutableListOf(// android 11 / api 30
        Manifest.permission.MANAGE_EXTERNAL_STORAGE, // 允许应用程序广泛访问作用域存储中的外部存储。
        Manifest.permission.MANAGE_DOCUMENTS, // 允许应用程序通常作为文档选择器的一部分来管理对文档的访问。 ProtectedPermissions
    )
    val PG_Core = useNet + useCom // 核心权限组，应当在首次安装时申请
}