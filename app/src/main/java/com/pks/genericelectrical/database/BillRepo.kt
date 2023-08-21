package com.pks.genericelectrical.database

class BillRepo(private val electricityDao: ElectricityDao) {

    suspend fun addBill(bill: BillDetails) {
        electricityDao.addBill(bill)
    }

    fun getAllBillsForCustomer(customerServiceNo: String): List<BillDetails> {
        return electricityDao.getAllBillsForCustomer(customerServiceNo)
    }

    fun getCheckSingleCustomer(customerServiceNo: String): List<BillDetails> {
        return electricityDao.getCheckSingleCustomer(customerServiceNo)
    }
}