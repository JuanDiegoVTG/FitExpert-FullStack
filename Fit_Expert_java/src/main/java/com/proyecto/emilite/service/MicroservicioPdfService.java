package com.proyecto.emilite.service;

import java.util.Map;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MicroservicioPdfService {

    // URL base de tu microservicio en la nube (Render)
    private final String URL_BASE = "https://fitexpert-fullstack-m8vv.onrender.com/fitexpert-api";
    
    // Inicializamos el RestTemplate una sola vez para toda la clase
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Sube el archivo PDF al microservicio PHP y retorna el ID generado en MongoDB
     */
    public String subirPdfAMongo(MultipartFile archivoPdf) {
        if (archivoPdf == null || archivoPdf.isEmpty()) {
            return null;
        }

        try {
            // 1. Configurar encabezados HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // 2. Convertir los bytes del archivo
            ByteArrayResource recursoArchivo = new ByteArrayResource(archivoPdf.getBytes()) {
                @Override
                public String getFilename() {
                    return archivoPdf.getOriginalFilename();
                }
            };

            // 3. Empaquetar el cuerpo
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("pdf", recursoArchivo);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 4. Consumir el microservicio POST
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(URL_BASE + "/upload_pdf.php", requestEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> jsonResponse = (Map<String, Object>) response.getBody();
                
                if (jsonResponse != null && jsonResponse.containsKey("id_mongo")) {
                    return (String) jsonResponse.get("id_mongo");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error al subir a Mongo vía PHP: " + e.getMessage());
        }
        return null;
    }

    /**
     * Recupera el PDF desde el microservicio PHP usando el ID de MongoDB
     */
    public byte[] obtenerPdfDeMongo(String mongoId) {
        if (mongoId == null || mongoId.isEmpty()) return null;
        
        try {
            String url = URL_BASE + "/get_pdf.php?id=" + mongoId;
            return restTemplate.getForObject(url, byte[].class);
        } catch (Exception e) {
            System.err.println("❌ Error recuperando PDF de Mongo: " + e.getMessage());
            return null;
        }
    }
}