------------------------------------------------------------------
---CREATE TABLE----------------------------------------------
------------------------------------------------------------------

---Authors Table--------------------------------------------------
CREATE TABLE Authors(
    author_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL
);

---Books Table-----------------------------------------------------
CREATE TABLE Books(
    book_id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    author_id INTEGER,
    ISBN TEXT UNIQUE,
    publication_date TEXT,
    available_copies INTEGER DEFAULT 1,
    FOREIGN KEY (author_id) REFERENCES Authors (author_id)
);

---members Table--------------------------------------------------
CREATE TABLE members (
    member_id INTEGER PRIMARY KEY AUTOINCREMENT,
    member_name TEXT NOT NULL
);

---users Table----------------------------------------------------
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL,
    password TEXT NOT NULL
);

---BorrowedBooks Table--------------------------------------------
CREATE TABLE BorrowedBooks (
    borrow_id INTEGER PRIMARY KEY AUTOINCREMENT,
    member_id INTEGER,
    book_id INTEGER,
    borrow_date TEXT,
    return_date TEXT,
    FOREIGN KEY (member_id) REFERENCES members (member_id),
    FOREIGN KEY (book_id) REFERENCES Books (book_id),
    UNIQUE (member_id, book_id)
);

------------------------------------------------------------------
---SEQUENCE TRIGGER-----------------------------------------------
------------------------------------------------------------------

---Author Sequence Trigger----------------------------------------
CREATE TRIGGER reset_authors_sequence 
AFTER DELETE ON Authors
BEGIN
    UPDATE 
        sqlite_sequence 
    SET 
        seq = (SELECT MAX(author_id) FROM Authors) 
    WHERE 
        name = 'Authors';
END;

---Book Sequence Trigger------------------------------------------
CREATE TRIGGER reset_books_sequence 
AFTER DELETE ON Books
BEGIN
    UPDATE 
        sqlite_sequence 
    SET 
        seq = (SELECT MAX(book_id) FROM Books) 
    WHERE 
        name = 'Books';
END;

---Author(No Books) Sequence Trigger-------------------------------
CREATE TRIGGER delete_author_if_no_books_after_delete
AFTER DELETE ON Books
FOR EACH ROW
BEGIN
    DELETE FROM 
        Authors
    WHERE 
        author_id = OLD.author_id
    AND NOT EXISTS 
        (SELECT 1 FROM Books WHERE author_id = OLD.author_id);
END;

---Member Sequence Trigger-----------------------------------------
CREATE TRIGGER reset_members_sequence 
AFTER DELETE ON members
BEGIN
    UPDATE 
        sqlite_sequence 
    SET 
        seq = (SELECT MAX(member_id) FROM members) 
    WHERE name = 'members';
END;

---User Sequence Trigger-------------------------------------------
CREATE TRIGGER reset_users_sequence AFTER DELETE ON users
BEGIN
    UPDATE sqlite_sequence SET seq = (SELECT MAX(id) FROM users) WHERE name = 'users';
END;

---BorrowedBook Sequence Trigger-----------------------------------
CREATE TRIGGER reset_borrowed_books_sequence 
AFTER DELETE ON BorrowedBooks
BEGIN
    UPDATE 
        sqlite_sequence 
    SET 
        seq = (SELECT MAX(borrow_id) FROM BorrowedBooks) 
    WHERE name = 'BorrowedBooks';
END;

DELETE FROM Authors;
DELETE FROM Books;
DELETE FROM BorrowedBooks;

------------------------------------------------------------------
---INSERT INTO----------------------------------------------------
------------------------------------------------------------------

