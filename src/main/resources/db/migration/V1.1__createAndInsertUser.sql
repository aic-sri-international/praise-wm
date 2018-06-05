CREATE TABLE user (
  user_id         INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name            VARCHAR(50) NOT NULL,
  fullname        VARCHAR(50) NOT NULL,
  password        VARCHAR(64) NOT NULL,
  create_date     DATETIME    NULL,
  last_login_date DATETIME    NULL,
  UNIQUE INDEX name_uidx (name)
);

DELIMITER //

CREATE TRIGGER user_bins
BEFORE INSERT ON user
FOR EACH ROW
  BEGIN
    SET NEW.create_date = UTC_TIMESTAMP();
    SET NEW.password = sha2(NEW.password, 256);
  END //

CREATE TRIGGER user_bupd
BEFORE UPDATE ON user
FOR EACH ROW
  BEGIN
    IF (NEW.password IS NULL OR NEW.password = '')
    THEN
      SET NEW.password = OLD.password;
    ELSE
      SET NEW.password = sha2(NEW.password, 256);
    END IF;
  END //

DELIMITER ;

INSERT INTO user (name, password, fullname) VALUES ('user', 'user', 'user');
INSERT INTO user (name, password, fullname) VALUES ('admin', 'admin', 'admin');
