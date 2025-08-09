-- V8__add_food_items.sql
-- Seed food_item + menu_item with fixed UUIDs
-- Generated for Asian Kitchen

-- =========================================================
-- SUSHI STARTER
-- =========================================================
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url) VALUES
('11111111-1111-1111-1111-111111111101','MISO SUPPE','Mix aus Brühen, Tofu, Edamame, Gemüse',NULL,NULL,NULL),
('11111111-1111-1111-1111-111111111102','EDAMAME','Sojabohnen mit Meersalz',NULL,NULL,NULL),
('11111111-1111-1111-1111-111111111103','WAKAME SALAT','Algen',NULL,NULL,NULL),
('11111111-1111-1111-1111-111111111104','CHICKEN KARAGE','Poulet',NULL,NULL,NULL),
('11111111-1111-1111-1111-111111111105','SPICY TUNA TATAR','Thunfisch, Lauch, Spicy Mayo Sauce, Sesam',NULL,NULL,NULL),
('11111111-1111-1111-1111-111111111106','SALMON TATAR','Lachs, Philadelphia Käse, Avocado, Sesam',NULL,NULL,NULL),
('11111111-1111-1111-1111-111111111107','SALMON SHASHIMI 5Stk','Lachs',NULL,NULL,NULL),
('11111111-1111-1111-1111-111111111108','SASHIMI DUO','Thun 5x, Lachs 5x',NULL,NULL,NULL),
('11111111-1111-1111-1111-111111111109','TUNA SHASHIMI 5Stk','Thun',NULL,NULL,NULL);

INSERT INTO menu_item (id, food_item_id, category, available, price) VALUES
                                                                         ('22222222-2222-2222-2222-222222222201','11111111-1111-1111-1111-111111111101','SUSHI_STARTER',TRUE, 6.00),
                                                                         ('22222222-2222-2222-2222-222222222202','11111111-1111-1111-1111-111111111102','SUSHI_STARTER',TRUE, 7.50),
                                                                         ('22222222-2222-2222-2222-222222222203','11111111-1111-1111-1111-111111111103','SUSHI_STARTER',TRUE, 7.50),
                                                                         ('22222222-2222-2222-2222-222222222204','11111111-1111-1111-1111-111111111104','SUSHI_STARTER',TRUE,12.00),
                                                                         ('22222222-2222-2222-2222-222222222205','11111111-1111-1111-1111-111111111105','SUSHI_STARTER',TRUE,13.00),
                                                                         ('22222222-2222-2222-2222-222222222206','11111111-1111-1111-1111-111111111106','SUSHI_STARTER',TRUE,12.00),
                                                                         ('22222222-2222-2222-2222-222222222207','11111111-1111-1111-1111-111111111107','SUSHI_STARTER',TRUE,11.00),
                                                                         ('22222222-2222-2222-2222-222222222208','11111111-1111-1111-1111-111111111108','SUSHI_STARTER',TRUE,21.00),
                                                                         ('22222222-2222-2222-2222-222222222209','11111111-1111-1111-1111-111111111109','SUSHI_STARTER',TRUE,12.00);

