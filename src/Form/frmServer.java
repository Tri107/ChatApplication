/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Form;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 *
 * @author DELL
 */
public class frmServer extends javax.swing.JFrame {

    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    /**
     * Creates new form frmServer
     */
    public frmServer() {
        initComponents();
        btnkhoidong.addActionListener(evt -> startServer());
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(8386);
            txaserver.append("Server đã khởi động tại cổng 8386\n");

            Thread acceptThread = new Thread(() -> {
                while (!serverSocket.isClosed()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        txaserver.append("Client mới đã kết nối: " + clientSocket.getInetAddress() + "\n");
                        ClientHandler clientHandler = new ClientHandler(clientSocket);
                        clients.add(clientHandler);
                        new Thread(clientHandler).start();
                    } catch (IOException e) {
                        txaserver.append("Lỗi khi chấp nhận kết nối: " + e.getMessage() + "\n");
                    }
                }
            });
            acceptThread.start();

        } catch (IOException e) {
            txaserver.append("Lỗi khởi động server: " + e.getMessage() + "\n");
        }
    }

    private class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private BufferedInputStream bufferedIn;
    private DataOutputStream dataOut;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            bufferedIn = new BufferedInputStream(clientSocket.getInputStream());  // BufferedInputStream thay cho DataInputStream
            dataOut = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("IMAGE|")) {
                    handleImageTransfer();
                } else if (message.startsWith("FILE|")) {
                    handleFileTransfer();
                } else {
                    broadcastMessage(message);
                }
            }
        } catch (IOException e) {
            txaserver.append("Lỗi với client: " + e.getMessage() + "\n");
        } finally {
            closeConnection();
        }
    }

    private void handleImageTransfer() throws IOException {

        long imageSize = readLongFromBufferedStream();
        txaserver.append("Receiving image of size: " + imageSize + "\n");
        int nameLength = readIntFromBufferedStream();
        byte[] nameBytes = new byte[nameLength];
        readFully(bufferedIn, nameBytes);
        String imageName = new String(nameBytes, StandardCharsets.UTF_8);
        txaserver.append("Image name: " + imageName + "\n");
        byte[] imageData = new byte[(int) imageSize];
        readFully(bufferedIn, imageData);
        txaserver.append("Successfully received image: " + imageName + "\n");
        broadcastImage(imageName, imageData);
    }

    private void handleFileTransfer() throws IOException {
        long fileSize = readLongFromBufferedStream();
        txaserver.append("Receiving file of size: " + fileSize + "\n");
        int nameLength = readIntFromBufferedStream();
        byte[] nameBytes = new byte[nameLength];
        readFully(bufferedIn, nameBytes);
        String fileName = new String(nameBytes, StandardCharsets.UTF_8);
        //byte[] fileData = new byte[(int) fileSize];
        //readFully(bufferedIn, fileData);
        txaserver.append("Received file: " + fileName + " (Size: " + fileSize + " bytes)\n");
        broadcastFile(fileName);
    }

    private long readLongFromBufferedStream() throws IOException {
        byte[] longBytes = new byte[8];  // Long có kích thước 8 byte
        readFully(bufferedIn, longBytes);
         return ByteBuffer.wrap(longBytes)
                     .order(ByteOrder.BIG_ENDIAN)  // Đảm bảo sử dụng BIG_ENDIAN
                     .getLong(); 
    }

    private int readIntFromBufferedStream() throws IOException {
        byte[] intBytes = new byte[4];  // Int có kích thước 4 byte
        readFully(bufferedIn, intBytes);
        return ((int) (intBytes[0] & 0xFF) << 24)
             | ((int) (intBytes[1] & 0xFF) << 16)
             | ((int) (intBytes[2] & 0xFF) << 8)
             | ((int) (intBytes[3] & 0xFF));
    }

    private void readFully(BufferedInputStream in, byte[] buffer) throws IOException {
        int offset = 0;
    int bytesRead;
    while (offset < buffer.length && (bytesRead = in.read(buffer, offset, buffer.length - offset)) != -1) {
        offset += bytesRead;
    }
    if (offset < buffer.length) {
        throw new EOFException("Premature EOF encountered while reading data");
    }
    }

    private void broadcastMessage(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != this) {
                    client.out.println(message);
                }
            }
        }
    }

    private void broadcastImage(String imageName, byte[] imageData) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != this) {
                    try {
                        client.out.println("IMAGE|");
                        client.dataOut.writeLong(imageData.length);
                        byte[] nameBytes = imageName.getBytes("UTF-8");
                        client.dataOut.writeInt(nameBytes.length);
                        client.dataOut.write(nameBytes);
                        client.dataOut.write(imageData);
                        client.dataOut.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void broadcastFile(String fileName) {
    synchronized (clients) {
        for (ClientHandler client : clients) {
            if (client != this) {
                try {
                    // Gửi thông báo cho client rằng có một file mới
                    client.out.println("FILE|");

                    // Gửi tên file (không gửi nội dung file)
                    byte[] nameBytes = fileName.getBytes("UTF-8");
                    client.dataOut.writeInt(nameBytes.length);  // Gửi độ dài tên file
                    client.dataOut.write(nameBytes);            // Gửi tên file
                    client.dataOut.flush();  // Đảm bảo dữ liệu được gửi ngay lập tức
                    System.out.println("File name sent successfully to client: " + client.clientSocket.getInetAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Failed to send file name to client: " + client.clientSocket.getInetAddress());
                }
            }
        }
    }
}

    private void closeConnection() {
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (bufferedIn != null) {
                bufferedIn.close();
            }
            if (dataOut != null) {
                dataOut.close();
            }
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        clients.remove(this);
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

        jScrollPane1 = new javax.swing.JScrollPane();
        txaserver = new javax.swing.JTextArea();
        btnkhoidong = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        txaserver.setColumns(20);
        txaserver.setRows(5);
        jScrollPane1.setViewportView(txaserver);

        btnkhoidong.setText("jButton1");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(43, 43, 43)
                .addComponent(btnkhoidong)
                .addContainerGap(57, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnkhoidong)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(163, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(frmServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(frmServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(frmServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(frmServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new frmServer().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnkhoidong;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea txaserver;
    // End of variables declaration//GEN-END:variables
}
