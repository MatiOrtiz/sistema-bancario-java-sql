package banco.modelo.empleado.beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.cj.x.protobuf.MysqlxDatatypes.Scalar.String;
import com.mysql.cj.xdevapi.Statement;

import banco.utils.Fechas;


public class DAOPagoImpl implements DAOPago {

	private static Logger logger = LoggerFactory.getLogger(DAOPagoImpl.class);
	
	private Connection conexion;
	
	public DAOPagoImpl(Connection c) {
		this.conexion = c;
	}

	@Override
	public ArrayList<PagoBean> recuperarPagos(int nroPrestamo) throws Exception {
		logger.info("Inicia la recuperacion de los pagos del prestamo {}", nroPrestamo);

		java.lang.String sql = "SELECT * FROM pago WHERE nro_prestamo ="+nroPrestamo+" ;";
		ArrayList<PagoBean> lista = new ArrayList<PagoBean>();

		try{
			java.sql.Statement stmt = conexion.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if(!rs.isBeforeFirst()){
				throw new Exception("No se encontraron pagos para el prestamo");
			}else{
				while (rs.next()) {
					PagoBean fila = new PagoBeanImpl();
					fila.setNroPrestamo(nroPrestamo);
					fila.setNroPago(rs.getInt("nro_pago"));
					fila.setFechaVencimiento(Fechas.convertirStringADate(rs.getString("fecha_venc")));
					fila.setFechaPago(Fechas.convertirStringADate(rs.getString("fecha_pago")));
					lista.add(fila);
				}
			}
			
		}catch(SQLException ex){
			throw new Exception("Error al recuperar los pagos del prestamo");
		}		
		return lista;
	}

	@Override
	public void registrarPagos(int nroPrestamo, List<Integer> cuotasAPagar)  throws Exception {
		logger.info("Inicia el pago de las {} cuotas del prestamo {}", cuotasAPagar.size(), nroPrestamo);
		
		java.lang.String sqlConsulta = "SELECT fecha_pago FROM pago WHERE nro_pago = ? AND nro_prestamo = ?";
		java.lang.String sqlActualizacion = "UPDATE pago SET fecha_pago = ? WHERE nro_pago = ? AND nro_prestamo = ?"; 

		//fecha actual
		Date fecha = new Date();
		java.lang.String fechaStr = Fechas.convertirDateAStringDB(fecha);

		try{
			PreparedStatement selectStmt = conexion.prepareStatement(sqlConsulta);
			PreparedStatement updateStmt = conexion.prepareStatement(sqlActualizacion);
			
			for (Integer cuota : cuotasAPagar) {
				//Verifica si la cuota ya fue pagada
				selectStmt.setInt(1, cuota);
				selectStmt.setInt(2, nroPrestamo);
				ResultSet rs = selectStmt.executeQuery();

				//controla  que la cuota no esté paga
				if (rs.next()) {
					Date fechaPago = rs.getDate("fecha_pago");
					if (fechaPago == null) {
						updateStmt.setString(1, fechaStr);
						updateStmt.setInt(2, cuota);
						updateStmt.setInt(3, nroPrestamo);
						updateStmt.executeUpdate();
					} 
				rs.close();
				}
			}
			selectStmt.close();
       		updateStmt.close();
		}catch(SQLException ex){
			throw new Exception(ex.getMessage());
		}
	}
}
