
-- Online Store Database System
-- Student: Hicham Ouikrim
-- Student ID: 312024704044
-- Suffix: 044 appended to all objects per course requirement


-- ============================================================
-- DROP EXISTING OBJECTS 
-- ============================================================
DROP TRIGGER IF EXISTS trg_low_stock_warning_044;
DROP TRIGGER IF EXISTS trg_auto_member_points_044;
DROP VIEW  IF EXISTS vw_cashier_products_044;
DROP VIEW  IF EXISTS vw_management_sales_stats_044;
DROP VIEW  IF EXISTS vw_member_discount_products_044;
DROP TABLE IF EXISTS points_records_044;
DROP TABLE IF EXISTS payments_044;
DROP TABLE IF EXISTS order_items_044;
DROP TABLE IF EXISTS orders_044;
DROP TABLE IF EXISTS inventory_044;
DROP TABLE IF EXISTS products_044;
DROP TABLE IF EXISTS customers_044;
DROP TABLE IF EXISTS suppliers_044;
DROP TABLE IF EXISTS low_stock_warnings_044;

-- ============================================================
-- TABLE 1: suppliers_044
-- ============================================================
CREATE TABLE suppliers_044 (
    supplier_id     INT             AUTO_INCREMENT PRIMARY KEY,
    supplier_name   VARCHAR(100)    NOT NULL,
    contact_person  VARCHAR(100),
    email           VARCHAR(150)    UNIQUE NOT NULL,
    phone           VARCHAR(20),
    address         VARCHAR(255),
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLE 2: customers_044
-- ============================================================
CREATE TABLE customers_044 (
    customer_id     INT             AUTO_INCREMENT PRIMARY KEY,
    first_name      VARCHAR(50)     NOT NULL,
    last_name       VARCHAR(50)     NOT NULL,
    email           VARCHAR(150)    UNIQUE NOT NULL,
    phone           VARCHAR(20),
    address         VARCHAR(255),
    membership_type ENUM('Regular','Silver','Gold','Platinum') DEFAULT 'Regular',
    total_points    INT             DEFAULT 0,
    points_rate     DECIMAL(5,2)    DEFAULT 1.00,
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLE 3: products_044
-- ============================================================
CREATE TABLE products_044 (
    product_id      INT             AUTO_INCREMENT PRIMARY KEY,
    supplier_id     INT,
    product_name    VARCHAR(150)    NOT NULL,
    description     TEXT,
    category        VARCHAR(80),
    purchase_price  DECIMAL(10,2)   NOT NULL,
    selling_price   DECIMAL(10,2)   NOT NULL,
    member_discount DECIMAL(5,2)    DEFAULT 0.00,   -- % discount for members
    status          ENUM('Available','Unavailable','Discontinued') DEFAULT 'Available',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_supplier_044
        FOREIGN KEY (supplier_id) REFERENCES suppliers_044(supplier_id)
        ON DELETE SET NULL ON UPDATE CASCADE
);

-- ============================================================
-- TABLE 4: inventory_044
-- ============================================================
CREATE TABLE inventory_044 (
    inventory_id        INT         AUTO_INCREMENT PRIMARY KEY,
    product_id          INT         NOT NULL UNIQUE,
    quantity_in_stock   INT         NOT NULL DEFAULT 0,
    reorder_threshold   INT         NOT NULL DEFAULT 10,
    last_updated        DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventory_product_044
        FOREIGN KEY (product_id) REFERENCES products_044(product_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- ============================================================
-- TABLE 5: orders_044
-- ============================================================
CREATE TABLE orders_044 (
    order_id        INT             AUTO_INCREMENT PRIMARY KEY,
    customer_id     INT             NOT NULL,
    order_date      DATETIME        DEFAULT CURRENT_TIMESTAMP,
    status          ENUM('Pending','Processing','Shipped','Completed','Cancelled') DEFAULT 'Pending',
    total_amount    DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    shipping_address VARCHAR(255),
    notes           TEXT,
    points_awarded  TINYINT(1)      DEFAULT 0,   -- 0 = not yet, 1 = awarded
    CONSTRAINT fk_order_customer_044
        FOREIGN KEY (customer_id) REFERENCES customers_044(customer_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

-- ============================================================
-- TABLE 6: order_items_044  (junction between orders and products)
-- ============================================================
CREATE TABLE order_items_044 (
    item_id         INT             AUTO_INCREMENT PRIMARY KEY,
    order_id        INT             NOT NULL,
    product_id      INT             NOT NULL,
    quantity        INT             NOT NULL CHECK (quantity > 0),
    unit_price      DECIMAL(10,2)   NOT NULL,
    discount_applied DECIMAL(5,2)   DEFAULT 0.00,
    line_total      DECIMAL(12,2)   GENERATED ALWAYS AS
                        (quantity * unit_price * (1 - discount_applied / 100)) STORED,
    CONSTRAINT fk_item_order_044
        FOREIGN KEY (order_id)   REFERENCES orders_044(order_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_item_product_044
        FOREIGN KEY (product_id) REFERENCES products_044(product_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

-- ============================================================
-- TABLE 7: payments_044
-- ============================================================
CREATE TABLE payments_044 (
    payment_id      INT             AUTO_INCREMENT PRIMARY KEY,
    order_id        INT             NOT NULL UNIQUE,
    payment_date    DATETIME        DEFAULT CURRENT_TIMESTAMP,
    amount          DECIMAL(12,2)   NOT NULL,
    payment_method  ENUM('Cash','Credit Card','Debit Card','Bank Transfer','Digital Wallet') NOT NULL,
    payment_status  ENUM('Pending','Completed','Failed','Refunded') DEFAULT 'Pending',
    transaction_ref VARCHAR(100),
    CONSTRAINT fk_payment_order_044
        FOREIGN KEY (order_id) REFERENCES orders_044(order_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

-- ============================================================
-- TABLE 8: points_records_044
-- ============================================================
CREATE TABLE points_records_044 (
    record_id       INT             AUTO_INCREMENT PRIMARY KEY,
    customer_id     INT             NOT NULL,
    order_id        INT             NOT NULL,
    points_earned   INT             NOT NULL,
    awarded_at      DATETIME        DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_points_customer_044
        FOREIGN KEY (customer_id) REFERENCES customers_044(customer_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_points_order_044
        FOREIGN KEY (order_id)    REFERENCES orders_044(order_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- ============================================================
-- TABLE 9: low_stock_warnings_044
-- ============================================================
CREATE TABLE low_stock_warnings_044 (
    warning_id      INT             AUTO_INCREMENT PRIMARY KEY,
    product_id      INT             NOT NULL,
    remaining_stock INT             NOT NULL,
    threshold       INT             NOT NULL,
    warned_at       DATETIME        DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_warning_product_044
        FOREIGN KEY (product_id) REFERENCES products_044(product_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- ============================================================
-- INDEXES (4 minimum required)
-- ============================================================

-- Index 1: Speed up order lookups by customer (JOIN-heavy)
CREATE INDEX idx_orders_customer_044      ON orders_044(customer_id);

-- Index 2: Speed up order item lookups by product (sales reporting)
CREATE INDEX idx_order_items_product_044  ON order_items_044(product_id);

-- Index 3: Speed up inventory lookups by product (JOIN with products)
CREATE INDEX idx_inventory_product_044    ON inventory_044(product_id);

-- Index 4: Speed up order status filtering (cashier/manager views)
CREATE INDEX idx_orders_status_044        ON orders_044(status);

-- Index 5 (bonus): Speed up product category queries (catalog browsing)
CREATE INDEX idx_products_category_044    ON products_044(category, status);

-- Index 6 (bonus): Speed up payment status lookups (finance reporting)
CREATE INDEX idx_payments_status_044      ON payments_044(payment_status);

-- ============================================================
-- VIEW 1: vw_cashier_products_044
-- Cashier-Facing: basic product info, available only,
-- no purchase price, no supplier details
-- ============================================================
CREATE VIEW vw_cashier_products_044 AS
SELECT
    p.product_id,
    p.product_name,
    p.category,
    p.selling_price,
    p.member_discount,
    i.quantity_in_stock
FROM products_044   p
JOIN inventory_044  i ON p.product_id = i.product_id
WHERE p.status = 'Available'
  AND i.quantity_in_stock > 0;

-- ============================================================
-- VIEW 2: vw_management_sales_stats_044
-- Management-Facing: aggregated sales per product, completed
-- orders only, sorted descending by revenue
-- ============================================================
CREATE VIEW vw_management_sales_stats_044 AS
SELECT
    p.product_id,
    p.product_name,
    p.category,
    SUM(oi.quantity)    AS total_units_sold,
    SUM(oi.line_total)  AS total_revenue,
    AVG(oi.unit_price)  AS avg_selling_price,
    COUNT(DISTINCT oi.order_id) AS num_orders
FROM products_044    p
JOIN order_items_044 oi ON p.product_id = oi.product_id
JOIN orders_044       o ON oi.order_id  = o.order_id
WHERE o.status = 'Completed'
GROUP BY p.product_id, p.product_name, p.category
ORDER BY total_revenue DESC;

-- ============================================================
-- VIEW 3: vw_member_discount_products_044
-- Member-Facing: products with active discounts, in-stock only,
-- no purchase price or supplier details
-- ============================================================
CREATE VIEW vw_member_discount_products_044 AS
SELECT
    p.product_id,
    p.product_name,
    p.category,
    p.selling_price,
    p.member_discount,
    ROUND(p.selling_price * (1 - p.member_discount / 100), 2) AS discounted_price,
    i.quantity_in_stock
FROM products_044   p
JOIN inventory_044  i ON p.product_id = i.product_id
WHERE p.status         = 'Available'
  AND p.member_discount > 0
  AND i.quantity_in_stock > 0;

-- ============================================================
-- TRIGGER 1: trg_low_stock_warning_044
-- After INSERT or UPDATE on order_items_044,
-- deduct inventory and log a warning if stock drops below threshold
-- ============================================================
DELIMITER $$

CREATE TRIGGER trg_low_stock_warning_044
AFTER INSERT ON order_items_044
FOR EACH ROW
BEGIN
    DECLARE v_remaining INT;
    DECLARE v_threshold INT;

    -- Deduct sold quantity from inventory
    UPDATE inventory_044
    SET quantity_in_stock = quantity_in_stock - NEW.quantity
    WHERE product_id = NEW.product_id;

    -- Check remaining stock and threshold
    SELECT quantity_in_stock, reorder_threshold
    INTO   v_remaining, v_threshold
    FROM   inventory_044
    WHERE  product_id = NEW.product_id;

    -- If below threshold, write a warning record
    IF v_remaining < v_threshold THEN
        INSERT INTO low_stock_warnings_044 (product_id, remaining_stock, threshold)
        VALUES (NEW.product_id, v_remaining, v_threshold);
    END IF;
END$$

-- ============================================================
-- TRIGGER 2: trg_auto_member_points_044
-- After UPDATE on orders_044, when status changes to 'Completed',
-- calculate and award member points exactly once
-- ============================================================
CREATE TRIGGER trg_auto_member_points_044
AFTER UPDATE ON orders_044
FOR EACH ROW
BEGIN
    DECLARE v_points     INT;
    DECLARE v_rate       DECIMAL(5,2);

    -- Only fire on transition TO 'Completed' and not yet awarded
    IF NEW.status = 'Completed'
       AND OLD.status <> 'Completed'
       AND NEW.points_awarded = 0
    THEN
        -- Get the customer's points rate
        SELECT points_rate INTO v_rate
        FROM   customers_044
        WHERE  customer_id = NEW.customer_id;

        -- Calculate points: floor(total_amount) * points_rate
        SET v_points = FLOOR(NEW.total_amount) * v_rate;

        -- Add points to customer balance
        UPDATE customers_044
        SET    total_points = total_points + v_points
        WHERE  customer_id  = NEW.customer_id;

        -- Insert into points_records
        INSERT INTO points_records_044 (customer_id, order_id, points_earned)
        VALUES (NEW.customer_id, NEW.order_id, v_points);

        -- Mark order as points-awarded (prevents duplicates)
        UPDATE orders_044
        SET    points_awarded = 1
        WHERE  order_id = NEW.order_id;
    END IF;
END$$

DELIMITER ;

-- ============================================================
-- SAMPLE DATA: suppliers_044
-- ============================================================
INSERT INTO suppliers_044 (supplier_name, contact_person, email, phone, address) VALUES
('TechSource Global',  'James Wilson',  'james@techsource.com',  '+1-555-0101', '100 Tech Blvd, San Jose, CA'),
('FreshGoods Ltd',     'Maria Chen',    'maria@freshgoods.com',  '+1-555-0202', '250 Fresh Ave, Austin, TX'),
('HomeStyle Inc',      'Robert Kim',    'robert@homestyle.com',  '+1-555-0303', '77 Home St, Chicago, IL'),
('SportZone Supply',   'Laura Davis',   'laura@sportzone.com',   '+1-555-0404', '500 Sport Dr, Miami, FL'),
('BookWorld Dist',     'Ahmed Hassan',  'ahmed@bookworld.com',   '+1-555-0505', '12 Library Ln, Boston, MA');

-- ============================================================
-- SAMPLE DATA: customers_044
-- ============================================================
INSERT INTO customers_044 (first_name, last_name, email, phone, address, membership_type, total_points, points_rate) VALUES
('Alice',   'Johnson', 'alice@email.com',   '+1-555-1001', '10 Oak St, NY',      'Gold',     1500, 1.50),
('Bob',     'Martinez','bob@email.com',     '+1-555-1002', '20 Elm St, LA',      'Silver',    800, 1.25),
('Carol',   'White',   'carol@email.com',   '+1-555-1003', '30 Pine Ave, TX',    'Regular',   200, 1.00),
('David',   'Brown',   'david@email.com',   '+1-555-1004', '40 Maple Rd, IL',    'Platinum', 5000, 2.00),
('Eva',     'Lee',     'eva@email.com',     '+1-555-1005', '50 Cedar Blvd, FL',  'Regular',   100, 1.00),
('Frank',   'Garcia',  'frank@email.com',   '+1-555-1006', '60 Walnut Dr, WA',   'Silver',    600, 1.25),
('Grace',   'Taylor',  'grace@email.com',   '+1-555-1007', '70 Birch Ln, OR',    'Gold',     2200, 1.50),
('Henry',   'Wilson',  'henry@email.com',   '+1-555-1008', '80 Aspen Way, CO',   'Regular',     0, 1.00);

-- ============================================================
-- SAMPLE DATA: products_044
-- ============================================================
INSERT INTO products_044 (supplier_id, product_name, description, category, purchase_price, selling_price, member_discount, status) VALUES
(1, 'Laptop Pro 15',       '15-inch laptop, Intel i7, 16GB RAM',       'Electronics',  600.00,  999.99, 10.00, 'Available'),
(1, 'Wireless Headphones', 'Noise-cancelling Bluetooth headphones',     'Electronics',   45.00,   89.99,  5.00, 'Available'),
(1, 'USB-C Hub 7-Port',    '7-port USB-C hub with power delivery',      'Electronics',   12.00,   29.99,  0.00, 'Available'),
(2, 'Organic Coffee Beans','1kg arabica roast, single origin',          'Food',           8.00,   18.99,  8.00, 'Available'),
(2, 'Green Tea Pack',      '50-bag assorted green tea',                 'Food',           3.00,    9.99,  5.00, 'Available'),
(3, 'Ergonomic Chair',     'Mesh back, lumbar support, adjustable',     'Furniture',    120.00,  249.99, 12.00, 'Available'),
(3, 'Standing Desk',       '120x60cm electric height-adjustable desk',  'Furniture',    200.00,  399.99,  0.00, 'Available'),
(4, 'Running Shoes X9',    'Lightweight trail running shoes, unisex',   'Sports',        40.00,   89.99, 15.00, 'Available'),
(4, 'Yoga Mat Premium',    '6mm non-slip eco-friendly yoga mat',        'Sports',        10.00,   34.99,  0.00, 'Available'),
(5, 'Database Systems',    'Textbook: Database Systems by Ramakrishnan','Books',          20.00,   49.99,  0.00, 'Available'),
(5, 'Clean Code',          'A Handbook of Agile Software Craftsmanship','Books',          15.00,   39.99,  0.00, 'Available'),
(1, 'Webcam HD 1080p',     '1080p webcam with built-in microphone',     'Electronics',   18.00,   49.99,  5.00, 'Unavailable');

-- ============================================================
-- SAMPLE DATA: inventory_044
-- ============================================================
INSERT INTO inventory_044 (product_id, quantity_in_stock, reorder_threshold) VALUES
(1,  25,  5),
(2,  80, 10),
(3, 150, 20),
(4,  40, 10),
(5, 200, 15),
(6,  15,  3),
(7,   8,  2),
(8,  60, 10),
(9, 100, 15),
(10,  30,  5),
(11,  45,  5),
(12,   0, 10);

-- ============================================================
-- SAMPLE DATA: orders_044
-- ============================================================
INSERT INTO orders_044 (customer_id, status, total_amount, shipping_address, notes) VALUES
(1, 'Completed', 1089.98, '10 Oak St, NY',      'Leave at door'),
(2, 'Completed',  124.98, '20 Elm St, LA',      NULL),
(3, 'Processing',  59.99, '30 Pine Ave, TX',    'Gift wrap please'),
(4, 'Completed',  649.98, '40 Maple Rd, IL',    NULL),
(5, 'Pending',     44.98, '50 Cedar Blvd, FL',  NULL),
(1, 'Shipped',    399.99, '10 Oak St, NY',      'Expedited shipping'),
(6, 'Completed',  179.98, '60 Walnut Dr, WA',   NULL),
(7, 'Cancelled',   89.99, '70 Birch Ln, OR',    'Changed mind'),
(8, 'Pending',     89.99, '80 Aspen Way, CO',   NULL),
(2, 'Completed',  284.97, '20 Elm St, LA',      'Birthday order');

-- ============================================================
-- SAMPLE DATA: order_items_044
-- ============================================================
INSERT INTO order_items_044 (order_id, product_id, quantity, unit_price, discount_applied) VALUES
-- Order 1: Laptop + Headphones (Gold member 10%+5% discount)
(1, 1, 1, 999.99, 10.00),
(1, 2, 1,  89.99,  5.00),
-- Order 2: Coffee + Green Tea (Silver 8%+5%)
(2, 4, 2,  18.99,  8.00),
(2, 5, 3,   9.99,  5.00),
-- Order 3: Running Shoes (no member discount for Regular)
(3, 8, 1,  89.99,  0.00),
-- Order 4: Chair + USB Hub (Platinum 12%+0%)
(4, 6, 1, 249.99, 12.00),
(4, 3, 5,  29.99,  0.00),
-- Order 5: Tea + Coffee
(5, 5, 2,   9.99,  0.00),
(5, 4, 1,  18.99,  0.00),
-- Order 6: Standing Desk
(6, 7, 1, 399.99,  0.00),
-- Order 7: Chair (Silver 12%)
(7, 6, 1, 249.99, 12.00),
(7, 2, 1,  89.99,  5.00),
-- Order 8: Running Shoes
(8, 8, 1,  89.99,  0.00),
-- Order 9: Yoga Mat + Running Shoes
(9, 9, 1,  34.99,  0.00),
(9, 8, 1,  89.99, 15.00),
-- Order 10: Books (Silver no discount)
(10, 10, 2, 49.99, 0.00),
(10, 11, 3, 39.99, 0.00);

-- ============================================================
-- SAMPLE DATA: payments_044
-- ============================================================
INSERT INTO payments_044 (order_id, amount, payment_method, payment_status, transaction_ref) VALUES
(1,  1089.98, 'Credit Card',    'Completed', 'TXN-20260101-001'),
(2,   124.98, 'Digital Wallet', 'Completed', 'TXN-20260102-002'),
(3,    59.99, 'Cash',           'Pending',   NULL),
(4,   649.98, 'Bank Transfer',  'Completed', 'TXN-20260103-003'),
(5,    44.98, 'Credit Card',    'Pending',   NULL),
(6,   399.99, 'Debit Card',     'Completed', 'TXN-20260104-004'),
(7,   179.98, 'Credit Card',    'Completed', 'TXN-20260105-005'),
(8,    89.99, 'Cash',           'Refunded',  'TXN-20260106-006'),
(9,    89.99, 'Digital Wallet', 'Pending',   NULL),
(10,  284.97, 'Credit Card',    'Completed', 'TXN-20260107-007');

-- Mark completed orders as having awarded points already
-- (simulating trigger having already run on these)
UPDATE orders_044 SET points_awarded = 1
WHERE status = 'Completed';

-- ============================================================
-- SINGLE-TABLE QUERIES (10 required)
-- ============================================================

-- Q1: List all available products sorted by selling price descending
SELECT product_id, product_name, category, selling_price, status
FROM products_044
WHERE status = 'Available'
ORDER BY selling_price DESC;

-- Q2: Count customers by membership type
SELECT membership_type, COUNT(*) AS customer_count,
       AVG(total_points) AS avg_points
FROM customers_044
GROUP BY membership_type
ORDER BY avg_points DESC;

-- Q3: Find products with member discounts > 5%
SELECT product_name, selling_price, member_discount,
       ROUND(selling_price * (1 - member_discount/100), 2) AS member_price
FROM products_044
WHERE member_discount > 5 AND status = 'Available';

-- Q4: Show inventory items below reorder threshold
SELECT product_id, quantity_in_stock, reorder_threshold,
       (reorder_threshold - quantity_in_stock) AS units_short
FROM inventory_044
WHERE quantity_in_stock < reorder_threshold
ORDER BY units_short DESC;

-- Q5: Count orders by status
SELECT status, COUNT(*) AS order_count,
       SUM(total_amount) AS total_value
FROM orders_044
GROUP BY status;

-- Q6: Find top 3 customers by total points
SELECT customer_id, CONCAT(first_name,' ',last_name) AS full_name,
       membership_type, total_points
FROM customers_044
ORDER BY total_points DESC
LIMIT 3;

-- Q7: Show all payments with method breakdown
SELECT payment_method, COUNT(*) AS num_payments,
       SUM(amount) AS total_collected,
       AVG(amount) AS avg_payment
FROM payments_044
WHERE payment_status = 'Completed'
GROUP BY payment_method
ORDER BY total_collected DESC;

-- Q8: List products by category with average selling price
SELECT category, COUNT(*) AS product_count,
       ROUND(AVG(selling_price),2) AS avg_price,
       MIN(selling_price) AS min_price,
       MAX(selling_price) AS max_price
FROM products_044
GROUP BY category
ORDER BY avg_price DESC;

-- Q9: Show all orders placed in 2026
SELECT order_id, customer_id, order_date, status, total_amount
FROM orders_044
WHERE YEAR(order_date) = 2026
ORDER BY order_date DESC;

-- Q10: Find customers who have earned more than 500 points
SELECT customer_id, CONCAT(first_name,' ',last_name) AS full_name,
       email, total_points, membership_type
FROM customers_044
WHERE total_points > 500
ORDER BY total_points DESC;

-- ============================================================
-- MULTI-TABLE QUERIES (5 required: JOIN, GROUP BY, subqueries)
-- ============================================================

-- Q11: Full order details with customer name and payment status
SELECT o.order_id,
       CONCAT(c.first_name,' ',c.last_name) AS customer_name,
       c.membership_type,
       o.order_date,
       o.status         AS order_status,
       o.total_amount,
       p.payment_method,
       p.payment_status
FROM orders_044   o
JOIN customers_044 c ON o.customer_id = c.customer_id
LEFT JOIN payments_044 p ON o.order_id = p.order_id
ORDER BY o.order_date DESC;

-- Q12: Revenue per product including product name and supplier
SELECT pr.product_name,
       s.supplier_name,
       SUM(oi.quantity)   AS units_sold,
       SUM(oi.line_total) AS revenue
FROM order_items_044 oi
JOIN products_044     pr ON oi.product_id  = pr.product_id
JOIN suppliers_044     s ON pr.supplier_id = s.supplier_id
JOIN orders_044        o ON oi.order_id    = o.order_id
WHERE o.status = 'Completed'
GROUP BY pr.product_id, pr.product_name, s.supplier_name
ORDER BY revenue DESC;

-- Q13: Customers who have placed more than 1 order (subquery)
SELECT c.customer_id,
       CONCAT(c.first_name,' ',c.last_name) AS full_name,
       c.membership_type,
       order_counts.num_orders,
       order_counts.total_spent
FROM customers_044 c
JOIN (
    SELECT customer_id,
           COUNT(*)          AS num_orders,
           SUM(total_amount) AS total_spent
    FROM orders_044
    GROUP BY customer_id
    HAVING COUNT(*) > 1
) AS order_counts ON c.customer_id = order_counts.customer_id
ORDER BY order_counts.total_spent DESC;

-- Q14: Low stock products with supplier contact info
SELECT pr.product_name,
       pr.category,
       i.quantity_in_stock,
       i.reorder_threshold,
       s.supplier_name,
       s.contact_person,
       s.email AS supplier_email,
       s.phone AS supplier_phone
FROM inventory_044 i
JOIN products_044   pr ON i.product_id  = pr.product_id
LEFT JOIN suppliers_044 s ON pr.supplier_id = s.supplier_id
WHERE i.quantity_in_stock <= i.reorder_threshold
ORDER BY i.quantity_in_stock ASC;

-- Q15: Monthly sales summary with order count and revenue (GROUP BY with JOIN)
SELECT DATE_FORMAT(o.order_date, '%Y-%m') AS sales_month,
       COUNT(DISTINCT o.order_id)          AS num_orders,
       COUNT(DISTINCT o.customer_id)       AS unique_customers,
       SUM(oi.line_total)                  AS total_revenue,
       ROUND(AVG(o.total_amount), 2)       AS avg_order_value
FROM orders_044      o
JOIN order_items_044 oi ON o.order_id = oi.order_id
WHERE o.status IN ('Completed','Shipped','Processing')
GROUP BY DATE_FORMAT(o.order_date, '%Y-%m')
ORDER BY sales_month DESC;

-- ============================================================
-- CRUD DEMONSTRATIONS
-- ============================================================

-- CREATE: Add a new product
INSERT INTO products_044 (supplier_id, product_name, description, category, purchase_price, selling_price, member_discount, status)
VALUES (1, 'Smart Watch Series 5', 'GPS fitness tracker, heart rate monitor', 'Electronics', 80.00, 179.99, 10.00, 'Available');

-- CREATE: Add inventory entry for the new product
INSERT INTO inventory_044 (product_id, quantity_in_stock, reorder_threshold)
VALUES (LAST_INSERT_ID(), 50, 8);

-- READ: Retrieve a specific customer profile
SELECT c.*, COUNT(o.order_id) AS total_orders, SUM(o.total_amount) AS lifetime_value
FROM customers_044 c
LEFT JOIN orders_044 o ON c.customer_id = o.customer_id
WHERE c.customer_id = 1
GROUP BY c.customer_id;

-- UPDATE: Upgrade customer membership based on points
UPDATE customers_044
SET membership_type = 'Platinum'
WHERE total_points >= 4000 AND membership_type != 'Platinum';

-- UPDATE: Mark a pending order as Processing
UPDATE orders_044
SET status = 'Processing'
WHERE order_id = 5 AND status = 'Pending';

-- UPDATE: Mark order as Completed (triggers points award automatically)
UPDATE orders_044
SET status = 'Completed'
WHERE order_id = 5;

-- DELETE: Remove a cancelled order's items and the order itself
DELETE FROM order_items_044 WHERE order_id = 8;
DELETE FROM orders_044      WHERE order_id = 8 AND status IN ('Cancelled','Pending');

-- ============================================================
-- VERIFY VIEWS
-- ============================================================
SELECT * FROM vw_cashier_products_044;
SELECT * FROM vw_management_sales_stats_044;
SELECT * FROM vw_member_discount_products_044;

-- VERIFY points records
SELECT pr.*, CONCAT(c.first_name,' ',c.last_name) AS customer_name
FROM points_records_044 pr
JOIN customers_044 c ON pr.customer_id = c.customer_id;

-- VERIFY low stock warnings
SELECT w.*, p.product_name
FROM low_stock_warnings_044 w
JOIN products_044 p ON w.product_id = p.product_id;
