package org.b3log.siyuan.appUtils;

import static android.content.Context.TELEPHONY_SERVICE;

import static org.b3log.siyuan.BuildConfig.DEBUG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.media.AudioAttributes;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class HWs {
    private static final String TAG = "mlink";
    private static final Object object = new Object();
    private static HWs HWs;

    public static HWs getInstance() {
        if (HWs == null) {
            synchronized (object) {
                if (HWs == null) {
                    HWs = new HWs();
                }
            }
        }
        return HWs;
    }

    /*
     * 获取MEID
     * 注：调用前需要获取READ_PHONE_STATE权限
     * */
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public String getMEID(Context context) {
        String meid = "";
        TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        if (null != mTelephonyMgr) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                meid = mTelephonyMgr.getMeid();
                Log.i(TAG, "Android版本大于o-26-优化后的获取---meid:" + meid);
            } else {
                meid = mTelephonyMgr.getDeviceId();
            }
        }

        Log.i(TAG, "优化后的获取---meid:" + meid);

        return meid;
    }

    /**
     * 获取IMEI
     * 注：调用前需要获取READ_PHONE_STATE权限
     *
     * @param context Context
     * @param index   取第几个imei(0,1)
     * @return
     */
    @SuppressLint("MissingPermission")
    public String getIMEI(Context context, int index) {
        String imei = "";
        TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        if (null != mTelephonyMgr) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                imei = mTelephonyMgr.getImei(index);
                Log.i(TAG, "Android版本大于o-26-优化后的获取---imei-:" + imei);
            } else {
                try {
                    imei = getDoubleImei(mTelephonyMgr, "getDeviceIdGemini", index);
                } catch (Exception e) {
                    try {
                        imei = getDoubleImei(mTelephonyMgr, "getDeviceId", index);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    Log.e(TAG, "get device id fail: " + e.toString());
                }
            }
        }

        Log.i(TAG, "优化后的获取---imei：" + imei);
        return imei;
    }

    /**
     * 获取双卡手机的imei
     */
    private String getDoubleImei(TelephonyManager telephony, String predictedMethodName, int slotID) throws Exception {
        String inumeric = null;

        Class<?> telephonyClass = Class.forName(telephony.getClass().getName());
        Class<?>[] parameter = new Class[1];
        parameter[0] = int.class;
        Method getSimID = telephonyClass.getMethod(predictedMethodName, parameter);
        Object[] obParameter = new Object[1];
        obParameter[0] = slotID;
        Object ob_phone = getSimID.invoke(telephony, obParameter);
        if (ob_phone != null) {
            inumeric = ob_phone.toString();
        }
        return inumeric;
    }

    /**
     * 获取品牌
     */
    public String getPhoneBrand() {
//        TelephonyManager manager= (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        String mtype = android.os.Build.MODEL;
        String brand = android.os.Build.BRAND;//手机品牌
        return brand;
    }

    /**
     * 获取型号
     */
    public String getPhoneMODEL() {
        String model = android.os.Build.MODEL;//手机型号
        return model;
    }

    /**
     * 获取手机分辨率
     *
     * @param context
     * @return
     */
    public String getResolution(Context context) {
        // 方法1 Android获得屏幕的宽和高
        WindowManager windowManager = ((Activity) context).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();
        Log.w(TAG, "分辨率：" + screenWidth + "*" + screenHeight);
        return screenWidth + "*" + screenHeight;
    }

    /**
     * 获取运营商
     *
     * @param context
     * @return
     */
    public String getNetOperator(Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        String iNumeric = manager.getSimOperator();
        String netOperator = "";
        if (iNumeric.length() > 0) {
            if (iNumeric.equals("46000") || iNumeric.equals("46002")) {
                // 中国移动
                netOperator = "中国移动";
            } else if (iNumeric.equals("46003")) {
                // 中国电信
                netOperator = "中国电信";
            } else if (iNumeric.equals("46001")) {
                // 中国联通
                netOperator = "中国联通";
            } else {
                //未知
                netOperator = "未知";
            }
        }
        Log.w(TAG, "运营商：" + netOperator);
        return netOperator;
    }

    /**
     * 获取联网方式
     */
    public String getNetMode(Context context) {
        String strNetworkType = "未知";
//        TelephonyManager manager= (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        manager.getNetworkType();
        ConnectivityManager manager = (ConnectivityManager) context.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            int netMode = networkInfo.getType();
            if (netMode == ConnectivityManager.TYPE_WIFI) {
                strNetworkType = "WIFI";
                //wifi
            } else if (netMode == ConnectivityManager.TYPE_MOBILE) {
                int networkType = networkInfo.getSubtype();
                switch (networkType) {

                    //2g
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                        strNetworkType = "2G";
                        break;

                    //3g
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                    case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                    case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                        strNetworkType = "3G";
                        break;

                    case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                        strNetworkType = "4G";
                        break;

                    default:
                        String _strSubTypeName = networkInfo.getSubtypeName();
                        // http://baike.baidu.com/item/TD-SCDMA 中国移动 联通 电信 三种3G制式
                        if (_strSubTypeName.equalsIgnoreCase("TD-SCDMA") || _strSubTypeName.equalsIgnoreCase("WCDMA") || _strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                            strNetworkType = "3G";
                        } else {
                            strNetworkType = _strSubTypeName;
                        }
                        break;
                }
            }
        }
        Log.w(TAG, "联网方式:" + strNetworkType);
        return strNetworkType;
    }

    /**
     * 获取操作系统
     *
     * @return
     */
    public String getOS() {
        Log.w(TAG, "操作系统:" + "Android" + android.os.Build.VERSION.RELEASE);
        return "Android" + android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取wifi当前ip地址
     *
     * @param context
     * @return
     */
    public String getLocalIpAddress(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int i = wifiInfo.getIpAddress();
            return int2ip(i);
        } catch (Exception ex) {
            return " 请保证是WIFI,或者请重新打开网络!\n" + ex.getMessage();
        }
    }

    /**
     * 将ip的整数形式转换成ip形式
     *
     * @param ipInt
     * @return
     */
    public String int2ip(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

    /**
     * 获取蓝牙MAC地址
     *
     * @return
     */
    public String getBtAddressByReflection() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Field field = null;
        try {
            field = BluetoothAdapter.class.getDeclaredField("mService");
            field.setAccessible(true);
            Object bluetoothManagerService = field.get(bluetoothAdapter);
            if (bluetoothManagerService == null) {
                return null;
            }
            Method method = bluetoothManagerService.getClass().getMethod("getAddress");
            if (method != null) {
                Object obj = method.invoke(bluetoothManagerService);
                if (obj != null) {
                    return obj.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取android序列号SN
     * 8675 8604 3504 498
     *
     * @return id或者空串
     */
    public synchronized String getSerialNumber() {
        String serialNumber = null;
        try {
            Class clazz = Class.forName("android.os.SystemProperties");
            if (clazz != null) {
                Method method_get = clazz.getMethod("get", String.class, String.class);
                if (method_get != null) {
                    serialNumber = (String) (method_get.invoke(clazz, "ro.serialno", ""));
                }
            }
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }

        return serialNumber != null ? serialNumber : "";
    }

    /*
     * 获取手机号(基于sim卡是否有写入，未写入则返回空)
     * 注：调用前需要获取READ_PHONE_STATE权限！！！
     * @return 手机号
     *
     *  //截取+86
            if (phone.startsWith("+86")) {
                phone = phone.substring(3, phone.length());
            }
     *
     *
     * */
    @SuppressLint("MissingPermission")
    public String getPhone(Context context) {
        String tel = "";
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
            tel = tm.getLine1Number();
            return tel;
        } catch (Exception e) {
            tel = "";
        }
        return tel;

    }

    /**
     * 唤醒屏幕
     *
     * @param context
     */
    @SuppressLint("InvalidWakeLockTag")
    public static void wakeUpAndUnlock(Context context) {
        //屏锁管理器
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        //解锁
        kl.disableKeyguard();
        //获取电源管理器对象
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
        //点亮屏幕
        wl.acquire();
        //释放
        wl.release();
    }

    /**
     * 震动
     *
     * @param context
     * @param vibrationPattern 第二参数表示从哪里开始循环，比如这里的0表示这个数组在第一次循环完之后会从下标0开始循环到最后，这里的如果是-1表示不循环。
     */
    public void vibrator(Context context, long[] vibrationPattern) {
        //获取系统的Vibrator服务
        Vibrator vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        vibrator.vibrate(vibrationPattern, -1);
    }

    /**
     * 震动 适配6.0以上
     *
     * @param context
     * @param pattern 第二参数表示从哪里开始循环，比如这里的0表示这个数组在第一次循环完之后会从下标0开始循环到最后，这里的如果是-1表示不循环。
     */
    public void vibratorForLollipop(Context context, long[] pattern) {
        Vibrator mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        AudioAttributes audioAttributes = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM) //key
                    .build();
            mVibrator.vibrate(pattern, -1, audioAttributes);
        } else {
            mVibrator.vibrate(pattern, -1);
        }
    }
}