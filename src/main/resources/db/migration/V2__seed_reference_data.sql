--- 1) Default ADMIN user (password already BCrypt‐hashed; replace with your desired hash)
INSERT INTO `user` (
    id,
    username,
    password,
    role,
    first_name,
    last_name,
    email,
    phone_number,
    city,
    plz,
    street,
    street_no
) VALUES (
             UUID_TO_BIN(UUID()),
             'admin',
             '$2a$12$jbWObWExwdbhQ3BQvbB3tui0VBC3uotZEuV0RKKSl77lS2B5wenz.',  -- bcrypt("yourAdminPassword")
             'ADMIN',
             'Namgyal',
             'Choedon',
             'admin@asiankitchen.com',
             '0000000000',
             'Metropolis',
             '12345',
             'HQ St.',
             '1'
         );

-- 2) Default RestaurantInfo
INSERT INTO `restaurant_info` (
    id,
    name,
    about_text,
    city,
    plz,
    street,
    street_no,
    email,
    phone,
    instagram_url,
    google_maps_url,
    opening_hours
) VALUES (
             UUID_TO_BIN(UUID()),
             'Asian Kitchen',
             'Welcome to Asian Kitchen — your go-to spot for authentic Asian flavors, crafted with love and the freshest ingredients.',
             'Metropolis',
             '12345',
             'HQ St.',
             '1',
             'contact@asiankitchen.com',
             '+1234567890',
             'https://instagram.com/asiankitchen',
             'https://maps.google.com/?q=Asian+Kitchen',
             'Mon–Fri: 11:00–22:00\nSat–Sun: 12:00–23:00'
         );
