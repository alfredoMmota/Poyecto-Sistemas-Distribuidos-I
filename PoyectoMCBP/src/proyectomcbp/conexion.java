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

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alfredo Mota
 */
public class conexion {

    //se declaran los atributos
    public int puerto;
    public String server;
    public DatagramSocket Socket;
    public int[] direcciones = new int[6];

    public int getPuerto() {
        return puerto;
    }

    public String getServer() {
        return server;
    }

    public DatagramSocket getSocket() {
        return Socket;
    }

    public conexion() {
        //se declara un vector con las direcciones de los procesos
        direcciones[0] = 6790;//proceso 1
        direcciones[1] = 6791;//proceso 2
        direcciones[2] = 6792;//proceso 3
        direcciones[3] = 6793;//proceso 4
        direcciones[4] = 6794;//proceso 5
        direcciones[5] = 6795;//proceso 6
    }

    public int getDirecciones(int indice) {
        return direcciones[indice];
    }

    public void run(int puerto) {//se crea el socket con el puerto
        try {
            Socket = new DatagramSocket(puerto);
            server = "localhost";
        } catch (SocketException ex) {
            Logger.getLogger(conexion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
