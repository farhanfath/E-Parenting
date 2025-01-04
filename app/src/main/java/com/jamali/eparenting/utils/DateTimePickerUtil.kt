package com.jamali.eparenting.utils

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.EditText
import java.util.Calendar

@SuppressLint("DefaultLocale")
object DateTimePickerUtil {

    fun showDatePicker(
        context: Context,
        editText: EditText,
        onDateSelected: ((String) -> Unit)? = null
    ) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                // Format tanggal DD-MM-YYYY
                val formattedDate = String.format(
                    "%02d-%02d-%d",
                    selectedDayOfMonth,
                    selectedMonth + 1,
                    selectedYear
                )

                // Atur teks pada EditText
                editText.setText(formattedDate)

                // Panggil callback jika disediakan
                onDateSelected?.invoke(formattedDate)
            },
            year, month, day
        )

        datePickerDialog.show()
    }

    fun showTimePicker(
        context: Context,
        editText: EditText,
        is24HourFormat: Boolean = true,
        onTimeSelected: ((String) -> Unit)? = null
    ) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                // Format waktu HH:MM
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)

                // Atur teks pada EditText
                editText.setText(formattedTime)

                // Panggil callback jika disediakan
                onTimeSelected?.invoke(formattedTime)
            },
            hour, minute, is24HourFormat
        )

        timePickerDialog.show()
    }

    /**
     * Mendapatkan tanggal hari ini dalam format DD-MM-YYYY
     */
    fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        return String.format(
            "%02d-%02d-%d",
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.YEAR)
        )
    }

    /**
     * Mendapatkan waktu saat ini dalam format HH:MM
     */
    fun getCurrentTime(is24HourFormat: Boolean = true): String {
        val calendar = Calendar.getInstance()
        return String.format(
            "%02d:%02d",
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE)
        )
    }
}