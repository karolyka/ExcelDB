package domain.car

import Entity

data class Car(val id: Int, val name: String, val factory: CarFactory) : Entity
