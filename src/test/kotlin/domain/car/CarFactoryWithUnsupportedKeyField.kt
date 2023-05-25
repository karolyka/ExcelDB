package domain.car

import Entity
import annotations.Sheet
import domain.Unsupported

@Sheet("CarFactory")
data class CarFactoryWithUnsupportedKeyField(val id: Unsupported, val name: String) : Entity
