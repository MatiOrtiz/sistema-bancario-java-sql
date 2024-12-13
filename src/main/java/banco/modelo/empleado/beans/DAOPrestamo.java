package banco.modelo.empleado.beans;

public interface DAOPrestamo {

	/**
	 * Crea el Prestamo segun el Bean.
	 *  
	 * @throws Exception
	 */
	public void crearPrestamo(PrestamoBean prestamo) throws Exception;


	/**
	 * Obtiene el prestamo según el id nroPrestamo
	 * 
	 * @param nroPrestamo
	 * @return Un prestamo que corresponde a ese id o null
	 * @throws Exception si hubo algun problema de conexión
	 */
	public PrestamoBean recuperarPrestamo(int nroPrestamo) throws Exception;
		
	
}
