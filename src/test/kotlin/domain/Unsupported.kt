package domain

import Entity
import annotations.Sheet

@Sheet("UserWithUnsupportedField")
data class Unsupported(val id: Int, val unsupported: Exception) : Entity