---Insert authors--------------------------------------------------
INSERT INTO Authors (name) VALUES
('J.K. Rowling'), 
('George R.R. Martin'), 
('J.R.R. Tolkien'), 
('Agatha Christie'), 
('Dan Brown'), 
('Stephen King'), 
('Haruki Murakami'), 
('Isaac Asimov'), 
('Arthur C. Clarke'), 
('Jane Austen'), 
('Charles Dickens'), 
('Mark Twain'), 
('Ernest Hemingway'), 
('Leo Tolstoy'), 
('F. Scott Fitzgerald'),
('Margaret Atwood'),
('Neil Gaiman'),
('Kurt Vonnegut'),
('Ray Bradbury'),
('Toni Morrison'),
('Virginia Woolf'),
('Gabriel Garcia Marquez'),
('John Steinbeck'),
('H.G. Wells'),
('Philip K. Dick'),
('Chuck Palahniuk'),
('Cormac McCarthy'),
('Zadie Smith'),
('Alice Walker'),
('David Mitchell'),
('Salman Rushdie'),
('Michael Ende'),
('E.L. James'),
('Stephen Hawking'),
('Colson Whitehead'),
('Danielle Steel'),
('John Grisham'),
('Nora Roberts'),
('Elena Ferrante'),
('Octavia Butler'),
('C.S. Lewis'),
('Roald Dahl'),
('Louisa May Alcott'),
('D.H. Lawrence'),
('Friedrich Nietzsche'),
('Herman Melville'),
('Jack Kerouac'),
('Philip Roth'),
('Maya Angelou'),
('Ralph Ellison'),
('J.D. Salinger'),
('George Orwell'),
('Mary Shelley'),
('Edgar Allan Poe'),
('Anne Rice'),
('David Foster Wallace'),
('Gillian Flynn'),
('Yaa Gyasi'),
('Chimamanda Ngozi Adichie'),
('Liane Moriarty'),
('Khaled Hosseini'),
('Tess Gerritsen'),
('Richard Adams'),
('Stephen Chbosky'),
('Willa Cather'),
('Ken Follett'),
('John le Carré');

---Insert books with valid author_id-------------------------------
INSERT INTO Books (title, author_id, ISBN, publication_date, available_copies) VALUES
('Harry Potter and the Sorcerers Stone', 1, '978-0-590-35340-3', '1997-06-26', 10),
('Harry Potter and the Chamber of Secrets', 1, '978-0-590-35341-0', '1998-07-02', 8),
('Harry Potter and the Prisoner of Azkaban', 1, '978-0-590-35342-7', '1999-07-08', 6),
('Harry Potter and the Goblet of Fire', 1, '978-0-590-35343-4', '2000-07-08', 5),
('Harry Potter and the Order of the Phoenix', 1, '978-0-590-35344-1', '2003-06-21', 4),
('Harry Potter and the Half-Blood Prince', 1, '978-0-590-35345-8', '2005-07-16', 3),
('Harry Potter and the Deathly Hallows', 1, '978-0-590-35346-5', '2007-07-21', 2),
('A Game of Thrones', 2, '978-0-553-89784-5', '1996-08-06', 5),
('A Clash of Kings', 2, '978-0-553-89785-2', '1998-11-16', 4),
('A Storm of Swords', 2, '978-0-553-89786-9', '2000-03-21', 3),
('A Feast for Crows', 2, '978-0-553-89787-6', '2005-10-17', 2),
('A Dance with Dragons', 2, '978-0-553-89788-3', '2011-07-12', 1),
('The Hobbit', 3, '978-0-618-00221-3', '1937-09-21', 8),
('The Fellowship of the Ring', 3, '978-0-618-00222-0', '1954-07-29', 5),
('The Two Towers', 3, '978-0-618-00223-7', '1954-11-11', 4),
('The Return of the King', 3, '978-0-618-00224-4', '1955-10-20', 3),
('Murder on the Orient Express', 4, '978-0-06-207350-1', '1934-01-01', 6),
('And Then There Were None', 4, '978-0-06-207351-8', '1939-11-06', 5),
('The ABC Murders', 4, '978-0-06-207352-5', '1936-01-01', 4),
('The Da Vinci Code', 5, '978-0-385-50420-1', '2003-03-18', 12),
('Angels & Demons', 5, '978-0-7432-1777-3', '2000-05-01', 10),
('The Lost Symbol', 5, '978-0-385-50421-8', '2009-09-15', 8),
('The Shining', 6, '978-0-385-00967-8', '1977-01-28', 4),
('It', 6, '978-0-670-81302-4', '1986-09-15', 6),
('Misery', 6, '978-0-450-09772-6', '1987-06-08', 5),
('Norwegian Wood', 7, '978-0-375-71123-2', '1987-09-04', 9),
('Kafka on the Shore', 7, '978-0-06-083866-2', '2002-09-12', 7),
('Foundation', 8, '978-0-553-80371-0', '1951-06-01', 7),
('Foundation and Empire', 8, '978-0-553-80372-7', '1952-05-01', 5),
('Second Foundation', 8, '978-0-553-80373-4', '1953-06-01', 4),
('2001: A Space Odyssey', 9, '978-0-451-52851-2', '1968-06-01', 5),
('Rendezvous with Rama', 9, '978-0-451-52852-9', '1973-06-01', 6),
('Pride and Prejudice', 10, '978-1-85326-000-1', '1813-01-28', 15),
('Sense and Sensibility', 10, '978-1-85326-001-8', '1811-10-30', 10),
('Oliver Twist', 11, '978-0-14-143974-7', '1837-02-01', 7),
('A Christmas Carol', 11, '978-0-14-143957-0', '1843-12-19', 6),
('The Adventures of Huckleberry Finn', 12, '978-0-14-243717-9', '1884-12-10', 6);

