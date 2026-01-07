const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const app = express();
const PORT = 3000;

app.use(cors());
app.use(bodyParser.json());

// Base de datos simulada en memoria
let usuarios = [{ id: 1, email: "test@test.com", password: "123", nombre: "Estudiante" }];
let favoritos = []; // { userId, showId, title, image }
let historial = []; // { userId, query, timestamp }

// 1. Login (Persistencia de sesión)
app.post('/api/login', (req, res) => {
    const { email, password } = req.body;
    const user = usuarios.find(u => u.email === email && u.password === password);
    if (user) {
        res.json({ success: true, token: "fake-jwt-token-123", user });
    } else {
        res.status(401).json({ success: false, message: "Credenciales inválidas" });
    }
});

// 2. Obtener Favoritos (Sincronización)
app.get('/api/favoritos/:userId', (req, res) => {
    const userId = parseInt(req.params.userId);
    const userFavs = favoritos.filter(f => f.userId === userId);
    res.json(userFavs);
});

// 3. Agregar Favorito (Sincronización)
app.post('/api/favoritos', (req, res) => {
    const { userId, showId, title, image } = req.body;
    // Evitar duplicados
    if (!favoritos.find(f => f.userId === userId && f.showId === showId)) {
        favoritos.push({ userId, showId, title, image });
    }
    res.json({ success: true, message: "Guardado" });
});

// 4. Guardar Historial (Para admin y recomendaciones)
app.post('/api/historial', (req, res) => {
    const { userId, query } = req.body;
    historial.push({ userId, query, timestamp: new Date() });
    res.json({ success: true });
});

// 5. Endpoint Admin: Ver todo el historial
app.get('/api/admin/historial', (req, res) => {
    res.json(historial);
});

app.listen(PORT, () => {
    console.log(`API corriendo en http://localhost:${PORT}`);
    // Ojo: Para acceder desde el emulador Android usa la IP: 10.0.2.2
});