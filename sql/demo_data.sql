USE fulkoping_library;

-- Insert media types and get their IDs
INSERT INTO media_types (type_name, loan_period_days) VALUES
    ('CD', 10),
    ('DVD', 10),
    ('Blu-ray', 10),
    ('Vinyl', 10),
    ('Audiobook', 30),
    ('Book', 30),
    ('Magazine', 10);

-- Get media type IDs
SET @book_type_id = (SELECT media_type_id FROM media_types WHERE type_name = 'Book');
SET @magazine_type_id = (SELECT media_type_id FROM media_types WHERE type_name = 'Magazine');
SET @cd_type_id = (SELECT media_type_id FROM media_types WHERE type_name = 'CD');

-- Insert sample users with hashed passwords and salts
INSERT INTO users (username, password_hash, salt, name, email) VALUES
    ('alice', 'hashed_password_1', 'salt_1', 'Alice Johnson', 'alice@example.com'),
    ('bob', 'hashed_password_2', 'salt_2', 'Bob Smith', 'bob@example.com');

-- Insert sample books with media_type_id
INSERT INTO library_items (title, author, type, isbn, is_available, media_type_id) VALUES
    ('1984', 'George Orwell', 'BOOK', '9780451524935', TRUE, @book_type_id),
    ('To Kill a Mockingbird', 'Harper Lee', 'BOOK', '9780061120084', TRUE, @book_type_id),
    ('The Great Gatsby', 'F. Scott Fitzgerald', 'BOOK', '9780743273565', TRUE, @book_type_id),
    ('Pride and Prejudice', 'Jane Austen', 'BOOK', '9781503290564', TRUE, @book_type_id),
    ('The Alchemist', 'Paulo Coelho', 'BOOK', '9780062315007', TRUE, @book_type_id);

-- Insert sample magazines with media_type_id
INSERT INTO library_items (title, author, type, isbn, is_available, publisher, issn, media_type_id) VALUES
    ('National Geographic', 'National Geographic Society', 'MAGAZINE', NULL, TRUE, 'National Geographic Society', '0027-9358', @magazine_type_id),
    ('Time', 'Time USA, LLC', 'MAGAZINE', NULL, TRUE, 'Time USA, LLC', '0040-781X', @magazine_type_id),
    ('The New Yorker', 'Condé Nast', 'MAGAZINE', NULL, TRUE, 'Condé Nast', '0028-792X', @magazine_type_id),
    ('Vogue', 'Condé Nast', 'MAGAZINE', NULL, TRUE, 'Condé Nast', '0042-8000', @magazine_type_id),
    ('Wired', 'Condé Nast', 'MAGAZINE', NULL, TRUE, 'Condé Nast', '1059-1028', @magazine_type_id);

-- Insert sample media with media_type_id
INSERT INTO library_items (title, author, type, isbn, is_available, media_type_id) VALUES
    ('The Dark Knight', 'Christopher Nolan', 'MEDIA', NULL, TRUE, @cd_type_id),
    ('Inception', 'Christopher Nolan', 'MEDIA', NULL, TRUE, @cd_type_id),
    ('The Lord of the Rings: The Fellowship of the Ring', 'Peter Jackson', 'MEDIA', NULL, TRUE, @cd_type_id),
    ('The Matrix', 'The Wachowskis', 'MEDIA', NULL, TRUE, @cd_type_id),
    ('Interstellar', 'Christopher Nolan', 'MEDIA', NULL, TRUE, @cd_type_id);

-- Insert sample loans
INSERT INTO loans (user_id, item_id, loan_date, return_date) VALUES
    (1, 1, '2023-10-01', NULL),
    (2, 2, '2023-10-05', NULL);

-- Insert sample reservations
INSERT INTO reservations (user_id, item_id, reservation_date, expiry_date) VALUES
    (1, 3, '2023-10-10', '2023-11-09');
