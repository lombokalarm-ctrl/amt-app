package com.example.util

import java.text.NumberFormat
import java.util.Locale

object FormatUtils {
    fun formatRupiah(amount: Double): String {
        return try {
            val localeID = Locale("in", "ID")
            val numberFormat = NumberFormat.getCurrencyInstance(localeID)
            numberFormat.format(amount).replace("Rp", "Rp ").replace(",00", "").replace(",-", "")
        } catch (e: Exception) {
            "Rp " + String.format("%,.0f", amount).replace(',', '.')
        }
    }

    fun terbilang(amount: Double): String {
        val number = amount.toLong()
        if (number == 0L) return "Nol Rupiah"
        
        val units = listOf("", "Satu", "Dua", "Tiga", "Empat", "Lima", "Enam", "Tujuh", "Delapan", "Sembilan", "Sepuluh", "Sebelas")
        
        fun convert(n: Long): String {
            return when {
                n < 12 -> units[n.toInt()]
                n < 20 -> convert(n - 10) + " Belas"
                n < 100 -> convert(n / 10) + " Puluh " + convert(n % 10)
                n < 200 -> "Seratus " + convert(n - 100)
                n < 1000 -> convert(n / 100) + " Ratus " + convert(n % 100)
                n < 2000 -> "Seribu " + convert(n - 1000)
                n < 1000000 -> convert(n / 1000) + " Ribu " + convert(n % 1000)
                n < 1000000000 -> convert(n / 1000000) + " Juta " + convert(n % 1000000)
                n < 1000000000000L -> convert(n / 1000000000L) + " Milyar " + convert(n % 1000000000L)
                else -> convert(n / 1000000000000L) + " Triliun " + convert(n % 1000000000000L)
            }
        }
        
        val result = convert(number).replace("\\s+".toRegex(), " ").trim()
        return "$result Rupiah"
    }
}
