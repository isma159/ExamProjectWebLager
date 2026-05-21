CREATE TABLE Clients
(
    clientId   INT IDENTITY CONSTRAINT PK_Clients PRIMARY KEY,
    clientName VARCHAR(100) NOT NULL,
    deleted_at DATETIME2
)
GO

CREATE TABLE Profiles
(
    profileId     INT IDENTITY CONSTRAINT PK_Profiles PRIMARY KEY,
    clientId      INT NOT NULL,
    profileName   VARCHAR(100) NOT NULL,

    status        VARCHAR(25) NOT NULL
        CONSTRAINT CK_Profiles_Status CHECK (status IN ('INACTIVE', 'ACTIVE')),

    exportLabel   VARCHAR(50) NOT NULL,

    rotation   INT NOT NULL DEFAULT 0
        CONSTRAINT CK_Profiles_Rotation CHECK (rotation IN (0, 90, 180, 270)),

    hue        INT NOT NULL DEFAULT 0
        CONSTRAINT CK_Profiles_Hue CHECK (hue BETWEEN -100 AND 100),

    brightness INT NOT NULL DEFAULT 0
        CONSTRAINT CK_Profiles_Brightness CHECK (brightness BETWEEN -100 AND 100),

    contrast   INT NOT NULL DEFAULT 0
        CONSTRAINT CK_Profiles_Contrast CHECK (contrast BETWEEN -100 AND 100),

    saturation INT NOT NULL DEFAULT 0
        CONSTRAINT CK_Profiles_Saturation CHECK (saturation BETWEEN -100 AND 100),

    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    deleted_at DATETIME2,

    CONSTRAINT FK_Profiles_Client
        FOREIGN KEY (clientId) REFERENCES Clients(clientId),

    CONSTRAINT UX_Profiles_Client_ProfileName
        UNIQUE (clientId, profileName)
)
GO

CREATE INDEX IX_Profiles_ClientId ON Profiles(clientId)
GO

CREATE TABLE Boxes
(
    boxId       INT IDENTITY CONSTRAINT PK_Boxes PRIMARY KEY,
    boxName     VARCHAR(100) NOT NULL,
    profileId   INT NOT NULL,

    created_at  DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    modified_at DATETIME2,
    deleted_at  DATETIME2,

    CONSTRAINT FK_Boxes_Profile
        FOREIGN KEY (profileId) REFERENCES Profiles(profileId)
)
GO

CREATE INDEX IX_Boxes_ProfileId ON Boxes(profileId)
GO

CREATE TABLE Documents
(
    documentId INT IDENTITY CONSTRAINT PK_Documents PRIMARY KEY,
    boxId      INT NOT NULL,

    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    modified_at DATETIME2,
    deleted_at  DATETIME2,

    CONSTRAINT FK_Documents_Box
        FOREIGN KEY (boxId) REFERENCES Boxes(boxId)
)
GO

CREATE INDEX IX_Documents_BoxId ON Documents(boxId)
GO

CREATE TABLE Files
(
    fileId        INT IDENTITY CONSTRAINT PK_Files PRIMARY KEY,
    documentId    INT NOT NULL,

    referenceId   INT NOT NULL,
    sortId        INT NOT NULL,

    imageData     VARBINARY(MAX) NOT NULL,
    fileSizeBytes INT NOT NULL,

    created_at    DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    deleted_at    DATETIME2,

    CONSTRAINT FK_Files_Document
        FOREIGN KEY (documentId) REFERENCES Documents(documentId)
)
GO

CREATE UNIQUE INDEX UX_Files_Document_Sort_Active
ON Files(documentId, sortId)
WHERE deleted_at IS NULL
GO

CREATE INDEX IX_Files_DocumentId ON Files(documentId)
GO