-- =========================================================
-- SUSHI ROLLS
-- =========================================================
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url) VALUES
                                                                                     ('11111111-1111-1111-1111-111111111201','SPICY TUNA ROLL 4Stk','Thunfisch, Gurke, Lauch, Spicy Mayo',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111202','CALIFORNIA ROLL 4Stk','Surimi, Premium Snow Crab Meat, Gurke, Avocado, Mayo, Tobiko',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111203','CRUNCHY PRAWN ROLL 4Stk','Crevetten, Röstzwiebel, Mayo, Avocado, Aal Sauce',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111204','THOMMOUSSE URAMAKI 4Stk','Thonmousse, Mayo, Sesam, Gurke',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111205','COCOS SALMON ROLL 4Stk','Lachs, Cocosnusscreme, Avocado, Mango',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111206','FLAMED SALMON ROLL 4Stk','Lachs, Spicy Mayo, Avocado',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111207','DRAGON UNAGI ROLL 4Stk','Unagi, Avocado, Gurke, Aal Sauce',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111208','RAINBOW TUNA ROLL 4Stk','Thunfisch, Avocado',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111209','RAINBOW SALMON ROLL 4Stk','Lachs, Avocado',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-11111111120a','RAINBOW SALMON MANGO 4Stk','Lachs, Avocado, Mango',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-11111111120b','FRIED SALMON FUTOMAKI 4Stk',NULL,NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-11111111120c','TEMPURA FUTOMAKI 4Stk','Ebi Tempura, Avocado, Philadelphia Käse, Gurke, Eisbergsalat, Karotten, Aal Sauce',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-11111111120d','CHICKEN KATSU ROLL 4Stk','Poulet Katsu, Spicy Mayo, Avocado, Gurke, Eisbergsalat, Karotten',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-11111111120e','VEGI RAINBOW ROLL 4Stk','Avocado, Mango',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-11111111120f','COCOS VEGI ROLL 4Stk','Cocos, Avocado, Mango, Inari',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111210','VEGI FUTOMAKI 4Stk',NULL,NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111211','CHEFS CHOICE VEGI ROLL 4Stk','The best of our Chefs Choice',NULL,NULL,NULL);

INSERT INTO menu_item (
                       id, food_item_id, category, available, price
) VALUES
      ('22222222-2222-2222-2222-222222222301','11111111-1111-1111-1111-111111111201','SUSHI_ROLLS',TRUE, 8.00),
      ('22222222-2222-2222-2222-222222222302','11111111-1111-1111-1111-111111111202','SUSHI_ROLLS',TRUE, 8.00),
      ('22222222-2222-2222-2222-222222222303','11111111-1111-1111-1111-111111111203','SUSHI_ROLLS',TRUE, 8.00),
      ('22222222-2222-2222-2222-222222222304','11111111-1111-1111-1111-111111111204','SUSHI_ROLLS',TRUE, 7.50),
      ('22222222-2222-2222-2222-222222222305','11111111-1111-1111-1111-111111111205','SUSHI_ROLLS',TRUE, 8.00),
      ('22222222-2222-2222-2222-222222222306','11111111-1111-1111-1111-111111111206','SUSHI_ROLLS',TRUE, 9.00),
      ('22222222-2222-2222-2222-222222222307','11111111-1111-1111-1111-111111111207','SUSHI_ROLLS',TRUE, 9.00),
      ('22222222-2222-2222-2222-222222222308','11111111-1111-1111-1111-111111111208','SUSHI_ROLLS',TRUE, 9.00),
      ('22222222-2222-2222-2222-222222222309','11111111-1111-1111-1111-111111111209','SUSHI_ROLLS',TRUE, 9.00),
      ('22222222-2222-2222-2222-22222222230a','11111111-1111-1111-1111-11111111120a','SUSHI_ROLLS',TRUE, 9.00),
      ('22222222-2222-2222-2222-22222222230b','11111111-1111-1111-1111-11111111120b','SUSHI_ROLLS',TRUE,12.00),
      ('22222222-2222-2222-2222-22222222230c','11111111-1111-1111-1111-11111111120c','SUSHI_ROLLS',TRUE,10.00),
      ('22222222-2222-2222-2222-22222222230d','11111111-1111-1111-1111-11111111120d','SUSHI_ROLLS',TRUE,11.00),
      ('22222222-2222-2222-2222-22222222230e','11111111-1111-1111-1111-11111111120e','SUSHI_ROLLS',TRUE, 8.50),
      ('22222222-2222-2222-2222-22222222230f','11111111-1111-1111-1111-11111111120f','SUSHI_ROLLS',TRUE, 7.00),
      ('22222222-2222-2222-2222-222222222310','11111111-1111-1111-1111-111111111210','SUSHI_ROLLS',TRUE, 9.00),
      ('22222222-2222-2222-2222-222222222311','11111111-1111-1111-1111-111111111211','SUSHI_ROLLS',TRUE, 9.00);

