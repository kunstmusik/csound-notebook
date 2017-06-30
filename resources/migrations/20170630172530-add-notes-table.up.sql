CREATE TABLE notes 
(id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
 note_id CHAR(8),
 orc TEXT,
 sco TEXT,
 is_live BOOL,
 is_public BOOL,
 user_id int,
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
 );