CREATE TABLE FileAdjustmentSettings
(
    fileAdjustmentSettingsId INT IDENTITY CONSTRAINT PK_FileAdjustmentSettings PRIMARY KEY,
    fileId INT NOT NULL,

    rotation   INT NOT NULL DEFAULT 0
        CONSTRAINT CK_FileAdj_Rotation CHECK (rotation IN (0, 90, 180, 270)),

    hue        INT NOT NULL DEFAULT 0
        CONSTRAINT CK_FileAdj_Hue CHECK (hue BETWEEN -100 AND 100),

    brightness INT NOT NULL DEFAULT 0
        CONSTRAINT CK_FileAdj_Brightness CHECK (brightness BETWEEN -100 AND 100),

    contrast   INT NOT NULL DEFAULT 0
        CONSTRAINT CK_FileAdj_Contrast CHECK (contrast BETWEEN -100 AND 100),

    saturation INT NOT NULL DEFAULT 0
        CONSTRAINT CK_FileAdj_Saturation CHECK (saturation BETWEEN -100 AND 100),

    created_at  DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    modified_at DATETIME2,
    deleted_at  DATETIME2,

    CONSTRAINT FK_FileAdj_File
        FOREIGN KEY (fileId) REFERENCES Files(fileId),

    CONSTRAINT UX_FileAdj_File UNIQUE (fileId)
)
GO

CREATE TABLE Users
(
    userId       INT IDENTITY CONSTRAINT PK_Users PRIMARY KEY,
    username     VARCHAR(50) NOT NULL UNIQUE,
    passwordHash VARCHAR(255) NOT NULL,

    role VARCHAR(20) NOT NULL
        CONSTRAINT CK_Users_Role CHECK (role IN ('ADMIN', 'USER')),

    deleted_at DATETIME2
)
GO

CREATE TABLE Logs
(
    logsId        INT IDENTITY CONSTRAINT PK_Logs PRIMARY KEY,
    userId        INT NOT NULL,

    entityId      INT NULL,
    entityType    VARCHAR(50) NOT NULL
        CONSTRAINT CK_Logs_EntityType
        CHECK (entityType IN ('CLIENT', 'PROFILE', 'BOX', 'DOCUMENT', 'FILE', 'USER')),

    action        VARCHAR(100) NOT NULL,
    log_timestamp DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_Logs_User
        FOREIGN KEY (userId) REFERENCES Users(userId)
)
GO

CREATE INDEX IX_Logs_UserId ON Logs(userId)
GO

CREATE TABLE UserClients
(
    userId   INT NOT NULL,
    clientId INT NOT NULL,

    CONSTRAINT PK_UserClients PRIMARY KEY (userId, clientId),

    CONSTRAINT FK_UserClients_User
        FOREIGN KEY (userId) REFERENCES Users(userId),

    CONSTRAINT FK_UserClients_Client
        FOREIGN KEY (clientId) REFERENCES Clients(clientId)
)
GO

CREATE INDEX IX_UserClients_ClientId ON UserClients(clientId)
GO

CREATE TABLE UserProfiles
(
    userId    INT NOT NULL,
    profileId INT NOT NULL,

    CONSTRAINT PK_UserProfiles PRIMARY KEY (userId, profileId),

    CONSTRAINT FK_UserProfiles_User
        FOREIGN KEY (userId) REFERENCES Users(userId),

    CONSTRAINT FK_UserProfiles_Profile
        FOREIGN KEY (profileId) REFERENCES Profiles(profileId)
)
GO

CREATE INDEX IX_UserProfiles_ProfileId ON UserProfiles(profileId)
GO

CREATE VIEW vw_BoxMetadata
AS
SELECT
    b.boxId,
    p.profileName,
    b.boxName,

    COUNT(DISTINCT d.documentId) AS documentCount,
    COUNT(DISTINCT f.fileId)     AS fileCount,

    b.created_at
FROM Boxes b
JOIN Profiles p
    ON p.profileId = b.profileId
LEFT JOIN Documents d
    ON d.boxId = b.boxId
   AND d.deleted_at IS NULL
LEFT JOIN Files f
    ON f.documentId = d.documentId
   AND f.deleted_at IS NULL
WHERE b.deleted_at IS NULL
GROUP BY
    b.boxId,
    p.profileName,
    b.boxName,
    b.created_at
GO
