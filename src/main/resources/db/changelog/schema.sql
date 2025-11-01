-- ============================
-- Customer Table
-- ============================
CREATE TABLE customer (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(255) NOT NULL
);

-- ============================
-- Order Table
-- ============================
CREATE TABLE "order" (
                         id BIGSERIAL PRIMARY KEY,
                         description VARCHAR(255) NOT NULL,
                         customer_id BIGINT NOT NULL,
                         CONSTRAINT fk_order_customer FOREIGN KEY (customer_id)
                             REFERENCES customer (id)
                             ON DELETE CASCADE
);

-- ============================
-- Product Table
-- ============================
CREATE TABLE product (
                         id BIGSERIAL PRIMARY KEY,
                         description VARCHAR(255) NOT NULL
);

-- ============================
-- Order_Product Join Table
-- ============================
CREATE TABLE order_product (
                               order_id BIGINT NOT NULL,
                               product_id BIGINT NOT NULL,
                               PRIMARY KEY (order_id, product_id),
                               CONSTRAINT fk_order_product_order FOREIGN KEY (order_id)
                                   REFERENCES "order" (id)
                                   ON DELETE CASCADE,
                               CONSTRAINT fk_order_product_product FOREIGN KEY (product_id)
                                   REFERENCES product (id)
                                   ON DELETE CASCADE
);
