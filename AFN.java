import java.io.*;
import java.util.*;

public class AFN {
    private char[] alfabeto; // Simbolos de entrada
    private int cantidadEstados; // Numero total de estados, incluye estado absorbente 0
    private Set<Integer> estadosFinales; // Conjunto de estados finales
    private List<Map<Integer, Set<Integer>>> matrizTransiciones; // Matriz: [simbolo][estado] -> conjunto de estados
    private static final int INDICE_LAMBDA = 0; // Indice para transiciones lambda

    // Constructor: Lee el AFN desde un archivo
    public AFN(String rutaArchivo) {
        // Inicializar estructuras por defecto para evitar NullPointerException
        alfabeto = new char[0];
        cantidadEstados = 0;
        estadosFinales = new HashSet<>();
        matrizTransiciones = new ArrayList<>();

        try {
            // Verificar si el archivo existe
            File archivo = new File(rutaArchivo);
            if (!archivo.exists()) {
                throw new IOException("El archivo " + rutaArchivo + " no existe");
            }
            BufferedReader lector = new BufferedReader(new FileReader(archivo));
            // Leer alfabeto
            String lineaAlfabeto = lector.readLine();
            if (lineaAlfabeto == null || lineaAlfabeto.trim().isEmpty()) {
                throw new IllegalArgumentException("El archivo esta vacio o no tiene alfabeto en la primera linea");
            }
            String[] simbolos = lineaAlfabeto.split(",");
            if (simbolos.length == 0) {
                throw new IllegalArgumentException("El alfabeto esta vacio o tiene un formato incorrecto");
            }
            alfabeto = new char[simbolos.length];
            for (int i = 0; i < simbolos.length; i++) {
                if (simbolos[i].trim().isEmpty()) {
                    throw new IllegalArgumentException("Simbolo vacio en el alfabeto en posicion " + i);
                }
                alfabeto[i] = simbolos[i].charAt(0);
            }
            // Leer cantidad de estados
            String lineaEstados = lector.readLine();
            if (lineaEstados == null || lineaEstados.trim().isEmpty()) {
                throw new IllegalArgumentException("Falta la linea con la cantidad de estados");
            }
            cantidadEstados = Integer.parseInt(lineaEstados.trim());
            if (cantidadEstados <= 0) {
                throw new IllegalArgumentException("La cantidad de estados debe ser mayor a 0");
            }
            // Leer estados finales
            estadosFinales = new HashSet<>();
            String lineaFinales = lector.readLine();
            if (lineaFinales == null || lineaFinales.trim().isEmpty()) {
                throw new IllegalArgumentException("Falta la linea con los estados finales");
            }
            String[] finales = lineaFinales.split(",");
            for (String estado : finales) {
                if (estado.trim().isEmpty()) {
                    throw new IllegalArgumentException("Estado final vacio en la lista");
                }
                estadosFinales.add(Integer.parseInt(estado.trim()));
            }
            // Inicializar matriz de transiciones
            matrizTransiciones = new ArrayList<>();
            for (int i = 0; i <= alfabeto.length; i++) {
                Map<Integer, Set<Integer>> transicionesEstado = new HashMap<>();
                for (int estado = 0; estado < cantidadEstados; estado++) {
                    transicionesEstado.put(estado, new HashSet<>());
                }
                matrizTransiciones.add(transicionesEstado);
            }
            // Leer matriz de transiciones
            for (int simbolo = 0; simbolo <= alfabeto.length; simbolo++) {
                String lineaTransicion = lector.readLine();
                if (lineaTransicion == null || lineaTransicion.trim().isEmpty()) {
                    throw new IllegalArgumentException("Falta linea de transiciones para simbolo " + simbolo);
                }
                String[] transiciones = lineaTransicion.split(",");
                if (transiciones.length != cantidadEstados) {
                    throw new IllegalArgumentException("Numero de transiciones (" + transiciones.length + ") no coincide con cantidad de estados (" + cantidadEstados + ")");
                }
                for (int estado = 0; estado < cantidadEstados; estado++) {
                    String destinos = transiciones[estado].trim();
                    Set<Integer> conjuntoDestinos = matrizTransiciones.get(simbolo).get(estado);
                    if (!destinos.isEmpty()) {
                        String[] estadosDestino = destinos.split(";");
                        for (String destino : estadosDestino) {
                            if (destino.trim().isEmpty()) {
                                throw new IllegalArgumentException("Destino vacio en transicion para estado " + estado);
                            }
                            conjuntoDestinos.add(Integer.parseInt(destino.trim()));
                        }
                    }
                }
            }
            lector.close();
        } catch (IOException error) {
            System.out.println("Error al leer archivo: " + error.getMessage());
            throw new RuntimeException("No se pudo leer el archivo", error);
        } catch (NumberFormatException error) {
            System.out.println("Error de formato numerico: " + error.getMessage());
            throw new RuntimeException("Formato numerico incorrecto en el archivo", error);
        } catch (IllegalArgumentException error) {
            System.out.println("Error en el formato del archivo: " + error.getMessage());
            throw new RuntimeException("Formato incorrecto en el archivo", error);
        }
    }

