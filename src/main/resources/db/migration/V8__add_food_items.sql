-- V20__seed_menu_items.sql
-- Seed menu items (food_item + menu_item)
-- Requires Postgres + pgcrypto
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ------------- helper: upsert food items by (name) -------------
-- If you already have a unique index on food_item(name) this will be safe to re-run.
-- If not, consider adding one: CREATE UNIQUE INDEX IF NOT EXISTS ux_food_item_name ON food_item(name);

-- -------------------- SUSHI STARTER --------------------
WITH fi AS (
    SELECT * FROM (VALUES
                       ('MISO SUPPE', 'Mix aus Brühen, Tofu, Edamame, Gemüse'),
                       ('EDAMAME', 'Sojabohnen mit Meersalz'),
                       ('WAKAME SALAT', 'Algen'),
                       ('CHICKEN KARAGE', 'Poulet'),
                       ('SPICY TUNA TATAR', 'Thunfisch, Lauch, Spicy Mayo Sauce, Sesam'),
                       ('SALMON TATAR', 'Lachs, Philadelphia Käse, Avocado, Sesam'),
                       ('SALMON SHASHIMI 5Stk', 'Lachs'),
                       ('SASHIMI DUO', 'Thun 5x, Lachs 5x'),
                       ('TUNA SHASHIMI 5Stk', 'Thun')
                  ) t(name, description)
),
     ins AS (
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url)
SELECT gen_random_uuid(), name, description, NULL, NULL, NULL FROM fi
    ON CONFLICT (name) DO NOTHING
  RETURNING id, name
)
INSERT INTO menu_item (id, food_item_id, category, available, price)
SELECT gen_random_uuid(), fi2.id, 'SUSHI_STARTER', TRUE, src.price
FROM (VALUES
          ('MISO SUPPE', 6.00),
          ('EDAMAME', 7.50),
          ('WAKAME SALAT', 7.50),
          ('CHICKEN KARAGE', 12.00),
          ('SPICY TUNA TATAR', 13.00),
          ('SALMON TATAR', 12.00),
          ('SALMON SHASHIMI 5Stk', 11.00),
          ('SASHIMI DUO', 21.00),
          ('TUNA SHASHIMI 5Stk', 12.00)
     ) AS src(name, price)
         JOIN (
    SELECT id, name FROM ins
    UNION ALL
    SELECT id, name FROM food_item
) fi2 ON fi2.name = src.name
    ON CONFLICT DO NOTHING;

