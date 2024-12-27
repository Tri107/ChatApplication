-- Tạo cơ sở dữ liệu
CREATE DATABASE ChatApp;
GO

USE ChatApp;
GO

-- Bảng lưu thông tin người dùng
CREATE TABLE Users (
    UserID INT IDENTITY(1,1) PRIMARY KEY,
    DisplayName NVARCHAR(100) NOT NULL,
    Username NVARCHAR(50) NOT NULL UNIQUE,
    PasswordHash NVARCHAR(255) NOT NULL,
    Email NVARCHAR(100) NOT NULL UNIQUE,
    IsVerified BIT NOT NULL DEFAULT 0, -- Trạng thái xác minh email
    CreatedAt DATETIME NOT NULL DEFAULT GETDATE()
);

-- Bảng lưu OTP để xác minh email
CREATE TABLE OTPs (
    OTPID INT IDENTITY(1,1) PRIMARY KEY,
    UserID INT NOT NULL FOREIGN KEY REFERENCES Users(UserID),
    OTPCode NVARCHAR(10) NOT NULL,
    ExpiryTime DATETIME NOT NULL,
    IsUsed BIT NOT NULL DEFAULT 0
);

-- Bảng danh sách bạn bè
CREATE TABLE Friends (
    FriendID INT IDENTITY(1,1) PRIMARY KEY,
    UserID INT NOT NULL FOREIGN KEY REFERENCES Users(UserID),
    FriendUserID INT NOT NULL,
    Status NVARCHAR(20) NOT NULL DEFAULT 'Pending', -- Pending, Accepted, Blocked
    CreatedAt DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_Friends UNIQUE (UserID, FriendUserID)
);

-- Bảng lưu tin nhắn
CREATE TABLE Messages (
    MessageID INT IDENTITY(1,1) PRIMARY KEY,
    SenderID INT NOT NULL FOREIGN KEY REFERENCES Users(UserID),
    ReceiverID INT NOT NULL FOREIGN KEY REFERENCES Users(UserID),
    Content NVARCHAR(MAX),
    FilePath NVARCHAR(255), -- Đường dẫn tệp hoặc hình ảnh
    CreatedAt DATETIME NOT NULL DEFAULT GETDATE()
);

-- Bảng lưu thông tin nhóm chat
CREATE TABLE ChatGroups (
    GroupID INT IDENTITY(1,1) PRIMARY KEY,
    GroupName NVARCHAR(100) NOT NULL,
    CreatedBy INT NOT NULL FOREIGN KEY REFERENCES Users(UserID),
    CreatedAt DATETIME NOT NULL DEFAULT GETDATE()
);

-- Bảng lưu thành viên nhóm
CREATE TABLE GroupMembers (
    GroupMemberID INT IDENTITY(1,1) PRIMARY KEY,
    GroupID INT NOT NULL FOREIGN KEY REFERENCES ChatGroups(GroupID),
    UserID INT NOT NULL FOREIGN KEY REFERENCES Users(UserID),
    JoinedAt DATETIME NOT NULL DEFAULT GETDATE()
);

-- Bảng lưu tin nhắn nhóm
CREATE TABLE GroupMessages (
    GroupMessageID INT IDENTITY(1,1) PRIMARY KEY,
    GroupID INT NOT NULL FOREIGN KEY REFERENCES ChatGroups(GroupID),
    SenderID INT NOT NULL FOREIGN KEY REFERENCES Users(UserID),
    Content NVARCHAR(MAX),
    FilePath NVARCHAR(255), -- Đường dẫn tệp hoặc hình ảnh
    CreatedAt DATETIME NOT NULL DEFAULT GETDATE()
);
----

INSERT INTO Users (DisplayName, Username, PasswordHash, Email, IsVerified) 
VALUES 
('Nguyen Minh Tri', 'tri', '123', 'test123@gmail.com', 1),
('Nguyen Cong Vinh', 'vinh', '456', 'test456@gmail.com', 1),
('Nguyen Long Vu', 'vu', '789', 'test789@gmail.com', 1);
select * from Users