/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package chatapp;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

/**
 *
 * @author admin
 */
public class ChatApplication {
     public static void main(String[] args) {
        JFrame frame = new JFrame("Chat Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // Panel bên trái chứa danh sách tên
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        // Danh sách tên
        String[] names = {"Nguyễn Công Vinh", "Nguyễn Minh Trí", "Nguyễn Long Vũ", "D", "E"};

        // Lưu trữ dữ liệu tin nhắn
        HashMap<String, String> messages = new HashMap<>();
        messages.put("Nguyễn Công Vinh", "Cuộc trò chuyện giữa bạn và Nguyễn Công Vinh:\n- Xin chào!\n- Hôm nay thế nào?");
        messages.put("Nguyễn Minh Trí", "");
        messages.put("Nguyễn Long Vũ", "");
        messages.put("D", "");
        messages.put("E", "");

        // Tạo khung hiển thị tin nhắn
        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        // Thêm các JLabel vào danh sách bên trái
        for (String name : names) {
            JLabel nameLabel = new JLabel(name);
            nameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            nameLabel.setOpaque(true);
            nameLabel.setBackground(Color.LIGHT_GRAY);
            nameLabel.setPreferredSize(new Dimension(200, 30));
            nameLabel.setHorizontalAlignment(SwingConstants.LEFT);

            // Thêm sự kiện khi nhấn vào tên
            nameLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Nếu là "Nguyễn Công Vinh", hiển thị tin nhắn
                    if (name.equals("Nguyễn Công Vinh")) {
                        chatArea.setText(messages.get(name));
                    } else {
                        chatArea.setText(""); // Xóa nội dung nếu không phải "Nguyễn Công Vinh"
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    nameLabel.setBackground(Color.GRAY);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    nameLabel.setBackground(Color.LIGHT_GRAY);
                }
            });

            leftPanel.add(nameLabel);
        }

        // Panel chính để chia giao diện
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(leftPanel), new JScrollPane(chatArea));
        splitPane.setDividerLocation(250);

        frame.add(splitPane);
        frame.setVisible(true);
    }
}
