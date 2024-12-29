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
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;

/**
 *
 * @author DELL
 */
public class frmChatApp extends javax.swing.JFrame {

    private String username;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private File imageFile;
    private Connection connection;
    public static final String FILE_DIRECTORY = "files";

    /**
     * Creates new form frmChatApp
     */
    public frmChatApp(String username) {
        initComponents();
        this.username = username;
        txtusername.setText(username);

        connectToServer();
        connectToDatabase();
         hashAndUpdatePasswords();
        loadMessagesFromDatabase();
        btngui.addActionListener(evt -> sendMessage());
    }

    public frmChatApp() {
        initComponents();
        this.username = username;
        txtusername.setText(username);

        connectToServer();
        connectToDatabase();
        
        btngui.addActionListener(evt -> sendMessage());
    }

    private void connectToDatabase() {
        try {
            // Thông tin kết nối tới MySQL
            String url = "jdbc:mysql://localhost:3306/chatapp";  // Đổi với tên cơ sở dữ liệu của bạn
            String user = "root";  // Đổi với tên người dùng MySQL của bạn
            String password = "";  // Đổi với mật khẩu MySQL của bạn
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Thuật toán mã hóa không khả dụng!", e);
        }
    }

    private void hashAndUpdatePasswords() {
        if (connection == null) {
            return;
        }
        try (PreparedStatement selectStatement = connection.prepareStatement(
                "SELECT UserID, PasswordHash FROM Users"); PreparedStatement updateStatement = connection.prepareStatement(
                        "UPDATE Users SET PasswordHash = ? WHERE UserID = ?")) {

            ResultSet resultSet = selectStatement.executeQuery();

            while (resultSet.next()) {
                int userId = resultSet.getInt("UserID");
                String passwordHash = resultSet.getString("PasswordHash");

                // Kiểm tra xem mật khẩu đã được mã hóa chưa
                if (passwordHash != null && passwordHash.length() == 32) { // MD5 có 32 ký tự
                    continue; // Đã mã hóa, bỏ qua
                }

                // Mã hóa lại mật khẩu
                String hashedPassword = hashPassword(passwordHash);

                // Cập nhật vào cơ sở dữ liệu
                updateStatement.setString(1, hashedPassword);
                updateStatement.setInt(2, userId);
                updateStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void registerUser(String username, String password) {
        String hashedPassword = hashPassword(password);
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO Users (Username, PasswordHash) VALUES (?, ?)")) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, hashedPassword);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không thể đăng ký người dùng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean loginUser(String username, String password) {
        String hashedPassword = hashPassword(password);
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT * FROM Users WHERE Username = ? AND PasswordHash = ?")) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, hashedPassword);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next(); // Nếu có kết quả, đăng nhập thành công
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không thể đăng nhập!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void loadMessagesFromDatabase() {
        try {
            String sql = "SELECT m.Content, m.CreatedAt, u.Username "
                    + "FROM Messages m "
                    + "JOIN Users u ON m.SenderID = u.UserID "
                    + "ORDER BY m.CreatedAt";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String message = resultSet.getString("Content");
                String timestamp = resultSet.getString("CreatedAt");
                String sender = resultSet.getString("Username");
                String fullMessage = String.format("[%s] %s: %s", timestamp, sender, message);
                appendMessage(fullMessage, false);  // Hiển thị tin nhắn lên giao diện
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveMessageToDatabase(String message) {
        try {
            // Giả sử bạn lấy ID của người gửi và người nhận từ đâu đó
            // Ví dụ: Người gửi và người nhận là các ID tĩnh cho đơn giản
            int senderID = 1;  // Thay thế bằng ID người gửi thực tế
            int receiverID = 2;  // Thay thế bằng ID người nhận thực tế

            // Câu lệnh SQL để chèn tin nhắn vào cơ sở dữ liệu
            String sql = "INSERT INTO Messages (SenderID, ReceiverID, Content) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, senderID);  // Gán ID người gửi
            preparedStatement.setInt(2, receiverID);  // Gán ID người nhận
            preparedStatement.setString(3, message);  // Gán nội dung tin nhắn

            // Thực thi câu lệnh SQL
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 8386);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            dataIn = new DataInputStream(socket.getInputStream());
            dataOut = new DataOutputStream(socket.getOutputStream());

            Thread receiveThread = new Thread(() -> {
                try {
                    String currentMessage;
                    while ((currentMessage = in.readLine()) != null) {
                        if (currentMessage.startsWith("IMAGE|")) {
                            receiveImage(currentMessage);
                        } else if (currentMessage.startsWith("FILE|")) {
                            receiveFile();
                        } else {
                            appendMessage(currentMessage, false);
                        }
                    }
                } catch (IOException e) {
                    appendMessage("Lỗi khi nhận tin nhắn: " + e.getMessage(), false);
                }
            });
            receiveThread.start();
        } catch (IOException e) {
            appendMessage("Không thể kết nối tới server: " + e.getMessage(), false);
        }

    }

    private void sendMessage() {
        String message = txtchat.getText().trim();
        if (!message.isEmpty()) {
            String timestamp = getTimestamp();
            String fullMessage = String.format("[%s] %s: %s", timestamp, username, message);
            out.println(fullMessage);  // Gửi tin nhắn qua socket
            appendMessage(fullMessage, true);  // Hiển thị tin nhắn lên giao diện

            // Lưu tin nhắn vào cơ sở dữ liệu
            saveMessageToDatabase(fullMessage);

            txtchat.setText("");  // Xóa hộp thoại nhập tin nhắn
        }
    }

    private void receiveImage(String currentMessage) {
        try {
            long imageSize = dataIn.readLong(); // Đọc kích thước file
            int nameLength = dataIn.readInt(); // Đọc độ dài tên file
            byte[] nameBytes = new byte[nameLength];
            dataIn.readFully(nameBytes); // Đọc tên file
            String imageName = new String(nameBytes, "UTF-8");

            byte[] imageData = new byte[(int) imageSize];
            int bytesRead = 0;

            while (bytesRead < imageSize) {
                int result = dataIn.read(imageData, bytesRead, (int) imageSize - bytesRead);
                if (result == -1) {
                    break;
                }
                bytesRead += result;
            }

            if (bytesRead != imageSize) {
                appendMessage("Lỗi: Dữ liệu ảnh không đầy đủ.", false);
                return;
            }

            String senderName = currentMessage.substring(6);
            displayImageInChat(imageData, false, senderName);
        } catch (IOException e) {
            appendMessage("Lỗi khi nhận ảnh: " + e.getMessage(), false);
        }
    }

    private void appendMessage(String message, boolean isSender) {
        try {
            StyledDocument doc = txpboxchat.getStyledDocument();

            // Create a new style for the message
            Style messageStyle = txpboxchat.addStyle("MessageStyle", null);

            // Set alignment based on sender/receiver
            SimpleAttributeSet alignment = new SimpleAttributeSet();
            if (isSender) {
                StyleConstants.setAlignment(alignment, StyleConstants.ALIGN_RIGHT);
                StyleConstants.setForeground(messageStyle, new Color(0, 102, 204));  // Dark blue for sender
                StyleConstants.setBackground(messageStyle, new Color(232, 240, 254)); // Light blue background
            } else {
                StyleConstants.setAlignment(alignment, StyleConstants.ALIGN_LEFT);
                StyleConstants.setForeground(messageStyle, new Color(51, 51, 51));   // Dark gray for receiver
                StyleConstants.setBackground(messageStyle, new Color(241, 241, 241)); // Light gray background
            }

            // Create padding and margin
            StyleConstants.setSpaceAbove(messageStyle, 5.0f);
            StyleConstants.setSpaceBelow(messageStyle, 5.0f);
            StyleConstants.setLeftIndent(messageStyle, isSender ? 100.0f : 10.0f);
            StyleConstants.setRightIndent(messageStyle, isSender ? 10.0f : 100.0f);
            StyleConstants.setFontFamily(messageStyle, "Segoe UI");
            StyleConstants.setFontSize(messageStyle, 14);

            // Apply alignment to the paragraph
            doc.setParagraphAttributes(doc.getLength(), 1, alignment, false);

            // Insert message with style
            doc.insertString(doc.getLength(), message + "\n", messageStyle);

            // Scroll to the bottom
            txpboxchat.setCaretPosition(doc.getLength());

        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(new Date());
    }

    private void sendImage(File imageFile) {
        if (imageFile == null || !imageFile.exists() || imageFile.length() == 0) {
            JOptionPane.showMessageDialog(this, "File không tồn tại hoặc rỗng.");
            return;
        }

        try (FileInputStream fileInputStream = new FileInputStream(imageFile)) { // Tự động đóng luồng
            byte[] imageBytes = new byte[(int) imageFile.length()];
            int bytesRead = fileInputStream.read(imageBytes);

            if (bytesRead != imageBytes.length) {
                throw new IOException("Lỗi khi đọc file, không đọc đủ dữ liệu.");
            }

            out.println("IMAGE|" + username);
            dataOut.writeLong(imageBytes.length); // Gửi kích thước file
            byte[] nameBytes = imageFile.getName().getBytes("UTF-8");
            dataOut.writeInt(nameBytes.length); // Gửi độ dài tên file
            dataOut.write(nameBytes); // Gửi tên file
            dataOut.write(imageBytes); // Gửi nội dung file
            dataOut.flush();

            displayImageInChat(imageBytes, true, username);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi gửi ảnh: " + e.getMessage());
            reconnect();
        }
    }

    private void displayImageInChat(byte[] imageData, boolean isSender, String sender) {
        try {
            StyledDocument doc = txpboxchat.getStyledDocument();
            Style style = txpboxchat.addStyle("ImageStyle", null);
            SimpleAttributeSet align = new SimpleAttributeSet();
            StyleConstants.setAlignment(align, isSender ? StyleConstants.ALIGN_RIGHT : StyleConstants.ALIGN_LEFT);
            doc.setParagraphAttributes(doc.getLength(), 1, align, false);
            String displayName = isSender ? username : sender;
            StyleConstants.setForeground(style, isSender ? Color.BLUE : Color.BLACK);
            StyleConstants.setBold(style, true);
            doc.insertString(doc.getLength(), displayName + ":\n", style);

            // Reset style cho ảnh
            style = txpboxchat.addStyle("ImageStyle", null);

            // Tạo và resize ảnh
            ImageIcon originalIcon = new ImageIcon(imageData);
            Image originalImage = originalIcon.getImage();

            // Tính toán kích thước mới giữ tỷ lệ
            int maxWidth = 300;
            int maxHeight = 300;

            double scale = Math.min(
                    (double) maxWidth / originalImage.getWidth(null),
                    (double) maxHeight / originalImage.getHeight(null)
            );

            int newWidth = (int) (originalImage.getWidth(null) * scale);
            int newHeight = (int) (originalImage.getHeight(null) * scale);

            Image resizedImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            ImageIcon resizedIcon = new ImageIcon(resizedImage);

            // Chèn ảnh
            txpboxchat.setCaretPosition(doc.getLength());
            txpboxchat.insertIcon(resizedIcon);
            doc.insertString(doc.getLength(), "\n\n", style);

            // Cuộn xuống cuối
            txpboxchat.setCaretPosition(doc.getLength());
        } catch (Exception e) {
            e.printStackTrace();
            appendMessage("Lỗi khi hiển thị ảnh: " + e.getMessage(), false);
        }
    }

    private void chooseAndSendImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "gif"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            imageFile = fileChooser.getSelectedFile();
            sendImage(imageFile);
        }
    }

    private void sendFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                appendMessage("File không tồn tại hoặc không hợp lệ.", true);
                return;
            }
            if (file.length() == 0) {
                JOptionPane.showMessageDialog(this, "File không hợp lệ.");
                return;
            }

            // Đọc dữ liệu file
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());

            // Gửi tên file
            bos.write((file.getName() + "\n").getBytes(StandardCharsets.UTF_8));

            // Gửi dữ liệu file
            bos.write(fileBytes);
            bos.flush();

            // Chỉ hiển thị thông báo đã gửi file
            appendMessage(username + file.getName(), true);
        } catch (IOException e) {
            appendMessage("Lỗi khi gửi file: " + e.getMessage(), true);
        }
    }

