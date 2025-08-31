package com.github.grishberg.cad3d.plugin

interface Cad3dPlugin {

    fun requestModels(config: Config, listener: ResultListener)

    val name: String

    val version: Long
}