    // Calcula el cierre lambda de un conjunto de estados
    private Set<Integer> calcularCierreLambda(Set<Integer> estados) {
        Set<Integer> cierre = new HashSet<>(estados);
        LinkedList<Integer> pila = new LinkedList<>(estados);
        while (!pila.isEmpty()) {
            int estadoActual = pila.pop();
            Set<Integer> destinosLambda = matrizTransiciones.get(INDICE_LAMBDA).get(estadoActual);
            for (int destino : destinosLambda) {
                if (!cierre.contains(destino)) {
                    cierre.add(destino);
                    pila.push(destino);
                }
            }
        }
        return cierre;
    }

    // Verifica si una cuerda es aceptada por el AFN
    public boolean accept(String cuerda) {
        Set<Integer> estadosActuales = calcularCierreLambda(Collections.singleton(1));
        for (int i = 0; i < cuerda.length(); i++) {
            char simbolo = cuerda.charAt(i);
            int indiceSimbolo = -1;
            for (int j = 0; j < alfabeto.length; j++) {
                if (alfabeto[j] == simbolo) {
                    indiceSimbolo = j + 1; // Lambda esta en indice 0
                    break;
                }
            }
            if (indiceSimbolo == -1) {
                return false; // Simbolo no valido
            }
            Set<Integer> nuevosEstados = new HashSet<>();
            for (int estado : estadosActuales) {
                Set<Integer> destinos = matrizTransiciones.get(indiceSimbolo).get(estado);
                for (int destino : destinos) {
                    nuevosEstados.add(destino);
                }
            }
            estadosActuales = calcularCierreLambda(nuevosEstados);
        }
        for (int estado : estadosActuales) {
            if (estadosFinales.contains(estado)) {
                return true;
            }
        }
        return false;
    }