INSERT INTO Books (title, author_id, ISBN, publication_date, available_copies) VALUES
('The Prince and the Pauper', 12, '978-0-14-243705-6', '1881-01-01', 5),
('The Old Man and the Sea', 13, '978-0-68-480122-3', '1952-09-01', 4),
('For Whom the Bell Tolls', 13, '978-0-68-480121-6', '1940-10-21', 3),
('War and Peace', 14, '978-0-14-044793-4', '1869-03-01', 3),
('Anna Karenina', 14, '978-0-14-303500-8', '1877-04-01', 2),
('The Great Gatsby', 15, '978-0-7432-7356-5', '1925-04-10', 10),
('Tender is the Night', 15, '978-0-7432-7357-2', '1934-04-12', 5),
('The Handmaids Tale', 16, '978-0-385-49081-7', '1985-04-17', 8),
('Oryx and Crake', 16, '978-0-385-48918-7', '2003-09-24', 6),
('American Gods', 17, '978-0-06-055812-7', '2001-06-19', 5),
('Coraline', 17, '978-0-06-055812-0', '2002-07-01', 4),
('Slaughterhouse-Five', 18, '978-0-380-78698-0', '1969-03-31', 6),
('Cats Cradle', 18, '978-0-380-78697-3', '1963-01-01', 5),
('Fahrenheit 451', 19, '978-1-4516-7331-9', '1953-10-19', 7),
('The Martian Chronicles', 19, '978-0-345-40716-5', '1950-09-01', 4);

INSERT INTO Books (title, author_id, ISBN, publication_date, available_copies) VALUES
('Beloved', 20, '978-1-4000-3349-6', '1987-09-16', 4),
('Song of Solomon', 20, '978-0-452-27888-9', '1977-09-01', 3),
('To the Lighthouse', 21, '978-0-15-671865-8', '1927-05-05', 3),
('Mrs. Dalloway', 21, '978-0-15-671870-2', '1925-05-14', 2),
('One Hundred Years of Solitude', 22, '978-0-06-088328-8', '1967-05-30', 9),
('Love in the Time of Cholera', 22, '978-0-06-088328-9', '1985-03-01', 5),
('The Grapes of Wrath', 23, '978-0-14-303943-6', '1939-04-14', 5),
('East of Eden', 23, '978-0-14-303943-7', '1952-09-19', 4),
('The Time Machine', 24, '978-0-451-52877-3', '1895-01-01', 8),
('The Invisible Man', 24, '978-0-451-52888-9', '1897-01-01', 7),
('Brave New World', 25, '978-0-06-085052-4', '1932-09-01', 7),
('The Catcher in the Rye', 26, '978-0-316-76948-1', '1951-07-16', 6);

