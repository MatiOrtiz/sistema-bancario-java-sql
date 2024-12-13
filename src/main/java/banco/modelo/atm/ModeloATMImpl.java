package banco.modelo.atm;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import banco.modelo.ModeloImpl;
import banco.utils.Fechas;


public class ModeloATMImpl extends ModeloImpl implements ModeloATM {
	
	private static Logger logger = LoggerFactory.getLogger(ModeloATMImpl.class);	

	private String tarjeta = null;   // mantiene la tarjeta del cliente actual
	private Integer codigoATM = null;
	
	/*
	 * La información del cajero ATM se recupera del archivo que se encuentra definido en ModeloATM.CONFIG
	 */
	public ModeloATMImpl() {
		logger.debug("Se crea el modelo ATM.");

		logger.debug("Recuperación de la información sobre el cajero");
		
		Properties prop = new Properties();
		try (FileInputStream file = new FileInputStream(ModeloATM.CONFIG))
		{
			logger.debug("Se intenta leer el archivo de propiedades {}",ModeloATM.CONFIG);
			prop.load(file);

			codigoATM = Integer.valueOf(prop.getProperty("atm.codigo.cajero"));

			logger.debug("Código cajero ATM: {}", codigoATM);
		}
		catch(Exception ex)
		{
        	logger.error("Se produjo un error al recuperar el archivo de propiedades {}.",ModeloATM.CONFIG); 
		}
		return;
	}

	@Override
	public boolean autenticarUsuarioAplicacion(String tarjeta, String pin) throws Exception	{
		
		logger.info("Se intenta autenticar la tarjeta {} con pin {}", tarjeta, pin);

		boolean conexion= this.conectar("atm","atm");
		boolean resultado = false;

		if(conexion){
			try{
				long numTarjeta = Long.parseLong(tarjeta);
				String sql = "SELECT nro_tarjeta, PIN " +
								"FROM Tarjeta " +
								"WHERE nro_tarjeta = " + numTarjeta + " AND PIN = MD5('" + pin + "');";
				ResultSet resultSet = this.consulta(sql);
				if (resultSet != null && resultSet.next()){
					this.tarjeta = resultSet.getString("nro_tarjeta");
					resultado = true;
					resultSet.close();
				}
			}catch (SQLException ex) {
				logger.error("SQLException: {}", ex.getMessage());
				logger.error("SQLState: {}", ex.getSQLState());
				logger.error("VendorError: {}", ex.getErrorCode());
			}
		}else{
			throw new Exception("No se pudo conectar a la base de datos");
		}
		return resultado;
	}
	
	
	@Override
	public Double obtenerSaldo() throws Exception {
		logger.info("Se intenta obtener el saldo de cliente {}", 3);

		if (this.tarjeta == null ) {
			throw new Exception("El cliente no ingresó la tarjeta");
		}

		Double saldo= -1.0;

		try{
			long numTarjeta = Long.parseLong(this.tarjeta);
			String sql= "SELECT saldo " +
						"FROM tarjeta NATURAL JOIN trans_cajas_ahorro " +
						"WHERE nro_tarjeta = " + numTarjeta;
			ResultSet resultSet = this.consulta(sql);
			if (resultSet != null && resultSet.next()){
				saldo = resultSet.getDouble("saldo");
				resultSet.close();
			}
		} catch (SQLException ex){
			logger.error("SQLException: {}", ex.getMessage());
			logger.error("SQLState: {}", ex.getSQLState());
			logger.error("VendorError: {}", ex.getErrorCode());
			throw new Exception("No se pudo obtener el saldo");
		}

		return saldo;
	}	

	@Override
	public ArrayList<TransaccionCajaAhorroBean> cargarUltimosMovimientos() throws Exception {
		return this.cargarUltimosMovimientos(ModeloATM.ULTIMOS_MOVIMIENTOS_CANTIDAD);
	}	
	
