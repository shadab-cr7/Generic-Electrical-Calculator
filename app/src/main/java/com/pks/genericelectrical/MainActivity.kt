package com.pks.genericelectrical

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.pks.genericelectrical.database.BillDetails
import com.pks.genericelectrical.database.BillRepo
import com.pks.genericelectrical.database.ElectricityDatabase
import com.pks.genericelectrical.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Matcher
import java.util.regex.Pattern


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var repository: BillRepo
    private lateinit var activity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        activity = this@MainActivity
        try {
            val billDao = ElectricityDatabase.getDataBase(activity).getUserDao()
            repository = BillRepo(billDao)
        } catch (er: Exception) {
            logData(er.message.toString())
        }

        binding.btnSubmit.setOnClickListener {
            if (checkValidations()) {
                checkDBValidationsAndProceed(
                    binding.textInputEditTextEnterCustomerServiceNumber.text.toString().trim(),
                    binding.textInputEditTextEnterMeterReading.text.toString().trim()
                )

            }
        }

    }

    private fun checkDBValidationsAndProceed(serviceNumber: String, meterReading: String) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val list =
                    repository.getCheckSingleCustomer(serviceNumber)
                if (list.isEmpty()) {
//                    no entry found for this customer
//                    repository.addBill(BillDetails(0, serviceNumber, meterReading))
                    sendToNextActivity(activity, serviceNumber, meterReading, "0")
                } else {
//                    checking units validation with currently entered units
                    if (meterReading.toDouble() > list[0].meter_reading.toDouble()) {
//                        repository.addBill(BillDetails(0, serviceNumber, meterReading))
                        sendToNextActivity(
                            activity,
                            serviceNumber,
                            meterReading,
                            list[0].meter_reading
                        )
                    } else {
                        activity.runOnUiThread {
                            binding.textInputLayoutEnterMeterReading.error =
                                resources.getString(R.string.txt_please_enter_meter_valid_reading)
                                    .trim()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logData("Exception while checkDBValidations -${e.localizedMessage}")
        }
    }

    private fun checkValidations(): Boolean {
        try {
            removeErrorFromTextInputLayout(binding.linearLayoutCustomerServiceNumber)
            if (TextUtils.isEmpty(
                    binding.textInputEditTextEnterCustomerServiceNumber.text.toString().trim()
                )
            ) {
                binding.textInputLayoutEnterCustomerServiceNumber.error =
                    resources.getString(R.string.txt_please_enter_customer_service_number)
                        .toString().trim()
                return false
            } else if (
                binding.textInputEditTextEnterCustomerServiceNumber.text.toString()
                    .trim().length != 10

            ) {
                binding.textInputLayoutEnterCustomerServiceNumber.error =
                    resources.getString(R.string.txt_customer_service_number_should_be)
                        .toString().trim()
                return false
            } else if (
                !isAlphaNumeric(
                    binding.textInputEditTextEnterCustomerServiceNumber.text.toString()
                        .trim()
                )

            ) {
                binding.textInputLayoutEnterCustomerServiceNumber.error =
                    resources.getString(R.string.txt_customer_service_number_not_valid)
                        .toString().trim()
                return false
            } else if (TextUtils.isEmpty(
                    binding.textInputEditTextEnterMeterReading.text.toString().trim()
                )
            ) {
                binding.textInputLayoutEnterMeterReading.error =
                    resources.getString(R.string.txt_please_enter_meter_reading)
                        .toString().trim()
                return false
            } else if (binding.textInputEditTextEnterMeterReading.text.toString().toDouble() <= 0) {
                binding.textInputLayoutEnterMeterReading.error =
                    resources.getString(R.string.txt_please_enter_meter_valid_reading)
                        .toString().trim()
                return false
            } else return true
        } catch (e: Exception) {
            logData(e.toString())
            return false
        }
    }

    fun logData(logData: String) {
        Log.v("logData", logData)
    }

    fun removeErrorFromTextInputLayout(linearLayout: LinearLayout) {
        try {

            for (i in 0 until linearLayout.childCount) {
                val view: View = linearLayout.getChildAt(i)
                if (view is TextInputLayout) {
                    view.error = null
                    view.error = ""
                    view.isErrorEnabled = false
                }

            }


        } catch (e: Exception) {
            e.message?.let { logData(it) }
        }
    }

    fun sendToNextActivity(
        activity: Activity,
        serviceNumber: String,
        meterReading: String,
        lastMeterReading: String
    ) {
        val intent = Intent(activity, ConsumptionDetailsActivity::class.java)
        intent.putExtra(Constants.mServiceNumber, serviceNumber)
        intent.putExtra(Constants.mEnteredMeterReading, meterReading)
        intent.putExtra(Constants.mLastMeterReading, lastMeterReading)
        activity.startActivity(intent)
    }

    fun isAlphaNumeric(str: String?): Boolean {
        // Regex to check string is alphanumeric or not.
        val regex = "^(?=.*[a-zA-Z])(?=.*[0-9])[A-Za-z0-9]+$"

        // Compile the ReGex
        val p: Pattern = Pattern.compile(regex)

        // If the string is empty
        // return false
        if (str == null) {
            return false
        }

        // Pattern class contains matcher() method
        // to find matching between given string
        // and regular expression.
        val m: Matcher = p.matcher(str)

        // Return if the string
        // matched the ReGex
        return m.matches()
    }
}