-- =========================================================
-- HOSO MAKI
-- =====
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url) VALUES
                                                                                     ('11111111-1111-1111-1111-111111111301','SALMON MAKI 4Stk','Lachs',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111302','SALMON AVO MAKI 4Stk','Lachs, Avocado',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111303','TUNA MAKI 4Stk','Thunfisch',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111304','TUNA AVO MAKI','Thunfisch, Avocado',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111305','THONMOUSSE MAKI 4Stk','Thunfisch, Gurke',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111306','Veg OSHINKO MAKI 4Stk','Eingelegter Rettich',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111307','Veg AVO MAKI 4Stk','Avocado',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111308','Veg- KAPPA MAKI 4Stk','Gurke',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111309','Veg- TAMAGO MAKI 4Stk','Tamago (Omelette)',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-11111111130a','Veg- KÜRBIS MAKI 4Stk','Kürbis',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-11111111130b','Veg- MANGO MAKI 4Stk','Mango',NULL,NULL,NULL);

INSERT INTO menu_item (id, food_item_id, category, available, price) VALUES
                                                                         ('22222222-2222-2222-2222-222222222401','11111111-1111-1111-1111-111111111301','HOSO_MAKI',TRUE,4.50),
                                                                         ('22222222-2222-2222-2222-222222222402','11111111-1111-1111-1111-111111111302','HOSO_MAKI',TRUE,4.50),
                                                                         ('22222222-2222-2222-2222-222222222403','11111111-1111-1111-1111-111111111303','HOSO_MAKI',TRUE,4.50),
                                                                         ('22222222-2222-2222-2222-222222222404','11111111-1111-1111-1111-111111111304','HOSO_MAKI',TRUE,4.50),
                                                                         ('22222222-2222-2222-2222-222222222405','11111111-1111-1111-1111-111111111305','HOSO_MAKI',TRUE,4.50),
                                                                         ('22222222-2222-2222-2222-222222222406','11111111-1111-1111-1111-111111111306','HOSO_MAKI',TRUE,4.00),
                                                                         ('22222222-2222-2222-2222-222222222407','11111111-1111-1111-1111-111111111307','HOSO_MAKI',TRUE,4.00),
                                                                         ('22222222-2222-2222-2222-222222222408','11111111-1111-1111-1111-111111111308','HOSO_MAKI',TRUE,4.00),
                                                                         ('22222222-2222-2222-2222-222222222409','11111111-1111-1111-1111-111111111309','HOSO_MAKI',TRUE,4.00),
                                                                         ('22222222-2222-2222-2222-22222222240a','11111111-1111-1111-1111-11111111130a','HOSO_MAKI',TRUE,4.00),
                                                                         ('22222222-2222-2222-2222-22222222240b','11111111-1111-1111-1111-11111111130b','HOSO_MAKI',TRUE,4.00);

-- =========================================================
-- NIGIRI
-- =========================================================
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url) VALUES
                                                                                     ('11111111-1111-1111-1111-111111111401','UNAGI NIGIRI 1Stk','Aal',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111402','TUNA NIGIRI 1Stk','Thunfisch',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111403','SALMON NIGIRI 1Stk','Lachs',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111404','TAMAGO NIGIRI 1Stk','Omelette',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111405','INARI 2Stk','Tofutasche',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111406','EBI NIGIRI 1Stk','Crevette',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111407','THONMOUSSE GUNKAN 1Stk','Thunfisch',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111408','IKURA GUNKAN 1Stk','Lachskaviar',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111409','ABURI 1Stk','Flambierter Lachs',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-11111111140a','AVO NIGIRI 1Stk','Avocado',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-11111111140b','SPECIAL NIGIRI 2Stk','Tuna/Salmon/Ebi',NULL,NULL,NULL);

