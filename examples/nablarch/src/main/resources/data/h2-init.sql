-- =============================================================================
-- H2 Database Initialization Script for nablarch-example-web
-- =============================================================================
-- DB URL:  jdbc:h2:./h2/db/nablarch_example;AUTO_SERVER=TRUE
-- DB User: NABLARCH_EXAMPLE
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 1. Application Tables (from EDM: h2.edm)
-- ---------------------------------------------------------------------------

-- SYSTEM_ACCOUNT: System account information
CREATE TABLE IF NOT EXISTS SYSTEM_ACCOUNT (
    USER_ID                  INTEGER       NOT NULL AUTO_INCREMENT,
    LOGIN_ID                 VARCHAR(20)   NOT NULL,
    USER_PASSWORD            VARCHAR(44)   NOT NULL,
    USER_ID_LOCKED           BOOLEAN       NOT NULL,
    PASSWORD_EXPIRATION_DATE DATE          NOT NULL,
    FAILED_COUNT             SMALLINT      NOT NULL,
    EFFECTIVE_DATE_FROM      DATE,
    EFFECTIVE_DATE_TO        DATE,
    LAST_LOGIN_DATE_TIME     TIMESTAMP,
    VERSION                  BIGINT        NOT NULL DEFAULT 1,
    PRIMARY KEY (USER_ID)
);

-- USERS: User profile information
CREATE TABLE IF NOT EXISTS USERS (
    USER_ID    INTEGER      NOT NULL,
    KANJI_NAME VARCHAR(128) NOT NULL,
    KANA_NAME  VARCHAR(128) NOT NULL,
    PRIMARY KEY (USER_ID)
);

-- PASSWORD_HISTORY: Password change history
CREATE TABLE IF NOT EXISTS PASSWORD_HISTORY (
    PASSWORD_HISTORY_ID BIGINT      NOT NULL,
    USER_ID             INTEGER,
    USER_PASSWORD       VARCHAR(44) NOT NULL,
    PRIMARY KEY (PASSWORD_HISTORY_ID)
);

-- INDUSTRY: Industry master
CREATE TABLE IF NOT EXISTS INDUSTRY (
    INDUSTRY_CODE CHAR(2)     NOT NULL,
    INDUSTRY_NAME VARCHAR(50),
    PRIMARY KEY (INDUSTRY_CODE)
);

-- CLIENT: Client information
CREATE TABLE IF NOT EXISTS CLIENT (
    CLIENT_ID     INTEGER      NOT NULL AUTO_INCREMENT,
    CLIENT_NAME   VARCHAR(128) NOT NULL,
    INDUSTRY_CODE CHAR(2)      NOT NULL,
    PRIMARY KEY (CLIENT_ID)
);

-- PROJECT: Project information
CREATE TABLE IF NOT EXISTS PROJECT (
    PROJECT_ID                  INTEGER      NOT NULL AUTO_INCREMENT,
    PROJECT_NAME                VARCHAR(128) NOT NULL,
    PROJECT_TYPE                VARCHAR(128) NOT NULL,
    PROJECT_CLASS               VARCHAR(128) NOT NULL,
    PROJECT_START_DATE          DATE,
    PROJECT_END_DATE            DATE,
    CLIENT_ID                   INTEGER,
    PROJECT_MANAGER             VARCHAR(128),
    PROJECT_LEADER              VARCHAR(128),
    USER_ID                     INTEGER,
    NOTE                        VARCHAR(512),
    SALES                       INTEGER,
    COST_OF_GOODS_SOLD          INTEGER,
    SGA                         INTEGER,
    ALLOCATION_OF_CORP_EXPENSES INTEGER,
    VERSION                     BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (PROJECT_ID)
);

-- USER_SESSION: DB session store
CREATE TABLE IF NOT EXISTS USER_SESSION (
    SESSION_ID           CHAR(100) NOT NULL,
    SESSION_OBJECT       BINARY    NOT NULL,
    EXPIRATION_DATETIME  TIMESTAMP NOT NULL,
    PRIMARY KEY (SESSION_ID)
);

-- ---------------------------------------------------------------------------
-- 2. Nablarch Framework System Tables (from schema config)
-- ---------------------------------------------------------------------------

