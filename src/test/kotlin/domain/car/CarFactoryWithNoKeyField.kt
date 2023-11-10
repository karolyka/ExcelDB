package domain.car

import Entity
import annotations.Sheet

@Sheet("CarFactory")
data class CarFactoryWithNoKeyField(val name: String) : Entity
