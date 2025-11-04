Manuel Abrego Sorli 2025
# 💸 Transferencias Ya

## 🏫 Introducción

**Transferencias Ya** es una aplicación educativa desarrollada en **Kotlin**, cuyo propósito es simular el funcionamiento básico de un sistema de transferencias bancarias.  
Fue creada como proyecto de exposición para la **feria de ciencias**, combinando conocimientos de programación, bases de datos y diseño de software.

El sistema permite gestionar **usuarios, cuentas y transacciones** mediante una interfaz de consola interactiva.  
Su objetivo es mostrar cómo los principios de la informática y la programación orientada a objetos pueden aplicarse para crear soluciones reales y seguras, similares a las que utilizan los bancos digitales.

👨‍👩‍👧‍👦 **Para padres:** Este proyecto demuestra cómo los estudiantes aplican la lógica y la tecnología para resolver problemas y optimizar soluciones que nos enfrenta el mundo real.  
👩‍🏫 **Para profesores:** El sistema implementa buenas prácticas de desarrollo, uso de Gradle, organización en capas y patrones de diseño, y una práctica sólida de las reglas SOLID y la POO. 
🧑‍🎓 **Para alumnos:** Es un ejemplo accesible para aprender sobre programación orientada a objetos. 

---

## 🚀 Requisitos previos

Antes de ejecutar la aplicación, asegurate de tener instalado lo siguiente:

| Herramienta | Versión recomendada | Verificación |
|--------------|---------------------|---------------|
| **JDK (Java Development Kit)** | 17 | `java -version` |
| **Gradle**      | versión 9.0.0               | `gradle -v` |
| **Git** *(para clonar el repositorio)* | Última versión estable | `git --version` |



---

## 📦 Clonar o descargar el proyecto

Podés obtener el proyecto de dos maneras:

### Opción 1: Clonar con Git

```bash
git clone https://github.com/tu-usuario/Transferencias_Ya.git
cd Transferencias_Ya
```

### Opción 2: Descargar manualmente

1. Descargá el archivo ZIP del repositorio o desde la entrega del proyecto.  
2. Extraé el contenido en una carpeta local.  
3. Abrí una terminal en la carpeta raíz del proyecto

---

## ⚙️ Compilación y ejecución

### Paso 1: Compilar el proyecto

En Linux o macOS:
```bash
./gradlew build
```

En Windows:
```bash
gradlew.bat build
```

Esto genera los archivos compilados dentro de `build/libs/`, incluyendo el JAR ejecutable.

### Paso 2: Ejecutar la aplicación

Podés ejecutarla directamente con Gradle:

```bash
./gradlew run
```

O bien, ejecutar el JAR generado:

```bash
java -jar build/libs/app-1.0-SNAPSHOT.jar
```

---

## 🧩 Estructura del proyecto

```
Transferencias_Ya/
├── build.gradle.kts        # Configuración de Gradle
├── settings.gradle.kts     # Configuración del nombre del proyecto
├── src/
│   ├── main/
│   │   └── kotlin/org/example/
│   │       ├── database/     # Conexión y manejo de base de datos
│   │       ├── model/        # Clases de dominio (Usuario, Cuenta, Transacción)
│   │       ├── service/      # Lógica de negocio
│   │       ├── ui/           # Interfaz de usuario por consola
│   │       └── util/         # Utilidades generales
│   └── test/                 # Tests unitarios (si aplica)
└── build/                    # Archivos generados automáticamente
```

---

## ⚙️ Configuración adicional

Actualmente el proyecto utiliza clases en el paquete `org.example.database`, pero no requiere configuraciones externas.  
Si se agregara soporte a base de datos o persistencia, las configuraciones se realizarían en el archivo:
```
src/main/resources/config.properties
```
(asegurate de crearlo si el proyecto lo requiere).

No se necesitan variables de entorno ni dependencias externas adicionales.

---

## 🧪 Verificación

Para comprobar que todo funciona correctamente:

1. Ejecutá `./gradlew build`  
   - Si todo está bien, el build finalizará sin errores.  
2. Luego corré la aplicación:  
   - `./gradlew run`  
   - Deberías ver un menú de opciones en consola (gestión de usuarios, cuentas y transacciones).

---

## 👨‍💻 Autor

Proyecto desarrollado por **Manuel Abrego Sorli**  
Materia: *Programación II (2025)*  
Lenguaje: *Kotlin 1.9+*

---

## 📄 Licencia

Este proyecto se distribuye con fines educativos y puede modificarse libremente con fines académicos.

