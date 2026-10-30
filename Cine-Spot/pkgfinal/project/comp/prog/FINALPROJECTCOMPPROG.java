package pkgfinal.project.comp.prog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Cinema Management System (console, text-file storage).
 * Works on any Java 8+ (no arrow-style switches).
 *
 * @author kramnextdoor
 */
public class FINALPROJECTCOMPPROG {

    // All data files are stored inside this folder (created automatically).
    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + File.separator + "users.txt";
    private static final String MOVIES_FILE = DATA_DIR + File.separator + "movies.txt";
    private static final String SHOWTIMES_FILE = DATA_DIR + File.separator + "showtimes.txt";
    private static final String BOOKINGS_FILE = DATA_DIR + File.separator + "bookings.txt";

    private static final List<User> users = new ArrayList<>();
    private static final List<Movie> movies = new ArrayList<>();
    private static final List<Showtime> showtimes = new ArrayList<>();
    private static final List<Booking> bookings = new ArrayList<>();

    private static User currentUser = null;
    private static final Scanner scanner = new Scanner(System.in);

    enum Role { ADMIN, STAFF, CUSTOMER }

    // ===================== DATA CLASSES =====================

    static class User {
        String id, username, password;
        Role role;

        User(String id, String username, String password, Role role) {
            this.id = id;
            this.username = username;
            this.password = password;
            this.role = role;
        }

        String toFileString() {
            return String.join(",", id, username, password, role.name());
        }

        static User fromFileString(String line) {
            String[] p = line.split(",");
            return new User(p[0], p[1], p[2], Role.valueOf(p[3]));
        }
    }

    static class Movie {
        String id, title, genre;
        int durationMins;

        Movie(String id, String title, String genre, int durationMins) {
            this.id = id;
            this.title = title;
            this.genre = genre;
            this.durationMins = durationMins;
        }

        String toFileString() {
            return String.join(",", id, title, genre, String.valueOf(durationMins));
        }

        static Movie fromFileString(String line) {
            String[] p = line.split(",");
            return new Movie(p[0], p[1], p[2], Integer.parseInt(p[3]));
        }
    }

    static class Showtime {
        String id, movieId, time;
        int totalSeats, availableSeats;
        double price;

        Showtime(String id, String movieId, String time, int totalSeats, int availableSeats, double price) {
            this.id = id;
            this.movieId = movieId;
            this.time = time;
            this.totalSeats = totalSeats;
            this.availableSeats = availableSeats;
            this.price = price;
        }

        String toFileString() {
            return String.join(",", id, movieId, time, String.valueOf(totalSeats),
                    String.valueOf(availableSeats), String.valueOf(price));
        }

        static Showtime fromFileString(String line) {
            String[] p = line.split(",");
            return new Showtime(p[0], p[1], p[2], Integer.parseInt(p[3]),
                    Integer.parseInt(p[4]), Double.parseDouble(p[5]));
        }
    }

    static class Booking {
        String id, userId, showtimeId;
        int seatCount;
        double totalPrice;

        Booking(String id, String userId, String showtimeId, int seatCount, double totalPrice) {
            this.id = id;
            this.userId = userId;
            this.showtimeId = showtimeId;
            this.seatCount = seatCount;
            this.totalPrice = totalPrice;
        }

        String toFileString() {
            return String.join(",", id, userId, showtimeId, String.valueOf(seatCount),
                    String.valueOf(totalPrice));
        }

        static Booking fromFileString(String line) {
            String[] p = line.split(",");
            return new Booking(p[0], p[1], p[2], Integer.parseInt(p[3]), Double.parseDouble(p[4]));
        }
    }

    // ===================== MAIN =====================

    public static void main(String[] args) {
        initStorage();
        loadAllData();

        System.out.println("=========================================");
        System.out.println("           WELCOME TO CINESPOT           ");
        System.out.println("=========================================");

        while (true) {
            if (currentUser == null) {
                showMainMenu();
            } else {
                switch (currentUser.role) {
                    case ADMIN:
                        showAdminMenu();
                        break;
                    case STAFF:
                        showStaffMenu();
                        break;
                    case CUSTOMER:
                        showCustomerMenu();
                        break;
                }
            }
        }
    }

    // ===================== INPUT HELPERS =====================

