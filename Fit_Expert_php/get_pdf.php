<?php
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET");
require_once __DIR__ . '/vendor/autoload.php';

if (isset($_GET['id'])) {
    try {
        $mongoUri = getenv('MONGODB_URI') ?: "mongodb://localhost:27017";
        $manager = new \MongoDB\Driver\Manager($mongoUri);
        
        $id = new \MongoDB\BSON\ObjectId($_GET['id']);
        $query = new \MongoDB\Driver\Query(['_id' => $id]);
        $cursor = $manager->executeQuery('fitexpert_db.hojas_vida', $query);
        $result = $cursor->toArray();

        if (count($result) > 0) {
            $archivo = $result[0];
            header("Content-Type: application/pdf");
            echo $archivo->contenido_binario->getData();
            exit();
        } else {
            http_response_code(404);
            echo "PDF no encontrado";
        }
    } catch (Exception $e) {
        http_response_code(500);
        echo "Error de conexión a MongoDB";
    }
}
?>