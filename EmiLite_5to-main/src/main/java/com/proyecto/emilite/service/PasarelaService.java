
package com.proyecto.emilite.service;

import org.springframework.stereotype.Service;

import com.proyecto.emilite.dto.PagoDTO;

@Service
public class PasarelaService {

    public String generarLinkPago(PagoDTO dto) {

        String baseUrl = "https://sandbox.pagos.com/checkout?";
        String referencia = dto.getReferencia();
        double monto = dto.getMonto();
        String metodo = dto.getMetodoPago();

        String metodoParam = "";

        switch (metodo.toUpperCase()) {

            case "NEQUI":
                metodoParam = "payment_method=NEQUI";
                break;

            case "PSE":
                metodoParam = "payment_method=PSE";
                break;

            case "TARJETA":
                metodoParam = "payment_method=CARD";
                break;

            default:
                metodoParam = "payment_method=ALL";
        }

        return baseUrl +
                "ref=" + referencia +
                "&amount=" + monto +
                "&" + metodoParam;
    }
}
