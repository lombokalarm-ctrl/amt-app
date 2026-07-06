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
import com.example.data.model.Payment
import com.example.data.model.Pilgrim
import com.example.ui.viewmodel.TravelViewModel
import com.example.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(viewModel: TravelViewModel) {
    val payments by viewModel.payments.collectAsState()
    val pilgrims by viewModel.pilgrims.collectAsState()
    val schedules by viewModel.schedules.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Tagihan Jamaah, 1: Riwayat Transaksi, 2: Invoice Maker
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("SEMUA") } // "SEMUA", "LUNAS", "SEBAGIAN", "BELUM_BAYAR"

    var showAddPaymentDialog by remember { mutableStateOf(false) }
    var showPilgrimDetailDialog by remember { mutableStateOf(false) }
    var selectedPilgrimForDetail by remember { mutableStateOf<Pilgrim?>(null) }

    // New Invoice and Receipt States
    var showReceiptDialog by remember { mutableStateOf(false) }
    var selectedPaymentForReceipt by remember { mutableStateOf<Payment?>(null) }

    // Mock Print Simulation state
    var showPrintSimulation by remember { mutableStateOf(false) }
    var printSimulationStep by remember { mutableStateOf(0) } // 0: Connecting, 1: Formatting, 2: Printing, 3: Completed
    var printSimulationMessage by remember { mutableStateOf("") }
    var printSimulationSuccess by remember { mutableStateOf(false) }

    // Invoice Maker states
    var selectedPilgrimIdForInvoice by remember { mutableStateOf(0) }
    var invoiceDateString by remember { mutableStateOf("06 Jul 2026") }
    var invoiceDueDateString by remember { mutableStateOf("13 Jul 2026") }
    var discountString by remember { mutableStateOf("") }
    var customItemName by remember { mutableStateOf("") }
    var customItemPrice by remember { mutableStateOf("") }
    var customItemQty by remember { mutableStateOf("1") }
    var invoiceItemsList by remember { mutableStateOf<List<InvoiceItem>>(emptyList()) }

    // Automatically initialize invoice items with the selected pilgrim's package
    LaunchedEffect(selectedPilgrimIdForInvoice, pilgrims, schedules) {
        val selectedPilgrim = pilgrims.find { it.id == selectedPilgrimIdForInvoice }
        if (selectedPilgrim != null) {
            val schedule = schedules.find { it.id == selectedPilgrim.scheduleId }
            if (schedule != null) {
                invoiceItemsList = listOf(
                    InvoiceItem(
                        name = "Paket Perjalanan: ${schedule.title}",
                        price = schedule.price,
                        quantity = 1
                    )
                )
            } else {
                invoiceItemsList = emptyList()
            }
        } else {
            invoiceItemsList = emptyList()
        }
    }

    // Unified states for recording payments
    var selectedPilgrimIdForAdd by remember { mutableStateOf(0) }
    var amountString by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Transfer Bank") }
    var paymentType by remember { mutableStateOf("DP") }
    var notes by remember { mutableStateOf("") }

    // Aggregate Financial Numbers
    val totalCollected = remember(payments) { payments.sumOf { it.amount } }
    val totalTarget = remember(pilgrims, schedules) {
        pilgrims.sumOf { pilgrim ->
            val sched = schedules.find { it.id == pilgrim.scheduleId }
            sched?.price ?: 0.0
        }
    }
    val outstandingReceivable = (totalTarget - totalCollected).coerceAtLeast(0.0)

    // Filter individual pilgrims based on search and selected payment status
    val filteredPilgrims = remember(pilgrims, payments, schedules, searchQuery, selectedStatusFilter) {
        pilgrims.filter { pilgrim ->
            val matchesSearch = pilgrim.name.contains(searchQuery, ignoreCase = true)
            
            val sched = schedules.find { it.id == pilgrim.scheduleId }
            val price = sched?.price ?: 0.0
            val totalPaid = payments.filter { it.pilgrimId == pilgrim.id }.sumOf { it.amount }
            
            val status = when {
                price == 0.0 -> "LUNAS"
                totalPaid >= price -> "LUNAS"
                totalPaid > 0.0 -> "SEBAGIAN"
                else -> "BELUM_BAYAR"
            }
            val matchesFilter = selectedStatusFilter == "SEMUA" || status == selectedStatusFilter
            
            matchesSearch && matchesFilter
        }
    }

    // Filter payments based on search (by pilgrim name, method, type)
    val filteredPayments = remember(payments, pilgrims, searchQuery) {
        payments.filter { payment ->
            val pilgrim = pilgrims.find { it.id == payment.pilgrimId }
            val matchesPilgrim = pilgrim?.name?.contains(searchQuery, ignoreCase = true) == true
            val matchesMethod = payment.paymentMethod.contains(searchQuery, ignoreCase = true)
            val matchesType = payment.paymentType.contains(searchQuery, ignoreCase = true)

            searchQuery.isEmpty() || matchesPilgrim || matchesMethod || matchesType
        }
    }

    val dateFormater = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Setoran & Pembayaran", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            if (activeTab < 2) {
                FloatingActionButton(
                    onClick = { 
                        selectedPilgrimIdForAdd = if (pilgrims.isNotEmpty()) pilgrims.first().id else 0
                        amountString = ""
                        paymentMethod = "Transfer Bank"
                        paymentType = "DP"
                        notes = ""
                        showAddPaymentDialog = true 
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("record_payment_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Catat Setoran")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Finance Dashboard Widgets
            AnimatedVisibility(visible = activeTab < 2) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    border = BorderStroke(1.dp, Color(0xFF49454F)),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Ringkasan Keuangan Travel",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Total Receivables
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Total Piutang", fontSize = 10.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = FormatUtils.formatRupiah(totalTarget),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Total Received
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Dana Masuk", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = FormatUtils.formatRupiah(totalCollected),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Remaining Balance row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sisa Sisa Tagihan Global:",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = FormatUtils.formatRupiah(outstandingReceivable),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // Tabs Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tabs = listOf("Tagihan Jamaah", "Riwayat Setoran", "Invoice Maker")
                tabs.forEachIndexed { index, title ->
                    val isSelected = activeTab == index
                    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color(0xFF2B2930)
                    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFCAC4D0)
                    val strokeColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color(0xFF49454F)

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeTab = index }
                            .testTag("payment_tab_$index"),
                        colors = CardDefaults.cardColors(containerColor = containerColor),
                        border = BorderStroke(1.dp, strokeColor),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = contentColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search transactions
            AnimatedVisibility(visible = activeTab < 2) {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .testTag("payment_search_input"),
                        placeholder = { 
                            if (activeTab == 0) Text("Cari jamaah...") 
                            else Text("Cari setoran berdasarkan nama jamaah...") 
                        },
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
                }
            }

            // Tab Content
            when (activeTab) {
                0 -> {
                // Status Filter Row for Pilgrim tab
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val filters = listOf(
                        "SEMUA" to "Semua",
                        "LUNAS" to "Lunas",
                        "SEBAGIAN" to "Dicicil",
                        "BELUM_BAYAR" to "Belum Bayar"
                    )
                    filters.forEach { (filterVal, label) ->
                        val isSelected = selectedStatusFilter == filterVal
                        val containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color(0xFF1C1B1F)
                        val contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else Color(0xFFCAC4D0)
                        val strokeColor = if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f) else Color(0xFF49454F)

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedStatusFilter = filterVal }
                                .testTag("filter_payment_status_$filterVal"),
                            colors = CardDefaults.cardColors(containerColor = containerColor),
                            border = BorderStroke(1.dp, strokeColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = contentColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Pilgrim Bills list
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
                                text = "Tidak ada tagihan jamaah yang cocok.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
                            val pilgrimPayments = payments.filter { it.pilgrimId == pilgrim.id }
                            val totalPaid = pilgrimPayments.sumOf { it.amount }
                            val schedule = schedules.find { it.id == pilgrim.scheduleId }
                            val price = schedule?.price ?: 0.0
                            val remaining = (price - totalPaid).coerceAtLeast(0.0)

                            val statusText = when {
                                price == 0.0 -> "LUNAS"
                                totalPaid >= price -> "LUNAS"
                                totalPaid > 0.0 -> "DICICIL"
                                else -> "BELUM BAYAR"
                            }
                            
                            val statusBgColor = when (statusText) {
                                "LUNAS" -> Color(0xFF1B5E20).copy(alpha = 0.2f)
                                "DICICIL" -> Color(0xFFE65100).copy(alpha = 0.2f)
                                else -> Color(0xFFB71C1C).copy(alpha = 0.2f)
                            }
                            
                            val statusTextColor = when (statusText) {
                                "LUNAS" -> Color(0xFF81C784)
                                "DICICIL" -> Color(0xFFFFB74D)
                                else -> Color(0xFFE57373)
                            }

                            val statusStrokeColor = when (statusText) {
                                "LUNAS" -> Color(0xFF81C784).copy(alpha = 0.4f)
                                "DICICIL" -> Color(0xFFFFB74D).copy(alpha = 0.4f)
                                else -> Color(0xFFE57373).copy(alpha = 0.4f)
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedPilgrimForDetail = pilgrim
                                        showPilgrimDetailDialog = true
                                    }
                                    .testTag("pilgrim_payment_card_${pilgrim.id}"),
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
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = pilgrim.name,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "No. HP: ${pilgrim.phone}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        }

                                        // Status Pill
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = statusBgColor),
                                            border = BorderStroke(1.dp, statusStrokeColor),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = statusText,
                                                color = statusTextColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Travel Details
                                    Text(
                                        text = "Paket: ${schedule?.title ?: "Belum Terdaftar"}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Financial Grid
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Biaya Paket", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                            Text(
                                                text = FormatUtils.formatRupiah(price),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Sudah Bayar", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                            Text(
                                                text = FormatUtils.formatRupiah(totalPaid),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Sisa Tagihan", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                            Text(
                                                text = if (remaining <= 0.0) "LUNAS" else FormatUtils.formatRupiah(remaining),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (remaining <= 0.0) Color(0xFF81C784) else Color(0xFFE57373)
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(
                                            onClick = {
                                                selectedPilgrimIdForAdd = pilgrim.id
                                                amountString = if (remaining > 0.0) remaining.toInt().toString() else ""
                                                paymentType = if (remaining > 0.0) "Pelunasan" else "DP"
                                                paymentMethod = "Transfer Bank"
                                                notes = ""
                                                showAddPaymentDialog = true
                                            },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Bayar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Riwayat Setoran (Transactions History List)
                Text(
                    text = "Riwayat Transaksi Setoran",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (filteredPayments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Belum ada transaksi pembayaran.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredPayments) { payment ->
                            val pilgrim = pilgrims.find { it.id == payment.pilgrimId }
                            val schedule = schedules.find { it.id == pilgrim?.scheduleId }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("payment_card_${payment.id}"),
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
                                                text = pilgrim?.name ?: "Nama Tidak Diketahui",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = schedule?.title ?: "Paket Tidak Diketahui",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Official Kuitansi/Receipt button
                                            IconButton(
                                                onClick = {
                                                    selectedPaymentForReceipt = payment
                                                    showReceiptDialog = true
                                                },
                                                modifier = Modifier.size(24.dp).testTag("payment_receipt_button_${payment.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ReceiptLong,
                                                    contentDescription = "Cetak Resi / Kuitansi",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            // Refund button (Delete transaction)
                                            IconButton(
                                                onClick = { viewModel.deletePayment(payment.id) },
                                                modifier = Modifier.size(24.dp).testTag("payment_refund_button_${payment.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = "Batalkan/Refund",
                                                    tint = Color(0xFFC62828),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                // Payment type pill
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF332D41)),
                                                    border = BorderStroke(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.2f)),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = payment.paymentType,
                                                        color = Color(0xFFD0BCFF),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                // Payment method pill
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                                                    border = BorderStroke(1.dp, Color(0xFFBBC3FF).copy(alpha = 0.2f)),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = payment.paymentMethod,
                                                        color = Color(0xFFBBC3FF),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Memo: ${payment.notes.ifEmpty { "Tanpa catatan" }}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = FormatUtils.formatRupiah(payment.amount),
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = dateFormater.format(Date(payment.paymentDate)),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                    InvoiceMakerWorkspace(
                        pilgrims = pilgrims,
                        schedules = schedules,
                        payments = payments,
                        selectedPilgrimId = selectedPilgrimIdForInvoice,
                        onSelectedPilgrimChange = { selectedPilgrimIdForInvoice = it },
                        invoiceItemsList = invoiceItemsList,
                        onInvoiceItemsListChange = { invoiceItemsList = it },
                        customItemName = customItemName,
                        onCustomItemNameChange = { customItemName = it },
                        customItemPrice = customItemPrice,
                        onCustomItemPriceChange = { customItemPrice = it },
                        customItemQty = customItemQty,
                        onCustomItemQtyChange = { customItemQty = it },
                        discountString = discountString,
                        onDiscountChange = { discountString = it },
                        invoiceDateString = invoiceDateString,
                        onInvoiceDateStringChange = { invoiceDateString = it },
                        invoiceDueDateString = invoiceDueDateString,
                        onInvoiceDueDateStringChange = { invoiceDueDateString = it },
                        onPrintClick = {
                            printSimulationStep = 0
                            printSimulationMessage = "Menghubungkan ke printer..."
                            printSimulationSuccess = false
                            showPrintSimulation = true
                        }
                    )
                }
            }
        }

        // 1. ADD PAYMENT DIALOG (Smart validation & remaining balance tracking)
        if (showAddPaymentDialog) {
            var pilgrimExpanded by remember { mutableStateOf(false) }
            var methodExpanded by remember { mutableStateOf(false) }
            var typeExpanded by remember { mutableStateOf(false) }
            var amountError by remember { mutableStateOf(false) }

            val paymentMethods = listOf("Transfer Bank", "Tunai", "Debit", "Lainnya")
            val paymentTypes = listOf("DP", "Cicilan", "Pelunasan")

            // Real-time calculation of remaining balance for selected pilgrim
            val currentSelectedPilgrim = pilgrims.find { it.id == selectedPilgrimIdForAdd }
            val currentPilgrimSchedule = schedules.find { it.id == currentSelectedPilgrim?.scheduleId }
            val pilgrimPayments = payments.filter { it.pilgrimId == selectedPilgrimIdForAdd }
            val currentPilgrimPaid = pilgrimPayments.sumOf { it.amount }
            val currentPilgrimPackagePrice = currentPilgrimSchedule?.price ?: 0.0
            val currentPilgrimRemainingBill = (currentPilgrimPackagePrice - currentPilgrimPaid).coerceAtLeast(0.0)

            AlertDialog(
                onDismissRequest = { showAddPaymentDialog = false },
                title = { Text("Catat Setoran Baru", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Pilgrim selector dropdown
                        ExposedDropdownMenuBox(
                            expanded = pilgrimExpanded,
                            onExpandedChange = { pilgrimExpanded = it }
                        ) {
                            val selectedPilgrimName = currentSelectedPilgrim?.name ?: "Pilih Nama Jamaah"
                            OutlinedTextField(
                                value = selectedPilgrimName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Jamaah Pembayar") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pilgrimExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("payment_pilgrim_dropdown")
                            )
                            ExposedDropdownMenu(
                                expanded = pilgrimExpanded,
                                onDismissRequest = { pilgrimExpanded = false }
                            ) {
                                if (pilgrims.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("Belum ada jamaah terdaftar.") },
                                        onClick = { pilgrimExpanded = false }
                                    )
                                } else {
                                    pilgrims.forEach { pilgrim ->
                                        DropdownMenuItem(
                                            text = { Text(pilgrim.name) },
                                            onClick = {
                                                selectedPilgrimIdForAdd = pilgrim.id
                                                pilgrimExpanded = false
                                                // Recalculate remaining to prefill
                                                val newPilgrimPayments = payments.filter { it.pilgrimId == pilgrim.id }
                                                val newPaid = newPilgrimPayments.sumOf { it.amount }
                                                val newSched = schedules.find { it.id == pilgrim.scheduleId }
                                                val newPrice = newSched?.price ?: 0.0
                                                val newRem = (newPrice - newPaid).coerceAtLeast(0.0)
                                                amountString = if (newRem > 0.0) newRem.toInt().toString() else ""
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Smart information block showing selected pilgrim's financial state
                        if (currentSelectedPilgrim != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (currentPilgrimRemainingBill <= 0)
                                        Color(0xFF1B5E20).copy(alpha = 0.15f)
                                    else
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Keberangkatan: ${currentPilgrimSchedule?.title ?: "Belum pilih paket"}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Total Biaya:", fontSize = 11.sp)
                                        Text(FormatUtils.formatRupiah(currentPilgrimPackagePrice), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Sudah Dibayar:", fontSize = 11.sp)
                                        Text(FormatUtils.formatRupiah(currentPilgrimPaid), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Sisa Tagihan Jamaah:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = if (currentPilgrimRemainingBill <= 0) "LUNAS" else FormatUtils.formatRupiah(currentPilgrimRemainingBill),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (currentPilgrimRemainingBill <= 0) Color(0xFF81C784) else Color(0xFFE57373)
                                        )
                                    }
                                }
                            }
                        }

                        // Amount to Pay
                        OutlinedTextField(
                            value = amountString,
                            onValueChange = {
                                amountString = it
                                amountError = it.trim().isEmpty() || it.toDoubleOrNull() == null
                            },
                            label = { Text("Jumlah Setoran (IDR)") },
                            modifier = Modifier.fillMaxWidth().testTag("payment_amount_input"),
                            isError = amountError,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            placeholder = { Text("e.g. 5000000") }
                        )

                        // Payment Type Selector Dropdown
                        ExposedDropdownMenuBox(
                            expanded = typeExpanded,
                            onExpandedChange = { typeExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = paymentType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Jenis Pembayaran") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("payment_type_dropdown")
                            )
                            ExposedDropdownMenu(
                                expanded = typeExpanded,
                                onDismissRequest = { typeExpanded = false }
                            ) {
                                paymentTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            paymentType = type
                                            typeExpanded = false
                                            // Auto prefill amount if they select "Pelunasan" and there's a remaining bill
                                            if (type == "Pelunasan" && currentPilgrimRemainingBill > 0) {
                                                amountString = currentPilgrimRemainingBill.toInt().toString()
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        // Payment Method Selector Dropdown
                        ExposedDropdownMenuBox(
                            expanded = methodExpanded,
                            onExpandedChange = { methodExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = paymentMethod,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Metode Pembayaran") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("payment_method_dropdown")
                            )
                            ExposedDropdownMenu(
                                expanded = methodExpanded,
                                onDismissRequest = { methodExpanded = false }
                            ) {
                                paymentMethods.forEach { method ->
                                    DropdownMenuItem(
                                        text = { Text(method) },
                                        onClick = {
                                            paymentMethod = method
                                            methodExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Notes/Memo
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Keterangan / Memo") },
                            modifier = Modifier.fillMaxWidth().testTag("payment_notes_input"),
                            singleLine = true,
                            placeholder = { Text("e.g. Bukti transfer terlampir") }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amountVal = amountString.toDoubleOrNull()
                            if (selectedPilgrimIdForAdd == 0 && pilgrims.isNotEmpty()) {
                                selectedPilgrimIdForAdd = pilgrims.first().id
                            }

                            if (amountVal == null || amountVal <= 0 || selectedPilgrimIdForAdd == 0) {
                                amountError = amountVal == null || amountVal <= 0
                                return@Button
                            }

                            viewModel.addPayment(
                                pilgrimId = selectedPilgrimIdForAdd,
                                amount = amountVal,
                                paymentMethod = paymentMethod,
                                paymentType = paymentType,
                                notes = notes
                            )
                            showAddPaymentDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("payment_save_button")
                    ) {
                        Text("Simpan Setoran")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddPaymentDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }

        // 2. DETAILED PILGRIM PAYMENT STATUS & HISTORY DIALOG
        if (showPilgrimDetailDialog && selectedPilgrimForDetail != null) {
            val pilgrim = selectedPilgrimForDetail!!
            val pilgrimPayments = payments.filter { it.pilgrimId == pilgrim.id }
            val totalPaid = pilgrimPayments.sumOf { it.amount }
            val schedule = schedules.find { it.id == pilgrim.scheduleId }
            val price = schedule?.price ?: 0.0
            val remaining = (price - totalPaid).coerceAtLeast(0.0)

            AlertDialog(
                onDismissRequest = { 
                    showPilgrimDetailDialog = false
                    selectedPilgrimForDetail = null
                },
                title = { 
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Rincian Pembayaran Jamaah", 
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = pilgrim.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Billing Info Summary Box
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)),
                            border = BorderStroke(1.dp, Color(0xFF49454F)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Paket: ${schedule?.title ?: "Belum Terdaftar"}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Harga Paket:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Text(FormatUtils.formatRupiah(price), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total Setoran:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Text(FormatUtils.formatRupiah(totalPaid), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Sisa Tagihan:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = if (remaining <= 0) "LUNAS" else FormatUtils.formatRupiah(remaining),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (remaining <= 0) Color(0xFF81C784) else Color(0xFFE57373)
                                    )
                                }
                            }
                        }

                        // Payment history subheader
                        Text(
                            text = "Histori Transaksi Setoran",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (pilgrimPayments.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Belum ada catatan setoran masuk.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        } else {
                            // Render payments list
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(pilgrimPayments) { p ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = p.paymentType,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = p.paymentMethod,
                                                        fontSize = 9.sp,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                    )
                                                }
                                                Text(
                                                    text = dateFormater.format(Date(p.paymentDate)),
                                                    fontSize = 9.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                                )
                                                if (p.notes.isNotEmpty()) {
                                                    Text(
                                                        text = "Memo: ${p.notes}",
                                                        fontSize = 9.sp,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                                    )
                                                }
                                            }
                                            
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = FormatUtils.formatRupiah(p.amount),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                IconButton(
                                                    onClick = { 
                                                        viewModel.deletePayment(p.id)
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.DeleteOutline,
                                                        contentDescription = "Hapus Pembayaran",
                                                        tint = Color(0xFFC62828),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    if (remaining > 0.0) {
                        Button(
                            onClick = {
                                selectedPilgrimIdForAdd = pilgrim.id
                                amountString = remaining.toInt().toString()
                                paymentType = "Pelunasan"
                                paymentMethod = "Transfer Bank"
                                notes = ""
                                showPilgrimDetailDialog = false
                                showAddPaymentDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("dialog_pay_remaining_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bayar Sisa")
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            showPilgrimDetailDialog = false
                            selectedPilgrimForDetail = null
                        }
                    ) {
                        Text("Tutup")
                    }
                }
            )
        }

        // Receipt (Kuitansi) Dialog
        if (showReceiptDialog && selectedPaymentForReceipt != null) {
            val payment = selectedPaymentForReceipt!!
            val pilgrim = pilgrims.find { it.id == payment.pilgrimId }
            val schedule = schedules.find { it.id == pilgrim?.scheduleId }
            
            AlertDialog(
                onDismissRequest = { 
                    showReceiptDialog = false
                    selectedPaymentForReceipt = null
                },
                title = null, // Custom beautiful receipt formatting
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                            border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Header
                                Text(
                                    text = "KAFILAH INDONESIA TOURS & TRAVEL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )
                                Text(
                                    text = "Izin Umroh Kemenag RI No. 123/2024",
                                    fontSize = 8.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "Gedung Kafilah, Jl. Dr. Saharjo No. 12, Jakarta",
                                    fontSize = 8.sp,
                                    color = Color.Gray
                                )
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                Text(
                                    text = "KUITANSI PEMBAYARAN",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black
                                )
                                Text(
                                    text = "No: KWT/2026/07/${payment.id.toString().padStart(4, '0')}",
                                    fontSize = 9.sp,
                                    color = Color.DarkGray
                                )
                                
                                Spacer(modifier = Modifier.height(14.dp))
                                
                                // Fields
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Telah Diterima Dari:", fontSize = 9.sp, color = Color.Gray, modifier = Modifier.width(90.dp))
                                    Text(pilgrim?.name ?: "-", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1f))
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Uang Sejumlah:", fontSize = 9.sp, color = Color.Gray, modifier = Modifier.width(90.dp))
                                    Text(FormatUtils.formatRupiah(payment.amount), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1B5E20), modifier = Modifier.weight(1f))
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Terbilang:", fontSize = 9.sp, color = Color.Gray, modifier = Modifier.width(90.dp))
                                    Text(FormatUtils.terbilang(payment.amount), fontSize = 9.sp, color = Color.Black, modifier = Modifier.weight(1f))
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Untuk Pembayaran:", fontSize = 9.sp, color = Color.Gray, modifier = Modifier.width(90.dp))
                                    Text("Setoran ${payment.paymentType} - Paket ${schedule?.title ?: "Perjalanan"}", fontSize = 10.sp, color = Color.Black, modifier = Modifier.weight(1f))
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Metode Bayar:", fontSize = 9.sp, color = Color.Gray, modifier = Modifier.width(90.dp))
                                    Text(payment.paymentMethod, fontSize = 9.sp, color = Color.Black, modifier = Modifier.weight(1f))
                                }

                                if (payment.notes.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Keterangan:", fontSize = 9.sp, color = Color.Gray, modifier = Modifier.width(90.dp))
                                        Text(payment.notes, fontSize = 9.sp, color = Color.Black, modifier = Modifier.weight(1f))
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Signature Block
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                        border = BorderStroke(1.dp, Color(0xFF2E7D32)),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "LUNAS",
                                                color = Color(0xFF2E7D32),
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                    
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Jakarta, ${SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date(payment.paymentDate))}", fontSize = 8.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("Petugas Keuangan", fontSize = 8.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text("Admin Staff", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        HorizontalDivider(color = Color.Black, thickness = 0.5.dp, modifier = Modifier.width(60.dp))
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            printSimulationStep = 0
                            printSimulationMessage = "Menghubungkan ke printer thermal..."
                            printSimulationSuccess = false
                            showPrintSimulation = true
                        }
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cetak")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            showReceiptDialog = false
                            selectedPaymentForReceipt = null
                        }
                    ) {
                        Text("Tutup")
                    }
                }
            )
        }

        // Print Simulation Dialog
        if (showPrintSimulation) {
            LaunchedEffect(showPrintSimulation) {
                // Steps of simulation
                kotlinx.coroutines.delay(800)
                printSimulationStep = 1
                printSimulationMessage = "Membuat format dokumen PDF..."
                kotlinx.coroutines.delay(1000)
                printSimulationStep = 2
                printSimulationMessage = "Mengirim data ke Printer Travel..."
                kotlinx.coroutines.delay(1200)
                printSimulationStep = 3
                printSimulationMessage = "Dokumen sedang dicetak..."
                kotlinx.coroutines.delay(1000)
                printSimulationSuccess = true
            }

            AlertDialog(
                onDismissRequest = { 
                    if (printSimulationSuccess) {
                        showPrintSimulation = false
                    }
                },
                title = {
                    Text(
                        text = if (printSimulationSuccess) "Cetak Selesai" else "Proses Pencetakan",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (!printSimulationSuccess) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = printSimulationMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = Color(0xFF81C784),
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = "Dokumen berhasil dicetak & dikirim ke antrean printer!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                confirmButton = {
                    if (printSimulationSuccess) {
                        Button(onClick = { showPrintSimulation = false }) {
                            Text("Tutup")
                        }
                    }
                }
            )
        }
    }
}

data class InvoiceItem(
    val name: String,
    val price: Double,
    val quantity: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceMakerWorkspace(
    pilgrims: List<Pilgrim>,
    schedules: List<DepartureSchedule>,
    payments: List<Payment>,
    selectedPilgrimId: Int,
    onSelectedPilgrimChange: (Int) -> Unit,
    invoiceItemsList: List<InvoiceItem>,
    onInvoiceItemsListChange: (List<InvoiceItem>) -> Unit,
    customItemName: String,
    onCustomItemNameChange: (String) -> Unit,
    customItemPrice: String,
    onCustomItemPriceChange: (String) -> Unit,
    customItemQty: String,
    onCustomItemQtyChange: (String) -> Unit,
    discountString: String,
    onDiscountChange: (String) -> Unit,
    invoiceDateString: String,
    onInvoiceDateStringChange: (String) -> Unit,
    invoiceDueDateString: String,
    onInvoiceDueDateStringChange: (String) -> Unit,
    onPrintClick: () -> Unit
) {
    var pilgrimExpanded by remember { mutableStateOf(false) }
    
    // Calculations
    val subtotal = invoiceItemsList.sumOf { item -> item.price * item.quantity }
    val discountVal = discountString.toDoubleOrNull() ?: 0.0
    val totalInvoice = (subtotal - discountVal).coerceAtLeast(0.0)
    
    val selectedPilgrimObj = pilgrims.find { it.id == selectedPilgrimId }
    val paymentsMade = if (selectedPilgrimObj != null) {
        payments.filter { it.pilgrimId == selectedPilgrimObj.id }.sumOf { it.amount }
    } else {
        0.0
    }
    val balanceDue = (totalInvoice - paymentsMade).coerceAtLeast(0.0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("invoice_maker_workspace"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Workspace Pembuatan Invoice",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Pilih Jamaah dan sesuaikan layanan tambahan untuk menerbitkan invoice.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        // 1. Selector & Dates
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFF49454F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Pilih Penerima & Tanggal", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    
                    // Pilgrim Selector
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedPilgrimObj?.name ?: "Pilih Jamaah...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Jamaah") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { pilgrimExpanded = true }
                                .testTag("invoice_pilgrim_selector"),
                            trailingIcon = {
                                IconButton(onClick = { pilgrimExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = pilgrimExpanded,
                            onDismissRequest = { pilgrimExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            pilgrims.forEach { pilgrim ->
                                val sched = schedules.find { it.id == pilgrim.scheduleId }
                                DropdownMenuItem(
                                    text = { 
                                        Text("${pilgrim.name} (${sched?.title ?: "Belum Terdaftar"})") 
                                    },
                                    onClick = {
                                        onSelectedPilgrimChange(pilgrim.id)
                                        pilgrimExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Dates Inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = invoiceDateString,
                            onValueChange = onInvoiceDateStringChange,
                            label = { Text("Tgl Terbit") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = invoiceDueDateString,
                            onValueChange = onInvoiceDueDateStringChange,
                            label = { Text("Jatuh Tempo") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }
        }

        // 2. Custom Items addition & items list
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFF49454F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Layanan & Biaya Tambahan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customItemName,
                            onValueChange = onCustomItemNameChange,
                            label = { Text("Nama Item") },
                            modifier = Modifier.weight(2f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = customItemPrice,
                            onValueChange = onCustomItemPriceChange,
                            label = { Text("Harga (IDR)") },
                            modifier = Modifier.weight(1.5f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = customItemQty,
                            onValueChange = onCustomItemQtyChange,
                            label = { Text("Qty") },
                            modifier = Modifier.weight(0.8f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        IconButton(
                            onClick = {
                                val priceVal = customItemPrice.toDoubleOrNull()
                                val qtyVal = customItemQty.toIntOrNull() ?: 1
                                if (customItemName.isNotBlank() && priceVal != null && priceVal > 0) {
                                    onInvoiceItemsListChange(invoiceItemsList + InvoiceItem(customItemName, priceVal, qtyVal))
                                    onCustomItemNameChange("")
                                    onCustomItemPriceChange("")
                                    onCustomItemQtyChange("1")
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Tambah Item", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    // Items Table
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Daftar Tagihan:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        if (invoiceItemsList.isEmpty()) {
                            Text("Belum ada tagihan ditambahkan.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                        } else {
                            invoiceItemsList.forEachIndexed { idx, item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        Text("${item.quantity} x ${FormatUtils.formatRupiah(item.price)}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(FormatUtils.formatRupiah(item.price * item.quantity), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = {
                                                onInvoiceItemsListChange(invoiceItemsList.toMutableList().apply { removeAt(idx) })
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFE57373), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                if (idx < invoiceItemsList.lastIndex) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                }
                            }
                        }
                    }

                    // Discount Field
                    OutlinedTextField(
                        value = discountString,
                        onValueChange = onDiscountChange,
                        label = { Text("Potongan / Diskon Tambahan (IDR)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        }

        // 3. Official Invoice Preview Layout
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFDFD)),
                border = BorderStroke(1.5.dp, Color.LightGray),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Badge Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("INVOICE TAGIHAN", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1B5E20))
                            Text("No: INV/2026/07/${if(selectedPilgrimId != 0) selectedPilgrimId.toString().padStart(4, '0') else "0000"}", fontSize = 9.sp, color = Color.Gray)
                        }
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    paymentsMade >= totalInvoice && totalInvoice > 0.0 -> Color(0xFFE8F5E9)
                                    paymentsMade > 0.0 -> Color(0xFFFFF3E0)
                                    else -> Color(0xFFFFEBEE)
                                }
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = when {
                                    paymentsMade >= totalInvoice && totalInvoice > 0.0 -> "LUNAS"
                                    paymentsMade > 0.0 -> "SEBAGIAN"
                                    else -> "BELUM BAYAR"
                                },
                                color = when {
                                    paymentsMade >= totalInvoice && totalInvoice > 0.0 -> Color(0xFF2E7D32)
                                    paymentsMade > 0.0 -> Color(0xFFE65100)
                                    else -> Color(0xFFC62828)
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = Color(0xFF1B5E20), thickness = 2.dp)
                    Spacer(modifier = Modifier.height(4.dp))

                    // Bill From & Bill To Block
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("DITERBITKAN OLEH:", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("Kafilah Indonesia Travel", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("Jakarta, Indonesia", fontSize = 9.sp, color = Color.DarkGray)
                        }
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text("DITUJUKAN KEPADA:", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(selectedPilgrimObj?.name ?: "Nama Jamaah", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("No. HP: ${selectedPilgrimObj?.phone ?: "-"}", fontSize = 9.sp, color = Color.DarkGray)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tanggal Terbit: $invoiceDateString", fontSize = 9.sp, color = Color.DarkGray)
                        Text("Jatuh Tempo: $invoiceDueDateString", fontSize = 9.sp, color = Color.DarkGray)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(4.dp))

                    // Simple Columns Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Deskripsi Item", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(2f))
                        Text("Qty", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                        Text("Jumlah", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)

                    // Display Items
                    if (invoiceItemsList.isEmpty()) {
                        Text("Belum ada layanan/item.", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 12.dp))
                    } else {
                        invoiceItemsList.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.name, fontSize = 9.sp, color = Color.Black, modifier = Modifier.weight(2f))
                                Text(item.quantity.toString(), fontSize = 9.sp, color = Color.Black, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                                Text(FormatUtils.formatRupiah(item.price * item.quantity), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                            }
                        }
                    }

                    HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)

                    // Calculation Summary block
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal", fontSize = 9.sp, color = Color.Gray)
                            Text(FormatUtils.formatRupiah(subtotal), fontSize = 9.sp, color = Color.Black)
                        }
                        if (discountVal > 0.0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Potongan Harga", fontSize = 9.sp, color = Color.Gray)
                                Text("- " + FormatUtils.formatRupiah(discountVal), fontSize = 9.sp, color = Color(0xFFC62828))
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Invoice", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(FormatUtils.formatRupiah(totalInvoice), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Dana Sudah Disetor", fontSize = 9.sp, color = Color.Gray)
                            Text(FormatUtils.formatRupiah(paymentsMade), fontSize = 9.sp, color = Color(0xFF2E7D32))
                        }
                        HorizontalDivider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(vertical = 2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sisa Sisa Tagihan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                            Text(FormatUtils.formatRupiah(balanceDue), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC62828))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Signature and actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Catatan:", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("Pembayaran resmi hanya sah melalui rekening travel BSI PT Kafilah Indonesia.", fontSize = 7.sp, color = Color.Gray, maxLines = 2)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Petugas Keuangan", fontSize = 8.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Siti Aminah", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            HorizontalDivider(color = Color.Black, thickness = 0.5.dp, modifier = Modifier.width(60.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preview Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* Share simulation */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bagikan", fontSize = 11.sp)
                        }
                        Button(
                            onClick = onPrintClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cetak Invoice", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}


