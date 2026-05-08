CREATE TABLE repairs(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    description VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    entry_date TIMESTAMP NOT NULL,
    exit_date TIMESTAMP,
    cost DECIMAL(10, 2),
    vehicle_id BIGINT NOT NULL,
    mechanic_id BIGINT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_repairs_vehicles FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    CONSTRAINT fk_repairs_users FOREIGN KEY (mechanic_id) REFERENCES users(id)
);