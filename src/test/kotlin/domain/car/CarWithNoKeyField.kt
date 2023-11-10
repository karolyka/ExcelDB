package domain.car

import Entity

data class CarWithNoKeyField(val id: Int, val name: String, val factory: CarFactoryWithNoKeyField) : Entity
