-- Members Table
CREATE TABLE Members (
    member_id SERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    age INT,
    weight INT,
    height INT,
    goal_date DATE,
    goal_weight INT
);

-- Trainers Table
CREATE TABLE IF NOT EXISTS Trainers (
    trainer_id SERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL
);

-- Admins Table
CREATE TABLE IF NOT EXISTS Admins (
    admin_id SERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL
);

-- Trainer Schedules Table
CREATE TABLE IF NOT EXISTS TrainerSchedules (
    availability_id SERIAL PRIMARY KEY,
    trainer_id INT NOT NULL,
    member_id INT,
    availability_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
	cost FLOAT NOT NULL,
	paid BOOL NOT NULL,
    FOREIGN KEY (trainer_id) REFERENCES Trainers(trainer_id),
    FOREIGN KEY (member_id) REFERENCES Members(member_id)
);

-- Rooms Table
CREATE TABLE IF NOT EXISTS Rooms (
    room_id SERIAL PRIMARY KEY,
    room_name VARCHAR(100) NOT NULL,
	max_capacity INT NOT NULL
);

-- Room Bookings Table
CREATE TABLE IF NOT EXISTS RoomBookings (
    booking_id SERIAL PRIMARY KEY,
    room_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
	end_date DATE NOT NULL,
	start_time TIME NOT NULL,
	end_time TIME NOT NULL,
	day_of_week VARCHAR(100) NOT NULL,
    FOREIGN KEY (room_id) REFERENCES Rooms(room_id)
);

-- Class Schedules Table
CREATE TABLE IF NOT EXISTS ClassSchedules (
    class_id SERIAL PRIMARY KEY,
	trainer_id INT NOT NULL,
	room_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
	end_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
	num_weeks INT NOT NULL,
	day_of_week VARCHAR(100) NOT NULL,
	participants INT NOT NULL,
	cost FLOAT NOT NULL,
	FOREIGN KEY (trainer_id) REFERENCES Trainers(trainer_id),
    FOREIGN KEY (room_id) REFERENCES Rooms(room_id)
);

-- Class Registrations Table
CREATE TABLE IF NOT EXISTS ClassRegistrations (
    member_id INT NOT NULL,
    class_id INT NOT NULL,
    paid BOOL NOT NULL,
    PRIMARY KEY (member_id, class_id),
    FOREIGN KEY (member_id) REFERENCES Members(member_id),
    FOREIGN KEY (class_id) REFERENCES ClassSchedules(class_id)
);

-- Equipment Table
CREATE TABLE IF NOT EXISTS Equipment (
    equipment_id SERIAL PRIMARY KEY,
    room_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    maintenance_start_date DATE,
    maintenance_end_date DATE,
    FOREIGN KEY (room_id) REFERENCES Rooms(room_id)
);