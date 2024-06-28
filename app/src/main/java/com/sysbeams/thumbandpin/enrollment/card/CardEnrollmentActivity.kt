package com.sysbeams.thumbandpin.enrollment.card

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import com.common.sdk.emv.PinpadBytesOut
import com.common.sdk.emv.PinpadEnum
import com.common.sdk.emv.PinpadService
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.sysbeams.thumbandpin.R
import com.telpo.emv.EmvAmountData
import com.telpo.emv.EmvCandidateApp
import com.telpo.emv.EmvOnlineData
import com.telpo.emv.EmvParam
import com.telpo.emv.EmvPinData
import com.telpo.emv.EmvService
import com.telpo.emv.EmvServiceListener
import com.telpo.emv.EmvTLV
import com.telpo.util.StringUtil
import java.io.UnsupportedEncodingException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.locks.ReentrantLock

class CardEnrollmentActivity: ComponentActivity() {
    private var STATUS: Int = 0;
    private var IS_READ: Boolean = false
    lateinit var emvService: EmvService
    private val TAG = "MainActivity"

    //The last result
    var _LastCode = 0

    //Payment amount
    var amount = 0.00

    //Display message buffer
    var DisplayBuf = StringBuffer("")

    //context
    var context: Context? = null
    //Pinpad service

    //Pinpad service
    private lateinit var pinpadService: PinpadService

    var et_MsgView: EditText? = null
    var bt_confirm: MaterialButton? = null

    //Process dialog
    var processDialog: AlertDialog? = null

    //Show pan message dialog
    var panDialog: AlertDialog? = null

    //Select app result
    var selectAPPResult = 0

    //whether the UI thread is running
    var UIThreadisRunning = true


    //PinKey index
    var PIN_KEY_INDEX = 1

    //PanKey index
    var PAN_KEY_INDEX = 2

    //macKey index
    var MAC_KEY_INDEX = 3

    //Dukpt's current KSN
    var currentKSN = ""

    //PAN Dukpt's current KSN
    var PanCurrentKSN = ""

    //Is the device initialized succ
    var isDevInit = false

    //MK/SK mode or not(false:DUKPT mode)
    var isMkMode = true

    //DES mode or not(fales:AES mode)
    var isDesMode = true

    //Is pan encryption in Des mode
    var isPanDesMode = true

    //is pan encryption in mk/sk mode
    var isPanMkMode = true

    //PIN Block
    var pinBlock = ""

    //PIN format（Des：0、1、3；Aes：4）
    //PAN
    var cardNum = ""

    //Is online transaction or not
    var isOnlineTransaction = true

    //The signal to stop detect
    var stopDetect = false

    //Is NFC card
    var isNFC = false

    //Gson
    private val gson: Gson? = null

    //Track1 data
    var Track1: String? = null

    //Track2 data
    var Track2: String? = null

    //Is Mag card
    var isMag = false

    //If no err ,go to pay
    var isNoErr = true

    //locker
    var ThreadLock = ReentrantLock()

    //terminal mode
    var TerminalMode = 29 //set the default terminal mode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_enrollment)

        InitPinPad();

        val builder = MaterialAlertDialogBuilder(this)
        panDialog = builder.setTitle("Please Input PIN")
            .setCancelable(false)
            .create()

