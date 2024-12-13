package banco.modelo.empleado;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.naming.spi.DirStateFactory.Result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import banco.modelo.ModeloImpl;
import banco.modelo.empleado.beans.ClienteBean;
import banco.modelo.empleado.beans.ClienteMorosoBean;
import banco.modelo.empleado.beans.DAOCliente;
import banco.modelo.empleado.beans.DAOClienteImpl;
import banco.modelo.empleado.beans.DAOClienteMoroso;
import banco.modelo.empleado.beans.DAOClienteMorosoImpl;
import banco.modelo.empleado.beans.DAOEmpleado;
import banco.modelo.empleado.beans.DAOEmpleadoImpl;
import banco.modelo.empleado.beans.DAOPago;
import banco.modelo.empleado.beans.DAOPagoImpl;
import banco.modelo.empleado.beans.DAOPrestamo;
import banco.modelo.empleado.beans.DAOPrestamoImpl;
import banco.modelo.empleado.beans.EmpleadoBean;
import banco.modelo.empleado.beans.PagoBean;
import banco.modelo.empleado.beans.PrestamoBean;

public class ModeloEmpleadoImpl extends ModeloImpl implements ModeloEmpleado {

	private static Logger logger = LoggerFactory.getLogger(ModeloEmpleadoImpl.class);	

	// Indica el usuario actualmente logueado. Null corresponde que todavia no se ha autenticado
	private Integer legajo = null;
	
	public ModeloEmpleadoImpl() {
		logger.debug("Se crea el modelo Empleado.");
	}
	

	@Override
	public boolean autenticarUsuarioAplicacion(String legajo, String password) throws Exception {
		logger.info("Se intenta autenticar el legajo {} con password {}", legajo, password);
		
		Integer legajoInt;
		boolean conexion = conectar("empleado", "empleado");

		try {
			legajoInt = Integer.valueOf(legajo.trim());
        }
        catch (Exception ex) {
        	throw new Exception("Se esperaba que el legajo sea un valor entero.");
        }

		String sql = "SELECT legajo, password FROM banco.empleado WHERE legajo ="+legajoInt+" AND password=MD5('"+password+"')";

		if(conexion){
			try{
				ResultSet rs = this.consulta(sql);
				if(!rs.next()){
					conexion = false;
				}
				rs.close();
			}catch(SQLException ex){
				logger.error("SQLException: {}", ex.getMessage());
				logger.error("SQLState: {}", ex.getSQLState());
				logger.error("VendorError: {}", ex.getErrorCode());
			}	
		}
		this.legajo = legajoInt;
		return conexion;
	}
	
	@Override
	public EmpleadoBean obtenerEmpleadoLogueado() throws Exception {
		logger.info("Solicita al DAO un empleado con legajo {}", this.legajo);
		if (this.legajo == null) {
			logger.info("No hay un empleado logueado.");
			throw new Exception("No hay un empleado logueado. La sesión terminó.");
		}
		
		DAOEmpleado dao = new DAOEmpleadoImpl(this.conexion);
		return dao.recuperarEmpleado(this.legajo);
	}	
	
	@Override
	public ArrayList<String> obtenerTiposDocumento() throws Exception {
		logger.info("recupera los tipos de documentos.");
		
		ArrayList<String> tipos = new ArrayList<String>();
		String sql = "SELECT DISTINCT tipo_doc FROM empleado;" ;
		try{
			ResultSet rs = this.consulta(sql);
			int i = 1;
			while(rs != null && rs.next()){
				String tipo = rs.getString(i);
				tipos.add(tipo);
				i++;
				System.out.println(tipo);
			}
			rs.close();
		}catch(SQLException ex){
			logger.error("SQLException: {}", ex.getMessage());
			logger.error("SQLState: {}", ex.getSQLState());
			logger.error("VendorError: {}", ex.getErrorCode());
		}
		return tipos;
	}	

	@Override
	public double obtenerTasa(double monto, int cantidadMeses) throws Exception {
		logger.info("Busca la tasa correspondiente a el monto {} con una cantidad de meses {}", monto, cantidadMeses);
		monto = Math.round(monto*100)/100;
		String sql = "SELECT tasa FROM tasa_prestamo WHERE periodo="+cantidadMeses+" AND monto_inf<="+monto+" AND monto_sup>="+monto+" ;";
		double tasa = 0;
		try{
			ResultSet rs = this.consulta(sql);
			if(rs != null && rs.next()){
				tasa = rs.getDouble(1);
			}else{
				throw new Exception("No se encontraron tasas correspondientes al monto y cantidad de meses");
			}
			rs.close();
		}catch(SQLException ex){
			logger.error("SQLException: {}", ex.getMessage());
			logger.error("SQLState: {}", ex.getSQLState());
			logger.error("VendorError: {}", ex.getErrorCode());
		}
   		return tasa;
	}

	@Override
	public double obtenerInteres(double monto, double tasa, int cantidadMeses) {
		return (monto * tasa * cantidadMeses) / 1200;
	}


