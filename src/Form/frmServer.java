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
import java.nio.file.Files;
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
                    } else if (message.startsWith("DOWNLOAD|")) {
                        String fileName = message.substring(9); // Cắt bỏ "DOWNLOAD|"
                        handleDownloadRequest(fileName, dataOut); // Gọi phương thức tải file
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
            txaserver.append("Bắt đầu nhận file...\n");

            // Đọc kích thước file
            long fileSize = readLongFromBufferedStream();
            txaserver.append("Kích thước file: " + fileSize + " bytes\n");

            // Đọc độ dài và tên file
            int nameLength = readIntFromBufferedStream();
            byte[] nameBytes = new byte[nameLength];
            readFully(bufferedIn, nameBytes);
            String fileName = new String(nameBytes, StandardCharsets.UTF_8);
            txaserver.append("Tên file: " + fileName + "\n");

            // Đọc nội dung file
            byte[] fileData = new byte[(int) fileSize];
            readFully(bufferedIn, fileData);
            txaserver.append("Dữ liệu file đã được nhận đủ.\n");

            // Lưu file
            String serverFilesPath = "D:\\server_files";
            File outputDir = new File(serverFilesPath);
            if (!outputDir.exists() && !outputDir.mkdirs()) {
                txaserver.append("Không thể tạo thư mục: " + serverFilesPath + "\n");
                return;
            }
            File outputFile = new File(outputDir, fileName);
            try (FileOutputStream fileOut = new FileOutputStream(outputFile)) {
                fileOut.write(fileData);
                txaserver.append("File đã được lưu tại: " + outputFile.getAbsolutePath() + "\n");
            } catch (IOException e) {
                txaserver.append("Lỗi khi lưu file: " + e.getMessage() + "\n");
                e.printStackTrace();
            }
        }

        private long readLongFromBufferedStream() throws IOException {
            byte[] longBytes = new byte[8];  // Long có kích thước 8 byte
            readFully(bufferedIn, longBytes);
            return ByteBuffer.wrap(longBytes)
                    .order(ByteOrder.BIG_ENDIAN) // Đảm bảo sử dụng BIG_ENDIAN
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
                throw new EOFException("Không đọc đủ dữ liệu từ stream.");
            }
        }

        private void handleDownloadRequest(String fileName, DataOutputStream out) {
            try {
                // Đảm bảo thư mục "server_files" tồn tại
                File file = new File("server_files", fileName);

                if (file.exists() && file.isFile()) {
                    // Gửi thông tin file về cho client
                    out.writeUTF("FILE|" + fileName); // Gửi tên file

                    // Gửi kích thước file
                    out.writeLong(file.length());

                    // Gửi dữ liệu file
                    sendFileToClient(file); // Gọi hàm sendFileToClient để gửi file

                    txaserver.append("File " + fileName + " đã được gửi cho client.\n");
                } else {
                    // Nếu không tìm thấy file
                    out.writeUTF("ERROR|File không tồn tại.");
                    txaserver.append("File " + fileName + " không tồn tại trên server.\n");
                }
            } catch (IOException e) {
                txaserver.append("Lỗi khi gửi file: " + e.getMessage() + "\n");
                e.printStackTrace();
            }
        }

        private void sendFileToClient(File file) throws IOException {
            byte[] fileData = Files.readAllBytes(file.toPath());
            dataOut.writeLong(fileData.length);
            byte[] nameBytes = file.getName().getBytes(StandardCharsets.UTF_8);
            dataOut.writeInt(nameBytes.length);
            dataOut.write(nameBytes);
            dataOut.write(fileData);
            dataOut.flush();
            txaserver.append("File đã được gửi thành công: " + file.getName() + "\n");
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
                            client.out.println("FILE|");
                            byte[] nameBytes = fileName.getBytes("UTF-8");
                            client.dataOut.writeInt(nameBytes.length);
                            client.dataOut.write(nameBytes);
                            client.dataOut.flush();
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

    private void stopServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            txaserver.append("Server đã dừng.\n");
        } catch (IOException e) {
            txaserver.append("Lỗi khi dừng server: " + e.getMessage() + "\n");
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
        btnstop = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        txaserver.setColumns(20);
        txaserver.setRows(5);
        jScrollPane1.setViewportView(txaserver);

        btnkhoidong.setBackground(new java.awt.Color(51, 51, 255));
        btnkhoidong.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnkhoidong.setForeground(new java.awt.Color(255, 255, 255));
        btnkhoidong.setText("Start");

        btnstop.setBackground(new java.awt.Color(255, 0, 0));
        btnstop.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnstop.setForeground(new java.awt.Color(255, 255, 255));
        btnstop.setText("Stop");
        btnstop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnstopActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnkhoidong)
                    .addComponent(btnstop))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnkhoidong)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnstop))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnstopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnstopActionPerformed
        stopServer();
    }//GEN-LAST:event_btnstopActionPerformed

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
    private javax.swing.JButton btnstop;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea txaserver;
    // End of variables declaration//GEN-END:variables
}
