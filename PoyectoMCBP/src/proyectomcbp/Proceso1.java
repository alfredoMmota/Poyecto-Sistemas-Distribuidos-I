/*    ----------------------------------------------------------------------
 *    |                          Proyecto Final                            |
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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;

/**
 *
 * @author Alfredo Mota
 */
public class Proceso1 extends javax.swing.JFrame {

    conexion P1 = new conexion();//se crea un objeto de la clase conexion 
    AlgoritmoMCBP mcbp = new AlgoritmoMCBP();//se crea un objeto de la clase que verifica el estado de los mensajes
    byte[] buffer = new byte[1024];//declaracion de atributos
    int[] vector = new int[6];
    DatagramPacket recibido;
    String mensaje;
    int contador = 1;
    String dato;
    ArrayList<String> ci = new ArrayList();
    ArrayList<String> recibidos = new ArrayList();
    ArrayList<String> espera = new ArrayList();
    DefaultListModel mensajes = new DefaultListModel();
    String[] HM = new String[10];

    /**
     * Creates new form Proceso1
     */
    public Proceso1() {
        initComponents();
        P1.run(6790);//se asigna un puerto al proceso
        MostrarVT();//se muestra VT del proceso
        mensajes = new DefaultListModel();//se crea un modelo para el manejo de la lista 
        Historial.setModel(mensajes);//se agrega el modelo a la lista que aparece en la interfaz
        CI.setText("{ 0 }");//se setea un 0 para indicar que el CI inicialmente esta vacio
    }

    public void MostrarVT() {
        vector = mcbp.ObtenerVectorProceso(0);//se obtiene el Vt del proceso
        String vectorVT = "(";//se da el formato de la salida para la interfaz
        for (int i = 0; i < vector.length; i++) {
            if (i < vector.length - 1) {//se realiza un barrido del arreglo para ir agregando cada valor
                vectorVT = vectorVT + vector[i] + ",";
            } else {
                vectorVT = vectorVT + vector[i] + ")";
            }
            VT.setText(vectorVT);//se escribe el VT en el label dentrod e la interfaz
        }
    }

