package domain

import Entity
import annotations.Sheet

@Sheet("User")
class NoPrimaryConstructor private constructor(val id: Int, val name: String) : Entity