-- -------------------- SUSHI ROLLS --------------------
WITH fi AS (
    SELECT * FROM (VALUES
                       ('SPICY TUNA ROLL 4Stk', 'Thunfisch, Gurke, Lauch, Spicy Mayo'),
                       ('CALIFORNIA ROLL 4Stk', 'Surimi, Premium Snow Crab Meat, Gurke, Avocado, Mayo, Tobiko'),
                       ('CRUNCHY PRAWN ROLL 4Stk', 'Crevetten, Röstzwiebel, Mayo, Avocado, Aal Sauce'),
                       ('THOMMOUSSE URAMAKI 4Stk', 'Thonmousse, Mayo, Sesam, Gurke'),
                       ('COCOS SALMON ROLL 4Stk', 'Lachs, Cocosnusscreme, Avocado, Mango'),
                       ('FLAMED SALMON ROLL 4Stk', 'Lachs, Spicy Mayo, Avocado'),
                       ('DRAGON UNAGI ROLL 4Stk', 'Unagi, Avocado, Gurke, Aal Sauce'),
                       ('RAINBOW TUNA ROLL 4Stk', 'Thunfisch, Avocado'),
                       ('RAINBOW SALMON ROLL 4Stk', 'Lachs, Avocado'),
                       ('RAINBOW SALMON MANGO 4Stk', 'Lachs, Avocado, Mango'),
                       ('FRIED SALMON FUTOMAKI 4Stk', NULL),
                       ('TEMPURA FUTOMAKI 4Stk', 'Ebi Tempura, Avocado, Philadelphia Käse, Gurke, Eisbergsalat, Karotten, Aal Sauce'),
                       ('CHICKEN KATSU ROLL 4Stk', 'Poulet Katsu, Spicy Mayo, Avocado, Gurke, Eisbergsalat, Karotten'),
                       ('VEGI RAINBOW ROLL 4Stk', 'Avocado, Mango'),
                       ('COCOS VEGI ROLL 4Stk', 'Cocos, Avocado, Mango, Inari'),
                       ('VEGI FUTOMAKI 4Stk', NULL),
                       ('CHEFS CHOICE VEGI ROLL 4Stk', 'The best of our Chefs Choice')
                  ) t(name, description)
),
     ins AS (
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url)
SELECT gen_random_uuid(), name, description, NULL, NULL, NULL FROM fi
    ON CONFLICT (name) DO NOTHING
  RETURNING id, name
)
INSERT INTO menu_item (id, food_item_id, category, available, price)
SELECT gen_random_uuid(), fi2.id, 'SUSHI_ROLLS', TRUE, src.price
FROM (VALUES
          ('SPICY TUNA ROLL 4Stk', 8.00),
          ('CALIFORNIA ROLL 4Stk', 8.00),
          ('CRUNCHY PRAWN ROLL 4Stk', 8.00),
          ('THOMMOUSSE URAMAKI 4Stk', 7.50),
          ('COCOS SALMON ROLL 4Stk', 8.00),
          ('FLAMED SALMON ROLL 4Stk', 9.00),
          ('DRAGON UNAGI ROLL 4Stk', 9.00),
          ('RAINBOW TUNA ROLL 4Stk', 9.00),
          ('RAINBOW SALMON ROLL 4Stk', 9.00),
          ('RAINBOW SALMON MANGO 4Stk', 9.00),
          ('FRIED SALMON FUTOMAKI 4Stk', 12.00),
          ('TEMPURA FUTOMAKI 4Stk', 10.00),
          ('CHICKEN KATSU ROLL 4Stk', 11.00),
          ('VEGI RAINBOW ROLL 4Stk', 8.50),
          ('COCOS VEGI ROLL 4Stk', 7.00),
          ('VEGI FUTOMAKI 4Stk', 9.00),
          ('CHEFS CHOICE VEGI ROLL 4Stk', 9.00)
     ) AS src(name, price)
         JOIN (
    SELECT id, name FROM ins
    UNION ALL
    SELECT id, name FROM food_item
) fi2 ON fi2.name = src.name
    ON CONFLICT DO NOTHING;

-- -------------------- HOSO MAKI --------------------
WITH fi AS (
    SELECT * FROM (VALUES
                       ('SALMON MAKI 4Stk', 'Lachs'),
                       ('SALMON AVO MAKI 4Stk', 'Lachs, Avocado'),
                       ('TUNA MAKI 4Stk', 'Thunfisch'),
                       ('TUNA AVO MAKI 4Stk', 'Thunfisch, Avocado'),
                       ('THONMOUSSE MAKI 4Stk', 'Thunfisch, Gurke'),
                       ('Veg OSHINKO MAKI 4Stk', 'Eingelegter Rettich'),
                       ('Veg AVO MAKI 4Stk', 'Avocado'),
                       ('Veg- KAPPA MAKI 4Stk', 'Gurke'),
                       ('Veg- TAMAGO MAKI 4Stk', 'Tamago (Omelette)'),
                       ('Veg- KÜRBIS MAKI 4Stk', 'Kürbis'),
                       ('Veg- MANGO MAKI 4Stk', 'Mango')
                  ) t(name, description)
),
     ins AS (
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url)
SELECT gen_random_uuid(), name, description, NULL, NULL, NULL FROM fi
    ON CONFLICT (name) DO NOTHING
  RETURNING id, name
)
INSERT INTO menu_item (id, food_item_id, category, available, price)
SELECT gen_random_uuid(), fi2.id, 'HOSO_MAKI', TRUE, src.price
FROM (VALUES
          ('SALMON MAKI 4Stk', 4.50),
          ('SALMON AVO MAKI 4Stk', 4.50),
          ('TUNA MAKI 4Stk', 4.50),
          ('TUNA AVO MAKI 4Stk', 4.50),
          ('THONMOUSSE MAKI 4Stk', 4.50),
          ('Veg OSHINKO MAKI 4Stk', 4.00),
          ('Veg AVO MAKI 4Stk', 4.00),
          ('Veg- KAPPA MAKI 4Stk', 4.00),
          ('Veg- TAMAGO MAKI 4Stk', 4.00),
          ('Veg- KÜRBIS MAKI 4Stk', 4.00),
          ('Veg- MANGO MAKI 4Stk', 4.00)
     ) AS src(name, price)
         JOIN (
    SELECT id, name FROM ins
    UNION ALL
    SELECT id, name FROM food_item
) fi2 ON fi2.name = src.name
    ON CONFLICT DO NOTHING;

