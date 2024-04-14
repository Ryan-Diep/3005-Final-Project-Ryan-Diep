import java.sql.*;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Scanner;

public class main {
    // JDBC URL, username, and password of PostgreSQL server
    static final String JDBC_DRIVER = "org.postgresql.Driver";
    static final String DB_URL = "jdbc:postgresql://localhost:5432/Final";
    static final String USER = "postgres";
    static final String PASS = "root";

    // Method to create a new member
    public static void createMember(Connection conn, String email, String password, String name, int age, int weight, int height, String goal_date, int goal_weight) throws  SQLException{
        String query = "INSERT INTO Members (email, password, name, age, weight, height, goal_date, goal_weight) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setString(1, email);
        statement.setString(2, password);
        statement.setString(3, name);
        statement.setInt(4, age);
        statement.setInt(5, weight);
        statement.setInt(6, height);
        statement.setDate(7, Date.valueOf(goal_date));
        statement.setInt(8, goal_weight);
        statement.executeUpdate();
        statement.close();
        System.out.println("Member " + name + " created successfully!");
    }

    // Method to retrieve member information by username
    public static void getMemberById(Connection conn, int id) throws SQLException {
        String memberQuery = "SELECT * FROM Members WHERE member_id = ?";
        PreparedStatement memberStatement = conn.prepareStatement(memberQuery);
        memberStatement.setInt(1, id);
        ResultSet memberResult = memberStatement.executeQuery();
    
        if (memberResult.next()) {
            String email = memberResult.getString("email");
            String name = memberResult.getString("name");
            int age = memberResult.getInt("age");
            int weight = memberResult.getInt("weight");
            int height = memberResult.getInt("height");
            Date goalDate = memberResult.getDate("goal_date");
            int goalWeight = memberResult.getInt("goal_weight");
    
            int trainerSessionsCompleted = countTrainerSessions(conn, id);
            int classSessionsCompleted = countClassSessions(conn, id);
    
            System.out.println("\n------------------------------------");
            System.out.println("Id: " + id);
            System.out.println("Email: " + email);
            System.out.println("Name: " + name);
            System.out.println("Age: " + age);
            System.out.println("Weight (kg): " + weight);
            System.out.println("Height (cm): " + height);
            System.out.println("Goal Date: " + goalDate);
            System.out.println("Goal Weight: " + goalWeight);
            System.out.println("Trainer Sessions Completed: " + trainerSessionsCompleted);
            System.out.println("Class Sessions Completed: " + classSessionsCompleted);
            System.out.println("Balance: $" + calculateTotalCost(conn, id));
        } else {
            System.out.println("No member found with id: " + id);
        }
    
        memberResult.close();
        memberStatement.close();
    }

    // Method to retrieve member information by name
    public static void getMemberByName(Connection conn, String name) throws SQLException {
        String memberQuery = "SELECT * FROM Members WHERE name = ?";
        PreparedStatement memberStatement = conn.prepareStatement(memberQuery);
        memberStatement.setString(1, name);
        ResultSet memberResult = memberStatement.executeQuery();
    
        boolean memberFound = false;
    
        while (memberResult.next()) {
            memberFound = true;
            int memberId = memberResult.getInt("member_id");
            String email = memberResult.getString("email");
            int age = memberResult.getInt("age");
            int weight = memberResult.getInt("weight");
            int height = memberResult.getInt("height");
            Date goalDate = memberResult.getDate("goal_date");
            int goalWeight = memberResult.getInt("goal_weight");
    
            int trainerSessionsCompleted = countTrainerSessions(conn, memberId);
            int classSessionsCompleted = countClassSessions(conn, memberId);
    
            System.out.println("Email: " + email);
            System.out.println("Name: " + name);
            System.out.println("Age: " + age);
            System.out.println("Weight (kg): " + weight);
            System.out.println("Height (cm): " + height);
            System.out.println("Goal Date: " + goalDate);
            System.out.println("Goal Weight: " + goalWeight);
            System.out.println("Trainer Sessions Completed: " + trainerSessionsCompleted);
            System.out.println("Class Sessions Completed: " + classSessionsCompleted);
            System.out.println("Balance: $" + calculateTotalCost(conn, memberId));
            System.out.println();
        }
    
        if (!memberFound) {
            System.out.println("No member found with name: " + name);
        }
    
        memberResult.close();
        memberStatement.close();
    }
    
    public static int countTrainerSessions(Connection conn, int memberId) throws SQLException {
        String query = "SELECT COUNT(*) FROM TrainerSchedules WHERE member_id = ? AND availability_date < CURRENT_DATE";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setInt(1, memberId);
        ResultSet result = statement.executeQuery();
        result.next();
        int count = result.getInt(1);
        result.close();
        statement.close();
        return count;
    }

    public static void listUserSessions(Connection conn, int memberId) throws SQLException {
        String query = "SELECT * FROM TrainerSchedules WHERE member_id = ?";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setInt(1, memberId);
        ResultSet result = statement.executeQuery();
        System.out.println("\n------------------------------------");
        System.out.println("Scheduled Sessions:");
        while (result.next()) {
            System.out.println("\nSession ID: " + result.getInt("availability_id"));
            System.out.println("Availability Date: " + result.getDate("availability_date"));
            System.out.println("Start Time: " + result.getTime("start_time"));
            System.out.println("End Time: " + result.getTime("end_time"));
            System.out.println("Cost: $" + result.getFloat("cost"));
            System.out.println();
        }
    
        result.close();
        statement.close();
    }
    
    
    public static int countClassSessions(Connection conn, int memberId) throws SQLException {
        String query = "SELECT COUNT(*) FROM ClassRegistrations " +
                       "JOIN ClassSchedules ON ClassRegistrations.class_id = ClassSchedules.class_id " +
                       "WHERE ClassRegistrations.member_id = ? AND ClassSchedules.end_date < CURRENT_DATE";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setInt(1, memberId);
        ResultSet result = statement.executeQuery();
        result.next();
        int count = result.getInt(1);
        result.close();
        statement.close();
        return count;
    }    

    public static void listUserClassSessions(Connection conn, int memberId) throws SQLException {
        String query = "SELECT * FROM ClassRegistrations " +
                       "JOIN ClassSchedules ON ClassRegistrations.class_id = ClassSchedules.class_id " +
                       "WHERE ClassRegistrations.member_id = ?";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setInt(1, memberId);
        ResultSet result = statement.executeQuery();
    
        System.out.println("\n------------------------------------");
        System.out.println("Booked Classes:");
        while (result.next()) {
            System.out.println("\nClass ID: " + result.getInt("class_id"));
            System.out.println("Class Name: " + result.getString("name"));
            System.out.println("Start Date: " + result.getDate("start_date"));
            System.out.println("End Date: " + result.getDate("end_date"));
            System.out.println("Start Time: " + result.getTime("start_time"));
            System.out.println("End Time: " + result.getTime("end_time"));
            System.out.println("Day of Week: " + result.getString("day_of_week"));
            System.out.println("Participants: " + result.getInt("participants"));
            System.out.println("Cost: $" + result.getFloat("cost"));
            // Print other class details as needed
            System.out.println();
        }
    
        result.close();
        statement.close();
    }
    

    // Method to update member field
    public static void updateMemberField(Connection conn, int id, int field, String change) throws  SQLException{
        String query;
        PreparedStatement statement;
        switch (field) {
            case 1:
                query = "UPDATE Members SET email = ? WHERE member_id = ?";
                statement = conn.prepareStatement(query);
                statement.setString(1, change);
                statement.setInt(2, id);
                statement.executeUpdate();
                statement.close();
                break;
            case 2:
                query = "UPDATE Members SET password = ? WHERE member_id = ?";
                statement = conn.prepareStatement(query);
                statement.setString(1, change);
                statement.setInt(2, id);
                statement.executeUpdate();
                statement.close();
                break;
            
            case 3:
                query = "UPDATE Members SET name = ? WHERE member_id = ?";
                statement = conn.prepareStatement(query);
                statement.setString(1, change);
                statement.setInt(2, id);
                statement.executeUpdate();
                statement.close();
                break;

            case 4:
                query = "UPDATE Members SET age = ? WHERE member_id = ?";
                statement = conn.prepareStatement(query);
                statement.setInt(1, Integer.valueOf(change));
                statement.setInt(2, id);
                statement.executeUpdate();
                statement.close();
                break;

            case 5:
                query = "UPDATE Members SET weight = ? WHERE member_id = ?";
                statement = conn.prepareStatement(query);
                statement.setInt(1, Integer.valueOf(change));
                statement.setInt(2, id);
                statement.executeUpdate();
                statement.close();
                break;

            case 6:
                query = "UPDATE Members SET height = ? WHERE member_id = ?";
                statement = conn.prepareStatement(query);
                statement.setInt(1, Integer.valueOf(change));
                statement.setInt(2, id);
                statement.executeUpdate();
                statement.close();
                break;

            case 7:
                query = "UPDATE Members SET goal_date = ? WHERE member_id = ?";
                statement = conn.prepareStatement(query);
                statement.setDate(1, Date.valueOf(change));
                statement.setInt(2, id);
                statement.executeUpdate();
                statement.close();
                break;

            case 8:
                query = "UPDATE Members SET goal_weight = ? WHERE member_id = ?";
                statement = conn.prepareStatement(query);
                statement.setInt(1, Integer.valueOf(change));
                statement.setInt(2, id);
                statement.executeUpdate();
                statement.close();
                break;
        }
    }

