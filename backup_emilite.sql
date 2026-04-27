-- MySQL dump 10.13  Distrib 8.0.45, for Linux (x86_64)
--
-- Host: localhost    Database: emilite_db
-- ------------------------------------------------------
-- Server version	8.0.45-0ubuntu0.24.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `mensaje`
--

DROP TABLE IF EXISTS `mensaje`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mensaje` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text NOT NULL,
  `fecha_registro` datetime(6) NOT NULL,
  `receiver_id` bigint NOT NULL,
  `sender_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKtiih2q40ylrr1xhuhqyfi7wu3` (`receiver_id`),
  KEY `FK4k0d2osrpbhxfjx4g5jkpojn1` (`sender_id`),
  CONSTRAINT `FK4k0d2osrpbhxfjx4g5jkpojn1` FOREIGN KEY (`sender_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKtiih2q40ylrr1xhuhqyfi7wu3` FOREIGN KEY (`receiver_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mensaje`
--

LOCK TABLES `mensaje` WRITE;
/*!40000 ALTER TABLE `mensaje` DISABLE KEYS */;
INSERT INTO `mensaje` VALUES (1,'hola','2026-04-24 09:38:50.728579',6,6),(2,'hola','2026-04-24 09:51:07.173628',7,6),(3,'Hola','2026-04-26 23:58:42.368853',7,6),(4,'Como vas','2026-04-27 00:28:02.777050',6,7),(5,'hello','2026-04-27 00:28:36.401931',6,6),(6,'HOLA','2026-04-27 00:29:32.983346',6,6),(7,'Buenass','2026-04-27 00:40:40.906549',6,6),(8,'hello','2026-04-27 00:40:55.928241',6,7),(9,'hola teacher','2026-04-27 00:41:39.218375',6,6),(10,'hola','2026-04-27 00:43:00.992073',6,7),(11,'gg','2026-04-27 00:43:42.279265',6,6),(12,'jj','2026-04-27 00:43:49.855297',6,7),(13,'hola','2026-04-27 00:48:34.341080',6,6),(14,'hola','2026-04-27 00:49:33.871649',6,7),(15,'bro','2026-04-27 00:49:42.269946',6,7),(16,'hola','2026-04-27 01:11:50.355901',6,6),(17,'hola','2026-04-27 01:11:56.083581',6,7),(18,'Juanito','2026-04-27 01:12:33.401405',6,7),(19,'hola','2026-04-27 01:22:03.010127',7,6),(20,'hola','2026-04-27 01:22:30.551275',6,7),(21,'hola profe','2026-04-27 01:27:25.535461',7,6),(22,'como fue mi papachooo','2026-04-27 01:27:39.298467',6,7),(23,'Juan ya te envie tu primera rutina','2026-04-27 01:46:17.189250',6,7),(24,'gracias Diego','2026-04-27 01:46:28.924946',7,6);
/*!40000 ALTER TABLE `mensaje` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notificacion`
--

