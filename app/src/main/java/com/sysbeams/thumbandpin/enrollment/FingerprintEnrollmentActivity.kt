package com.sysbeams.thumbandpin.enrollment

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.PowerManager
import android.os.SystemClock
import android.provider.Settings
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.suprema.BioMiniFactory
import com.suprema.CaptureResponder
import com.suprema.IBioMiniDevice
import com.suprema.IUsbEventHandler
import com.suprema.util.Logger
import com.sysbeams.thumbandpin.R
import com.sysbeams.thumbandpin.SharedPreferencesHelper
import com.sysbeams.thumbandpin.api.models.UserEnrollmentDto
import com.telpo.tps550.api.fingerprint.FingerPrint


class FingerprintEnrollmentActivity: ComponentActivity() {
    lateinit var mWakeLock: PowerManager.WakeLock
    private var mCaptureStartTime: Long = 0
    private var mBioMiniFactory: BioMiniFactory? = null
    private var mTemplateData: IBioMiniDevice.TemplateData? = null
    private val mCaptureOption = IBioMiniDevice.CaptureOption()
    var mCurrentDevice: IBioMiniDevice? = null
    private val BASE_EVENT = 3000
    private val SET_TEXT_LOGVIEW: Int = BASE_EVENT + 10
    private var mBVNorNIN = ""
    private var mUser: UserEnrollmentDto? = null
    private val ACTIVATE_USB_DEVICE: Int = BASE_EVENT + 1
    private val REMOVE_USB_DEVICE: Int = BASE_EVENT + 2
    private val UPDATE_DEVICE_INFO: Int = BASE_EVENT + 3
    private val REQUEST_USB_PERMISSION: Int = BASE_EVENT + 4
    private val MAKE_DELAY_1SEC: Int = BASE_EVENT + 5
    private val MAKE_TOAST: Int = BASE_EVENT + 11
    private val SHOW_CAPTURE_IMAGE_DEVICE: Int = BASE_EVENT + 12
    var mUsbDevice: UsbDevice? = null
    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    private val REQUEST_WRITE_PERMISSION = 786
    private var isAbortCapturing = false

