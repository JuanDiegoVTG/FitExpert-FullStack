<?php
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, GET");
header("Access-Control-Allow-Headers: Content-Type");
require_once __DIR__ . '/vendor/autoload.php';

$javaAppUrl = getenv('JAVA_APP_URL') ?: "http://localhost:8082";

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    header("Content-Type: application/json; charset=UTF-8");

    if (!isset($_FILES['pdf']) || $_FILES['pdf']['error'] !== UPLOAD_ERR_OK) {
        http_response_code(400);
        echo json_encode(["status" => "error", "message" => "No se recibio un archivo PDF valido."]);
        exit();
    }

    try {
        
        

        // Conexión al MongoDB local de tu Ubuntu 
        /** @var mixed $manager */
        $mongoUri = getenv('MONGODB_URI') ?: "mongodb://localhost:27017"; 
        $manager = new \MongoDB\Driver\Manager($mongoUri);
        
        $file = $_FILES['pdf'];
        $fileName = $file['name'];
        $fileData = file_get_contents($file['tmp_name']); 
        
        /** @var mixed $bulk */
        $bulk = new \MongoDB\Driver\BulkWrite;
        
        $documentoPdf = [
            '_id' => new \MongoDB\BSON\ObjectId,
            'nombre_archivo' => $fileName,
            'contenido_binario' => new \MongoDB\BSON\Binary($fileData, 0),
            'fecha_subida' => date("Y-m-d H:i:s")
        ];
        
        $bulk->insert($documentoPdf);
        $manager->executeBulkWrite('fitexpert_db.hojas_vida', $bulk);
        
        http_response_code(201);
        echo json_encode([
            "status" => "success",
            "message" => "¡Melo! PDF almacenado en MongoDB con exito.",
            "id_mongo" => (string)$documentoPdf['_id']
        ]);
        exit();

    } catch (Exception $e) {
        http_response_code(500);
        echo json_encode(["status" => "error", "message" => "Error en MongoDB: " . $e->getMessage()]);
        exit();
    }
}
?>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>FitExpert NoSQL - Hojas de Vida</title>
    <style>
        :root {
            --bg-main: #0b0d19;
            --bg-card: #151828;
            --neon-blue: #00d2ff;
            --neon-green: #00ffa3;
            --text-main: #e2e8f0;
            --text-muted: #94a3b8;
        }

        body {
            font-family: 'Segoe UI', system-ui, sans-serif;
            background: var(--bg-main);
            background-image: radial-gradient(circle at 50% 20%, #1e2342 0%, var(--bg-main) 70%);
            color: var(--text-main);
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            margin: 0;
        }

        .card {
            background: var(--bg-card);
            padding: 40px;
            border-radius: 20px;
            border: 1px solid #232846;
            box-shadow: 0 15px 35px rgba(0, 0, 0, 0.7), 0 0 15px rgba(0, 210, 255, 0.1);
            text-align: center;
            max-width: 420px;
            width: 100%;
            transition: transform 0.3s ease;
        }

        .card:hover {
            transform: translateY(-5px);
            border-color: var(--neon-blue);
            box-shadow: 0 15px 35px rgba(0, 0, 0, 0.7), 0 0 25px rgba(0, 210, 255, 0.2);
        }

        h2 {
            color: #fff;
            font-size: 26px;
            margin-top: 0;
            margin-bottom: 8px;
            letter-spacing: 0.5px;
            text-shadow: 0 0 10px rgba(255,255,255,0.1);
        }

        h2 span {
            color: var(--neon-blue);
            text-shadow: 0 0 15px rgba(0, 210, 255, 0.5);
        }

        p {
            color: var(--text-muted);
            font-size: 14px;
            margin-bottom: 30px;
        }

        .file-box {
            border: 2px dashed #2d3454;
            padding: 30px 20px;
            border-radius: 12px;
            background: #0f111f;
            margin-bottom: 25px;
            position: relative;
            transition: all 0.3s ease;
        }

        .file-box:hover {
            border-color: var(--neon-blue);
            background: #12162b;
        }

        input[type="file"] {
            width: 100%;
            color: var(--text-muted);
            font-size: 14px;
            cursor: pointer;
        }

        button {
            background: linear-gradient(135deg, var(--neon-blue), #0084ff);
            color: #fff;
            border: none;
            padding: 14px 28px;
            font-size: 16px;
            font-weight: 600;
            border-radius: 10px;
            cursor: pointer;
            width: 100%;
            box-shadow: 0 4px 15px rgba(0, 210, 255, 0.3);
            transition: all 0.2s ease;
        }

        button:hover {
            filter: brightness(1.1);
            box-shadow: 0 6px 20px rgba(0, 210, 255, 0.5);
            transform: translateY(-1px);
        }

        /* Vista de Respuesta de Éxito */
        .response-container {
            display: none;
            margin-top: 20px;
            padding: 15px;
            background: #0d1f1a;
            border: 1px solid var(--neon-green);
            border-radius: 10px;
            text-align: left;
        }

        .response-title {
            color: var(--neon-green);
            font-weight: bold;
            margin-bottom: 5px;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .mongo-id {
            font-family: 'Courier New', monospace;
            background: #111424;
            padding: 8px;
            border-radius: 5px;
            font-size: 13px;
            word-break: break-all;
            color: #a6adc8;
            border: 1px solid #232846;
            display: block;
            margin-top: 5px;
        }
    </style>
</head>
<body>

    <div class="card">
        <h2>Fit<span>Expert</span> NoSQL</h2>
        <p>Gestión Centralizada de Hojas de Vida</p>
        
        <form id="uploadForm" enctype="multipart/form-data">
            <div class="file-box">
                <input type="file" name="pdf" accept="application/pdf" required>
            </div>
            <button type="submit" id="btnSubmit">Almacenar Documento</button>
        </form>

        <br>

        <a href="<?php echo $javaAppUrl; ?>/admin/usuarios" class="btn-back">← Volver a Gestión de Usuarios</a>

        <div id="responseBox" class="response-container">
            <div class="response-title">✓ Registro Almacenado</div>
            <span style="font-size: 13px; color: var(--text-muted);">ID de Referencia NoSQL:</span>
            <span id="idMongoSpan" class="mongo-id"></span>
        </div>
    </div>

    <script>
        document.getElementById('uploadForm').addEventListener('submit', function(e) {
            e.preventDefault();
            
            const btn = document.getElementById('btnSubmit');
            const formData = new FormData(this);
            
            btn.innerText = 'Procesando...';
            btn.disabled = true;

            fetch('', {
                method: 'POST',
                body: formData
            })
            .then(res => res.json())
            .then(data => {
                btn.innerText = 'Almacenar Documento';
                btn.disabled = false;
                
                if(data.status === 'success') {
                    document.getElementById('idMongoSpan').innerText = data.id_mongo;
                    document.getElementById('responseBox').style.display = 'block';
                    this.reset();
                } else {
                    alert('Error: ' + data.message);
                }
            })
            .catch(err => {
                btn.innerText = 'Almacenar Documento';
                btn.disabled = false;
                alert('Error en la conexión con el servidor.');
            });
        });
    </script>
</body>
</html>