package com.proyecto.emilite.service;

import java.util.Map;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MicroservicioPdfService {

    // Apuntamos al puerto 8080 que configuramos en tu Apache de Linux
    private final String URL_MICROSERVICIO = "http://localhost:8080/fitexpert-api/upload_pdf.php";

    public String subirPdfAMongo(MultipartFile archivoPdf) {
        if (archivoPdf == null || archivoPdf.isEmpty()) {
            return null;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();

            // 1. Configurar encabezados HTTP como form-data (Multipart)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // 2. Convertir los bytes del archivo para enviarlos por red
            ByteArrayResource recursoArchivo = new ByteArrayResource(archivoPdf.getBytes()) {
                @Override
                public String getFilename() {
                    return archivoPdf.getOriginalFilename();
                }
            };

            // 3. Empaquetar el cuerpo con la llave 'pdf' que espera PHP
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("pdf", recursoArchivo);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 4. Consumir el microservicio por método POST
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(URL_MICROSERVICIO, requestEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Casteamos de forma segura
                @SuppressWarnings("unchecked")
                Map<String, Object> jsonResponse = (Map<String, Object>) response.getBody();
                
                
                if (jsonResponse != null && jsonResponse.containsKey("id_mongo")) {
                    return (String) jsonResponse.get("id_mongo");
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Falló la conexión con el microservicio PHP: " + e.getMessage());
        }
        return null;
    }
}