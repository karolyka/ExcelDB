package domain.user

import Entity

data class UserMissingColumn(val id: Int, val name: String) : Entity
