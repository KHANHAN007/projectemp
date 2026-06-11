CREATE TABLE roles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(30) NOT NULL UNIQUE,
  description VARCHAR(255)
);

CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  phone_number VARCHAR(20) NOT NULL UNIQUE,
  email VARCHAR(120) NOT NULL UNIQUE,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  kyc BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE kyc_profiles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  id_number VARCHAR(30) NOT NULL UNIQUE,
  full_name VARCHAR(120) NOT NULL,
  dob DATE NOT NULL,
  sex VARCHAR(20) NOT NULL,
  address VARCHAR(255) NOT NULL,
  id_card_front_url VARCHAR(500) NOT NULL,
  status VARCHAR(20) NOT NULL,
  rejection_reason VARCHAR(500),
  verified_by BIGINT,
  verified_at DATETIME(6),
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  CONSTRAINT fk_kyc_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_kyc_verifier FOREIGN KEY (verified_by) REFERENCES users(id)
);

CREATE TABLE accounts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  account_number VARCHAR(30) NOT NULL UNIQUE,
  balance DECIMAL(19,2) NOT NULL DEFAULT 0,
  currency VARCHAR(3) NOT NULL DEFAULT 'VND',
  transaction_pin VARCHAR(100) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  version BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT chk_account_balance CHECK (balance >= 0)
);

CREATE TABLE transactions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  transaction_code VARCHAR(40) NOT NULL UNIQUE,
  from_account_id BIGINT NOT NULL,
  to_account_id BIGINT,
  beneficiary_bank VARCHAR(120),
  beneficiary_account VARCHAR(50),
  amount DECIMAL(19,2) NOT NULL,
  description VARCHAR(255),
  type VARCHAR(30) NOT NULL,
  status VARCHAR(30) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  CONSTRAINT fk_tx_from FOREIGN KEY (from_account_id) REFERENCES accounts(id),
  CONSTRAINT fk_tx_to FOREIGN KEY (to_account_id) REFERENCES accounts(id),
  CONSTRAINT chk_tx_amount CHECK (amount > 0)
);
CREATE INDEX idx_tx_from_created ON transactions(from_account_id, created_at);
CREATE INDEX idx_tx_to_created ON transactions(to_account_id, created_at);

CREATE TABLE refresh_tokens (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  token_hash VARCHAR(64) NOT NULL UNIQUE,
  expires_at DATETIME(6) NOT NULL,
  revoked BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME(6) NOT NULL,
  CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users(id)
);

INSERT INTO roles(name, description) VALUES
('ADMIN', 'System administrator'),
('STAFF', 'Bank staff'),
('CUSTOMER', 'Bank customer');
