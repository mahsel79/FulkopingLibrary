USE fulkoping_library;

-- Insert sample users with hashed passwords and salts
INSERT INTO users (username, password_hash, salt, name, email) VALUES
                                                                   ('alice', 'hashed_password_1', 'salt_1', 'Alice Johnson', 'alice@example.com'),
                                                                   ('bob', 'hashed_password_2', 'salt_2', 'Bob Smith', 'bob@example.com');

-- Insert sample books into library_items
INSERT INTO library_items (title, author, type, isbn, is_available) VALUES
                                                                        ('1984', 'George Orwell', 'BOOK', '9780451524935', TRUE),
                                                                        ('To Kill a Mockingbird', 'Harper Lee', 'BOOK', '9780061120084', TRUE),
                                                                        ('The Great Gatsby', 'F. Scott Fitzgerald', 'BOOK', '9780743273565', TRUE),
                                                                        ('Pride and Prejudice', 'Jane Austen', 'BOOK', '9781503290564', TRUE),
                                                                        ('The Alchemist', 'Paulo Coelho', 'BOOK', '9780062315007', TRUE);

-- Insert sample magazines into library_items
INSERT INTO library_items (title, author, type, isbn, is_available, publisher, issn) VALUES
                                                                                         ('National Geographic', 'National Geographic Society', 'MAGAZINE', NULL, TRUE, 'National Geographic Society', '0027-9358'),
                                                                                         ('Time', 'Time USA, LLC', 'MAGAZINE', NULL, TRUE, 'Time USA, LLC', '0040-781X'),
                                                                                         ('The New Yorker', 'Condé Nast', 'MAGAZINE', NULL, TRUE, 'Condé Nast', '0028-792X'),
                                                                                         ('Vogue', 'Condé Nast', 'MAGAZINE', NULL, TRUE, 'Condé Nast', '0042-8000'),
                                                                                         ('Wired', 'Condé Nast', 'MAGAZINE', NULL, TRUE, 'Condé Nast', '1059-1028');

-- Insert sample media into library_items
INSERT INTO library_items (title, author, type, isbn, is_available) VALUES
                                                                        ('The Dark Knight', 'Christopher Nolan', 'MEDIA', NULL, TRUE),
                                                                        ('Inception', 'Christopher Nolan', 'MEDIA', NULL, TRUE),
                                                                        ('The Lord of the Rings: The Fellowship of the Ring', 'Peter Jackson', 'MEDIA', NULL, TRUE),
                                                                        ('The Matrix', 'The Wachowskis', 'MEDIA', NULL, TRUE),
                                                                        ('Interstellar', 'Christopher Nolan', 'MEDIA', NULL, TRUE);

-- Insert sample loans
INSERT INTO loans (user_id, item_id, loan_date, return_date) VALUES
                                                                 (1, 1, '2023-10-01', NULL),
                                                                 (2, 2, '2023-10-05', NULL);

-- Insert sample reservations
INSERT INTO reservations (user_id, item_id, reservation_date, expiry_date) VALUES
    (1, 3, '2023-10-10', '2023-11-09');