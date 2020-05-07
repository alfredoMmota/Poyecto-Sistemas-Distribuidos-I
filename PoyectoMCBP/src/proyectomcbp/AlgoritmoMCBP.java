/*    ----------------------------------------------------------------------
 *    |                         Proyecto Final                             |
 *    |          Implementacion del Protocolo Boadcast Causal Minimo       |
 *    |            Laboratorio Nacional de Informatica Avanzada            |
 *    |                Maestria en Computacion Aplicada                    |
 *    |               Sistemas Distribuidos y Ubicuos I                    |
 *    |                   Jose Alfredo Mendoza Mota                        |
 *    |                 Brenda Astrid Landa Contreras                      |
 *    |                   Juan Arturo Flores Rivera                        |
 *    ----------------------------------------------------------------------
 */
package proyectomcbp;

/**
 *
 * @author Alfredo Mota
 */
public class AlgoritmoMCBP {
//declaracion de vector VT,hm
    int[][] VT = new int[6][6];
    int[] hm = new int[20];
    int contadorhm = 0;

    public AlgoritmoMCBP() {
        
    }
//se llenan los 6 vectores iniciales para cada VT
    public int[][] Vectores() {
        for (int i = 0; i < 6; i++) {
            for (int y = 0; y < 6; y++) {
                VT[i][y] = 0;
            }
        }
        return VT;
    }
//regresa el vector especificoo de cada proceso
    public int[] ObtenerVectorProceso(int proceso) {
        int[] vector = new int[6];
        for (int x = 0; x < 6; x++) {
            vector[x] = VT[proceso][x];
        }
        return vector;
    }
//modifica la posicion de VT
    public void ModificarVT(int proceso, int indice) {
        VT[proceso][indice] = VT[proceso][indice] + 1;
    }
//creacion del hm para el mensaje que se quiere revisar
    public void CrearHm(int a) {
        if (a == 0) {
            hm[0] = 0;
        } else {
            hm[contadorhm] = a;
            contadorhm = contadorhm + 1;
        }
    }
//funcion que le asigna al mensaje el estado de entregado o en espera
    public String RecepcionMensajes(int proceso, int k, int tk, String a) {
        String estado = "";//creacion de una variable la cual nos muestra si el mensaje sera entregado o estara en espera
        int contador = 0;
        String[] Datos = new String[20];
        if (tk == (VT[proceso][k - 1]) + 1) {//primera condicion tk=VT(proceso)[k]+1 ejemplo 1=(0,0+1,0,0,0,0)
            if (a.startsWith("0")) {//segunda condicion de entraga
                estado = "Entrega";//si el ci llega vacio en automatico es entregado
            } else {//en caso contrario
                Datos = a.split(",");//divide c1 en el numero de elementos que contiene y se almacenan
                String[] valores = new String[(Datos.length) / 2];//se genera un arreglo de Strings para almacenar resultados
                for (int i = 0; i < Datos.length; i++) {
                    if (hm[i + 1] <= (VT[proceso][hm[i] - 1])) {//se compara la segunda condicion de entrega ejemplo 1<=(0,1,0,0,0,0)
                        valores[contador] = "verdadero";//se cumple, agrega una etiqueta verdadero al arreglo valores
                    } else {
                        valores[contador] = "falso";//no se cumple, agrega etiqueta falso al arreglo valores
                    }
                    contador = contador + 1;//incrementa el contador
                    i = i + 1;//incrementa en 1 i, de este modo en cada iteracion aumentara 2
                }
                int control = 0;//se crea una variable de control para determinar si sera entregado el mensaje
                for (int j = 0; j < valores.length; j++) {
                    if ("verdadero".equals(valores[j])) {//si la etiqueta dentro de valores coincide
                        control = control + 1;//incrementa en 1 la variable de control
                    }
                }
                if (control == valores.length) {//comparacion de la variable de control con el tamaño del arreglo valores
                    estado = "Entrega";//si coincide, el estado del mensaje sera entregado
                } else {
                    estado = "Espera";//si no coincide, el estado del mensaje sera en espera
                }
            }
        } else {
            estado = "Espera";//si no cumple la primer condicion en automatico pasa a Espera
        }
        contadorhm = 0;//reinicio el contador que me crea el hm
        return estado;//regreso la variable estado.
    }
}
