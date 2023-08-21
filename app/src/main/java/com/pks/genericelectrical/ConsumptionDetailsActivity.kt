package com.pks.genericelectrical

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pks.genericelectrical.database.BillDetails
import com.pks.genericelectrical.database.BillRepo
import com.pks.genericelectrical.database.ElectricityDatabase
import com.pks.genericelectrical.databinding.ActivityConsumptiondetailsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConsumptionDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConsumptiondetailsBinding
    private var mServiceNumber: String = ""
    private var mEnteredMeterReading: String = ""
    private var mLastMeterReading: String = "0"
    private lateinit var repository: BillRepo
    private lateinit var activity: Activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityConsumptiondetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        activity = this@ConsumptionDetailsActivity
        try {

            val billDao = ElectricityDatabase.getDataBase(activity).getUserDao()
            repository = BillRepo(billDao)
        } catch (er: Exception) {
            logData(er.message.toString())
        }
        intent.getStringExtra(Constants.mServiceNumber).also {
            if (it != null) {
                mServiceNumber = it
            }
        }
        intent.getStringExtra(Constants.mEnteredMeterReading).also {
            if (it != null) {
                mEnteredMeterReading = it
            }
        }
        intent.getStringExtra(Constants.mLastMeterReading).also {
            if (it != null && it != "0") {
                mLastMeterReading = it
            }
        }
        getPreviousBillsFromData(mServiceNumber)
        binding.textInputEditTextCurrentMeterReading.setText(mEnteredMeterReading)
        binding.textInputEditTextConsumptionCost.setText(calculateBill(mEnteredMeterReading.toDouble() - mLastMeterReading.toDouble()).toString())
        binding.btnSave.setOnClickListener {
//            save the bill details in DB
            addBillToDB(BillDetails(0, mServiceNumber, mEnteredMeterReading))
        }
    }

    private fun addBillToDB(billDetails: BillDetails) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                if (billDetails != null) {
                    repository.addBill(billDetails)
                    activity.runOnUiThread {
                        Toast.makeText(
                            activity,
                            R.string.txt_bill_details_saved_successfully,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    val intent = Intent(activity, MainActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    activity.startActivity(intent)
                }
            }
        } catch (e: Exception) {
            logData("Exception while addBillToDB -${e.localizedMessage}")
        }
    }

    private fun getPreviousBillsFromData(fromServiceNumber: String) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val list: ArrayList<BillDetails> =
                    repository.getAllBillsForCustomer(fromServiceNumber) as ArrayList<BillDetails>
                if (list.isEmpty()) {
                    binding.tableLinearLayout.visibility = View.GONE
                } else {
                    if (list.size > 3) {
                        val sublist: MutableList<BillDetails> = list.subList(0, 3)
                        setDataForTable(sublist, list[3].meter_reading)
                    } else {
                        setDataForTable(list, "0")
                    }
                }
            }
        } catch (e: Exception) {
            logData("Exception while getPreviousBillsFromData -${e.localizedMessage}")
        }
    }

    private fun setDataForTable(list: MutableList<BillDetails>, fourthReading: String) {
        try {
            val adapter = BillHistoryAdapter()
            adapter.setData(activity, list, fourthReading)
            binding.recyclerview.adapter = adapter
        } catch (e: Exception) {
            logData("Exception while setDataForTable -${e.localizedMessage}")
        }
    }

    fun logData(logData: String) {
        Log.v("logData", logData)
    }

    private fun calculateBill(units: Double): Double {

        if (units <= 100) {
            return units * Constants.unitRate1st
        } else if (units <= 500) {
            return (100 * Constants.unitRate1st
                    + (units - 100)
                    * Constants.unitRate2nd)
        } else if (units > 500) {
            return (100 * Constants.unitRate1st + 400 * Constants.unitRate2nd
                    + ((units - 500)
                    * Constants.unitRate3rd))
        } else {
            return 0.00
        }
    }
}