	@Override
	public double obtenerValorCuota(double monto, double interes, int cantidadMeses) {
		return (monto + interes) / cantidadMeses;
	}
		

	@Override
	public ClienteBean recuperarCliente(String tipoDoc, int nroDoc) throws Exception {
		DAOCliente dao = new DAOClienteImpl(this.conexion);
		return dao.recuperarCliente(tipoDoc, nroDoc);
	}


	@Override
	public ArrayList<Integer> obtenerCantidadMeses(double monto) throws Exception {
		logger.info("recupera los períodos (cantidad de meses) según el monto {} para el prestamo.", monto);
		ArrayList<Integer> cantMeses = new ArrayList<Integer>();
		Set<Integer> periodosValidos = Set.of(6,12,18,24,60,120);

		String sql = "SELECT periodo FROM tasa_prestamo WHERE monto_sup>="+monto+" AND monto_inf<="+monto+" ;";
		try{
			ResultSet rs = this.consulta(sql);
			if(!rs.isBeforeFirst()){
				throw new Exception("No hay períodos disponibles para el monto ingresado");
			}
			else{
				while (rs.next()) {
					int periodo = rs.getInt(1);
					if(periodosValidos.contains(periodo))
						cantMeses.add(periodo);
				}
			}
			rs.close();
		}catch(SQLException ex){
			logger.error("SQLException: {}", ex.getMessage());
			logger.error("SQLState: {}", ex.getSQLState());
			logger.error("VendorError: {}", ex.getErrorCode());
		}	
		return cantMeses;
	}

	@Override	
	public Integer prestamoVigente(int nroCliente) throws Exception {
		logger.info("Verifica si el cliente {} tiene algun prestamo que tienen cuotas por pagar.", nroCliente);
		Integer prestamo = null;
		String sql = "SELECT DISTINCT prestamo.nro_prestamo "+
			"FROM prestamo JOIN pago ON prestamo.nro_prestamo = pago.nro_prestamo "+
			"WHERE prestamo.nro_cliente = "+nroCliente+" AND fecha_pago IS NULL;";

		try{
			ResultSet rs = this.consulta(sql);
			if(rs!= null){
				if(rs.next())
					prestamo = rs.getInt(1);
				rs.close();
			}
		}catch(SQLException ex){
			logger.error("SQLException: {}", ex.getMessage());
			logger.error("SQLState: {}", ex.getSQLState());
			logger.error("VendorError: {}", ex.getErrorCode());
		}
		return prestamo;
	}


	@Override
	public void crearPrestamo(PrestamoBean prestamo) throws Exception {
		logger.info("Crea un nuevo prestamo.");
		
		if (this.legajo == null) {
			throw new Exception("No hay un empleado registrado en el sistema que se haga responsable por este prestamo.");
		}
		else 
		{
			logger.info("Actualiza el prestamo con el legajo {}",this.legajo);
			prestamo.setLegajo(this.legajo);
			
			DAOPrestamo dao = new DAOPrestamoImpl(this.conexion);		
			dao.crearPrestamo(prestamo);
		}
	}
	
	@Override
	public PrestamoBean recuperarPrestamo(int nroPrestamo) throws Exception {
		logger.info("Busca el prestamo número {}", nroPrestamo);
		
		DAOPrestamo dao = new DAOPrestamoImpl(this.conexion);		
		return dao.recuperarPrestamo(nroPrestamo);
	}
	
	@Override
	public ArrayList<PagoBean> recuperarPagos(Integer prestamo) throws Exception {
		logger.info("Solicita la busqueda de pagos al modelo sobre el prestamo {}.", prestamo);
		
		DAOPago dao = new DAOPagoImpl(this.conexion);		
		return dao.recuperarPagos(prestamo);
	}
	

	@Override
	public void pagarCuotas(String p_tipo, int p_dni, int nroPrestamo, List<Integer> cuotasAPagar) throws Exception {
		
		// Valida que sea un cliente que exista sino genera una excepción
		ClienteBean c = this.recuperarCliente(p_tipo.trim(), p_dni);

		// Valida el prestamo
		if (nroPrestamo != this.prestamoVigente(c.getNroCliente())) {
			throw new Exception ("El nro del prestamo no coincide con un prestamo vigente del cliente");
		}

		if (cuotasAPagar.size() == 0) {
			throw new Exception ("Debe seleccionar al menos una cuota a pagar.");
		}
		
		DAOPago dao = new DAOPagoImpl(this.conexion);
		dao.registrarPagos(nroPrestamo, cuotasAPagar);		
	}


	@Override
	public ArrayList<ClienteMorosoBean> recuperarClientesMorosos() throws Exception {
		logger.info("Modelo solicita al DAO que busque los clientes morosos");
		DAOClienteMoroso dao = new DAOClienteMorosoImpl(this.conexion);
		return dao.recuperarClientesMorosos();	
	}
	

	
}
