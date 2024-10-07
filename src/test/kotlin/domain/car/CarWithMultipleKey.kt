package domain.car

import Entity
import annotations.Key

data class CarWithMultipleKey(
    @Key val id: Int,
    @Key val name: String,
    val factory: CarFactory,
) : Entity
