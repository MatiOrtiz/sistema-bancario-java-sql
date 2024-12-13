#Archivo (banco.sql) para la creación de la base de datos banco

CREATE DATABASE banco;

USE banco;


-- banco.ciudad definition
CREATE TABLE ciudad (
  `cod_postal` smallint unsigned NOT NULL,
  `nombre` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`cod_postal`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- banco.sucursal definition
CREATE TABLE sucursal (
  `nro_suc` smallint unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `direccion` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `telefono` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `horario` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `cod_postal` smallint unsigned NOT NULL,
  PRIMARY KEY (`nro_suc`),
  KEY `Sucursal_Ciudad_FK` (`cod_postal`),
  CONSTRAINT `Sucursal_Ciudad_FK` FOREIGN KEY (`cod_postal`) REFERENCES `ciudad` (`cod_postal`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- banco.empleado definition
CREATE TABLE empleado (
  `legajo` smallint unsigned NOT NULL AUTO_INCREMENT,
  `apellido` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `nombre` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tipo_doc` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `nro_doc` int unsigned NOT NULL,
  `direccion` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `telefono` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `cargo` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `nro_suc` smallint unsigned NOT NULL,
  PRIMARY KEY (`legajo`),
  KEY `Empleado_Sucursal_FK` (`nro_suc`),
  CONSTRAINT `empleado_sucursal_FK` FOREIGN KEY (`nro_suc`) REFERENCES `sucursal` (`nro_suc`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- banco.cliente definition
CREATE TABLE cliente (
  `nro_cliente` smallint unsigned NOT NULL AUTO_INCREMENT,
  `apellido` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `nombre` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tipo_doc` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `nro_doc` int unsigned NOT NULL,
  `direccion` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `telefono` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `fecha_nac` date NOT NULL,
  PRIMARY KEY (`nro_cliente`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- banco.plazo_fijo definition
CREATE TABLE plazo_fijo (
  `nro_plazo` int unsigned NOT NULL AUTO_INCREMENT,
  `capital` decimal(16,2) unsigned NOT NULL,
  `fecha_inicio` date NOT NULL,
  `fecha_fin` date NOT NULL,
  `tasa_interes` decimal(4,2) unsigned NOT NULL,
  `interes` decimal(16,2) unsigned NOT NULL,
  `nro_suc` smallint unsigned NOT NULL,
  PRIMARY KEY (`nro_plazo`),
  KEY `plazo_fijo_sucursal_FK` (`nro_suc`),
  CONSTRAINT `plazo_fijo_sucursal_FK` FOREIGN KEY (`nro_suc`) REFERENCES `sucursal` (`nro_suc`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- banco.tasa_plazo_fijo definition
CREATE TABLE tasa_plazo_fijo (
  `periodo` smallint unsigned NOT NULL,
  `monto_inf` decimal(16,2) unsigned NOT NULL,
  `monto_sup` decimal(16,2) unsigned NOT NULL,
  `tasa` decimal(4,2) unsigned NOT NULL,
  PRIMARY KEY (`periodo`,`monto_inf`,`monto_sup`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- banco.plazo_cliente definition
CREATE TABLE plazo_cliente (
  `nro_plazo` int unsigned NOT NULL,
  `nro_cliente` smallint unsigned NOT NULL,
  PRIMARY KEY (`nro_plazo`,`nro_cliente`),
  KEY `plazo_cliente_Cliente_FK` (`nro_cliente`),
  CONSTRAINT `plazo_cliente_cliente_FK` FOREIGN KEY (`nro_cliente`) REFERENCES `cliente` (`nro_cliente`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `plazo_cliente_plazo_fijo_FK` FOREIGN KEY (`nro_plazo`) REFERENCES `plazo_fijo` (`nro_plazo`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- banco.prestamo definition
CREATE TABLE prestamo (
  `nro_prestamo` int unsigned NOT NULL AUTO_INCREMENT,
  `fecha` date NOT NULL,
  `cant_meses` smallint unsigned NOT NULL,
  `monto` decimal(10,2) unsigned NOT NULL,
  `tasa_interes` decimal(4,2) unsigned NOT NULL,
  `interes` decimal(9,2) unsigned NOT NULL,
  `valor_cuota` decimal(9,2) unsigned NOT NULL,
  `legajo` smallint unsigned NOT NULL,
  `nro_cliente` smallint unsigned NOT NULL,
  PRIMARY KEY (`nro_prestamo`),
  KEY `Prestamo_Empleado_FK` (`legajo`),
  KEY `Prestamo_Cliente_FK` (`nro_cliente`),
  CONSTRAINT `prestamo_cliente_FK` FOREIGN KEY (`nro_cliente`) REFERENCES `cliente` (`nro_cliente`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `prestamo_empleado_FK` FOREIGN KEY (`legajo`) REFERENCES `empleado` (`legajo`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- banco.0. definition
CREATE TABLE pago (
  `nro_prestamo` int unsigned NOT NULL,
  `nro_pago` smallint unsigned NOT NULL,
  `fecha_venc` date NOT NULL,
  `fecha_pago` date DEFAULT NULL,
  PRIMARY KEY (`nro_prestamo`,`nro_pago`),
  CONSTRAINT `pago_prestamo_FK` FOREIGN KEY (`nro_prestamo`) REFERENCES `prestamo` (`nro_prestamo`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- banco.tasa_prestamo definition
CREATE TABLE tasa_prestamo (
  `periodo` smallint unsigned NOT NULL,
  `monto_inf` decimal(10,2) unsigned NOT NULL,
  `monto_sup` decimal(10,2) unsigned NOT NULL,
  `tasa` decimal(4,2) unsigned NOT NULL,
  PRIMARY KEY (`periodo`,`monto_inf`,`monto_sup`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- banco.caja_ahorro definition
CREATE TABLE caja_ahorro (
  `nro_ca` int unsigned NOT NULL AUTO_INCREMENT,
  `CBU` bigint unsigned NOT NULL,
  `saldo` decimal(16,2) unsigned NOT NULL,
  PRIMARY KEY (`nro_ca`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- banco.cliente_ca definition
CREATE TABLE cliente_ca (
  `nro_cliente` smallint unsigned NOT NULL,
  `nro_ca` int unsigned NOT NULL,
  PRIMARY KEY (`nro_cliente`,`nro_ca`),
  KEY `cliente_CA_Cliente_FK` (`nro_cliente`),
  KEY `cliente_CA_Caja_Ahorro_FK` (`nro_ca`),
  CONSTRAINT `cliente_ca_caja_ahorro_FK` FOREIGN KEY (`nro_ca`) REFERENCES `caja_ahorro` (`nro_ca`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `cliente_ca_cliente_FK` FOREIGN KEY (`nro_cliente`) REFERENCES `cliente` (`nro_cliente`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- banco.tarjeta definition
CREATE TABLE tarjeta (
  `nro_tarjeta` bigint unsigned NOT NULL AUTO_INCREMENT,
  `PIN` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `CVT` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `fecha_venc` date NOT NULL,
  `nro_cliente` smallint unsigned NOT NULL,
  `nro_ca` int unsigned NOT NULL,
  PRIMARY KEY (`nro_tarjeta`),
  KEY `Tarjeta_cliente_CA_FK` (`nro_cliente`,`nro_ca`),
  CONSTRAINT `Tarjeta_cliente_CA_FK` FOREIGN KEY (`nro_cliente`, `nro_ca`) REFERENCES `cliente_ca` (`nro_cliente`, `nro_ca`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- banco.caja definition
CREATE TABLE caja (
  `cod_caja` smallint unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`cod_caja`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- banco.ventanilla definition
CREATE TABLE ventanilla (
  `cod_caja` smallint unsigned NOT NULL,
  `nro_suc` smallint unsigned NOT NULL,
  PRIMARY KEY (`cod_caja`),
  KEY `Ventanilla_Sucursal_FK` (`nro_suc`),
  CONSTRAINT `ventanilla_caja_FK` FOREIGN KEY (`cod_caja`) REFERENCES `caja` (`cod_caja`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `ventanilla_sucursal_FK` FOREIGN KEY (`nro_suc`) REFERENCES `sucursal` (`nro_suc`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- banco.atm definition
CREATE TABLE atm (
  `cod_caja` smallint unsigned NOT NULL,
  `cod_postal` smallint unsigned NOT NULL,
  `direccion` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`cod_caja`),
  KEY `ATM_Ciudad_FK` (`cod_postal`),
  CONSTRAINT `atm_caja_FK` FOREIGN KEY (`cod_caja`) REFERENCES `caja` (`cod_caja`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `ATM_Ciudad_FK` FOREIGN KEY (`cod_postal`) REFERENCES `ciudad` (`cod_postal`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- banco.transaccion definition
CREATE TABLE transaccion (
  `nro_trans` int unsigned NOT NULL AUTO_INCREMENT,
  `fecha` date NOT NULL,
  `hora` time NOT NULL,
  `monto` decimal(16,2) unsigned NOT NULL,
  PRIMARY KEY (`nro_trans`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- banco.debito definition
CREATE TABLE debito (
  `nro_trans` int unsigned NOT NULL,
  `descripcion` tinytext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `nro_cliente` smallint unsigned NOT NULL,
  `nro_ca` int unsigned NOT NULL,
  PRIMARY KEY (`nro_trans`),
  KEY `Debito_cliente_CA_FK` (`nro_cliente`,`nro_ca`),
  CONSTRAINT `Debito_cliente_CA_FK` FOREIGN KEY (`nro_cliente`, `nro_ca`) REFERENCES `cliente_ca` (`nro_cliente`, `nro_ca`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `debito_transaccion_FK` FOREIGN KEY (`nro_trans`) REFERENCES `transaccion` (`nro_trans`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- banco.transaccion_por_caja definition
CREATE TABLE transaccion_por_caja (
  `nro_trans` int unsigned NOT NULL,
  `cod_caja` smallint unsigned NOT NULL,
  PRIMARY KEY (`nro_trans`),
  KEY `Transaccion_por_Caja_Caja_FK` (`cod_caja`),
  CONSTRAINT `transaccion_por_caja_caja_FK` FOREIGN KEY (`cod_caja`) REFERENCES `caja` (`cod_caja`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `transaccion_por_caja_transaccion_FK` FOREIGN KEY (`nro_trans`) REFERENCES `transaccion` (`nro_trans`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- banco.deposito definition
CREATE TABLE deposito (
  `nro_trans` int unsigned NOT NULL,
  `nro_ca` int unsigned NOT NULL,
  PRIMARY KEY (`nro_trans`),
  KEY `Deposito_Caja_Ahorro_FK` (`nro_ca`),
  CONSTRAINT `deposito_caja_ahorro_FK` FOREIGN KEY (`nro_ca`) REFERENCES `caja_ahorro` (`nro_ca`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `Deposito_Transaccion_por_Caja_FK` FOREIGN KEY (`nro_trans`) REFERENCES `transaccion_por_caja` (`nro_trans`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- banco.extraccion definition
CREATE TABLE extraccion (
  `nro_trans` int unsigned NOT NULL,
  `nro_cliente` smallint unsigned NOT NULL,
  `nro_ca` int unsigned NOT NULL,
  PRIMARY KEY (`nro_trans`),
  KEY `Extraccion_cliente_CA_FK_1` (`nro_cliente`,`nro_ca`),
  CONSTRAINT `Extraccion_cliente_CA_FK` FOREIGN KEY (`nro_cliente`, `nro_ca`) REFERENCES `cliente_ca` (`nro_cliente`, `nro_ca`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `Extraccion_cliente_CA_FK_1` FOREIGN KEY (`nro_cliente`, `nro_ca`) REFERENCES `cliente_ca` (`nro_cliente`, `nro_ca`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `Extraccion_Transaccion_por_Caja_FK` FOREIGN KEY (`nro_trans`) REFERENCES `transaccion_por_caja` (`nro_trans`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- banco.transferencia definition
CREATE TABLE transferencia (
  `nro_trans` int unsigned NOT NULL,
  `nro_cliente` smallint unsigned NOT NULL,
  `origen` int unsigned NOT NULL,
  `destino` int unsigned NOT NULL,
  PRIMARY KEY (`nro_trans`),
  KEY `Transferencia_cliente_CA_FK_1` (`nro_cliente`,`origen`),
  KEY `Transferencia_Caja_Ahorro_FK` (`destino`),
  CONSTRAINT `transferencia_caja_ahorro_FK` FOREIGN KEY (`destino`) REFERENCES `caja_ahorro` (`nro_ca`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `Transferencia_cliente_CA_FK` FOREIGN KEY (`nro_cliente`, `origen`) REFERENCES `cliente_ca` (`nro_cliente`, `nro_ca`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `Transferencia_Transaccion_por_Caja_FK` FOREIGN KEY (`nro_trans`) REFERENCES `transaccion_por_caja` (`nro_trans`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

#--------------------------------------------
#Creación de stored procedures

DELIMITER $$

CREATE PROCEDURE extraer_dinero(
    IN id_tarjeta INT,
    IN monto DECIMAL(16,2), 
    IN cod_caja INT,
    OUT resultado VARCHAR(255)
)
BEGIN
    DECLARE id_cliente INT;
    DECLARE ca_origen INT;
    DECLARE id_transaccion INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        SET resultado = 'SQLEXCEPTION, transaccion_cancelada!';
        ROLLBACK;
    END;

    START TRANSACTION;
    SELECT nro_ca INTO ca_origen FROM tarjeta WHERE nro_tarjeta = id_tarjeta;
    IF EXISTS (SELECT saldo FROM caja_ahorro WHERE nro_ca = ca_origen AND saldo >= monto FOR UPDATE) 
      THEN
        SELECT nro_cliente INTO id_cliente FROM tarjeta WHERE nro_tarjeta = id_tarjeta;
        UPDATE caja_ahorro SET saldo = saldo - monto WHERE nro_ca = ca_origen;
		
        INSERT INTO transaccion(fecha, hora, monto) VALUES(CURRENT_DATE, CURRENT_TIME, monto);
                
        INSERT INTO transaccion_por_caja(nro_trans, cod_caja) VALUES(LAST_INSERT_ID(), cod_caja);
        INSERT INTO extraccion(nro_trans, nro_cliente, nro_ca) VALUES(LAST_INSERT_ID(), id_cliente, ca_origen);
        
        SELECT saldo INTO resultado FROM caja_ahorro WHERE nro_ca = ca_origen;
      ELSE 
      SET resultado = "Saldo insuficiente";
    END IF;

    COMMIT;
END $$

DELIMITER $$

CREATE PROCEDURE transferir_dinero(
    IN id_tarjeta INT,
    IN ca_destino INT, 
    IN monto DECIMAL(16,2), 
    IN cod_caja INT,
    OUT resultado VARCHAR(255)
)
BEGIN
  DECLARE id_cliente INT;
  DECLARE ca_origen INT;
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
      SET resultado = 'SQLEXCEPTION, transaccion_cancelada!';
      ROLLBACK;
    END;
    
  START TRANSACTION;
	SELECT nro_ca INTO ca_origen FROM tarjeta WHERE nro_tarjeta = id_tarjeta;
    IF EXISTS (SELECT * FROM caja_ahorro WHERE nro_ca = ca_destino) 
	THEN
	  IF EXISTS (SELECT saldo FROM caja_ahorro WHERE nro_ca = ca_origen AND saldo > monto FOR UPDATE )
      THEN 
      UPDATE caja_ahorro SET saldo = saldo - monto WHERE nro_ca = ca_origen;
      UPDATE caja_ahorro SET saldo = saldo + monto WHERE nro_ca = ca_destino;
      
      SELECT nro_cliente INTO id_cliente FROM tarjeta WHERE nro_tarjeta = id_tarjeta;

      INSERT INTO transaccion(fecha, hora, monto) VALUES(CURRENT_DATE, CURRENT_TIME, monto);
      INSERT INTO transaccion_por_caja(nro_trans, cod_caja) VALUES(LAST_INSERT_ID(), cod_caja);
      INSERT INTO transferencia(nro_trans,nro_cliente, origen, destino) VALUES(LAST_INSERT_ID(), id_cliente, ca_origen, ca_destino);

      INSERT INTO transaccion(fecha, hora, monto) VALUES(CURRENT_DATE, CURRENT_TIME, monto);
      INSERT INTO transaccion_por_caja(nro_trans, cod_caja) VALUES(LAST_INSERT_ID(), cod_caja);
      INSERT INTO deposito(nro_trans, nro_ca) VALUES(LAST_INSERT_ID(), ca_destino); 

      SELECT saldo INTO resultado FROM caja_ahorro WHERE nro_ca = ca_origen;
      ELSE
		SET resultado = "Saldo insuficiente";
	  END IF;
    ELSE
      SET resultado = "No se encontro caja de ahorro destino";
    END IF;
    
    COMMIT;
END $$

DELIMITER $$

CREATE TRIGGER trigger_pagos
AFTER INSERT ON prestamo
FOR EACH ROW
BEGIN
  DECLARE num INT DEFAULT 1;

  WHILE num <= NEW.cant_meses DO
    INSERT INTO pago(nro_prestamo, nro_pago, fecha_venc)
    VALUES(NEW.nro_prestamo, num, DATE_ADD(NEW.fecha, INTERVAL num MONTH));
    SET num = num + 1;
  END WHILE;    
    
END $$

DELIMITER ;



#--------------------------------------------
#Creación de vistas
#HACER UNA VISTA POR CADA TIPO TRANSACCION?


CREATE VIEW tipo_transaccion AS
SELECT transaccion.nro_trans, fecha, hora, 'Debito' AS tipo, monto
FROM Debito JOIN Transaccion ON transaccion.nro_trans=Debito.nro_trans
UNION ALL
SELECT transaccion.nro_trans, fecha, hora, 'Deposito' AS tipo, monto
FROM Deposito JOIN Transaccion ON transaccion.nro_trans=Deposito.nro_trans
UNION ALL
SELECT transaccion.nro_trans, fecha, hora, 'Extraccion' AS tipo, monto
FROM Extraccion JOIN Transaccion ON transaccion.nro_trans=Extraccion.nro_trans
UNION ALL
SELECT transaccion.nro_trans, fecha, hora, 'Transferencia' AS tipo, monto
FROM Transferencia JOIN Transaccion ON transaccion.nro_trans=Transferencia.nro_trans;

  



CREATE VIEW cliente_transacciones AS
SELECT debito.nro_trans, cliente.*, caja_ahorro.*, NULL AS cod_caja, NULL AS destino
FROM Debito JOIN Cliente ON Debito.nro_cliente=cliente.nro_cliente
JOIN Caja_Ahorro ON debito.nro_ca=caja_ahorro.nro_ca
UNION ALL
SELECT Deposito.nro_trans, NULL AS nro_cliente, NULL AS tipo_doc, NULL AS nro_doc, NULL AS nombre, NULL AS apellido, NULL AS direccion, NULL AS telefono, NULL AS fecha_nac, caja_ahorro.*,  Transaccion_por_Caja.cod_caja, NULL AS destino
FROM Deposito JOIN Caja_Ahorro ON Deposito.nro_ca=caja_ahorro.nro_ca
JOIN Transaccion_por_Caja ON Transaccion_por_Caja.nro_trans=Deposito.nro_trans
UNION ALL
SELECT Extraccion.nro_trans, cliente.*, caja_ahorro.*, Transaccion_por_Caja.cod_caja, NULL AS destino
FROM Extraccion JOIN Cliente ON Extraccion.nro_cliente=cliente.nro_cliente
JOIN Caja_Ahorro ON Extraccion.nro_ca=caja_ahorro.nro_ca
JOIN Transaccion_por_Caja ON Transaccion_por_Caja.nro_trans=Extraccion.nro_trans
UNION ALL
SELECT Transferencia.nro_trans, cliente.*, caja_ahorro.*, Transaccion_por_Caja.cod_caja, Transferencia.destino
FROM Transferencia JOIN Cliente ON Transferencia.nro_cliente=cliente.nro_cliente
JOIN Caja_Ahorro ON Transferencia.origen=caja_ahorro.nro_ca
JOIN Transaccion_por_Caja ON Transaccion_por_Caja.nro_trans=Transferencia.nro_trans;





CREATE VIEW  trans_cajas_ahorro AS
SELECT cliente_transacciones.nro_ca, cliente_transacciones.saldo,
  tipo_transaccion.*, cliente_transacciones.cod_caja,
  cliente_transacciones.nro_cliente, cliente_transacciones.tipo_doc, cliente_transacciones.nro_doc, cliente_transacciones.nombre, cliente_transacciones.apellido,
  cliente_transacciones.destino
FROM tipo_transaccion JOIN cliente_transacciones ON tipo_transaccion.nro_trans=cliente_transacciones.nro_trans;


#---------------------------------------------
#Creación de usuarios
#Usar funciones para codificar contraseña

DROP USER IF EXISTS ``@localhost;



/*Usuario admin  se utilizara para administrar la base de datos por lo tanto debera
tener acceso total sobre todas las tablas, con la opcion de crear usuarios y otorgar privilegios
sobre las mismas. 
*/
DROP USER IF EXISTS 'admin'@'localhost';

CREATE USER 'admin'@'localhost' IDENTIFIED BY 'admin';

GRANT ALL PRIVILEGES ON banco.* TO 'admin'@'localhost' WITH GRANT OPTION;


/*: Este usuario estara destinado a permitir el acceso de la aplicacion de administracion
que utilizan los empleados del banco para administrar los clientes, prestamos, cajas de ahorro y
plazos fijos. 
*/
DROP USER IF EXISTS 'empleado'@'%';

CREATE USER 'empleado'@'%' IDENTIFIED BY 'empleado';



GRANT SELECT ON empleado TO 'empleado'@'%';
GRANT SELECT ON sucursal TO 'empleado'@'%';
GRANT SELECT ON tasa_plazo_fijo TO 'empleado'@'%';
GRANT SELECT ON tasa_prestamo TO 'empleado'@'%';


GRANT SELECT, INSERT ON prestamo To 'empleado'@'%';
GRANT SELECT, INSERT ON tasa_plazo_fijo To 'empleado'@'%';
GRANT SELECT, INSERT ON plazo_cliente To 'empleado'@'%';
GRANT SELECT, INSERT ON caja_ahorro To 'empleado'@'%';
GRANT SELECT, INSERT ON Tarjeta To 'empleado'@'%';

GRANT SELECT, INSERT, UPDATE ON cliente_ca TO 'empleado'@'%';
GRANT SELECT, INSERT, UPDATE ON cliente TO 'empleado'@'%';
GRANT SELECT, INSERT, UPDATE ON pago TO 'empleado'@'%';


/*Este usuario esta destinado a permitir el acccmdeso de los ATM, para que los clientes puedan
consultar el estado de sus cajas de ahorro y realizar transacciones.
*/
DROP USER IF EXISTS 'atm'@'%';
CREATE USER 'atm'@'%' IDENTIFIED BY 'atm';


GRANT SELECT ON trans_cajas_ahorro TO 'atm'@'%';
GRANT SELECT, UPDATE ON banco.tarjeta TO 'atm'@'%';

DROP USER IF EXISTS 'atm'@'localhost';
CREATE USER 'atm'@'localhost' IDENTIFIED BY 'atm';


GRANT SELECT ON trans_cajas_ahorro TO 'atm'@'localhost';
GRANT SELECT, UPDATE ON banco.tarjeta TO 'atm'@'localhost';

GRANT EXECUTE ON PROCEDURE extraer_dinero TO 'atm'@'%';
GRANT EXECUTE ON PROCEDURE extraer_dinero TO 'atm'@'localhost';

GRANT EXECUTE ON PROCEDURE transferir_dinero TO 'atm'@'%';
GRANT EXECUTE ON PROCEDURE transferir_dinero TO 'atm'@'localhost';


flush privileges;
