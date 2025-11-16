-- V1__Create_initial_tables.sql

-- Tabela de Livros
CREATE TABLE books (
        id BIGSERIAL PRIMARY KEY,
        isbn VARCHAR(20) UNIQUE, -- Adicionar @Column(unique=true) na entidade
        title VARCHAR(255) NOT NULL, -- Adicionar @Column(nullable=false) na entidade
        author VARCHAR(255),
        publisher VARCHAR(255),
        description TEXT,
        genre VARCHAR(100),
        pages INTEGER,
        language VARCHAR(100), -- Aumentado
        image_url VARCHAR(1024) -- Nome da coluna padrão para imageUrl
);

-- Tabela de Usuários (NOME ALTERADO PARA CORRESPONDER @Table)
CREATE TABLE table_users (
        id BIGSERIAL PRIMARY KEY,
        username VARCHAR(50) UNIQUE NOT NULL, -- Adicionar @Column(unique=true, nullable=false) na entidade
        password VARCHAR(255) NOT NULL,
        email VARCHAR(100) UNIQUE NOT NULL, -- Adicionar @Column(unique=true, nullable=false) na entidade
        full_name VARCHAR(150)
);

-- Tabela de Perfis/Roles
CREATE TABLE roles (
        id BIGSERIAL PRIMARY KEY,
        name VARCHAR(50) UNIQUE NOT NULL
);

-- Tabela de Junção: Usuário <-> Role (Referenciando table_users)
CREATE TABLE user_roles (
        user_id BIGINT NOT NULL REFERENCES table_users(id), -- Referência atualizada
        role_id BIGINT NOT NULL REFERENCES roles(id),
        PRIMARY KEY (user_id, role_id)
);

-- Tabela de Junção: Usuário <-> Livro (Estante Pessoal - Referenciando table_users)
CREATE TABLE user_book (
        id BIGSERIAL PRIMARY KEY,
        user_id BIGINT NOT NULL REFERENCES table_users(id) ON DELETE CASCADE, -- Referência atualizada
        book_id BIGINT NOT NULL REFERENCES books(id),
--         reading_status VARCHAR(50) NOT NULL, -- Adicionar @Column(nullable=false) na entidade
        rating DOUBLE PRECISION,
        review TEXT,
        current_page INTEGER DEFAULT 0,
        is_favorite BOOLEAN DEFAULT false,
        UNIQUE (user_id, book_id) -- Adicionar @Table(uniqueConstraints=...) na entidade
);

-- Inserir Roles padrão
INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN') ON CONFLICT (name) DO NOTHING;