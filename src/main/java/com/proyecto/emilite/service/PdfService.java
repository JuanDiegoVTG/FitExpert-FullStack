package com.proyecto.emilite.service;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;

@Service
public class PdfService {

    private final TemplateEngine templateEngine;

    public PdfService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] generarPdf(String templateName, Context context) throws Exception {

        // Renderiza HTML desde plantilla Thymeleaf
        String html = templateEngine.process(templateName, context);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();

        // Necesario para leer imágenes del contexto Spring
        renderer.getSharedContext().setReplacedElementFactory(
                (ReplacedElementFactory) new org.xhtmlrenderer.pdf.ITextUserAgent(renderer.getOutputDevice())
        );

        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(baos);

        return baos.toByteArray();
    }
}
