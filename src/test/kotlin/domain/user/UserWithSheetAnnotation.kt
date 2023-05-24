package domain.user

import Entity
import annotations.Sheet

@Sheet(name = "User")
data class UserWithSheetAnnotation(val id: Int, val name: String) : Entity