    private void receiveFile() {
        try {
            int nameLength = dataIn.readInt();
            byte[] nameBytes = new byte[nameLength];
            dataIn.readFully(nameBytes);
            String fileName = new String(nameBytes, "UTF-8");

            long fileSize = dataIn.readLong();
            byte[] fileData = new byte[(int) fileSize];
            dataIn.readFully(fileData);

            // Tạo và kiểm tra thư mục files
            File filesDir = new File(FILE_DIRECTORY);
            if (!filesDir.exists()) {
                filesDir.mkdir();
            }

            // Lưu file vào thư mục files
            File saveFile = new File(filesDir, fileName);
            Files.write(saveFile.toPath(), fileData);

            displayFileInChat(null, false, username, fileName);
        } catch (IOException e) {
            appendMessage("Lỗi khi nhận file: " + e.getMessage(), false);
        }
    }

    private void displayFileInChat(byte[] fileData, boolean isSender, String sender, String fileName) {
        try {
            StyledDocument doc = txpboxchat.getStyledDocument();
            Style style = txpboxchat.addStyle("FileStyle", null);
            SimpleAttributeSet align = new SimpleAttributeSet();
            StyleConstants.setAlignment(align, isSender ? StyleConstants.ALIGN_RIGHT : StyleConstants.ALIGN_LEFT);
            doc.setParagraphAttributes(doc.getLength(), 1, align, false);

            // Chỉ hiển thị tên người gửi và tên file
            if (isSender) {
                doc.insertString(doc.getLength(), username + fileName + "\n", style);
            } else {
                doc.insertString(doc.getLength(), sender + username + fileName + "\n", style);
            }

            // Tạo liên kết mở file
            // String filePath = "C:\\Users\\DELL\\OneDrive\\Pictures\\Project Chat\\" + fileName;
            //doc.insertString(doc.getLength(), "[Mở file]", style);
            //doc.setCharacterAttributes(doc.getLength() - 8, 8, getLinkStyle(filePath), false);
            doc.insertString(doc.getLength(), "\n\n", style);

            // Cuộn xuống cuối
            txpboxchat.setCaretPosition(doc.getLength());
        } catch (Exception e) {
            e.printStackTrace();
            appendMessage("Lỗi khi hiển thị file: " + e.getMessage(), false);
        }
    }