-- CODE_PATTERN: Code pattern definitions (code-table-schema.config)
CREATE TABLE IF NOT EXISTS CODE_PATTERN (
    CODE_ID    VARCHAR(8)  NOT NULL,
    CODE_VALUE VARCHAR(2)  NOT NULL,
    PATTERN01  CHAR(1),
    PATTERN02  CHAR(1),
    PATTERN03  CHAR(1),
    PATTERN04  CHAR(1),
    PATTERN05  CHAR(1),
    PATTERN06  CHAR(1),
    PATTERN07  CHAR(1),
    PATTERN08  CHAR(1),
    PATTERN09  CHAR(1),
    PATTERN10  CHAR(1),
    PATTERN11  CHAR(1),
    PATTERN12  CHAR(1),
    PATTERN13  CHAR(1),
    PATTERN14  CHAR(1),
    PATTERN15  CHAR(1),
    PATTERN16  CHAR(1),
    PATTERN17  CHAR(1),
    PATTERN18  CHAR(1),
    PATTERN19  CHAR(1),
    PATTERN20  CHAR(1),
    PRIMARY KEY (CODE_ID, CODE_VALUE)
);

-- CODE_NAME: Code name definitions (code-table-schema.config)
CREATE TABLE IF NOT EXISTS CODE_NAME (
    CODE_ID    VARCHAR(8)  NOT NULL,
    CODE_VALUE VARCHAR(2)  NOT NULL,
    LANG       VARCHAR(2)  NOT NULL,
    SORT_ORDER BIGINT,
    CODE_NAME  VARCHAR(50),
    SHORT_NAME VARCHAR(50),
    OPTION01   VARCHAR(40),
    OPTION02   VARCHAR(40),
    OPTION03   VARCHAR(40),
    OPTION04   VARCHAR(40),
    OPTION05   VARCHAR(40),
    OPTION06   VARCHAR(40),
    OPTION07   VARCHAR(40),
    OPTION08   VARCHAR(40),
    OPTION09   VARCHAR(40),
    OPTION10   VARCHAR(40),
    PRIMARY KEY (CODE_ID, CODE_VALUE, LANG)
);

-- BUSINESS_DATE: Business date management (business-date-table-schema.config)
CREATE TABLE IF NOT EXISTS BUSINESS_DATE (
    SEGMENT_ID VARCHAR(2) NOT NULL,
    BIZ_DATE   VARCHAR(8) NOT NULL,
    PRIMARY KEY (SEGMENT_ID)
);

-- ID_GENERATE: ID generator (idgenerator-table-schema.config)
CREATE TABLE IF NOT EXISTS ID_GENERATE (
    GENERATOR_ID VARCHAR(128) NOT NULL,
    GENERATED_NO BIGINT       NOT NULL,
    PRIMARY KEY (GENERATOR_ID)
);

-- MESSAGE: Message definitions (message-table-schema.config)
CREATE TABLE IF NOT EXISTS MESSAGE (
    MESSAGE_ID VARCHAR(8)    NOT NULL,
    LANG       VARCHAR(2)    NOT NULL,
    MESSAGE    VARCHAR(2000),
    PRIMARY KEY (MESSAGE_ID, LANG)
);

-- REQUEST: Request/service availability control (request-table-schema.config)
CREATE TABLE IF NOT EXISTS REQUEST (
    REQUEST_ID        VARCHAR(128) NOT NULL,
    REQUEST_NAME      VARCHAR(50),
    SERVICE_AVAILABLE VARCHAR(1)   NOT NULL DEFAULT '1',
    PRIMARY KEY (REQUEST_ID)
);

-- BATCH_REQUEST: Batch request management (batch-request-table-schema.config)
CREATE TABLE IF NOT EXISTS BATCH_REQUEST (
    BATCH_REQUEST_ID   VARCHAR(128) NOT NULL,
    SERVICE_AVAILABLE  VARCHAR(1)   NOT NULL DEFAULT '1',
    RESUME_POINT       VARCHAR(256),
    PROCESS_ACTIVE_FLG VARCHAR(1),
    PROCESS_HALT_FLG   VARCHAR(1),
    PRIMARY KEY (BATCH_REQUEST_ID)
);

-- ---------------------------------------------------------------------------
-- Permission check tables (permission-check-table-schema.config)
-- ---------------------------------------------------------------------------

-- UGROUP: User group
CREATE TABLE IF NOT EXISTS UGROUP (
    UGROUP_ID VARCHAR(50) NOT NULL,
    PRIMARY KEY (UGROUP_ID)
);

