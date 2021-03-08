package edu.wctc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;

public class Main {

    private Scanner keyboard;
    private Cookbook cookbook;


    public Main() {
        keyboard = new Scanner(System.in);
        cookbook = new Cookbook();

        try {
            System.out.println("Reading in meals information from file...");
            List<String> fileLines = Files.readAllLines(Paths.get("meals_data.csv"));

            for (String line : fileLines) {
                String[] fields = line.split(",");
                cookbook.addMeal(fields[0], fields[1], fields[2]);
            }

            runMenu();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Main();
    }

    private void listByMealType() {
        // Default value pre-selected in case
        // something goes wrong w/user choice
        MealType mealType = MealType.DINNER;

        System.out.print("Which meal type? ");

        // Generate the menu using the ordinal value of the enum
        for (MealType m : MealType.values()) {
            System.out.println((m.ordinal() + 1) + ". " + m.getPrettyPrint());
        }

        System.out.print("Please enter your choice: ");
        String ans = keyboard.nextLine();

        try {
            int ansNum = parseInt(ans);
            if (ansNum < MealType.values().length) {
                mealType = MealType.values()[ansNum - 1];
            }
        } catch (NumberFormatException nfe) {
            System.out.println(String.format("Invalid meal type %s. Defaulted to %s.",
                    ans, mealType.getPrettyPrint()));
        }

        cookbook.printMealsByType(mealType);
    }

    private void printMenu() {
        System.out.println("");
        System.out.println("Select Action");
        System.out.println("1. List All Items");
        System.out.println("2. List All Items by Meal");
        System.out.println("3. Search by Meal Name");
        System.out.println("4. Do Control Break");
        System.out.println("5. Exit");
        System.out.print("Please enter your choice: ");
    }

    private void runMenu() {
        boolean userContinue = true;

        while (userContinue) {
            printMenu();

            String ans = keyboard.nextLine();
            switch (ans) {
                case "1":
                    cookbook.printAllMeals();
                    break;
                case "2":
                    listByMealType();
                    break;
                case "3":
                    searchByName();
                    break;
                case "4":
                    /*doControlBreak();*/
                    newControlBreak();
                    break;
                case "5":
                    userContinue = false;
                    break;
            }
        }

        System.out.println("Goodbye");
        System.exit(0);
    }

    private void searchByName() {
        keyboard.nextLine();
        System.out.print("Please enter name to search: ");
        String ans = keyboard.nextLine();
        cookbook.printByNameSearch(ans);
    }

    private void newControlBreak() {
        List<Meal> mealList = cookbook.getMeals();
        List<MealType> mealTypes = Arrays.asList(MealType.values());

        System.out.printf("%-20s%-20s%-20s%-20s%-20s%-20s%n", "Meal Type", "Total", "Mean", "Min", "Max", "Median");

        int count = 0;
        float total = 0;
        float mean = 0;
        int min = 0;
        int max = 0;
        int median = 0;

        for (MealType mealType : mealTypes) {
            List<Meal> currentMeals = mealList.stream()
                    .filter(m -> m.getMealType() == mealType)
                    .collect(Collectors.toList());

            count = currentMeals.size();

            total = currentMeals.stream()
                    .mapToInt(Meal::getCalories)
                    .sum();

            mean = total / count;

            min = currentMeals.stream()
                    .mapToInt(Meal::getCalories)
                    .min()
                    .orElse(0);

            max = currentMeals.stream()
                    .mapToInt(Meal::getCalories)
                    .max()
                    .orElse(0);

            List<Meal> sortedMeals = currentMeals.stream()
                    .sorted(Comparator.comparing(Meal::getCalories))
                    .collect(Collectors.toList());

            if (count % 2 == 0) {
                median = (sortedMeals.get(count / 2).getCalories() + sortedMeals.get((count / 2) - 1).getCalories()) / 2;
            } else {
                median = sortedMeals.get(count / 2).getCalories();
            }

            System.out.printf("%-20s%-20s%-20s%-20s%-20s%-20s%n", mealType.getPrettyPrint(), total, mean, min, max, median);
        }
    }

    private void doControlBreak() {
        List<Meal> mealList = new ArrayList<>();

        try (Scanner fileInput = new Scanner(new File("meals_data.csv"))) {
            while (fileInput.hasNext()) {
                String line = fileInput.nextLine();
                String[] columns = line.split(",");

                String mealType = columns[0];
                MealType typeOfMeal;
                switch (mealType) {
                    case "Breakfast":
                        typeOfMeal = MealType.BREAKFAST;
                        break;
                    case "Dessert":
                        typeOfMeal = MealType.DESSERT;
                        break;
                    case "Dinner":
                        typeOfMeal = MealType.DINNER;
                        break;
                    case "Lunch":
                        typeOfMeal = MealType.LUNCH;
                        break;
                    default:
                        typeOfMeal = null;
                }

                String item = columns[1];
                int calories = parseInt(columns[2]);

                Meal meal = new Meal(typeOfMeal, item, calories);
                mealList.add(meal);
            }

            String currentMealType = mealList.get(0).getMealType().getPrettyPrint();

            System.out.printf("%-20s%-20s%-20s%-20s%-20s%-20s%n", "Meal Type", "Total", "Mean", "Min", "Max", "Median");

            int count = 0;
            float total = 0;
            float mean = 0;
            float min = 0;
            float max = 0;
            float median = 0;
            List<Integer> currentTypeCalories = new ArrayList<>();

            for (Meal meal : mealList) {
                if (!currentMealType.equals(meal.getMealType().getPrettyPrint())) {
                    Collections.sort(currentTypeCalories);
                    median = currentTypeCalories.get(count / 2);
                    mean = total / count;
                    System.out.printf("%-20s%-20f%-20f%-20f%-20f%-20f%n", currentMealType, total, mean, min, max, median);

                    total = 0;
                    min = 0;
                    max = 0;
                    count = 0;

                    currentMealType = meal.getMealType().getPrettyPrint();
                    currentTypeCalories.clear();
                }

                if (min == 0 || meal.getCalories() < min) {
                    min = meal.getCalories();
                }
                if (meal.getCalories() > max) {
                    max = meal.getCalories();
                }
                total += meal.getCalories();
                count++;
                currentTypeCalories.add(meal.getCalories());
            }

            mean = total / count;
            median = currentTypeCalories.get(count / 2);
            System.out.printf("%-20s%-20f%-20f%-20f%-20f%-20f%n", currentMealType, total, mean, min, max, median);

        } catch (IOException ioe) {
            System.out.println("Can't open file");
        }
    }
}

