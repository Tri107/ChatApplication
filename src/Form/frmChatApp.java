/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Form;

import java.awt.Color;
import java.awt.Image;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 *
 * @author DELL
 */
public class frmChatApp extends javax.swing.JFrame {

    private String username;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    /**
     * Creates new form frmChatApp
     */
    public frmChatApp(String username) {
        initComponents();
        this.username = username;
        txtusername.setText(username);

        connectToServer();
        btngui.addActionListener(evt -> sendMessage());
    }

    public frmChatApp() {
        initComponents();
        this.username = username;
        txtusername.setText(username);

        connectToServer();
        btngui.addActionListener(evt -> sendMessage());
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 8386);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Thread receiveThread = new Thread(() -> {
    try {
        String message;
        while ((message = in.readLine()) != null) {
            if (message.startsWith("IMAGE|")) {
                String imageName = message.substring(6); // Loại bỏ "IMAGE|"
                receiveImage(imageName); // Gọi hàm nhận ảnh
            } else {
                appendMessage(message, false); // Truyền false vì đây là tin nhắn từ đối phương
            }
        }
    } catch (IOException e) {
        appendMessage("Lỗi nhận tin nhắn: " + e.getMessage(), false); // Sửa lại để truyền tham số thứ hai
    }
});
receiveThread.start();
        } catch (IOException e) {
            appendMessage("Không thể kết nối tới server: " + e.getMessage(),false);
        }
    }

    private void sendMessage() {
        String message = txtchat.getText().trim();
        if (!message.isEmpty()) {
            out.println(username + ": " + message);
            appendMessage(username + ": " + message, true); // Hiển thị tin nhắn của người gửi
            txtchat.setText(""); // Xóa ô nhập
        }
    }

    private void receiveImage(String imageName) {
        try {
            DataInputStream dataIn = new DataInputStream(socket.getInputStream());
            int imageLength = dataIn.readInt();
            byte[] imageBytes = new byte[imageLength];
            dataIn.readFully(imageBytes);  // Đọc toàn bộ ảnh từ server
            displayImageInChat(imageBytes, false); // Hiển thị ảnh trong chat
        } catch (IOException e) {
            appendMessage("Lỗi khi nhận ảnh: " + e.getMessage(), false);
        }
    }

    private void appendMessage(String message, boolean isSender) {
        try {
            // Lấy StyledDocument từ JTextPane
            StyledDocument doc = txpboxchat.getStyledDocument();

            // Tạo style mới cho văn bản
            Style style = txpboxchat.addStyle("Default", null);
            if (isSender) {
                // Tin nhắn của người gửi sẽ nằm bên phải
                StyleConstants.setAlignment(style, StyleConstants.ALIGN_RIGHT);
                StyleConstants.setForeground(style, Color.BLUE); // Màu xanh cho người gửi
            } else {
                // Tin nhắn của đối phương sẽ nằm bên trái
                StyleConstants.setAlignment(style, StyleConstants.ALIGN_LEFT);
                StyleConstants.setForeground(style, Color.BLACK); // Màu đen cho đối phương
            }

            // Chèn văn bản vào JTextPane
            doc.insertString(doc.getLength(), message + "\n", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void sendImage(String filePath) {
        try {
            File imageFile = new File(filePath);
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());

            out.println("IMAGE|");  // Gửi tín hiệu ảnh
            out.flush();

            DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
            dataOut.writeInt(imageBytes.length);  // Gửi kích thước ảnh
            dataOut.write(imageBytes);  // Gửi ảnh
            dataOut.flush();  // Đảm bảo gửi ảnh hoàn tất
        } catch (IOException e) {
            e.printStackTrace();
            appendMessage("Lỗi khi gửi ảnh: " + e.getMessage(), true);
        }
    }

    private void displayImageInChat(byte[] imageBytes, boolean isSender) {
        try {
            ImageIcon imageIcon = new ImageIcon(imageBytes);
            Image image = imageIcon.getImage();
            Image resizedImage = image.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            imageIcon = new ImageIcon(resizedImage);

            StyledDocument doc = txpboxchat.getStyledDocument();
            Style style = txpboxchat.addStyle("Default", null);

            if (isSender) {
                StyleConstants.setAlignment(style, StyleConstants.ALIGN_RIGHT);
            } else {
                StyleConstants.setAlignment(style, StyleConstants.ALIGN_LEFT);
            }

            doc.insertString(doc.getLength(), "\n", style);
            txpboxchat.insertIcon(imageIcon);
        } catch (Exception e) {
            e.printStackTrace();
            appendMessage("Lỗi khi hiển thị ảnh: " + e.getMessage(), false);
        }
    }

    private void chooseAndSendImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn hình ảnh");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            sendImage(filePath);

            try {
                byte[] imageBytes = Files.readAllBytes(selectedFile.toPath());
                displayImageInChat(imageBytes, true);  // Hiển thị ảnh đã gửi
            } catch (IOException e) {
                e.printStackTrace();
            }
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

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        txtusername = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtchat = new javax.swing.JTextArea();
        btngui = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        btnimage = new javax.swing.JButton();
        btnfile = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        btndownload = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        txpboxchat = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("Icon");

        jList1.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jList1.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "A", "B", "C", "D", "E" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane3.setViewportView(jList1);

        txtusername.setText("jLabel2");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(txtusername)
                .addContainerGap(150, Short.MAX_VALUE))
            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(txtusername)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 556, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        txtchat.setColumns(20);
        txtchat.setRows(5);
        jScrollPane2.setViewportView(txtchat);

        btngui.setText("Send");

        jButton2.setText("Icons");

        btnimage.setText("Images");
        btnimage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnimageActionPerformed(evt);
            }
        });

        btnfile.setText("Files");
        btnfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnfileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 53, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 48, Short.MAX_VALUE)
        );

        jTextField1.setText("Tên Người Nhận");

        btndownload.setText("download");

        jScrollPane4.setViewportView(txpboxchat);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addComponent(btngui, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(jButton2)
                                        .addGap(18, 18, 18)
                                        .addComponent(btnimage)
                                        .addGap(18, 18, 18)
                                        .addComponent(btnfile)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(btndownload)))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 479, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(btnimage)
                    .addComponent(btnfile)
                    .addComponent(btndownload))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btngui, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnfileActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file để gửi");
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            appendMessage("Đã chọn file: " + selectedFile.getName(), true);
        }
    }//GEN-LAST:event_btnfileActionPerformed

    private void btnimageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnimageActionPerformed
        chooseAndSendImage();
    }//GEN-LAST:event_btnimageActionPerformed

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
            java.util.logging.Logger.getLogger(frmChatApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(frmChatApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(frmChatApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(frmChatApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new frmChatApp().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btndownload;
    private javax.swing.JButton btnfile;
    private javax.swing.JButton btngui;
    private javax.swing.JButton btnimage;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList<String> jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextPane txpboxchat;
    private javax.swing.JTextArea txtchat;
    private javax.swing.JLabel txtusername;
    // End of variables declaration//GEN-END:variables
}