DROP TABLE IF EXISTS `notificacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notificacion` (
  `leida` bit(1) DEFAULT NULL,
  `fecha_creacion` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `remitente_id` bigint DEFAULT NULL,
  `usuario_id` bigint NOT NULL,
  `mensaje` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKatp0snqldxdvhxa9dmmmeolgc` (`remitente_id`),
  KEY `FK5hnclv9lmmc1w4335x04warbm` (`usuario_id`),
  CONSTRAINT `FK5hnclv9lmmc1w4335x04warbm` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKatp0snqldxdvhxa9dmmmeolgc` FOREIGN KEY (`remitente_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notificacion`
--

LOCK TABLES `notificacion` WRITE;
/*!40000 ALTER TABLE `notificacion` DISABLE KEYS */;
/*!40000 ALTER TABLE `notificacion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pago`
--

DROP TABLE IF EXISTS `pago`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pago` (
  `monto` decimal(10,2) NOT NULL,
  `fecha_pago` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `promocion_id` bigint DEFAULT NULL,
  `servicio_id` bigint DEFAULT NULL,
  `usuario_id` bigint NOT NULL,
  `estado` varchar(20) NOT NULL,
  `metodo_pago` varchar(50) NOT NULL,
  `referencia_pago` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKg6sbxq7pnbp0yq4vrg6o18m0k` (`referencia_pago`),
  KEY `FKq3xjx3ltwqid4qj4qj69jm0ls` (`promocion_id`),
  KEY `FKn3rh9ywt97ns3csqcaghwyrm4` (`servicio_id`),
  KEY `FK6c9athkcn2dotm3xbta93tu3s` (`usuario_id`),
  CONSTRAINT `FK6c9athkcn2dotm3xbta93tu3s` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKn3rh9ywt97ns3csqcaghwyrm4` FOREIGN KEY (`servicio_id`) REFERENCES `servicio` (`id`),
  CONSTRAINT `FKq3xjx3ltwqid4qj4qj69jm0ls` FOREIGN KEY (`promocion_id`) REFERENCES `promocion` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pago`
--

LOCK TABLES `pago` WRITE;
/*!40000 ALTER TABLE `pago` DISABLE KEYS */;
/*!40000 ALTER TABLE `pago` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `perfil`
--

DROP TABLE IF EXISTS `perfil`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `perfil` (
  `altura` double DEFAULT NULL,
  `cadera` double DEFAULT NULL,
  `cintura` double DEFAULT NULL,
  `cuello` double DEFAULT NULL,
  `edad` int DEFAULT NULL,
  `peso` double DEFAULT NULL,
  `entrenador_id` bigint DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `nivel_actividad` varchar(255) DEFAULT NULL,
  `nombre_completo` varchar(255) NOT NULL,
  `objetivo` varchar(255) DEFAULT NULL,
  `observaciones` varchar(255) DEFAULT NULL,
  `sexo` varchar(255) DEFAULT NULL,
  `telefono` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKqimyhrxv3rmjmv7cs5fi1ek85` (`usuario_id`),
  KEY `FK5kdruduv07inwcduuwp0ou2u` (`entrenador_id`),
  CONSTRAINT `FK5kdruduv07inwcduuwp0ou2u` FOREIGN KEY (`entrenador_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKno01a8iut56nipcu6qdnxgeg5` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `perfil`
--

LOCK TABLES `perfil` WRITE;
/*!40000 ALTER TABLE `perfil` DISABLE KEYS */;
INSERT INTO `perfil` VALUES (174,30.1,33.3,46,19,56,NULL,1,6,NULL,'ligero','Juan Diego Guasca','Ganar masa muscular',NULL,'M',NULL),(NULL,NULL,NULL,NULL,NULL,NULL,NULL,2,7,NULL,NULL,'Diego Ospina',NULL,NULL,'M',NULL);
/*!40000 ALTER TABLE `perfil` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `progreso`
--

DROP TABLE IF EXISTS `progreso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `progreso` (
  `cadera` double DEFAULT NULL,
  `cintura` double DEFAULT NULL,
  `cuello` double DEFAULT NULL,
  `grasa` double DEFAULT NULL,
  `imc` double DEFAULT NULL,
  `peso` double DEFAULT NULL,
  `fecha_registro` datetime(6) DEFAULT NULL,
  `id_progreso` bigint NOT NULL AUTO_INCREMENT,
  `id_usuario` bigint NOT NULL,
  PRIMARY KEY (`id_progreso`),
  KEY `FKhjg9krbn13hhlorpn8i8cvity` (`id_usuario`),
  CONSTRAINT `FKhjg9krbn13hhlorpn8i8cvity` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `progreso`
--

LOCK TABLES `progreso` WRITE;
/*!40000 ALTER TABLE `progreso` DISABLE KEYS */;
INSERT INTO `progreso` VALUES (NULL,NULL,NULL,0,18.38,55,'2026-04-24 07:40:40.187564',1,6),(NULL,NULL,NULL,0,18.5,56,'2026-04-27 01:46:55.091762',2,6);
/*!40000 ALTER TABLE `progreso` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `promocion`
--

DROP TABLE IF EXISTS `promocion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `promocion` (
  `activa` bit(1) NOT NULL,
  `descuento_porcentaje` decimal(5,2) DEFAULT NULL,
  `fecha_fin` date NOT NULL,
  `fecha_inicio` date NOT NULL,
  `max_usos` int NOT NULL,
  `usos_actuales` int NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `codigo` varchar(50) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKrv60lkvwd9gjqcsdm5v3kjajv` (`codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `promocion`
--

LOCK TABLES `promocion` WRITE;
/*!40000 ALTER TABLE `promocion` DISABLE KEYS */;
/*!40000 ALTER TABLE `promocion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rol`
--

DROP TABLE IF EXISTS `rol`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rol` (
  `activo` bit(1) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK43kr6s7bts1wqfv43f7jd87kp` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rol`
--

LOCK TABLES `rol` WRITE;
/*!40000 ALTER TABLE `rol` DISABLE KEYS */;
INSERT INTO `rol` VALUES (_binary '',1,'ROLE_ADMIN','Administrador del Sistema'),(_binary '',2,'ROLE_ENTRENADOR','Entrenador certificado'),(_binary '',3,'ROLE_CLIENTE','Cliente del servicio');
/*!40000 ALTER TABLE `rol` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rutina`
--

DROP TABLE IF EXISTS `rutina`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rutina` (
  `activo` bit(1) NOT NULL,
  `duracion_semanas` int DEFAULT NULL,
  `favorita` bit(1) NOT NULL,
  `cliente_id` bigint NOT NULL,
  `fecha_creacion` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nivel_dificultad` varchar(50) DEFAULT NULL,
  `tipo` varchar(50) DEFAULT NULL,
  `nombre` varchar(100) NOT NULL,
  `descripcion` text,
  PRIMARY KEY (`id`),
  KEY `FK3fg60vim7duiufe6o03uimdug` (`cliente_id`),
  CONSTRAINT `FK3fg60vim7duiufe6o03uimdug` FOREIGN KEY (`cliente_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rutina`
--

LOCK TABLES `rutina` WRITE;
/*!40000 ALTER TABLE `rutina` DISABLE KEYS */;
INSERT INTO `rutina` VALUES (_binary '',2,_binary '',6,'2026-04-27 01:45:19.494613',1,'Principiante','Fuerza','FITNESS PRESS','Burpiess 3X4\r\nPlancha 60s');
/*!40000 ALTER TABLE `rutina` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `servicio`
--

DROP TABLE IF EXISTS `servicio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `servicio` (
  `activo` bit(1) NOT NULL,
  `duracion_minutos` int NOT NULL,
  `precio` decimal(10,2) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `descripcion` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `servicio`
--

LOCK TABLES `servicio` WRITE;
/*!40000 ALTER TABLE `servicio` DISABLE KEYS */;
/*!40000 ALTER TABLE `servicio` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `activo` bit(1) NOT NULL,
  `cv_score` double DEFAULT NULL,
  `enabled` bit(1) NOT NULL,
  `es_premuim` bit(1) DEFAULT NULL,
  `fecha_nacimiento` date DEFAULT NULL,
  `validado` bit(1) NOT NULL,
  `entrenador_id` bigint DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `rol_id` bigint NOT NULL,
  `username` varchar(50) NOT NULL,
  `descripcion` varchar(500) DEFAULT NULL,
  `apellidos` varchar(255) NOT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `nombres` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `telefono` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK863n1y3x0jalatoir4325ehal` (`username`),
  UNIQUE KEY `UK5171l57faosmj8myawaucatdw` (`email`),
  KEY `FKo5rwky2n5scow8ryy7y8v5jeh` (`entrenador_id`),
  KEY `FKshkwj12wg6vkm6iuwhvcfpct8` (`rol_id`),
  CONSTRAINT `FKo5rwky2n5scow8ryy7y8v5jeh` FOREIGN KEY (`entrenador_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKshkwj12wg6vkm6iuwhvcfpct8` FOREIGN KEY (`rol_id`) REFERENCES `rol` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES (_binary '',NULL,_binary '',_binary '\0',NULL,_binary '',NULL,1,1,'admin',NULL,'Sistema',NULL,'admin@fitexpert.com','Admin','$2a$10$2RYXGFHZVkVsmweAsXK.HeWSmu3rlTupkIoQMFADgHO/GKrdi7T/2',NULL),(_binary '',NULL,_binary '',_binary '\0','2006-11-26',_binary '',7,6,3,'JuanDiegoVTG',NULL,'Guasca',NULL,'juandiegoguasca0@gmail.com','Juan Diego','$2a$10$7vWdLBR.1PGyr8inQwlxIOt8h4SbetwfPl2Mi9aCks3nZLpHkdWp.','3236475684'),(_binary '',NULL,_binary '',_binary '\0','2000-04-20',_binary '',NULL,7,2,'DiegoFit',NULL,'Ospina',NULL,'ospinadiego712@gmail.com','Diego','$2a$10$Tgb3V3fRhmtBelD2vUg1iOar7lC/3coI0RIng01GDT/AIV9yMQhJ.',NULL);
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-26 20:54:11
