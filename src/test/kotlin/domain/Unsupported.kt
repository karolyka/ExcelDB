package domain

import Entity
import annotations.Sheet
import java.lang.Exception

@Sheet("UserWithUnsupportedField")
data class Unsupported(val id: Int, val unsupported: Exception) : Entity
