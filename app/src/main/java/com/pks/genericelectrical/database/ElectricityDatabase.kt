package com.pks.genericelectrical.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [BillDetails::class], version = 1, exportSchema = false)
abstract class ElectricityDatabase : RoomDatabase() {
    abstract fun getUserDao(): ElectricityDao

    companion object {
        @Volatile
        private var INSTANCE: ElectricityDatabase? = null
        fun getDataBase(context: Context): ElectricityDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ElectricityDatabase::class.java,
                    "electricity_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}