        //Init the EmvService
        emvService = EmvService.getInstance()
        STATUS = EmvService.Open(this)
        if(EmvService.EMV_DEVICE_TRUE == EmvService.deviceOpen()){
            STATUS = EmvService.IccOpenReader()
            if(EmvService.EMV_DEVICE_TRUE == STATUS){
                IS_READ = EmvService.IccCheckCard(300) == EmvService.EMV_DEVICE_TRUE
                if (IS_READ) {
                    EmvService.IccCard_Poweron()
                    emvService.setListener(cardListener)
                    startIcTransaction()
                    EmvService.IccCard_Poweroff()
                    Toast.makeText(this@CardEnrollmentActivity, "Card Read: ${cardNum}", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@CardEnrollmentActivity, "No card detected.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun InitPinPad(): Int {
        pinpadService = PinpadService(context)
        return pinpadService.Pinpad_Open(context)
    }

    fun changePanUIVisibility(isShow: Boolean, text: String?) {
        this.runOnUiThread(Runnable {
            if (isShow) {
                panDialog!!.setMessage(text)
                panDialog!!.window!!.setGravity(Gravity.TOP)
                panDialog!!.show()
            } else {
                if (panDialog!!.isShowing) {
                    panDialog!!.cancel()
                }
            }
        })
    }

    private fun startIcTransaction() {
        _LastCode = emvService.Emv_TransInit()
        if (EmvService.EMV_TRUE != _LastCode) {
            return
        }
        val param = EmvParam()
        param.Capability = byteArrayOf(0xE0.toByte(), 0xF9.toByte(), 0xC8.toByte())
        _LastCode = emvService.Emv_SetParam(param)
        if (EmvService.EMV_TRUE != _LastCode) {
            return
        }
        _LastCode = emvService.Emv_StartApp(0)
        if (EmvService.EMV_TRUE == _LastCode) {
        } else {
            isNoErr = false
        }
        changePanUIVisibility(false, null)
        if (!isOnlineTransaction) {
            //upload offline transaction data
            pay()
        }
//        if (processDialog!!.isShowing) {
//            processDialog!!.dismiss()
//        }
    }

    fun pay(): Boolean {
        return true
    }

    private var cardListener: EmvServiceListener = object : EmvServiceListener() {
        override fun onInputAmount(emvAmountData: EmvAmountData): Int {
            emvAmountData.Amount = (amount * 100).toLong()
            emvAmountData.TransCurrCode = 156
            emvAmountData.ReferCurrCode = 156
            emvAmountData.TransCurrExp = 2
            emvAmountData.ReferCurrExp = 2
            emvAmountData.ReferCurrCon = 1
            emvAmountData.CashbackAmount = 0
            return EmvService.EMV_TRUE
        }

        override fun onInputPin(emvPinData: EmvPinData): Int {
            changePanUIVisibility(false, null)
            var result = 0
            val hidePan: String = cardNum.substring(0, 6) + "******" + cardNum.substring(
                cardNum.length - 4,
                cardNum.length
            )
            changePanUIVisibility(true, "PAN:$hidePan")
            if (emvPinData.type == EmvService.ONLIEN_ENCIPHER_PIN) {
                //online PIN
                if (isMkMode) {
                    //MK/SK mode
                    pinBlock = getMkPin(cardNum)
                } else {
                    //Dukpt mode
                    pinBlock = getDukptPin(cardNum)
                }
            } else {
                //offline PIN（Emv lib calls pinpad,application does not handle that）
                Log.i(TAG, "offline PIN!")
                return EmvService.EMV_TRUE
            }
            if (_LastCode == PinpadService.PIN_ERROR_TIMEOUT) {
                result = EmvService.ERR_TIMEOUT
            } else if (_LastCode == PinpadService.PIN_ERROR_CANCEL) {
                result = EmvService.ERR_USERCANCEL
            } else if (_LastCode == PinpadService.PIN_ERROR_NOKEY) {
                result = EmvService.ERR_NOPIN
            } else if (_LastCode == PinpadService.PIN_OK) {
                result = EmvService.EMV_TRUE
                emvPinData.Pin = StringUtil.hexStringToByte(pinBlock)
            } else {
                isNoErr = false
                return result
            }
            return result
        }

        override fun onSelectApp(p0: Array<out EmvCandidateApp>?): Int {
            TODO("Not yet implemented")
        }

        override fun onSelectAppFail(p0: Int): Int {
            TODO("Not yet implemented")
        }


        override fun onFinishReadAppData(): Int {
            var ret = 0
            var tlv = EmvTLV(0x9F06)
            emvService.Emv_GetTLV(tlv)
            tlv = EmvTLV(0x5A)
            ret = emvService.Emv_GetTLV(tlv)
            if (EmvService.EMV_TRUE == ret) {
                cardNum = StringUtil.bytesToHexString(tlv.Value).replace("F", "")
            } else {
                tlv = EmvTLV(0x57)
                ret = emvService.Emv_GetTLV(tlv)
                if (EmvService.EMV_TRUE == ret) {
                    val str_57 = StringUtil.bytesToHexString(tlv.Value)
                    cardNum = str_57.substring(0, str_57.indexOf('D'))
                } else {
                }
            }
            if (!(null == cardNum || cardNum.isEmpty())) {
            }
            return EmvService.EMV_TRUE
        }

        override fun onVerifyCert(): Int {
            TODO("Not yet implemented")
        }



        override fun onOnlineProcess(emvOnlineData: EmvOnlineData): Int {
            if (null == emvOnlineData) {
                return EmvService.ONLINE_FAILED.toInt()
            }
            isOnlineTransaction = true
            if ((null == pinBlock || pinBlock.isEmpty()) && isNFC) {
                isNFC = false
                //when the contactless card does not go onInputPin callback,force input online PIN
                val hidePan: String = cardNum.substring(0, 6) + "******" + cardNum.substring(
                    cardNum.length - 4,
                    cardNum.length
                )
                changePanUIVisibility(true, "PAN:$hidePan")
                //online PIN
                if (isMkMode) {
                    //MK/SK mode
                    pinBlock = getMkPin(cardNum)
                } else {
                    //Dukpt mode
                    pinBlock = getDukptPin(cardNum)
                }
                if ("" === pinBlock) {
                    isNoErr = false
                    changePanUIVisibility(false, null)
                    return EmvService.ONLINE_FAILED.toInt()
                }
            }
            changePanUIVisibility(false, null)
            return if (pay()) {
                emvOnlineData.ResponeCode = "00".toByteArray()
                EmvService.ONLINE_APPROVE.toInt()
            } else {
                EmvService.ONLINE_FAILED.toInt()
            }
        }

        override fun onRequireTagValue(i: Int, i1: Int, bytes: ByteArray): Int {
            return 0
        }

        override fun onRequireDatetime(bytes: ByteArray): Int {
            val formatter = SimpleDateFormat("yyyyMMddHHmmss")
            val curDate = Date(System.currentTimeMillis()) //Get the current time
            val str = formatter.format(curDate)
            var time = ByteArray(0)
            return try {
                time = str.toByteArray(charset("ascii"))
                System.arraycopy(time, 0, bytes, 0, bytes.size)
                EmvService.EMV_TRUE
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                Log.e("MyEmvService", "onRequireDatetime failed")
                EmvService.EMV_FALSE
            }
        }

        override fun onReferProc(): Int {
            TODO("Not yet implemented")
        }

        override fun OnCheckException(s: String): Int {
            return EmvService.EMV_FALSE
        }

        override fun OnCheckException_qvsdc(i: Int, s: String): Int {
            return EmvService.EMV_FALSE
        }
    }

    /**
     * get PIN with MK/SK
     * @param
     * @return String result PinBlockStr
     */
    fun getMkPin(cardNum: String?): String {
        var PinBlockStr = ""
        val pinBlock = PinpadBytesOut()
        _LastCode = pinpadService.Pinpad_GetPin(
            PIN_KEY_INDEX,
            cardNum,
            PinpadEnum.ENUM_PIN_BLOCK_FORMAT.ISO_9564_FORMAT_0,
            12,
            4,
            60,
            pinBlock
        )
        if (_LastCode != PinpadService.PIN_OK) {
            isNoErr = false
        } else {
            PinBlockStr = StringUtil.bytesToHexString(pinBlock.outResult)
        }
        return PinBlockStr
    }

    /**
     * get PIN with DUKPT
     * @param
     * @return String result PinBlockStr
     */
    fun getDukptPin(cardNum: String?): String {
        var PinBlockStr = ""
        val ksn = PinpadBytesOut()
        if (isDesMode) {
            //start session
            _LastCode = pinpadService.Pinpad_DEA_DUKPT_Session_Start(11, ksn)
            if (PinpadService.PIN_OK == _LastCode) {
                currentKSN = StringUtil.bytesToHexString(ksn.outResult)
                Log.i(TAG, "KSN:$currentKSN")
                //DES mode
                val pinBlock = PinpadBytesOut()
                _LastCode = pinpadService.Pinpad_DEA_DUKPT_GetPin(
                    cardNum,
                    PinpadEnum.ENUM_PIN_BLOCK_FORMAT.ISO_9564_FORMAT_0,
                    12,
                    4,
                    60,
                    pinBlock
                )
                if (PinpadService.PIN_OK == _LastCode) {
                    PinBlockStr = StringUtil.bytesToHexString(pinBlock.outResult)
                } else {
                    isNoErr = false
                }
            } else {
                isNoErr = false
            }
            pinpadService.Pinpad_DEA_DUKPT_Session_End()
        } else {
            //start session
            _LastCode = pinpadService!!.Pinpad_AES_DUKPT_Session_Start(11, ksn)
            if (PinpadService.PIN_OK == _LastCode) {
                currentKSN = StringUtil.bytesToHexString(ksn.outResult)
                Log.i(TAG, "KSN:$currentKSN")
                //AES mode
                val pinBlock = PinpadBytesOut()
                _LastCode = pinpadService.Pinpad_AES_DUKPT_GetPin(
                    PinpadEnum.ENUM_AES_DUKPT_KeyUsage._PINEncryption,
                    cardNum,
                    12,
                    4,
                    60,
                    pinBlock
                )
                if (PinpadService.PIN_OK == _LastCode) {
                    PinBlockStr = StringUtil.bytesToHexString(pinBlock.outResult)
                } else {
                }
            } else {
            }
            pinpadService!!.Pinpad_AES_DUKPT_Session_End()
        }
        return PinBlockStr
    }
}