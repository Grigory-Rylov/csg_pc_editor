# PC Case Generator / Viewer

3D-модель корпуса ПК из алюминиевого профиля 20×20 мм
с материнской платой, видеокартой и блоком питания.

## Сборка

```bash
./gradlew pccase:build
```

## Запуск

### GUI-режим (интерактивный просмотрщик)

Запускает окно с 3D-сценой, панель с чекбоксами для скрытия/показа компонентов, управление мышью:
- **Перетаскивание**: вращение камеры
- **Ctrl + перетаскивание**: панорамирование
- **Колёсико мыши**: зум
- **R**: сброс камеры

```bash
./gradlew pccase:run
```

### Консольный режим (рендер в PNG)

Сохраняет сцену в `pccase/stl_pccase/scene.png` (1920×1080),
не требует дисплея:

```bash
./gradlew pccase:run --args="--render"
```

## Компоненты

| Компонент | Модель | Размеры, мм | Цвет |
|-----------|--------|-------------|------|
| Рама | Алюминиевый профиль 20×20 мм | 530×350×330 | Синеватый (стойки) / серый (балки) |
| Материнская плата | Tyan S8030 (2× SP3, 16× DDR4) | 280×260×1.6 | Зелёный |
| Видеокарта | Gigabyte RTX 3090 Turbo (blower) | 290×112×38 | Красный |
| Блок питания | ATX | 150×86×140 | Тёмно-серый |
| **Габариты корпуса** | | **530×350×330** | |

## Учёт алюминиевого профиля

При каждом запуске формируется отчёт `pccase/stl_pccase/profile_report.txt`:

```
  Horizontal (Z): 290.0mm x 6pcs = 1740.0mm
  Vertical (Y):   350.0mm x 4pcs = 1400.0mm
  Horizontal (X): 490.0mm x 6pcs = 2940.0mm
  Total: 6080.0mm (6.1m)
  Profile: 20x20mm aluminum extrusion
```

Используется для заказа профилей нужной длины.

## Структура модуля

```
pccase/src/main/java/com/github/grishberg/cad3d/
├── Main.kt               # Точка входа (GUI / --render)
pccase/src/main/java/com/github/grishberg/cad3d/pccase/
├── PcCaseViewer.kt       # Интерактивный просмотрщик (Java2D)
├── SceneRenderer.kt      # Софтверный рендерер (Java2D, PNG)
├── AluminumProfile.kt    # Фабрика профилей + BOM-трекер
├── PcFrame.kt            # Рама из профилей
├── Motherboard.kt        # Tyan S8030
├── Gpu.kt                # Gigabyte RTX 3090 Turbo
└── Psu.kt                # ATX блок питания
```
