CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1) USER
CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       username VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(50) NOT NULL,
                       first_name VARCHAR(255),
                       last_name VARCHAR(255),
                       email VARCHAR(255),
                       phone_number VARCHAR(50),
                       street VARCHAR(255),
                       street_no VARCHAR(50),
                       plz VARCHAR(50),
                       city VARCHAR(255)
);

-- 2) RESTAURANT_INFO
CREATE TABLE restaurant_info (
                                 id UUID PRIMARY KEY,
                                 name VARCHAR(255) NOT NULL,
                                 about_text TEXT,
                                 street VARCHAR(255),
                                 street_no VARCHAR(50),
                                 plz VARCHAR(50),
                                 city VARCHAR(255),
                                 email VARCHAR(255),
                                 phone VARCHAR(50),
                                 instagram_url VARCHAR(255),
                                 google_maps_url VARCHAR(255),
                                 opening_hours TEXT,
                                 created_at TIMESTAMP NOT NULL DEFAULT NOW()
);


-- 3) FOOD_ITEM
CREATE TABLE food_item (
                           id UUID PRIMARY KEY,
                           name VARCHAR(255) NOT NULL,
                           description TEXT,
                           ingredients TEXT,
                           allergies TEXT,
                           image_url VARCHAR(512)
);

-- 4) MENU_ITEM
CREATE TABLE menu_item (
                           id UUID PRIMARY KEY,
                           food_item_id UUID NOT NULL REFERENCES food_item(id) ON DELETE CASCADE ON UPDATE CASCADE,
                           category VARCHAR(50) NOT NULL,
                           available BOOLEAN NOT NULL,
                           price DECIMAL(10, 2) NOT NULL
);

-- 5) BUFFET_ITEM
CREATE TABLE buffet_item (
                             id UUID PRIMARY KEY,
                             food_item_id UUID NOT NULL REFERENCES food_item(id) ON DELETE CASCADE ON UPDATE CASCADE,
                             available BOOLEAN NOT NULL,
                             price DECIMAL(10,2) NOT NULL DEFAULT 0
);

-- 6) ORDER
CREATE TABLE orders (
                        id UUID PRIMARY KEY,
                        user_id UUID REFERENCES users(id),
                        first_name VARCHAR(255), last_name VARCHAR(255),
                        email VARCHAR(255), phone VARCHAR(50),
                        street VARCHAR(255), street_no VARCHAR(50),
                        plz VARCHAR(50), city VARCHAR(255),
                        order_type VARCHAR(50) NOT NULL,
                        special_instructions TEXT,
                        status VARCHAR(50) NOT NULL,
                        total_price DECIMAL(10,2) NOT NULL DEFAULT 0,
                        created_at TIMESTAMP NOT NULL,
                        deleted_at TIMESTAMP
);

-- 7) ORDER_ITEM
CREATE TABLE order_item (
                            id UUID PRIMARY KEY,
                            order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE ON UPDATE CASCADE,
                            menu_item_id UUID NOT NULL REFERENCES menu_item(id),
                            quantity INT NOT NULL
);

-- 8) BUFFET_ORDER
CREATE TABLE buffet_order (
                              id UUID PRIMARY KEY,
                              user_id UUID REFERENCES users(id),
                              first_name VARCHAR(255),
                              last_name VARCHAR(255),
                              email VARCHAR(255),
                              phone VARCHAR(50),
                              street VARCHAR(255),
                              street_no VARCHAR(50),
                              plz VARCHAR(50),
                              city VARCHAR(255),
                              order_type VARCHAR(50) NOT NULL,
                              special_instructions TEXT,
                              status VARCHAR(50) NOT NULL,
                              total_price DECIMAL(10,2) NOT NULL DEFAULT 0,
                              created_at TIMESTAMP NOT NULL
);

-- 9) BUFFET_ORDER_ITEM
CREATE TABLE buffet_order_item (
                                   id UUID PRIMARY KEY,
                                   buffet_order_id UUID NOT NULL REFERENCES buffet_order(id) ON DELETE CASCADE ON UPDATE CASCADE,
                                   buffet_item_id UUID NOT NULL REFERENCES buffet_item(id),
                                   quantity INT NOT NULL
);

-- 10) RESERVATION
CREATE TABLE reservation (
                             id UUID PRIMARY KEY,
                             user_id UUID REFERENCES users(id),
                             first_name VARCHAR(255),
                             last_name VARCHAR(255),
                             email VARCHAR(255),
                             phone VARCHAR(50),
                             street VARCHAR(255),
                             street_no VARCHAR(50),
                             plz VARCHAR(50),
                             city VARCHAR(255),
                             reservation_date_time TIMESTAMP NOT NULL,
                             number_of_people INT NOT NULL,
                             special_requests TEXT,
                             status VARCHAR(50) NOT NULL,
                             created_at TIMESTAMP NOT NULL
);
