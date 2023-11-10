package domain.car

import Entity
import annotations.Sheet

@Sheet("Car")
data class CarWithUnsupportedKeyField(val id: Int, val name: String, val factory: CarFactoryWithUnsupportedKeyField) :
    Entity
