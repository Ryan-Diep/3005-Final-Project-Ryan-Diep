-- Insert statements for Members
INSERT INTO Members (email, password, name, age, weight, height, goal_date, goal_weight)
VALUES ('member1@example.com', 'password1', 'John Doe', 30, 75, 180, '2024-06-30', 70),
       ('member2@example.com', 'password2', 'Alice Smith', 25, 65, 165, '2024-07-15', 60),
       ('member3@example.com', 'password3', 'Bob Johnson', 35, 80, 175, '2024-08-20', 75);

-- Insert statements for Trainers
INSERT INTO Trainers (email, password, name)
VALUES ('trainer1@example.com', 'password1', 'Mike Trainer'),
       ('trainer2@example.com', 'password2', 'Sarah Fitness');

-- Insert statements for Admins
INSERT INTO Admins (email, password, name)
VALUES ('admin1@example.com', 'password1', 'Admin1'),
       ('admin2@example.com', 'password2', 'Admin2');

-- Insert statements for Trainer Schedules
INSERT INTO TrainerSchedules (trainer_id, member_id, availability_date, start_time, end_time, cost, paid)
VALUES 
    (1, NULL, '2024-06-01', '08:00:00', '09:00:00', 20.00, FALSE),
    (1, NULL, '2024-06-03', '09:00:00', '10:00:00', 25.00, FALSE),
    (2, NULL, '2024-06-02', '10:00:00', '11:00:00', 15.00, FALSE),
    (2, NULL, '2024-06-04', '11:00:00', '12:00:00', 18.00, FALSE),
    (1, NULL, '2024-06-05', '08:00:00', '09:00:00', 20.00, FALSE),
    (2, NULL, '2024-06-06', '10:00:00', '11:00:00', 15.00, FALSE),
    (1, NULL, '2024-06-08', '08:00:00', '09:00:00', 20.00, FALSE),
    (2, NULL, '2024-06-09', '10:00:00', '11:00:00', 15.00, FALSE),
    (1, NULL, '2024-06-10', '08:00:00', '09:00:00', 20.00, FALSE),
    (2, NULL, '2024-06-11', '10:00:00', '11:00:00', 15.00, FALSE);

-- Insert statements for Rooms
INSERT INTO Rooms (room_name, max_capacity)
VALUES ('Room1', 20),
       ('Room2', 30),
       ('Room3', 15);

-- Insert statements for Class Schedules
INSERT INTO ClassSchedules (trainer_id, room_id, name, start_date, end_date, start_time, end_time, num_weeks, day_of_week, participants, cost)
VALUES (1, 1, 'Yoga Class', '2024-06-01', '2024-07-01', '08:00:00', '09:00:00', 4, 'Monday', 10, 10.00),
       (2, 2, 'Zumba Class', '2024-06-02', '2024-06-30', '10:00:00', '11:00:00', 4, 'Tuesday', 15, 12.50),
       (1, 3, 'Pilates Class', '2024-06-03', '2024-07-03', '09:00:00', '10:00:00', 4, 'Wednesday', 12, 11.00);

-- Insert statements for Equipment
INSERT INTO Equipment (room_id, name, maintenance_start_date, maintenance_end_date)
VALUES (1, 'Treadmill', NULL, NULL),
       (2, 'Dumbbells', '2024-06-01', '2024-06-10'),
       (3, 'Yoga Mats', NULL, NULL);
