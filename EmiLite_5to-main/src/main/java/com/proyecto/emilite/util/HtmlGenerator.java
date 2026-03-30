package com.proyecto.emilite.util;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class HtmlGenerator {

    private static TemplateEngine templateEngine;

    public HtmlGenerator(TemplateEngine templateEngine) {
        HtmlGenerator.templateEngine = templateEngine;
    }

    public static String generateHtml(String templateName, org.springframework.ui.Model model) {
        Context context = new Context();
        for (Map.Entry<String, Object> entry : model.asMap().entrySet()) {
            context.setVariable(entry.getKey(), entry.getValue());
        }
        return templateEngine.process(templateName, context);
    }
}