-- -------------------- NIGIRI --------------------
WITH fi AS (
    SELECT * FROM (VALUES
                       ('UNAGI NIGIRI 1Stk', 'Aal'),
                       ('TUNA NIGIRI 1Stk', 'Thunfisch'),
                       ('SALMON NIGIRI 1Stk', 'Lachs'),
                       ('TAMAGO NIGIRI 1Stk', 'Omelette'),
                       ('INARI 2Stk', 'Tofutasche'),
                       ('EBI NIGIRI 1Stk', 'Crevette'),
                       ('THONMOUSSE GUNKAN 1Stk', 'Thunfisch'),
                       ('IKURA GUNKAN 1Stk', 'Lachskaviar'),
                       ('ABURI 1Stk', 'Flambierter Lachs'),
                       ('AVO NIGIRI 1Stk', 'Avocado'),
                       ('SPECIAL NIGIRI 2Stk', 'Tuna/Salmon/Ebi')
                  ) t(name, description)
),
     ins AS (
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url)
SELECT gen_random_uuid(), name, description, NULL, NULL, NULL FROM fi
    ON CONFLICT (name) DO NOTHING
  RETURNING id, name
)
INSERT INTO menu_item (id, food_item_id, category, available, price)
SELECT gen_random_uuid(), fi2.id, 'NIGIRI', TRUE, src.price
FROM (VALUES
          ('UNAGI NIGIRI 1Stk', 3.50),
          ('TUNA NIGIRI 1Stk', 4.00),
          ('SALMON NIGIRI 1Stk', 3.50),
          ('TAMAGO NIGIRI 1Stk', 3.00),
          ('INARI 2Stk', 6.00),
          ('EBI NIGIRI 1Stk', 3.50),
          ('THONMOUSSE GUNKAN 1Stk', 3.00),
          ('IKURA GUNKAN 1Stk', 5.00),
          ('ABURI 1Stk', 4.00),
          ('AVO NIGIRI 1Stk', 3.00),
          ('SPECIAL NIGIRI 2Stk', 8.00)
     ) AS src(name, price)
         JOIN (
    SELECT id, name FROM ins
    UNION ALL
    SELECT id, name FROM food_item
) fi2 ON fi2.name = src.name
    ON CONFLICT DO NOTHING;

-- -------------------- TEMAKI --------------------
WITH fi AS (
    SELECT * FROM (VALUES
                       ('SPICY TUNA TEMAKI', 'Thunfisch'),
                       ('SALMON TEMAKI', 'Lachs'),
                       ('EBI TEMAKI', 'Crevetten')
                  ) t(name, description)
),
     ins AS (
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url)
SELECT gen_random_uuid(), name, description, NULL, NULL, NULL FROM fi
    ON CONFLICT (name) DO NOTHING
  RETURNING id, name
)
INSERT INTO menu_item (id, food_item_id, category, available, price)
SELECT gen_random_uuid(), fi2.id, 'TEMAKI', TRUE, src.price
FROM (VALUES
          ('SPICY TUNA TEMAKI', 8.00),
          ('SALMON TEMAKI', 7.00),
          ('EBI TEMAKI', 7.00)
     ) AS src(name, price)
         JOIN (
    SELECT id, name FROM ins
    UNION ALL
    SELECT id, name FROM food_item
) fi2 ON fi2.name = src.name
    ON CONFLICT DO NOTHING;