INSERT INTO menu_item (id, food_item_id, category, available, price) VALUES
                                                                         ('22222222-2222-2222-2222-222222222501','11111111-1111-1111-1111-111111111401','NIGIRI',TRUE,3.50),
                                                                         ('22222222-2222-2222-2222-222222222502','11111111-1111-1111-1111-111111111402','NIGIRI',TRUE,4.00),
                                                                         ('22222222-2222-2222-2222-222222222503','11111111-1111-1111-1111-111111111403','NIGIRI',TRUE,3.50),
                                                                         ('22222222-2222-2222-2222-222222222504','11111111-1111-1111-1111-111111111404','NIGIRI',TRUE,3.00),
                                                                         ('22222222-2222-2222-2222-222222222505','11111111-1111-1111-1111-111111111405','NIGIRI',TRUE,6.00),
                                                                         ('22222222-2222-2222-2222-222222222506','11111111-1111-1111-1111-111111111406','NIGIRI',TRUE,3.50),
                                                                         ('22222222-2222-2222-2222-222222222507','11111111-1111-1111-1111-111111111407','NIGIRI',TRUE,3.00),
                                                                         ('22222222-2222-2222-2222-222222222508','11111111-1111-1111-1111-111111111408','NIGIRI',TRUE,5.00),
                                                                         ('22222222-2222-2222-2222-222222222509','11111111-1111-1111-1111-111111111409','NIGIRI',TRUE,4.00),
                                                                         ('22222222-2222-2222-2222-22222222250a','11111111-1111-1111-1111-11111111140a','NIGIRI',TRUE,3.00),
                                                                         ('22222222-2222-2222-2222-22222222250b','11111111-1111-1111-1111-11111111140b','NIGIRI',TRUE,8.00);

-- =========================================================
-- TEMAKI
-- =========================================================
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url) VALUES
                                                                                     ('11111111-1111-1111-1111-111111111501','SPICY TUNA TEMAKI','Thunfisch',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111502','SALMON TEMAKI','Lachs',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111503','EBI TEMAKI','Crevetten',NULL,NULL,NULL);

INSERT INTO menu_item (id, food_item_id, category, available, price) VALUES
                                                                         ('22222222-2222-2222-2222-222222222601','11111111-1111-1111-1111-111111111501','TEMAKI',TRUE,8.00),
                                                                         ('22222222-2222-2222-2222-222222222602','11111111-1111-1111-1111-111111111502','TEMAKI',TRUE,7.00),
                                                                         ('22222222-2222-2222-2222-222222222603','11111111-1111-1111-1111-111111111503','TEMAKI',TRUE,7.00);