    // Method to create a new Trainer
    public static void createTrainer(Connection conn, String email, String password, String name) throws  SQLException{
        String query = "INSERT INTO Trainers (email, password, name) VALUES (?, ?, ?)";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setString(1, email);
        statement.setString(2, password);
        statement.setString(3, name);
        statement.executeUpdate();
        statement.close();
        System.out.println("Trainer " + name + " created successfully!");
    }

    // Method to retrieve all Trainers information
    public static void getAllTrainers(Connection conn) throws  SQLException{
        String query = "SELECT * FROM Trainers";
        Statement statement = conn.createStatement();
        ResultSet result = statement.executeQuery(query);
        System.out.println("\n------------------------------------");
        System.out.println("Trainers: ");
        while (result.next()) {
            System.out.println("\nId: " + result.getInt("trainer_id"));
            System.out.println("Email: " + result.getString("email"));
            System.out.println("Name: " + result.getString("name"));
        }
        result.close();
        statement.close();
    }

    // Method to retrieve Trainer information by ID along with availabilities where member_id isn't null
    public static void getTrainerById(Connection conn, int id) throws SQLException {
        String trainerQuery = "SELECT trainer_id, email, name FROM Trainers WHERE trainer_id = ?";
        String availabilityQuery = "SELECT * FROM TrainerSchedules WHERE trainer_id = ? AND member_id IS NULL";

        PreparedStatement trainerStatement = conn.prepareStatement(trainerQuery);
        trainerStatement.setInt(1, id);
        ResultSet trainerResult = trainerStatement.executeQuery();

        if (trainerResult.next()) {
            System.out.println("Trainer ID: " + trainerResult.getInt("trainer_id"));
            System.out.println("Trainer Email: " + trainerResult.getString("email"));
            System.out.println("Trainer Name: " + trainerResult.getString("name"));
        } 
        else {
            System.out.println("Trainer with ID " + id + " not found.");
            return;
        }

        PreparedStatement availabilityStatement = conn.prepareStatement(availabilityQuery);
        availabilityStatement.setInt(1, id);
        ResultSet availabilityResult = availabilityStatement.executeQuery();

        System.out.println("\nAvailability details:");
        while (availabilityResult.next()) {
            System.out.println("\nAvailability ID: " + availabilityResult.getInt("availability_id"));
            System.out.println("Availability Date: " + availabilityResult.getDate("availability_date"));
            System.out.println("Start Time: " + availabilityResult.getTime("start_time"));
            System.out.println("End Time: " + availabilityResult.getTime("end_time"));
            System.out.println("Cost: $" + availabilityResult.getFloat("cost"));
            System.out.println();
        }
    }

    // Method to add Trainer availability
    public static void addTrainerAvailability(Connection conn, int trainerId, String date, String startTime, Float cost) throws SQLException {
        if(!checkTrainerTimeConflict(conn, trainerId, date, startTime)) {
            String query = "INSERT INTO trainerschedules (trainer_id, member_id, availability_date, start_time, end_time, cost, paid) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, trainerId);
            statement.setObject(2, null);
            statement.setDate(3, Date.valueOf(date));
            statement.setTime(4, Time.valueOf(startTime));
            statement.setTime(5, Time.valueOf(calculateEndTime(startTime)));
            statement.setFloat(6, cost);
            statement.setBoolean(7, false);
            statement.executeUpdate();
            statement.close();
            System.out.println("Trainer availability added successfully!");
        }
    }

    public static boolean checkTrainerTimeConflict(Connection conn, int trainerId, String date, String startTime) throws SQLException {
        Time startTimeValue = Time.valueOf(startTime);
        Time workingHoursStart = Time.valueOf("09:00:00");
        Time workingHoursEnd = Time.valueOf("19:00:00");
        if (startTimeValue.before(workingHoursStart) || startTimeValue.after(workingHoursEnd)) {
            System.out.println("Error: Session must be within working hours (9:00 AM to 7:00 PM)");
            return true;
        }

        String query = "SELECT * FROM trainerschedules WHERE trainer_id = ? AND availability_date = ? " +
                       "AND (start_time = ? OR start_time = ? OR start_time = ?)";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setInt(1, trainerId);
        statement.setDate(2, Date.valueOf(date));
        statement.setTime(3, startTimeValue);
        statement.setTime(4, Time.valueOf(add30Minutes(startTimeValue)));
        statement.setTime(5, Time.valueOf(subtract30Minutes(startTimeValue)));
        ResultSet result = statement.executeQuery();
    
        if (result.next()) {
            System.out.println("Error: Time conflict detected. Trainer already has an availability at this time.");
            return true;
        }
    
        return false;
    }
    
    public static String add30Minutes(Time time) {
        long timeInMillis = time.getTime();
        timeInMillis += 30 * 60 * 1000;
        return new Time(timeInMillis).toString();
    }
    
    public static String subtract30Minutes(Time time) {
        long timeInMillis = time.getTime();
        timeInMillis -= 30 * 60 * 1000; 
        return new Time(timeInMillis).toString();
    }

    // Method to book Trainer session
    public static void bookTrainerSession(Connection conn, int sessionId, int memberId) throws SQLException {
        String query = "UPDATE trainerschedules SET member_id = ? WHERE availability_id = ?";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setInt(1, memberId);
        statement.setInt(2, sessionId);
        statement.executeUpdate();
        statement.close();
        System.out.println("Session booked!");
    }

    public static void removeMemberFromSession(Connection conn, int sessionId) throws SQLException {
        String query = "UPDATE TrainerSchedules SET member_id = NULL WHERE availability_id = ?";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setInt(1, sessionId);
        statement.executeUpdate();
        statement.close();
    }    

    // Method to create a new Admin
    public static void createAdmin(Connection conn, String email, String password, String name) throws  SQLException{
        String query = "INSERT INTO Admins (email, password, name) VALUES (?, ?, ?)";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setString(1, email);
        statement.setString(2, password);
        statement.setString(3, name);
        statement.executeUpdate();
        statement.close();
        System.out.println("Admin " + name + " created successfully!");
    }

    public static void addRoom(Connection conn, String roomName, int maxCapacity) throws SQLException {
        String query = "INSERT INTO Rooms (room_name, max_capacity) VALUES (?, ?)";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setString(1, roomName);
        statement.setInt(2, maxCapacity);
        statement.executeUpdate();
        statement.close();
        System.out.println("Room added successfully!");
    }

    public static void getAllRooms(Connection conn) throws  SQLException{
        String query = "SELECT * FROM Rooms";
        PreparedStatement statement = conn.prepareStatement(query);
        ResultSet result = statement.executeQuery();
        System.out.println("\n------------------------------------");
        System.out.println("Rooms: ");
        while (result.next()) {
            System.out.println("Room Id: " + result.getInt("room_id"));
            System.out.println("Name: " + result.getString("room_name"));
            System.out.println("Max Capacity: " + result.getInt("max_capacity"));
        }
        result.close();
        statement.close();
    }

    public static void addClass(Connection conn, int trainerId, String className, int roomId, String startDate, String startTime, int numWeeks, Float cost) throws SQLException {
        if(!checkClassTimeConflict(conn, roomId, startDate, startTime, numWeeks) && !checkRoomTimeConflict(conn, roomId, startDate, startTime, numWeeks)) {
            String query = "INSERT INTO ClassSchedules (trainer_id, room_id, name, start_date, end_date, start_time, end_time, num_weeks, day_of_week, participants, cost) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, trainerId);
            statement.setInt(2, roomId);
            statement.setString(3, className);
            statement.setDate(4, Date.valueOf(startDate));
            statement.setDate(5, Date.valueOf(calculateEndDate(startDate, numWeeks)));
            statement.setTime(6, Time.valueOf(startTime));
            statement.setTime(7, Time.valueOf(calculateEndTime(startTime)));
            statement.setInt(8, numWeeks);
            statement.setString(9, getDayOfWeek(startDate).toString());
            statement.setInt(10, 0);
            statement.setFloat(11, cost);
            statement.executeUpdate();
            statement.close();
            System.out.println("Class added successfully!");
        }
    }

