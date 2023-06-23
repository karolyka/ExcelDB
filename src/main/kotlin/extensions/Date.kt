package extensions

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Date

val Date.asLocalDate: LocalDate
    get() = toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

val Date.asLocalDateTime: LocalDateTime
    get() = toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()

val Date.asLocalTime: LocalTime
    get() = toInstant().atZone(ZoneId.systemDefault()).toLocalTime()

val Date.asCalendar: Calendar
    get() = Calendar.Builder().setInstant(this).build()
