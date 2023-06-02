package domain.car

import Entity

data class CarWithEngine(val id: Int, val name: String, val factory: CarFactory, val engine: CarFactory?) : Entity