-- -------------------- SUSHI PLATTEN --------------------
WITH fi AS (
    SELECT * FROM (VALUES
                       ('BORUTO DELIGHTS', '16Stk Maki'),
                       ('NINJA DELIGHT', '4Stk Maki, 3Stk Nigiri, 4Stk Roll'),
                       ('ROCK LEE VEGI DELIGHT', '4Stk Maki, 3Stk Nigiri, 4Stk Roll'),
                       ('SALMON DELIGHT', '8Stk Sushi Roll, 4Stk Maki, 3Stk Lachs Sashimi'),
                       ('TUNA DELIGHT', '8Stk Rainbow Salmon Roll, 4Stk Tuna Maki, 1Stk Tuna Tatar'),
                       ('SASUKE VEGI DELIGHT für 2 Personen', NULL),
                       ('SAKURA DELIGHT für 2 Personen', NULL),
                       ('NARUTO PLATTE für 3 Personen', 'Assorted Platter'),
                       ('KAKASHI PLATTE für 4 Personen', 'Assorted Platter'),
                       ('BURGDORFER PLATTE für 8 Personen', 'Assorted Platter')
                  ) t(name, description)
),
     ins AS (
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url)
SELECT gen_random_uuid(), name, description, NULL, NULL, NULL FROM fi
    ON CONFLICT (name) DO NOTHING
  RETURNING id, name
)
INSERT INTO menu_item (id, food_item_id, category, available, price)
SELECT gen_random_uuid(), fi2.id, 'SUSHI_PLATTEN', TRUE, src.price
FROM (VALUES
          ('BORUTO DELIGHTS', 16.00),
          ('NINJA DELIGHT', 21.00),
          ('ROCK LEE VEGI DELIGHT', 20.00),
          ('SALMON DELIGHT', 29.00),
          ('TUNA DELIGHT', 32.00),
          ('SASUKE VEGI DELIGHT für 2 Personen', 49.00),
          ('SAKURA DELIGHT für 2 Personen', 69.00),
          ('NARUTO PLATTE für 3 Personen', 98.00),
          ('KAKASHI PLATTE für 4 Personen', 120.00),
          ('BURGDORFER PLATTE für 8 Personen', 240.00)
     ) AS src(name, price)
         JOIN (
    SELECT id, name FROM ins
    UNION ALL
    SELECT id, name FROM food_item
) fi2 ON fi2.name = src.name
    ON CONFLICT DO NOTHING;

-- -------------------- BOWLS --------------------
WITH fi AS (
    SELECT * FROM (VALUES
                       ('UNADON', 'Sushi Reis, Unagi, Frühlingszwiebel, Aal Sauce'),
                       ('TOFU POKE', 'Sushi Reis, Fried Tofu, Quinoa, Edamame, Avocado, Mango, Frühlingszwiebel, Mikrogreens'),
                       ('LACHS POKE', 'Sushi Reis, Lachs, Edamame, Mango, Avocado, Gurke, Karotten, Wakame, Frühlingszwiebel, Mikrogreens, Käse'),
                       ('TUNA POKE', 'Marinierter Thunfisch, ...')
                  ) t(name, description)
),
     ins AS (
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url)
SELECT gen_random_uuid(), name, description, NULL, NULL, NULL FROM fi
    ON CONFLICT (name) DO NOTHING
  RETURNING id, name
)
INSERT INTO menu_item (id, food_item_id, category, available, price)
SELECT gen_random_uuid(), fi2.id, 'BOWLS', TRUE, src.price
FROM (VALUES
          ('UNADON', 18.00),
          ('TOFU POKE', 16.00),
          ('LACHS POKE', 18.00),
          ('TUNA POKE', 18.00)
     ) AS src(name, price)
         JOIN (
    SELECT id, name FROM ins
    UNION ALL
    SELECT id, name FROM food_item
) fi2 ON fi2.name = src.name
    ON CONFLICT DO NOTHING;

