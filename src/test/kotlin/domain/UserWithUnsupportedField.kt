package domain

import Entity

class UnsupportedClass
data class UserWithUnsupportedField(val id: Int, val name: String, val unsupported: UnsupportedClass) : Entity