-- UGROUP_SYSTEM_ACCOUNT: User group - system account association
CREATE TABLE IF NOT EXISTS UGROUP_SYSTEM_ACCOUNT (
    UGROUP_ID           VARCHAR(50) NOT NULL,
    USER_ID             INTEGER     NOT NULL,
    EFFECTIVE_DATE_FROM DATE,
    EFFECTIVE_DATE_TO   DATE,
    PRIMARY KEY (UGROUP_ID, USER_ID)
);

-- PERMISSION_UNIT: Permission unit
CREATE TABLE IF NOT EXISTS PERMISSION_UNIT (
    PERMISSION_UNIT_ID VARCHAR(50) NOT NULL,
    PRIMARY KEY (PERMISSION_UNIT_ID)
);

-- PERMISSION_UNIT_REQUEST: Permission unit - request association
CREATE TABLE IF NOT EXISTS PERMISSION_UNIT_REQUEST (
    PERMISSION_UNIT_ID VARCHAR(50)  NOT NULL,
    REQUEST_ID         VARCHAR(128) NOT NULL,
    PRIMARY KEY (PERMISSION_UNIT_ID, REQUEST_ID)
);

-- UGROUP_AUTHORITY: User group - permission unit association
CREATE TABLE IF NOT EXISTS UGROUP_AUTHORITY (
    UGROUP_ID          VARCHAR(50) NOT NULL,
    PERMISSION_UNIT_ID VARCHAR(50) NOT NULL,
    PRIMARY KEY (UGROUP_ID, PERMISSION_UNIT_ID)
);

-- SYSTEM_ACCOUNT_AUTHORITY: System account - permission unit association
CREATE TABLE IF NOT EXISTS SYSTEM_ACCOUNT_AUTHORITY (
    USER_ID            INTEGER     NOT NULL,
    PERMISSION_UNIT_ID VARCHAR(50) NOT NULL,
    PRIMARY KEY (USER_ID, PERMISSION_UNIT_ID)
);

-- ---------------------------------------------------------------------------
-- Mail tables (mail-table-schema.config) -- included for completeness
-- ---------------------------------------------------------------------------

-- MAIL_REQUEST
CREATE TABLE IF NOT EXISTS MAIL_REQUEST (
    MAIL_REQUEST_ID      VARCHAR(128) NOT NULL,
    SUBJECT              VARCHAR(150),
    MAIL_FROM            VARCHAR(100),
    REPLY_TO             VARCHAR(100),
    RETURN_PATH          VARCHAR(100),
    CHARSET              VARCHAR(50),
    STATUS               CHAR(1),
    REQUEST_DATETIME     TIMESTAMP,
    SEND_DATETIME        TIMESTAMP,
    MAIL_SEND_PATTERN_ID VARCHAR(2),
    MAIL_BODY            CLOB,
    PROCESS_ID           VARCHAR(36),
    PRIMARY KEY (MAIL_REQUEST_ID)
);

-- MAIL_RECIPIENT
CREATE TABLE IF NOT EXISTS MAIL_RECIPIENT (
    MAIL_REQUEST_ID VARCHAR(128) NOT NULL,
    SERIAL_NUMBER   BIGINT       NOT NULL,
    RECIPIENT_TYPE  CHAR(2),
    MAIL_ADDRESS    VARCHAR(100),
    PRIMARY KEY (MAIL_REQUEST_ID, SERIAL_NUMBER)
);

-- MAIL_ATTACHED_FILE
CREATE TABLE IF NOT EXISTS MAIL_ATTACHED_FILE (
    MAIL_REQUEST_ID VARCHAR(128) NOT NULL,
    SERIAL_NUMBER   BIGINT       NOT NULL,
    FILE_NAME       VARCHAR(150),
    CONTENT_TYPE    VARCHAR(50),
    ATTACHED_FILE   BLOB,
    PRIMARY KEY (MAIL_REQUEST_ID, SERIAL_NUMBER)
);

-- MAIL_TEMPLATE
CREATE TABLE IF NOT EXISTS MAIL_TEMPLATE (
    MAIL_TEMPLATE_ID VARCHAR(128) NOT NULL,
    LANG             VARCHAR(2)   NOT NULL,
    SUBJECT          VARCHAR(150),
    CHARSET          VARCHAR(50),
    MAIL_BODY        CLOB,
    PRIMARY KEY (MAIL_TEMPLATE_ID, LANG)
);

-- =============================================================================
-- 3. Seed Data
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 3.1 Business Date (required: segment '01' for default business date)
-- ---------------------------------------------------------------------------
MERGE INTO BUSINESS_DATE (SEGMENT_ID, BIZ_DATE) KEY (SEGMENT_ID) VALUES ('01', '20260228');

