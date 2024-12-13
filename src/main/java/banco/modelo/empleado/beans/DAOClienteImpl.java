package banco.modelo.empleado.beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import banco.utils.Fechas;

public class DAOClienteImpl implements DAOCliente {

	private static Logger logger = LoggerFactory.getLogger(DAOClienteImpl.class);
	
	private Connection conexion;
	
	public DAOClienteImpl(Connection c) {
		this.conexion = c;
	}
	
	@Override
	public ClienteBean recuperarCliente(String tipoDoc, int nroDoc) throws Exception {

		logger.info("recupera el cliente con documento de tipo {} y nro {}.", tipoDoc, nroDoc);
		
		ClienteBean cliente = null;
		try{
			String sql= "SELECT nro_cliente, apellido, nombre, tipo_doc, nro_doc, direccion, telefono, fecha_nac " +
						"FROM cliente " +
						"WHERE tipo_doc = '" + tipoDoc + "' AND nro_doc = " + nroDoc;
			PreparedStatement ps = conexion.prepareStatement(sql);
			ResultSet resultSet = ps.executeQuery();
			if (resultSet!=null && resultSet.next()) {
				cliente= new ClienteBeanImpl();
				cliente.setNroCliente(resultSet.getInt("nro_cliente"));
				cliente.setApellido(resultSet.getString("apellido"));
				cliente.setNombre(resultSet.getString("nombre"));
				cliente.setTipoDocumento(resultSet.getString("tipo_doc"));
				cliente.setNroDocumento(resultSet.getInt("nro_doc"));
				cliente.setDireccion(resultSet.getString("direccion"));
				cliente.setTelefono(resultSet.getString("telefono"));
				cliente.setFechaNacimiento(Fechas.convertirStringADate(resultSet.getString("fecha_nac")));
				resultSet.close();
			}
			ps.close();
		} catch (SQLException ex){
			throw new Exception("No se pudo recuperar cliente.");
		}

		if(cliente==null){
			throw new Exception("El cliente no existe.");
		}

		return cliente;
	}

	@Override
	public ClienteBean recuperarCliente(Integer nroCliente) throws Exception {
		logger.info("recupera el cliente por nro de cliente.");
		
		ClienteBean cliente = null;
		try{
			String sql= "SELECT nro_cliente, apellido, nombre, tipo_doc, nro_doc, direccion, telefono, fecha_nac " +
					"FROM cliente " +
					"WHERE nro_cliente = " + nroCliente;
			PreparedStatement ps = conexion.prepareStatement(sql);
			ResultSet resultSet = ps.executeQuery();
			if (resultSet!=null && resultSet.next()) {
				cliente= new ClienteBeanImpl();
				cliente.setNroCliente(resultSet.getInt("nro_cliente"));
				cliente.setApellido(resultSet.getString("apellido"));
				cliente.setNombre(resultSet.getString("nombre"));
				cliente.setTipoDocumento(resultSet.getString("tipo_doc"));
				cliente.setNroDocumento(resultSet.getInt("nro_doc"));
				cliente.setDireccion(resultSet.getString("direccion"));
				cliente.setTelefono(resultSet.getString("telefono"));
				cliente.setFechaNacimiento(Fechas.convertirStringADate(resultSet.getString("fecha_nac")));
				resultSet.close();
			}
			ps.close();
		} catch (SQLException ex){
			throw new Exception("No se pudo recuperar cliente.");
		}

		if(cliente==null){
			throw new Exception("El cliente no existe.");
		}

		return cliente;
	}

}
