-- 1) USER table
CREATE TABLE `user` (
                        `id`            BINARY(16)    NOT NULL PRIMARY KEY,
                        `username`      VARCHAR(255)  NOT NULL UNIQUE,
                        `password`      VARCHAR(255)  NOT NULL,
                        `role`          VARCHAR(50)   NOT NULL,
                        `first_name`    VARCHAR(255),
                        `last_name`     VARCHAR(255),
                        `email`         VARCHAR(255),
                        `phone_number`  VARCHAR(50),
                        `street`        VARCHAR(255),
                        `street_no`     VARCHAR(50),
                        `plz`           VARCHAR(50),
                        `city`          VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2) RESTAURANT_INFO
CREATE TABLE `restaurant_info` (
                                   `id`               BINARY(16)    NOT NULL PRIMARY KEY,
                                   `name`             VARCHAR(255)  NOT NULL,
                                   `about_text`       LONGTEXT,
                                   `city`             VARCHAR(255),
                                   `plz`              VARCHAR(50),
                                   `street`           VARCHAR(255),
                                   `street_no`        VARCHAR(50),
                                   `email`            VARCHAR(255),
                                   `phone`            VARCHAR(50),
                                   `instagram_url`    VARCHAR(255),
                                   `google_maps_url`  VARCHAR(255),
                                   `opening_hours`    LONGTEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3) FOOD_ITEM
CREATE TABLE `food_item` (
                             `id`           BINARY(16)    NOT NULL PRIMARY KEY,
                             `name`         VARCHAR(255)  NOT NULL,
                             `description`  LONGTEXT,
                             `ingredients`  LONGTEXT,
                             `allergies`    LONGTEXT,
                             `image_url`    VARCHAR(512)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4) MENU_ITEM
CREATE TABLE `menu_item` (
                             `id`            BINARY(16)   NOT NULL PRIMARY KEY,
                             `food_item_id`  BINARY(16)   NOT NULL,
                             `category`      VARCHAR(50)  NOT NULL,
                             `available`     TINYINT(1)   NOT NULL,
                             `price`         DECIMAL(10,2) NOT NULL,
                             INDEX (`food_item_id`),
                             FOREIGN KEY (`food_item_id`) REFERENCES `food_item`(`id`)
                                 ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5) BUFFET_ITEM
CREATE TABLE `buffet_item` (
                               `id`            BINARY(16)   NOT NULL PRIMARY KEY,
                               `food_item_id`  BINARY(16)   NOT NULL,
                               `available`     TINYINT(1)   NOT NULL,
                               INDEX (`food_item_id`),
                               FOREIGN KEY (`food_item_id`) REFERENCES `food_item`(`id`)
                                   ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6) `order`
CREATE TABLE `order` (
                         `id`                  BINARY(16)   NOT NULL PRIMARY KEY,
                         `user_id`             BINARY(16),
                         `first_name`          VARCHAR(255),
                         `last_name`           VARCHAR(255),
                         `email`               VARCHAR(255),
                         `phone`               VARCHAR(50),
                         `street`              VARCHAR(255),
                         `street_no`           VARCHAR(50),
                         `plz`                 VARCHAR(50),
                         `city`                VARCHAR(255),
                         `order_type`          VARCHAR(50)  NOT NULL,
                         `special_instructions` LONGTEXT,
                         `status`              VARCHAR(50)  NOT NULL,
                         `created_at`          DATETIME     NOT NULL,
                         `deleted_at`          DATETIME,
                         INDEX (`user_id`),
                         FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7) ORDER_ITEM
CREATE TABLE `order_item` (
                              `id`             BINARY(16)   NOT NULL PRIMARY KEY,
                              `order_id`       BINARY(16)   NOT NULL,
                              `menu_item_id`   BINARY(16)   NOT NULL,
                              `quantity`       INT          NOT NULL,
                              INDEX (`order_id`),
                              INDEX (`menu_item_id`),
                              FOREIGN KEY (`order_id`)     REFERENCES `order`(`id`)       ON DELETE CASCADE ON UPDATE CASCADE,
                              FOREIGN KEY (`menu_item_id`) REFERENCES `menu_item`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8) BUFFET_ORDER
CREATE TABLE `buffet_order` (
                                `id`                   BINARY(16)   NOT NULL PRIMARY KEY,
                                `user_id`              BINARY(16),
                                `first_name`           VARCHAR(255),
                                `last_name`            VARCHAR(255),
                                `email`                VARCHAR(255),
                                `phone`                VARCHAR(50),
                                `street`               VARCHAR(255),
                                `street_no`            VARCHAR(50),
                                `plz`                  VARCHAR(50),
                                `city`                 VARCHAR(255),
                                `order_type`           VARCHAR(50)  NOT NULL,
                                `special_instructions` LONGTEXT,
                                `status`               VARCHAR(50)  NOT NULL,
                                `created_at`           DATETIME     NOT NULL,
                                INDEX (`user_id`),
                                FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9) BUFFET_ORDER_ITEM
CREATE TABLE `buffet_order_item` (
                                     `id`               BINARY(16)   NOT NULL PRIMARY KEY,
                                     `buffet_order_id`  BINARY(16)   NOT NULL,
                                     `buffet_item_id`   BINARY(16)   NOT NULL,
                                     `quantity`         INT          NOT NULL,
                                     INDEX (`buffet_order_id`),
                                     INDEX (`buffet_item_id`),
                                     FOREIGN KEY (`buffet_order_id`) REFERENCES `buffet_order`(`id`)
                                         ON DELETE CASCADE ON UPDATE CASCADE,
                                     FOREIGN KEY (`buffet_item_id`)  REFERENCES `buffet_item`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10) RESERVATION
CREATE TABLE `reservation` (
                               `id`                   BINARY(16)   NOT NULL PRIMARY KEY,
                               `user_id`              BINARY(16),
                               `first_name`           VARCHAR(255),
                               `last_name`            VARCHAR(255),
                               `email`                VARCHAR(255),
                               `phone`                VARCHAR(50),
                               `street`               VARCHAR(255),
                               `street_no`            VARCHAR(50),
                               `plz`                  VARCHAR(50),
                               `city`                 VARCHAR(255),
                               `reservation_date_time` DATETIME    NOT NULL,
                               `number_of_people`     INT          NOT NULL,
                               `special_requests`     LONGTEXT,
                               `status`               VARCHAR(50)  NOT NULL,
                               `created_at`           DATETIME     NOT NULL,
                               INDEX (`user_id`),
                               FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
