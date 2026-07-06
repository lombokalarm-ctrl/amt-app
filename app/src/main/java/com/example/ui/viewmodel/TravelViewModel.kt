package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.DepartureSchedule
import com.example.data.model.Pilgrim
import com.example.data.model.Payment
import com.example.data.repository.TravelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TravelViewModel(private val repository: TravelRepository) : ViewModel() {

    // Main Flows from repository
    val schedules: StateFlow<List<DepartureSchedule>> = repository.allSchedules
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pilgrims: StateFlow<List<Pilgrim>> = repository.allPilgrims
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val payments: StateFlow<List<Payment>> = repository.allPayments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Pre-populate data if the database is currently empty
        viewModelScope.launch {
            val existingSchedules = repository.allSchedules.first()
            if (existingSchedules.isEmpty()) {
                seedInitialData()
            }
        }
    }

    private suspend fun seedInitialData() {
        // 1. Add Schedules
        val s1Id = repository.insertSchedule(
            DepartureSchedule(
                title = "Umroh VIP Ramadhan (9 Hari)",
                departureDate = "2026-10-12",
                price = 38500000.0,
                quota = 45,
                packageType = "UMROH",
                hotelMekkah = "Pullman Zamzam Makkah",
                hotelMadinah = "Al Haram Madinah",
                airline = "Saudi Arabian Airlines"
            )
        )

        val s2Id = repository.insertSchedule(
            DepartureSchedule(
                title = "Haji Khusus VIP 2026 (25 Hari)",
                departureDate = "2026-11-20",
                price = 195000000.0,
                quota = 20,
                packageType = "HAJI",
                hotelMekkah = "Fairmont Makkah Clock Royal",
                hotelMadinah = "The Oberoi Madinah",
                airline = "Garuda Indonesia"
            )
        )

        val s3Id = repository.insertSchedule(
            DepartureSchedule(
                title = "Umroh Hemat Awal Musim (9 Hari)",
                departureDate = "2026-12-05",
                price = 28000000.0,
                quota = 50,
                packageType = "UMROH",
                hotelMekkah = "Anjum Hotel Makkah",
                hotelMadinah = "Odyst Hotel Madinah",
                airline = "Lion Air"
            )
        )

        // 2. Add Pilgrims linked to those schedules
        val p1Id = repository.insertPilgrim(
            Pilgrim(
                name = "Ahmad Subarjo",
                phone = "081234567890",
                passportNumber = "B1234567",
                scheduleId = s1Id.toInt(),
                status = "Siap Berangkat",
                notes = "Paspor dan pasfoto sudah lengkap diserahkan ke kantor."
            )
        )

        val p2Id = repository.insertPilgrim(
            Pilgrim(
                name = "Siti Aminah",
                phone = "081987654321",
                passportNumber = "B9876543",
                scheduleId = s1Id.toInt(),
                status = "Visa",
                notes = "Sedang proses pengajuan visa Umroh."
            )
        )

        val p3Id = repository.insertPilgrim(
            Pilgrim(
                name = "Budi Santoso",
                phone = "085211223344",
                passportNumber = "C1122334",
                scheduleId = s2Id.toInt(),
                status = "Pemberkasan",
                notes = "Masih melengkapi berkas syarat Haji Khusus."
            )
        )

        val p4Id = repository.insertPilgrim(
            Pilgrim(
                name = "Dewi Lestari",
                phone = "087766554433",
                passportNumber = "D4433221",
                scheduleId = s3Id.toInt(),
                status = "Pendaftaran",
                notes = "Baru melakukan registrasi awal."
            )
        )

        // 3. Add Payments
        // Ahmad Subarjo (Lunas: 38.5M)
        repository.insertPayment(
            Payment(
                pilgrimId = p1Id.toInt(),
                amount = 10000000.0,
                paymentMethod = "Transfer Bank",
                paymentType = "DP",
                notes = "Setoran awal tanda jadi Umroh Ramadhan."
            )
        )
        repository.insertPayment(
            Payment(
                pilgrimId = p1Id.toInt(),
                amount = 28500000.0,
                paymentMethod = "Transfer Bank",
                paymentType = "Pelunasan",
                notes = "Pelunasan sisa biaya paket Umroh VIP."
            )
        )

        // Siti Aminah (Belum Lunas: Paid 25M of 38.5M)
        repository.insertPayment(
            Payment(
                pilgrimId = p2Id.toInt(),
                amount = 10000000.0,
                paymentMethod = "Transfer Bank",
                paymentType = "DP",
                notes = "Pembayaran DP pendaftaran."
            )
        )
        repository.insertPayment(
            Payment(
                pilgrimId = p2Id.toInt(),
                amount = 15000000.0,
                paymentMethod = "Debit",
                paymentType = "Cicilan",
                notes = "Setoran cicilan kedua."
            )
        )

        // Budi Santoso (Haji: Paid 100M of 195M)
        repository.insertPayment(
            Payment(
                pilgrimId = p3Id.toInt(),
                amount = 100000000.0,
                paymentMethod = "Transfer Bank",
                paymentType = "DP",
                notes = "Setoran pendaftaran Haji Khusus nomor porsi."
            )
        )

        // Dewi Lestari (Umroh Hemat: Paid 5M of 28M)
        repository.insertPayment(
            Payment(
                pilgrimId = p4Id.toInt(),
                amount = 5000000.0,
                paymentMethod = "Tunai",
                paymentType = "DP",
                notes = "Booking fee kuota paket hemat."
            )
        )
    }

    // --- Pilgrim Methods ---
    fun addPilgrim(
        name: String,
        phone: String,
        passportNumber: String,
        scheduleId: Int?,
        status: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.insertPilgrim(
                Pilgrim(
                    name = name,
                    phone = phone,
                    passportNumber = passportNumber,
                    scheduleId = scheduleId,
                    status = status,
                    notes = notes
                )
            )
        }
    }

    fun updatePilgrim(pilgrim: Pilgrim) {
        viewModelScope.launch {
            repository.updatePilgrim(pilgrim)
        }
    }

    fun deletePilgrim(id: Int) {
        viewModelScope.launch {
            repository.deletePilgrim(id)
        }
    }

    // --- Schedule Methods ---
    fun addSchedule(
        title: String,
        departureDate: String,
        price: Double,
        quota: Int,
        packageType: String,
        hotelMekkah: String,
        hotelMadinah: String,
        airline: String
    ) {
        viewModelScope.launch {
            repository.insertSchedule(
                DepartureSchedule(
                    title = title,
                    departureDate = departureDate,
                    price = price,
                    quota = quota,
                    packageType = packageType,
                    hotelMekkah = hotelMekkah,
                    hotelMadinah = hotelMadinah,
                    airline = airline
                )
            )
        }
    }

    fun updateSchedule(schedule: DepartureSchedule) {
        viewModelScope.launch {
            repository.updateSchedule(schedule)
        }
    }

    fun deleteSchedule(id: Int) {
        viewModelScope.launch {
            repository.deleteSchedule(id)
        }
    }

    // --- Payment Methods ---
    fun addPayment(
        pilgrimId: Int,
        amount: Double,
        paymentMethod: String,
        paymentType: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.insertPayment(
                Payment(
                    pilgrimId = pilgrimId,
                    amount = amount,
                    paymentMethod = paymentMethod,
                    paymentType = paymentType,
                    notes = notes
                )
            )
        }
    }

    fun deletePayment(id: Int) {
        viewModelScope.launch {
            repository.deletePayment(id)
        }
    }

    // Get payments for specific pilgrim
    fun getPilgrimPaymentsFlow(pilgrimId: Int): Flow<List<Payment>> {
        return repository.getPaymentsByPilgrim(pilgrimId)
    }

    // Get pilgrims registered for specific schedule
    fun getSchedulePilgrimsFlow(scheduleId: Int): Flow<List<Pilgrim>> {
        return repository.getPilgrimsBySchedule(scheduleId)
    }
}

class TravelViewModelFactory(private val repository: TravelRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TravelViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TravelViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
