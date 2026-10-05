CREATE TABLE categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_categories_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE books (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(50) NOT NULL,
    available_quantity INT NOT NULL,
    category_id BIGINT NULL,
    version BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_books_isbn UNIQUE (isbn),
    CONSTRAINT fk_books_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT chk_books_available_quantity CHECK (available_quantity >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE members (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_code VARCHAR(50) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_members_code UNIQUE (member_code),
    CONSTRAINT uk_members_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE fine_policies (
    id BIGINT NOT NULL AUTO_INCREMENT,
    daily_fine_amount DECIMAL(12, 2) NOT NULL,
    max_fine_amount DECIMAL(12, 2) NULL,
    grace_days INT NOT NULL,
    is_active BIT(1) NOT NULL,
    created_at DATE NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE borrowings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    borrowed_date DATE NOT NULL,
    due_date DATE NOT NULL,
    returned_date DATE NULL,
    status VARCHAR(30) NOT NULL,
    total_fine_amount DECIMAL(12, 2) NOT NULL,
    paid_fine_amount DECIMAL(12, 2) NOT NULL,
    waived_fine_amount DECIMAL(12, 2) NOT NULL,
    unpaid_fine_amount DECIMAL(12, 2) NOT NULL,
    version BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_borrowings_member FOREIGN KEY (member_id) REFERENCES members (id),
    INDEX idx_borrowings_member_status (member_id, status),
    INDEX idx_borrowings_due_status (due_date, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE borrowing_details (
    id BIGINT NOT NULL AUTO_INCREMENT,
    borrowing_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    returned_quantity INT NOT NULL,
    last_returned_date DATE NULL,
    late_days INT NOT NULL,
    fine_amount DECIMAL(12, 2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_borrowing_book UNIQUE (borrowing_id, book_id),
    CONSTRAINT fk_details_borrowing FOREIGN KEY (borrowing_id) REFERENCES borrowings (id),
    CONSTRAINT fk_details_book FOREIGN KEY (book_id) REFERENCES books (id),
    CONSTRAINT chk_detail_quantity CHECK (quantity > 0 AND returned_quantity >= 0 AND returned_quantity <= quantity),
    INDEX idx_details_borrowing (borrowing_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE fine_payments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    borrowing_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    payment_date DATE NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    note VARCHAR(500) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_payments_borrowing FOREIGN KEY (borrowing_id) REFERENCES borrowings (id),
    INDEX idx_payments_borrowing (borrowing_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE fine_waivers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    borrowing_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    approved_by VARCHAR(100) NOT NULL,
    approved_date DATE NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_waivers_borrowing FOREIGN KEY (borrowing_id) REFERENCES borrowings (id),
    INDEX idx_waivers_borrowing (borrowing_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