    lateinit var mContext: Context
    private val mToolbar: Toolbar? = null
    lateinit var mUsbManager: UsbManager
    private var mPermissionIntent: PendingIntent? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fingerprint)
        mContext = this
        mBVNorNIN = intent.getStringExtra("id").toString()
        requestWakeLock()
        mUsbManager = getSystemService(USB_SERVICE) as UsbManager
        initUsbListener()
        addDeviceToUsbDeviceList()
        val backBtn: ImageButton = findViewById(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            captureFingerprint()
        }, 3000)
    }

    override fun onResume() {
        super.onResume()
        // set the scan image to not coloured
        val fingerPrintScan: ImageView = findViewById(R.id.fingerprint_scan)
        val scanner: Drawable? =
            ContextCompat.getDrawable(this, R.drawable.fingerprint_scan)
        fingerPrintScan.setImageDrawable(scanner)

        // set progress text to 0% and mark as done
        val progressText: TextView = findViewById(R.id.progress)
        progressText.setText("0%")

        val progressMark: ImageView = findViewById(R.id.progress_mark)
        progressMark.visibility = View.INVISIBLE
    }

    override fun onPostResume() {
        super.onPostResume()
        Logger.d("START!")
        if (!mWakeLock.isHeld) mWakeLock.acquire()
    }

    override fun onPause() {
        super.onPause()
        Logger.d("START!")
        if (mWakeLock.isHeld) mWakeLock.release()
    }

    override fun onStop() {
        super.onStop()
        Logger.d("START!")
        if (mWakeLock.isHeld) mWakeLock.release()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.d("START!")
        var result = 0
        if (mCurrentDevice != null) {
            if (mCurrentDevice!!.isCapturing) {
                doAbortCapture()
                while (mCurrentDevice!!.isCapturing) {
                    SystemClock.sleep(10)
                }
            }
        }
        if (mBioMiniFactory != null) {
            //if (mUsbDevice != null) result = mBioMiniFactory!!.removeDevice(mUsbDevice)
            if (result == IBioMiniDevice.ErrorCode.OK.value() || result == IBioMiniDevice.ErrorCode.ERR_NO_DEVICE.value()) {
                //mBioMiniFactory!!.close()
                mContext.unregisterReceiver(mUsbReceiver)
                //mUsbDevice = null
                //mCurrentDevice = null
            }
        }
        //FingerPrint.fingerPrintPower(0)
    }

    private fun doAbortCapture() {
        Thread(Runnable {
            if (mCurrentDevice != null) {
                if (mCurrentDevice!!.isCapturing == false) {
                    setLogInTextView("Capture Function is already aborted.")
                    mCaptureOption.captureFuntion = IBioMiniDevice.CaptureFuntion.NONE
                    isAbortCapturing = false
                    return@Runnable
                }
                val result = mCurrentDevice!!.abortCapturing()
                Logger.d("run: abortCapturing : $result")
                if (result == 0) {
                    if (mCaptureOption.captureFuntion != IBioMiniDevice.CaptureFuntion.NONE) setLogInTextView(
                        mCaptureOption.captureFuntion.name + " is aborted."
                    )
                    mCaptureOption.captureFuntion = IBioMiniDevice.CaptureFuntion.NONE
                    isAbortCapturing = false
                } else {
                    if (result == IBioMiniDevice.ErrorCode.ERR_CAPTURE_ABORTING.value()) {
                        setLogInTextView("abortCapture is still running.")
                    } else setLogInTextView("abort capture fail!")
                }
            }
        }).start()
    }

    private fun addDeviceToUsbDeviceList() {
        Logger.d("start!")
        if (mUsbManager == null) {
            Logger.d("mUsbManager is null")
            return
        }
        if (mUsbDevice != null) {
            Logger.d("usbdevice is not null!")
            return
        }
        val deviceList = mUsbManager.getDeviceList()
        val deviceIter: Iterator<UsbDevice> = deviceList.values.iterator()
        while (deviceIter.hasNext()) {
            val _device = deviceIter.next()
            if (_device.vendorId == 0x16d1) {
                Logger.d("found suprema usb device")
                mUsbDevice = _device
                if (!mUsbManager.hasPermission(mUsbDevice)) {
                    Logger.d("This device need to Usb Permission!")
                    mPermissionIntent = PendingIntent.getBroadcast(
                        mContext,
                        0,
                        Intent(ACTION_USB_PERMISSION),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                    requestUsbPermission()
                    Thread.sleep(5000)
                } else {
                    Logger.d("This device alread have USB permission! please activate this device.")
                    //                    _rsApi.deviceAttached(mUsbDevice);
                    mHandler.sendEmptyMessage(ACTIVATE_USB_DEVICE)
                }
            } else {
                Logger.d("This device is not suprema device!  : " + _device.vendorId)
            }
        }
    }

    private fun requestUsbPermission() {
        val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val usbDevice: UsbDevice? = mUsbDevice

        val permissionIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(ACTION_USB_PERMISSION),
            PendingIntent.FLAG_IMMUTABLE
        )

        usbManager.requestPermission(usbDevice, permissionIntent)
    }

    private fun initUsbListener() {
        mContext.registerReceiver(mUsbReceiver, IntentFilter(ACTION_USB_PERMISSION))
        val attachfilter = IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        mContext.registerReceiver(mUsbReceiver, attachfilter)
        val detachfilter = IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED)
        mContext.registerReceiver(mUsbReceiver, detachfilter)
    }


    private val mUsbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (ACTION_USB_PERMISSION == action) {
                synchronized(this) {
                    val usbDevice =
                        intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    val granted =
                        intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    if (granted && usbDevice != null) {
                        // Permission granted, you can now access the USB device
                    } else {
                        // Permission denied, handle accordingly
                    }
                }
            }
        }
    }

    private fun requestWakeLock() {
        Logger.d("START!")
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, ":BioMini WakeLock")
        mWakeLock.acquire()
    }

    @SuppressLint("ResourceType")
    private fun captureFingerprint() {
        doSingleCapture()
        Handler(Looper.getMainLooper()).postDelayed({
            if(mTemplateData != null){
                captureDone()
                Handler(Looper.getMainLooper()).postDelayed({
                    val activity = intent.getStringExtra("activity").toString()
                    val intent = Intent(this@FingerprintEnrollmentActivity, EnrollmentInfoActivity::class.java)
                    intent.putExtra("id", mBVNorNIN).putExtra("activity", activity)
                    startActivity(intent)
                },3000)
            }
            else{
                Toast.makeText(this@FingerprintEnrollmentActivity, "Unable to capture finger, Tap the finger scan to try again", Toast.LENGTH_LONG).show()
                val fingerprintScan = findViewById<ImageView>(R.id.fingerprint_scan)
                fingerprintScan.isClickable = true
                fingerprintScan.setOnClickListener {
                    captureFingerprint()
                }
            }
        }, 5000)
    }



    @SuppressLint("ResourceType")
    private fun doSingleCapture() {
        Logger.d("START!")
        mCaptureStartTime = System.currentTimeMillis()
        mTemplateData = null
        mCaptureOption.captureFuntion = IBioMiniDevice.CaptureFuntion.CAPTURE_SINGLE
        mCaptureOption.extractParam.captureTemplate = true
        mCaptureOption.captureTimeout = 10000
        createBioMiniDevice()
        mCurrentDevice = mBioMiniFactory!!.getDevice(mUsbDevice)
         mCurrentDevice!!.captureSingle(
            mCaptureOption,
            mCaptureCallBack,
            true
        )
    }


    private fun handleDevChange(event: IUsbEventHandler.DeviceChangeEvent, dev: Any) {
        Logger.d("START!")
    }

    var mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                ACTIVATE_USB_DEVICE -> {
                    if (mUsbDevice != null) Logger.d("ACTIVATE_USB_DEVICE : " + mUsbDevice!!.getDeviceName())
                    createBioMiniDevice()
                }

                REMOVE_USB_DEVICE -> {
                    Logger.d("REMOVE_USB_DEVICE")
                }

                UPDATE_DEVICE_INFO -> Logger.d("UPDATE_DEVICE_INFO")
                REQUEST_USB_PERMISSION -> {
                    mPermissionIntent = PendingIntent.getBroadcast(
                        mContext,
                        0,
                        Intent(ACTION_USB_PERMISSION),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                    mUsbManager.requestPermission(mUsbDevice, mPermissionIntent)
                }

                MAKE_DELAY_1SEC -> try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                SET_TEXT_LOGVIEW -> {
                    val _log = msg.obj as String
                    // append the new string
                    // scrollBottom(_log)
                }

                MAKE_TOAST -> {
                    Logger.d("MAKE_TOAST : " + msg.obj as String)
                    Toast.makeText(mContext, msg.obj as String, Toast.LENGTH_SHORT).show()
                }

                SHOW_CAPTURE_IMAGE_DEVICE -> {
                    Logger.d("SHOW_CAPTURE_IMAGE_DEVICE")
                    val _captureImgDev = msg.obj as Bitmap
                    // mImageView.setImageBitmap(_captureImgDev)
                }
            }
        }
    }

    private fun createBioMiniDevice() {
        Logger.d("START!")
        if (mUsbDevice == null) {
            //setLogInTextView(getResources().getString(R.string.error_device_not_conneted))
            return
        }
        mBioMiniFactory?.close()
        Logger.d("new BioMiniFactory( )")
        mBioMiniFactory = object : BioMiniFactory(mContext, mUsbManager) {
            //for android sample
            override fun onDeviceChange(event: IUsbEventHandler.DeviceChangeEvent, dev: Any) {
                Logger.d("onDeviceChange : $event")
                handleDevChange(event, dev)
            }
        }
        Logger.d("new BioMiniFactory( ) : $mBioMiniFactory")
        (mBioMiniFactory as BioMiniFactory).setTransferMode(IBioMiniDevice.TransferMode.MODE1)
        val _result: Boolean = (mBioMiniFactory as BioMiniFactory).addDevice(mUsbDevice)
        if (_result) {
            if (mCurrentDevice != null) {
                //setLogInTextView(getResources().getString(R.string.device_attached))
                Logger.d("mCurrentDevice attached : $mCurrentDevice")
                runOnUiThread {
                    if (mCurrentDevice != null /*&& mCurrentDevice.getDeviceInfo() != null*/) {
                        mToolbar?.setTitle(mCurrentDevice!!.deviceInfo.deviceName)
                        requestWritePermission()
                    }
                }
                mCurrentDevice = (mBioMiniFactory as BioMiniFactory).getDevice(mUsbDevice)
                mCurrentDevice!!.setParameter(
                    IBioMiniDevice.Parameter(
                        IBioMiniDevice.ParameterType.TIMEOUT,
                        10000
                    )
                )
            } else {
                Logger.d("mCurrentDevice is null")
            }
        } else {
            Logger.d("addDevice is fail!")
        }
    }

    private fun requestWritePermission() {
        Logger.d("start!")
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_WRITE_PERMISSION
            )
        } else {
            Logger.d("WRITE_EXTERNAL_STORAGE permission already granted!")
            requestBatteryOptimization()
        }
    }

    private fun requestBatteryOptimization() {
        val intent = Intent()
        val packageName = packageName
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.setData(Uri.parse("package:$packageName"))
            startActivity(intent)
        }
    }

    private fun captureDone(){
        // set the scan image to coloured
        val fingerPrintScan: ImageView = findViewById(R.id.fingerprint_scan)
        val fingerPrintScanColoured: Drawable? =
            ContextCompat.getDrawable(this, R.drawable.fingerprint_coloured)
        fingerPrintScan.setImageDrawable(fingerPrintScanColoured)

        // set progress text to 100% and mark as done
        val progressText: TextView = findViewById(R.id.progress)
        progressText.setText("100%")

        val progressMark: ImageView = findViewById(R.id.progress_mark)
        progressMark.visibility = View.VISIBLE

        return
    }

    var mCaptureCallBack: CaptureResponder = object : CaptureResponder() {

        override fun onCaptureEx(
            context: Any,
            option: IBioMiniDevice.CaptureOption,
            capturedImage: Bitmap,
            capturedTemplate: IBioMiniDevice.TemplateData,
            fingerState: IBioMiniDevice.FingerState
        ): Boolean {
            Logger.d("START! : " + mCaptureOption.captureFuntion.toString())
            Logger.d("TemplateData is not null!")
            mTemplateData = capturedTemplate
            if (option.captureFuntion == IBioMiniDevice.CaptureFuntion.CAPTURE_SINGLE) {
                Logger.d("register user template data.")
                // check if user exits
                mUser = UserEnrollmentDto(mBVNorNIN, mTemplateData!!.data.size, mTemplateData!!.data)
                SharedPreferencesHelper.storeObject(this@FingerprintEnrollmentActivity, mBVNorNIN, mUser)
            }

            return mTemplateData != null
        }

        override fun onCaptureError(context: Any, errorCode: Int, error: String) {
            if (errorCode == IBioMiniDevice.ErrorCode.CTRL_ERR_IS_CAPTURING.value()) {
                setLogInTextView("Other capture function is running. abort capture function first!")
            } else if (errorCode == IBioMiniDevice.ErrorCode.CTRL_ERR_CAPTURE_ABORTED.value()) {
                Logger.d("CTRL_ERR_CAPTURE_ABORTED occured.")
            } else if (errorCode == IBioMiniDevice.ErrorCode.CTRL_ERR_FAKE_FINGER.value()) {
                setLogInTextView("Fake Finger Detected")
                if (mCurrentDevice != null && mCurrentDevice!!.lfdLevel > 0) {
                   // setLogInTextView("LFD SCORE : " + mCurrentDevice!!.lfdScoreFromCapture)
                }
            } else {
               setLogInTextView(mCaptureOption.captureFuntion.name + " is fail by " + error)
               setLogInTextView("Please try again.")
            }
        }
    }

    @Synchronized
    fun setLogInTextView(msg: String?) {
        sendMsgToHandler(SET_TEXT_LOGVIEW, msg!!)
    }

    private fun sendMsgToHandler(what: Int, msgToSend: String) {
        val msg = Message()
        msg.what = what
        msg.obj = msgToSend
        mHandler.sendMessage(msg)
    }


}