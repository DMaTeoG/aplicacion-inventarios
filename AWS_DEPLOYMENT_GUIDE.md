# Guía de Despliegue en AWS (EC2 m7i-flex.large + Git Clone + Nginx + Spring Boot + Render DB)

¡Excelente decisión! La instancia **m7i-flex.large** de AWS es una máquina sumamente potente y moderna. Cuenta con **2 vCPUs** (procesadores Intel Xeon Scalable de 4ª generación) y **8 GiB de memoria RAM**. 

Con estas especificaciones técnicas, el servidor **soportará de sobra** el clonado de Git y el proceso de compilación de Maven (Spring Boot) y npm (Angular) directamente en el servidor. Esto simplifica enormemente tu flujo de trabajo: **no tendrás que subir pesados archivos JAR o ZIP por internet**, simplemente harás un `git pull` en el servidor y compilarás todo allí.

---

## 📌 Arquitectura de Despliegue en la Instancia m7i-flex.large
1. **Frontend (Angular)**: Compilado directamente en el servidor y servido por **Nginx** en la ruta pública `/var/www/html`.
2. **Backend (Spring Boot)**: Compilado directamente en el servidor, ejecutándose en segundo plano mediante un servicio **Systemd** en el puerto interno `8085`.
3. **Proxy Inverso (Nginx)**: Recibe el tráfico en los puertos `80` (HTTP) y `443` (HTTPS), sirve la SPA de Angular, y redirige limpiamente las peticiones `/api` al backend Spring Boot local, resolviendo problemas de CORS automáticamente.
4. **Base de Datos**: PostgreSQL administrada externamente en **Render.com**.
5. **Almacenamiento**: Bucket de **AWS S3** para reportes y documentos.

---

## 🛠️ Paso 1: Configurar e Iniciar la Instancia EC2

1. **Lanzar Instancia**: Selecciona **Ubuntu Server 24.04 LTS** como sistema operativo.
2. **Tipo de instancia**: Busca y selecciona `m7i-flex.large`.
3. **Grupo de Seguridad (Security Group)**:
   Añade las siguientes reglas de entrada para permitir conexiones:
   * **SSH** (puerto `22`): Tu IP o `0.0.0.0/0`.
   * **HTTP** (puerto `80`): `0.0.0.0/0`.
   * **HTTPS** (puerto `443`): `0.0.0.0/0`.
   *(Nota: Por seguridad, mantén el puerto `8085` cerrado. Nginx será el único que se comunique con la API de forma interna).*

---

## 🔑 Paso 2: Conectarse por SSH desde Windows

Abre **PowerShell** en tu máquina local en la carpeta donde guardaste tu llave `.pem`:

```powershell
ssh -i "inventario-key.pem" ubuntu@<DIRECCION-IP-PUBLICA-DE-EC2>
```

---

## 📦 Paso 3: Instalar las Herramientas de Compilación en el Servidor

Dado que compilaremos todo directamente en la instancia, instalaremos el kit de desarrollo de Java (JDK), Node.js, npm, git y las librerías necesarias:

```bash
# 1. Actualizar el gestor de paquetes de Ubuntu
sudo apt update && sudo apt upgrade -y

# 2. Instalar Git
sudo apt install git -y

# 3. Instalar Java 21 JDK (Kit de Desarrollo completo para compilar Spring Boot)
sudo apt install openjdk-21-jdk -y

# 4. Instalar Node.js (versión 20.x LTS) y npm para compilar Angular
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install nodejs -y

# 5. Instalar Nginx para servir el frontend y hacer de proxy inverso
sudo apt install nginx -y
sudo systemctl enable nginx
sudo systemctl start nginx

# 6. Verificar que todas las herramientas estén listas
git --version
java -version
node -v
npm -v
```

---

## 🚀 Paso 4: Clonar el Repositorio e Instalar

