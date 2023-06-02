package domain.user

import Entity

data class UserWithDefault(val id: Int, val name: String, val email: String = "", val address: String? = null) : Entity
