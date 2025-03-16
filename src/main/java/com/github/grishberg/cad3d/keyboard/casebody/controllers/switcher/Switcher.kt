package com.github.grishberg.cad3d.keyboard.casebody.controllers.switcher

import com.github.grishberg.cad3d.keyboard.ModelHolder
import eu.printingin3d.javascad.models.Abstract3dModel

interface Switcher {

    fun createSwitcher(): ModelHolder
    fun createSwitcherHole(): Abstract3dModel
}
