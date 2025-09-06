DELETE FROM restaurant_info;

INSERT INTO public.restaurant_info
(id, "name", about_text, street, street_no, plz, city, email, phone, instagram_url, google_maps_url, opening_hours, created_at)
VALUES('23ca9750-5055-403a-9639-bbaea80b4931'::uuid, 'Asian Kitchen', 'Welcome to our cozy corner of flavor, where every dish is served with a side of love! We’re a husband-and-wife team who enjoys crafting vibrant sushi rolls and fragrant Thai specialties. Our dream is simple: to share our joy for this work and wrap our guests in the warmth of our kitchen and give you a place where the food feels as genuine as the people behind it. Come join us at the table—we can’t wait to welcome you into our family!', 'Baslerstrasse', '16', '4632', 'Trimbach', 'asian.kitchen84@gmail.com', '+41 78 705 82 31', '', 'https://www.google.com/maps/place/Baslerstrasse+16,+4632+Trimbach/@47.3575929,7.9010725,19z/data=!3m1!4b1!4m6!3m5!1s0x479031b6c36921dd:0x805d34bc60c21cd5!8m2!3d47.3575929!4d7.9010725!16s%2Fg%2F11c5b4scch?entry=ttu&g_ep=EgoyMDI1MDkwMy4wIKXMDSoASAFQAw%3D%3D', 'Monday: Lunch 11:00 – 14:00 · Dinner 17:00–23:00
Tuesday: Lunch 11:00 – 14:00 · Dinner 17:00–22:00
Wednesday: Lunch 11:00 – 14:00 · Dinner 17:00–22:00
Thursday: Lunch 11:00 – 14:00 · Dinner 17:00–22:00
Friday: Lunch 11:00 – 14:00 · Dinner 17:00–23:00
Sunday: Lunch 11:00 – 14:00 · Dinner 16:30–21:00', '2025-08-09 15:18:33.050');