Clonaremos el código en la carpeta del usuario `ubuntu` (directorio `/home/ubuntu/`):

```bash
# Asegurarse de estar en la carpeta home
cd /home/ubuntu

# Clonar el proyecto (reemplaza con la URL real de tu repositorio Git)
git clone <URL_DE_TU_REPOSITORIO_GIT> aplicacion-inventarios

# Entrar a la carpeta del proyecto
cd aplicacion-inventarios
```

---

## 💻 Paso 5: Compilar y Desplegar el Backend (Spring Boot)

### 1. Modificar los Servicios del Frontend a Rutas Relativas (¡Importante!)
Para evitar problemas de CORS y no depender de IPs estáticas hardcodeadas, asegúrate de que en tus archivos TypeScript de Angular (`stock.service.ts`, `product.service.ts`, `auth.service.ts`, `movimiento.service.ts`) las llamadas apunten a **rutas relativas** en lugar de `http://localhost:8085`.

*Ejemplo en `stock.service.ts`:*
```typescript
// Cambiar esto:
// private apiUrl = 'http://localhost:8085/api/stocks';

// Por esto:
private apiUrl = '/api/stocks';
```

### 2. Compilar el Backend en el Servidor
Entra en la carpeta del backend y ejecuta el empaquetado:

```bash
cd /home/ubuntu/aplicacion-inventarios/app

# Otorgar permisos de ejecución al Maven Wrapper (si aplica)
chmod +x mvnw

# Compilar y empaquetar el JAR saltando los tests unitarios
./mvnw clean package -DskipTests
```
*Gracias al procesador y memoria de tu `m7i-flex.large`, esto terminará rápidamente y generará el archivo `target/app-0.0.1-SNAPSHOT.jar`.*

### 3. Crear el Servicio Systemd para la API
Para mantener la API corriendo siempre en segundo plano:

```bash
sudo nano /etc/systemd/system/inventario-api.service
```

Pega el siguiente contenido (configura tus credenciales reales de base de datos e integraciones):

```ini
[Unit]
Description=API de Sistema de Inventarios (Spring Boot)
After=syslog.target network.target

[Service]
User=ubuntu
WorkingDirectory=/home/ubuntu/aplicacion-inventarios/app
ExecStart=/usr/bin/java -jar /home/ubuntu/aplicacion-inventarios/app/target/app-0.0.1-SNAPSHOT.jar
SuccessExitStatus=143
Restart=always
RestartSec=10

# Variables de entorno
Environment=SPRING_DATASOURCE_URL=jdbc:postgresql://dpg-d8atrn8jo6nc7383bcd0-a.oregon-postgres.render.com:5432/inventario_6kis
Environment=SPRING_DATASOURCE_USERNAME=admin
Environment=SPRING_DATASOURCE_PASSWORD=IxeQmKUHzWPyik2PVpLV6NaSAQQOLgiO
Environment=SERVER_PORT=8085
Environment=APP_JWT_SECRET=40d58e146ca34d18a86595af66fdd20615926700f2b1417c9ca149f543d560a7

# Configuración de AWS S3 (Opcional, si almacenas archivos físicos en S3)
# Environment=AWS_ACCESS_KEY_ID=TU_ACCESS_KEY
# Environment=AWS_SECRET_ACCESS_KEY=TU_SECRET_KEY
# Environment=AWS_S3_BUCKET_NAME=TU_BUCKET_NAME
# Environment=AWS_REGION=us-east-1

[Install]
WantedBy=multi-user.target
```
*Guarda con `Ctrl+O`, Enter y sal con `Ctrl+X`.*

### 4. Habilitar e Iniciar el Servicio
```bash
sudo systemctl daemon-reload
sudo systemctl enable inventario-api
sudo systemctl start inventario-api

# Verificar que esté activo (en verde)
sudo systemctl status inventario-api
```
*(Puedes ver los logs en vivo con: `journalctl -u inventario-api -f -n 100`)*

