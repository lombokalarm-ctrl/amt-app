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
import com.example.data.model.Payment
import com.example.ui.viewmodel.TravelViewModel
import com.example.util.FormatUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PilgrimScreen(viewModel: TravelViewModel) {
    val pilgrims by viewModel.pilgrims.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val payments by viewModel.payments.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("Semua") }

    // Dialog state
    var showAddEditDialog by remember { mutableStateOf(false) }
    var pilgrimToEdit by remember { mutableStateOf<Pilgrim?>(null) }

    // Detail state
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedPilgrimForDetail by remember { mutableStateOf<Pilgrim?>(null) }

    val scope = rememberCoroutineScope()

    // Filtered pilgrims
    val filteredPilgrims = remember(pilgrims, searchQuery, selectedStatusFilter) {
        pilgrims.filter { pilgrim ->
            val matchesSearch = pilgrim.name.contains(searchQuery, ignoreCase = true) ||
                    pilgrim.passportNumber.contains(searchQuery, ignoreCase = true) ||
                    pilgrim.phone.contains(searchQuery, ignoreCase = true)

            val matchesStatus = selectedStatusFilter == "Semua" || pilgrim.status == selectedStatusFilter

            matchesSearch && matchesStatus
        }
    }

    val statusOptions = listOf("Semua", "Pendaftaran", "Pemberkasan", "Paspor", "Visa", "Siap Berangkat")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manajemen Jamaah", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    pilgrimToEdit = null
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_pilgrim_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Jamaah")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search & Filter Block
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFF49454F)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pilgrim_search_input"),
                        placeholder = { Text("Cari nama, nomor paspor, atau HP...") },
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

                    // Status Filters Horizontal Row
                    Text(
                        text = "Filter Status Dokumen:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Horizontal scrollable-like or wrapped row of filters. Since there are 6 options, let's wrap it in an easily clickable layout
                        Box(modifier = Modifier.fillMaxWidth()) {
                            // Let's use a flow row simulation or simple wrapped row
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    statusOptions.take(3).forEach { status ->
                                        FilterChip(
                                            selected = selectedStatusFilter == status,
                                            onClick = { selectedStatusFilter = status },
                                            label = { Text(status, fontSize = 12.sp) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    statusOptions.drop(3).forEach { status ->
                                        FilterChip(
                                            selected = selectedStatusFilter == status,
                                            onClick = { selectedStatusFilter = status },
                                            label = { Text(status, fontSize = 12.sp) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // List of Pilgrims
            if (filteredPilgrims.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PeopleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tidak ada data jamaah ditemukan.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Silakan klik tombol + untuk mendaftarkan jamaah baru.",
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
                    items(filteredPilgrims) { pilgrim ->
                        val pilgrimSchedule = schedules.find { it.id == pilgrim.scheduleId }
                        val pilgrimPayments = payments.filter { it.pilgrimId == pilgrim.id }
                        val totalPaid = pilgrimPayments.sumOf { it.amount }
                        val packagePrice = pilgrimSchedule?.price ?: 0.0
                        val isPaid = totalPaid >= packagePrice && packagePrice > 0

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedPilgrimForDetail = pilgrim
                                    showDetailDialog = true
                                }
                                .testTag("pilgrim_card_${pilgrim.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, Color(0xFF49454F)),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = pilgrim.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "HP: ${pilgrim.phone} | Paspor: ${pilgrim.passportNumber.ifEmpty { "-" }}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }

                                    // Document status pill
                                    val (statusBg, statusText) = when (pilgrim.status) {
                                        "Siap Berangkat" -> Color(0xFF2D332D) to Color(0xFFB2F0B2)
                                        "Visa" -> Color(0xFF332D41) to Color(0xFFBBC3FF)
                                        "Paspor" -> Color(0xFF332D41) to Color(0xFFEADDFF)
                                        "Pemberkasan" -> Color(0xFF3D2D2D) to Color(0xFFFFB2D0)
                                        else -> Color(0xFF49454F) to Color(0xFFCAC4D0)
                                    }
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = statusBg),
                                        border = BorderStroke(1.dp, statusText.copy(alpha = 0.2f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = pilgrim.status,
                                            color = statusText,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Joined package
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Paket Keberangkatan:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        )
                                        Text(
                                            text = pilgrimSchedule?.title ?: "Belum Pilih Paket",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = if (pilgrimSchedule != null) MaterialTheme.colorScheme.primary else Color.Red,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    // Payment Status Label
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Status Pembayaran:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        )
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isPaid) Color(0xFF2D332D) else Color(0xFF3D2D2D)
                                            ),
                                            border = BorderStroke(1.dp, if (isPaid) Color(0xFFB2F0B2).copy(alpha = 0.2f) else Color(0xFFF0B2B2).copy(alpha = 0.2f)),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = if (isPaid) "Lunas" else "Belum Lunas",
                                                color = if (isPaid) Color(0xFFB2F0B2) else Color(0xFFF0B2B2),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
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

        // 1. ADD / EDIT DIALOG
        if (showAddEditDialog) {
            var name by remember { mutableStateOf(pilgrimToEdit?.name ?: "") }
            var phone by remember { mutableStateOf(pilgrimToEdit?.phone ?: "") }
            var passportNumber by remember { mutableStateOf(pilgrimToEdit?.passportNumber ?: "") }
            var scheduleId by remember { mutableStateOf(pilgrimToEdit?.scheduleId ?: if (schedules.isNotEmpty()) schedules.first().id else 0) }
            var status by remember { mutableStateOf(pilgrimToEdit?.status ?: "Pendaftaran") }
            var notes by remember { mutableStateOf(pilgrimToEdit?.notes ?: "") }

            var scheduleExpanded by remember { mutableStateOf(false) }
            var statusExpanded by remember { mutableStateOf(false) }

            var nameError by remember { mutableStateOf(false) }
            var phoneError by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showAddEditDialog = false },
                title = { Text(if (pilgrimToEdit == null) "Registrasi Jamaah Baru" else "Edit Data Jamaah", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Name
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                nameError = it.trim().isEmpty()
                            },
                            label = { Text("Nama Lengkap") },
                            modifier = Modifier.fillMaxWidth().testTag("pilgrim_name_input"),
                            isError = nameError,
                            singleLine = true
                        )

                        // Phone
                        OutlinedTextField(
                            value = phone,
                            onValueChange = {
                                phone = it
                                phoneError = it.trim().isEmpty()
                            },
                            label = { Text("Nomor HP") },
                            modifier = Modifier.fillMaxWidth().testTag("pilgrim_phone_input"),
                            isError = phoneError,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true
                        )

                        // Passport
                        OutlinedTextField(
                            value = passportNumber,
                            onValueChange = { passportNumber = it },
                            label = { Text("Nomor Paspor") },
                            modifier = Modifier.fillMaxWidth().testTag("pilgrim_passport_input"),
                            singleLine = true
                        )

                        // Schedule Package Selector
                        ExposedDropdownMenuBox(
                            expanded = scheduleExpanded,
                            onExpandedChange = { scheduleExpanded = it }
                        ) {
                            val selectedScheduleText = schedules.find { it.id == scheduleId }?.title ?: "Pilih Paket Keberangkatan"
                            OutlinedTextField(
                                value = selectedScheduleText,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Paket Keberangkatan") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = scheduleExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("pilgrim_package_dropdown")
                            )
                            ExposedDropdownMenu(
                                expanded = scheduleExpanded,
                                onDismissRequest = { scheduleExpanded = false }
                            ) {
                                if (schedules.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("Belum ada paket. Buat jadwal dahulu.") },
                                        onClick = { scheduleExpanded = false }
                                    )
                                } else {
                                    schedules.forEach { s ->
                                        DropdownMenuItem(
                                            text = { Text("${s.title} (${FormatUtils.formatRupiah(s.price)})") },
                                            onClick = {
                                                scheduleId = s.id
                                                scheduleExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Status Selector
                        ExposedDropdownMenuBox(
                            expanded = statusExpanded,
                            onExpandedChange = { statusExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = status,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Status Dokumen") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("pilgrim_status_dropdown")
                            )
                            ExposedDropdownMenu(
                                expanded = statusExpanded,
                                onDismissRequest = { statusExpanded = false }
                            ) {
                                statusOptions.drop(1).forEach { statusOption -> // drop "Semua" filter
                                    DropdownMenuItem(
                                        text = { Text(statusOption) },
                                        onClick = {
                                            status = statusOption
                                            statusExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Notes
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Catatan / Keterangan") },
                            modifier = Modifier.fillMaxWidth().testTag("pilgrim_notes_input"),
                            maxLines = 3
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (name.trim().isEmpty() || phone.trim().isEmpty()) {
                                nameError = name.trim().isEmpty()
                                phoneError = phone.trim().isEmpty()
                                return@Button
                            }

                            val selectedScheduleId = if (scheduleId == 0 && schedules.isNotEmpty()) schedules.first().id else scheduleId

                            if (pilgrimToEdit == null) {
                                viewModel.addPilgrim(
                                    name = name,
                                    phone = phone,
                                    passportNumber = passportNumber,
                                    scheduleId = if (selectedScheduleId > 0) selectedScheduleId else null,
                                    status = status,
                                    notes = notes
                                )
                            } else {
                                viewModel.updatePilgrim(
                                    pilgrimToEdit!!.copy(
                                        name = name,
                                        phone = phone,
                                        passportNumber = passportNumber,
                                        scheduleId = if (selectedScheduleId > 0) selectedScheduleId else null,
                                        status = status,
                                        notes = notes
                                    )
                                )
                            }
                            showAddEditDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("pilgrim_save_button")
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
        }

        // 2. DETAIL DIALOG (Profiles, History, and Payment check)
        if (showDetailDialog && selectedPilgrimForDetail != null) {
            val pilgrim = selectedPilgrimForDetail!!
            val schedule = schedules.find { it.id == pilgrim.scheduleId }
            val pilgrimPayments = payments.filter { it.pilgrimId == pilgrim.id }
            val totalPaid = pilgrimPayments.sumOf { it.amount }
            val targetPrice = schedule?.price ?: 0.0
            val remaining = targetPrice - totalPaid
            val isPaid = totalPaid >= targetPrice && targetPrice > 0

            AlertDialog(
                onDismissRequest = { showDetailDialog = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Profil Lengkap Jamaah", fontWeight = FontWeight.Bold, fontSize = 20.sp)
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
                        // User Name Banner
                        Text(
                            text = pilgrim.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Grid of details
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row {
                                    Text("HP: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(90.dp))
                                    Text(pilgrim.phone, style = MaterialTheme.typography.bodyMedium)
                                }
                                Row {
                                    Text("Paspor: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(90.dp))
                                    Text(pilgrim.passportNumber.ifEmpty { "Belum ada paspor" }, style = MaterialTheme.typography.bodyMedium)
                                }
                                Row {
                                    Text("Status Dok: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(90.dp))
                                    Text(pilgrim.status, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                Row {
                                    Text("Catatan: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(90.dp))
                                    Text(pilgrim.notes.ifEmpty { "-" }, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Package info
                        Text("Paket & Keuangan", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = schedule?.title ?: "Belum pilih paket keberangkatan",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Biaya Paket: ", style = MaterialTheme.typography.bodyMedium)
                                    Text(FormatUtils.formatRupiah(targetPrice), fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total Setoran: ", style = MaterialTheme.typography.bodyMedium)
                                    Text(FormatUtils.formatRupiah(totalPaid), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Sisa Tagihan: ", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = if (remaining <= 0) "LUNAS" else FormatUtils.formatRupiah(remaining),
                                        fontWeight = FontWeight.Bold,
                                        color = if (remaining <= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // History of Payments for this pilgrim
                        Text("Riwayat Setoran Pembayaran", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(6.dp))
                        if (pilgrimPayments.isEmpty()) {
                            Text(
                                text = "Belum ada transaksi pembayaran yang dicatat.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        } else {
                            Box(modifier = Modifier.heightIn(max = 100.dp)) {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    items(pilgrimPayments) { pay ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f),
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("${pay.paymentType} - ${pay.paymentMethod}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Text(pay.notes.ifEmpty { "Tanpa catatan" }, fontSize = 10.sp, color = Color.Gray)
                                            }
                                            Text(FormatUtils.formatRupiah(pay.amount), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
                                viewModel.deletePilgrim(pilgrim.id)
                                showDetailDialog = false
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus Jamaah", tint = Color(0xFFC62828))
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Edit Button
                        Button(
                            onClick = {
                                pilgrimToEdit = pilgrim
                                showDetailDialog = false
                                showAddEditDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Profil")
                        }
                    }
                }
            )
        }
    }
}
