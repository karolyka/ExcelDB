package domain

import Entity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Calendar
import java.util.Date

data class DataTypes(
    val id: Int,
    val boolean: Boolean,
    val calendar: Calendar,
    val date: Date,
    val double: Double,
    val int: Int,
    val localDate: LocalDate,
    val localDateTime: LocalDateTime,
    val localTime: LocalTime
) :
    Entity
