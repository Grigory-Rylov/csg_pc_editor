package com.github.grishberg.cad3d.common;

import com.github.grishberg.cad3d.ui.DebugVisualizer;

/**
 * Интерфейс для представления отладочной команды
 */
public interface DebugCmd {

    /**
     * Возвращает описание команды
     * @return описание команды
     */
    String getDescription();

    /**
     * Отрисовывает отладочную информацию поверх 3D фигуры
     * @param debugVisualizer визуализатор отладки для создания примитивов
     */
    void render(DebugVisualizer debugVisualizer);
}
