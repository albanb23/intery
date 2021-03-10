package com.albaburdallo.intery.wallet

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

class ChartViewModel: ViewModel() {

    private val _selectedDate = MutableLiveData<LocalDate>()
    val selectedDate: LiveData<LocalDate> = _selectedDate

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    val dates: List<LocalDate>
        @RequiresApi(Build.VERSION_CODES.O)
        get() {
            val now = LocalDate.now().withDayOfMonth(1)
            val muteDate = mutableListOf<LocalDate>()
            muteDate.add(LocalDate.ofEpochDay(0))
            val old = LocalDate.of(2000,1,1)
            val start = ChronoUnit.MONTHS.between(now, old)-1
            muteDate.addAll((start..now.monthValue).map { index ->
                now.minusMonths(now.monthValue - index.toLong())
            })
            muteDate.add(LocalDate.ofEpochDay(0))
            return muteDate
        }
}