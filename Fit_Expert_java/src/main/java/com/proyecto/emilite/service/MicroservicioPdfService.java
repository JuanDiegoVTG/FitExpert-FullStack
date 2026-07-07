package com.proyecto.emilite.service;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@SuppressWarnings("null")
@Service
public class MicroservicioPdfService {

    // Lee la propiedad desde application.properties
    @Value("${api.php.url}")
    private String urlBase;

    private final RestTemplate restTemplate = new RestTemplate();

    public String subirPdfAMongo(MultipartFile archivoPdf) {
        if (archivoPdf == null || archivoPdf.isEmpty()) {
            System.out.println("❌ ERROR: Archivo vacío recibido en el servicio.");
            return null;
        }

        try {
            // Construimos la URL completa usando la propiedad inyectada
            String urlFinal = urlBase + "/upload_pdf.php";
            System.out.println("🚀 Intentando conectar a: " + urlFinal);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Crear recurso para el archivo
            ByteArrayResource recursoArchivo = new ByteArrayResource(archivoPdf.getBytes()) {
                @Override
                public String getFilename() { return archivoPdf.getOriginalFilename(); }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("pdf", recursoArchivo);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Llamada al microservicio PHP
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    urlFinal,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object idMongo = response.getBody().get("id_mongo");
                System.out.println("✅ ÉXITO: ID obtenido de Mongo: " + idMongo);
                return (idMongo != null) ? idMongo.toString() : null;
            } else {
                System.out.println("⚠️ PHP respondió con error: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("❌ EXCEPCIÓN EN SERVICIO: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public byte[] obtenerPdfDeMongo(String mongoId) {
        if (mongoId == null || mongoId.isEmpty()) return null;
        
        try {
            String url = urlBase + "/get_pdf.php?id=" + mongoId;
            // Usamos byte[].class para asegurar que recibimos el PDF binario
            return restTemplate.getForObject(url, byte[].class);
        } catch (Exception e) {
            System.err.println("❌ Error en servicio PDF: " + e.getMessage());
            return null;
        }
    }
}