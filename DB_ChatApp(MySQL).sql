-- Tạo cơ sở dữ liệu
CREATE DATABASE ChatApp;
USE ChatApp;

-- Bảng lưu thông tin người dùng
CREATE TABLE Users (
    UserID INT AUTO_INCREMENT PRIMARY KEY,
    DisplayName VARCHAR(100) NOT NULL,
    Username VARCHAR(50) NOT NULL UNIQUE,
    PasswordHash VARCHAR(255) NOT NULL,
    Email VARCHAR(100) NOT NULL UNIQUE,
    IsVerified BOOLEAN NOT NULL DEFAULT FALSE, -- Trạng thái xác minh email
    CreatedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Bảng lưu OTP để xác minh email
CREATE TABLE OTPs (
    OTPID INT AUTO_INCREMENT PRIMARY KEY,
    UserID INT NOT NULL,
    OTPCode VARCHAR(10) NOT NULL,
    ExpiryTime DATETIME NOT NULL,
    IsUsed BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (UserID) REFERENCES Users(UserID)
);

-- Bảng danh sách bạn bè
CREATE TABLE Friends (
    FriendID INT AUTO_INCREMENT PRIMARY KEY,
    UserID INT NOT NULL,
    FriendUserID INT NOT NULL,
    Status VARCHAR(20) NOT NULL DEFAULT 'Pending', -- Pending, Accepted, Blocked
    CreatedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (UserID, FriendUserID),
    FOREIGN KEY (UserID) REFERENCES Users(UserID),
    FOREIGN KEY (FriendUserID) REFERENCES Users(UserID)
);

-- Bảng lưu tin nhắn
CREATE TABLE Messages (
    MessageID INT AUTO_INCREMENT PRIMARY KEY,
    SenderID INT NOT NULL,
    ReceiverID INT NOT NULL,
    Content TEXT,
    FilePath VARCHAR(255), -- Đường dẫn tệp hoặc hình ảnh
    CreatedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (SenderID) REFERENCES Users(UserID),
    FOREIGN KEY (ReceiverID) REFERENCES Users(UserID)
);

-- Bảng lưu thông tin nhóm chat
CREATE TABLE ChatGroups (
    GroupID INT AUTO_INCREMENT PRIMARY KEY,
    GroupName VARCHAR(100) NOT NULL,
    CreatedBy INT NOT NULL,
    CreatedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (CreatedBy) REFERENCES Users(UserID)
);

-- Bảng lưu thành viên nhóm
CREATE TABLE GroupMembers (
    GroupMemberID INT AUTO_INCREMENT PRIMARY KEY,
    GroupID INT NOT NULL,
    UserID INT NOT NULL,
    JoinedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (GroupID) REFERENCES ChatGroups(GroupID),
    FOREIGN KEY (UserID) REFERENCES Users(UserID)
);

-- Bảng lưu tin nhắn nhóm
CREATE TABLE GroupMessages (
    GroupMessageID INT AUTO_INCREMENT PRIMARY KEY,
    GroupID INT NOT NULL,
    SenderID INT NOT NULL,
    Content TEXT,
    FilePath VARCHAR(255), -- Đường dẫn tệp hoặc hình ảnh
    CreatedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (GroupID) REFERENCES ChatGroups(GroupID),
    FOREIGN KEY (SenderID) REFERENCES Users(UserID)
);

-- Thêm dữ liệu mẫu vào bảng Users
INSERT INTO Users (DisplayName, Username, PasswordHash, Email, IsVerified) 
VALUES 
('Nguyen Minh Tri', 'tri', '123', 'test123@gmail.com', TRUE),
('Nguyen Cong Vinh', 'vinh', '456', 'test456@gmail.com', TRUE),
('Nguyen Long Vu', 'vu', '789', 'test789@gmail.com', TRUE);

-- Xem thông tin người dùng
SELECT * FROM Users;

-- Kiểm tra đăng nhập
SELECT * FROM Users
WHERE Username = 'tri' AND PasswordHash = '123';