    /** Reads a whole number; keeps asking until valid. */
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Please enter a whole number.");
            }
        }
    }

    /** Reads a decimal number; keeps asking until valid. */
    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Please enter a valid number.");
            }
        }
    }

    /** Reads text; rejects empty input and commas (commas would corrupt the files). */
    private static String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = scanner.nextLine().trim();
            if (s.isEmpty()) {
                System.out.println("[ERROR] This field cannot be empty.");
            } else if (s.contains(",")) {
                System.out.println("[ERROR] Commas are not allowed.");
            } else {
                return s;
            }
        }
    }

    /** Like readText but allows blank (keep old value). Still rejects commas. */
    private static String readOptionalText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = scanner.nextLine().trim();
            if (s.contains(",")) {
                System.out.println("[ERROR] Commas are not allowed.");
            } else {
                return s;
            }
        }
    }

    // ===================== ID GENERATION =====================

    private static String nextId(String prefix, List<String> existingIds) {
        int max = 0;
        for (String id : existingIds) {
            try {
                max = Math.max(max, Integer.parseInt(id.substring(prefix.length())));
            } catch (Exception ignored) {
            }
        }
        return prefix + (max + 1);
    }

    private static String nextUserId() {
        List<String> ids = new ArrayList<>();
        for (User u : users) ids.add(u.id);
        return nextId("U", ids);
    }

    private static String nextMovieId() {
        List<String> ids = new ArrayList<>();
        for (Movie m : movies) ids.add(m.id);
        return nextId("M", ids);
    }

    private static String nextShowtimeId() {
        List<String> ids = new ArrayList<>();
        for (Showtime s : showtimes) ids.add(s.id);
        return nextId("S", ids);
    }

    private static String nextBookingId() {
        List<String> ids = new ArrayList<>();
        for (Booking b : bookings) ids.add(b.id);
        return nextId("B", ids);
    }

    // ===================== STORAGE =====================

    private static void initStorage() {
        try {
            // Create the data folder first (does nothing if it already exists)
            Files.createDirectories(Paths.get(DATA_DIR));

            createIfNotExists(USERS_FILE);
            createIfNotExists(MOVIES_FILE);
            createIfNotExists(SHOWTIMES_FILE);
            createIfNotExists(BOOKINGS_FILE);

            if (Files.size(Paths.get(USERS_FILE)) == 0) {
                List<String> defaultUsers = Arrays.asList(
                        "U1,admin,admin123,ADMIN",
                        "U2,staff,staff123,STAFF",
                        "U3,john,user123,CUSTOMER"
                );
                Files.write(Paths.get(USERS_FILE), defaultUsers);
            }
        } catch (IOException e) {
            System.out.println("Error initializing storage: " + e.getMessage());
        }
    }

    private static void createIfNotExists(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    private static void loadAllData() {
        users.clear();
        movies.clear();
        showtimes.clear();
        bookings.clear();
        try {
            for (String l : Files.readAllLines(Paths.get(USERS_FILE))) {
                if (l.trim().isEmpty()) continue;
                try {
                    users.add(User.fromFileString(l));
                } catch (Exception e) {
                    System.out.println("Skipped bad line in " + USERS_FILE + ": " + l);
                }
            }
            for (String l : Files.readAllLines(Paths.get(MOVIES_FILE))) {
                if (l.trim().isEmpty()) continue;
                try {
                    movies.add(Movie.fromFileString(l));
                } catch (Exception e) {
                    System.out.println("Skipped bad line in " + MOVIES_FILE + ": " + l);
                }
            }
            for (String l : Files.readAllLines(Paths.get(SHOWTIMES_FILE))) {
                if (l.trim().isEmpty()) continue;
                try {
                    showtimes.add(Showtime.fromFileString(l));
                } catch (Exception e) {
                    System.out.println("Skipped bad line in " + SHOWTIMES_FILE + ": " + l);
                }
            }
            for (String l : Files.readAllLines(Paths.get(BOOKINGS_FILE))) {
                if (l.trim().isEmpty()) continue;
                try {
                    bookings.add(Booking.fromFileString(l));
                } catch (Exception e) {
                    System.out.println("Skipped bad line in " + BOOKINGS_FILE + ": " + l);
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to load records: " + e.getMessage());
        }
    }

    private static void saveData() {
        try {
            List<String> uL = new ArrayList<>();
            for (User u : users) uL.add(u.toFileString());
            Files.write(Paths.get(USERS_FILE), uL);

            List<String> mL = new ArrayList<>();
            for (Movie m : movies) mL.add(m.toFileString());
            Files.write(Paths.get(MOVIES_FILE), mL);

            List<String> sL = new ArrayList<>();
            for (Showtime s : showtimes) sL.add(s.toFileString());
            Files.write(Paths.get(SHOWTIMES_FILE), sL);

            List<String> bL = new ArrayList<>();
            for (Booking b : bookings) bL.add(b.toFileString());
            Files.write(Paths.get(BOOKINGS_FILE), bL);
        } catch (IOException e) {
            System.out.println("Failed to save records: " + e.getMessage());
        }
    }

    // ===================== LOGIN / REGISTER =====================

    private static void showMainMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. Login");
        System.out.println("2. Register (Customer)");
        System.out.println("3. Exit");
        System.out.print("Select an option: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                login();
                break;
            case "2":
                registerCustomer();
                break;
            case "3":
                System.out.println("Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private static void login() {
        System.out.print("Username: ");
        String uname = scanner.nextLine().trim();
        System.out.print("Password: ");
        String pass = scanner.nextLine();

        for (User u : users) {
            if (u.username.equalsIgnoreCase(uname) && u.password.equals(pass)) {
                currentUser = u;
                System.out.println("\n[SUCCESS] Welcome, " + u.username + " (" + u.role + ")");
                return;
            }
        }
        System.out.println("[ERROR] Invalid credentials.");
    }

    private static void registerCustomer() {
        String uname = readText("Choose Username: ");
        for (User u : users) {
            if (u.username.equalsIgnoreCase(uname)) {
                System.out.println("[ERROR] Username taken.");
                return;
            }
        }
        String pass = readText("Choose Password: ");

        users.add(new User(nextUserId(), uname, pass, Role.CUSTOMER));
        saveData();
        System.out.println("[SUCCESS] Account created.");
    }

    private static void logout() {
        System.out.println("\n[LOGOUT] User " + currentUser.username + " logged out.");
        currentUser = null;
    }

    // ===================== ADMIN =====================

    private static void showAdminMenu() {
        System.out.println("\n--- ADMIN DASHBOARD ---");
        System.out.println("1. Movie Operations (Create/Read/Update/Delete)");
        System.out.println("2. Showtime Operations (Create/Read/Delete)");
        System.out.println("3. User Operations (Create Staff/Delete User)");
        System.out.println("4. View All Sales & Bookings");
        System.out.println("5. Logout");
        System.out.print("Select choice: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                adminMovieMenu();
                break;
            case "2":
                adminShowtimeMenu();
                break;
            case "3":
                adminUserMenu();
                break;
            case "4":
                viewSalesReports();
                break;
            case "5":
                logout();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private static void adminMovieMenu() {
        System.out.println("\n-- Movie Operations --");
        System.out.println("1. Add Movie\n2. View Movies\n3. Update Movie\n4. Delete Movie");
        System.out.print("Choice: ");
        String opt = scanner.nextLine().trim();
        switch (opt) {
            case "1":
                String t = readText("Title: ");
                String g = readText("Genre: ");
                int dur = readInt("Duration (mins): ");
                movies.add(new Movie(nextMovieId(), t, g, dur));
                saveData();
                System.out.println("[SUCCESS] Movie added.");
                break;
            case "2":
                listMovies();
                break;
            case "3":
                updateMovie();
                break;
            case "4":
                listMovies();
                System.out.print("Movie ID to delete: ");
                Movie m = findMovie(scanner.nextLine().trim());
                if (m == null) {
                    System.out.println("[ERROR] Not found.");
                    break;
                }
                boolean hasShowtimes = false;
                for (Showtime s : showtimes) {
                    if (s.movieId.equalsIgnoreCase(m.id)) {
                        hasShowtimes = true;
                        break;
                    }
                }
                if (hasShowtimes) {
                    System.out.println("[ERROR] Delete this movie's showtimes first.");
                } else {
                    movies.remove(m);
                    saveData();
                    System.out.println("[SUCCESS] Deleted.");
                }
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private static void listMovies() {
        System.out.println("\n--- Movie Catalog ---");
        if (movies.isEmpty()) System.out.println("No movies listed.");
        for (Movie m : movies) {
            System.out.printf("[%s] %s | Genre: %s | Duration: %d mins%n",
                    m.id, m.title, m.genre, m.durationMins);
        }
    }

    private static void updateMovie() {
        listMovies();
        System.out.print("Movie ID to update: ");
        Movie target = findMovie(scanner.nextLine().trim());
        if (target == null) {
            System.out.println("[ERROR] Movie not found.");
            return;
        }

        String t = readOptionalText("New Title (" + target.title + ") [Enter to keep]: ");
        if (!t.isEmpty()) target.title = t;

        String g = readOptionalText("New Genre (" + target.genre + ") [Enter to keep]: ");
        if (!g.isEmpty()) target.genre = g;

        while (true) {
            System.out.print("New Duration (" + target.durationMins + ") [Enter to keep]: ");
            String d = scanner.nextLine().trim();
            if (d.isEmpty()) break;
            try {
                target.durationMins = Integer.parseInt(d);
                break;
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Please enter a whole number.");
            }
        }

        saveData();
        System.out.println("[SUCCESS] Movie updated.");
    }

    private static void adminShowtimeMenu() {
        System.out.println("\n-- Showtime Operations --");
        System.out.println("1. Create Showtime\n2. View Showtimes\n3. Delete Showtime");
        System.out.print("Choice: ");
        String opt = scanner.nextLine().trim();
        switch (opt) {
            case "1":
                listMovies();
                System.out.print("Select Movie ID: ");
                String mId = scanner.nextLine().trim();
                Movie mv = findMovie(mId);
                if (mv == null) {
                    System.out.println("[ERROR] Invalid Movie ID.");
                    return;
                }
                String time = readText("Time (e.g. 2026-11-01 18:00): ");
                int cap = readInt("Seats: ");
                double price = readDouble("Price: ");
                if (cap <= 0 || price < 0) {
                    System.out.println("[ERROR] Seats must be positive and price cannot be negative.");
                    return;
                }
                showtimes.add(new Showtime(nextShowtimeId(), mv.id, time, cap, cap, price));
                saveData();
                System.out.println("[SUCCESS] Showtime added.");
                break;
            case "2":
                listShowtimes();
                break;
            case "3":
                listShowtimes();
                System.out.print("Showtime ID to delete: ");
                Showtime st = findShowtime(scanner.nextLine().trim());
                if (st != null) {
                    showtimes.remove(st);
                    saveData();
                    System.out.println("[SUCCESS] Deleted.");
                } else {
                    System.out.println("[ERROR] Not found.");
                }
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private static void listShowtimes() {
        System.out.println("\n--- Active Showtimes ---");
        if (showtimes.isEmpty()) System.out.println("No showtimes available.");
        for (Showtime s : showtimes) {
            Movie m = findMovie(s.movieId);
            String title = (m != null) ? m.title : "Unknown";
            System.out.printf("[%s] Movie: %s | Time: %s | Seats Left: %d/%d | Price: $%.2f%n",
                    s.id, title, s.time, s.availableSeats, s.totalSeats, s.price);
        }
    }

    private static void adminUserMenu() {
        System.out.println("\n-- User Operations --");
        System.out.println("1. Create Staff Account\n2. View All Users\n3. Delete User");
        System.out.print("Choice: ");
        String opt = scanner.nextLine().trim();
        switch (opt) {
            case "1":
                String u = readText("Staff Username: ");
                for (User existing : users) {
                    if (existing.username.equalsIgnoreCase(u)) {
                        System.out.println("[ERROR] Username taken.");
                        return;
                    }
                }
                String p = readText("Staff Password: ");
                users.add(new User(nextUserId(), u, p, Role.STAFF));
                saveData();
                System.out.println("[SUCCESS] Staff created.");
                break;
            case "2":
                for (User usr : users) {
                    System.out.printf("[%s] %s (Role: %s)%n", usr.id, usr.username, usr.role);
                }
                break;
            case "3":
                System.out.print("User ID to delete: ");
                User target = findUser(scanner.nextLine().trim());
                if (target != null && !target.id.equals(currentUser.id)) {
                    users.remove(target);
                    saveData();
                    System.out.println("[SUCCESS] User deleted.");
                } else {
                    System.out.println("[ERROR] Invalid user or target is self.");
                }
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private static void viewSalesReports() {
        System.out.println("\n--- Sales & Revenue Report ---");
        double totalRev = 0;
        int totalTickets = 0;
        if (bookings.isEmpty()) System.out.println("No bookings yet.");
        for (Booking b : bookings) {
            totalRev += b.totalPrice;
            totalTickets += b.seatCount;
            Showtime s = findShowtime(b.showtimeId);
            Movie m = (s != null) ? findMovie(s.movieId) : null;
            User u = findUser(b.userId);
            System.out.printf("Booking ID: %s | Customer: %s | Movie: %s | Tickets: %d | Total: $%.2f%n",
                    b.id, (u != null ? u.username : "Guest"),
                    (m != null ? m.title : "Unknown"), b.seatCount, b.totalPrice);
        }
        System.out.println("----------------------------------------");
        System.out.printf("TOTAL TICKETS SOLD: %d | TOTAL REVENUE: $%.2f%n", totalTickets, totalRev);
    }

    // ===================== STAFF =====================

    private static void showStaffMenu() {
        System.out.println("\n--- STAFF DASHBOARD ---");
        System.out.println("1. Sell Ticket (OTC)\n2. Cancel Booking\n3. View Movies & Showtimes\n4. Logout");
        System.out.print("Select choice: ");
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                bookTickets();
                break;
            case "2":
                cancelBooking();
                break;
            case "3":
                listMovies();
                listShowtimes();
                break;
            case "4":
                logout();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    // ===================== CUSTOMER =====================

    private static void showCustomerMenu() {
        System.out.println("\n--- CUSTOMER DASHBOARD ---");
        System.out.println("1. Browse Movies & Showtimes\n2. Book Seats\n3. My Bookings\n4. Cancel Booking\n5. Logout");
        System.out.print("Select choice: ");
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                listMovies();
                listShowtimes();
                break;
            case "2":
                bookTickets();
                break;
            case "3":
                viewMyBookings();
                break;
            case "4":
                cancelBooking();
                break;
            case "5":
                logout();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    // ===================== BOOKING (shared by staff & customer) =====================

    private static void bookTickets() {
        listShowtimes();
        System.out.print("Select Showtime ID: ");
        Showtime st = findShowtime(scanner.nextLine().trim());
        if (st == null) {
            System.out.println("[ERROR] Showtime not found.");
            return;
        }

        int seats = readInt("Seats to reserve: ");
        if (seats <= 0 || seats > st.availableSeats) {
            System.out.println("[ERROR] Invalid seat quantity (available: " + st.availableSeats + ").");
            return;
        }

        double total = seats * st.price;
        st.availableSeats -= seats;
        String bookingId = nextBookingId();
        bookings.add(new Booking(bookingId, currentUser.id, st.id, seats, total));
        saveData();
        System.out.printf("[SUCCESS] Booking %s confirmed: %d seat(s). Total: $%.2f%n",
                bookingId, seats, total);
    }

    private static void viewMyBookings() {
        System.out.println("\n--- My Booking History ---");
        boolean found = false;
        for (Booking b : bookings) {
            if (b.userId.equals(currentUser.id)) {
                found = true;
                Showtime st = findShowtime(b.showtimeId);
                Movie m = (st != null) ? findMovie(st.movieId) : null;
                System.out.printf("ID: %s | Movie: %s | Seats: %d | Total: $%.2f%n",
                        b.id, (m != null ? m.title : "Unknown"), b.seatCount, b.totalPrice);
            }
        }
        if (!found) System.out.println("No bookings found.");
    }

    private static void cancelBooking() {
        if (currentUser.role == Role.CUSTOMER) {
            viewMyBookings();
        } else {
            viewSalesReports();
        }

        System.out.print("Enter Booking ID to cancel: ");
        Booking target = findBooking(scanner.nextLine().trim());

        if (target == null) {
            System.out.println("[ERROR] Booking ID not found.");
            return;
        }
        if (currentUser.role == Role.CUSTOMER && !target.userId.equals(currentUser.id)) {
            System.out.println("[ERROR] Unauthorized.");
            return;
        }

        Showtime st = findShowtime(target.showtimeId);
        if (st != null) st.availableSeats += target.seatCount;

        bookings.remove(target);
        saveData();
        System.out.println("[SUCCESS] Booking cancelled and seats released.");
    }

    // ===================== FINDERS =====================

    private static Movie findMovie(String id) {
        for (Movie m : movies) if (m.id.equalsIgnoreCase(id)) return m;
        return null;
    }

    private static Showtime findShowtime(String id) {
        for (Showtime s : showtimes) if (s.id.equalsIgnoreCase(id)) return s;
        return null;
    }

    private static User findUser(String id) {
        for (User u : users) if (u.id.equalsIgnoreCase(id)) return u;
        return null;
    }

    private static Booking findBooking(String id) {
        for (Booking b : bookings) if (b.id.equalsIgnoreCase(id)) return b;
        return null;
    }
}