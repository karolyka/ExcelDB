package domain.car

import Entity
import annotations.Column

data class CarWithMultipleKey(
    @Column(keyColumn = true) val id: Int,
    @Column(keyColumn = true) val name: String,
    val factory: CarFactory
) : Entity
