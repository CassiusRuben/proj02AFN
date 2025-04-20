/*
	Utilice esta clase para guardar la informacion de su
	AFN. NO DEBE CAMBIAR LOS NOMBRES DE LA CLASE NI DE LOS 
	METODOS que ya existen, sin embargo, usted es libre de 
	agregar los campos y metodos que desee.
*/
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
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
		Set<Integer> actuales = new HashSet<>();
    	actuales.add(0); // empezamos desde el estado 0
    	actuales = cerrarLambda(actuales); // expandimos con lambda

    	for (int i = 0; i < string.length(); i++) {
        	String simbolo = String.valueOf(string.charAt(i));
        	Set<Integer> siguientes = new HashSet<>();

        	for (int estado : actuales) {
            	Set<Integer> destinos = transiciones.get(estado).get(simbolo);
            	siguientes.addAll(destinos); // agregamos todos los destinos por ese símbolo
        	}

        	// Expandimos con lambda desde los nuevos estados
        	actuales = cerrarLambda(siguientes);

        	// Si no hay estados vivos, ya no hay camino posible → cadena rechazada
        	if (actuales.isEmpty()) return false;
    	
		}

    	// Al final, si algún estado es final, aceptamos
    	for (int estado : actuales) {
        	if (estadosFinales[estado]) return true;
    	}

    	return false;
		
	}

	/*
	Implementación personal, cerrarLambda para calcular el alcance de lambda
	sobre los estados sin usar los símbolos de la cadena originalmente
	 */
	private Set<Integer> cerrarLambda(Set<Integer> estados) {
		Set<Integer> resultado = new HashSet<>(estados);
		Stack<Integer> pila = new Stack<>();
	
		// Inicializamos la pila con los estados recibidos
		for (int estado : estados) {
			pila.push(estado);
		}
	
		while (!pila.isEmpty()) {
			int actual = pila.pop();
	
			// Revisamos los estados a los que podemos ir por transiciones lambda
			for (int destino : transiciones.get(actual).get("lambda")) {
				if (!resultado.contains(destino)) {
					resultado.add(destino);
					pila.push(destino); // seguimos explorando desde este nuevo destino
				}
			}
		}
	
		return resultado;
	}
	


	/*
		Implemente el metodo toAFD. Este metodo debe generar un archivo
		de texto que contenga los datos de un AFD segun las especificaciones
		del proyecto.
	*/
	public void toAFD(String afdPath){
    try {
        // Estructuras para construir el AFD
        List<Set<Integer>> afdEstados = new ArrayList<>();
        Map<Set<Integer>, Integer> estadoID = new HashMap<>();
        Map<Integer, Map<String, Integer>> afdTransiciones = new HashMap<>();
        Set<Integer> afdFinales = new HashSet<>();

        // Paso 1: estado inicial del AFD
        Set<Integer> inicial = new HashSet<>();
        inicial.add(0);
        inicial = cerrarLambda(inicial);

        afdEstados.add(inicial);
        estadoID.put(inicial, 0);

        Queue<Set<Integer>> cola = new LinkedList<>();
        cola.add(inicial);

        int contadorEstados = 1;

        // Paso 2: construcción de todos los estados y transiciones
        while (!cola.isEmpty()) {
            Set<Integer> actual = cola.poll();
            int idActual = estadoID.get(actual);
            afdTransiciones.put(idActual, new HashMap<>());

            for (String simbolo : alfabeto) {
                Set<Integer> destinos = new HashSet<>();
                for (int estado : actual) {
                    destinos.addAll(transiciones.get(estado).get(simbolo));
                }

                // Cerradura lambda después de consumir el símbolo
                destinos = cerrarLambda(destinos);

                if (destinos.isEmpty()) {
                    afdTransiciones.get(idActual).put(simbolo, -1); // estado inválido
                } else {
                    if (!estadoID.containsKey(destinos)) {
                        estadoID.put(destinos, contadorEstados++);
                        afdEstados.add(destinos);
                        cola.add(destinos);
                    }
                    int idDestino = estadoID.get(destinos);
                    afdTransiciones.get(idActual).put(simbolo, idDestino);
                }
            }
        }

        // Paso 3: marcar estados finales del AFD
        for (int i = 0; i < afdEstados.size(); i++) {
            Set<Integer> conjunto = afdEstados.get(i);
            for (int estado : conjunto) {
                if (estadosFinales[estado]) {
                    afdFinales.add(i);
                    break;
                }
            }
        }

        // Paso 4: escribir el archivo
        PrintWriter writer = new PrintWriter(afdPath);

        // Línea 1: alfabeto
        writer.println(String.join(",", alfabeto));

        // Línea 2: cantidad de estados
        writer.println(afdEstados.size());

        // Línea 3: estados finales
        if (!afdFinales.isEmpty()) {
            List<String> finales = new ArrayList<>();
            for (int f : afdFinales) {
                finales.add(String.valueOf(f));
            }
            writer.println(String.join(",", finales));
        } else {
            writer.println(); // línea vacía si no hay finales
        }

        // Siguientes líneas: matriz de transición (una por símbolo)
        for (String simbolo : alfabeto) {
            List<String> fila = new ArrayList<>();
            for (int i = 0; i < afdEstados.size(); i++) {
                int destino = afdTransiciones.get(i).get(simbolo);
                if (destino == -1) {
                    fila.add("{}");
                } else {
                    fila.add("{" + destino + "}");
                }
            }
            writer.println(String.join(",", fila));
        }

        writer.close();

    } catch (Exception e) {
        System.err.println("Error al generar el AFD: " + e.getMessage());
    }
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
			if (args.length == 0) {
				System.out.println("Uso:");
				System.out.println("  java AFN archivo.afn                        → evaluar cadenas");
				System.out.println("  java AFN archivo.afn -to-afd salida.afd     → convertir a AFD");
				return;
			}
		
			String afnPath = args[0];
			AFN afn = new AFN(afnPath);
		
			// Modo: convertir a AFD
			if (args.length >= 2 && args[1].equals("-to-afd")) {
				if (args.length < 3) {
					System.out.println("Debe proporcionar el path del archivo de salida (.afd)");
					return;
				}
		
				String afdPath = args[2];
				afn.toAFD(afdPath);
				System.out.println("Archivo AFD generado exitosamente: " + afdPath);
			} else {
				// Modo: evaluación interactiva de cuerdas
				Scanner sc = new Scanner(System.in);
				while (true) {
					System.out.print("Ingrese cuerda (vacía para salir): ");
					String input = sc.nextLine();
		
					if (input.isEmpty()) break;
		
					boolean aceptada = afn.accept(input);
					System.out.println(aceptada ? "ACEPTADA" : "RECHAZADA");
				}
				sc.close();
			}
		
		
		
	}
}