-- ---------------------------------------------------------------------------
-- 3.2 ID Generator (for auto-incrementing entity IDs)
-- ---------------------------------------------------------------------------
MERGE INTO ID_GENERATE (GENERATOR_ID, GENERATED_NO) KEY (GENERATOR_ID) VALUES ('PROJECT_ID', 1000);
MERGE INTO ID_GENERATE (GENERATOR_ID, GENERATED_NO) KEY (GENERATOR_ID) VALUES ('CLIENT_ID', 1000);

-- ---------------------------------------------------------------------------
-- 3.3 Industry Master (from EDM)
-- ---------------------------------------------------------------------------
MERGE INTO INDUSTRY (INDUSTRY_CODE, INDUSTRY_NAME) KEY (INDUSTRY_CODE) VALUES ('01', '農業');
MERGE INTO INDUSTRY (INDUSTRY_CODE, INDUSTRY_NAME) KEY (INDUSTRY_CODE) VALUES ('02', '林業');
MERGE INTO INDUSTRY (INDUSTRY_CODE, INDUSTRY_NAME) KEY (INDUSTRY_CODE) VALUES ('03', '漁業');
MERGE INTO INDUSTRY (INDUSTRY_CODE, INDUSTRY_NAME) KEY (INDUSTRY_CODE) VALUES ('04', '鉱業');
MERGE INTO INDUSTRY (INDUSTRY_CODE, INDUSTRY_NAME) KEY (INDUSTRY_CODE) VALUES ('05', '建設業');
MERGE INTO INDUSTRY (INDUSTRY_CODE, INDUSTRY_NAME) KEY (INDUSTRY_CODE) VALUES ('06', '製造業');
MERGE INTO INDUSTRY (INDUSTRY_CODE, INDUSTRY_NAME) KEY (INDUSTRY_CODE) VALUES ('07', '電気・ガス・水道業');
MERGE INTO INDUSTRY (INDUSTRY_CODE, INDUSTRY_NAME) KEY (INDUSTRY_CODE) VALUES ('08', '運輸・通信業');
MERGE INTO INDUSTRY (INDUSTRY_CODE, INDUSTRY_NAME) KEY (INDUSTRY_CODE) VALUES ('09', '卸売・小売業');
MERGE INTO INDUSTRY (INDUSTRY_CODE, INDUSTRY_NAME) KEY (INDUSTRY_CODE) VALUES ('10', '金融・保険業');
MERGE INTO INDUSTRY (INDUSTRY_CODE, INDUSTRY_NAME) KEY (INDUSTRY_CODE) VALUES ('11', '不動産業');
MERGE INTO INDUSTRY (INDUSTRY_CODE, INDUSTRY_NAME) KEY (INDUSTRY_CODE) VALUES ('12', 'サービス業');
MERGE INTO INDUSTRY (INDUSTRY_CODE, INDUSTRY_NAME) KEY (INDUSTRY_CODE) VALUES ('13', '官公庁');
MERGE INTO INDUSTRY (INDUSTRY_CODE, INDUSTRY_NAME) KEY (INDUSTRY_CODE) VALUES ('14', '情報通信業');

-- ---------------------------------------------------------------------------
-- 3.4 System Account (test user)
--     Login ID: 10000001  /  Password: pass123-
--     PBKDF2WithHmacSha1, fixedSalt=01234567890123456789, saltSeed=userId,
--     iterationCount=3966, keyLength=256
-- ---------------------------------------------------------------------------
MERGE INTO SYSTEM_ACCOUNT (USER_ID, LOGIN_ID, USER_PASSWORD, USER_ID_LOCKED,
    PASSWORD_EXPIRATION_DATE, FAILED_COUNT, EFFECTIVE_DATE_FROM, EFFECTIVE_DATE_TO,
    LAST_LOGIN_DATE_TIME, VERSION) KEY (USER_ID)
VALUES (1, '10000001', 'TfElfP7gKf8afm0onXQHDG4E0rHEWSBBURWU6LLQwOQ=', FALSE,
    '2099-12-31', 0, '2010-01-01', '2099-12-31', NULL, 1);

-- ---------------------------------------------------------------------------
-- 3.5 Users (profile for test user)
-- ---------------------------------------------------------------------------
MERGE INTO USERS (USER_ID, KANJI_NAME, KANA_NAME) KEY (USER_ID)
VALUES (1, 'テストユーザ', 'てすとゆーざ');

