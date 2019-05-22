package com.kotlin.widget.utils

import android.app.Activity
import android.app.ActivityManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.widget.Toast
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class UtilTools {
    companion object {
        val GPS_REQUEST_CODE: Int = 10
        val CROP_PHOTO: Int = 1
        val IMAGE_FILE = Environment.getExternalStorageDirectory().absoluteFile
        //系统相册目录
        var galleryPath = (Environment.getExternalStorageDirectory().toString()
                + File.separator + Environment.DIRECTORY_DCIM
                + File.separator + "Camera" + File.separator)

        // 两次点击按钮之间的点击间隔不能少于1000毫秒
        private val MIN_CLICK_DELAY_TIME = 1000
        private var lastClickTime: Long = 0
        fun getTime(): String {
            return (Date().time / 1000).toString()
        }

        fun getSignature(): String {
            val sb = StringBuffer()
            sb.append(SecretUtil.MD5(StringUtil.SECRETKEY))
            sb.append(SecretUtil.MD5((Date().time / 1000).toString()))
            //        Log.e("+++",timestamp +"\n" + SecretUtil.SHA1(sb.toString()));
            return SecretUtil.SHA1(sb.toString())
        }

        //获取是否存在NavigationBar
        fun checkDeviceHasNavigationBar(context: Context): Boolean {
            var hasNavigationBar = false
            val rs = context.resources
            val id = rs.getIdentifier("config_showNavigationBar", "bool", "android")
            if (id > 0) {
                hasNavigationBar = rs.getBoolean(id)
            }
            try {
                val systemPropertiesClass = Class.forName("android.os.SystemProperties")
                val m = systemPropertiesClass.getMethod("get", String::class.java)
                val navBarOverride = m.invoke(systemPropertiesClass, "qemu.hw.mainkeys") as String
                if ("1" == navBarOverride) {
                    hasNavigationBar = false
                } else if ("0" == navBarOverride) {
                    hasNavigationBar = true
                }
            } catch (e: Exception) {

            }

            return hasNavigationBar

        }

        fun hasNavigationBarShow(activity: AppCompatActivity): Boolean {
            val dm = DisplayMetrics()
            val display = activity.windowManager.defaultDisplay
            display.getMetrics(dm)
            val screenWidth = dm.widthPixels
            val screenHeight = dm.heightPixels
            val density = dm.density

            val realDisplayMetrics = DisplayMetrics()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                display.getRealMetrics(realDisplayMetrics)
            else {
                try {
                    val c = Class.forName("android.view.Display")
                    val method = c.getMethod("", DisplayMetrics::class.java)
                    method.invoke(display, realDisplayMetrics)
                } catch (e: Exception) {
                    realDisplayMetrics.setToDefaults()
                }
            }

            val screenRealWidth = realDisplayMetrics.widthPixels
            val screenRealHeight = realDisplayMetrics.heightPixels

            return (screenRealHeight - screenHeight) > 0
        }

        /**
         * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
         */
        fun dip2px(context: Context, dpValue: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (dpValue * scale + 0.5f).toInt()
        }

        /**
         * 保存bitmap返回保存地址
         * @param context
         * @param bmp
         * @param quality
         * @param currentFile
         * @return
         */
        fun saveImageToGallery(context: Context, bmp: Bitmap, quality: Int, currentFile: File): String {
            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(currentFile)
                bmp.compress(Bitmap.CompressFormat.JPEG, quality, fos)
                fos.flush()
                return currentFile.path
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                return ""
            } catch (e: IOException) {
                e.printStackTrace()
                return ""
            } finally {
                try {
                    if (fos != null) {
                        fos.close()
                        return currentFile.path
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    return ""
                }

            }
        }

        fun getImageCurFile(context: Context): File {
            val fileNameDir = context.packageName
            val appDir = File(IMAGE_FILE, fileNameDir)
            if (!appDir.exists()) {
                appDir.mkdirs()
            }
            //图片名称 时间命名
            val format = SimpleDateFormat("yyyyMMddHHmmss",Locale.getDefault())
            val date = Date(System.currentTimeMillis())
            val fileName = Date().time.toString() + ".jpg"
            val saveFile = File(appDir, fileName)
            try {
                if (saveFile.exists()) {
                    saveFile.delete()
                }
                saveFile.createNewFile()
                return saveFile
            } catch (e: IOException) {
                e.printStackTrace()
                return saveFile
            }

        }

        fun getGalleryPathFile(context: Context): File {
            val appDir = File(galleryPath)
            if (!appDir.exists()) {
                appDir.mkdirs()
            }
            //图片名称 时间命名
            val fileName = Date().time.toString() + ".jpg"
            val saveFile = File(appDir, fileName)
            return try {
                if (saveFile.exists()) {
                    saveFile.delete()
                }
                saveFile.createNewFile()
                saveFile
            } catch (e: IOException) {
                e.printStackTrace()
                saveFile
            }
        }

        fun clearUploadFold(context: Context){
            val fileNameDir = context.packageName
            val uploadFolder = File(IMAGE_FILE, "$fileNameDir/upload")
            if (uploadFolder.exists())
                deleteFileWithDir(uploadFolder)
        }

        fun deleteFileWithDir(dir:File){
            for (file in dir.listFiles()) {
                if (file.isFile) file.delete() // 删除所有文件
            }
        }

        /**
         * 通知相册更新
         */
        fun notifyGallery(context: Context, file: File) {
            if (file.exists()) {
                MediaStore.Images.Media.insertImage(context.contentResolver, file.path, file.name, null)
                val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                val uri = Uri.fromFile(file)
                intent.data = uri
                context.sendBroadcast(intent)
            }
        }

        /**
         * 获取系统时间
         * @return
         */
        fun getSystemTime(format: String): String {

            val formatter = SimpleDateFormat(format,Locale.getDefault())
            val curDate = Date(System.currentTimeMillis())//获取当前时间
            val str = formatter.format(curDate)
            return str
        }

        fun stringToDate(string: String, format: String): Date {
            val formatter = SimpleDateFormat(format,Locale.getDefault())
            return formatter.parse(string)
        }

        /**
         * 获取系统时间
         * @return
         */
        fun getDateFormat(date: Date, format: String): String {

            val formatter = SimpleDateFormat(format,Locale.getDefault())
            val str = formatter.format(date)
            return str
        }

        /**
         * 获取本地软件版本号
         */
        fun getLocalVersionName(ctx: Context): String {
            var localVersion = ""
            try {
                val packageInfo = ctx.applicationContext
                        .packageManager
                        .getPackageInfo(ctx.packageName, 0)
                localVersion = packageInfo.versionName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            return localVersion
        }

        /**
         * 获取屏幕宽度
         * @param context
         * @return
         */
        fun getScreenWidth(context: Context): Int {
            return context.resources.displayMetrics.widthPixels
        }

        /**
         * 获取屏幕高度
         * @param context
         * @return
         */
        fun getScreenHeight(context: Context): Int {
            return context.resources.displayMetrics.heightPixels
        }

        fun isAppInstalled(context: Context, packagename: String): Boolean {
            var packageInfo: PackageInfo?
            try {
                packageInfo = context.packageManager.getPackageInfo(packagename, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                packageInfo = null
                e.printStackTrace()
            }

            return packageInfo != null
        }

        private fun call(callContent: Context, phoneNo: String?) {
            val builder = AlertDialog.Builder(callContent)
            builder.setMessage("确认拨打  $phoneNo  吗？")
            builder.setTitle("提示")
            builder.setPositiveButton("拨打") { dialogInterface, i ->
                // 需要 CALL_PHONE 权限
                val phoneIntent = Intent()
                try {
                    phoneIntent.action = "android.intent.action.CALL"       // 调用拨号，直接拨打
                    phoneIntent.data = Uri.parse("tel:$phoneNo")
                    callContent.startActivity(phoneIntent)
                } catch (e: Exception) {
                    phoneIntent.action = "android.intent.action.DIAL"    // 调用拨号盘，不拨打
                    phoneIntent.data = Uri.parse("tel:$phoneNo")
                    callContent.startActivity(phoneIntent)
                }

                dialogInterface.cancel()
            }
            builder.setNegativeButton("取消") { dialogInterface, i -> dialogInterface.cancel() }

            builder.create().show()
        }

        /**
         * 根据资源名称获取本地ID
         */
        fun getResourceByName(context: Context, resourceName: String, resType: String): Int {
            return context.resources.getIdentifier(resourceName, resType, context.packageName)
        }

        /**
         * 获取进程名字
         */
        fun getProcessName(context: Context, pid: Int): String {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val list = activityManager.runningAppProcesses
            val i = list.iterator()
            while (i.hasNext()) {
                val info = i.next()
                try {
                    if (info.pid == pid)
                        return info.processName
                } catch (e: Exception) {

                }
            }
            return ""
        }

        fun isFastClick(): Boolean {
            var flag = false
            val curClickTime = System.currentTimeMillis()
            if (curClickTime - lastClickTime >= MIN_CLICK_DELAY_TIME) {
                flag = true
            }
            lastClickTime = curClickTime
            return flag
        }

        /**
         * 调用相机
         * @param activity
         * @param takeUri
         */
        fun takePhoto(activity: AppCompatActivity, takeUri: Uri) {
            val path = takeUri.path

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE) //照相
            if (Integer.valueOf(Build.VERSION.SDK_INT) >= Build.VERSION_CODES.LOLLIPOP) {
                val file = File(path!!)
                val outputUri = FileProvider.getUriForFile(activity, activity.packageName + ".fileprovider", file)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri) //指定图片输出地址
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, takeUri) //指定图片输出地址
            }
            // 广播刷新相册
            val intentBc = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            intentBc.data = takeUri
            activity.sendBroadcast(intentBc)
            activity.startActivityForResult(intent, 888) //启动照相
        }


        fun getSignature(map: HashMap<String, String>): String {
            var signStr = ""
            val treeSet = TreeSet<String>()
            for (entry: Map.Entry<String, String> in map.entries) {
                if (entry.key != "token" && entry.key != "timestamp") {
                    treeSet.add(entry.key)
                }
            }

            val iterator = treeSet.iterator()

            while (iterator.hasNext())
                signStr = signStr + map[iterator.next()] + "&"
            if (signStr.isNotEmpty())
                signStr = signStr.substring(0, signStr.length - 1)
            val paramSign = SecretUtil.MD5(signStr)
            val secretKey = SecretUtil.MD5(StringUtil.SECRETKEY)
            val timestamp = SecretUtil.MD5(UtilTools.getTime())
            return SecretUtil.SHA1(paramSign + secretKey + timestamp)
        }

        /**
         * 判断是否有网络
         *
         * @param context
         * @return
         */
        fun isNetWorkON(context: Context): Boolean {
            var netStatus = false
            val cwjManager = context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (cwjManager.activeNetworkInfo != null) {
                netStatus = cwjManager.activeNetworkInfo.isAvailable
            }
            if (!netStatus) {
                Toast.makeText(context, "无网络,请检查网络", Toast.LENGTH_SHORT).show()
            }
            return netStatus
        }

        /**
         * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
         * @param context
         * @return true 表示开启
         */

        fun isOpenGPS(context: Context): Boolean {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
            val gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
//            val network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            return gps
        }


        /**
         * 强制打开Gps
         */
        fun openForceGps(context: Context) {
            var intent = Intent()
            intent.setClassName("com.android.settings",
                    "com.android.settings.widget.SettingsAppWidgetProvider")
            intent.addCategory("android.intent.category.ALTERNATIVE").data = Uri.parse("custom:3")
            PendingIntent.getBroadcast(context, 0, intent, 0).send()
        }



        /**
         * 跳转到设置页面打开GPS
         */
        fun openGPSSetting(context: Activity) {
            val dialog = AlertDialog.Builder(context).setMessage("开启位置权限，获取精准位置")
                    .setPositiveButton("确定") { _, _ ->
                        var intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        context.startActivityForResult(intent, GPS_REQUEST_CODE)
                    }.create()
            dialog.setCancelable(false)
            dialog.show()
        }
    }


}