-- -------------------- DONBURI --------------------
WITH fi AS (
    SELECT * FROM (VALUES
                       ('LACHS DONBURI', 'Marinierter Lachs, Ei mit Aal Sauce, Reis'),
                       ('Mixed Chirashi', 'Lachs Sashimi, Tuna Sashimi, Ebi, Ikura, Avocado, Tamago, Nori, Reis'),
                       ('TUNA DONBURI', 'Marinierter Thunfisch, Ei mit Aal Sauce, Reis')
                  ) t(name, description)
),
     ins AS (
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url)
SELECT gen_random_uuid(), name, description, NULL, NULL, NULL FROM fi
    ON CONFLICT (name) DO NOTHING
  RETURNING id, name
)
INSERT INTO menu_item (id, food_item_id, category, available, price)
SELECT gen_random_uuid(), fi2.id, 'DONBURI', TRUE, src.price
FROM (VALUES
          ('LACHS DONBURI', 10.00),
          ('Mixed Chirashi', 25.00),
          ('TUNA DONBURI', 11.00)
     ) AS src(name, price)
         JOIN (
    SELECT id, name FROM ins
    UNION ALL
    SELECT id, name FROM food_item
) fi2 ON fi2.name = src.name
    ON CONFLICT DO NOTHING;

-- -------------------- THAI STARTER --------------------
WITH fi AS (
    SELECT * FROM (VALUES
                       ('SPRING ROLLS 2Stk (vegan)', 'gefüllt mit Glasnudeln, Gemüse & süss-saurem Dip'),
                       ('CRISPY SHRIMPS 5Stk', 'knusprig gebackene Krevette auf Salatblätter & süss-saurem Dip'),
                       ('SATAY', 'in Kokosmilch marinierte Pouletspiesschen mit Erdnuss-Sauce'),
                       ('GYOZAS', 'gebratene Teigtaschen mit verschiedenen Füllungen und Saucen (Soja/Spicy Mayo/Chili)')
                  ) t(name, description)
),
     ins AS (
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url)
SELECT gen_random_uuid(), name, description, NULL, NULL, NULL FROM fi
    ON CONFLICT (name) DO NOTHING
  RETURNING id, name
)
INSERT INTO menu_item (id, food_item_id, category, available, price)
SELECT gen_random_uuid(), fi2.id, 'THAI_STARTER', TRUE, src.price
FROM (VALUES
          ('SPRING ROLLS 2Stk (vegan)', 9.00),
          ('CRISPY SHRIMPS 5Stk', 11.00),
          ('SATAY', 13.50),
          ('GYOZAS', 13.50)
     ) AS src(name, price)
         JOIN (
    SELECT id, name FROM ins
    UNION ALL
    SELECT id, name FROM food_item
) fi2 ON fi2.name = src.name
    ON CONFLICT DO NOTHING;

-- -------------------- THAI SUPPE --------------------
WITH fi AS (
    SELECT * FROM (VALUES
                       ('BEEF NOODLE SUPPE', 'Rinder mit Reisnudeln, Rindfleisch streifen Beefballs, Sojasprossen & Koriander')
                  ) t(name, description)
),
     ins AS (
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url)
SELECT gen_random_uuid(), name, description, NULL, NULL, NULL FROM fi
    ON CONFLICT (name) DO NOTHING
  RETURNING id, name
)
INSERT INTO menu_item (id, food_item_id, category, available, price)
SELECT gen_random_uuid(), fi2.id, 'THAI_SUPPE', TRUE, src.price
FROM (VALUES
          ('BEEF NOODLE SUPPE', 22.00)
     ) AS src(name, price)
         JOIN (
    SELECT id, name FROM ins
    UNION ALL
    SELECT id, name FROM food_item
) fi2 ON fi2.name = src.name
    ON CONFLICT DO NOTHING;

