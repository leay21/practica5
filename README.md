# 🎬 App de Series y Películas (Cliente-Servidor)

Una aplicación Android nativa desarrollada en **Kotlin** bajo la arquitectura **MVVM**. Implementa una lógica **Offline-First** robusta, sincronizando datos entre una base de datos local (**Room**) y una API REST personalizada en **Node.js**, además de consumir la API pública de **TVMaze**.

La aplicación cuenta con gestión de usuarios, roles (Admin/User), historial de búsquedas y un sistema inteligente de recomendaciones.

---

## 📱 Capturas de Pantalla

| Login y Registro | Búsqueda y Lista | Detalles y Favoritos |
|:---:|:---:|:---:|
| ![Login Screen](https://github.com/user-attachments/assets/1100010a-2140-4064-b176-85df9a079cd2)  | ![Search Screen](https://github.com/user-attachments/assets/943a3680-0fdf-4d97-969d-e99da64caf71) | ![Favorites Screen](https://github.com/user-attachments/assets/725139b0-eaa0-46b9-891b-e43c4abb9c83) |

| Recomendaciones | Historial (Admin) | Modo Offline |
|:---:|:---:|:---:|
| ![Recommendations](https://github.com/user-attachments/assets/c5a406be-983d-4757-b1d3-6321b6a243c9) | ![History Admin](https://github.com/user-attachments/assets/8221770e-d2a8-4d6b-9833-a5ee60d6862d) | ![Offline Mode](https://github.com/user-attachments/assets/86aee987-7344-4a79-8496-860425b5c594) |

---


## ✨ Características Principales

### 🔐 Autenticación y Roles
* **Login y Registro:** Creación de cuentas y autenticación contra el backend propio.
* **Roles de Usuario:**
    * **Usuario Estándar:** Gestiona sus propios favoritos y ve su historial personal.
    * **Administrador:** Acceso a paneles exclusivos para visualizar el historial de búsquedas y favoritos de **todos** los usuarios registrados.

### 💾 Arquitectura Offline-First
* **Persistencia Local:** Uso de **Room Database** para guardar favoritos. La app es totalmente funcional sin internet.
* **Sincronización:** Los datos se guardan localmente primero y se sincronizan en segundo plano con el servidor REST cuando hay conexión.
* **Manejo de Errores:** Control de excepciones de red y feedback visual al usuario.

### 🔍 Búsqueda y API Externa
* Consumo de la **API de TVMaze** para buscar series en tiempo real.
* Visualización de portadas, títulos y resúmenes.

### 🧠 Sistema de Recomendaciones
* Algoritmo de **"Mix Aleatorio"**: Analiza el historial de búsqueda y los favoritos guardados del usuario.
* Selecciona temas al azar basados en los gustos del usuario y realiza búsquedas paralelas (`async/await`) para ofrecer sugerencias variadas y personalizadas.

### 🎨 UI/UX Personalizada
* Diseño basado en **Material Design**.
* Personalización con colores institucionales (**Azul ESCOM**).
* Títulos dinámicos en la ActionBar según el contexto (Saludo al usuario vs Título de la app).

---

## 🛠️ Tecnologías Utilizadas

### Android (Cliente)
* **Lenguaje:** Kotlin.
* **Patrón:** MVVM (Model-View-ViewModel).
* **Red:** Retrofit 2 + OkHttp (con Logging Interceptor).
* **Base de Datos:** Room (SQLite abstraction).
* **Concurrencia:** Coroutines & Flow.
* **UI:** RecyclerView, CardView, ViewBinding.

### Backend (Servidor)
* **Runtime:** Node.js.
* **Framework:** Express.js.
* **Persistencia:** Memoria (Array storage simulando DB para propósitos de la práctica).
* **Endpoints:** RESTful API.

---

## 🚀 Instalación y Configuración

Sigue estos pasos para probar el proyecto localmente.

### 1. Configurar el Backend (Servidor)

Necesitas tener instalado **Node.js**.

1. Navega a la carpeta del servidor:
   ```bash
   cd mi-api-rest
2. Instala las dependencias:
   ```bash
   npm install
3. Ejecuta el servidor:
   ```bash
   node server.js
El servidor correrá en el puerto 3000.

### 2. Configurar el cliente

1. Abre el proyecto en Android Studio.
2. Averigua la dirección IP local de tu computadora (ej. ejecutando ipconfig o ifconfig).
3. Abre el archivo: **app/src/main/java/com/example/practica5/data/RetrofitClient.kt**.
4. Modifica la IP en la configuración de myApi para que apunte a tu computadora:
   ```kotlin
   .baseUrl("[http://192.168.1.](http://192.168.1.)XX:3000/") // <-- Pon tu IP aquí
5. Sincroniza Gradle y ejecuta la app en un emulador o dispositivo físico. Nota: Asegúrate de que el celular y la PC estén en la misma red Wi-Fi.

---

## 👤 Autor
**Toral Alvarez Yael Adair**
