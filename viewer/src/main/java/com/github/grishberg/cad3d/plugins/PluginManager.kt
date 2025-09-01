package com.github.grishberg.cad3d.plugins

import com.github.grishberg.cad3d.plugin.Cad3dPlugin

interface PluginManager {

    /**
     * Добавляем слушателя загрузки плагинов
     */
    fun setOnPluginLoadedListener(listener: OnPluginLoadedListener?)

    /**
     * Начинает слушать папку по поиску новых плагинов
     */
    fun start()

    /**
     * Останавливает слушать папку, выгружает все классы.
     */
    fun stop()

    interface OnPluginLoadedListener {

        /**
         * Вызывается в главном потоке когда список текущих плагинов изменился.
         */
        fun onPluginsLoaded(plugins: List<Cad3dPlugin>)
    }
}
