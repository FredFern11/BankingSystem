package banking;
import org.sqlite.SQLiteDataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.Scanner;

public class Main {

    public static String url = "jdbc:sqlite:/Users/fredericfernandez/Desktop/Simple Banking System/Simple Banking System/task/card.s3db";
    public static Integer balanceLocal = 0;
    public static String[] userStorage = {"", "",};
    public static String pinLocal;
    public static String cardNumberLocal;

    public static void Home() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("1. Create an account\n2. Log into account\n0. Exit\n");
        int redirect = scanner.nextInt();

        if (redirect == 1) {
            CreateAccount();
        } else if (redirect == 2) {
            LogIn();
        } else if (redirect == 0) {
            System.out.println("Bye!"); System.exit(0);
        }
    }

    public static void AccountUI() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("1. Balance\n2. Add income\n3. Do transfer\n4. Close account\n5. Log out\n0. Exit\n");
        int redirect = scanner.nextInt();

        if (redirect == 1) {
            GetUser(cardNumberLocal, 1);
            System.out.println("Balance: " + balanceLocal + "$");
            AccountUI();

        } else if (redirect == 2) {
            AddIncome();

        } else if (redirect == 3) {
            Transfer();

        } else if (redirect == 4) {
            CloseAccount();
            System.out.println("\nThis account has been closed!\n");

        } else if (redirect == 5) {
            System.out.println("\nYou have successfully logged out\n");
            Home();

        } else if (redirect == 0) {
            System.out.println("Bye!");
            System.exit(0);
        }
    }

    public static void CreateAccount() {
        CardNumGen();
        System.out.println("\nYour card number:\n" + userStorage[0]);

        System.out.println("\nYour card pin:\n" + userStorage[1]);
        System.out.println(" ");

        Home();
    }

    public static void CardNumGen() {
        // generation of card numbers
        Random random = new Random();

        String cardNumber = "400000";

        // if first number of pin = 0 storage will just erase it because he thinks = null
        String pin = String.valueOf(random.nextInt(9) +1);

        int i = 6;
        int m = 0;
        int k;
        int v;

        while (i < 15) {
            k = random.nextInt(9);
            cardNumber += String.valueOf(k);
            i++;
        }

        while (m < 3) {
            v = random.nextInt(10);
            pin += String.valueOf(v);
            m++;
        }

        // Luhn algorithm

        // numbers manipulation
        cardNumber += LuhnAlgorithm(cardNumber);

        userStorage[0] = cardNumber;
        userStorage[1] = pin;

        StoreUser();
    }

    private static int LuhnAlgorithm(String cardNumber) {
        int[] cardNumberInt = new int[cardNumber.length()];
        for (int i = 0; i < cardNumber.length(); i++) {
            cardNumberInt[i] = cardNumber.charAt(i) - '0';
        }

        int checkSum = 0;
        int q = 0;

        while (q < 15) {
            if ((q+1) % 2 != 0) {
                cardNumberInt[q] = cardNumberInt[q] * 2;
            }

            if (cardNumberInt[q] > 9) {
                cardNumberInt[q] -= 9;
            }

            checkSum += cardNumberInt[q];
            q++;
        }

        // finding checksum
        int checkNumber = 0;
        while (checkNumber < 10) {
            if ((checkSum + checkNumber) % 10 == 0) {
                return checkNumber;
            }

            checkNumber++;
        }

        return -10101;
    }

    private static void LogIn() {

        Scanner scanner = new Scanner(System.in);

        // ask user to enter Card number
        System.out.println("\nEnter your card number:");
        cardNumberLocal = scanner.next();

        // ask user to enter Pin
        System.out.println("\nEnter your pin:");
        pinLocal = scanner.next();

        GetUser(cardNumberLocal, 1);

        if (pinLocal.equals(userStorage[1]) && cardNumberLocal.equals(userStorage[0])) {
            System.out.println("\nYou have successfully logged in!");
            AccountUI();
        } else {
            System.out.println("\nWrong card number or PIN!");
            Home();
        }

    }

    private static void AddIncome() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nEnter income: ");
        Integer income = scanner.nextInt();
        UpdateBalance(cardNumberLocal, income);

        AccountUI();
    }

    public static void UpdateBalance(String account, Integer amount) {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        try (Connection con = dataSource.getConnection()) {
            // Statement creation
            try (Statement statement = con.createStatement()) {
                // Statement execution
                statement.executeUpdate("UPDATE card SET balance = balance +" + amount + " WHERE number = " + account);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void StoreUser() {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        try (Connection con = dataSource.getConnection()) {
            // Statement creation
            try (Statement statement = con.createStatement()) {
                // Statement execution
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS card(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "number TEXT," +
                        "pin TEXT," +
                        "balance INT DEFAULT 0)");

                statement.executeUpdate("INSERT INTO card (number, pin) VALUES (" + userStorage[0] + ',' + userStorage[1] + ')');

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String GetUser(String card, Integer store) {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        try (Connection con = dataSource.getConnection()) {
            // Statement creation
            try (Statement statement = con.createStatement()) {

                try (ResultSet storage = statement.executeQuery("SELECT pin, balance, number FROM card WHERE number =" + card)) {
                    while (storage.next()) {
                        // retrieve column values
                        if (store == 1) {
                            userStorage[0] = storage.getString("number");
                            userStorage[1] = storage.getString("pin");
                            balanceLocal = storage.getInt("balance");
                        } else if (store == 2) {
                            storage.getString("pin");
                            return storage.getString("pin");
                        }
                    }
                }

            } catch (SQLException e) {}
        } catch (SQLException e) {}
        return null;
    }

    public static void Transfer() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Transfer\nEnter card number:");
        String cardNumDest = scanner.next();


        String checkNumber = (LuhnAlgorithm(cardNumDest)) + "0";
        String lastNumberCard = cardNumDest.charAt(cardNumDest.length()-1) + "0";

        if (!checkNumber.equals(lastNumberCard)) {System.out.println("Probably you made a mistake in the card number");
            AccountUI();}

        if (cardNumberLocal.equals(cardNumDest)) {
            System.out.println("You can't transfer money to the same account\n");
            AccountUI();
        }

        if (GetUser(cardNumDest, 2) == null){
            System.out.println("Such a card does not exist.");
            AccountUI();
        }

        System.out.println("Enter how much money you want to transfer:");

        Integer amountTrans = scanner.nextInt();
        GetUser(cardNumberLocal, 1);

        if (balanceLocal < amountTrans) {
            System.out.println("Not enough money!\n");
            AccountUI();
        }



        UpdateBalance(cardNumDest, amountTrans);
        UpdateBalance(cardNumberLocal, -amountTrans);

        AccountUI();
    }

    public static void CloseAccount() {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        try (Connection con = dataSource.getConnection()) {
            // Statement creation
            try (Statement statement = con.createStatement()) {
                statement.executeUpdate("DELETE FROM card WHERE number=" + cardNumberLocal);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Account closed");
        Home();
    }

    public static void CreateFile(String name) throws IOException {
        File f = new File(name);
        if(!f.exists()){
            f.createNewFile();
            System.out.println("File created");
        }else if (f.exists()){
            System.out.println("File already exists");
        }
        StoreUser();
    }

    public static void main(String [] args) throws IOException {
        CreateFile(args[1]);
        Home();
    }

}