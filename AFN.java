/*
	Utilice esta clase para guardar la informacion de su
	AFN. NO DEBE CAMBIAR LOS NOMBRES DE LA CLASE NI DE LOS 
	METODOS que ya existen, sin embargo, usted es libre de 
	agregar los campos y metodos que desee.
*/
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class AFN{
	private String[] alfabeto;
	private int cantidadEstados;
	private boolean[] estadosFinales;
	private Map<Integer, Map<String, Set<Integer>>> transiciones;


	/*
		Implemente el constructor de la clase AFN
		que recibe como argumento un string que 
		representa el path del archivo que contiene
		la informacion del AFN (i.e. "Documentos/archivo.AFN").
		Puede utilizar la estructura de datos que desee
	*/
	public AFN(String path){

    try {
        BufferedReader br = new BufferedReader(new FileReader(path));

        // Paso 1: leer alfabeto
        String linea = br.readLine();
        alfabeto = linea.split(",");

        // Paso 2: leer cantidad de estados
        linea = br.readLine();
        cantidadEstados = Integer.parseInt(linea);

        // Paso 3: leer estados finales
        linea = br.readLine();
        estadosFinales = new boolean[cantidadEstados];
        if (!linea.isEmpty()) {
            String[] finales = linea.split(",");
            for (String f : finales) {
                int estadoFinal = Integer.parseInt(f.trim());
                estadosFinales[estadoFinal] = true;
            }
        }

        // Paso 4: inicializar estructura de transiciones
        transiciones = new HashMap<>();
        for (int i = 0; i < cantidadEstados; i++) {
            transiciones.put(i, new HashMap<>());
            transiciones.get(i).put("lambda", new HashSet<>());
            for (String simbolo : alfabeto) {
                transiciones.get(i).put(simbolo, new HashSet<>());
            }
        }

        // Paso 5: leer matriz de transiciones
        List<String> simbolos = new ArrayList<>();
        simbolos.add("lambda"); // primera fila
        simbolos.addAll(Arrays.asList(alfabeto));

        for (String simbolo : simbolos) {
            linea = br.readLine();
            String[] celdas = linea.split(",");

            for (int estadoOrigen = 0; estadoOrigen < celdas.length; estadoOrigen++) {
                String celda = celdas[estadoOrigen].trim();

                if (!celda.equals("{}")) {
                    String contenido = celda.substring(1, celda.length() - 1); // quita {}
                    if (!contenido.isEmpty()) {
                        String[] destinos = contenido.split(";");
                        for (String dest : destinos) {
                            int estadoDestino = Integer.parseInt(dest.trim());
                            transiciones.get(estadoOrigen).get(simbolo).add(estadoDestino);
                        }
                    }
                }
            }
        }

        br.close();

    } catch (Exception e) {
        System.err.println("Error al leer el archivo: " + e.getMessage());
    }
}


	/*
		Implemente el metodo accept, que recibe como argumento
		un String que representa la cuerda a evaluar, y devuelve
		un boolean dependiendo de si la cuerda es aceptada o no 
		por el AFN. Recuerde lo aprendido en el proyecto 1.
	*/
	public boolean accept(String string){
		return false;
	}

	/*
		Implemente el metodo toAFD. Este metodo debe generar un archivo
		de texto que contenga los datos de un AFD segun las especificaciones
		del proyecto.
	*/
	public void toAFD(String afdPath){
	}

	/*
		El metodo main debe recibir como primer argumento el path
		donde se encuentra el archivo ".afd" y debe empezar a evaluar 
		cuerdas ingresadas por el usuario una a una hasta leer una cuerda vacia (""),
		en cuyo caso debe terminar. Tiene la libertad de implementar este metodo
		de la forma que desee. Si se envia la bandera "-to-afd", entonces en vez de
		evaluar, debe generar un archivo .afd
	*/
	public static void main(String[] args) throws Exception{
		
	}
}