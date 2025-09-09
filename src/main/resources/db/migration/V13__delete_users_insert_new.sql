DELETE from users;

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
             '$2a$12$G0iGcdMQJr8Lw9LT4n2eBOyPGJy8rhTquEPwcJaRzImVmOUb1Dj9e',
             'ADMIN',
             'Namgyal Chodon',
             'Chopatsang',
             'asian.kitchen84@gmail.com',
             '+41 78 705 82 31',
             'Baslerstrasse',
             '16',
             '4632',
             'Trimbach',
             NOW()
         );