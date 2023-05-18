package domain

import Entity

data class UserWithOptional(val id: Int, val name: String, val email: String? = null) : Entity
