package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DepartureSchedule
import com.example.data.model.Pilgrim
import com.example.ui.viewmodel.TravelViewModel
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(viewModel: TravelViewModel) {
    val schedules by viewModel.schedules.collectAsState()
    val pilgrims by viewModel.pilgrims.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var scheduleToEdit by remember { mutableStateOf<DepartureSchedule?>(null) }

    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedScheduleForDetail by remember { mutableStateOf<DepartureSchedule?>(null) }

    // Search & Filter state
    var searchQuery by remember { mutableStateOf("") }
    var selectedPackageType by remember { mutableStateOf("SEMUA") } // "SEMUA", "UMROH", "HAJI"

    // Safe Deletion confirmation state
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var scheduleToDelete by remember { mutableStateOf<DepartureSchedule?>(null) }

    // Date Picker state
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jadwal Keberangkatan", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scheduleToEdit = null
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_schedule_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Paket")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Introductory text card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                border = BorderStroke(1.dp, Color(0xFF49454F)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Kelola paket Umroh & Haji, tentukan harga, kuota, maskapai penerbangan, serta hotel transit di Mekkah dan Madinah.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }
            }

            // Search & Filter Block
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFF49454F)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("schedule_search_input"),
                        placeholder = { Text("Cari paket, maskapai, tanggal...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Package Type Filter Tabs Row
                    Text(
                        text = "Filter Kategori Paket:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val categories = listOf("SEMUA" to "Semua Paket", "UMROH" to "Umroh", "HAJI" to "Haji")
                        categories.forEach { (type, label) ->
                            val isSelected = selectedPackageType == type
                            val bg = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color(0xFF2B2930)
                            val textCol = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFCAC4D0)
                            val strokeCol = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color(0xFF49454F)
                            
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedPackageType = type }
                                    .testTag("filter_tab_${type.lowercase()}"),
                                colors = CardDefaults.cardColors(containerColor = bg),
                                border = BorderStroke(1.dp, strokeCol),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textCol
                                    )
                                }
                            }
                        }
                    }
                }
            }

            val filteredSchedules = remember(schedules, searchQuery, selectedPackageType) {
                schedules.filter { schedule ->
                    val matchesSearch = schedule.title.contains(searchQuery, ignoreCase = true) ||
                            schedule.airline.contains(searchQuery, ignoreCase = true) ||
                            schedule.departureDate.contains(searchQuery, ignoreCase = true)
                    val matchesType = selectedPackageType == "SEMUA" || schedule.packageType == selectedPackageType
                    matchesSearch && matchesType
                }
            }

            // List of schedules
            if (schedules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Belum ada jadwal keberangkatan.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Klik tombol + di bawah untuk merancang paket baru.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            } else if (filteredSchedules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tidak ada paket yang cocok.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Coba kata kunci pencarian atau filter yang lain.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredSchedules) { schedule ->
                        // Calculate registered pilgrims and remaining quota
                        val registeredCount = pilgrims.count { it.scheduleId == schedule.id }
                        val remainingQuota = (schedule.quota - registeredCount).coerceAtLeast(0)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedScheduleForDetail = schedule
                                    showDetailDialog = true
                                }
                                .testTag("schedule_card_${schedule.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, Color(0xFF49454F)),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(
                                                    color = if (schedule.packageType == "UMROH")
                                                        MaterialTheme.colorScheme.primaryContainer
                                                    else
                                                        MaterialTheme.colorScheme.secondaryContainer,
                                                    shape = RoundedCornerShape(8.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (schedule.packageType == "UMROH")
                                                    Icons.Default.FlightTakeoff
                                                else
                                                    Icons.Default.MilitaryTech,
                                                contentDescription = null,
                                                tint = if (schedule.packageType == "UMROH")
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = schedule.title,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Tanggal Keberangkatan: ${schedule.departureDate}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    }

                                    // Package Type Tag
                                    val tagBg = if (schedule.packageType == "UMROH") Color(0xFF332D41) else Color(0xFF2B2930)
                                    val tagText = if (schedule.packageType == "UMROH") Color(0xFFD0BCFF) else Color(0xFFBBC3FF)
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = tagBg),
                                        border = BorderStroke(1.dp, tagText.copy(alpha = 0.2f)),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = schedule.packageType,
                                            color = tagText,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text(
                                            text = "Hotel Mekkah / Madinah:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        )
                                        Text(
                                            text = "${schedule.hotelMekkah} / ${schedule.hotelMadinah}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Flight,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = schedule.airline,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = FormatUtils.formatRupiah(schedule.price),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))

                                        // Quota availability pill
                                        val pillColor = if (remainingQuota == 0) Color(0xFFC62828) else if (remainingQuota < 5) Color(0xFFEF6C00) else Color(0xFF2E7D32)
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = pillColor.copy(alpha = 0.1f)),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "Sisa Kursi: $remainingQuota / ${schedule.quota}",
                                                color = pillColor,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 1. ADD / EDIT SCHEDULE DIALOG
        if (showAddEditDialog) {
            var title by remember { mutableStateOf(scheduleToEdit?.title ?: "") }
            var departureDate by remember { mutableStateOf(scheduleToEdit?.departureDate ?: "2026-") }
            var priceString by remember { mutableStateOf(scheduleToEdit?.price?.toInt()?.toString() ?: "") }
            var quotaString by remember { mutableStateOf(scheduleToEdit?.quota?.toString() ?: "") }
            var packageType by remember { mutableStateOf(scheduleToEdit?.packageType ?: "UMROH") }
            var hotelMekkah by remember { mutableStateOf(scheduleToEdit?.hotelMekkah ?: "") }
            var hotelMadinah by remember { mutableStateOf(scheduleToEdit?.hotelMadinah ?: "") }
            var airline by remember { mutableStateOf(scheduleToEdit?.airline ?: "") }

            var titleError by remember { mutableStateOf(false) }
            var dateError by remember { mutableStateOf(false) }
            var priceError by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showAddEditDialog = false },
                title = { Text(if (scheduleToEdit == null) "Buat Paket Baru" else "Edit Detail Paket", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Title
                        OutlinedTextField(
                            value = title,
                            onValueChange = {
                                title = it
                                titleError = it.trim().isEmpty()
                            },
                            label = { Text("Nama Paket Keberangkatan") },
                            modifier = Modifier.fillMaxWidth().testTag("schedule_title_input"),
                            isError = titleError,
                            singleLine = true
                        )

                        // Date
                        OutlinedTextField(
                            value = departureDate,
                            onValueChange = {
                                departureDate = it
                                dateError = it.trim().isEmpty()
                            },
                            label = { Text("Tanggal (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth().testTag("schedule_date_input"),
                            isError = dateError,
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = "Pilih Tanggal")
                                }
                            }
                        )

                        // Price and Quota Side by Side
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = priceString,
                                onValueChange = {
                                    priceString = it
                                    priceError = it.trim().isEmpty() || it.toDoubleOrNull() == null
                                },
                                label = { Text("Harga (IDR)") },
                                modifier = Modifier.weight(1.3f).testTag("schedule_price_input"),
                                isError = priceError,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = quotaString,
                                onValueChange = { quotaString = it },
                                label = { Text("Kuota Pax") },
                                modifier = Modifier.weight(0.7f).testTag("schedule_quota_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }

                        // Package Type Selector Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("Tipe Paket: ", fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = packageType == "UMROH",
                                    onClick = { packageType = "UMROH" }
                                )
                                Text("Umroh")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = packageType == "HAJI",
                                    onClick = { packageType = "HAJI" }
                                )
                                Text("Haji")
                            }
                        }

                        // Hotels
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = hotelMekkah,
                                onValueChange = { hotelMekkah = it },
                                label = { Text("Hotel Makkah") },
                                modifier = Modifier.weight(1f).testTag("schedule_hotel_mekkah_input"),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = hotelMadinah,
                                onValueChange = { hotelMadinah = it },
                                label = { Text("Hotel Madinah") },
                                modifier = Modifier.weight(1f).testTag("schedule_hotel_madinah_input"),
                                singleLine = true
                            )
                        }

                        // Airline
                        OutlinedTextField(
                            value = airline,
                            onValueChange = { airline = it },
                            label = { Text("Maskapai Penerbangan") },
                            modifier = Modifier.fillMaxWidth().testTag("schedule_airline_input"),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val priceDouble = priceString.toDoubleOrNull()
                            val quotaInt = quotaString.toIntOrNull() ?: 45

                            if (title.trim().isEmpty() || departureDate.trim().isEmpty() || priceDouble == null) {
                                titleError = title.trim().isEmpty()
                                dateError = departureDate.trim().isEmpty()
                                priceError = priceDouble == null
                                return@Button
                            }

                            if (scheduleToEdit == null) {
                                viewModel.addSchedule(
                                    title = title,
                                    departureDate = departureDate,
                                    price = priceDouble,
                                    quota = quotaInt,
                                    packageType = packageType,
                                    hotelMekkah = hotelMekkah,
                                    hotelMadinah = hotelMadinah,
                                    airline = airline
                                )
                            } else {
                                viewModel.updateSchedule(
                                    scheduleToEdit!!.copy(
                                        title = title,
                                        departureDate = departureDate,
                                        price = priceDouble,
                                        quota = quotaInt,
                                        packageType = packageType,
                                        hotelMekkah = hotelMekkah,
                                        hotelMadinah = hotelMadinah,
                                        airline = airline
                                    )
                                )
                            }
                            showAddEditDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("schedule_save_button")
                    ) {
                        Text("Simpan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddEditDialog = false }) {
                        Text("Batal")
                    }
                }
            )

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState()
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                    departureDate = formatter.format(java.util.Date(millis))
                                    dateError = false
                                }
                                showDatePicker = false
                            }
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Batal")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        }

        // 2. DETAIL & PASSENGER MANIFEST DIALOG
        if (showDetailDialog && selectedScheduleForDetail != null) {
            val schedule = selectedScheduleForDetail!!
            val manifestPilgrims = pilgrims.filter { it.scheduleId == schedule.id }
            val registeredCount = manifestPilgrims.size
            val remainingQuota = schedule.quota - registeredCount

            AlertDialog(
                onDismissRequest = { showDetailDialog = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Spesifikasi Detail Paket", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        IconButton(onClick = { showDetailDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        // Title
                        Text(
                            text = schedule.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Travel Details Block
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row {
                                    Text("Tanggal: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(110.dp))
                                    Text(schedule.departureDate, style = MaterialTheme.typography.bodyMedium)
                                }
                                Row {
                                    Text("Tipe: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(110.dp))
                                    Text(schedule.packageType, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                Row {
                                    Text("Biaya: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(110.dp))
                                    Text(FormatUtils.formatRupiah(schedule.price), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Row {
                                    Text("Hotel Mekkah: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(110.dp))
                                    Text(schedule.hotelMekkah, style = MaterialTheme.typography.bodyMedium)
                                }
                                Row {
                                    Text("Hotel Madinah: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(110.dp))
                                    Text(schedule.hotelMadinah, style = MaterialTheme.typography.bodyMedium)
                                }
                                Row {
                                    Text("Maskapai: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(110.dp))
                                    Text(schedule.airline, style = MaterialTheme.typography.bodyMedium)
                                }
                                Row {
                                    Text("Kuota Terisi: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(110.dp))
                                    Text("$registeredCount terdaftar (Sisa ${remainingQuota.coerceAtLeast(0)} kursi)", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Passenger Manifest list
                        Text("Manifest Jamaah Terdaftar", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(6.dp))
                        if (manifestPilgrims.isEmpty()) {
                            Text(
                                text = "Belum ada jamaah yang didaftarkan pada paket ini.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        } else {
                            Box(modifier = Modifier.heightIn(max = 120.dp)) {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    items(manifestPilgrims) { pilgrim ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f),
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(pilgrim.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text("HP: ${pilgrim.phone}", fontSize = 10.sp, color = Color.Gray)
                                            }
                                            Text(pilgrim.status, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Delete Button
                        IconButton(
                            onClick = {
                                scheduleToDelete = schedule
                                showDeleteConfirmDialog = true
                                showDetailDialog = false
                            },
                            modifier = Modifier.testTag("delete_schedule_button_${schedule.id}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus Paket", tint = Color(0xFFC62828))
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Edit Button
                        Button(
                            onClick = {
                                scheduleToEdit = schedule
                                showDetailDialog = false
                                showAddEditDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Paket")
                        }
                    }
                }
            )
        }

        // 3. DELETE CONFIRMATION DIALOG
        if (showDeleteConfirmDialog && scheduleToDelete != null) {
            val schedule = scheduleToDelete!!
            val registeredPilgrimsCount = pilgrims.count { it.scheduleId == schedule.id }
            
            AlertDialog(
                onDismissRequest = { 
                    showDeleteConfirmDialog = false
                    scheduleToDelete = null
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Peringatan",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = { 
                    Text(
                        text = "Konfirmasi Hapus Paket", 
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) 
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Apakah Anda yakin ingin menghapus paket keberangkatan ini?",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = schedule.title,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        if (registeredPilgrimsCount > 0) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF3D2D2D)),
                                border = BorderStroke(1.dp, Color(0xFFF0B2B2).copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = "Bahaya",
                                        tint = Color(0xFFF0B2B2),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "PERINGATAN: Ada $registeredPilgrimsCount jamaah terdaftar pada paket ini. Menghapus paket akan mengosongkan jadwal keberangkatan mereka!",
                                        color = Color(0xFFF0B2B2),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteSchedule(schedule.id)
                            showDeleteConfirmDialog = false
                            scheduleToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("confirm_delete_schedule_button")
                    ) {
                        Text("Ya, Hapus")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            showDeleteConfirmDialog = false
                            scheduleToDelete = null
                        }
                    ) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}
