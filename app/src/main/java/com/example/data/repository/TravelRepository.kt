package com.example.data.repository

import com.example.data.db.DepartureScheduleDao
import com.example.data.db.PilgrimDao
import com.example.data.db.PaymentDao
import com.example.data.model.DepartureSchedule
import com.example.data.model.Pilgrim
import com.example.data.model.Payment
import kotlinx.coroutines.flow.Flow

class TravelRepository(
    private val scheduleDao: DepartureScheduleDao,
    private val pilgrimDao: PilgrimDao,
    private val paymentDao: PaymentDao
) {
    val allSchedules: Flow<List<DepartureSchedule>> = scheduleDao.getAllSchedules()
    val allPilgrims: Flow<List<Pilgrim>> = pilgrimDao.getAllPilgrims()
    val allPayments: Flow<List<Payment>> = paymentDao.getAllPayments()

    suspend fun getScheduleById(id: Int): DepartureSchedule? {
        return scheduleDao.getScheduleById(id)
    }

    suspend fun getPilgrimById(id: Int): Pilgrim? {
        return pilgrimDao.getPilgrimById(id)
    }

    fun getPilgrimsBySchedule(scheduleId: Int): Flow<List<Pilgrim>> {
        return pilgrimDao.getPilgrimsBySchedule(scheduleId)
    }

    fun getPaymentsByPilgrim(pilgrimId: Int): Flow<List<Payment>> {
        return paymentDao.getPaymentsByPilgrim(pilgrimId)
    }

    suspend fun insertSchedule(schedule: DepartureSchedule): Long {
        return scheduleDao.insertSchedule(schedule)
    }

    suspend fun updateSchedule(schedule: DepartureSchedule) {
        scheduleDao.updateSchedule(schedule)
    }

    suspend fun deleteSchedule(scheduleId: Int) {
        scheduleDao.deleteScheduleById(scheduleId)
    }

    suspend fun insertPilgrim(pilgrim: Pilgrim): Long {
        return pilgrimDao.insertPilgrim(pilgrim)
    }

    suspend fun updatePilgrim(pilgrim: Pilgrim) {
        pilgrimDao.updatePilgrim(pilgrim)
    }

    suspend fun deletePilgrim(pilgrimId: Int) {
        paymentDao.deletePaymentsByPilgrimId(pilgrimId)
        pilgrimDao.deletePilgrimById(pilgrimId)
    }

    suspend fun insertPayment(payment: Payment): Long {
        return paymentDao.insertPayment(payment)
    }

    suspend fun deletePayment(paymentId: Int) {
        paymentDao.deletePaymentById(paymentId)
    }
}
