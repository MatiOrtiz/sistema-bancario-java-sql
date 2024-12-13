package banco.modelo.empleado.beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DAOClienteMorosoImpl implements DAOClienteMoroso {

	private static Logger logger = LoggerFactory.getLogger(DAOClienteMorosoImpl.class);
	
	private Connection conexion;
	
	public DAOClienteMorosoImpl(Connection c) {
		this.conexion = c;
	}
	
	@Override
	public ArrayList<ClienteMorosoBean> recuperarClientesMorosos() throws Exception {
		logger.info("Busca los clientes morosos.");
		
		DAOPrestamo daoPrestamo = new DAOPrestamoImpl(this.conexion);		
		DAOCliente daoCliente = new DAOClienteImpl(this.conexion);
		ArrayList<ClienteMorosoBean> morosos = new ArrayList<ClienteMorosoBean>();
		
		PrestamoBean prestamo = null;
		ClienteBean cliente = null;

		//todos los numeros de clientes morosos
		String sql =  "SELECT DISTINCT prestamo.nro_prestamo, prestamo.nro_cliente "+
			" FROM pago join prestamo on pago.nro_prestamo = prestamo.nro_prestamo "+
			" WHERE fecha_pago IS NULL AND fecha_venc < curdate(); ";

		String sqlCuotasAtrasadas = "SELECT count(nro_pago) AS cuotas_atrasadas FROM pago WHERE fecha_pago IS NULL AND nro_prestamo = ?;"; 
		try{
			Statement stmt = conexion.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			PreparedStatement cuotasAtrasadasstmt = conexion.prepareStatement(sqlCuotasAtrasadas);

			while(rs.next()){
				ClienteMorosoBean moroso = new ClienteMorosoBeanImpl();
				int nro_prestamo = rs.getInt("nro_prestamo");
				prestamo = daoPrestamo.recuperarPrestamo(nro_prestamo);
				cliente = daoCliente.recuperarCliente(rs.getInt("nro_cliente"));
				moroso.setCliente(cliente);
				moroso.setPrestamo(prestamo);

				cuotasAtrasadasstmt.setInt(1, nro_prestamo);
				ResultSet rsAtrasadas = cuotasAtrasadasstmt.executeQuery();
				if(rsAtrasadas.next()){
					moroso.setCantidadCuotasAtrasadas(rsAtrasadas.getInt("cuotas_atrasadas"));
					rsAtrasadas.close();
				}else{
					throw new Exception("Error al obtener la cantidad de cuotas atrasadas");
				}
				if(moroso.getCantidadCuotasAtrasadas()>= 2){
					morosos.add(moroso);
				}
			}
			rs.close();
			stmt.close();
			cuotasAtrasadasstmt.close();
		}catch(SQLException ex){
			throw new Exception("Error al obtener los clientes morosos");
		}
		return morosos;				
	}

}