-- =========================================================
-- SUSHI PLATTEN
-- =========================================================
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url) VALUES
                                                                                     ('11111111-1111-1111-1111-111111111601','BORUTO DELIGHTS','16Stk Maki',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111602','NINJA DELIGHT','4Stk Maki, 3Stk Nigiri, 4Stk Roll',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111603','ROCK LEE VEGI DELIGHT','4Stk Maki, 3Stk Nigiri, 4Stk Roll',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111604','SALMON DELIGHT','8Stk Sushi Roll, 4Stk Maki, 3Stk Lachs Sashimi',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111605','TUNA DELIGHT','8Stk Rainbow Salmon Roll, 4Stk Tuna Maki, 1Stk Tuna Tatar',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111606','SASUKE VEGI DELIGHT für 2 Personen',NULL,NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111607','SAKURA DELIGHT für 2 Personen',NULL,NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111608','NARUTO PLATTE für 3 Personen','Assorted Platter',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111609','KAKASHI PLATTE für 4 Personen','Assorted Platter',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-11111111160a','BURGDORFER PLATTE für 8 Personen','Assorted Platter',NULL,NULL,NULL);

INSERT INTO menu_item (id, food_item_id, category, available, price) VALUES
                                                                         ('22222222-2222-2222-2222-222222222701','11111111-1111-1111-1111-111111111601','SUSHI_PLATTEN',TRUE, 16.00),
                                                                         ('22222222-2222-2222-2222-222222222702','11111111-1111-1111-1111-111111111602','SUSHI_PLATTEN',TRUE, 21.00),
                                                                         ('22222222-2222-2222-2222-222222222703','11111111-1111-1111-1111-111111111603','SUSHI_PLATTEN',TRUE, 20.00),
                                                                         ('22222222-2222-2222-2222-222222222704','11111111-1111-1111-1111-111111111604','SUSHI_PLATTEN',TRUE, 29.00),
                                                                         ('22222222-2222-2222-2222-222222222705','11111111-1111-1111-1111-111111111605','SUSHI_PLATTEN',TRUE, 32.00),
                                                                         ('22222222-2222-2222-2222-222222222706','11111111-1111-1111-1111-111111111606','SUSHI_PLATTEN',TRUE, 49.00),
                                                                         ('22222222-2222-2222-2222-222222222707','11111111-1111-1111-1111-111111111607','SUSHI_PLATTEN',TRUE, 69.00),
                                                                         ('22222222-2222-2222-2222-222222222708','11111111-1111-1111-1111-111111111608','SUSHI_PLATTEN',TRUE, 98.00),
                                                                         ('22222222-2222-2222-2222-222222222709','11111111-1111-1111-1111-111111111609','SUSHI_PLATTEN',TRUE,120.00),
                                                                         ('22222222-2222-2222-2222-22222222270a','11111111-1111-1111-1111-11111111160a','SUSHI_PLATTEN',TRUE,240.00);

-- =========================================================
-- BOWLS
-- =========================================================
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url) VALUES
                                                                                     ('11111111-1111-1111-1111-111111111701','UNADON','Sushi Reis, Unagi, Frühlingszwiebel, Aal Sauce',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111702','TOFU POKE','Sushi Reis, Fried Tofu, Quinoa, Edamame, Avocado, Mango, Frühlingszwiebel, Mikrogreens',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111703','LACHS POKE','Sushi Reis, Lachs, Edamame, Mango, Avocado, Gurke, Karotten, Wakame, Frühlingszwiebel, Mikrogreens, Käse',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111704','TUNA POKE','Marinierter Thunfisch, ...',NULL,NULL,NULL);

INSERT INTO menu_item (id, food_item_id, category, available, price) VALUES
                                                                         ('22222222-2222-2222-2222-222222222801','11111111-1111-1111-1111-111111111701','BOWLS',TRUE,18.00),
                                                                         ('22222222-2222-2222-2222-222222222802','11111111-1111-1111-1111-111111111702','BOWLS',TRUE,16.00),
                                                                         ('22222222-2222-2222-2222-222222222803','11111111-1111-1111-1111-111111111703','BOWLS',TRUE,18.00),
                                                                         ('22222222-2222-2222-2222-222222222804','11111111-1111-1111-1111-111111111704','BOWLS',TRUE,18.00);

-- =========================================================
-- DONBURI
-- =========================================================
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url) VALUES
                                                                                     ('11111111-1111-1111-1111-111111111801','LACHS DONBURI','Marinierter Lachs, Ei mit Aal Sauce, Reis',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111802','Mixed Chirashi','Lachs Sashimi, Tuna Sashimi, Ebi, Ikura, Avocado, Tamago, Nori, Reis',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111803','TUNA DONBURI','Marinierter Thunfisch, Ei mit Aal Sauce, Reis',NULL,NULL,NULL);

INSERT INTO menu_item (id, food_item_id, category, available, price) VALUES
                                                                         ('22222222-2222-2222-2222-222222222901','11111111-1111-1111-1111-111111111801','DONBURI',TRUE,10.00),
                                                                         ('22222222-2222-2222-2222-222222222902','11111111-1111-1111-1111-111111111802','DONBURI',TRUE,25.00),
                                                                         ('22222222-2222-2222-2222-222222222903','11111111-1111-1111-1111-111111111803','DONBURI',TRUE,11.00);

