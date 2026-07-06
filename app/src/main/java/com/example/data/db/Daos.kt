package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DepartureSchedule
import com.example.data.model.Pilgrim
import com.example.data.model.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface DepartureScheduleDao {
    @Query("SELECT * FROM departure_schedules ORDER BY departureDate ASC")
    fun getAllSchedules(): Flow<List<DepartureSchedule>>

    @Query("SELECT * FROM departure_schedules WHERE id = :id")
    suspend fun getScheduleById(id: Int): DepartureSchedule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: DepartureSchedule): Long

    @Update
    suspend fun updateSchedule(schedule: DepartureSchedule)

    @Query("DELETE FROM departure_schedules WHERE id = :id")
    suspend fun deleteScheduleById(id: Int)
}

@Dao
interface PilgrimDao {
    @Query("SELECT * FROM pilgrims ORDER BY name ASC")
    fun getAllPilgrims(): Flow<List<Pilgrim>>

    @Query("SELECT * FROM pilgrims WHERE scheduleId = :scheduleId ORDER BY name ASC")
    fun getPilgrimsBySchedule(scheduleId: Int): Flow<List<Pilgrim>>

    @Query("SELECT * FROM pilgrims WHERE id = :id")
    suspend fun getPilgrimById(id: Int): Pilgrim?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPilgrim(pilgrim: Pilgrim): Long

    @Update
    suspend fun updatePilgrim(pilgrim: Pilgrim)

    @Query("DELETE FROM pilgrims WHERE id = :id")
    suspend fun deletePilgrimById(id: Int)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE pilgrimId = :pilgrimId ORDER BY paymentDate DESC")
    fun getPaymentsByPilgrim(pilgrimId: Int): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deletePaymentById(id: Int)

    @Query("DELETE FROM payments WHERE pilgrimId = :pilgrimId")
    suspend fun deletePaymentsByPilgrimId(pilgrimId: Int)
}
