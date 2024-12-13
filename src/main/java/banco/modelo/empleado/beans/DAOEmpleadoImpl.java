package banco.modelo.empleado.beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DAOEmpleadoImpl implements DAOEmpleado {

	private static Logger logger = LoggerFactory.getLogger(DAOEmpleadoImpl.class);
	
	private Connection conexion;
	
	public DAOEmpleadoImpl(Connection c) {
		this.conexion = c;
	}


	@Override
	public EmpleadoBean recuperarEmpleado(int legajo) throws Exception {
		logger.info("recupera el empleado que corresponde al legajo {}.", legajo);
		
		/**
		 * TODO Debe recuperar los datos del empleado que corresponda al legajo pasado como parámetro.
		 *      Si no existe deberá retornar null y 
		 *      De ocurre algun error deberá generar una excepción.		 * 
		 */		
		EmpleadoBean empleado = null;
		
		try{
			String sql= "SELECT legajo, apellido, nombre, tipo_doc, nro_doc, direccion, telefono, cargo, password, nro_suc " +
						"FROM empleado " +
						"WHERE legajo = " + legajo;
			PreparedStatement ps= conexion.prepareStatement(sql);
			ResultSet resultSet= ps.executeQuery();
			if(resultSet!=null && resultSet.next()){
				empleado = new EmpleadoBeanImpl();
			empleado.setLegajo(resultSet.getInt("legajo"));
			empleado.setApellido(resultSet.getString("apellido"));
			empleado.setNombre(resultSet.getString("nombre"));
			empleado.setTipoDocumento(resultSet.getString("tipo_doc"));
			empleado.setNroDocumento(resultSet.getInt("nro_doc"));
			empleado.setDireccion(resultSet.getString("direccion"));
			empleado.setTelefono(resultSet.getString("telefono"));
			empleado.setCargo(resultSet.getString("cargo"));
			empleado.setPassword(resultSet.getString("password")); 
			empleado.setNroSucursal(resultSet.getInt("nro_suc"));
			resultSet.close();
			}
			ps.close();
		} catch(SQLException ex){
			throw new Exception("No se pudo recuperar el empleado.");
		}
		
		return empleado;
	}

}