    public static void getAllClasses(Connection conn) throws  SQLException{
        String query = "SELECT * FROM ClassSchedules";
        PreparedStatement statement = conn.prepareStatement(query);
        ResultSet result = statement.executeQuery();
        while (result.next()) {
            System.out.println("Class Id: " + result.getInt("class_id"));
            System.out.println("Class Name: " + result.getString("name"));
            System.out.println("Trainer Id: " + getTrainerNameById(conn, result.getInt("trainer_id")));
            System.out.println("Room Id: " + getRoomNameById(conn, result.getInt("room_id")));
            System.out.println("Start Date: " + result.getDate("start_date"));
            System.out.println("End Date: " + result.getDate("end_date"));
            System.out.println("Start Time: " + result.getTime("start_time"));
            System.out.println("End Time: " + result.getTime("end_time"));
            System.out.println("Day of the Week: " + result.getString("day_of_week"));
            System.out.println("Number of Weeks: " + result.getInt("num_weeks"));
            System.out.println("Availability: " + result.getInt("participants") + "/" + getRoomMaxCapacityById(conn, result.getInt("room_id")));
            System.out.println("Cost: $" + result.getFloat("cost"));
        }
        result.close();
        statement.close();
    }

    public static int getRoomMaxCapacityById(Connection conn, int roomId) throws SQLException {
        String query = "SELECT max_capacity FROM Rooms WHERE room_id = ?";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setInt(1, roomId);
        ResultSet result = statement.executeQuery();
        if (result.next()) {
            return result.getInt("max_capacity");
        }
        return -1;
    }

    public static boolean checkClassTimeConflict(Connection conn, int roomId, String startDate, String startTime, int numWeeks) throws SQLException {
        Time startTimeValue = Time.valueOf(startTime);
        DayOfWeek startDayOfWeek = getDayOfWeek(startDate);
    
        Time workingHoursStart = Time.valueOf("09:00:00");
        Time workingHoursEnd = Time.valueOf("19:00:00");
        if (startTimeValue.before(workingHoursStart) || startTimeValue.after(workingHoursEnd)) {
            System.out.println("Error: Class must start within working hours (9:00 AM to 7:00 PM)");
            return true;
        }
    
        Time endTimeValue = Time.valueOf(calculateEndTime(startTime));
    
        String query = "SELECT * FROM ClassSchedules WHERE room_id = ? " +
                       "AND (start_date BETWEEN ? AND ? OR end_date BETWEEN ? AND ? OR (start_date <= ? AND end_date >= ?))";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setInt(1, roomId);
        statement.setDate(2, Date.valueOf(startDate));
        statement.setDate(3, Date.valueOf(calculateEndDate(startDate, numWeeks)));
        statement.setDate(4, Date.valueOf(startDate));
        statement.setDate(5, Date.valueOf(calculateEndDate(startDate, numWeeks)));
        statement.setDate(6, Date.valueOf(startDate));
        statement.setDate(7, Date.valueOf(calculateEndDate(startDate, numWeeks)));
        ResultSet result = statement.executeQuery();
    
        while (result.next()) {
            Time existingStartTime = result.getTime("start_time");
            Time existingEndTime = result.getTime("end_time");
            DayOfWeek existingDayOfWeek = DayOfWeek.valueOf(result.getString("day_of_week"));
    
            if ((startTimeValue.equals(existingStartTime) || startTimeValue.after(existingStartTime)) &&
                (startTimeValue.before(existingEndTime))) {
                if (existingDayOfWeek == startDayOfWeek) {
                    System.out.println("Error: Time and day conflict detected. Room already booked at this time.");
                    return true;
                }
            }
    
            if ((endTimeValue.equals(existingEndTime) || endTimeValue.before(existingEndTime)) &&
                (endTimeValue.after(existingStartTime))) {
                if (existingDayOfWeek == startDayOfWeek) {
                    System.out.println("Error: Time and day conflict detected. Room already booked at this time.");
                    return true;
                }
            }
    
            if ((existingStartTime.after(startTimeValue) || existingStartTime.equals(startTimeValue)) &&
                (existingEndTime.before(endTimeValue) || existingEndTime.equals(endTimeValue))) {
                if (existingDayOfWeek == startDayOfWeek) {
                    System.out.println("Error: Time and day conflict detected. Room already booked at this time.");
                    return true;
                }
            }
        }
    
        return false;
    }    

    public static boolean checkRoomTimeConflict(Connection conn, int roomId, String startDate, String startTime, int numWeeks) throws SQLException {
        Time startTimeValue = Time.valueOf(startTime);
        DayOfWeek startDayOfWeek = getDayOfWeek(startDate);
    
        Time workingHoursStart = Time.valueOf("09:00:00");
        Time workingHoursEnd = Time.valueOf("19:00:00");
        if (startTimeValue.before(workingHoursStart) || startTimeValue.after(workingHoursEnd)) {
            System.out.println("Error: Room must be booked within working hours (9:00 AM to 7:00 PM)");
            return true;
        }
    
        Time endTimeValue = Time.valueOf(calculateEndTime(startTime));
    
        String query = "SELECT * FROM RoomBookings WHERE room_id = ? " +
                       "AND (start_date BETWEEN ? AND ? OR end_date BETWEEN ? AND ? OR (start_date <= ? AND end_date >= ?))";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setInt(1, roomId);
        statement.setDate(2, Date.valueOf(startDate));
        statement.setDate(3, Date.valueOf(calculateEndDate(startDate, numWeeks)));
        statement.setDate(4, Date.valueOf(startDate));
        statement.setDate(5, Date.valueOf(calculateEndDate(startDate, numWeeks)));
        statement.setDate(6, Date.valueOf(startDate));
        statement.setDate(7, Date.valueOf(calculateEndDate(startDate, numWeeks)));
        ResultSet result = statement.executeQuery();
    
        while (result.next()) {
            Time existingStartTime = result.getTime("start_time");
            Time existingEndTime = result.getTime("end_time");
            DayOfWeek existingDayOfWeek = DayOfWeek.valueOf(result.getString("day_of_week"));

            if ((startTimeValue.equals(existingStartTime) || startTimeValue.after(existingStartTime)) &&
                (startTimeValue.before(existingEndTime))) {
                if (existingDayOfWeek == startDayOfWeek) {
                    System.out.println("Error: Time and day conflict detected. Room already booked at this time.");
                    return true;
                }
            }
    
            if ((endTimeValue.equals(existingEndTime) || endTimeValue.before(existingEndTime)) &&
                (endTimeValue.after(existingStartTime))) {
                if (existingDayOfWeek == startDayOfWeek) {
                    System.out.println("Error: Time and day conflict detected. Room already booked at this time.");
                    return true;
                }
            }
    
            if ((existingStartTime.after(startTimeValue) || existingStartTime.equals(startTimeValue)) &&
                (existingEndTime.before(endTimeValue) || existingEndTime.equals(endTimeValue))) {
                if (existingDayOfWeek == startDayOfWeek) {
                    System.out.println("Error: Time and day conflict detected. Room already booked at this time.");
                    return true;
                }
            }
        }
    
        return false;
    }       

    public static DayOfWeek getDayOfWeek(String date) {
        LocalDate localDate = LocalDate.parse(date);
        return localDate.getDayOfWeek();
    }

    public static String getTrainerNameById(Connection conn, int trainerId) throws SQLException {
        String query = "SELECT name FROM Trainers WHERE trainer_id = ?";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setInt(1, trainerId);
        ResultSet result = statement.executeQuery();
        if (result.next()) {
            return result.getString("name");
        }
        return null; 
    }
    
    // Method to get the room name by ID
    public static String getRoomNameById(Connection conn, int roomId) throws SQLException {
        String query = "SELECT room_name FROM Rooms WHERE room_id = ?";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setInt(1, roomId);
        ResultSet result = statement.executeQuery();
        if (result.next()) {
            return result.getString("room_name");
        }
        return null;
    }

    private static String calculateEndDate(String startDate, int numberOfWeeks) {
        java.time.LocalDate localDate = java.time.LocalDate.parse(startDate);
        localDate = localDate.plusWeeks(numberOfWeeks);
        return localDate.toString();
    }

    private static String calculateEndTime(String startTime) {
        java.time.LocalTime localTime = java.time.LocalTime.parse(startTime);
        localTime = localTime.plusHours(1);
        return localTime.toString() + ":00";
    }

    public static void addClassRegistration(Connection conn, int memberId, int classId) throws SQLException {
        String checkQuery = "SELECT * FROM ClassRegistrations WHERE member_id = ? AND class_id = ?";
        PreparedStatement checkStatement = conn.prepareStatement(checkQuery);
        checkStatement.setInt(1, memberId);
        checkStatement.setInt(2, classId);
        ResultSet resultSet = checkStatement.executeQuery();
        if (resultSet.next()) {
            System.out.println("You are already registered for this class.");
            return; 
        }
        checkStatement.close();
    
        String updateQuery = "UPDATE ClassSchedules SET participants = participants + 1 WHERE class_id = ?";
        PreparedStatement updateStatement = conn.prepareStatement(updateQuery);
        updateStatement.setInt(1, classId);
        updateStatement.executeUpdate();
        updateStatement.close();
        
        String insertQuery = "INSERT INTO ClassRegistrations (member_id, class_id, paid) VALUES (?, ?, ?)";
        PreparedStatement insertStatement = conn.prepareStatement(insertQuery);
        insertStatement.setInt(1, memberId);
        insertStatement.setInt(2, classId);
        insertStatement.setBoolean(3, false);
        insertStatement.executeUpdate();
        insertStatement.close();
    
        System.out.println("Class successfully registered!");
    }    
    
