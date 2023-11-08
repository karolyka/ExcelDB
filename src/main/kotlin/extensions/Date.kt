package extensions

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Date

/** This property gives a [LocalDate] from a [Date] */
val Date.asLocalDate: LocalDate
    get() = toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

/** This property gives a [LocalDateTime] from a [Date] */
val Date.asLocalDateTime: LocalDateTime
    get() = toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()

/** This property gives a [LocalTime] from a [Date] */
val Date.asLocalTime: LocalTime
    get() = toInstant().atZone(ZoneId.systemDefault()).toLocalTime()

/** This property gives a [Calendar] from a [Date] */
val Date.asCalendar: Calendar
    get() = Calendar.Builder().setInstant(this).build()
