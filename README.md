# San Andreas Edition (Forge 1.20.1)

Fase 1: HUD estilo GTA SA (arma, reloj, vida, dinero).

## Como abrirlo
1. Baja el MDK oficial de Forge 1.20.1 (47.3.0) desde files.minecraftforge.net.
2. Copia de ese MDK la carpeta `gradle/` y los archivos `gradlew` y `gradlew.bat` a esta carpeta.
3. Necesitas JDK 17.
4. Abre la carpeta en IntelliJ IDEA (importa como proyecto Gradle) y ejecuta `./gradlew genIntellijRuns`, luego la run config `runClient`.
5. Para generar el .jar: `./gradlew build` (sale en build/libs).
