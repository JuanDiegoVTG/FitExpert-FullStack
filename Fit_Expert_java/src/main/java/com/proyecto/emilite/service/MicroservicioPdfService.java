package com.proyecto.emilite.service;

import java.util.Map;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*; // Importa HttpHeaders, HttpEntity, ResponseEntity, MediaType, HttpMethod
import org.springframework.stereotype.Service;
import org.springframework.util.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.ParameterizedTypeReference; // Necesario para el Map

@SuppressWarnings("null")
@Service
public class MicroservicioPdfService {

    private final String URL_BASE = "https://fitexpert-fullstack-m8vv.onrender.com/fitexpert-api";
    private final RestTemplate restTemplate = new RestTemplate();

    public String subirPdfAMongo(MultipartFile archivoPdf) {
        if (archivoPdf == null || archivoPdf.isEmpty()) {
            return null;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            // Ya no forzamos setContentType aquí para dejar que Spring genere el boundary
            
            ByteArrayResource recursoArchivo = new ByteArrayResource(archivoPdf.getBytes()) {
                @Override
                public String getFilename() {
                    return archivoPdf.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("pdf", recursoArchivo);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // CORRECCIÓN: Usamos 'exchange' en lugar de 'postForEntity' para soportar el Map genérico
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    URL_BASE + "/upload_pdf.php",
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> jsonResponse = response.getBody();

            if (response.getStatusCode().is2xxSuccessful() && jsonResponse != null) {
                Object idObj = jsonResponse.get("id_mongo");
                return (idObj != null) ? idObj.toString() : null;
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR: " + e.getMessage());
        }
        return null;
    }

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