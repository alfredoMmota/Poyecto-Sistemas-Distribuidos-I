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
public class Proceso5 extends javax.swing.JFrame {

    conexion P5 = new conexion();
    AlgoritmoMCBP mcbp = new AlgoritmoMCBP();
    byte[] buffer = new byte[1024];
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
    public Proceso5() {
        initComponents();
        P5.run(6794);
        MostrarVT();
        mensajes = new DefaultListModel();
        Historial.setModel(mensajes);
        CI.setText("{ 0 }");
    }

    public void MostrarVT() {
        vector = mcbp.ObtenerVectorProceso(4);
        String vectorVT = "(";
        for (int i = 0; i < vector.length; i++) {
            if (i < vector.length - 1) {
                vectorVT = vectorVT + vector[i] + ",";
            } else {
                vectorVT = vectorVT + vector[i] + ")";
            }
            VT.setText(vectorVT);
        }
    }

    public void recibir() {
        try {
            recibido = new DatagramPacket(buffer, buffer.length);
            String temporal;
            while (true) {
                P5.Socket.receive(recibido);
                temporal = new String(recibido.getData(), recibido.getOffset(), recibido.getLength());
                VerificarMensaje(temporal);
                TimeUnit.SECONDS.sleep(1);
                break;
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(Proceso5.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void VerificarMensaje(String a) {
        String cdi = "";
        String[] Datos = new String[4];
        String[] Datos2 = new String[10];
        String[] Datos3 = new String[10];
        String[] hmensaje = new String[10];
        Datos = a.split("-", 4);
        String hm = Datos[3];
        if (hm.startsWith("0")) {
            mcbp.CrearHm(0);
        } else {
            HM = hm.split(",");
            try {
                for (String HM1 : HM) {
                    dato = HM1;
                    mcbp.CrearHm(Integer.parseInt(dato));
                }
            } catch (NumberFormatException ex) {
                if (dato.startsWith("1")) {
                    mcbp.CrearHm(1);
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
        }
        String estado = mcbp.RecepcionMensajes(4, Integer.parseInt(Datos[0]), Integer.parseInt(Datos[1]), Datos[3]);
        if ("Entrega".equals(estado)) {
            int posicion = Integer.parseInt(Datos[0]);
            mcbp.ModificarVT(4, posicion - 1);
            if (ci.isEmpty()) {
                ci.add(Datos[0] + "," + Datos[1]);
                for (int i = 0; i < ci.size(); i++) {
                    cdi = cdi + "(" + ci.get(i) + ")";
                }
                CI.setText(cdi);
            } else {
                int indice = 0;
                int tamaño2 = ci.size();
                for (int i = 0; i < tamaño2; i++) {
                    Datos2 = ci.get(indice).split(",");
                    int temp = Integer.parseInt(Datos2[0]);
                    int temp2 = Integer.parseInt(Datos[0]);
                    if (temp == temp2) {
                        ci.remove(Datos2[0] + "," + Datos2[1]);
                    } else {
                        indice = indice + 1;
                    }
                }
                Datos3 = hm.split(",");
                if (ci.isEmpty()) {
                    ci.add(Datos[0] + "," + Datos[1]);
                    for (int i = 0; i < ci.size(); i++) {
                        cdi = cdi + "(" + ci.get(i) + ")";
                    }
                    CI.setText(cdi);
                } else {
                    int control = 0;
                    int control2 = 0;
                    int tamaño = ci.size();
                    for (int i = 0; i < tamaño; i++) {
                        hmensaje = ci.get(control).split(",");
                        for (int j = 0; j < Datos3.length; j++) {
                            control2 = 0;
                            String validar = Datos3[j].trim();
                            String hmensaje1 = hmensaje[control2].trim();
                            if (hmensaje1.equals(validar)) {
                                String verificar = Datos3[j + 1].trim();
                                String hmensaje2 = hmensaje[control2 + 1].trim();
                                if (hmensaje2.equals(verificar)) {
                                    String eliminar = ci.get(control).trim();
                                    ci.remove(eliminar);
                                }
                            }
                            j = j + 1;
                        }
                        control = control + 1;
                    }
                    ci.add(Datos[0] + "," + Datos[1]);
                    for (int x = 0; x < ci.size(); x++) {
                        cdi = cdi + "(" + ci.get(x) + ")";
                    }
                    CI.setText(cdi);
                }
            }
            Recibidos.append(estado + "(" + a + ")" + "\n");
            recibidos.add(new String(recibido.getData()));
            MostrarVT();
            if (espera.size() > 0) {
                int control = 0;
                int tamaño = espera.size();
                for (int i = 0; i < tamaño; i++) {
                    String verificar = a.trim();
                    String valor = espera.get(control).trim();
                    if (valor.equals(verificar)) {
                        espera.remove(verificar);
                        Espera.append("Se entrego " + a + "\n");
                    }
                }
                VerificarmensajesEnEspera();
            }
        }
        if ("Espera".equals(estado)) {
            if (espera.isEmpty()) {
                Espera.append(estado + "(" + a + ")" + "\n");
                espera.add(new String(recibido.getData(), recibido.getOffset(), recibido.getLength()));
            } else {
                int condicion = 0;
                for (int x = 0; x < espera.size(); x++) {
                    String msj_en_espera = a.trim();
                    String msj_en_espera_guardado = espera.get(x).trim();
                    if (msj_en_espera.equals(msj_en_espera_guardado)) {
                        condicion = 1;
                    }
                }
                if (condicion == 1) {
                    Espera.append(estado + "(" + a + ")" + "\n");
                } else {
                    espera.add(new String(recibido.getData(), recibido.getOffset(), recibido.getLength()));
                    Espera.append(estado + "(" + a + ")" + "\n");
                }
            }
        }
    }

    public void VerificarmensajesEnEspera() {
        for (int i = 0; i < espera.size(); i++) {
            VerificarMensaje(espera.get(i));
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

        jScrollPane3 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
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
        P1 = new javax.swing.JButton();
        P2 = new javax.swing.JButton();
        P3 = new javax.swing.JButton();
        P4 = new javax.swing.JButton();
        P6 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        Reporte = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        Historial = new javax.swing.JList<>();

        jScrollPane3.setViewportView(jEditorPane1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Proceso 5");

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

        P1.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        P1.setText("1");
        P1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        P1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                P1ActionPerformed(evt);
            }
        });

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
                .addComponent(P1, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(P2, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(P3, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(P4, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                    .addComponent(P2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(P6, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(P4, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(P3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(P1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void P1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_P1ActionPerformed
        Reporte.setText(" ");
        try {
            byte[] m;
            InetAddress aHost = InetAddress.getByName(P5.getServer());
            String m1 = Historial.getSelectedValue();
            m = m1.getBytes();
            DatagramPacket enviado = new DatagramPacket(m, m.length, aHost, P5.getDirecciones(0));
            P5.Socket.send(enviado);
            Reporte.setText("Mensaje Enviado");
        } catch (UnknownHostException ex) {
            Logger.getLogger(Proceso5.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Proceso5.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_P1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        Reporte.setText(" ");
        recibir();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void P2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_P2ActionPerformed
        Reporte.setText(" ");
        try {
            byte[] m;
            InetAddress aHost = InetAddress.getByName(P5.getServer());
            String m1 = Historial.getSelectedValue();
            m = m1.getBytes();
            DatagramPacket enviado = new DatagramPacket(m, m.length, aHost, P5.getDirecciones(1));
            P5.Socket.send(enviado);
            Reporte.setText("Mensaje Enviado");
        } catch (UnknownHostException ex) {
            Logger.getLogger(Proceso5.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Proceso5.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_P2ActionPerformed

    private void P3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_P3ActionPerformed
        Reporte.setText(" ");
        try {
            byte[] m;
            InetAddress aHost = InetAddress.getByName(P5.getServer());
            String m1 = Historial.getSelectedValue();
            m = m1.getBytes();
            DatagramPacket enviado = new DatagramPacket(m, m.length, aHost, P5.getDirecciones(2));
            P5.Socket.send(enviado);
            Reporte.setText("Mensaje Enviado");
        } catch (UnknownHostException ex) {
            Logger.getLogger(Proceso5.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Proceso5.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_P3ActionPerformed

    private void P4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_P4ActionPerformed
        Reporte.setText(" ");
        try {
            byte[] m;
            InetAddress aHost = InetAddress.getByName(P5.getServer());
            String m1 = Historial.getSelectedValue();
            m = m1.getBytes();
            DatagramPacket enviado = new DatagramPacket(m, m.length, aHost, P5.getDirecciones(3));
            P5.Socket.send(enviado);
            Reporte.setText("Mensaje Enviado");
        } catch (UnknownHostException ex) {
            Logger.getLogger(Proceso5.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Proceso5.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_P4ActionPerformed

    private void P6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_P6ActionPerformed
        Reporte.setText(" ");
        try {
            byte[] m;
            InetAddress aHost = InetAddress.getByName(P5.getServer());
            String m1 = Historial.getSelectedValue();
            m = m1.getBytes();
            DatagramPacket enviado = new DatagramPacket(m, m.length, aHost, P5.getDirecciones(5));
            P5.Socket.send(enviado);
            Reporte.setText("Mensaje Enviado");
        } catch (UnknownHostException ex) {
            Logger.getLogger(Proceso5.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Proceso5.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_P6ActionPerformed

    private void CrearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CrearActionPerformed
        mensaje = "5-" + contador + "-" + Mensaje.getText() + "-";
        if (ci.isEmpty()) {
            mensaje = mensaje + "0";
        } else {
            for (int i = 0; i < ci.size(); i++) {
                if (i == ci.size() - 1) {
                    mensaje = mensaje + ci.get(i);
                } else {
                    mensaje = mensaje + ci.get(i) + ",";
                }
            }
        }
        mensajes.addElement(mensaje);
        contador = contador + 1;
        mcbp.ModificarVT(4, 4);
        MostrarVT();
        Mensaje.setText("");
        ci.clear();
        CI.setText("( 0 )");
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
            java.util.logging.Logger.getLogger(Proceso5.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Proceso5().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel CI;
    private javax.swing.JButton Crear;
    private javax.swing.JTextArea Espera;
    private javax.swing.JList<String> Historial;
    private javax.swing.JTextField Mensaje;
    private javax.swing.JButton P1;
    private javax.swing.JButton P2;
    private javax.swing.JButton P3;
    private javax.swing.JButton P4;
    private javax.swing.JButton P6;
    private javax.swing.JTextArea Recibidos;
    private javax.swing.JLabel Reporte;
    private javax.swing.JLabel VT;
    private javax.swing.JButton jButton1;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    // End of variables declaration//GEN-END:variables
}
