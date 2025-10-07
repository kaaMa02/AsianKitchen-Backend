UPDATE public.restaurant_info
SET opening_hours = $json$
    {
  "1": [ { "open": "11:00", "close": "14:00" }, { "open": "17:00", "close": "23:00" } ],
  "2": [ { "open": "11:00", "close": "14:00" }, { "open": "17:00", "close": "22:00" } ],
  "3": [ { "open": "11:00", "close": "14:00" }, { "open": "17:00", "close": "22:00" } ],
  "4": [ { "open": "11:00", "close": "14:00" }, { "open": "17:00", "close": "22:00" } ],
  "5": [ { "open": "11:00", "close": "14:00" }, { "open": "17:00", "close": "23:00" } ],
  "6": [ ],
  "7": [ { "open": "11:00", "close": "14:00" }, { "open": "16:30", "close": "21:00" } ]
}
$json$;
