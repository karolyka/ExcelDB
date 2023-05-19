package domain

import Entity
import java.time.LocalDateTime
import java.util.Date

data class BoolAndFormula(
    val bool: Boolean,
    val numericFormula: Int,
    val stringFormula: String,
    val localDateTime: LocalDateTime,
    val date: Date
) :
    Entity
