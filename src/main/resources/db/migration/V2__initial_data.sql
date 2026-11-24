INSERT INTO canteen_settings (id, name, service_fee, opens_at, closes_at, enforce_hours)
VALUES (1, 'University Canteen', 0, '07:00', '22:30', FALSE);

INSERT INTO products (name, description, price, stock_quantity) VALUES
    ('Chicken croquette', 'Chicken and cream cheese', 6.00, 20),
    ('Cheese bread', 'Large', 4.50, 30),
    ('Orange juice', 'Freshly squeezed, 300 ml', 7.00, 15),
    ('Coffee', 'Espresso', 3.00, 40),
    ('Chocolate cake cup', 'Chocolate', 8.00, 0);
