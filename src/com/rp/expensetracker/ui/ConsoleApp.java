package com.rp.expensetracker.ui;

import com.rp.expensetracker.io.ExpenseStore;
import com.rp.expensetracker.model.Expense;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.io.IOException;

/*
 * Simple menu-driven CLI.
 * Operations: add, list, filter (year+month), delete by ID, save, exit (auto-save).
 */
public class ConsoleApp {
    private final Scanner sc = new Scanner(System.in);
    private final ExpenseStore store = new ExpenseStore("expenses.csv");
    private final List<Expense> expenses = new ArrayList<>();
    private int nextId = 1; // in-memory auto-increment

    public ConsoleApp() {
        try {
            expenses.addAll(store.load());
            nextId = findNextId(expenses);
        } catch (IOException e) {
            System.out.println("Note: starting fresh (no CSV yet).");
        }
    }

    public void run() {
        while (true) {
            System.out.println("\n=== Expense Tracker ===");
            System.out.println("1) Add expense");
            System.out.println("2) List all");
            System.out.println("3) Filter by year/month");
            System.out.println("4) Delete by ID");
            System.out.println("5) Save");
            System.out.println("6) Exit");
            System.out.print("Choose: ");
            String ch = sc.nextLine().trim();

            switch (ch) {
                case "1": add(); break;
                case "2": list(expenses); break;
                case "3": filter(); break;
                case "4": deleteById(); break;
                case "5": save(); break;
                case "6": save(); System.out.println("Bye!"); return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    // ---- actions ----

    private void add() {
        try {
            System.out.print("Date (YYYY-MM-DD): ");
            String date = sc.nextLine().trim();
            validateDate(date);

            System.out.print("Category (e.g., Food/Travel/Bills): ");
            String cat = sc.nextLine().trim();
            if (cat.isEmpty()) { System.out.println("Category required."); return; }

            System.out.print("Description (optional): ");
            String desc = sc.nextLine().trim();

            System.out.print("Amount (>= 0): ");
            double amt = Double.parseDouble(sc.nextLine().trim());
            if (amt < 0) { System.out.println("Amount must be >= 0."); return; }

            expenses.add(new Expense(nextId++, date, cat, desc, amt));
            System.out.println("Added.");
        } catch (NumberFormatException e) {
            System.out.println("Amount must be a number.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void list(List<Expense> list) {
        if (list.isEmpty()) { System.out.println("(No records)"); return; }

        System.out.printf("%-4s %-12s %-12s %-30s %10s%n",
                "ID", "Date", "Category", "Description", "Amount");
        System.out.println("-------------------------------------------------------------------------------");
        double total = 0;
        for (Expense e : list) {
            System.out.printf("%-4d %-12s %-12s %-30s %10.2f%n",
                    e.id, e.date, e.category, truncate(e.description, 28), e.amount);
            total += e.amount;
        }
        System.out.println("-------------------------------------------------------------------------------");
        System.out.printf("Total: %.2f%n", total);
    }

    private void filter() {
        try {
            System.out.print("Year (e.g., 2025): ");
            int y = Integer.parseInt(sc.nextLine().trim());
            System.out.print("Month (1-12): ");
            int m = Integer.parseInt(sc.nextLine().trim());
            if (m < 1 || m > 12) { System.out.println("Month must be 1..12"); return; }

            String prefix = String.format("%04d-%02d", y, m); // "yyyy-MM"
            List<Expense> filtered = new ArrayList<>();
            for (Expense e : expenses) {
                if (e.date != null && e.date.startsWith(prefix)) filtered.add(e);
            }
            list(filtered);
        } catch (NumberFormatException e) {
            System.out.println("Enter valid numbers for year/month.");
        }
    }

    private void deleteById() {
        System.out.print("Enter ID to delete: ");
        String s = sc.nextLine().trim();
        try {
            int id = Integer.parseInt(s);
            boolean removed = expenses.removeIf(e -> e.id == id);
            System.out.println(removed ? "Deleted." : "ID not found.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
        }
    }

    private void save() {
        try {
            store.save(expenses);
            System.out.println("Saved to expenses.csv");
        } catch (IOException e) {
            System.out.println("Save failed: " + e.getMessage());
        }
    }

    // ---- helpers ----

    private void validateDate(String date) {
        try { LocalDate.parse(date); }
        catch (DateTimeParseException e) { throw new IllegalArgumentException("Date must be YYYY-MM-DD."); }
    }

    private String truncate(String s, int n) {
        if (s == null) return "";
        return s.length() <= n ? s : s.substring(0, n - 3) + "...";
    }

    private int findNextId(List<Expense> list) {
        int max = 0;
        for (Expense e : list) if (e.id > max) max = e.id;
        return max + 1;
        // ensures no collision when reloading from CSV
    }
}
