package domain.user

import Entity

data class UserWithOptional(val id: Int, val name: String, val email: String? = null) : Entity
