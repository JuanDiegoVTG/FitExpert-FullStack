package com.proyecto.emilite.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proyecto.emilite.model.Contrato;
import com.proyecto.emilite.model.Usuario;
import com.proyecto.emilite.repository.ContratoRepository;


@Service
public class ContratoService {

    @Autowired
    private ContratoRepository contratoRepository;

    // Método que llamará cuando el pago sea aprobado
    public void crearVínculo(Usuario cliente, Usuario entrenador) {
        Contrato nuevoContrato = new Contrato();
        nuevoContrato.setCliente(cliente);
        nuevoContrato.setEntrenador(entrenador);
        nuevoContrato.setFechaInicio(LocalDate.now());
        nuevoContrato.setActivo(true);
        contratoRepository.save(nuevoContrato);
    }

    // Método para desbloquear la IA
    public boolean tieneEntrenadorActivo(Long clienteId) {
        return contratoRepository.existsByClienteIdAndActivoTrue(clienteId);
    }
}