    private void requestFileFromServer(String fileName, File destinationFile) {
        try {
            out.println("DOWNLOAD|" + fileName); // Gửi yêu cầu tải file

            // Đọc phản hồi từ server
            int responseCode = dataIn.readInt(); // Server gửi mã phản hồi (1: thành công, 0: thất bại)
            if (responseCode == 0) {
                JOptionPane.showMessageDialog(this, "File không tồn tại trên server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Đọc tên file
            int nameLength = dataIn.readInt();
            byte[] nameBytes = new byte[nameLength];
            dataIn.readFully(nameBytes);
            String serverFileName = new String(nameBytes, "UTF-8");

            // Đọc dữ liệu file
            long fileSize = dataIn.readLong();
            byte[] fileData = new byte[(int) fileSize];
            dataIn.readFully(fileData);

            // Lưu file vào thư mục đã chọn
            Files.write(destinationFile.toPath(), fileData);

            JOptionPane.showMessageDialog(this, "Đã tải file thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải file từ server: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void requestFileDownload(String fileName) {
        try {
            // Gửi yêu cầu tải file tới server
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("DOWNLOAD|" + fileName);  // Gửi yêu cầu tải file

            // Nhận phản hồi từ server
            DataInputStream in = new DataInputStream(socket.getInputStream());
            String response = in.readUTF();

            if (response.startsWith("FILE|")) {
                // Server đồng ý gửi file
                String fileNameFromServer = response.substring(5);
                long fileSize = in.readLong();

                // Tạo file để lưu dữ liệu nhận được
                File file = new File("downloaded_" + fileNameFromServer);
                try (BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(file))) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long remainingBytes = fileSize;

                    while ((bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, remainingBytes))) != -1) {
                        fileOut.write(buffer, 0, bytesRead);
                        remainingBytes -= bytesRead;
                        if (remainingBytes == 0) {
                            break;
                        }
                    }
                    fileOut.flush();
                    JOptionPane.showMessageDialog(this, "Tải file " + fileNameFromServer + " thành công.");
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi lưu file: " + e.getMessage());
                }
            } else {
                // Server gửi lỗi, ví dụ file không tồn tại
                JOptionPane.showMessageDialog(this, "Lỗi: " + response.substring(6));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối khi tải file: " + e.getMessage());
        }
    }

    private void copyFile(File source, File destination) throws IOException {
        try (FileInputStream fileIn = new FileInputStream(source); FileOutputStream fileOut = new FileOutputStream(destination)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileIn.read(buffer)) != -1) {
                fileOut.write(buffer, 0, bytesRead);
            }
        }
    }

    private void reconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            connectToServer();
        } catch (IOException e) {
            appendMessage("Không thể kết nối lại với server: " + e.getMessage(), false);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtchat = new javax.swing.JTextArea();
        btngui = new javax.swing.JButton();
        btnimage = new javax.swing.JButton();
        btnfile = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        txpboxchat = new javax.swing.JTextPane();
        btndownload = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        CHÂTPPLICATION = new javax.swing.JLabel();
        txtusername = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(204, 255, 255));
        jPanel2.setForeground(new java.awt.Color(255, 255, 255));

        txtchat.setColumns(20);
        txtchat.setRows(5);
        jScrollPane2.setViewportView(txtchat);

        btngui.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/message.png"))); // NOI18N

        btnimage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/image.png"))); // NOI18N
        btnimage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnimageActionPerformed(evt);
            }
        });

        btnfile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/document.png"))); // NOI18N
        btnfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnfileActionPerformed(evt);
            }
        });

        jScrollPane4.setViewportView(txpboxchat);

        btndownload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/downlaod.png"))); // NOI18N
        btndownload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btndownloadActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/user.png"))); // NOI18N

        CHÂTPPLICATION.setFont(new java.awt.Font("UTM Aircona", 1, 18)); // NOI18N
        CHÂTPPLICATION.setText("CHAT APPLICATION");

        txtusername.setFont(new java.awt.Font("UTM Aircona", 0, 18)); // NOI18N
        txtusername.setText("username");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 741, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btngui, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnimage, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnfile, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btndownload)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(CHÂTPPLICATION)
                            .addComponent(txtusername, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(CHÂTPPLICATION)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtusername)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 456, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnimage)
                        .addComponent(btnfile))
                    .addComponent(btndownload, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btngui, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(0, 8, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnfileActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file để gửi");
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            sendFile(selectedFile.getAbsolutePath());
        }
    }//GEN-LAST:event_btnfileActionPerformed

    private void btnimageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnimageActionPerformed
        chooseAndSendImage();
    }//GEN-LAST:event_btnimageActionPerformed

    private void btndownloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btndownloadActionPerformed
        String fileName = JOptionPane.showInputDialog(this, "Nhập tên file cần tải:", "Tải file", JOptionPane.QUESTION_MESSAGE);

        // Kiểm tra nếu người dùng nhập tên file
        if (fileName != null && !fileName.trim().isEmpty()) {
            // Đường dẫn thư mục lưu file trên server
            String serverFilesPath = "C:\\server_files";
            File sourceFile = new File(serverFilesPath, fileName.trim());

            // Kiểm tra file có tồn tại trên server không
            if (!sourceFile.exists()) {
                JOptionPane.showMessageDialog(this, "File không tồn tại trong thư mục server_files!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Hiển thị hộp thoại để người dùng chọn nơi lưu file
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Chọn nơi lưu file");
            fileChooser.setSelectedFile(new File(fileName.trim()));

            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File destinationFile = fileChooser.getSelectedFile();

                try {
                    // Copy file từ server_files sang nơi người dùng chọn
                    copyFile(sourceFile, destinationFile);
                    JOptionPane.showMessageDialog(this, "File đã được tải về: " + destinationFile.getAbsolutePath(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi tải file: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        }
    }//GEN-LAST:event_btndownloadActionPerformed

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
    private javax.swing.JLabel CHÂTPPLICATION;
    private javax.swing.JButton btndownload;
    private javax.swing.JButton btnfile;
    private javax.swing.JButton btngui;
    private javax.swing.JButton btnimage;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextPane txpboxchat;
    private javax.swing.JTextArea txtchat;
    private javax.swing.JLabel txtusername;
    // End of variables declaration//GEN-END:variables
}
