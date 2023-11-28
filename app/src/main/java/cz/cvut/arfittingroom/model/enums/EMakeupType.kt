package cz.cvut.arfittingroom.model.enums

import cz.cvut.arfittingroom.R

//TODO DIFFERENT TYPES FOR FORMS?
//TODO load from DB?
enum class EMakeupType(val drawableId: Int) {
    LINER(R.drawable.liner),
    BLUSH(R.drawable.blush),
    LIPSTICK(R.drawable.lipstick)
}