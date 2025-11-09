UPDATE restaurant_info
SET opening_hours = '{
  "1":[{"open":"11:00","close":"14:00"},{"open":"17:00","close":"22:00"}],
  "2":[{"open":"11:00","close":"14:00"},{"open":"17:00","close":"22:00"}],
  "3":[],
  "4":[{"open":"11:00","close":"14:00"},{"open":"17:00","close":"22:00"}],
  "5":[{"open":"11:00","close":"14:00"},{"open":"17:00","close":"22:00"}],
  "6":[{"open":"11:00","close":"14:00"},{"open":"17:00","close":"22:00"}],
  "7":[{"open":"11:00","close":"14:00"},{"open":"17:00","close":"22:00"}]
}'
WHERE id = (SELECT id FROM restaurant_info ORDER BY created_at DESC LIMIT 1);
