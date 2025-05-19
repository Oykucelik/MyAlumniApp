-- Dummy data for notifications table
-- Using user_id = 1 for all notifications to avoid foreign key constraint issues

-- Connection notifications
INSERT INTO notifications (user_id, title, message, notification_type, related_id, is_read, created_at)
VALUES 
(1, 'New connection request', 'John Smith wants to connect with you', 'connection', 2, false, '2023-08-15 14:30:00'),
(1, 'Connection accepted', 'Maria Garcia accepted your connection request', 'connection', 3, true, '2023-08-14 09:45:00'),
(1, 'New connection request', 'Sarah Johnson wants to connect with you', 'connection', 4, false, '2023-08-16 11:20:00'),
(1, 'Connection accepted', 'David Lee accepted your connection request', 'connection', 5, false, '2023-08-13 16:55:00');

-- Message notifications
INSERT INTO notifications (user_id, title, message, notification_type, related_id, is_read, created_at)
VALUES 
(1, 'New message', 'You have a new message from Maria Garcia', 'message', 101, false, '2023-08-15 10:15:00'),
(1, 'New message', 'You have a new message from John Smith', 'message', 102, true, '2023-08-14 13:22:00'),
(1, 'New message', 'You have a new message from Sarah Johnson', 'message', 103, false, '2023-08-16 09:05:00');

-- Event notifications
INSERT INTO notifications (user_id, title, message, notification_type, related_id, is_read, created_at)
VALUES 
(1, 'Upcoming event', 'Alumni Networking Event this Friday', 'event', 201, false, '2023-08-12 11:30:00'),
(1, 'Event reminder', 'Career Fair tomorrow at 10 AM', 'event', 202, true, '2023-08-13 15:45:00'),
(1, 'New event', 'Workshop: Resume Building on August 20th', 'event', 203, false, '2023-08-15 14:10:00'),
(1, 'Event update', 'Venue changed for Alumni Dinner', 'event', 204, false, '2023-08-14 16:20:00');

-- Job notifications
INSERT INTO notifications (user_id, title, message, notification_type, related_id, is_read, created_at)
VALUES 
(1, 'New job opportunity', 'Software Engineer position at Google', 'job', 301, false, '2023-08-16 08:30:00'),
(1, 'Job application update', 'Your application for Product Manager has been reviewed', 'job', 302, true, '2023-08-15 17:15:00'),
(1, 'Job recommendation', 'New Data Analyst position matches your profile', 'job', 303, false, '2023-08-14 12:40:00'),
(1, 'Job interview', 'Interview scheduled for Marketing position', 'job', 304, false, '2023-08-13 10:55:00');

-- Mentorship notifications
INSERT INTO notifications (user_id, title, message, notification_type, related_id, is_read, created_at)
VALUES 
(1, 'Mentorship request', 'David Lee wants you to be their mentor', 'mentorship', 401, false, '2023-08-15 09:20:00'),
(1, 'Mentorship accepted', 'Maria Garcia accepted your mentorship request', 'mentorship', 402, true, '2023-08-14 14:35:00'),
(1, 'Mentorship session', 'Upcoming mentorship session on August 18th', 'mentorship', 403, false, '2023-08-16 13:50:00'); 