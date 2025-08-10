DELETE FROM users;

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
    city,
    created_at
) VALUES (
          uuid_generate_v4(),
          'admin',
          '$2a$12$JJWXLH0jPZYn.zQyXpvWBeput1W1m9afx5aKupyHIDLkyBbthRqfS',
          'ADMIN',
          'Namgyal Chodon',
          'Chopatsang',
          'asian.kitchen84@gmail.com',
          '+41 78 705 82 31',
          NULL,
          NULL,
          NULL,
          NULL,
          NOW()
      );

DELETE FROM public.restaurant_info
WHERE id='d12f8f44-f7b8-43de-845f-233ae13e8ecf'::uuid;

INSERT INTO restaurant_info (
    id, name, about_text,
    street, street_no, plz, city,
    email, phone, instagram_url, google_maps_url,
    opening_hours, created_at
) VALUES (
             uuid_generate_v4(),
             'Asian Kitchen',
             'Welcome to our cozy corner of flavor, where every dish is served with a side of love! We’re a husband-and-wife team who enjoys crafting vibrant sushi rolls and fragrant Thai specialties. Our dream is simple: to share our joy for this work and wrap our guests in the warmth of our kitchen and give you a place where the food feels as genuine as the people behind it. Come join us at the table—we can’t wait to welcome you into our family!',
             'HQ St.', '1', '12345', 'Metropolis',
             'asian.kitchen84@gmail.com',
             '+41 78 705 82 31',
             'https://instagram.com/asiankitchen',
             'https://maps.google.com/?q=Asian+Kitchen',
             E'Tuesday – Sunday: 11:00 – 14:00\nTuesday – Thursday: 17:00 – 22:00\nfriday - saturday: 17:00 – 23:00\nSunday: 16:30 - 21:00',
             NOW()
         );