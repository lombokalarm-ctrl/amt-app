package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "departure_schedules")
data class DepartureSchedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val departureDate: String, // format YYYY-MM-DD
    val price: Double,
    val quota: Int,
    val packageType: String, // "UMROH" or "HAJI"
    val hotelMekkah: String,
    val hotelMadinah: String,
    val airline: String
)

@Entity(tableName = "pilgrims")
data class Pilgrim(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val passportNumber: String,
    val scheduleId: Int?, // foreign key linking to DepartureSchedule.id
    val status: String, // "Pendaftaran", "Dokumen", "Paspor", "Visa", "Siap Berangkat"
    val notes: String,
    val registrationDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pilgrimId: Int, // links to Pilgrim.id
    val amount: Double,
    val paymentDate: Long = System.currentTimeMillis(),
    val paymentMethod: String, // "Transfer Bank", "Tunai", "Debit"
    val paymentType: String, // "DP", "Cicilan", "Pelunasan"
    val notes: String
)
