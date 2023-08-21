package com.pks.genericelectrical.database

import androidx.room.*

@Dao
interface ElectricityDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addBill(billDetails: BillDetails)

    @Query("select * from electricity_table where customer_serial_number =:customerServiceNo order by id desc limit 4")
    fun getAllBillsForCustomer(customerServiceNo:String): List<BillDetails>


    @Query("select * from electricity_table where customer_serial_number =:customerServiceNo order by id desc limit 1")
    fun getCheckSingleCustomer(customerServiceNo:String): List<BillDetails>
}