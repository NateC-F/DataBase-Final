-- ============================
--		MANGA CAFE
-- ============================

DROP DATABASE IF EXISTS MangaCafe;

CREATE DATABASE MangaCafe;
USE MangaCafe;
-- =================================================================================
CREATE TABLE Rooms
(
	room_id			INT 		PRIMARY KEY		AUTO_INCREMENT,
    room_name		VARCHAR(20)					NOT NULL
    -- occupied 		BOOL ???
)AUTO_INCREMENT = 1;
-- =================================================================================
CREATE TABLE Authors
(
	author_id		INT			PRIMARY KEY		AUTO_INCREMENT,
    author_name		VARCHAR(50) 				UNIQUE			NOT NULL
)AUTO_INCREMENT = 1;
-- =================================================================================
CREATE TABLE Books
(
	book_id			INT			PRIMARY KEY		AUTO_INCREMENT,
    book_name		VARCHAR(50)					UNIQUE			NOT NULL,
    available_copies 	INT		NOT NULL
)AUTO_INCREMENT = 1;
-- =================================================================================
Create TABLE Food
(
	food_id		INT			PRIMARY KEY		AUTO_INCREMENT,
    food_name	VARCHAR(20)					UNIQUE			NOT NULL,
    cost		decimal(3,2)				NOT NULL
)AUTO_INCREMENT = 1;
-- =================================================================================
CREATE TABLE Genre
(
	genre_id		INT		PRIMARY KEY		auto_increment,
    genre_name		VARCHAR(20)				NOT NULL
) auto_increment = 1;
-- =================================================================================
-- Displays the what books belong to what genre's
CREATE TABLE Book_Genre
(
	book_id 		INT,
    genre_id		INT,
    constraint bg_pk PRIMARY KEY (book_id,genre_id),
    constraint bg_book_fk foreign key (book_id) references Books (book_id) ON DELETE CASCADE ON UPDATE CASCADE,
    constraint bg_genre_fk foreign key (genre_id) references Genre (genre_id) ON DELETE CASCADE ON UPDATE CASCADE
);
-- =================================================================================
-- Displays the what books were written by what authors
CREATE TABLE Book_Author
(
	book_id 		INT,
    author_id		INT,
    constraint ba_pk PRIMARY KEY (book_id, author_id),
    constraint ba_book_fk foreign key (book_id) references Books (book_id) ON DELETE CASCADE ON UPDATE CASCADE,
    constraint ba_author_fk foreign key (author_id) references Authors (author_id) ON DELETE CASCADE ON UPDATE CASCADE
);
-- =================================================================================
-- Displays the what books are in what Rooms
CREATE TABLE Book_Room
(
	book_id 		INT,
    room_id			INT,
    constraint br_pk PRIMARY KEY (book_id,room_id),
    constraint br_book_fk foreign key (book_id) references Books (book_id) ON DELETE CASCADE ON UPDATE CASCADE,
    constraint br_room_fk foreign key (room_id) references Rooms (room_id) ON DELETE CASCADE ON UPDATE CASCADE
);
-- =================================================================================
-- Displays the what room has ordered what food
CREATE TABLE Food_Room
(
    room_id 		INT 		NOT NULL,
    food_id			INT			NOT NULL,
	quantity   		INT			DEFAULT 1,
    CONSTRAINT	fr_pk PRIMARY KEY (room_id, food_id),
    constraint fr_room_fk foreign key (room_id) references Rooms (room_id) ON DELETE CASCADE ON UPDATE CASCADE,
    constraint fr_food_fk foreign key (food_id) references Food (food_id) ON DELETE CASCADE ON UPDATE CASCADE
);
-- =================================================================================
CREATE TABLE Visitors 
(
    visitor_id 		INT 		PRIMARY KEY 		AUTO_INCREMENT,
    visitor_name 	VARCHAR(50) 					NOT NULL,
    check_in_time 	DATETIME 						DEFAULT NOW(),
    check_out_time	DATETIME,
    room_id INT,
    FOREIGN KEY (room_id) REFERENCES Rooms(room_id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ============================
--		 CREATING VIEWS
-- ============================
DROP VIEW IF EXISTS room_summary;

CREATE VIEW room_summary AS
SELECT r.room_id AS 'Room Number',
	   v.visitor_name AS 'Guests Name',
	   GROUP_CONCAT(DISTINCT b.book_name SEPARATOR ', ') AS 'Books In Room',
       GROUP_CONCAT(DISTINCT CONCAT(f.food_name, ' (x', fr.quantity, ')') SEPARATOR ', ') AS 'Food In Room'
FROM visitors v JOIN rooms r ON v.room_id = r.room_id
	 LEFT JOIN book_room br ON r.room_id = br.room_id
     LEFT JOIN books b ON br.book_id = b.book_id
     LEFT JOIN food_room fr ON r.room_id = fr.room_id
     LEFT JOIN food f ON fr.food_id = f.food_id
GROUP BY r.room_id, v.visitor_name;

-- =================================================================================
DROP VIEW IF EXISTS book_inventory;

CREATE VIEW book_inventory AS
SELECT b.book_name AS 'Book Name', b.available_copies AS 'Available Copies Left', 
	   GROUP_CONCAT(DISTINCT g.genre_name SEPARATOR', ') AS Genres
FROM books b JOIN book_genre bg ON b.book_id = bg.book_id
			 JOIN genre g ON bg.genre_id = g.genre_id
GROUP BY b.book_id;

-- =================================================================================

DROP VIEW IF EXISTS food_total;

CREATE VIEW food_total AS
SELECT f.food_name AS 'Food', fr.quantity AS 'How Many',r.room_id, 
       r.room_name AS 'What Room', quantity * cost AS 'Cost_Per_Item'
FROM food f JOIN food_room fr ON f.food_id = fr.food_id
			JOIN rooms r ON fr.room_id = r.room_id;
            

-- ============================
--		 CREATING PROCEDURE
-- ============================

DROP PROCEDURE IF EXISTS food_room_total;

DELIMITER //
CREATE PROCEDURE food_room_total(IN room_num INT, OUT total_cost DECIMAL (5,2))
BEGIN
	SELECT IFNULL(SUM(Cost_Per_Item), 0) INTO total_cost
	FROM food_total
	WHERE room_id = room_num;
END // 
DELIMITER ;
-- =================================================================================
DROP PROCEDURE IF EXISTS room_total_cost;

DELIMITER //
CREATE PROCEDURE room_total_cost(IN room_num INT)
BEGIN
	DECLARE food_total DECIMAL(5,2);
	DECLARE book_fee INT;
    DECLARE room_fee INT DEFAULT 10;
    DECLARE total_cost DECIMAL(5,2);

    CALL food_room_total(room_num, food_total); 
    
	SELECT COUNT(DISTINCT book_id) INTO book_fee
    FROM book_room
    WHERE room_id = room_num;    
    
    SET total_cost = food_total + book_fee + room_fee;
    
    SELECT  room_fee, book_fee, food_total,
			CONCAT('$', total_cost) AS 'Room_total';
    
END // 
DELIMITER ;
-- =================================================================================
DROP PROCEDURE IF EXISTS order_food;

DELIMITER //
CREATE PROCEDURE order_food(IN room_num INT, IN food_type INT, IN food_amount INT) 
BEGIN
	IF EXISTS
		(SELECT * 
		FROM food_room
		WHERE room_id = room_num AND food_id = food_type) 
	THEN 
		UPDATE food_room
        SET quantity = quantity + food_amount
        WHERE room_id = room_num AND food_id = food_type;
	ELSE
		INSERT INTO food_room (room_id, food_id, quantity)
        VALUES (room_num, food_type, food_amount);
	END IF;
END//
DELIMITER ;

-- =================================================================================
DROP PROCEDURE IF EXISTS order_book;

DELIMITER //
CREATE PROCEDURE order_book(IN room_num INT, IN book_num INT)
BEGIN
	IF EXISTS
		(SELECT * 
		FROM book_room
		WHERE room_id = room_num AND book_id = book_num) 
	THEN 
		SIGNAL SQLSTATE '45000'
			SET MESSAGE_TEXT = 'This Room Already Has That Book';
	ELSE 
		INSERT INTO book_room (room_id, book_id)
        VALUES(room_num, book_num);
	END IF;
END //
DELIMITER ;

-- =================================================================================
DROP PROCEDURE IF EXISTS check_out;

DELIMITER //
CREATE PROCEDURE check_out(IN room_num INT)
BEGIN
	UPDATE visitors
    SET room_id = null, check_out_time = NOW()
    WHERE room_id = room_num;
    
    DELETE FROM food_room WHERE room_id = room_num;
    
    DELETE FROM book_room WHERE room_id = room_num;
END // 
DELIMITER ;

-- ============================
--		 CREATING TRIGGER
-- ============================

DROP TRIGGER IF EXISTS auto_dec_book_copies;

DELIMITER //
CREATE TRIGGER auto_dec_book_copies 
AFTER INSERT ON book_room
FOR EACH ROW

BEGIN
	UPDATE books
    SET available_copies=available_copies-1
    WHERE book_id = NEW.book_id;
END//
DELIMITER ;

-- =================================================================================
DROP TRIGGER IF EXISTS auto_inc_book_copies;

DELIMITER //
CREATE TRIGGER auto_inc_book_copies
AFTER DELETE ON book_room
FOR EACH ROW

BEGIN
	UPDATE books
    SET available_copies=available_copies+1
    WHERE book_id = OLD.book_id;
END//
DELIMITER ;

-- =================================================================================
DROP TRIGGER IF EXISTS prevent_zero_available_copies;

DELIMITER //
CREATE TRIGGER prevent_zero_available_copies
BEFORE INSERT ON book_room
FOR EACH ROW

BEGIN
	DECLARE available INT;
    
    SELECT available_copies INTO available
    FROM books
    WHERE book_id = NEW.book_id;
    
    IF available = 0 THEN
	SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'No Available Copies Left';
    END IF;
END//
DELIMITER ;

-- ============================
--		 TABLE INSERTS
-- ============================

INSERT INTO Rooms 
	(room_name)
VALUES
	('Room 1'),('Room 2'),('Room 3'),('Room 4'),('Room 5'),
    ('Room 6'),('Room 7'),('Room 8'),('Room 9'),('Room 10');
-- =================================================================================
INSERT INTO Authors (author_name) VALUES
('Eiichiro Oda'),           -- 1
('Masashi Kishimoto'),      -- 2
('Akira Toriyama'),         -- 3
('Naoko Takeuchi'),         -- 4
('Hajime Isayama'),         -- 5
('Tite Kubo'),              -- 6
('Rumiko Takahashi'),       -- 7
('Kentaro Miura'),          -- 8
('Hiromu Arakawa'),         -- 9
('Yoshihiro Togashi');      -- 10
-- =================================================================================
INSERT INTO Books (book_name, available_copies) VALUES
-- Eiichiro Oda
('One Piece Vol. 1', 5),
('One Piece Vol. 2', 4),
('One Piece Vol. 3', 4),
-- Masashi Kishimoto
('Naruto Vol. 1', 3),
('Naruto Vol. 2', 2),
('Naruto Vol. 3', 3),
-- Akira Toriyama
('Dragon Ball Vol. 1', 4),
('Dragon Ball Vol. 2', 4),
('Dragon Ball Vol. 3', 3),
-- Naoko Takeuchi
('Sailor Moon Vol. 1', 2),
('Sailor Moon Vol. 2', 2),
('Sailor Moon Vol. 3', 1),
-- Hajime Isayama
('Attack on Titan Vol. 1', 6),
('Attack on Titan Vol. 2', 5),
('Attack on Titan Vol. 3', 5),
-- Tite Kubo
('Bleach Vol. 1', 4),
('Bleach Vol. 2', 3),
('Bleach Vol. 3', 3),

-- Rumiko Takahashi
('Inuyasha Vol. 1', 3),
('Inuyasha Vol. 2', 2),
('Inuyasha Vol. 3', 2),
-- Kentaro Miura
('Berserk Vol. 1', 2),
('Berserk Vol. 2', 2),
('Berserk Vol. 3', 1),
-- Hiromu Arakawa
('Fullmetal Alchemist Vol. 1', 4),
('Fullmetal Alchemist Vol. 2', 3),
('Fullmetal Alchemist Vol. 3', 3),
-- Yoshihiro Togashi
('Hunter x Hunter Vol. 1', 3),
('Hunter x Hunter Vol. 2', 3),
('Hunter x Hunter Vol. 3', 2);
-- =================================================================================
INSERT INTO Genre (genre_name) VALUES
('Action'),         -- 1
('Adventure'),      -- 2
('Fantasy'),        -- 3
('Shoujo'),         -- 4
('Shounen'),        -- 5
('Horror'),         -- 6
('Supernatural'),   -- 7
('Comedy');         -- 8
-- =================================================================================
INSERT INTO Book_Author (book_id, author_id) VALUES
(1, 1), (2, 1), (3, 1),       -- Eiichiro Oda
(4, 2), (5, 2), (6, 2),       -- Masashi Kishimoto
(7, 3), (8, 3), (9, 3),       -- Akira Toriyama
(10, 4), (11, 4), (12, 4),    -- Naoko Takeuchi
(13, 5), (14, 5), (15, 5),    -- Hajime Isayama
(16, 6), (17, 6), (18, 6),    -- Tite Kubo
(19, 7), (20, 7), (21, 7),    -- Rumiko Takahashi
(22, 8), (23, 8), (24, 8),    -- Kentaro Miura
(25, 9), (26, 9), (27, 9),    -- Hiromu Arakawa
(28, 10), (29, 10), (30, 10); -- Yoshihiro Togashi
-- =================================================================================
INSERT INTO Book_Genre (book_id, genre_id) VALUES
-- One Piece (Adventure, Action, Shounen)
(1, 1), (1, 2), (1, 5),
(2, 1), (2, 2), (2, 5),
(3, 1), (3, 2), (3, 5),
-- Naruto (Action, Fantasy, Shounen)
(4, 1), (4, 3), (4, 5),
(5, 1), (5, 3), (5, 5),
(6, 1), (6, 3), (6, 5),
-- Dragon Ball (Action, Fantasy, Comedy)
(7, 1), (7, 3), (7, 8),
(8, 1), (8, 3), (8, 8),
(9, 1), (9, 3), (9, 8),
-- Sailor Moon (Fantasy, Shoujo, Supernatural)
(10, 3), (10, 4), (10, 7),
(11, 3), (11, 4), (11, 7),
(12, 3), (12, 4), (12, 7),
-- Attack on Titan (Action, Horror, Shounen)
(13, 1), (13, 5), (13, 6),
(14, 1), (14, 5), (14, 6),
(15, 1), (15, 5), (15, 6),
-- Bleach (Action, Supernatural, Shounen)
(16, 1), (16, 5), (16, 7),
(17, 1), (17, 5), (17, 7),
(18, 1), (18, 5), (18, 7),
-- Inuyasha (Fantasy, Shoujo, Supernatural)
(19, 3), (19, 4), (19, 7),
(20, 3), (20, 4), (20, 7),
(21, 3), (21, 4), (21, 7),
-- Berserk (Action, Horror, Fantasy)
(22, 1), (22, 3), (22, 6),
(23, 1), (23, 3), (23, 6),
(24, 1), (24, 3), (24, 6),
-- Fullmetal Alchemist (Action, Fantasy, Shounen)
(25, 1), (25, 3), (25, 5),
(26, 1), (26, 3), (26, 5),
(27, 1), (27, 3), (27, 5),
-- Hunter x Hunter (Action, Adventure, Shounen)
(28, 1), (28, 2), (28, 5),
(29, 1), (29, 2), (29, 5),
(30, 1), (30, 2), (30, 5);
-- =================================================================================
INSERT INTO Food (food_name, cost) VALUES
('Coffee', 2.00),
('Iced Tea', 1.75),
('Sandwich', 4.50),
('Muffin', 2.25),
('Bottled Water', 1.00);





