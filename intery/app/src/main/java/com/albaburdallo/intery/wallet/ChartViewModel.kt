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

    //fechas con LiveData para que se actualice todo al cambiar de mes
    val dates: List<LocalDate>
        @RequiresApi(Build.VERSION_CODES.O)
        get() {
            val now = LocalDate.now().withDayOfMonth(1)
            val muteDate = mutableListOf<LocalDate>()
            muteDate.add(LocalDate.ofEpochDay(0)) //primer valor de la lista 0 para que este centrado
            val old = LocalDate.of(2000,1,1)
            val start = ChronoUnit.MONTHS.between(now, old)-1 //todas las fechas desde el 01/01/2000 hasta ahora
            muteDate.addAll((start..now.monthValue).map { index ->
                now.minusMonths(now.monthValue - index)//va metiendo en el map todas las fechas en orden desc
            })
            muteDate.add(LocalDate.ofEpochDay(0))//último valor de la lista 0 para que este centrado
            return muteDate
        }
}