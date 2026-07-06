package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DepartureSchedule
import com.example.data.model.Payment
import com.example.data.model.Pilgrim
import com.example.ui.viewmodel.TravelViewModel
import com.example.util.FormatUtils

@Composable
fun DashboardScreen(
    viewModel: TravelViewModel,
    onNavigateToPilgrims: () -> Unit,
    onNavigateToSchedules: () -> Unit,
    onNavigateToPayments: () -> Unit
) {
    val schedules by viewModel.schedules.collectAsState()
    val pilgrims by viewModel.pilgrims.collectAsState()
    val payments by viewModel.payments.collectAsState()

    // Calculations for Stats
    val totalPilgrims = pilgrims.size
    val totalSchedules = schedules.size
    val totalPaymentsAmount = payments.sumOf { it.amount }

    // Count Lunas
    val pilgrimPaidMap = remember(pilgrims, payments, schedules) {
        pilgrims.associate { pilgrim ->
            val schedule = schedules.find { it.id == pilgrim.scheduleId }
            val totalPaid = payments.filter { it.pilgrimId == pilgrim.id }.sumOf { it.amount }
            val targetPrice = schedule?.price ?: 0.0
            pilgrim.id to (totalPaid >= targetPrice && targetPrice > 0)
        }
    }
    val lunasCount = pilgrimPaidMap.values.count { it }
    val belumLunasCount = totalPilgrims - lunasCount

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. App Top Header Row (Styled from the mockup header)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = Color(0xFF49454F),
                                shape = RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = "Logo",
                            tint = Color(0xFFD0BCFF),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "JMROH System",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE6E1E5),
                            letterSpacing = (-0.25).sp
                        )
                        Text(
                            text = "Manajemen Umrah & Haji",
                            fontSize = 12.sp,
                            color = Color(0xFFCAC4D0)
                        )
                    }
                }

                // Profile Avatar Box with border
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF49454F), shape = RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "F",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD0BCFF)
                    )
                }
            }
        }

        // 2. Keberangkatan Terdekat (Hero Highlight Card)
        item {
            val nearestSchedule = schedules.minByOrNull { it.departureDate }
            val scheduleTitle = nearestSchedule?.title ?: "Umrah Syawal"
            val scheduleDetail = if (nearestSchedule != null) "Group A - ${nearestSchedule.airline}" else "Group A - Garuda Indonesia GA-980"
            val scheduleDate = nearestSchedule?.departureDate ?: "12 Mei 2024"
            
            val scheduleQuota = nearestSchedule?.quota ?: 45
            val assignedCount = if (nearestSchedule != null) pilgrims.count { it.scheduleId == nearestSchedule.id } else 33
            val remainingSlots = (scheduleQuota - assignedCount).coerceAtLeast(0)
            val slotsText = if (nearestSchedule != null) "$remainingSlots / $scheduleQuota Jamaah" else "12 / 45 Jamaah"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF332D41)),
                border = BorderStroke(1.dp, Color(0xFF49454F)),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            // Highlight Badge
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFD0BCFF),
                                    contentColor = Color(0xFF381E72)
                                ),
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Text(
                                    text = "KEBERANGKATAN TERDEKAT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = scheduleTitle,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFE6E1E5)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = scheduleDetail,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFCCC2DC)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.FlightTakeoff,
                            contentDescription = "Flight",
                            tint = Color(0xFFD0BCFF),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Card Tanggal
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF49454F)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "TANGGAL",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFCAC4D0)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = scheduleDate,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE6E1E5)
                                )
                            }
                        }

                        // Card Sisa Slot
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF49454F)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "SISA SLOT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFCAC4D0)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = slotsText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD0BCFF)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Stats Section
        item {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Primary Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color(0xFF49454F)),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.People,
                                    contentDescription = "Total Jamaah",
                                    tint = Color(0xFFD0BCFF),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "$totalPilgrims",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFD0BCFF)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Total Jamaah",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCAC4D0)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color(0xFF49454F)),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DateRange,
                                    contentDescription = "Jadwal",
                                    tint = Color(0xFFBBC3FF),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "$totalSchedules",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFBBC3FF)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Paket Jadwal",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCAC4D0)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Financial Stat Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color(0xFF49454F)),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    color = Color(0xFF49454F),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Payments,
                                contentDescription = "Dana Masuk",
                                tint = Color(0xFFD0BCFF)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "Total Dana Terkumpul",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCAC4D0)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = FormatUtils.formatRupiah(totalPaymentsAmount),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFD0BCFF)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Payment statuses breakdown
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF49454F).copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, Color(0xFF49454F).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color(0xFFB2F0B2), RoundedCornerShape(5.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Lunas: $lunasCount",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFFB2F0B2)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color(0xFFF0B2B2), RoundedCornerShape(5.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Belum Lunas: $belumLunasCount",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFFF0B2B2)
                            )
                        }
                    }
                }
            }
        }

        // 4. Quick Navigation Section
        item {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Akses Cepat",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Button 1: Jamaah
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToPilgrims() },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                        border = BorderStroke(1.dp, Color(0xFF49454F)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFFD0BCFF), RoundedCornerShape(24.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = "Jamaah",
                                    tint = Color(0xFF381E72),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = "Data Jamaah",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFE6E1E5)
                            )
                        }
                    }

                    // Button 2: Jadwal
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToSchedules() },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                        border = BorderStroke(1.dp, Color(0xFF49454F)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFFBBC3FF), RoundedCornerShape(24.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = "Jadwal",
                                    tint = Color(0xFF1E2772),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = "Paket Jadwal",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFE6E1E5)
                            )
                        }
                    }

                    // Button 3: Pembayaran
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToPayments() },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                        border = BorderStroke(1.dp, Color(0xFF49454F)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFFFFB2D0), RoundedCornerShape(24.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = "Bayar",
                                    tint = Color(0xFF5B1230),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = "Pembayaran",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFE6E1E5)
                            )
                        }
                    }
                }
            }
        }

        // 5. Upcoming Departure Schedules
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Jadwal Keberangkatan Terdekat",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = onNavigateToSchedules) {
                    Text("Semua", color = Color(0xFFD0BCFF))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (schedules.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada jadwal keberangkatan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCAC4D0).copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            items(schedules.take(3)) { schedule ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color(0xFF49454F)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    color = if (schedule.packageType == "UMROH")
                                        Color(0xFF332D41)
                                    else
                                        Color(0xFF2B2930),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (schedule.packageType == "UMROH") "U" else "H",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (schedule.packageType == "UMROH")
                                    Color(0xFFD0BCFF)
                                else
                                    Color(0xFFBBC3FF)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = schedule.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFE6E1E5),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = Color(0xFFCAC4D0),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = schedule.departureDate,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFCAC4D0)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = FormatUtils.formatRupiah(schedule.price),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFD0BCFF)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Kuota: ${schedule.quota} pax",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCAC4D0)
                            )
                        }
                    }
                }
            }
        }

        // 6. Recent Pilgrims Section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pendaftaran Jamaah Terbaru",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = onNavigateToPilgrims) {
                    Text("Semua", color = Color(0xFFD0BCFF))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (pilgrims.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada jamaah terdaftar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCAC4D0).copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            items(pilgrims.take(3)) { pilgrim ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color(0xFF49454F)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = Color(0xFF49454F),
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFFD0BCFF)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pilgrim.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFE6E1E5)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = pilgrim.phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCAC4D0)
                            )
                        }

                        // Elegant dark status pill
                        val isPaid = pilgrimPaidMap[pilgrim.id] ?: false
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isPaid) Color(0xFF2D332D) else Color(0xFF3D2D2D)
                            ),
                            border = BorderStroke(1.dp, if (isPaid) Color(0xFFB2F0B2).copy(alpha = 0.3f) else Color(0xFFF0B2B2).copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isPaid) "Lunas" else "Belum Lunas",
                                color = if (isPaid) Color(0xFFB2F0B2) else Color(0xFFF0B2B2),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
