package com.github.grishberg.cad3d.plugin

interface ResultListener {

    fun onReady(result: List<VertexHolder>)
}