---

## 🎨 Paso 6: Compilar y Desplegar el Frontend (Angular)

Ahora compilaremos el frontend Angular directamente en el servidor y moveremos los archivos de producción al directorio público de Nginx.

```bash
cd /home/ubuntu/aplicacion-inventarios/frontend

# 1. Instalar las dependencias de node
npm install

# 2. Compilar el frontend para producción
npm run build

# 3. Limpiar la carpeta pública de Nginx
sudo rm -rf /var/www/html/*

# 4. Copiar los archivos compilados a la carpeta pública de Nginx
# Nota: Verifica si la salida de Angular se crea en 'dist/frontend/browser' o similar.
sudo cp -r dist/frontend/browser/* /var/www/html/
```

---

## 🛡️ Paso 7: Configurar Nginx (Servidor Web & Proxy Inverso)

Configuraremos Nginx para que sirva los archivos de Angular y redirija todas las peticiones `/api/*` al backend que está escuchando localmente en el puerto `8085`.

```bash
sudo nano /etc/nginx/sites-available/default
```

Reemplaza todo el contenido por la siguiente plantilla profesional:

```nginx
server {
    listen 80 default_server;
    listen [::]:80 default_server;

    server_name _; # Coloca tu dominio aquí en el futuro (ej. mi-sistema.com)

    root /var/www/html;
    index index.html index.htm;

    # 1. Configuración de Frontend (Angular SPA)
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 2. Configuración de Backend (Proxy inverso a Spring Boot)
    location /api/ {
        proxy_pass http://127.0.0.1:8085/api/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # Timeouts de red razonables
        proxy_connect_timeout 90s;
        proxy_send_timeout 90s;
        proxy_read_timeout 90s;
    }
}
```
*Guarda con `Ctrl+O`, Enter y sal con `Ctrl+X`.*

Prueba la configuración y reinicia Nginx:
```bash
sudo nginx -t
sudo systemctl restart nginx
```

---

## 🔄 Paso 8: ¡El Super-Paso! Script de Actualización Automática (CI/CD Express)

Una de las mayores ventajas de usar un servidor potente y Git es que actualizar el sistema se vuelve facilísimo. 

Puedes crear un script automatizado en tu servidor para que, cada vez que hagas cambios en Windows y los subas a GitHub, actualices todo el servidor con un solo comando:

1. Crea el script en la raíz de tu servidor:
   ```bash
   nano /home/ubuntu/update.sh
   ```

2. Pega este código:
   ```bash
   #!/usr/bin/env bash
   set -e

   echo "🚀 Iniciando proceso de actualización automática..."

   cd /home/ubuntu/aplicacion-inventarios

   echo "📥 Descargando los últimos cambios desde Git..."
   git pull origin main

   echo "☕ Compilando el Backend (Spring Boot)..."
   cd app
   ./mvnw clean package -DskipTests

   echo "🔄 Reiniciando el Backend..."
   sudo systemctl restart inventario-api

   echo "🎨 Instalando y Compilando el Frontend (Angular)..."
   cd ../frontend
   npm install
   npm run build

   echo "🧹 Limpiando y desplegando en Nginx..."
   sudo rm -rf /var/www/html/*
   sudo cp -r dist/frontend/browser/* /var/www/html/

   echo "🔄 Reiniciando Nginx..."
   sudo systemctl restart nginx

   echo "✅ ¡Actualización y despliegue completados con éxito!"
   ```
   *Guarda con `Ctrl+O`, Enter y sal con `Ctrl+X`.*

3. Dale permisos de ejecución al script:
   ```bash
   chmod +x /home/ubuntu/update.sh
   ```

A partir de ahora, cada vez que subas cambios a Git, simplemente conéctate a tu EC2 y escribe:
```bash
./update.sh
```
¡Y tu servidor se actualizará y compilará de forma completamente automatizada en menos de 2 minutos!