    public static void removeClassRegistration(Connection conn, int memberId, int classId) throws SQLException {
        String checkQuery = "SELECT * FROM ClassRegistrations WHERE member_id = ? AND class_id = ?";
        PreparedStatement checkStatement = conn.prepareStatement(checkQuery);
        checkStatement.setInt(1, memberId);
        checkStatement.setInt(2, classId);
        ResultSet resultSet = checkStatement.executeQuery();
        if (!resultSet.next()) {
            System.out.println("You are not registered for this class.");
            return;
        }
        checkStatement.close();
    
        String deleteQuery = "DELETE FROM ClassRegistrations WHERE member_id = ? AND class_id = ?";
        PreparedStatement deleteStatement = conn.prepareStatement(deleteQuery);
        deleteStatement.setInt(1, memberId);
        deleteStatement.setInt(2, classId);
        deleteStatement.executeUpdate();
        deleteStatement.close();
    
        String updateQuery = "UPDATE ClassSchedules SET participants = participants - 1 WHERE class_id = ?";
        PreparedStatement updateStatement = conn.prepareStatement(updateQuery);
        updateStatement.setInt(1, classId);
        updateStatement.executeUpdate();
        updateStatement.close();
    
        System.out.println("Class registration removed successfully!");
    }
    
    public static void bookRoom(Connection conn, int roomId, String name, String date, String time) throws SQLException {
        if(!checkClassTimeConflict(conn, roomId, date, time, 1) && !checkRoomTimeConflict(conn, roomId, date, time, 1)) {    
            String query = "INSERT INTO RoomBookings (room_id, name, start_date, end_date, start_time, end_time, day_of_week) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, roomId);
            statement.setString(2, name);
            statement.setDate(3, Date.valueOf(date));
            statement.setDate(4, Date.valueOf(date));
            statement.setTime(5, Time.valueOf(time));
            statement.setTime(6, Time.valueOf(calculateEndTime(time)));
            statement.setString(7, getDayOfWeek(date).toString());
            statement.executeUpdate();
            statement.close();
            System.out.println("Room successfully booked!");
        }
    }

    public static void getAllBookedRooms(Connection conn) throws SQLException {
        String query = "SELECT " +
                       "'Class' AS type, " +
                       "c.class_id AS id, " +
                       "c.name AS name, " +
                       "c.room_id AS room, " +
                       "c.start_date AS start_date, " +
                       "c.start_time AS start_time, " +
                       "c.end_time AS end_time, " +
                       "c.end_date AS end_date, " +
                       "c.day_of_week AS day_of_week, " +
                       "c.name AS event_name " +
                       "FROM ClassSchedules c " +
                       "UNION " +
                       "SELECT " +
                       "'Room Booking' AS type, " +
                       "r.booking_id AS id, " +
                       "r.name AS name, " +
                       "r.room_id AS room, " +
                       "r.start_date AS start_date, " +
                       "r.start_time AS start_time, " +
                       "r.end_time AS end_time, " +
                       "r.end_date AS end_date, " +
                       "r.day_of_week AS day_of_week, " +
                       "null AS event_name " +
                       "FROM RoomBookings r " +
                       "ORDER BY start_date";
    
        PreparedStatement statement = conn.prepareStatement(query);
        ResultSet result = statement.executeQuery();
        System.out.println("\n------------------------------------");
        System.out.println("Booked Rooms: ");
    
        while (result.next()) {
            String type = result.getString("type");
            int id = result.getInt("id");
            String name = result.getString("name");
            int room = result.getInt("room");
            Date startDate = result.getDate("start_date");
            Time startTime = result.getTime("start_time");
            Time endTime = result.getTime("end_time");
            Date endDate = result.getDate("end_date");
            String dayOfWeek = result.getString("day_of_week");
    
            System.out.println("Type: " + type);
            System.out.println("ID: " + id);
            System.out.println("Name: " + name);
            System.out.println("Room: " + room);
            System.out.println("Day of Week: " + dayOfWeek);
            System.out.println("Start Date: " + startDate);
            System.out.println("Start Time: " + startTime);
            System.out.println("End Time: " + endTime);
            System.out.println("End Date: " + endDate);
        }
    
        result.close();
        statement.close();
    }    

    public static boolean checkUpdateClass(Connection conn, int roomId, String startDate, String startTime, int numWeeks, int classId) throws SQLException {
        Time startTimeValue = Time.valueOf(startTime);
        DayOfWeek startDayOfWeek = getDayOfWeek(startDate);
    
        Time workingHoursStart = Time.valueOf("09:00:00");
        Time workingHoursEnd = Time.valueOf("19:00:00");
        if (startTimeValue.before(workingHoursStart) || startTimeValue.after(workingHoursEnd)) {
            System.out.println("Error: Class must start within working hours (9:00 AM to 7:00 PM)");
            return true;
        }
    
        Time endTimeValue = Time.valueOf(calculateEndTime(startTime));
    
        String query = "SELECT * FROM ClassSchedules WHERE room_id = ? " +
                       "AND class_id != ? " +
                       "AND (start_date BETWEEN ? AND ? OR end_date BETWEEN ? AND ? OR (start_date <= ? AND end_date >= ?))";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setInt(1, roomId);
        statement.setInt(2, classId);
        statement.setDate(3, Date.valueOf(startDate));
        statement.setDate(4, Date.valueOf(calculateEndDate(startDate, numWeeks)));
        statement.setDate(5, Date.valueOf(startDate));
        statement.setDate(6, Date.valueOf(calculateEndDate(startDate, numWeeks)));
        statement.setDate(7, Date.valueOf(startDate));
        statement.setDate(8, Date.valueOf(calculateEndDate(startDate, numWeeks)));
        ResultSet result = statement.executeQuery();
    
        while (result.next()) {
            Time existingStartTime = result.getTime("start_time");
            Time existingEndTime = result.getTime("end_time");
            DayOfWeek existingDayOfWeek = DayOfWeek.valueOf(result.getString("day_of_week"));
    
            if ((startTimeValue.equals(existingStartTime) || startTimeValue.after(existingStartTime)) &&
                (startTimeValue.before(existingEndTime))) {
                if (existingDayOfWeek == startDayOfWeek) {
                    System.out.println("Error: Time and day conflict detected. Room already booked at this time.");
                    return true;
                }
            }
    
            if ((endTimeValue.equals(existingEndTime) || endTimeValue.before(existingEndTime)) &&
                (endTimeValue.after(existingStartTime))) {
                if (existingDayOfWeek == startDayOfWeek) {
                    System.out.println("Error: Time and day conflict detected. Room already booked at this time.");
                    return true;
                }
            }
    
            if ((existingStartTime.after(startTimeValue) || existingStartTime.equals(startTimeValue)) &&
                (existingEndTime.before(endTimeValue) || existingEndTime.equals(endTimeValue))) {
                if (existingDayOfWeek == startDayOfWeek) {
                    System.out.println("Error: Time and day conflict detected. Room already booked at this time.");
                    return true;
                }
            }
        }
    
        return false;
    }
    
    public static boolean checkUpdateRoom(Connection conn, int roomId, String startDate, String startTime, int numWeeks, int classId) throws SQLException {
        Time startTimeValue = Time.valueOf(startTime);
        DayOfWeek startDayOfWeek = getDayOfWeek(startDate);
    
        Time workingHoursStart = Time.valueOf("09:00:00");
        Time workingHoursEnd = Time.valueOf("19:00:00");
        if (startTimeValue.before(workingHoursStart) || startTimeValue.after(workingHoursEnd)) {
            System.out.println("Error: Room must be booked within working hours (9:00 AM to 7:00 PM)");
            return true;
        }
    
        Time endTimeValue = Time.valueOf(calculateEndTime(startTime));
    
        String query = "SELECT * FROM RoomBookings WHERE room_id = ? " +
                       "AND booking_id != ? " +
                       "AND (start_date BETWEEN ? AND ? OR end_date BETWEEN ? AND ? OR (start_date <= ? AND end_date >= ?))";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setInt(1, roomId);
        statement.setInt(2, classId);
        statement.setDate(3, Date.valueOf(startDate));
        statement.setDate(4, Date.valueOf(calculateEndDate(startDate, numWeeks)));
        statement.setDate(5, Date.valueOf(startDate));
        statement.setDate(6, Date.valueOf(calculateEndDate(startDate, numWeeks)));
        statement.setDate(7, Date.valueOf(startDate));
        statement.setDate(8, Date.valueOf(calculateEndDate(startDate, numWeeks)));
        ResultSet result = statement.executeQuery();
    
        while (result.next()) {
            Time existingStartTime = result.getTime("start_time");
            Time existingEndTime = result.getTime("end_time");
            DayOfWeek existingDayOfWeek = DayOfWeek.valueOf(result.getString("day_of_week"));
    
            if ((startTimeValue.equals(existingStartTime) || startTimeValue.after(existingStartTime)) &&
                (startTimeValue.before(existingEndTime))) {
                if (existingDayOfWeek == startDayOfWeek) {
                    System.out.println("Error: Time and day conflict detected. Room already booked at this time.");
                    return true;
                }
            }
    
            if ((endTimeValue.equals(existingEndTime) || endTimeValue.before(existingEndTime)) &&
                (endTimeValue.after(existingStartTime))) {
                if (existingDayOfWeek == startDayOfWeek) {
                    System.out.println("Error: Time and day conflict detected. Room already booked at this time.");
                    return true;
                }
            }
    
            if ((existingStartTime.after(startTimeValue) || existingStartTime.equals(startTimeValue)) &&
                (existingEndTime.before(endTimeValue) || existingEndTime.equals(endTimeValue))) {
                if (existingDayOfWeek == startDayOfWeek) {
                    System.out.println("Error: Time and day conflict detected. Room already booked at this time.");
                    return true;
                }
            }
        }
    
        return false;
    }
    
