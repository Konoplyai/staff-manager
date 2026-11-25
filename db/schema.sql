-- Структура бази даних company_db (PostgreSQL)
-- Запуск: psql -U company_user -d company_db -f db/schema.sql

DROP TABLE IF EXISTS bonus_history;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS departments;

CREATE TABLE departments (
    id        SERIAL PRIMARY KEY,
    name      VARCHAR(100) NOT NULL UNIQUE,
    location  VARCHAR(100),
    phone     VARCHAR(20)
);

CREATE TABLE employees (
    id             SERIAL PRIMARY KEY,
    last_name      VARCHAR(50)   NOT NULL,
    first_name     VARCHAR(50)   NOT NULL,
    middle_name    VARCHAR(50),
    position       VARCHAR(100)  NOT NULL,
    salary         NUMERIC(10,2) NOT NULL CHECK (salary > 0),
    bonus          NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (bonus >= 0),
    hire_date      DATE          NOT NULL,
    email          VARCHAR(100)  UNIQUE,
    phone          VARCHAR(20),
    department_id  INTEGER REFERENCES departments(id) ON DELETE RESTRICT,
    manager_id     INTEGER REFERENCES employees(id) ON DELETE SET NULL
);

CREATE TABLE bonus_history (
    id            SERIAL PRIMARY KEY,
    employee_id   INTEGER       NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    percent       NUMERIC(5,2)  NOT NULL CHECK (percent > 0),
    amount        NUMERIC(10,2) NOT NULL CHECK (amount > 0),
    accrual_date  DATE          NOT NULL DEFAULT CURRENT_DATE
);

CREATE INDEX idx_employees_last_name ON employees (last_name);
CREATE INDEX idx_employees_department ON employees (department_id);
CREATE INDEX idx_bonus_employee ON bonus_history (employee_id);
