package domain

import Entity
import annotations.Column

data class UserWithAnnotations(val id: Int, @Column(name = "fullName") val name: String) : Entity