INSERT INTO Books (title, author_id, ISBN, publication_date, available_copies) VALUES
('The Outsiders', 27, '978-0-14-038572-4', '1967-04-24', 9),
('The Book Thief', 28, '978-0-375-83100-3', '2005-09-01', 5),
('The Hunger Games', 29, '978-0-439-02352-9', '2008-09-14', 10),
('Divergent', 30, '978-0-06-202402-3', '2011-04-25', 4),
('Twilight', 31, '978-0-316-16017-6', '2005-10-05', 7),
('The Fault in Our Stars', 32, '978-0-525-47881-3', '2012-01-10', 6),
('The Maze Runner', 33, '978-0-385-74022-9', '2009-10-06', 5),
('The Perks of Being a Wallflower', 34, '978-0-671-02756-4', '1999-01-01', 8),
('1984', 35, '978-0-452-28423-5', '1949-06-08', 12),
('To Kill a Mockingbird', 36, '978-0-06-112008-5', '1960-07-11', 15),
('The Secret Garden', 37, '978-0-06-440188-4', '1911-01-01', 4),
('The Chronicles of Narnia', 38, '978-0-06-440942-2', '1950-10-16', 6),
('The Little Prince', 39, '978-0-15-601219-6', '1943-04-06', 5),
('Wuthering Heights', 40, '978-0-14-143955-7', '1847-12-01', 8);

INSERT INTO Books (title, author_id, ISBN, publication_date, available_copies) VALUES
('Frankenstein', 41, '978-0-452-28423-9', '1818-01-01', 7),
('Dracula', 42, '978-0-14-143984-5', '1897-05-26', 3),
('The Picture of Dorian Gray', 43, '978-0-14-143957-1', '1890-06-01', 2),
('The Great Gatsby', 44, '978-0-7432-7356-8', '1925-04-10', 10),
('The Grapes of Wrath', 45, '978-0-14-303943-9', '1939-04-14', 5),
('The Shining', 46, '978-0-451-52851-4', '1977-01-28', 4),
('The Outsiders', 47, '978-0-14-038572-7', '1967-04-24', 5),
('Little Women', 48, '978-0-14-303990-7', '1868-09-30', 7),
('The Fault in Our Stars', 49, '978-0-525-47881-5', '2012-01-10', 8),
('The Catcher in the Rye', 50, '978-0-316-76948-3', '1951-07-16', 9),
('War and Peace', 51, '978-0-14-044793-2', '1869-03-01', 7),
('The Hitchhikers Guide to the Galaxy', 54, '978-0-345-39180-6', '1979-10-12', 6);

INSERT INTO Books (title, author_id, ISBN, publication_date, available_copies) VALUES
('The Subtle Knife', 55, '978-0-440-23824-2', '1997-07-02', 7),
('The Amber Spyglass', 56, '978-0-440-23825-9', '2000-10-21', 5),
('The Handmaids Tale', 57, '978-0-385-51767-7', '1985-04-17', 8),
('Oryx and Crake', 58, '978-0-385-72101-7', '2003-09-24', 6),
('American Gods', 59, '978-0-380-97814-6', '2001-06-19', 5),
('Coraline', 60, '978-0-380-73468-6', '2002-07-01', 4),
('Slaughterhouse-Five', 61, '978-0-8129-7383-4', '1969-03-31', 6),
('Cats Cradle', 62, '978-0-380-02143-9', '1963-01-01', 5),
('Fahrenheit 451', 63, '978-1-4516-7331-0', '1953-10-19', 7),
('The Martian Chronicles', 64, '978-0-385-33019-1', '1950-09-01', 4),
('Beloved', 65, '978-1-4000-3349-9', '1987-09-16', 4),
('Song of Solomon', 66, '978-0-88001-317-2', '1977-09-01', 3);