-- ---------------------------------------------------------------------------
-- 3.6 Client (sample client)
-- ---------------------------------------------------------------------------
MERGE INTO CLIENT (CLIENT_ID, CLIENT_NAME, INDUSTRY_CODE) KEY (CLIENT_ID)
VALUES (1, 'サンプル顧客', '14');

-- ---------------------------------------------------------------------------
-- 3.7 User Group & Permissions (for authorization check)
-- ---------------------------------------------------------------------------

-- Create a user group
MERGE INTO UGROUP (UGROUP_ID) KEY (UGROUP_ID) VALUES ('ADMIN_GROUP');

-- Associate the test user with the group
MERGE INTO UGROUP_SYSTEM_ACCOUNT (UGROUP_ID, USER_ID, EFFECTIVE_DATE_FROM, EFFECTIVE_DATE_TO)
    KEY (UGROUP_ID, USER_ID)
VALUES ('ADMIN_GROUP', 1, '2010-01-01', '2099-12-31');

-- Create a permission unit
MERGE INTO PERMISSION_UNIT (PERMISSION_UNIT_ID) KEY (PERMISSION_UNIT_ID)
VALUES ('ADMIN_PERMISSION');

-- Associate user group with permission unit
MERGE INTO UGROUP_AUTHORITY (UGROUP_ID, PERMISSION_UNIT_ID) KEY (UGROUP_ID, PERMISSION_UNIT_ID)
VALUES ('ADMIN_GROUP', 'ADMIN_PERMISSION');

-- ---------------------------------------------------------------------------
-- 3.8 Request definitions (service availability)
--     All application routes are registered as available.
-- ---------------------------------------------------------------------------
MERGE INTO REQUEST (REQUEST_ID, REQUEST_NAME, SERVICE_AVAILABLE) KEY (REQUEST_ID) VALUES ('/action/login', 'ログイン', '1');
MERGE INTO REQUEST (REQUEST_ID, REQUEST_NAME, SERVICE_AVAILABLE) KEY (REQUEST_ID) VALUES ('/action/logout', 'ログアウト', '1');
MERGE INTO REQUEST (REQUEST_ID, REQUEST_NAME, SERVICE_AVAILABLE) KEY (REQUEST_ID) VALUES ('/action/project', 'プロジェクト', '1');
MERGE INTO REQUEST (REQUEST_ID, REQUEST_NAME, SERVICE_AVAILABLE) KEY (REQUEST_ID) VALUES ('/action/projectUpload', 'プロジェクトアップロード', '1');

-- Associate requests with permission unit
MERGE INTO PERMISSION_UNIT_REQUEST (PERMISSION_UNIT_ID, REQUEST_ID)
    KEY (PERMISSION_UNIT_ID, REQUEST_ID) VALUES ('ADMIN_PERMISSION', '/action/login');
MERGE INTO PERMISSION_UNIT_REQUEST (PERMISSION_UNIT_ID, REQUEST_ID)
    KEY (PERMISSION_UNIT_ID, REQUEST_ID) VALUES ('ADMIN_PERMISSION', '/action/logout');
MERGE INTO PERMISSION_UNIT_REQUEST (PERMISSION_UNIT_ID, REQUEST_ID)
    KEY (PERMISSION_UNIT_ID, REQUEST_ID) VALUES ('ADMIN_PERMISSION', '/action/project');
MERGE INTO PERMISSION_UNIT_REQUEST (PERMISSION_UNIT_ID, REQUEST_ID)
    KEY (PERMISSION_UNIT_ID, REQUEST_ID) VALUES ('ADMIN_PERMISSION', '/action/projectUpload');

-- ---------------------------------------------------------------------------
-- 3.9 Sample Project data
-- ---------------------------------------------------------------------------
MERGE INTO PROJECT (PROJECT_ID, PROJECT_NAME, PROJECT_TYPE, PROJECT_CLASS,
    PROJECT_START_DATE, PROJECT_END_DATE, CLIENT_ID, PROJECT_MANAGER, PROJECT_LEADER,
    USER_ID, NOTE, SALES, COST_OF_GOODS_SOLD, SGA, ALLOCATION_OF_CORP_EXPENSES, VERSION)
    KEY (PROJECT_ID)
VALUES (1, 'サンプルプロジェクト', 'development', 'a',
    '2024-01-01', '2025-03-31', 1, 'テストユーザ', 'テストユーザ',
    1, 'サンプルプロジェクトの備考', 10000000, 5000000, 2000000, 1000000, 1);