    // Method to update class field
    public static void updateClassField(Connection conn, int id, int field, String change) throws SQLException {
        String query = null;
        PreparedStatement statement;

        String classInfoQuery = "SELECT num_weeks, start_date, start_time, room_id FROM ClassSchedules WHERE class_id = ?";
        PreparedStatement classInfoStatement = conn.prepareStatement(classInfoQuery);
        classInfoStatement.setInt(1, id);
        ResultSet classInfoResult = classInfoStatement.executeQuery();

        int numWeeks = 0;
        int roomId = 0;
        String startDate = null;
        String startTime = null;

        if (classInfoResult.next()) {
            numWeeks = classInfoResult.getInt("num_weeks");
            roomId = classInfoResult.getInt("room_id");
            startDate = classInfoResult.getString("start_date");
            startTime = classInfoResult.getString("start_time");
        }

        classInfoResult.close();
        classInfoStatement.close();

        switch (field) {
            case 1:
                if (!checkUpdateClass(conn, roomId, change, startTime, numWeeks, id) && !checkRoomTimeConflict(conn, roomId, change, startTime, numWeeks)) {
                    query = "UPDATE ClassSchedules SET start_date = ?, end_date = ?, day_of_week = ? WHERE class_id = ?";
                    statement = conn.prepareStatement(query);
                    statement.setDate(1, Date.valueOf(change));
                    statement.setDate(2, Date.valueOf(calculateEndDate(change, numWeeks)));
                    statement.setString(3, getDayOfWeek(startDate).toString());
                    statement.setInt(4, id);
                    statement.executeUpdate();
                    statement.close();
                }
                break;

            case 2:
                if (!checkUpdateClass(conn, roomId, startDate, change, numWeeks, id) && !checkRoomTimeConflict(conn, roomId, startDate, change, numWeeks)) {
                    query = "UPDATE ClassSchedules SET start_time = ?, end_time = ?, day_of_week = ? WHERE class_id = ?";
                    statement = conn.prepareStatement(query);
                    statement.setTime(1, Time.valueOf(change));
                    statement.setTime(2, Time.valueOf(calculateEndTime(change)));
                    statement.setString(3, getDayOfWeek(startDate).toString());
                    statement.setInt(4, id);
                    statement.executeUpdate();
                    statement.close();
                }
                break;

            case 3:
                if (!checkUpdateClass(conn, roomId, startDate, startTime, Integer.valueOf(change), id) && !checkRoomTimeConflict(conn, roomId, startDate, startTime, Integer.valueOf(change))) {
                    query = "UPDATE ClassSchedules SET num_weeks = ?, end_date = ? WHERE class_id = ?";
                    statement = conn.prepareStatement(query);
                    statement.setInt(1, Integer.valueOf(change));
                    statement.setDate(2, Date.valueOf(calculateEndDate(startDate, Integer.valueOf(change))));
                    statement.setInt(3, id);
                    statement.executeUpdate();
                    statement.close();
                }
                break;

            case 4:
                if (!checkUpdateClass(conn, Integer.valueOf(change), startDate, startTime, numWeeks, id) && !checkRoomTimeConflict(conn, Integer.valueOf(change), startDate, startTime, numWeeks)) {
                    query = "UPDATE ClassSchedules SET room_id = ? WHERE class_id = ?";
                    statement = conn.prepareStatement(query);
                    statement.setInt(1, Integer.valueOf(change));
                    statement.setInt(2, id);
                    statement.executeUpdate();
                    statement.close();
                }
                break;

            case 5:
                query = "UPDATE ClassSchedules SET name = ? WHERE class_id = ?";
                statement = conn.prepareStatement(query);
                statement.setString(1, change);
                statement.setInt(2, id);
                statement.executeUpdate();
                statement.close();
                break;

            case 6:
                query = "UPDATE ClassSchedules SET cost = ? WHERE class_id = ?";
                statement = conn.prepareStatement(query);
                statement.setFloat(1, Float.valueOf(change));
                statement.setInt(2, id);
                statement.executeUpdate();
                statement.close();
                break;
        }
    }

    public static void updateRoomField(Connection conn, int id, int field, String change) throws SQLException {
        String query = null;
        PreparedStatement statement;

        String classInfoQuery = "SELECT start_date, start_time, room_id FROM RoomBookings WHERE booking_id = ?";
        PreparedStatement classInfoStatement = conn.prepareStatement(classInfoQuery);
        classInfoStatement.setInt(1, id);
        ResultSet classInfoResult = classInfoStatement.executeQuery();

        int numWeeks = 1;
        int roomId = 0;
        String startDate = null;
        String startTime = null;

        if (classInfoResult.next()) {
            roomId = classInfoResult.getInt("room_id");
            startDate = classInfoResult.getString("start_date");
            startTime = classInfoResult.getString("start_time");
        }

        classInfoResult.close();
        classInfoStatement.close();

        switch (field) {
            case 1:
                if (!checkUpdateRoom(conn, roomId, change, startTime, numWeeks, id) && !checkClassTimeConflict(conn, roomId, change, startTime, numWeeks)) {
                    query = "UPDATE RoomBookings SET start_date = ?, end_date = ?, day_of_week = ? WHERE booking_id = ?";
                    statement = conn.prepareStatement(query);
                    statement.setDate(1, Date.valueOf(change));
                    statement.setDate(2, Date.valueOf(change));
                    statement.setString(3, getDayOfWeek(startDate).toString());
                    statement.setInt(4, id);
                    statement.executeUpdate();
                    statement.close();
                }
                break;
            
            case 2:
                if (!checkUpdateRoom(conn, roomId, startDate, change, numWeeks, id) && !checkClassTimeConflict(conn, roomId, startDate, change, numWeeks)) {
                    query = "UPDATE RoomBookings SET start_time = ?, end_time = ?, day_of_week = ? WHERE booking_id = ?";
                    statement = conn.prepareStatement(query);
                    statement.setTime(1, Time.valueOf(change));
                    statement.setTime(2, Time.valueOf(calculateEndTime(change)));
                    statement.setString(3, getDayOfWeek(startDate).toString());
                    statement.setInt(4, id);
                    statement.executeUpdate();
                    statement.close();
                }
                break;

            case 3:
                if (!checkUpdateRoom(conn, Integer.valueOf(change), startDate, startTime, numWeeks, id) && !checkClassTimeConflict(conn, Integer.valueOf(change), startDate, startTime, numWeeks)) {
                    query = "UPDATE RoomBookings SET room_id = ? WHERE booking_id = ?";
                    statement = conn.prepareStatement(query);
                    statement.setInt(1, Integer.valueOf(change));
                    statement.setInt(2, id);
                    statement.executeUpdate();
                    statement.close();
                }
                break;

            case 4:
                query = "UPDATE RoomBookings SET name = ? WHERE booking_id = ?";
                statement = conn.prepareStatement(query);
                statement.setString(1, change);
                statement.setInt(2, id);
                statement.executeUpdate();
                statement.close();
                break;
        }
    }
    
    public static void addEquipment(Connection conn, int roomId, String name) throws SQLException {
        String query = "INSERT INTO Equipment (room_id, name) VALUES (?, ?)";
        PreparedStatement statement = conn.prepareStatement(query);           
        statement.setInt(1, roomId);
        statement.setString(2, name);
        statement.executeUpdate();
        System.out.println("Equipment added successfully.");
    }

    public static void addMaintenance(Connection conn, int equipmentId, String startDate, String endDate) throws SQLException {
        String query = "UPDATE Equipment SET maintenance_start_date = ?, maintenance_end_date = ? WHERE equipment_id = ?";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setDate(1, Date.valueOf(startDate));
        statement.setDate(2, Date.valueOf(endDate));
        statement.setInt(3, equipmentId);
        statement.executeUpdate();
    }

