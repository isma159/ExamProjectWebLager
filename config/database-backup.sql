create table Clients
(
    clientId   int identity
        primary key,
    clientName varchar(100) not null,
    deleted_at datetime2
)
    go

create table Profiles
(
    profileId     int identity
        primary key,
    clientId      int           not null
        references Clients,
    profileName   varchar(100)  not null
        unique,
    splitBehavior varchar(50)   not null
        check ([splitBehavior] = 'BARCODE' OR [splitBehavior] = 'NONE'),
    status        varchar(25)   not null
        check ([status] = 'INACTIVE' OR [status] = 'ACTIVE'),
    exportLabel   varchar(50)   not null,
    deleted_at    datetime2,
    brightness    int default 0 not null,
    contrast      int default 0 not null
)
    go

create table Boxes
(
    boxId       int identity
        primary key,
    boxName     varchar(100)                    not null,
    profileId   int                             not null
        references Profiles,
    created_at  datetime2 default sysdatetime() not null,
    modified_at datetime2,
    deleted_at  datetime2
)
    go

create table BoxMetadata
(
    metadataId    int identity
        primary key,
    boxId         int           not null
        references Boxes,
    profileName   varchar(100)  not null,
    boxName       varchar(100)  not null,
    documentCount int default 0 not null,
    fileCount     int default 0 not null,
    boxCreatedAt  datetime2     not null,
    deleted_at    datetime2
)
    go

create table Documents
(
    documentId  int identity
        primary key,
    boxId       int                             not null
        references Boxes,
    created_at  datetime2 default sysdatetime() not null,
    modified_at datetime2,
    deleted_at  datetime2
)
    go

create table Files
(
    fileId        int identity
        primary key,
    documentId    int                             not null
        references Documents,
    referenceId   int                             not null,
    sortId        int                             not null,
    imageData     varbinary(max)                  not null,
    fileSizeBytes int,
    created_at    datetime2 default sysdatetime() not null,
    rotation      int       default 0             not null
        check ([rotation] = 0 OR [rotation] = 90 OR [rotation] = 180 OR [rotation] = 270),
    deleted_at    datetime2,
    brightness    int       default 0,
    contrast      int       default 0
)
    go

create table Users
(
    userId       int identity
        primary key,
    username     varchar(50)  not null
        unique,
    passwordHash varchar(255) not null,
    role         varchar(20)  not null
        check ([role] = 'ADMIN' OR [role] = 'USER'),
    deleted_at   datetime2
)
    go

create table Logs
(
    logsId        int identity
        primary key,
    userId        int                             not null
        references Users,
    entityId      int,
    entityType    varchar(50),
    action        varchar(100)                    not null,
    log_timestamp datetime2 default sysdatetime() not null,
    deleted_at    datetime2
)
    go

create table UserClients
(
    userId   int not null
        references Users,
    clientId int not null
        references Clients,
    primary key (userId, clientId)
)
    go

create table UserProfiles
(
    userId    int not null
        references Users,
    profileId int not null
        references Profiles,
    primary key (userId, profileId)
)
    go