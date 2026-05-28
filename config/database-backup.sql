create table Clients
(
    clientId   int identity
        constraint PK_Clients
            primary key,
    clientName varchar(100) not null
        constraint Clients_pk
            unique,
    deleted_at datetime2
)
    go

create table Profiles
(
    profileId   int identity
        constraint PK_Profiles
            primary key,
    clientId    int                                not null
        constraint FK_Profiles_Client
            references Clients,
    profileName varchar(100)                       not null
        constraint Profiles_pk
            unique,
    status      varchar(25)                        not null
        constraint CK_Profiles_Status
            check ([status] = 'ACTIVE' OR [status] = 'INACTIVE'),
    exportLabel varchar(50)                        not null,
    rotation    int       default 0                not null
        constraint CK_Profiles_Rotation
            check ([rotation] = 270 OR [rotation] = 180 OR [rotation] = 90 OR [rotation] = 0),
    hue         int       default 0                not null
        constraint CK_Profiles_Hue
            check ([hue] >= (-100) AND [hue] <= 100),
    brightness  int       default 0                not null
        constraint CK_Profiles_Brightness
            check ([brightness] >= (-100) AND [brightness] <= 100),
    contrast    int       default 0                not null
        constraint CK_Profiles_Contrast
            check ([contrast] >= (-100) AND [contrast] <= 100),
    saturation  int       default 0                not null
        constraint CK_Profiles_Saturation
            check ([saturation] >= (-100) AND [saturation] <= 100),
    created_at  datetime2 default sysutcdatetime() not null,
    deleted_at  datetime2
)
    go

create table Boxes
(
    boxId      int identity
        constraint PK_Boxes
            primary key,
    boxName    varchar(100)                       not null,
    profileId  int                                not null
        constraint FK_Boxes_Profile
            references Profiles,
    created_at datetime2 default sysutcdatetime() not null,
    deleted_at datetime2
)
    go

create index IX_Boxes_ProfileId
    on Boxes (profileId)
    go

create table Documents
(
    documentId int identity
        constraint PK_Documents
            primary key,
    boxId      int                                not null
        constraint FK_Documents_Box
            references Boxes,
    created_at datetime2 default sysutcdatetime() not null,
    deleted_at datetime2
)
    go

create index IX_Documents_BoxId
    on Documents (boxId)
    go

create table Files
(
    fileId        int identity
        constraint PK_Files
            primary key,
    documentId    int                                not null
        constraint FK_Files_Document
            references Documents,
    referenceId   int                                not null,
    sortId        int                                not null,
    imageData     varbinary(max)                     not null,
    fileSizeBytes int                                not null,
    created_at    datetime2 default sysutcdatetime() not null,
    deleted_at    datetime2
)
    go

create table FileAdjustmentSettings
(
    fileAdjustmentSettingsId int identity
        constraint PK_FileAdjustmentSettings
            primary key,
    fileId                   int                                not null
        constraint UX_FileAdj_File
            unique
        constraint FK_FileAdj_File
            references Files,
    rotation                 int       default 0                not null
        constraint CK_FileAdj_Rotation
            check ([rotation] > (-360) AND [rotation] < 360),
    hue                      int       default 0                not null
        constraint CK_FileAdj_Hue
            check ([hue] >= (-100) AND [hue] <= 100),
    brightness               int       default 0                not null
        constraint CK_FileAdj_Brightness
            check ([brightness] >= (-100) AND [brightness] <= 100),
    contrast                 int       default 0                not null
        constraint CK_FileAdj_Contrast
            check ([contrast] >= (-100) AND [contrast] <= 100),
    saturation               int       default 0                not null
        constraint CK_FileAdj_Saturation
            check ([saturation] >= (-100) AND [saturation] <= 100),
    sharpness                int       default 0                not null,
    created_at               datetime2 default sysutcdatetime() not null,
    deleted_at               datetime2
)
    go

create unique index UX_Files_Document_Sort_Active
    on Files (documentId, sortId)
    where [deleted_at] IS NULL
go

create index IX_Files_DocumentId
    on Files (documentId)
    go

create index IX_Profiles_ClientId
    on Profiles (clientId)
    go

create table Users
(
    userId       int identity
        constraint PK_Users
            primary key,
    username     varchar(50)  not null
        unique,
    passwordHash varchar(255) not null,
    role         varchar(20)  not null
        constraint CK_Users_Role
            check ([role] = 'USER' OR [role] = 'ADMIN'),
    deleted_at   datetime2
)
    go

create table Logs
(
    logsId        int identity
        constraint PK_Logs
            primary key,
    userId        int                                not null
        constraint FK_Logs_User
            references Users,
    entityId      int,
    entityType    varchar(50)                        not null
        constraint CK_Logs_EntityType
            check ([entityType] = 'USER' OR [entityType] = 'FILE' OR [entityType] = 'DOCUMENT' OR
        [entityType] = 'BOX' OR [entityType] = 'PROFILE' OR [entityType] = 'CLIENT'),
    action        varchar(100)                       not null,
    log_timestamp datetime2 default sysutcdatetime() not null
)
    go

create index IX_Logs_UserId
    on Logs (userId)
    go

create table Sessions
(
    sessionId int identity
        constraint Sessions_pk
            primary key,
    userId    int                            not null
        constraint Sessions_Users_userId_fk
            references Users,
    boxName   varchar(100)                   not null
        constraint Sessions_pk_2
            unique,
    lockedAt  datetime2 default getutcdate() not null
)
    go

create table UserProfiles
(
    userId    int not null
        constraint FK_UserProfiles_User
            references Users,
    profileId int not null
        constraint FK_UserProfiles_Profile
            references Profiles,
    constraint PK_UserProfiles
        primary key (userId, profileId)
)
    go

create index IX_UserProfiles_ProfileId
    on UserProfiles (profileId)
    go

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
    go