	@Override
	public ArrayList<TransaccionCajaAhorroBean> cargarUltimosMovimientos(int cantidad) throws Exception {
		logger.info("Busca las ultimas {} transacciones en la BD de la tarjeta",cantidad);

		ArrayList<TransaccionCajaAhorroBean> lista = new ArrayList<TransaccionCajaAhorroBean>();

		try{
			long numTarjeta = Long.parseLong(this.tarjeta);
			String sql= "SELECT fecha, hora, tipo, (IF (tipo='Deposito', monto, monto * -1)) AS monto, cod_caja, destino "
						+ "FROM trans_cajas_ahorro INNER JOIN Tarjeta ON trans_cajas_ahorro.nro_ca= Tarjeta.nro_ca " +
						"WHERE nro_tarjeta = " + numTarjeta +
						" ORDER BY fecha DESC, hora DESC " +
						"lIMIT " + cantidad;

			ResultSet resultSet= this.consulta(sql);
			TransaccionCajaAhorroBean fila;
			if(resultSet != null) {
				while (resultSet.next()) {
					fila = new TransaccionCajaAhorroBeanImpl();

					fila.setTransaccionFechaHora(Fechas.convertirStringADate(resultSet.getString("fecha"),
							resultSet.getString("hora")));
					fila.setTransaccionTipo(resultSet.getString("tipo"));
					fila.setTransaccionMonto(resultSet.getDouble("monto"));
					fila.setTransaccionCodigoCaja(resultSet.getInt("cod_caja"));
					fila.setCajaAhorroDestinoNumero(resultSet.getInt("destino"));

					lista.add(fila);
				}
				resultSet.close();
			}
		} catch (SQLException ex) {
			logger.error("SQLException: {}", ex.getMessage());
			logger.error("SQLState: {}", ex.getSQLState());
			logger.error("VendorError: {}", ex.getErrorCode());
			throw new Exception("No fue posible acceder a los ultimos movimientos");
		}

		return lista;
	}	
	
	@Override
	public ArrayList<TransaccionCajaAhorroBean> cargarMovimientosPorPeriodo(Date desde, Date hasta)
			throws Exception {

		if (desde == null) {
			throw new Exception("El inicio del período no puede estar vacío");
		}
		if (hasta == null) {
			throw new Exception("El fin del período no puede estar vacío");
		}
		if (desde.after(hasta)) {
			throw new Exception("El inicio del período no puede ser posterior al fin del período");
		}	
		
		Date fechaActual = new Date();
		if (desde.after(fechaActual)) {
			throw new Exception("El inicio del período no puede ser posterior a la fecha actual");
		}	
		if (hasta.after(fechaActual)) {
			throw new Exception("El fin del período no puede ser posterior a la fecha actual");
		}				

		ArrayList<TransaccionCajaAhorroBean> lista = new ArrayList<TransaccionCajaAhorroBean>();

		try{
			long numTarjeta = Long.parseLong(this.tarjeta);
			String sql= "SELECT fecha, hora, tipo,  IF(tipo = 'Deposito', monto, monto * -1) AS monto, cod_caja, destino " +
						"FROM trans_cajas_ahorro, Tarjeta " +
						"WHERE nro_tarjeta = " + numTarjeta + " AND trans_cajas_ahorro.nro_ca = Tarjeta.nro_ca AND fecha >= '"
						+ Fechas.convertirDateADateSQL(desde) + "' AND fecha <= '" + Fechas.convertirDateADateSQL(hasta) + "' " +
						"ORDER BY fecha DESC,hora DESC";
			ResultSet resultSet= this.consulta(sql);
			TransaccionCajaAhorroBean fila;

			if (resultSet != null) {
				while (resultSet.next()) {
					fila = new TransaccionCajaAhorroBeanImpl();
					fila.setTransaccionFechaHora(Fechas.convertirStringADate(resultSet.getString("fecha"),
							resultSet.getString("hora")));
					fila.setTransaccionTipo(resultSet.getString("tipo"));
					fila.setTransaccionMonto(resultSet.getDouble("monto"));
					fila.setTransaccionCodigoCaja(resultSet.getInt("cod_caja"));
					fila.setCajaAhorroDestinoNumero(resultSet.getInt("destino"));

					lista.add(fila);
				}
				resultSet.close();
			}
		} catch (SQLException ex) {
			logger.error("SQLException: {}", ex.getMessage());
			logger.error("SQLState: {}", ex.getSQLState());
			logger.error("VendorError: {}", ex.getErrorCode());
			throw new Exception("No fue posible cargar los movimientos del periodo");
		}

		return lista;
	}
	
