USE fulkoping_library;

-- Insert media types and get their IDs
INSERT IGNORE INTO media_types (type_name, loan_period_days) VALUES
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

-- Insert default roles
INSERT INTO roles (role_name) VALUES
    ('USER'),
    ('ADMIN');

-- Insert sample users with hashed passwords and salts
INSERT INTO users (username, password_hash, salt, name, email) VALUES
    ('alice', 'eJgdIJt0Vcr++PuAu2EQJnycpxisQ0tp3tjXhqp12Ew=', 'D7nMu06E6f6GO786+027Vw==', 'Alice Johnson', 'alice@example.com'),
    ('bob', 't+7WJGIpjUEMxMpGqoZjDNKpAYpUXnXbdnNqx4Y0agE=', 'sjn/a5lOoEN8Nmrkml93uQ==', 'Bob Smith', 'bob@example.com');

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id) VALUES
    ((SELECT user_id FROM users WHERE username = 'alice'), (SELECT role_id FROM roles WHERE role_name = 'USER')),
    ((SELECT user_id FROM users WHERE username = 'bob'), (SELECT role_id FROM roles WHERE role_name = 'ADMIN'));

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

-- Insert sample media with media_type_id and directors
INSERT INTO library_items (title, author, type, isbn, is_available, media_type_id, director) VALUES
    ('The Dark Knight', 'Christopher Nolan', 'MEDIA', NULL, TRUE, @cd_type_id, 'Christopher Nolan'),
    ('Inception', 'Christopher Nolan', 'MEDIA', NULL, TRUE, @cd_type_id, 'Christopher Nolan'),
    ('The Lord of the Rings: The Fellowship of the Ring', 'Peter Jackson', 'MEDIA', NULL, TRUE, @cd_type_id, 'Peter Jackson'),
    ('The Matrix', 'The Wachowskis', 'MEDIA', NULL, TRUE, @cd_type_id, 'Lana Wachowski, Lilly Wachowski'),
    ('Interstellar', 'Christopher Nolan', 'MEDIA', NULL, TRUE, @cd_type_id, 'Christopher Nolan');

-- Insert sample loans
INSERT INTO loans (user_id, item_id, loan_date, return_date) VALUES
    (1, 1, '2025-02-01', NULL),
    (2, 2, '2025-02-05', NULL);

-- Insert sample reservations
INSERT INTO reservations (user_id, item_id, reservation_date, expiry_date) VALUES
    (1, 3, '2025-02-10', '2025-03-09');
