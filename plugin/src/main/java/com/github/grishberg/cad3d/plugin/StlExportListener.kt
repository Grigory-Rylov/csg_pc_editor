package com.github.grishberg.cad3d.plugin

interface StlExportListener {

    fun onExportPlan(fileNames: List<String>)

    fun onExportStart(fileName: String)

    fun onExportFinish(fileName: String, success: Boolean, errorMessage: String? = null)

    fun onAllFinished()
}
