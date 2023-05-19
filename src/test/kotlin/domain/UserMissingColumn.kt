package domain

import Entity

data class UserMissingColumn(val id: Int, val name: String) : Entity
