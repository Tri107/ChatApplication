/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Form;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import java.io.*;
import java.sql.ResultSet;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
    private DefaultListModel<String> userListModel;
    private JList<String> userList;
    private JTextPane chatPane;
   
    /**
     * Creates new form frmChatApp
     */
    public frmChatApp(String username) {
        initComponents();
        this.username = username;
        txtusername.setText(username);
         
        userListModel = new DefaultListModel<>();
       userList = new JList<>(userListModel);
        JScrollPane userListScrollPane = new JScrollPane(userList);
        getContentPane().add(userListScrollPane, BorderLayout.WEST); 

    // Khởi tạo chatPane và đảm bảo nó không null
    chatPane = new JTextPane();
    chatPane.setEditable(false);  // Không cho phép chỉnh sửa
    
    JScrollPane chatScrollPane = new JScrollPane(chatPane);
    getContentPane().add(chatScrollPane, BorderLayout.CENTER);

    connectToServer();
    connectToDatabase();
    loadMessagesFromDatabase();

    btngui.addActionListener(evt -> sendMessage());
    userList.addListSelectionListener(new ListSelectionListener() {
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {  // Kiểm tra nếu sự kiện đã được xử lý hoàn toàn
            String selectedUser = userList.getSelectedValue();  // Lấy tên người nhận từ JList
            if (selectedUser != null) {
                // Cập nhật tên người nhận vào JTextField
                txtnguoinhan.setText("Người nhận: " + selectedUser);
            } else {
                txtnguoinhan.setText("");  // Nếu không có người nhận nào được chọn, xóa tên
            }
        }
    }
});
 }

    public frmChatApp() {
        initComponents();
        this.username = username;
        txtusername.setText(username);
        
        chatPane = new JTextPane();
       chatPane.setEditable(false);  // Make it non-editable
       JScrollPane chatScrollPane = new JScrollPane(chatPane);
       getContentPane().add(chatScrollPane, BorderLayout.CENTER);
        connectToServer();
         connectToDatabase();
        btngui.addActionListener(evt -> sendMessage());
        userList.addListSelectionListener(new ListSelectionListener() {
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {  // Kiểm tra nếu sự kiện đã được xử lý hoàn toàn
            String selectedUser = userList.getSelectedValue();  // Lấy tên người nhận từ JList
            if (selectedUser != null) {
                // Cập nhật tên người nhận vào JTextField
                txtnguoinhan.setText("Người nhận: " + selectedUser);
            } else {
                txtnguoinhan.setText("");  // Nếu không có người nhận nào được chọn, xóa tên
            }
        }
    }
});
    }
    
   
    

    

   private void loadMessagesForUser(String selectedUser) {
    try {
        chatPane.setText("");  // Clear previous chat
        String sql = "SELECT m.Content, m.CreatedAt, u.Username FROM Messages m " +
                     "JOIN Users u ON m.SenderID = u.UserID WHERE (m.SenderID = ? OR m.ReceiverID = ?) " +
                     "AND (m.ReceiverID = ? OR m.SenderID = ?) ORDER BY m.CreatedAt";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, username);  // Sender ID
        preparedStatement.setString(2, selectedUser); // Receiver ID
        preparedStatement.setString(3, selectedUser);
        preparedStatement.setString(4, username); // Receiver ID
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            String message = resultSet.getString("Content");
            String timestamp = resultSet.getString("CreatedAt");
            String sender = resultSet.getString("Username");
            String fullMessage = String.format("[%s] %s: %s", timestamp, sender, message);
            appendMessage(fullMessage, sender.equals(username));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
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
   private void loadMessagesFromDatabase() {
    try {
        // Sửa tên cột 'MessageContent' thành 'Content'
        String sql = "SELECT m.Content, m.CreatedAt, u.Username " +
                     "FROM Messages m " +
                     "JOIN Users u ON m.SenderID = u.UserID " +
                     "ORDER BY m.CreatedAt";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            String message = resultSet.getString("Content");  // Đổi thành 'Content'
            String timestamp = resultSet.getString("CreatedAt");
            String sender = resultSet.getString("Username");
            String fullMessage = String.format("[%s] %s: %s", timestamp, sender, message);
            appendMessage(fullMessage, false);  // Hiển thị tin nhắn lên giao diện
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

   private void saveMessageToDatabase(String message, String receiverId) {
    try {
        // Sử dụng cột 'Content' thay vì 'MessageContent'
        String sql = "INSERT INTO Messages (SenderID, ReceiverID, Content, Timestamp) VALUES (?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, username);  // Giả sử `username` là ID của người gửi
        preparedStatement.setString(2, receiverId);  // ID của người nhận
        preparedStatement.setString(3, message);     // Nội dung tin nhắn
        preparedStatement.setString(4, getTimestamp());  // Giả sử có phương thức `getTimestamp()` để lấy thời gian
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
        // Khởi tạo DataInputStream và DataOutputStream
        dataIn = new DataInputStream(socket.getInputStream());
        dataOut = new DataOutputStream(socket.getOutputStream());

        Thread receiveThread = new Thread(() -> {
            try {
                String currentMessage;
                while ((currentMessage = in.readLine()) != null) {
                    // Nếu tin nhắn có chứa thông tin của người nhận, xử lý riêng
                    if (currentMessage.startsWith("MESSAGE|")) {
                        String messageContent = currentMessage.substring(8); // Lấy nội dung tin nhắn
                        appendMessage(messageContent, false); // Hiển thị tin nhắn
                    } else {
                        appendMessage("Lỗi: Tin nhắn không hợp lệ.", false);
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
    String message = txtchat.getText().trim();  // Lấy nội dung tin nhắn
    String selectedUser = userList.getSelectedValue();  // Lấy người nhận từ JList

    // Kiểm tra nếu không có người nhận nào được chọn hoặc tin nhắn trống
    if (selectedUser == null || selectedUser.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Vui lòng chọn người nhận.", "Lỗi", JOptionPane.WARNING_MESSAGE);
        return;  // Không gửi tin nhắn nếu không chọn người nhận
    }

    if (message.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Vui lòng nhập tin nhắn.", "Lỗi", JOptionPane.WARNING_MESSAGE);
        return;  // Không gửi tin nhắn nếu tin nhắn rỗng
    }

    // Tiến hành gửi tin nhắn nếu người nhận đã được chọn và tin nhắn không trống
    String receiverId = getReceiverId(selectedUser);  // Lấy ID người nhận từ cơ sở dữ liệu
    if (receiverId != null) {
        String timestamp = getTimestamp();  // Lấy thời gian gửi tin nhắn
        String fullMessage = String.format("[%s] %s: %s", timestamp, username, message);

        // Gửi tin nhắn đến server qua socket
        out.println(fullMessage);

        // Hiển thị tin nhắn trên giao diện người gửi
        appendMessage(fullMessage, true);  // Giả sử appendMessage là phương thức hiển thị tin nhắn

        // Lưu tin nhắn vào cơ sở dữ liệu
        saveMessageToDatabase(fullMessage, receiverId);

        // Xóa nội dung trong hộp nhập tin nhắn
        txtchat.setText("");
    } else {
        JOptionPane.showMessageDialog(this, "Không tìm thấy người nhận.", "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}









   private String getReceiverId(String selectedUser) {
    try {
        String sql = "SELECT UserID FROM Users WHERE Username = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, selectedUser);
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            return resultSet.getString("UserID");  // Trả về UserID của người nhận
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;  // Nếu không tìm thấy người nhận
}



    

    private void receiveImage(String currentMessage) {
        try {
        long imageSize = dataIn.readLong();
        String imageName = dataIn.readUTF();
        byte[] imageData = new byte[(int) imageSize];
        int bytesRead = 0;

        while (bytesRead < imageSize) {
            int result = dataIn.read(imageData, bytesRead, (int) imageSize - bytesRead);
            if (result == -1) break;
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
        StyledDocument doc = chatPane.getStyledDocument();

        // Create a new style for the message
        Style messageStyle = chatPane.addStyle("MessageStyle", null);
        
        
        
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
        chatPane.setCaretPosition(doc.getLength());
        
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

    try (FileInputStream fileInputStream = new FileInputStream(imageFile)) {
        byte[] imageBytes = new byte[(int) imageFile.length()];
        int bytesRead = fileInputStream.read(imageBytes);

        if (bytesRead != imageBytes.length) {
            throw new IOException("Lỗi khi đọc file, không đọc đủ dữ liệu.");
        }

        out.println("IMAGE|" + username);
        dataOut.writeLong(imageBytes.length);
        dataOut.writeUTF(imageFile.getName());
        dataOut.write(imageBytes);
        dataOut.flush();
        
        displayImageInChat(imageBytes, true, username);
    } catch (IOException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Lỗi khi gửi ảnh: " + e.getMessage());
        // Optionally, reset the connection here
        reconnect();
    }
}

    private void displayImageInChat(byte[] imageData, boolean isSender, String sender) {
    try {
        StyledDocument doc = txpboxchat.getStyledDocument();
        Style style = txpboxchat.addStyle("ImageStyle", null);
        
        // Căn lề dựa vào người gửi/nhận
        SimpleAttributeSet align = new SimpleAttributeSet();
        StyleConstants.setAlignment(align, isSender ? StyleConstants.ALIGN_RIGHT : StyleConstants.ALIGN_LEFT);
        doc.setParagraphAttributes(doc.getLength(), 1, align, false);

        // Hiển thị tên người gửi
        String displayName = isSender ? "Bạn" : sender;
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
        // Gọi sendImage khi đã chọn tệp
        sendImage(imageFile);
    }
    }
    private void sendFile(String filePath) {
    try {
        File file = new File(filePath);
        byte[] fileBytes = Files.readAllBytes(file.toPath());

        // Gửi tín hiệu tới server
        out.println("FILE|" + file.getName()); 
        out.flush();

        // Gửi nội dung file
        DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
        dataOut.writeInt(fileBytes.length); // Gửi độ dài file
        dataOut.write(fileBytes); // Gửi nội dung file
        dataOut.flush();

        appendMessage("Bạn đã gửi file: " + file.getName(), true);
    } catch (IOException e) {
        appendMessage("Lỗi khi gửi file: " + e.getMessage(), true);
    }
}
    private void saveReceivedFile(String fileName, byte[] fileData) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn nơi lưu file");
        fileChooser.setSelectedFile(new File(fileName));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(fileData);
                appendMessage("File đã được lưu tại: " + file.getAbsolutePath(), false);
            } catch (IOException e) {
                appendMessage("Lỗi khi lưu file: " + e.getMessage(), false);
            }
        }
    }
    private void receiveFile() {
    try {
            // Đọc kích thước file
            long fileSize = dataIn.readLong();
            // Đọc tên file
            String fileName = dataIn.readUTF();
            
            // Đọc dữ liệu file
            byte[] fileData = new byte[(int) fileSize];
            int bytesRead = 0;
            while (bytesRead < fileSize) {
                int result = dataIn.read(fileData, bytesRead, (int) fileSize - bytesRead);
                if (result == -1) break;
                bytesRead += result;
            }

            // Lưu file
            saveReceivedFile(fileName, fileData);
        } catch (IOException e) {
            appendMessage("Lỗi khi nhận file: " + e.getMessage(), false);
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
        txtusername = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList<>();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtchat = new javax.swing.JTextArea();
        btngui = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        btnimage = new javax.swing.JButton();
        btnfile = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        txtnguoinhan = new javax.swing.JTextField();
        btndownload = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        txpboxchat = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("Icon");

        txtusername.setText("jLabel2");

        jList2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jList2.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Nguyễn Công Vinh\t", "Nguyễn Minh Trí", "Nguyễn Long Vũ" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jList2.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList2ValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jList2);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(txtusername)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
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
                .addComponent(jScrollPane1))
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

        txtnguoinhan.setText("Tên Người Nhận");

        btndownload.setText("download");
        btndownload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btndownloadActionPerformed(evt);
            }
        });

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
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addComponent(btngui, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtnguoinhan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                    .addComponent(txtnguoinhan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                .addGap(0, 20, Short.MAX_VALUE))
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
        // TODO add your handling code here:
    }//GEN-LAST:event_btndownloadActionPerformed

    private void jList2ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList2ValueChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jList2ValueChanged

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
    private javax.swing.JList<String> jList2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextPane txpboxchat;
    private javax.swing.JTextArea txtchat;
    private javax.swing.JTextField txtnguoinhan;
    private javax.swing.JLabel txtusername;
    // End of variables declaration//GEN-END:variables
}