DELETE FROM Books;

DELETE FROM Authors;

---Members Data-----------------------------------------------------
INSERT INTO members (member_name) VALUES
('John Doe'),
('Jane Smith'),
('Mike Johnson'),
('Emily Davis'),
('Chris Brown'),
('Laura Wilson'),
('David Moore'),
('Sarah Taylor'),
('Daniel Harris'),
('Jessica Lee'),
('Matthew Clark'),
('Samantha Lewis'),
('William Young'),
('Olivia Walker'),
('James Hall');

---Users Data-------------------------------------------------------
INSERT INTO users (username, password) VALUES
('admin', '123'),
('eron', '123'),
('paul', '123'),
('zio', '123'),
('jules', '123');

---BorrowedBooks Data------------------------------------------------
INSERT INTO BorrowedBooks (member_id, book_id, borrow_date, return_date) VALUES
(1, 1, '2023-01-15', '2023-02-15'),
(1, 8, '2023-01-20', '2023-02-20'),
(2, 3, '2023-02-01', '2023-03-01'),
(2, 10, '2023-02-10', '2023-03-10'),
(3, 5, '2023-01-25', '2023-02-25'),
(3, 18, '2023-02-15', '2023-03-15'),
(4, 6, '2023-01-30', '2023-02-28'),
(4, 22, '2023-02-05', '2023-03-05'),
(5, 12, '2023-01-10', '2023-02-10'),
(5, 19, '2023-02-12', '2023-03-12'),
(6, 2, '2023-01-18', '2023-02-18'),
(6, 20, '2023-02-20', '2023-03-20'),
(7, 15, '2023-01-22', '2023-02-22'),
(7, 25, '2023-02-02', '2023-03-02'),
(8, 4, '2023-01-28', '2023-02-28'),
(8, 16, '2023-02-14', '2023-03-14'),
(9, 14, '2023-01-12', '2023-02-12'),
(9, 11, '2023-02-03', '2023-03-03'),
(10, 17, '2023-01-05', '2023-02-05'),
(10, 21, '2023-02-08', '2023-03-08'),
(1, 9, '2023-01-19', '2023-02-19'),
(2, 13, '2023-01-15', '2023-02-15'),
(3, 7, '2023-01-25', '2023-02-25'),
(4, 23, '2023-01-30', '2023-02-28'),
(5, 24, '2023-01-18', '2023-02-18'),
(6, 26, '2023-02-01', '2023-03-01'),
(7, 27, '2023-01-22', '2023-02-22'),
(8, 28, '2023-02-10', '2023-03-10'),
(9, 29, '2023-01-12', '2023-02-12'),
(10, 30, '2023-02-05', '2023-03-05'),
(1, 31, '2023-01-15', '2023-02-15'),
(2, 32, '2023-01-20', '2023-02-20');

------------------------------------------------------------------
---VIEW-----------------------------------------------------------
------------------------------------------------------------------

--View for displaying member details with borrowed books, borrow dates, and return dates
CREATE VIEW member_details_view AS
SELECT 
    m.member_id, 
    m.member_name, 
    GROUP_CONCAT(b.title, ', ') AS borrowed_books,
    GROUP_CONCAT(bb.borrow_date, ', ') AS borrow_dates,
    GROUP_CONCAT(bb.return_date, ', ') AS return_dates
FROM 
    members m
LEFT JOIN 
    BorrowedBooks bb 
ON 
    m.member_id = bb.member_id
LEFT JOIN 
    Books b 
ON 
    bb.book_id = b.book_id
GROUP BY 
    m.member_id, m.member_name;
