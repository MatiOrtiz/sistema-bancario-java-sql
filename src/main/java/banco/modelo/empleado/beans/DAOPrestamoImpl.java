package banco.modelo.empleado.beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.cj.protocol.Resultset;

import banco.utils.Fechas;

public class DAOPrestamoImpl implements DAOPrestamo {

	private static Logger logger = LoggerFactory.getLogger(DAOPrestamoImpl.class);
	
	private Connection conexion;
	
	public DAOPrestamoImpl(Connection c) {
		this.conexion = c;
	}
	
	
	@Override
	public void crearPrestamo(PrestamoBean prestamo) throws Exception {
		int cant_meses = prestamo.getCantidadMeses();
		double monto = prestamo.getMonto();
		double tasa = prestamo.getTasaInteres();
		double interes;
		double cuota;
		int legajo = prestamo.getLegajo();
		int cliente = prestamo.getNroCliente();

		java.util.Date fecha = new Date();
		java.sql.Date fechaStr = Fechas.convertirDateADateSQL(fecha);

		interes = (monto*tasa*cant_meses)/1200;
		cuota = (monto+interes)/cant_meses;

		logger.info("Creación o actualizacion del prestamo.");
		logger.debug("meses : {}", cant_meses);
		logger.debug("monto : {}", monto);
		logger.debug("tasa : {}", tasa);
		logger.debug("interes : {}", interes);
		logger.debug("cuota : {}", cuota);
		logger.debug("legajo : {}", legajo);
		logger.debug("cliente : {}", cliente	);
		logger.debug("fecha : {}", fechaStr	);
		
		 
		String sql = "INSERT INTO prestamo(fecha, cant_meses, monto, tasa_interes, interes, valor_cuota, legajo, nro_cliente) " +
             "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

		
		try{
			PreparedStatement pstmt = conexion.prepareStatement(sql);
			pstmt.setDate(1, Fechas.convertirDateADateSQL(fecha)); // Usa setDate para manejar la fecha SQL
			pstmt.setInt(2, cant_meses);
			pstmt.setDouble(3, monto);
			pstmt.setDouble(4, tasa);
			pstmt.setDouble(5, interes);
			pstmt.setDouble(6, cuota);
			pstmt.setInt(7, legajo);
			pstmt.setInt(8, cliente);
			pstmt.executeUpdate();
			pstmt.close();
		}catch(SQLException ex){
				logger.error("SQLException: {}", ex.getMessage());
				logger.error("SQLState: {}", ex.getSQLState());
				logger.error("VendorError: {}", ex.getErrorCode());
				throw new Exception("Error al crear nuevo prestamo");
		}
	}

	@Override
	public PrestamoBean recuperarPrestamo(int nroPrestamo) throws Exception {
		logger.info("Recupera el prestamo nro {}.", nroPrestamo);
		PrestamoBean prestamo = null;
	
		String sql = "SELECT * from prestamo WHERE nro_prestamo="+nroPrestamo+" ;";
		try{
			Statement stmt = conexion.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if(rs.next()){
				prestamo = new PrestamoBeanImpl();
				prestamo.setNroPrestamo(nroPrestamo);
				prestamo.setFecha(Fechas.convertirStringADate(rs.getString("fecha")));
				prestamo.setCantidadMeses(rs.getInt("cant_meses"));
				prestamo.setMonto(rs.getDouble("monto"));
				prestamo.setTasaInteres(rs.getDouble("tasa_interes"));
				prestamo.setInteres(rs.getDouble("interes"));
				prestamo.setValorCuota(rs.getDouble("valor_cuota"));
				prestamo.setLegajo(rs.getInt("legajo"));
				prestamo.setNroCliente(rs.getInt("nro_cliente"));
			}
			rs.close();
			stmt.close();
		}catch(SQLException ex){
			throw new Exception("Error en la conexión");
		}
		return prestamo;
	}

}