	@Override
	public Double extraer(Double monto) throws Exception {
		logger.info("Realiza la extraccion de ${} sobre la cuenta", monto);
		
		if (this.codigoATM == null) {
			throw new Exception("Hubo un error al recuperar la información sobre el ATM.");
		}
		if (this.tarjeta == null) {
			throw new Exception("Hubo un error al recuperar la información sobre la tarjeta del cliente.");
		}

		String resultado = "Extracción fallida";
		Double saldoActual= null;
		String saldoActualStr = null;
		try{
			String call = "{CALL extraer_dinero(?, ?, ?, ?)}";
			CallableStatement statement = conexion.prepareCall(call);

			statement.setLong(1, Long.parseLong(tarjeta));
			statement.setDouble(2, monto);
			statement.setInt(3, this.codigoATM);
			statement.registerOutParameter(4, java.sql.Types.VARCHAR);
			
			statement.executeUpdate();

			saldoActualStr = statement.getString(4);
			if ("Saldo insuficiente".equals(saldoActualStr)) {
				throw new Exception(saldoActualStr); 
			}
			
			saldoActual = Double.parseDouble(saldoActualStr);
			
			statement.close();
			resultado = ModeloATM.EXTRACCION_EXITOSA;
		} catch(SQLException ex){
			ex.printStackTrace();
			throw new Exception("Error al realizar la operación.");
		}

		if (!resultado.equals(ModeloATM.EXTRACCION_EXITOSA)) {
			throw new Exception(resultado);
		}
		
		return saldoActual;
	}

	
	@Override
	public int parseCuenta(String p_cuenta) throws Exception {
		
		logger.info("Intenta realizar el parsing de un codigo de cuenta {}", p_cuenta);

		if (p_cuenta == null) {
			throw new Exception("El código de la cuenta no puede ser vacío");
		}
		try{
			int cuenta = Integer.parseInt(p_cuenta);

			if(cuenta < 0){
				throw new Exception("El código de la cuenta no puede ser negativo.");
			}

			logger.info("Encontró la cuenta en la BD.");

			return cuenta;
			
		} catch(SQLException ex){
			logger.error("SQLException: {}", ex.getMessage());
			logger.error("SQLState: {}", ex.getSQLState());
			logger.error("VendorError: {}", ex.getErrorCode());
			throw new Exception("El código de la cuenta no tiene un formato válido.");
		}
		
	}	
	
	@Override
	public Double transferir(Double monto, int cajaDestino) throws Exception {
		logger.info("Realiza la transferencia de ${} sobre a la cuenta {}", monto, cajaDestino);	
		if (this.codigoATM == null) {
			throw new Exception("Hubo un error al recuperar la información sobre el ATM.");
		}
		if (this.tarjeta == null) {
			throw new Exception("Hubo un error al recuperar la información sobre la tarjeta del cliente.");
		}
		
		String resultado = "Transferencia fallida";
		Long numTarjeta = Long.parseLong(tarjeta);
		Double saldoResultante = -1d;
		String saldoResultanteStr = null;
	
		try{
			String sqlCall = "{call transferir_dinero(?, ?, ?, ?, ?)}";
			CallableStatement stmt = conexion.prepareCall(sqlCall);

			stmt.setLong(1, numTarjeta);
			stmt.setInt(2, cajaDestino);
			stmt.setDouble(3, monto);
			stmt.setInt(4, codigoATM);
			stmt.registerOutParameter(5, java.sql.Types.VARCHAR);

			stmt.executeUpdate();

			saldoResultanteStr = stmt.getString(5);
			if ("Saldo insuficiente".equals(saldoResultanteStr)) {
				throw new Exception(saldoResultanteStr); 
			}

			saldoResultante = Double.parseDouble(saldoResultanteStr);
		
			stmt.close();
			resultado = ModeloATM.TRANSFERENCIA_EXITOSA;
		}catch(SQLException ex){
			logger.info(ex.getMessage());
		}

		if (!resultado.equals(ModeloATM.TRANSFERENCIA_EXITOSA)) {
			throw new Exception(resultado);
		}
			
		return saldoResultante;
	}


	@Override
	public Double parseMonto(String p_monto) throws Exception {
		
		logger.info("Intenta realizar el parsing del monto {}", p_monto);
		
		if (p_monto == null) {
			throw new Exception("El monto no puede estar vacío");
		}

		try 
		{
			double monto = Double.parseDouble(p_monto);
			DecimalFormat df = new DecimalFormat("#.00");

			monto = Double.parseDouble(corregirComa(df.format(monto)));
			
			if(monto < 0)
			{
				throw new Exception("El monto no debe ser negativo.");
			}
			
			return monto;
		}		
		catch (NumberFormatException e)
		{
			throw new Exception("El monto no tiene un formato válido.");
		}	
	}

	private String corregirComa(String n)
	{
		String toReturn = "";
		
		for(int i = 0;i<n.length();i++)
		{
			if(n.charAt(i)==',')
			{
				toReturn = toReturn + ".";
			}
			else
			{
				toReturn = toReturn+n.charAt(i);
			}
		}
		
		return toReturn;
	}	
	
	

	
}
