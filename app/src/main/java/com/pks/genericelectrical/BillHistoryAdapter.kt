package com.pks.genericelectrical

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pks.genericelectrical.database.BillDetails
import com.pks.genericelectrical.databinding.BillHistoryItemBinding

class BillHistoryAdapter : RecyclerView.Adapter<BillHistoryAdapter.ViewHolder>() {
    lateinit var context: Context
    private var fourthReading: String = "0"
    private lateinit var mDataList: MutableList<BillDetails>

    @SuppressLint("NotifyDataSetChanged")
    fun setData(
        context: Context,
        list: MutableList<BillDetails>,
        fourthReading: String
    ) {
        this.fourthReading = fourthReading
        this.context = context
        this.mDataList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = BillHistoryItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    class ViewHolder(val binding: BillHistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {

            val billDetails = mDataList[position]
            holder.binding.tvCustomerServiceNumber.text = billDetails.customer_serial_number
            holder.binding.tvUnits.text = billDetails.meter_reading

            try {
                holder.binding.tvConsumptionCost.text =
                    calculateBill(billDetails.meter_reading.toDouble() - mDataList[position + 1].meter_reading.toDouble()).toString()
            } catch (e: Exception) {
                if (fourthReading == "0") {
                    holder.binding.tvConsumptionCost.text =
                        calculateBill(billDetails.meter_reading.toDouble() - 0.00).toString()
                } else {
                    holder.binding.tvConsumptionCost.text =
                        calculateBill(billDetails.meter_reading.toDouble() - fourthReading.toDouble()).toString()
                }
            }
        } catch (e: Exception) {
            logData("Exception while onBindViewHolder -${e.localizedMessage}")
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
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

    fun logData(logData: String) {
        Log.v("logData", logData)
    }
}
