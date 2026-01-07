const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const app = express();
const PORT = 3000;

app.use(cors());
app.use(bodyParser.json());

// --- BASE DE DATOS SIMULADA ---
// 1. Usuarios (Incluye un Admin por defecto)
let usuarios = [
    { id: 1, email: "admin@test.com", password: "123", nombre: "Administrador", rol: "admin" },
    { id: 2, email: "user@test.com", password: "123", nombre: "Estudiante", rol: "user" }
];

let favoritos = []; // { userId, showId, title, image }
let historial = []; // { userId, query, timestamp }

// --- AUTHENTICATION ---

// 1. Login (Devuelve también el ROL)
app.post('/api/login', (req, res) => {
    const { email, password } = req.body;
    const user = usuarios.find(u => u.email === email && u.password === password);
    if (user) {
        // Devolvemos el usuario SIN la contraseña
        const { password, ...userSafe } = user;
        res.json({ success: true, token: "fake-jwt-token", user: userSafe });
    } else {
        res.status(401).json({ success: false, message: "Credenciales inválidas" });
    }
});

// 2. Registro (NUEVO)
app.post('/api/register', (req, res) => {
    const { email, password, nombre } = req.body;
    
    if (usuarios.find(u => u.email === email)) {
        return res.json({ success: false, message: "El correo ya existe" });
    }

    const newUser = {
        id: usuarios.length + 1,
        email,
        password,
        nombre,
        rol: "user" // Por defecto todos son usuarios normales
    };
    
    usuarios.push(newUser);
    res.json({ success: true, message: "Usuario registrado" });
});

// --- FAVORITOS (Sincronización) ---

app.get('/api/favoritos/:userId', (req, res) => {
    const userId = parseInt(req.params.userId);
    const userFavs = favoritos.filter(f => f.userId === userId);
    res.json(userFavs);
});

app.post('/api/favoritos', (req, res) => {
    const { userId, showId, title, image } = req.body;
    // Evitar duplicados
    if (!favoritos.find(f => f.userId === userId && f.showId === showId)) {
        favoritos.push({ userId, showId, title, image });
    }
    res.json({ success: true, message: "Guardado" });
});

// --- HISTORIAL (Requisito 3) ---

// Guardar búsqueda
app.post('/api/historial', (req, res) => {
    const { userId, query } = req.body;
    historial.push({ userId, query, timestamp: new Date().toLocaleString() });
    res.json({ success: true });
});

// Obtener historial (Lógica Admin vs Usuario)
app.get('/api/historial/:userId', (req, res) => {
    const userId = parseInt(req.params.userId);
    const solicitante = usuarios.find(u => u.id === userId);

    if (!solicitante) return res.status(404).json([]);

    if (solicitante.rol === 'admin') {
        // Requisito: Administrador ve TODO el historial
        res.json(historial); 
    } else {
        // Requisito: Usuario ve SU historial
        const miHistorial = historial.filter(h => h.userId === userId);
        res.json(miHistorial);
    }
});

// Endpoint Admin: Ver TODOS los favoritos agrupados
app.get('/api/admin/favoritos', (req, res) => {
    // Devolvemos la lista cruda, el frontend se encargará de agruparla
    res.json(favoritos);
});

app.listen(PORT, () => {
    console.log(`Servidor Roles corriendo en http://localhost:${PORT}`);
});