    // Convierte el AFN a AFD y guarda en archivo
    public void toAFD(String rutaAFD) {
        try {
            // Abrir archivo para escribir el AFD
            BufferedWriter escritor = new BufferedWriter(new FileWriter(rutaAFD));
            
            // Estructuras para el AFD
            // Lista de estados del AFD (cada estado es un conjunto de estados del AFN)
            List<Set<Integer>> estadosAFD = new ArrayList<>();
            // Mapa para asignar un ID a cada conjunto de estados
            Map<Set<Integer>, Integer> idEstados = new HashMap<>();
            // Lista de transiciones del AFD: para cada estado, un mapa de simbolo a estado destino
            List<Map<Character, Integer>> transicionesAFD = new ArrayList<>();
            // Conjunto de estados finales del AFD
            Set<Integer> estadosFinalesAFD = new HashSet<>();
            // Cola para procesar estados del AFD
            LinkedList<Set<Integer>> cola = new LinkedList<>();
            
            // Paso 1: Crear el estado inicial del AFD
            // Es el cierre lambda del estado inicial del AFN (estado 1)
            Set<Integer> estadoInicial = calcularCierreLambda(Collections.singleton(1));
            estadosAFD.add(estadoInicial);
            idEstados.put(estadoInicial, 1);
            // Crear mapa de transiciones para este estado
            Map<Character, Integer> transicionesIniciales = new HashMap<>();
            transicionesAFD.add(transicionesIniciales);
            cola.add(estadoInicial);
            int siguienteId = 2;
            
            // Paso 2: Construccion de subconjuntos
            while (cola.size() > 0) {
                // Obtener el siguiente estado del AFD para procesar
                Set<Integer> estadoActual = cola.removeFirst();
                int idActual = 0;
                // Buscar manualmente el ID del estado actual
                for (Map.Entry<Set<Integer>, Integer> entrada : idEstados.entrySet()) {
                    if (entrada.getKey().equals(estadoActual)) {
                        idActual = entrada.getValue();
                        break;
                    }
                }
                
                // Obtener el mapa de transiciones para este estado
                Map<Character, Integer> transiciones = null;
                for (int i = 0; i < transicionesAFD.size(); i++) {
                    if (i == (idActual - 1)) {
                        transiciones = transicionesAFD.get(i);
                        break;
                    }
                }
                
                // Verificar si este estado del AFD es final
                boolean esFinal = false;
                for (int estado : estadoActual) {
                    boolean encontrado = false;
                    for (int estadoFinal : estadosFinales) {
                        if (estado == estadoFinal) {
                            encontrado = true;
                            break;
                        }
                    }
                    if (encontrado) {
                        estadosFinalesAFD.add(idActual);
                        esFinal = true;
                        break;
                    }
                }
                
                // Procesar cada simbolo del alfabeto
                for (int indiceSimbolo = 0; indiceSimbolo < alfabeto.length; indiceSimbolo++) {
                    char simbolo = alfabeto[indiceSimbolo];
                    Set<Integer> estadosSiguientes = new HashSet<>();
                    
                    // Calcular los estados siguientes con este simbolo
                    for (int estado : estadoActual) {
                        // Buscar el indice del simbolo en la matriz (lambda esta en 0, simbolos empiezan en 1)
                        int posicionSimbolo = indiceSimbolo + 1;
                        // Obtener los destinos desde este estado con este simbolo
                        Map<Integer, Set<Integer>> transicionesSimbolo = matrizTransiciones.get(posicionSimbolo);
                        Set<Integer> destinos = null;
                        for (Map.Entry<Integer, Set<Integer>> entrada : transicionesSimbolo.entrySet()) {
                            if (entrada.getKey() == estado) {
                                destinos = entrada.getValue();
                                break;
                            }
                        }
                        // Agregar todos los destinos al conjunto de estados siguientes
                        if (destinos != null) {
                            for (int destino : destinos) {
                                estadosSiguientes.add(destino);
                            }
                        }
                    }
                    
                    // Calcular el cierre lambda del conjunto de estados siguientes
                    Set<Integer> nuevoEstado = calcularCierreLambda(estadosSiguientes);
                    
                    // Asignar un ID al nuevo estado
                    int idSiguiente = 0; // Por defecto, estado absorbente
                    if (nuevoEstado.size() > 0) {
                        // Verificar si el estado ya existe
                        boolean estadoExiste = false;
                        for (Set<Integer> estadoExistente : idEstados.keySet()) {
                            if (estadoExistente.equals(nuevoEstado)) {
                                for (Map.Entry<Set<Integer>, Integer> entrada : idEstados.entrySet()) {
                                    if (entrada.getKey().equals(nuevoEstado)) {
                                        idSiguiente = entrada.getValue();
                                        break;
                                    }
                                }
                                estadoExiste = true;
                                break;
                            }
                        }
                        // Si no existe, crear un nuevo estado
                        if (!estadoExiste) {
                            estadosAFD.add(nuevoEstado);
                            idEstados.put(nuevoEstado, siguienteId);
                            Map<Character, Integer> nuevasTransiciones = new HashMap<>();
                            transicionesAFD.add(nuevasTransiciones);
                            cola.add(nuevoEstado);
                            idSiguiente = siguienteId;
                            siguienteId = siguienteId + 1;
                        }
                    }
                    
                    // Agregar la transicion
                    transiciones.put(simbolo, idSiguiente);
                }
            }
            
            // Paso 3: Escribir el archivo AFD
            // Escribir alfabeto
            for (int i = 0; i < alfabeto.length; i++) {
                escritor.write(alfabeto[i]);
                if (i < (alfabeto.length - 1)) {
                    escritor.write(",");
                }
            }
            escritor.write("\n");
            
            // Escribir cantidad de estados (incluye estado absorbente 0)
            int totalEstados = estadosAFD.size() + 1;
            escritor.write(String.valueOf(totalEstados));
            escritor.write("\n");
            
            // Escribir estados finales
            // Convertir el conjunto a una lista para ordenar
            List<Integer> listaFinales = new ArrayList<>();
            for (int estado : estadosFinalesAFD) {
                listaFinales.add(estado);
            }
            // Ordenar manualmente (burbuja)
            for (int i = 0; i < listaFinales.size(); i++) {
                for (int j = 0; j < (listaFinales.size() - 1 - i); j++) {
                    int valor1 = listaFinales.get(j);
                    int valor2 = listaFinales.get(j + 1);
                    if (valor1 > valor2) {
                        listaFinales.set(j, valor2);
                        listaFinales.set(j + 1, valor1);
                    }
                }
            }
            // Escribir estados finales
            for (int i = 0; i < listaFinales.size(); i++) {
                escritor.write(String.valueOf(listaFinales.get(i)));
                if (i < (listaFinales.size() - 1)) {
                    escritor.write(",");
                }
            }
            escritor.write("\n");
            
            // Escribir matriz de transiciones
            for (int indiceSimbolo = 0; indiceSimbolo < alfabeto.length; indiceSimbolo++) {
                char simbolo = alfabeto[indiceSimbolo];
                for (int estado = 0; estado < totalEstados; estado++) {
                    int destino = 0; // Estado absorbente por defecto
                    if (estado > 0) {
                        // Buscar las transiciones para este estado
                        Map<Character, Integer> transicionesEstado = transicionesAFD.get(estado - 1);
                        boolean transicionEncontrada = false;
                        for (Map.Entry<Character, Integer> entrada : transicionesEstado.entrySet()) {
                            if (entrada.getKey() == simbolo) {
                                destino = entrada.getValue();
                                transicionEncontrada = true;
                                break;
                            }
                        }
                        if (!transicionEncontrada) {
                            destino = 0; // Si no hay transicion, va al estado absorbente
                        }
                    }
                    escritor.write(String.valueOf(destino));
                    if (estado < (totalEstados - 1)) {
                        escritor.write(",");
                    }
                }
                escritor.write("\n");
            }
            
            escritor.close();
        } catch (IOException error) {
            System.out.println("Error al escribir AFD: " + error.getMessage());
        }
    }

