package domain

import Entity

data class UserNonUniqueColumns(val id: Int, val name: String) : Entity