-- =========================================================
-- THAI STARTER
-- =========================================================
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url) VALUES
                                                                                     ('11111111-1111-1111-1111-111111111901','SPRING ROLLS 2Stk (vegan)','gefüllt mit Glasnudeln, Gemüse & süss-saurem Dip',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111902','CRISPY SHRIMPS 5Stk','knusprig gebackene Krevette auf Salatblätter & süss-saurem Dip',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111903','SATAY','in Kokosmilch marinierte Pouletspiesschen mit Erdnuss-Sauce',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111111904','GYOZAS','gebratene Teigtaschen mit verschiedenen Füllungen und Saucen (Soja/Spicy Mayo/Chili)',NULL,NULL,NULL);

INSERT INTO menu_item (id, food_item_id, category, available, price) VALUES
                                                                         ('22222222-2222-2222-2222-222222223001','11111111-1111-1111-1111-111111111901','THAI_STARTER',TRUE, 9.00),
                                                                         ('22222222-2222-2222-2222-222222223002','11111111-1111-1111-1111-111111111902','THAI_STARTER',TRUE,11.00),
                                                                         ('22222222-2222-2222-2222-222222223003','11111111-1111-1111-1111-111111111903','THAI_STARTER',TRUE,13.50),
                                                                         ('22222222-2222-2222-2222-222222223004','11111111-1111-1111-1111-111111111904','THAI_STARTER',TRUE,13.50);

-- =========================================================
-- THAI SUPPE (only item with price shown)
-- =========================================================
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url) VALUES
    ('11111111-1111-1111-1111-111111112001','BEEF NOODLE SUPPE','Rinder mit Reisnudeln, Rindfleisch streifen Beefballs, Sojasprossen & Koriander',NULL,NULL,NULL);

INSERT INTO menu_item (id, food_item_id, category, available, price) VALUES
    ('22222222-2222-2222-2222-222222223101','11111111-1111-1111-1111-111111112001','THAI_SUPPE',TRUE,22.00);

-- =========================================================
-- THAI NOODLES
-- =========================================================
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url) VALUES
                                                                                     ('11111111-1111-1111-1111-111111112101','PAD THAI','gebratene Reisnudeln mit Ei, Frühlingsknoblauch, Sojasprossen, Erdnüssen, süss-sauer Tamarindensauce & Tofu',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111112102','PAD SI YU','gebratene breite Reisnudeln mit Rindfleisch, Knoblauch, Ei, schwarzer Sauce & frische Sojasprossen',NULL,NULL,NULL);

INSERT INTO menu_item (id, food_item_id, category, available, price) VALUES
                                                                         ('22222222-2222-2222-2222-222222223201','11111111-1111-1111-1111-111111112101','THAI_NOODLES',TRUE,20.00),
                                                                         ('22222222-2222-2222-2222-222222223202','11111111-1111-1111-1111-111111112102','THAI_NOODLES',TRUE,23.50);

-- =========================================================
-- THAI CURRY
-- =========================================================
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url) VALUES
                                                                                     ('11111111-1111-1111-1111-111111112201','GREEN THAI CURRY','grünes Curry mit Gemüse, Bambussprossen & Thai-Basilikum in Kokosmilch',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111112202','RED THAI CURRY','rotes Curry mit Gemüse, Bambussprossen & Thai-Basilikum in Kokosmilch',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111112203','MASSAMAN CURRY','Erdnuss-Curry mit geschmortem Rindfleischwürfel, Zwiebeln & Kartoffeln',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111112204','GENG PED PED','Bambussprossen, Thai-Basilikum in Kokosmilch, Cherry-Tomate, Ananas',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111112205','PANANG CURRY','rotes Panang Curry mit Rindfleisch & Langbohnen',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111112206','SPICY PEANUT CURRY (vegan)','plantet Chicken, Erdnüsse, Peperoni, Kaffir & Kokosmilch',NULL,NULL,NULL);

INSERT INTO menu_item (id, food_item_id, category, available, price) VALUES
                                                                         ('22222222-2222-2222-2222-222222223301','11111111-1111-1111-1111-111111112201','THAI_CURRY',TRUE,20.00),
                                                                         ('22222222-2222-2222-2222-222222223302','11111111-1111-1111-1111-111111112202','THAI_CURRY',TRUE,20.00),
                                                                         ('22222222-2222-2222-2222-222222223303','11111111-1111-1111-1111-111111112203','THAI_CURRY',TRUE,20.50),
                                                                         ('22222222-2222-2222-2222-222222223304','11111111-1111-1111-1111-111111112204','THAI_CURRY',TRUE,23.50),
                                                                         ('22222222-2222-2222-2222-222222223305','11111111-1111-1111-1111-111111112205','THAI_CURRY',TRUE,21.00),
                                                                         ('22222222-2222-2222-2222-222222223306','11111111-1111-1111-1111-111111112206','THAI_CURRY',TRUE,23.00);

-- =========================================================
-- THAI WOK (only items with prices shown)
-- =========================================================
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url) VALUES
                                                                                     ('11111111-1111-1111-1111-111111112301','FRIED-RICE','gebratener Reis',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111112302','CHICKEN CASHEW-NUTS','Poulet mit gerösteten Cashew-Nüssen, getrockneten Chilis, Zwiebeln & Peperoni',NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111112303','FRIED DUCK','gebackene Ente mit Gemüse & dunkler Austernsauce',NULL,NULL,NULL);

