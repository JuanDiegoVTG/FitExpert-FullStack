package com.proyecto.emilite.repository;

import com.proyecto.emilite.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;


public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    @Query("SELECT m FROM Mensaje m WHERE (m.sender.id = :id1 AND m.receiver.id = :id2) OR (m.sender.id = :id2 AND m.receiver.id = :id1) ORDER BY m.fechaRegistro ASC")
    List<Mensaje> findChatMessages(@Param("id1") Long id1, @Param("id2") Long id2);
}