    public void recibir() {
        try {//se crea un datagrama para recibir los mensajes
            recibido = new DatagramPacket(buffer, buffer.length);
            String temporal;//se crea una variable de tipo String para almacenar el mensaje
            while (true) {//se coloca en un ciclo while para estar a espera del mensaje
                P1.Socket.receive(recibido);//se recibe el mensaje
                    //se alamcena el mensaje en temporal, se obtiene el mensaje y se guarda solo el valor que contiene
                    //delimitandolo con el metodo datagrampacket.getOffset() y datagrampacket.getLength(), esto para 
                    //evitar que el mensaje almacene valores de mensajes recibidos previamente
                temporal = new String(recibido.getData(), recibido.getOffset(), recibido.getLength());
                VerificarMensaje(temporal);//invoco el metodo que verifica lo que se ara con el mensaje
                TimeUnit.SECONDS.sleep(1);//genero un delay de 1 segundo. Esto en caso de que si ningun mensaje
                //a llegado solo se salga del metodo.
                break;
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(Proceso1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void VerificarMensaje(String a) {//funcion para verificar el estado del mensaje
        String cdi = "";//creo una variable String para mostrar el CI del proceso
        String[] Datos = new String[4];// se crran ArraList para almacenar valores
        String[] Datos2 = new String[10];
        String[] Datos3 = new String[10];
        String[] hmensaje = new String[10];
        Datos = a.split("-", 4);//se realiza una division del mensaje completo para obtener los datos 
        String hm = Datos[3];//separa la 3 parte del mensaje, esta sera mi hm
        if (hm.startsWith("0")) {//si mi hm empieza en 0, es decir vacia, crea una HM para la funcion que asigna el estado con un valor de 0
            mcbp.CrearHm(0);
        } else {//si mi hm contiene mas de 1 valor
            HM = hm.split(",");//divide y almacena los valores que contiene
            try {
                for (String HM1 : HM) {//se realiza un barrido de los elementos que contiene hm
                    dato = HM1;
                    mcbp.CrearHm(Integer.parseInt(dato));//se crea el HM que permite determinar el estado del mensaje
                }
            } catch (NumberFormatException ex) {//excepcion que se genera para el error de convertir un string a entero
                if (dato.startsWith("1")) {//dependiendo el valor que contenga realiza la accion 
                    mcbp.CrearHm(1);//seteandole un valor de tipo entero
                } else if (dato.startsWith("2")) {
                    mcbp.CrearHm(2);
                } else if (dato.startsWith("3")) {
                    mcbp.CrearHm(3);
                } else if (dato.startsWith("4")) {
                    mcbp.CrearHm(4);
                } else if (dato.startsWith("5")) {
                    mcbp.CrearHm(5);
                } else if (dato.startsWith("6")) {
                    mcbp.CrearHm(6);
                }
            }
        }//se crea una variable String para almacenar el estado resultante del mensaje
        String estado = mcbp.RecepcionMensajes(0, Integer.parseInt(Datos[0]), Integer.parseInt(Datos[1]), hm);
        if ("Entrega".equals(estado)) {//si el estado es entregado
            int posicion = Integer.parseInt(Datos[0]);//se determina la posicion, es decir el proceso del cual viene el mensaje
            mcbp.ModificarVT(0, posicion - 1);//se modifica VT con ese valor de posicion
            if (ci.isEmpty()) {//si mi ci esta vacio
                ci.add(Datos[0] + "," + Datos[1]);//agrega lso primeros dos elementos del mensaje a mi CI
                for (int i = 0; i < ci.size(); i++) {
                    cdi = cdi + "(" + ci.get(i) + ")";
                }
                CI.setText(cdi);//se muestra el Ci en la interfaz
            } else {//si mi CI ya contenia elementos
                int indice = 0;//se crea una variable como indice
                int tamaño2 = ci.size();//obtiene el tamaño de mi CI
                for (int i = 0; i < tamaño2; i++) {//entra a la primera condicion
                    Datos2 = ci.get(indice).split(",");//separa cada elemento en 2 valores
                    int temp = Integer.parseInt(Datos2[0]);//obtiene el primer valor deñ elemento en CI
                    int temp2 = Integer.parseInt(Datos[0]);//obtiene el valor del proceso que envia el mensaje
                    if (temp == temp2) {//si los valores coinciden
                        ci.remove(Datos2[0] + "," + Datos2[1]);//se elimina de CI ese elemento 
                    } else {
                        indice = indice + 1;//si no coincide solo se incrementa el indice
                    }
                }
                Datos3 = hm.split(",");//divide mi CI
                if (ci.isEmpty()) {//se revisa si mi CI esta vacio
                    ci.add(Datos[0] + "," + Datos[1]);//se agrega el CI 
                    for (int i = 0; i < ci.size(); i++) {
                        cdi = cdi + "(" + ci.get(i) + ")";
                    }
                    CI.setText(cdi);//se muestra en la Interfaz
                } else {//si Ci contiene elementos
                    int control = 0;//se crean variables auxiliares
                    int control2 = 0;
                    int tamaño = ci.size();//se determina el tamaño de CI 
                    for (int i = 0; i < tamaño; i++) {//se realiza una barrido de CI
                        hmensaje = ci.get(control).split(",");//se crea una variable que almacena cada valor del elemento de CI (cada elemento en CI esta compuesto por dos valores)
                        for (int j = 0; j < Datos3.length; j++) {
                            control2 = 0;
                            String validar = Datos3[j].trim();//se crea una copia del elemento de hm
                            String hmensaje1 = hmensaje[control2].trim();//se crea una copia del primer elemento de CI
                            if (hmensaje1.equals(validar)) {//se compara si el primer elemento es igual
                                String verificar = Datos3[j + 1].trim();//se crea una copia del segundo elemento de hm
                                String hmensaje2 = hmensaje[control2 + 1].trim();//copia del segundo elemento de CI
                                if (hmensaje2.equals(verificar)) {//se verifica si son igual
                                    String eliminar = ci.get(control).trim();//se obtiene el elemento de CI
                                    ci.remove(eliminar);//se remueve el elemento del arraglo que almacena CI
                                    control = control - 1;
                                }
                            }
                            j = j + 1;
                        }
                        control = control + 1;
                    }
                    ci.add(Datos[0] + "," + Datos[1]);//agrega el CI nuevo
                    for (int x = 0; x < ci.size(); x++) {
                        cdi = cdi + "(" + ci.get(x) + ")";
                    }
                    CI.setText(cdi);//se muestra CI en la interfaz
                }
            }
            Recibidos.append(estado + "(" + a + ")" + "\n");//se muestra la estructura del mensaje recibido
            recibidos.add(new String(recibido.getData()));
            MostrarVT();//se muestra el nuevo vector VT
            if (espera.size() > 0) {//si el arreglo espera contiene elementos, se compara si el mensaje entregado 
                //estaba en espera y de ser asi lo elimina de la lista
                int control = 0;
                int tamaño = espera.size();
                for (int i = 0; i < tamaño; i++) {
                    String verificar = a.trim();//crea una copia del mensaje completo
                    String valor = espera.get(control).trim();//obtiene una copia del elemento en la lista espera
                    if (valor.equals(verificar)) {//si son iguales 
                        espera.remove(verificar);//elimina ese elemento de la lista de espera
                        Espera.append("Se entrego " + a + "\n");//imprime en pantalla que el mensaje fue entregado
                    }
                }
                VerificarmensajesEnEspera();//se verifica los mensajes en espera
            }
        }
        if ("Espera".equals(estado)) {//si el estado del mensaje es Espera
            if (espera.isEmpty()) {//si la lista espera esta vacia 
                Espera.append(estado + "(" + a + ")" + "\n");//imprime la estructura del mensaje en la pantalla
                //agrega el mensaje a la lista
                espera.add(new String(recibido.getData(), recibido.getOffset(), recibido.getLength()));
            } else {//si contiene elementos la lista espera
                int condicion = 0;//variable auxiliar
                for (int x = 0; x < espera.size(); x++) {//realiza un barrido de la lista
                    String msj_en_espera = a.trim();//crea una copia del mensaje
                    String msj_en_espera_guardado = espera.get(x).trim();//crea una copia del elemento en la lista
                    if (msj_en_espera.equals(msj_en_espera_guardado)) {//si coinciden los elementos
                        condicion = 1;//cambia de valor la variable auxiliar
                    }
                }
                if (condicion == 1) {//si la variable auxiliar es 1, por que el elemento se encontraba en la lista
                    Espera.append(estado + "(" + a + ")" + "\n");//solo imprime el emento en pantalla
                } else {//si la variable auxiliar es 0, agrega a la lista un nuevo elemento, el cual sera un mensaje en espera
                    espera.add(new String(recibido.getData(), recibido.getOffset(), recibido.getLength()));
                    Espera.append(estado + "(" + a + ")" + "\n");//imprimo en la interfaz el mensaje que se quedara en espera
                }
            }
        }
    }

    public void VerificarmensajesEnEspera() {
        for (int i = 0; i < espera.size(); i++) {//se hace un barrido de la lista espera
            VerificarMensaje(espera.get(i));//se mandan a verificar para saber si seran entregados o se mantienen en espera
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        VT = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        CI = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        Recibidos = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        Espera = new javax.swing.JTextArea();
        Mensaje = new javax.swing.JTextField();
        Crear = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        P2 = new javax.swing.JButton();
        P3 = new javax.swing.JButton();
        P4 = new javax.swing.JButton();
        P5 = new javax.swing.JButton();
        P6 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        Reporte = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        Historial = new javax.swing.JList<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Proceso 1");

        jLabel2.setText("VT:");

        VT.setText(" ");

        jLabel3.setText("CI:");

        CI.setText(" ");

        jLabel5.setText("Mensajes Recibidos");

        jLabel6.setText("Mensajes en Espera");

        Recibidos.setColumns(20);
        Recibidos.setRows(5);
        jScrollPane1.setViewportView(Recibidos);

        Espera.setColumns(20);
        Espera.setRows(5);
        jScrollPane2.setViewportView(Espera);

        Crear.setText("Crear mensaje");
        Crear.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        Crear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CrearActionPerformed(evt);
            }
        });

        jLabel7.setText("Historial de Mensajes Creados");

        jLabel8.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel8.setText("Procesos");

        P2.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        P2.setText("2");
        P2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        P2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                P2ActionPerformed(evt);
            }
        });