-- -------------------- THAI NOODLES --------------------
WITH fi AS (
    SELECT * FROM (VALUES
                       ('PAD THAI', 'gebratene Reisnudeln mit Ei, Frühlingsknoblauch, Sojasprossen, Erdnüssen, süss-sauer Tamarindensauce & Tofu'),
                       ('PAD SI YU', 'gebratene breite Reisnudeln mit Rindfleisch, Knoblauch, Ei, schwarzer Sauce & frische Sojasprossen')
                  ) t(name, description)
),
     ins AS (
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url)
SELECT gen_random_uuid(), name, description, NULL, NULL, NULL FROM fi
    ON CONFLICT (name) DO NOTHING
  RETURNING id, name
)
INSERT INTO menu_item (id, food_item_id, category, available, price)
SELECT gen_random_uuid(), fi2.id, 'THAI_NOODLES', TRUE, src.price
FROM (VALUES
          ('PAD THAI', 20.00),
          ('PAD SI YU', 23.50)
     ) AS src(name, price)
         JOIN (
    SELECT id, name FROM ins
    UNION ALL
    SELECT id, name FROM food_item
) fi2 ON fi2.name = src.name
    ON CONFLICT DO NOTHING;

-- -------------------- THAI CURRY --------------------
WITH fi AS (
    SELECT * FROM (VALUES
                       ('GREEN THAI CURRY', 'grünes Curry mit Gemüse, Bambussprossen & Thai-Basilikum in Kokosmilch'),
                       ('RED THAI CURRY', 'rotes Curry mit Gemüse, Bambussprossen & Thai-Basilikum in Kokosmilch'),
                       ('MASSAMAN CURRY', 'Erdnuss-Curry mit geschmortem Rindfleischwürfel, Zwiebeln & Kartoffeln'),
                       ('GENG PED PED', 'Bambussprossen, Thai-Basilikum in Kokosmilch, Cherry-Tomate, Ananas'),
                       ('PANANG CURRY', 'rotes Panang Curry mit Rindfleisch & Langbohnen'),
                       ('SPICY PEANUT CURRY (vegan)', 'plantet Chicken, Erdnüsse, Peperoni, Kaffir & Kokosmilch')
                  ) t(name, description)
),
     ins AS (
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url)
SELECT gen_random_uuid(), name, description, NULL, NULL, NULL FROM fi
    ON CONFLICT (name) DO NOTHING
  RETURNING id, name
)
INSERT INTO menu_item (id, food_item_id, category, available, price)
SELECT gen_random_uuid(), fi2.id, 'THAI_CURRY', TRUE, src.price
FROM (VALUES
          ('GREEN THAI CURRY', 20.00),
          ('RED THAI CURRY', 20.00),
          ('MASSAMAN CURRY', 20.50),
          ('GENG PED PED', 23.50),
          ('PANANG CURRY', 21.00),
          ('SPICY PEANUT CURRY (vegan)', 23.00)
     ) AS src(name, price)
         JOIN (
    SELECT id, name FROM ins
    UNION ALL
    SELECT id, name FROM food_item
) fi2 ON fi2.name = src.name
    ON CONFLICT DO NOTHING;

