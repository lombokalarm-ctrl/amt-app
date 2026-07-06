package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.viewmodel.TravelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: TravelViewModel
) {
    val context = LocalContext.current
    val pilgrims by viewModel.pilgrims.collectAsState()
    val schedules by viewModel.schedules.collectAsState()

    // SharedPreferences for dynamic persistence
    val sharedPrefs = remember {
        context.getSharedPreferences("company_profile_prefs", Context.MODE_PRIVATE)
    }

    // Dynamic State variables for Company Profile Details
    var travelName by remember {
        mutableStateOf(sharedPrefs.getString("travel_name", "PT KAFILAH INDONESIA TOURS") ?: "PT KAFILAH INDONESIA TOURS")
    }
    var tagline by remember {
        mutableStateOf(sharedPrefs.getString("tagline", "Biro Perjalanan Haji & Umrah Resmi Kemenag RI") ?: "Biro Perjalanan Haji & Umrah Resmi Kemenag RI")
    }
    var license by remember {
        mutableStateOf(sharedPrefs.getString("license", "Izin Kemenag No. 123 Tahun 2024") ?: "Izin Kemenag No. 123 Tahun 2024")
    }
    var companyNameFull by remember {
        mutableStateOf(sharedPrefs.getString("company_name_full", "PT Kafilah Indonesia Wisata") ?: "PT Kafilah Indonesia Wisata")
    }
    var skPpiu by remember {
        mutableStateOf(sharedPrefs.getString("sk_ppiu", "123/2024 (Penyelenggara Perjalanan Ibadah Umrah)") ?: "123/2024 (Penyelenggara Perjalanan Ibadah Umrah)")
    }
    var skPihk by remember {
        mutableStateOf(sharedPrefs.getString("sk_pihk", "456/2025 (Penyelenggara Ibadah Haji Khusus)") ?: "456/2025 (Penyelenggara Ibadah Haji Khusus)")
    }
    var association by remember {
        mutableStateOf(sharedPrefs.getString("association", "AMPHURI (Asosiasi Muslim Penyelenggara Haji & Umrah RI)") ?: "AMPHURI (Asosiasi Muslim Penyelenggara Haji & Umrah RI)")
    }
    var description by remember {
        mutableStateOf(sharedPrefs.getString("description", "Kafilah Indonesia Tours & Travel adalah biro perjalanan Haji & Umrah terpercaya sejak 2015. Kami berkomitmen memberikan layanan ibadah terbaik sesuai dengan tuntunan Al-Qur'an dan Sunnah, didukung oleh pembimbing berpengalaman, hotel sedekat mungkin dengan tempat ibadah, serta kepastian tiket keberangkatan. Kenyamanan, keamanan, dan kekhusyukan ibadah Anda adalah amanah terbesar bagi kami.") ?: "Kafilah Indonesia Tours & Travel adalah biro perjalanan Haji & Umrah terpercaya sejak 2015. Kami berkomitmen memberikan layanan ibadah terbaik sesuai dengan tuntunan Al-Qur'an dan Sunnah, didukung oleh pembimbing berpengalaman, hotel sedekat mungkin dengan tempat ibadah, serta kepastian tiket keberangkatan. Kenyamanan, keamanan, dan kekhusyukan ibadah Anda adalah amanah terbesar bagi kami.")
    }
    var address by remember {
        mutableStateOf(sharedPrefs.getString("address", "Jl. Dr. Saharjo No. 12, Manggarai, Tebet, Jakarta Selatan, 12850") ?: "Jl. Dr. Saharjo No. 12, Manggarai, Tebet, Jakarta Selatan, 12850")
    }
    var phoneHotline by remember {
        mutableStateOf(sharedPrefs.getString("phone_hotline", "021-8290000") ?: "021-8290000")
    }

    var logoType by remember {
        mutableStateOf(sharedPrefs.getString("logo_type", "ICON") ?: "ICON")
    }
    var logoIconName by remember {
        mutableStateOf(sharedPrefs.getString("logo_icon_name", "Explore") ?: "Explore")
    }
    var logoText by remember {
        mutableStateOf(sharedPrefs.getString("logo_text", "🕋") ?: "🕋")
    }
    var logoTintHex by remember {
        mutableStateOf(sharedPrefs.getString("logo_tint_hex", "#D0BCFF") ?: "#D0BCFF")
    }

    val logoTintParsed = remember(logoTintHex) {
        parseHexColor(logoTintHex)
    }

    // Form interactive states
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var contactMessage by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Toggle for company history / license view
    var showLegalityDetails by remember { mutableStateOf(false) }

    // Edit Dialog Toggle State
    var showEditDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("company_profile_screen"),
            contentPadding = PaddingValues(bottom = 80.dp) // extra bottom padding for FAB
        ) {
            // 1. Hero Banner Image & Overlapping Company Title
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    val heroPainter = painterResource(id = R.drawable.img_company_hero)
                    Image(
                        painter = heroPainter,
                        contentDescription = "Company Profile Banner",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp),
                        contentScale = ContentScale.Crop
                    )

                    // Smooth dark gradient overlay on the hero image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0x991C1B1F),
                                        Color(0xFF1C1B1F)
                                    )
                                )
                            )
                    )

                    // Overlapping Circular Logo Card
                    Card(
                        shape = CircleShape,
                        border = BorderStroke(2.dp, logoTintParsed),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.BottomCenter)
                            .offset(y = (-10).dp)
                            .testTag("company_profile_logo_card")
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (logoType == "EMOJI" || logoType == "TEXT") {
                                Text(
                                    text = logoText,
                                    fontSize = 32.sp,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                val logoIcon = when (logoIconName) {
                                    "Mosque" -> Icons.Default.Mosque
                                    "FlightTakeoff" -> Icons.Default.FlightTakeoff
                                    "Business" -> Icons.Default.Business
                                    "Map" -> Icons.Default.Map
                                    "Star" -> Icons.Default.Star
                                    else -> Icons.Default.Explore
                                }
                                Icon(
                                    imageVector = logoIcon,
                                    contentDescription = "Logo Kafilah",
                                    tint = logoTintParsed,
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Company Name and Subtitle
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = travelName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color(0xFFEADDFF),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("company_profile_title")
                    )
                    Text(
                        text = tagline,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // License Badge
                    Row(
                        modifier = Modifier
                            .background(Color(0xFF332D41), RoundedCornerShape(16.dp))
                            .clickable { showLegalityDetails = !showLegalityDetails }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("company_profile_legality_badge"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Verified",
                            tint = Color(0xFF81C784),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = license,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF81C784)
                        )
                        Icon(
                            imageVector = if (showLegalityDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Detail",
                            tint = Color(0xFF81C784),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Expandable Legality Details
                    AnimatedVisibility(
                        visible = showLegalityDetails,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                            border = BorderStroke(1.dp, Color(0xFF49454F))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Legalitas & Perizinan Resmi",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFFD0BCFF)
                                )
                                HorizontalDivider(color = Color(0xFF49454F))
                                Text(
                                    text = "• Nama Perusahaan: $companyNameFull",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "• SK PPIU No. $skPpiu",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "• SK PIHK No. $skPihk",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "• Asosiasi: $association",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // 3. Dynamic Stats Dashboard Grid
            item {
                val registeredPilgrimsCount = pilgrims.size
                
                // Historical statistics offsets to look premium
                val totalPilgrimsServed = 15240 + registeredPilgrimsCount
                val satisfactionRate = "99.8%"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Stat Card 1
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF332D41)),
                        border = BorderStroke(1.dp, Color(0xFF49454F))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "11+",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFD0BCFF)
                            )
                            Text(
                                text = "Thn Pengalaman",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Stat Card 2
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF332D41)),
                        border = BorderStroke(1.dp, Color(0xFF49454F))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = totalPilgrimsServed.toString(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFD0BCFF)
                            )
                            Text(
                                text = "Jamaah Terlayani",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Stat Card 3
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF332D41)),
                        border = BorderStroke(1.dp, Color(0xFF49454F))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = satisfactionRate,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFD0BCFF)
                            )
                            Text(
                                text = "Tingkat Kepuasan",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // 4. Brief Introduction
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                    border = BorderStroke(1.dp, Color(0xFF49454F))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Tentang Kami",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFFD0BCFF)
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 5. Brand Values
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Mengapa Memilih Kami?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFFD0BCFF),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )

                    ValuePillarRow(
                        icon = Icons.Default.MenuBook,
                        title = "Bimbingan Sesuai Sunnah",
                        description = "Ibadah dibimbing langsung oleh Asatidzah bermanhaj salaf yang berkompeten di bidang Fiqih Ibadah."
                    )

                    ValuePillarRow(
                        icon = Icons.Default.FlightTakeoff,
                        title = "Kepastian Berangkat",
                        description = "Tiket pesawat dipesan berpasangan (pergi-pulang) di awal sebelum pendaftaran jamaah dibuka."
                    )

                    ValuePillarRow(
                        icon = Icons.Default.Apartment,
                        title = "Hotel Dekat Masjid",
                        description = "Jaminan hotel sedekat mungkin (jarak jalan kaki dekat) dari Masjidil Haram dan Masjid Nabawi."
                    )

                    ValuePillarRow(
                        icon = Icons.Default.SupportAgent,
                        title = "Pendampingan 24/7",
                        description = "Tim handling berpengalaman mendampingi penuh sejak dari bandara asal, saat ibadah, hingga kembali pulang."
                    )
                }
            }

            // 6. Interactive Main Travel Packages Promo
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Paket Perjalanan Unggulan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFFD0BCFF)
                        )
                        Text(
                            text = "Total: ${schedules.size} Paket",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (schedules.isEmpty()) {
                        Text(
                            text = "Belum ada jadwal paket keberangkatan aktif.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        schedules.take(2).forEach { schedule ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        Toast.makeText(
                                            context,
                                            "Lihat jadwal lengkap di Tab 'Jadwal'",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF332D41)),
                                border = BorderStroke(0.5.dp, Color(0xFF49454F))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Color(0xFF49454F), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (schedule.packageType == "UMROH") Icons.Default.Mosque else Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color(0xFFD0BCFF),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = schedule.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Hotel Makkah: ${schedule.hotelMekkah}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFEADDFF), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = schedule.packageType,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF381E72)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 7. Headquarters and Branch Office Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                    border = BorderStroke(1.dp, Color(0xFF49454F))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Lokasi Kantor & Kontak",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFFD0BCFF)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Alamat",
                                tint = Color(0xFFD0BCFF),
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "Kantor Pusat / Utama",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = address,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        HorizontalDivider(color = Color(0xFF49454F), modifier = Modifier.padding(vertical = 4.dp))

                        // Simulated interactive contact action rows
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    Toast.makeText(
                                        context,
                                        "Menghubungi hotline $phoneHotline (Simulasi)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("btn_call_hotline"),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFD0BCFF))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFFD0BCFF)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Telp: $phoneHotline", fontSize = 10.sp, color = Color(0xFFD0BCFF), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }

                            Button(
                                onClick = {
                                    Toast.makeText(
                                        context,
                                        "Membuka Google Maps ke lokasi $travelName (Simulasi)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("btn_maps_office"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEADDFF))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFF381E72)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Peta Kantor", fontSize = 10.sp, color = Color(0xFF381E72))
                            }
                        }
                    }
                }
            }

            // 8. Contact Consultation Form Section (Interactivity)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF332D41)),
                    border = BorderStroke(1.dp, Color(0xFF49454F))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Konsultasi Ibadah & Pendaftaran",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFFD0BCFF)
                        )
                        Text(
                            text = "Ada pertanyaan mengenai paket atau ingin merencanakan keberangkatan? Kirim pesan langsung kepada tim konsultan kami.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Form Fields
                        OutlinedTextField(
                            value = contactName,
                            onValueChange = { contactName = it },
                            label = { Text("Nama Lengkap") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("contact_form_name"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFD0BCFF),
                                unfocusedBorderColor = Color(0xFF49454F)
                            )
                        )

                        OutlinedTextField(
                            value = contactPhone,
                            onValueChange = { contactPhone = it },
                            label = { Text("Nomor HP / WhatsApp") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("contact_form_phone"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFD0BCFF),
                                unfocusedBorderColor = Color(0xFF49454F)
                            )
                        )

                        OutlinedTextField(
                            value = contactMessage,
                            onValueChange = { contactMessage = it },
                            label = { Text("Pesan Pertanyaan") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .testTag("contact_form_message"),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFD0BCFF),
                                unfocusedBorderColor = Color(0xFF49454F)
                            )
                        )

                        Button(
                            onClick = {
                                if (contactName.isNotBlank() && contactPhone.isNotBlank() && contactMessage.isNotBlank()) {
                                    showSuccessDialog = true
                                } else {
                                    Toast.makeText(context, "Harap lengkapi semua kolom form!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("contact_form_submit_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kirim Konsultasi")
                        }
                    }
                }
            }
        }

        // 9. Floating Edit Button at the bottom-right corner!
        ExtendedFloatingActionButton(
            text = { Text("Edit Profil", fontWeight = FontWeight.Bold) },
            icon = { Icon(Icons.Default.Edit, contentDescription = "Edit Company Profile") },
            onClick = { showEditDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("btn_edit_profile_fab"),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }

    // Success Dialog for consultative form submission
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                contactName = ""
                contactPhone = ""
                contactMessage = ""
            },
            title = {
                Text(
                    text = "Konsultasi Terkirim",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Sukses",
                        tint = Color(0xFF81C784),
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = "Terima kasih, Bapak/Ibu $contactName. Konsultasi Anda telah terkirim ke staff konsultan $travelName. Kami akan menghubungi Anda kembali melalui nomor $contactPhone segera.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        contactName = ""
                        contactPhone = ""
                        contactMessage = ""
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Scrollable Edit Dialog for editing ALL profile fields dynamically!
    if (showEditDialog) {
        var editTravelName by remember { mutableStateOf(travelName) }
        var editTagline by remember { mutableStateOf(tagline) }
        var editLicense by remember { mutableStateOf(license) }
        var editCompanyNameFull by remember { mutableStateOf(companyNameFull) }
        var editSkPpiu by remember { mutableStateOf(skPpiu) }
        var editSkPihk by remember { mutableStateOf(skPihk) }
        var editAssociation by remember { mutableStateOf(association) }
        var editDescription by remember { mutableStateOf(description) }
        var editAddress by remember { mutableStateOf(address) }
        var editPhoneHotline by remember { mutableStateOf(phoneHotline) }

        var editLogoType by remember { mutableStateOf(logoType) }
        var editLogoIconName by remember { mutableStateOf(logoIconName) }
        var editLogoText by remember { mutableStateOf(logoText) }
        var editLogoTintHex by remember { mutableStateOf(logoTintHex) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = "Edit Profil Perusahaan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Kustomisasi Logo Perusahaan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFFD0BCFF),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    // Logo Type Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ICON" to "Icon Preset", "EMOJI" to "Karakter/Emoji").forEach { (type, label) ->
                            val isSelected = editLogoType == type
                            Box(
                                modifier = Modifier
                                    .weight(1.5f)
                                    .background(
                                        if (isSelected) Color(0xFF49454F) else Color(0xFF2B2930),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) Color(0xFFD0BCFF) else Color(0xFF49454F),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { editLogoType = type }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color.Gray
                                )
                            }
                        }
                    }

                    if (editLogoType == "ICON") {
                        // Icon Selection
                        Text("Pilih Icon:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val iconPresets = listOf(
                            "Explore" to "Kompas",
                            "Mosque" to "Masjid",
                            "FlightTakeoff" to "Pesawat",
                            "Business" to "Gedung",
                            "Map" to "Peta",
                            "Star" to "Bintang"
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            iconPresets.forEach { (iconId, label) ->
                                val isSelected = editLogoIconName == iconId
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isSelected) Color(0xFF49454F) else Color(0xFF2B2930),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 0.5.dp,
                                            color = if (isSelected) Color(0xFFD0BCFF) else Color(0xFF49454F),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { editLogoIconName = iconId }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = when (iconId) {
                                                "Mosque" -> Icons.Default.Mosque
                                                "FlightTakeoff" -> Icons.Default.FlightTakeoff
                                                "Business" -> Icons.Default.Business
                                                "Map" -> Icons.Default.Map
                                                "Star" -> Icons.Default.Star
                                                else -> Icons.Default.Explore
                                            },
                                            contentDescription = null,
                                            tint = if (isSelected) Color(0xFFD0BCFF) else Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = label,
                                            fontSize = 8.sp,
                                            color = if (isSelected) Color.White else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Custom Text / Emoji Input
                        OutlinedTextField(
                            value = editLogoText,
                            onValueChange = { 
                                if (it.length <= 4) editLogoText = it 
                            },
                            label = { Text("Karakter / Emoji Logo") },
                            placeholder = { Text("Contoh: 🕋 atau K") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("edit_logo_text"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFD0BCFF),
                                unfocusedBorderColor = Color(0xFF49454F)
                            )
                        )
                    }

                    // Logo Color Customization
                    Text("Pilih Warna Logo:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val colorPresets = listOf(
                        "#D0BCFF" to "Ungu",
                        "#FFD700" to "Emas",
                        "#81C784" to "Hijau",
                        "#4DD0E1" to "Sian",
                        "#FF8A80" to "Merah"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colorPresets.forEach { (hex, name) ->
                            val isSelected = editLogoTintHex.equals(hex, ignoreCase = true)
                            val parsedColor = remember(hex) { parseHexColor(hex) }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) Color(0xFF49454F) else Color(0xFF2B2930),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) Color(0xFFD0BCFF) else Color(0xFF49454F),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { editLogoTintHex = hex }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(parsedColor, CircleShape)
                                    )
                                    Text(
                                        text = name,
                                        fontSize = 9.sp,
                                        color = if (isSelected) Color.White else Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFF49454F), modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "Detail Informasi Perusahaan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFFD0BCFF),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    OutlinedTextField(
                        value = editTravelName,
                        onValueChange = { editTravelName = it },
                        label = { Text("Nama Agen/Travel") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_travel_name"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD0BCFF),
                            unfocusedBorderColor = Color(0xFF49454F)
                        )
                    )

                    OutlinedTextField(
                        value = editTagline,
                        onValueChange = { editTagline = it },
                        label = { Text("Slogan / Tagline") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_travel_tagline"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD0BCFF),
                            unfocusedBorderColor = Color(0xFF49454F)
                        )
                    )

                    OutlinedTextField(
                        value = editLicense,
                        onValueChange = { editLicense = it },
                        label = { Text("Lisensi Singkat Kemenag") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_travel_license"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD0BCFF),
                            unfocusedBorderColor = Color(0xFF49454F)
                        )
                    )

                    OutlinedTextField(
                        value = editCompanyNameFull,
                        onValueChange = { editCompanyNameFull = it },
                        label = { Text("Nama Lengkap PT Perusahaan") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_travel_pt_name"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD0BCFF),
                            unfocusedBorderColor = Color(0xFF49454F)
                        )
                    )

                    OutlinedTextField(
                        value = editSkPpiu,
                        onValueChange = { editSkPpiu = it },
                        label = { Text("No. SK PPIU (Umroh)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_travel_sk_ppiu"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD0BCFF),
                            unfocusedBorderColor = Color(0xFF49454F)
                        )
                    )

                    OutlinedTextField(
                        value = editSkPihk,
                        onValueChange = { editSkPihk = it },
                        label = { Text("No. SK PIHK (Haji)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_travel_sk_pihk"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD0BCFF),
                            unfocusedBorderColor = Color(0xFF49454F)
                        )
                    )

                    OutlinedTextField(
                        value = editAssociation,
                        onValueChange = { editAssociation = it },
                        label = { Text("Asosiasi Keanggotaan") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_travel_association"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD0BCFF),
                            unfocusedBorderColor = Color(0xFF49454F)
                        )
                    )

                    OutlinedTextField(
                        value = editPhoneHotline,
                        onValueChange = { editPhoneHotline = it },
                        label = { Text("No. Telepon / Hotline") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_travel_phone"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD0BCFF),
                            unfocusedBorderColor = Color(0xFF49454F)
                        )
                    )

                    OutlinedTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        label = { Text("Alamat Lengkap") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("edit_travel_address"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD0BCFF),
                            unfocusedBorderColor = Color(0xFF49454F)
                        )
                    )

                    OutlinedTextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        label = { Text("Deskripsi Singkat / Tentang Kami") },
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth().testTag("edit_travel_description"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD0BCFF),
                            unfocusedBorderColor = Color(0xFF49454F)
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Batal")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editTravelName.isNotBlank() && editAddress.isNotBlank() && editPhoneHotline.isNotBlank()) {
                            // Persist to SharedPreferences
                            sharedPrefs.edit().apply {
                                putString("travel_name", editTravelName)
                                putString("tagline", editTagline)
                                putString("license", editLicense)
                                putString("company_name_full", editCompanyNameFull)
                                putString("sk_ppiu", editSkPpiu)
                                putString("sk_pihk", editSkPihk)
                                putString("association", editAssociation)
                                putString("description", editDescription)
                                putString("address", editAddress)
                                putString("phone_hotline", editPhoneHotline)
                                putString("logo_type", editLogoType)
                                putString("logo_icon_name", editLogoIconName)
                                putString("logo_text", editLogoText)
                                putString("logo_tint_hex", editLogoTintHex)
                                apply()
                            }

                            // Update active states
                            travelName = editTravelName
                            tagline = editTagline
                            license = editLicense
                            companyNameFull = editCompanyNameFull
                            skPpiu = editSkPpiu
                            skPihk = editSkPihk
                            association = editAssociation
                            description = editDescription
                            address = editAddress
                            phoneHotline = editPhoneHotline
                            logoType = editLogoType
                            logoIconName = editLogoIconName
                            logoText = editLogoText
                            logoTintHex = editLogoTintHex

                            showEditDialog = false
                            Toast.makeText(context, "Profil perusahaan berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Nama, Alamat, dan No. Telepon wajib diisi!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.testTag("save_profile_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Simpan")
                }
            }
        )
    }
}

@Composable
fun ValuePillarRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2B2930), RoundedCornerShape(12.dp))
            .border(BorderStroke(0.5.dp, Color(0xFF49454F)), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFF332D41), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFD0BCFF),
                modifier = Modifier.size(16.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )
        }
    }
}

fun parseHexColor(hex: String): Color {
    val cleanHex = hex.removePrefix("#")
    return try {
        if (cleanHex.length == 6) {
            Color(cleanHex.toLong(16) or 0xFF000000)
        } else if (cleanHex.length == 8) {
            Color(cleanHex.toLong(16))
        } else {
            Color(0xFFD0BCFF)
        }
    } catch (e: Exception) {
        Color(0xFFD0BCFF)
    }
}