        P3.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        P3.setText("3");
        P3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        P3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                P3ActionPerformed(evt);
            }
        });

        P4.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        P4.setText("4");
        P4.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        P4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                P4ActionPerformed(evt);
            }
        });

        P5.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        P5.setText("5");
        P5.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        P5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                P5ActionPerformed(evt);
            }
        });

        P6.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        P6.setText("6");
        P6.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        P6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                P6ActionPerformed(evt);
            }
        });

        jButton1.setText("Revisar Buffer");
        jButton1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        Reporte.setText(" ");

        jScrollPane4.setViewportView(Historial);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(51, 51, 51)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(VT, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(59, 59, 59)
                                .addComponent(jLabel5))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(34, 34, 34)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(CI, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel6)
                                .addGap(143, 143, 143))
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(Mensaje, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel7))
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(Crear, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Reporte, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(132, 132, 132))))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(P2, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(P3, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(P4, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(P5, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(P6, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(38, 38, 38))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(VT)
                    .addComponent(jLabel3)
                    .addComponent(CI))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                    .addComponent(jScrollPane2))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(Mensaje, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(Crear, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(86, 86, 86)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(Reporte, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(P3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(P6, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(P5, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(P4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(P2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void P2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_P2ActionPerformed
        //este proceso se repite en cada uno de los 5 botones de envio,
        //cambiando el numero de puerto dependiendo del proceso el cual sera el receptor del mensaje
        Reporte.setText(" ");//limpio la etiqueta
        try {
            byte[] m;//creo un arreglo de bytes
            InetAddress aHost = InetAddress.getByName(P1.getServer());//se obtiene la direccion
            String m1 = Historial.getSelectedValue();//se obtiene el mensaje de la lista que se muestra en la interfaz
            m = m1.getBytes();//se convierte a un arreglo de bytes
            DatagramPacket enviado = new DatagramPacket(m, m.length, aHost, P1.getDirecciones(1));//se crea el mensaje a enviar
            P1.Socket.send(enviado);//se envia el mensaje
            Reporte.setText("Mensaje Enviado");//se muestra en pantalla que el mensaje fue enviado
        } catch (UnknownHostException ex) {
            Logger.getLogger(Proceso1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Proceso1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_P2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        Reporte.setText(" ");
        recibir();//se llama a la funcion para recibir el mensaje
    }//GEN-LAST:event_jButton1ActionPerformed

    private void P3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_P3ActionPerformed
        Reporte.setText(" ");
        try {
            byte[] m;
            InetAddress aHost = InetAddress.getByName(P1.getServer());
            String m1 = Historial.getSelectedValue();
            m = m1.getBytes();
            DatagramPacket enviado = new DatagramPacket(m, m.length, aHost, P1.getDirecciones(2));
            P1.Socket.send(enviado);
            Reporte.setText("Mensaje Enviado");
        } catch (UnknownHostException ex) {
            Logger.getLogger(Proceso1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Proceso1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_P3ActionPerformed

    private void P4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_P4ActionPerformed
        Reporte.setText(" ");
        try {
            byte[] m;
            InetAddress aHost = InetAddress.getByName(P1.getServer());
            String m1 = Historial.getSelectedValue();
            m = m1.getBytes();
            DatagramPacket enviado = new DatagramPacket(m, m.length, aHost, P1.getDirecciones(3));
            P1.Socket.send(enviado);
            Reporte.setText("Mensaje Enviado");
        } catch (UnknownHostException ex) {
            Logger.getLogger(Proceso1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Proceso1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_P4ActionPerformed

    private void P5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_P5ActionPerformed
        Reporte.setText(" ");
        try {
            byte[] m;
            InetAddress aHost = InetAddress.getByName(P1.getServer());
            String m1 = Historial.getSelectedValue();
            m = m1.getBytes();
            DatagramPacket enviado = new DatagramPacket(m, m.length, aHost, P1.getDirecciones(4));
            P1.Socket.send(enviado);
            Reporte.setText("Mensaje Enviado");
        } catch (UnknownHostException ex) {
            Logger.getLogger(Proceso1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Proceso1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_P5ActionPerformed

    private void P6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_P6ActionPerformed
        Reporte.setText(" ");
        try {
            byte[] m;
            InetAddress aHost = InetAddress.getByName(P1.getServer());
            String m1 = Historial.getSelectedValue();
            m = m1.getBytes();
            DatagramPacket enviado = new DatagramPacket(m, m.length, aHost, P1.getDirecciones(5));
            P1.Socket.send(enviado);
            Reporte.setText("Mensaje Enviado");
        } catch (UnknownHostException ex) {
            Logger.getLogger(Proceso1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Proceso1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_P6ActionPerformed

    private void CrearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CrearActionPerformed
        //se crea el mensaje
        mensaje = "1-" + contador + "-" + Mensaje.getText() + "-";//se crea la estructura del mensaje
        if (ci.isEmpty()) {//si CI esta vacia 
            mensaje = mensaje + "0";//agrega un 0, el cual representa que esta vacio
        } else {//si contiene elementos
            for (int i = 0; i < ci.size(); i++) {//realiza un barrido de la lista
                if (i == ci.size() - 1) {
                    mensaje = mensaje + ci.get(i);//agrega todos los elementos de la lista como una cadena separada por comas(,)
                } else {
                    mensaje = mensaje + ci.get(i) + ",";
                }
            }
        }
        mensajes.addElement(mensaje);//agrega el mensaje como elemento del modelo que tiene la lista para que se muestre en pantalla
        mcbp.ModificarVT(0, 0);//se modifica VT en la posicion del proceso que envia, se incrementa en 1
        MostrarVT();//se muestra VT
        Mensaje.setText("");//se limpia el campo donde se escribe el mensaje
        contador = contador + 1;//se incrementa el contador que almacena que mensaje es
        ci.clear();//se limpia CI
        CI.setText("( 0 )");//se manda a pantalla un CI vacio
    }//GEN-LAST:event_CrearActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Proceso1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Proceso1().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel CI;
    private javax.swing.JButton Crear;
    private javax.swing.JTextArea Espera;
    private javax.swing.JList<String> Historial;
    private javax.swing.JTextField Mensaje;
    private javax.swing.JButton P2;
    private javax.swing.JButton P3;
    private javax.swing.JButton P4;
    private javax.swing.JButton P5;
    private javax.swing.JButton P6;
    private javax.swing.JTextArea Recibidos;
    private javax.swing.JLabel Reporte;
    private javax.swing.JLabel VT;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    // End of variables declaration//GEN-END:variables
}