INSERT INTO menu_item (id, food_item_id, category, available, price) VALUES
                                                                         ('22222222-2222-2222-2222-222222223401','11111111-1111-1111-1111-111111112301','THAI_WOK',TRUE,19.00),
                                                                         ('22222222-2222-2222-2222-222222223402','11111111-1111-1111-1111-111111112302','THAI_WOK',TRUE,23.50),
                                                                         ('22222222-2222-2222-2222-222222223403','11111111-1111-1111-1111-111111112303','THAI_WOK',TRUE,24.00);

-- =========================================================
-- SIDES
-- =========================================================
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url) VALUES
                                                                                     ('11111111-1111-1111-1111-111111112401','REIS - Parfümreis',NULL,NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111112402','Gebratener Reis mit Ei',NULL,NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111112403','Gebratenereisnudeln',NULL,NULL,NULL,NULL);

INSERT INTO menu_item (id, food_item_id, category, available, price) VALUES
                                                                         ('22222222-2222-2222-2222-222222223501','11111111-1111-1111-1111-111111112401','SIDES',TRUE,3.00),
                                                                         ('22222222-2222-2222-2222-222222223502','11111111-1111-1111-1111-111111112402','SIDES',TRUE,5.00),
                                                                         ('22222222-2222-2222-2222-222222223503','11111111-1111-1111-1111-111111112403','SIDES',TRUE,4.00);

-- =========================================================
-- DRINK
-- =========================================================
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url) VALUES
                                                                                     ('11111111-1111-1111-1111-111111112501','Soft Drinks',NULL,NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111112502','Mangajo Drinks',NULL,NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111112503','Beir',NULL,NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111112504','Wein',NULL,NULL,NULL,NULL),
                                                                                     ('11111111-1111-1111-1111-111111112505','Heissgetranke',NULL,NULL,NULL,NULL);

INSERT INTO menu_item (id, food_item_id, category, available, price) VALUES
                                                                         ('22222222-2222-2222-2222-222222223601','11111111-1111-1111-1111-111111112501','DRINK',TRUE,4.50),
                                                                         ('22222222-2222-2222-2222-222222223602','11111111-1111-1111-1111-111111112502','DRINK',TRUE,5.00),
                                                                         ('22222222-2222-2222-2222-222222223603','11111111-1111-1111-1111-111111112503','DRINK',TRUE,5.00),
                                                                         ('22222222-2222-2222-2222-222222223604','11111111-1111-1111-1111-111111112504','DRINK',TRUE,6.50),
                                                                         ('22222222-2222-2222-2222-222222223605','11111111-1111-1111-1111-111111112505','DRINK',TRUE,4.50);

-- =========================================================
-- DESSERT
-- =========================================================
INSERT INTO food_item (id, name, description, ingredients, allergies, image_url) VALUES
    ('11111111-1111-1111-1111-111111112601','Desserts-Mochi 2Stk',NULL,NULL,NULL,NULL);

INSERT INTO menu_item (id, food_item_id, category, available, price) VALUES
    ('22222222-2222-2222-2222-222222223701','11111111-1111-1111-1111-111111112601','DESSERT',TRUE,6.00);