    // Metodo principal para modo interactivo y conversion a AFD
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso: java AFN <archivo_afn> [-to-afd <archivo_salida>]");
            return;
        }
        AFN automata = null;
        try {
            automata = new AFN(args[0]);
        } catch (RuntimeException error) {
            System.out.println("Error al inicializar el AFN: " + error.getMessage());
            return;
        }
        if (args.length >= 2 && args[1].equals("-to-afd")) {
            if (args.length != 3) {
                System.out.println("Uso: java AFN <archivo_afn> -to-afd <archivo_salida>");
                return;
            }
            automata.toAFD(args[2]);
        } else {
            BufferedReader lector = new BufferedReader(new InputStreamReader(System.in));
            String entrada;
            try {
                while (true) {
                    entrada = lector.readLine();
                    if (entrada == null || entrada.length() == 0) {
                        break;
                    }
                    if (automata.accept(entrada)) {
                        System.out.println("T");
                    } else {
                        System.out.println("F");
                    }
                }
                lector.close();
            } catch (IOException error) {
                System.out.println("Error al leer entrada: " + error.getMessage());
            }
        }
    }
}
// forma de ejecución para convertir AFN A AFD: java AFN tests/afn/nombre_test.afn
// forma de ejecución para probar cuerdas: java AFN tests/afn/nombre_test.afn