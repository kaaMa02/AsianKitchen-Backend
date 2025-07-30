-- V2__seed_reference_data.sql

-- 1) Default ADMIN user
INSERT INTO users (
    id,
    username,
    password,
    role,
    first_name,
    last_name,
    email,
    phone_number,
    street,
    street_no,
    plz,
    city
) VALUES (
             uuid_generate_v4(),
             'admin',
             '$2a$12$jbWObWExwdbhQ3BQvbB3tui0VBC3uotZEuV0RKKSl77lS2B5wenz.',  -- bcrypt("yourAdminPassword")
             'ADMIN',
             'Namgyal',
             'Choedon',
             'admin@asiankitchen.com',
             '0000000000',
             'HQ St.',
             '1',
             '12345',
             'Metropolis'
         );

-- 2) Default RestaurantInfo
INSERT INTO restaurant_info (
    id, name, about_text,
    street, street_no, plz, city,
    email, phone, instagram_url, google_maps_url,
    opening_hours, created_at
) VALUES (
             uuid_generate_v4(),
             'Asian Kitchen',
             'Welcome to Asian Kitchen …',
             'HQ St.', '1', '12345', 'Metropolis',
             'contact@asiankitchen.com',
             '+1234567890',
             'https://instagram.com/asiankitchen',
             'https://maps.google.com/?q=Asian+Kitchen',
             E'Mon–Fri: 11:00–22:00\nSat–Sun: 12:00–23:00',
             NOW()
         );