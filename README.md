初始化数据库:
```
CREATE DATABASE dbs_proj;
```
```
USE dbs_proj;
```
```
CREATE TABLE IF NOT EXISTS user (
  id bigint NOT NULL AUTO_INCREMENT,
  name varchar(20) NOT NULL,
  password varchar(72) NOT NULL,
  role varchar(10) NOT NULL,
  status varchar(10) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UK_user_name (name)
);

```
默认管理员
- root
- rooo123t

手动生成BCrypt密码: https://bcrypt-generator.com/ (可选)
```
INSERT INTO user (name, password, role, status) 
VALUES ('root', '$2a$11$2vsRBrG6UxCH9ABy/KCNXuOPv5GS68RxUmTa8bayPub.kGHZDCHW6', 'ADMIN', 'APPROVED');
```