-- -------------------- THAI WOK --------------------
WITH fi AS (
    SELECT * FROM (VALUES
                       ('FRIED-RICE', 'gebratener Reis'),
                       ('CHICKEN CASHEW-NUTS', 'Poulet mit gerösteten Cashew-Nüssen, getrockneten Chilis, Zwiebeln & Peperoni'),
                       ('FRIED DUCK', 'gebackene Ente mit Gemüse & dunkler Austernsauce')
                  ) t(name, description)
),
     ins AS (
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url)
SELECT gen_random_uuid(), name, description, NULL, NULL, NULL FROM fi
    ON CONFLICT (name) DO NOTHING
  RETURNING id, name
)
INSERT INTO menu_item (id, food_item_id, category, available, price)
SELECT gen_random_uuid(), fi2.id, 'THAI_WOK', TRUE, src.price
FROM (VALUES
          ('FRIED-RICE', 19.00),
          ('CHICKEN CASHEW-NUTS', 23.50),
          ('FRIED DUCK', 24.00)
     ) AS src(name, price)
         JOIN (
    SELECT id, name FROM ins
    UNION ALL
    SELECT id, name FROM food_item
) fi2 ON fi2.name = src.name
    ON CONFLICT DO NOTHING;

-- -------------------- SIDES --------------------
WITH fi AS (
    SELECT * FROM (VALUES
                       ('REIS - Parfümreis', NULL),
                       ('Gebratener Reis mit Ei', NULL),
                       ('Gebratenereisnudeln', NULL)
                  ) t(name, description)
),
     ins AS (
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url)
SELECT gen_random_uuid(), name, description, NULL, NULL, NULL FROM fi
    ON CONFLICT (name) DO NOTHING
  RETURNING id, name
)
INSERT INTO menu_item (id, food_item_id, category, available, price)
SELECT gen_random_uuid(), fi2.id, 'SIDES', TRUE, src.price
FROM (VALUES
          ('REIS - Parfümreis', 3.00),
          ('Gebratener Reis mit Ei', 5.00),
          ('Gebratenereisnudeln', 4.00)
     ) AS src(name, price)
         JOIN (
    SELECT id, name FROM ins
    UNION ALL
    SELECT id, name FROM food_item
) fi2 ON fi2.name = src.name
    ON CONFLICT DO NOTHING;

-- -------------------- DRINKS --------------------
WITH fi AS (
    SELECT * FROM (VALUES
                       ('Soft Drinks', NULL),
                       ('Mangajo Drinks', NULL),
                       ('Beir', NULL),
                       ('Wein', NULL),
                       ('Heissgetranke', NULL)
                  ) t(name, description)
),
     ins AS (
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url)
SELECT gen_random_uuid(), name, description, NULL, NULL, NULL FROM fi
    ON CONFLICT (name) DO NOTHING
  RETURNING id, name
)
INSERT INTO menu_item (id, food_item_id, category, available, price)
SELECT gen_random_uuid(), fi2.id, 'DRINK', TRUE, src.price
FROM (VALUES
          ('Soft Drinks', 4.50),
          ('Mangajo Drinks', 5.00),
          ('Beir', 5.00),
          ('Wein', 6.50),
          ('Heissgetranke', 4.50)
     ) AS src(name, price)
         JOIN (
    SELECT id, name FROM ins
    UNION ALL
    SELECT id, name FROM food_item
) fi2 ON fi2.name = src.name
    ON CONFLICT DO NOTHING;

-- -------------------- DESSERT --------------------
WITH fi AS (
    SELECT * FROM (VALUES
                       ('Desserts-Mochi 2Stk', NULL)
                  ) t(name, description)
),
     ins AS (
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url)
SELECT gen_random_uuid(), name, description, NULL, NULL, NULL FROM fi
    ON CONFLICT (name) DO NOTHING
  RETURNING id, name
)
INSERT INTO menu_item (id, food_item_id, category, available, price)
SELECT gen_random_uuid(), fi2.id, 'DESSERT', TRUE, src.price
FROM (VALUES
          ('Desserts-Mochi 2Stk', 6.00)
     ) AS src(name, price)
         JOIN (
    SELECT id, name FROM ins
    UNION ALL
    SELECT id, name FROM food_item
) fi2 ON fi2.name = src.name
    ON CONFLICT DO NOTHING;
