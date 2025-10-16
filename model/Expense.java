package com.rp.expensetracker.model;

// POJO representing a single expense row
public class Expense {
    public int id;             // auto-incremented id for delete/select
    public String date;        // yyyy-MM-dd
    public String category;    // e.g., Food, Travel
    public String description; // optional
    public double amount;      // >= 0

    public Expense(int id, String date, String category, String description, double amount) {
        this.id = id;
        this.date = date;
        this.category = category;
        this.description = description;
        this.amount = amount;
    }
}