    public static void deleteEquipment(Connection conn, int equipmentId) throws SQLException {
        String query = "DELETE FROM Equipment WHERE equipment_id = ?";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setInt(1, equipmentId);
        statement.executeUpdate();
    }

    public static void getAllEquipment(Connection conn) throws SQLException {
        String query = "SELECT * FROM Equipment";
        PreparedStatement statement = conn.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            System.out.println("--------------------------------------");
            int equipmentId = resultSet.getInt("equipment_id");
            int roomId = resultSet.getInt("room_id");
            String name = resultSet.getString("name");
            java.sql.Date maintenanceStartDate = resultSet.getDate("maintenance_start_date");
            java.sql.Date maintenanceEndDate = resultSet.getDate("maintenance_end_date");

            System.out.println("Equipment ID: " + equipmentId);
            System.out.println("Room ID: " + roomId);
            System.out.println("Name: " + name);
            System.out.println("Maintenance Start Date: " + maintenanceStartDate);
            System.out.println("Maintenance End Date: " + maintenanceEndDate);
        }
    }

    public static boolean authenticateMember(Connection conn, String username, String password) throws SQLException {
        String query = "SELECT * FROM Members WHERE email = ? AND password = ?";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setString(1, username);
        statement.setString(2, password);
        ResultSet resultSet = statement.executeQuery();
        return resultSet.next();
    }

    public static boolean authenticateTrainer(Connection conn, String username, String password) throws SQLException {
        String query = "SELECT * FROM Trainers WHERE email = ? AND password = ?";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setString(1, username);
        statement.setString(2, password);
        ResultSet resultSet = statement.executeQuery();
        return resultSet.next();
    }

    public static boolean authenticateAdmin(Connection conn, String username, String password) throws SQLException {
        String query = "SELECT * FROM Admins WHERE email = ? AND password = ?";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setString(1, username);
        statement.setString(2, password);
        ResultSet resultSet = statement.executeQuery();
        return resultSet.next();
    }

    // Method to calculate total cost based on user ID
    public static float calculateTotalCost(Connection conn, int userId) throws SQLException {
        float totalCost = 0;

        String trainerQuery = "SELECT cost FROM TrainerSchedules WHERE member_id = ? AND paid = false";
        PreparedStatement trainerStatement = conn.prepareStatement(trainerQuery);
        trainerStatement.setInt(1, userId);
        ResultSet trainerResult = trainerStatement.executeQuery();
        while (trainerResult.next()) {
            totalCost += trainerResult.getFloat("cost");
        }

        String classQuery = "SELECT cost FROM ClassSchedules " +
                            "JOIN ClassRegistrations ON ClassSchedules.class_id = ClassRegistrations.class_id " +
                            "WHERE member_id = ? AND paid = false";
        PreparedStatement classStatement = conn.prepareStatement(classQuery);
        classStatement.setInt(1, userId);
        ResultSet classResult = classStatement.executeQuery();
        while (classResult.next()) {
            totalCost += classResult.getFloat("cost");
        }

        return totalCost;
    }

    public static void markAllPaid(Connection conn, int userId) throws SQLException {
        String trainerQuery = "UPDATE TrainerSchedules SET paid = true WHERE member_id = ? AND paid = false";
        PreparedStatement trainerStatement = conn.prepareStatement(trainerQuery);
        trainerStatement.setInt(1, userId);
        trainerStatement.executeUpdate();

        String classQuery = "UPDATE ClassRegistrations SET paid = true " +
                            "WHERE member_id = ? AND paid = false";
        PreparedStatement classStatement = conn.prepareStatement(classQuery);
        classStatement.setInt(1, userId);
        classStatement.executeUpdate();
    }

    public static int getMemberIdByEmail(Connection conn, String email) throws SQLException {
        String query = "SELECT member_id FROM Members WHERE email = ?";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setString(1, email);
        ResultSet resultSet = statement.executeQuery();
        if(resultSet.next()) {
            int userId = resultSet.getInt("member_id");
            statement.close();
            return userId;
        } 
        else {
            statement.close();
            return -1;
        }
    }

    public static int getTrainerIdByEmail(Connection conn, String email) throws SQLException {
        String query = "SELECT trainer_id FROM Trainers WHERE email = ?";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setString(1, email);
        ResultSet resultSet = statement.executeQuery();
        if(resultSet.next()) {
            int userId = resultSet.getInt("trainer_id");
            statement.close();
            return userId;
        } 
        else {
            statement.close();
            return -1;
        }
    }

    public static int getAdminIdByEmail(Connection conn, String email) throws SQLException {
        String query = "SELECT admin_id FROM Admins WHERE email = ?";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setString(1, email);
        ResultSet resultSet = statement.executeQuery();
        if(resultSet.next()) {
            int userId = resultSet.getInt("admin_id");
            statement.close();
            return userId;
        } 
        else {
            statement.close();
            return -1;
        }
    }

    private static boolean isValidTime(String time) {
        String[] parts = time.split(":");
        int minutes = Integer.parseInt(parts[1]);
        return minutes == 0 || minutes == 30;
    }

    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;

        int userType = -1;
        int userId = -1;

        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Connected to database");
            Scanner scanner = new Scanner(System.in);
    
            boolean exit = false;
            while (!exit ) {
                while (userType < 0 && !exit) {
                    System.out.println("\n------------------------------------");
                    System.out.println("Welcome to the Health and Fitness Club Management System (Open 9am - 8pm)");
                    System.out.println("1. Sign Up");
                    System.out.println("2. Login");
                    System.out.println("3. Exit");
                    System.out.print("Enter your choice: ");
        
                    int choice1 = scanner.nextInt();
                    scanner.nextLine();
        
                    if (choice1 == 1) {
                        System.out.println("\n------------------------------------");
                        System.out.println("1. Sign Up as Member");
                        System.out.println("2. Sign Up as Trainer");
                        System.out.println("3. Sign Up as Admin");
                        System.out.println("4. Back");
                        System.out.print("Enter your choice: ");
                        int choice2 = scanner.nextInt();
                        scanner.nextLine();
        
                        if (choice2 == 1) {
                            System.out.println("\n------------------------------------");
                            System.out.println("Enter your details:");
        
                            System.out.print("Email: ");
                            String email = scanner.nextLine();
        
                            System.out.print("Password: ");
                            String password = scanner.nextLine();
        
                            System.out.print("Name: ");
                            String name = scanner.nextLine();
        
                            System.out.print("Age: ");
                            int age = scanner.nextInt();
                            scanner.nextLine();

                            System.out.print("Weight (kg): ");
                            int weight = scanner.nextInt();
                            scanner.nextLine();

                            System.out.print("Height (cm): ");
                            int height = scanner.nextInt();
                            scanner.nextLine();

                            System.out.print("Goal Weight (kg): ");
                            int goalWeight = scanner.nextInt();
                            scanner.nextLine();

                            System.out.print("Goal Date (yyyy-mm-dd): ");
                            String goalDate = scanner.nextLine();
        
                            createMember(conn, email, password, name, age, weight, height, goalDate, goalWeight);
                        } 
                        else if (choice2 == 2) {
                            System.out.println("\n------------------------------------");
                            System.out.println("Enter your details:");
        
                            System.out.print("Email: ");
                            String email = scanner.nextLine();
        
                            System.out.print("Password: ");
                            String password = scanner.nextLine();
        
                            System.out.print("Name: ");
                            String name = scanner.nextLine();

                            createTrainer(conn, email, password, name);
                        } 
                        else if (choice2 == 3) {
                            System.out.println("\n------------------------------------");
                            System.out.println("Enter your details:");
        
                            System.out.print("Email: ");
                            String email = scanner.nextLine();
        
                            System.out.print("Password: ");
                            String password = scanner.nextLine();
        
                            System.out.print("Name: ");
                            String name = scanner.nextLine();

                            createAdmin(conn, email, password, name);
                        } 
                        else if (choice2 == 4) {
                            continue;
                        }
                        else {
                            System.out.println("Invalid choice. Please try again.");
                        }
                    } 
                    else if (choice1 == 2) {
                        boolean validated = false;
                        while (!validated) {
                            System.out.println("\n------------------------------------");
                            System.out.println("1. Login as Member");
                            System.out.println("2. Login as Trainer");
                            System.out.println("3. Login as Admin");
                            System.out.println("4. Back");
                            System.out.print("Enter your choice: ");
                            int choice2 = scanner.nextInt();
                            scanner.nextLine();

                            if (choice2 == 1) {
                                System.out.println("\n------------------------------------");
                                System.out.println("Enter your details:");
            
                                System.out.print("Email: ");
                                String email = scanner.nextLine();
            
                                System.out.print("Password: ");
                                String password = scanner.nextLine();
        
                                if(authenticateMember(conn, email, password)){
                                    validated = true;
                                    userType = 1;
                                    userId = getMemberIdByEmail(conn, email);
                                }
                                else {
                                    System.out.println("Authentication Failed. Please Try Again");
                                }
                            } 
                            else if (choice2 == 2) {
                                System.out.println("\n------------------------------------");
                                System.out.println("Enter your details:");
            
                                System.out.print("Email: ");
                                String email = scanner.nextLine();
            
                                System.out.print("Password: ");
                                String password = scanner.nextLine();
        
                                if(authenticateTrainer(conn, email, password)){
                                    validated = true;
                                    userType = 2;
                                    userId = getTrainerIdByEmail(conn, email);
                                }
                                else {
                                    System.out.println("Authentication Failed. Please Try Again");
                                }
                            } 
                            else if (choice2 == 3) {
                                System.out.println("\n------------------------------------");
                                System.out.println("Enter your details:");
            
                                System.out.print("Email: ");
                                String email = scanner.nextLine();
            
                                System.out.print("Password: ");
                                String password = scanner.nextLine();
        
                                if(authenticateAdmin(conn, email, password)){
                                    validated = true;
                                    userType = 3;
                                    userId = getAdminIdByEmail(conn, email);
                                }
                                else {
                                    System.out.println("Authentication Failed. Please Try Again");
                                }
                            } 
                            else if (choice2 == 4) {
                                break;
                            }
                            else {
                                System.out.println("Invalid choice. Please try again.");
                            }
                        }
                    } 
                    else if (choice1 == 3) {
                        exit = true;
                    } 
                    else {
                        System.out.println("Invalid choice. Please try again.");
                    }
                }
                while (userType == 1) {
                    System.out.println("\n------------------------------------");
                    System.out.println("Member Options: ");
                    System.out.println("1. View Dashboard");
                    System.out.println("2. Update Information");
                    System.out.println("3. View Trainers");
                    System.out.println("4. View Classes");
                    System.out.println("5. View Trainer Availability");
                    System.out.println("6. Schedule Trainer");
                    System.out.println("7. Signup For Class");
                    System.out.println("8. View Scheduled Training Sessions");
                    System.out.println("9. View Scheduled Classes");
                    System.out.println("10. Cancel or Reschedule Training Sessions");
                    System.out.println("11. Cancel Classes");
                    System.out.println("12. Pay Balance");
                    System.out.println("13. Logout");
                    System.out.print("Enter your choice: ");
        
                    int choice1 = scanner.nextInt();
                    scanner.nextLine();

                    if (choice1 == 1) {
                        getMemberById(conn, userId);
                    }
                    else if (choice1 == 2) {
                        System.out.println("\n------------------------------------");
                        System.out.println("Update Options: ");
                        System.out.println("1. Update Email");
                        System.out.println("2. Update Password");
                        System.out.println("3. Update Age");
                        System.out.println("4. Update Weight");
                        System.out.println("5. Update Height");
                        System.out.println("6. Update Goal Weight");
                        System.out.println("7. Update Goal Date (yyyy-mm-dd)");
                        System.out.println("8. Back");
                        System.out.print("Enter your choice: ");

                        int choice2 = scanner.nextInt();
                        scanner.nextLine();

                        if(choice2 < 8 && choice2 > 0) {
                            try {
                                System.out.print("Enter your new value: ");
                                String choice3 = scanner.nextLine();
                                updateMemberField(conn, userId, choice2, choice3);
                            }
                            catch (Exception e) {
                                System.out.println("Invalid Value");
                            }
                        }
                    }
                    else if (choice1 == 3) {
                        getAllTrainers(conn);
                    }
                    else if (choice1 == 4) {
                        getAllClasses(conn);
                    }
                    else if (choice1 == 5) {
                        System.out.println("\n------------------------------------");
                        System.out.print("Enter your trainer id: ");
                        int choice2 = scanner.nextInt();
                        scanner.nextLine();

                        try {
                            getTrainerById(conn, choice2);
                        }
                        catch (Exception e) {
                            System.out.println("No such trainer");
                        }
                    }
                    else if (choice1 == 6) {
                        System.out.println("\n------------------------------------");
                        System.out.print("Enter your session id: ");
                        int choice2 = scanner.nextInt();
                        scanner.nextLine();

                        try {
                            bookTrainerSession(conn, choice2, userId);
                        }
                        catch (Exception e) {
                            System.out.println("No such session");
                        }
                    }
                    else if (choice1 == 7) {
                        System.out.println("\n------------------------------------");
                        System.out.print("Enter your class id: ");
                        int choice2 = scanner.nextInt();
                        scanner.nextLine();

                        try {
                            addClassRegistration(conn, choice2, userId);
                        }
                        catch (Exception e) {
                            System.out.println("No such class");
                        }
                    }
                    else if (choice1 == 8) {
                        listUserSessions(conn, userId);
                    }
                    else if (choice1 == 9) {
                        listUserClassSessions(conn, userId);
                    }
                    else if (choice1 == 10) {
                        System.out.println("\n------------------------------------");
                        System.out.println("Options: ");
                        System.out.println("1. Cancel Session");
                        System.out.println("2. Reschedule Session");
                        System.out.println("3. Back");
                        int choice2 = scanner.nextInt();
                        scanner.nextLine();
                        if (choice2 == 3) {
                            continue;
                        }
                        else {
                            System.out.print("Enter the session id you would like to modify: ");
                            int id = scanner.nextInt();
                            scanner.nextLine();
                            removeMemberFromSession(conn, id);

                            if (choice2 == 2) {
                                getAllTrainers(conn);
                                System.out.println("\n------------------------------------");
                                System.out.print("Enter your trainer id: ");
                                int choice3 = scanner.nextInt();
                                scanner.nextLine();

                                try {
                                    getTrainerById(conn, choice3);
                                    int choice4 = scanner.nextInt();
                                    scanner.nextLine();
        
                                    try {
                                        bookTrainerSession(conn, choice4, userId);
                                    }
                                    catch (Exception e) {
                                        System.out.println("No such session");
                                    }
                                }
                                catch (Exception e) {
                                    System.out.println("No such trainer");
                                }
                            }
                        }
                        
                    }
                    else if (choice1 == 11) {
                        System.out.println("\n------------------------------------");
                        System.out.print("Enter the class id you want to widthdraw from: ");
                        int choice2 = scanner.nextInt();
                        scanner.nextLine();
                        removeClassRegistration(conn, userId, choice2);
                    }
                    else if (choice1 == 12) {
                        System.out.println("\n------------------------------------");
                        System.out.print("Your balance is: $" + calculateTotalCost(conn, userId));
                        System.out.print("Would you like to pay your balance (y/n): ");
                        String choice2 = scanner.nextLine();

                        if (choice2.equals("y")) {
                            markAllPaid(conn, userId);
                            System.out.println("Your new balance is: $" + calculateTotalCost(conn, userId));
                        }
                        else {
                            System.out.println("Your new balance is still: $" + calculateTotalCost(conn, userId));
                        }
                    }
                    else if (choice1 == 13) {
                        userId = -1;
                        userType = -1;
                    }
                    else {
                        System.out.println("Invalid choice. Please try again.");
                    }
                }
                while (userType == 2) {
                    System.out.println("\n------------------------------------");
                    System.out.println("Trainer Options: ");
                    System.out.println("1. View Trainers");
                    System.out.println("2. View Classes");
                    System.out.println("3. View Trainer Availability");
                    System.out.println("4. Scheduele Availability");
                    System.out.println("5. Search for Member");
                    System.out.println("6. Logout");
                    System.out.print("Enter your choice: ");

                    int choice1 = scanner.nextInt();
                    scanner.nextLine();

                    if (choice1 == 1) {
                        getAllTrainers(conn);
                    }
                    else if (choice1 == 2) {
                        getAllClasses(conn);
                    }
                    else if (choice1 == 3) {
                        System.out.println("\n------------------------------------");
                        System.out.print("Enter your trainer id: ");
                        int choice2 = scanner.nextInt();
                        scanner.nextLine();

                        try {
                            getTrainerById(conn, choice2);
                        }
                        catch (Exception e) {
                            System.out.println("No such trainer");
                        }
                    }
                    else if (choice1 == 4) {
                        try {
                            System.out.println("\n------------------------------------");
                            System.out.println("All training sessions are 1 hour long");
                            System.out.println("Sessions can only be booked in 30 minute intervals (09:00:00, 09:30:00, 10:00:00, etc.)");
                            System.out.print("Enter session date (yyyy-mm-dd): ");
                            String startDate = scanner.nextLine();
                            System.out.print("Enter session start time (hh:mm:ss): ");
                            String startTime = scanner.nextLine();
                            if (isValidTime(startTime)) {
                                System.out.print("Enter session cost: $");
                                Float cost = scanner.nextFloat();
                                scanner.nextLine();
                                DecimalFormat df = new DecimalFormat("#.##");
                                cost = Float.parseFloat(df.format(cost));
                                addTrainerAvailability(conn, userId, startDate, startTime, cost);
                            } 
                            else {
                                System.out.println("Invalid time. Please enter a time in 30-minute intervals.");
                            }
                        } catch (Exception e) {
                            System.out.println("Invalid Values");
                        }
                    }
                    
                    else if (choice1 == 5) {
                        System.out.println("\n------------------------------------");
                        System.out.print("Enter Member name");
                        String name = scanner.nextLine();
                        System.out.println("\n------------------------------------");
                        System.out.println("Member Profiles with Name '" + name + "' : \n");
                        getMemberByName(conn, name);
                    }
                    else if (choice1 == 6) {
                        userId = -1;
                        userType = -1;
                    }
                    else {
                        System.out.println("Invalid choice. Please try again.");
                    }
                }
                while (userType == 3) {
                    System.out.println("\n------------------------------------");
                    System.out.println("Admin Options: ");
                    System.out.println("1. View Trainers");
                    System.out.println("2. View Classes");
                    System.out.println("3. View Trainer Availability");
                    System.out.println("4. View Rooms");
                    System.out.println("5. View Room Availability");
                    System.out.println("6. Book Room");
                    System.out.println("7. Book Class");
                    System.out.println("8. Update Room Booking");
                    System.out.println("9. Update Class");
                    System.out.println("10. Add Equipment");
                    System.out.println("11. Schedule Equipment for Maintenance");
                    System.out.println("12. Delete Equipment");
                    System.out.println("13. View Equipment");
                    System.out.println("14. Search for Member");
                    System.out.println("15. Adjust Member Balance");
                    System.out.println("16. Logout");
                    System.out.print("Enter your choice: ");

                    int choice1 = scanner.nextInt();
                    scanner.nextLine();

                    if (choice1 == 1) {
                        getAllTrainers(conn);
                    }
                    else if (choice1 == 2) {
                        getAllClasses(conn);
                    }
                    else if (choice1 == 3) {
                        System.out.println("\n------------------------------------");
                        System.out.print("Enter your trainer id: ");
                        int choice2 = scanner.nextInt();
                        scanner.nextLine();

                        try {
                            getTrainerById(conn, choice2);
                        }
                        catch (Exception e) {
                            System.out.println("No such trainer");
                        }
                    }
                    else if (choice1 == 4) {
                        getAllRooms(conn);
                    }
                    else if (choice1 == 5) {
                        getAllBookedRooms(conn);
                    }
                    else if (choice1 == 6) {
                        try {
                            System.out.println("\n------------------------------------");
                            System.out.println("All room sessions are 1 hour long");
                            System.out.println("Rooms can only be booked in 30 minute intervals (09:00:00, 09:30:00, 10:00:00, etc.)");
                            System.out.print("Enter room date (yyyy-mm-dd): ");
                            String startDate = scanner.nextLine();
                            System.out.print("Enter room start time (hh:mm:ss): ");
                            String startTime = scanner.nextLine();
                            if (isValidTime(startTime)) {

                                System.out.print("Enter room id: ");
                                int roomId = scanner.nextInt();
                                scanner.nextLine();
                                System.out.print("Enter room purpose: ");
                                String name = scanner.nextLine();
                                bookRoom(conn, roomId, name, startDate, startTime);
                            } 
                            else {
                                System.out.println("Invalid time. Please enter a time in 30-minute intervals.");
                            }
                        } catch (Exception e) {
                            System.out.println("Invalid Values");
                        }
                    }
                    else if (choice1 == 7) {
                        try {
                            System.out.println("\n------------------------------------");
                            System.out.println("All classes sessions are 1 hour long");
                            System.out.println("Classes can only be booked in 30 minute intervals (09:00:00, 09:30:00, 10:00:00, etc.)");
                            System.out.print("Enter class date (yyyy-mm-dd): ");
                            String startDate = scanner.nextLine();
                            System.out.print("Enter class start time (hh:mm:ss): ");
                            String startTime = scanner.nextLine();
                            if (isValidTime(startTime)) {

                                System.out.print("Enter room id: ");
                                int roomId = scanner.nextInt();
                                scanner.nextLine();
                                System.out.print("Enter class name: ");
                                String name = scanner.nextLine();
                                System.out.print("Enter number of week the class is: ");
                                int numWeeks = scanner.nextInt();
                                scanner.nextLine();
                                System.out.print("Enter trainer id in charge of class: ");
                                int trainerid = scanner.nextInt();
                                scanner.nextLine();
                                System.out.print("Enter session cost: $");
                                Float cost = scanner.nextFloat();
                                scanner.nextLine();
                                DecimalFormat df = new DecimalFormat("#.##");
                                cost = Float.parseFloat(df.format(cost));

                                addClass(conn, trainerid, name, roomId, startDate, startTime, numWeeks, cost);
                            } 
                            else {
                                System.out.println("Invalid time. Please enter a time in 30-minute intervals.");
                            }
                        } catch (Exception e) {
                            System.out.println("Invalid Values");
                        }
                    }
                    else if (choice1 == 8) {
                        System.out.println("\n------------------------------------");
                        System.out.println("Enter booking id: ");
                        int roomId = scanner.nextInt();
                        scanner.nextLine();

                        System.out.println("\n------------------------------------");
                        System.out.println("Update Options: ");
                        System.out.println("1. Update Start Date (yyyy-mm-dd)");
                        System.out.println("2. Update Start Time (hh:mm:ss)");
                        System.out.println("4. Update Room (id)");
                        System.out.println("5. Update Name");
                        System.out.println("6. Back");
                        System.out.print("Enter your choice: ");

                        int choice2 = scanner.nextInt();
                        scanner.nextLine();

                        if(choice2 < 7 && choice2 > 0) {
                            try {
                                System.out.print("Enter your new value: ");
                                String choice3 = scanner.nextLine();
                                updateRoomField(conn, roomId, choice2, choice3);
                            }
                            catch (Exception e) {
                                System.out.println("Invalid Value");
                            }
                        }
                    }
                    else if (choice1 == 9) {
                        System.out.println("\n------------------------------------");
                        System.out.println("Enter class id: ");
                        int roomId = scanner.nextInt();
                        scanner.nextLine();

                        System.out.println("\n------------------------------------");
                        System.out.println("Update Options: ");
                        System.out.println("1. Update Start Date (yyyy-mm-dd)");
                        System.out.println("2. Update Start Time (hh:mm:ss)");
                        System.out.println("3. Update Number of Weeks");
                        System.out.println("4. Update Room (id)");
                        System.out.println("5. Update Name");
                        System.out.println("6. Update Cost");
                        System.out.println("7. Back");
                        System.out.print("Enter your choice: ");

                        int choice2 = scanner.nextInt();
                        scanner.nextLine();

                        if(choice2 < 8 && choice2 > 0) {
                            try {
                                System.out.print("Enter your new value: ");
                                String choice3 = scanner.nextLine();
                                updateClassField(conn, roomId, choice2, choice3);
                            }
                            catch (Exception e) {
                                System.out.println("Invalid Value");
                            }
                        }
                    }
                    else if (choice1 == 10) {
                        System.out.println("\n------------------------------------");
                        System.out.println("Enter room id in which the equipment will be stored: ");
                        int roomId = scanner.nextInt();
                        scanner.nextLine();
                        System.out.println("Enter equipment name: ");
                        String name = scanner.nextLine();
                        addEquipment(conn, roomId, name);
                    }
                    else if (choice1 == 11) {
                        System.out.println("\n------------------------------------");
                        System.out.println("Enter equipment id that will be undergoing maintenance: ");
                        int equipmentId = scanner.nextInt();
                        scanner.nextLine();
                        System.out.println("Enter maintenance start date (yyyy-mm-dd): ");
                        String startDate = scanner.nextLine();
                        System.out.println("Enter maintenance end date (yyyy-mm-dd): ");
                        String endDate = scanner.nextLine();
                        addMaintenance(conn, equipmentId, startDate, endDate);
                    }
                    else if (choice1 == 12) {
                        System.out.println("\n------------------------------------");
                        System.out.println("Enter equipment id: ");
                        int equipmentId = scanner.nextInt();
                        scanner.nextLine();
                        deleteEquipment(conn, equipmentId);
                    }
                    else if (choice1 == 13) {
                        getAllEquipment(conn);
                    }
                    else if (choice1 == 14) {
                        System.out.println("\n------------------------------------");
                        System.out.print("Enter Member name");
                        String name = scanner.nextLine();
                        System.out.println("\n------------------------------------");
                        System.out.println("Member Profiles with Name '" + name + "' : \n");
                        getMemberByName(conn, name);
                    }
                    else if (choice1 == 15) {
                        System.out.println("\n------------------------------------");
                        System.out.println("Enter member id that has paid off their balance: ");
                        int memberId = scanner.nextInt();
                        scanner.nextLine();
                        markAllPaid(conn, memberId);
                    }
                    else if (choice1 == 16) {
                        userId = -1;
                        userType = -1;
                    }
                    else {
                        System.out.println("Invalid choice. Please try again.");
                    }
                }
            }
    
            conn.close();
            scanner.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException se2) {
            }
            try {
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
}    