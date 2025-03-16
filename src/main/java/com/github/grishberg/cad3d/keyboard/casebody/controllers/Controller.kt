package com.github.grishberg.cad3d.keyboard.casebody.controllers

import com.github.grishberg.cad3d.keyboard.ModelHolder

interface Controller {

    val width: Double
    val depth: Double
    val height: Double

    fun create(): ModelHolder
}
