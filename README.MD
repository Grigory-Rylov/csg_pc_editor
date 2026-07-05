## 3D PC Case Generator & Keyboard Designer

Три варианта рендеринга:

### 1. PC Viewer (JOGL GUI)
```bash
./gradlew :viewer:run
```

### 2. Console — PNG render (headless)
```bash
./gradlew :pccase:run --args="--render"
```
Output: `pccase/stl_pccase/scene.png`

### 3. Android App (OpenGL ES)
```bash
cd android-viewer
./gradlew assembleDebug
```
APK: `android-viewer/app/build/outputs/apk/debug/app-debug.apk`

---

## Modules

| Module | Type | Назначение |
|--------|------|------------|
| `cad3d` | JVM | Генерация сплита, STL экспорт |
| `javascad` | JVM | CSG движок |
| `plugin` | JVM | Интерфейсы плагинов |
| `viewer` | JVM | Десктопный просмотрщик (JOGL) |
| `console` | JVM | Консольный экспорт STL |
| `pccase` | JVM | Модель корпуса ПК |
| `kbd_core` | JVM | Ядро клавиатуры |
| `common` | JVM | Общие утилиты |
| `android-viewer` | Android | Android приложение с OpenGL ES |

## Требования для Android сборки

- ARM64 Linux: требуется ARM64 aapt2 (кастомная сборка из `~/data/projects/android/android-arm-tools/`)
- Настройки в `~/.gradle/gradle.properties`:
  ```properties
  android.aapt2FromMavenOverride=/home/orangepi/Android/Sdk/build-tools/34.0.0/aapt2
  ```
- JDK 21
- Android SDK с `platforms;android-34` и `build-tools;34.0.0`

## VM options для десктопа

```
--add-exports=java.desktop/sun.awt=ALL-UNNAMED
--add-opens=java.desktop/sun.awt=ALL-UNNAMED
```
