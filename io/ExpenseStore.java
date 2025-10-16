package com.rp.expensetracker.io;

import com.rp.expensetracker.model.Expense;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/*
 * Minimal CSV storage: "id","date","category","description","amount"
 * - Quotes wrap each field; inner quotes are doubled (" -> "").
 * - Safe for commas in description/category.
 */
public class ExpenseStore {
    private final File file;

    public ExpenseStore(String fileName) {
        this.file = new File(fileName);
    }

    // Save all expenses to CSV (overwrites file)
    public void save(List<Expense> expenses) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            out.println("\"id\",\"date\",\"category\",\"description\",\"amount\"");
            for (Expense e : expenses) {
                out.println(csv(e.id) + "," + csv(e.date) + "," + csv(e.category) + "," +
                            csv(e.description) + "," + csv(Double.toString(e.amount)));
            }
        }
    }

    // Load all expenses from CSV (returns empty list if file missing)
    public List<Expense> load() throws IOException {
        List<Expense> list = new ArrayList<>();
        if (!file.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean header = true;
            while ((line = br.readLine()) != null) {
                if (header) { header = false; continue; }
                line = line.trim();
                if (line.isEmpty()) continue;
                List<String> cols = parse(line);
                if (cols.size() != 5) continue; // skip bad row
                try {
                    int id = Integer.parseInt(cols.get(0));
                    String date = cols.get(1);
                    String cat = cols.get(2);
                    String desc = cols.get(3);
                    double amt = Double.parseDouble(cols.get(4));
                    list.add(new Expense(id, date, cat, desc, amt));
                } catch (Exception ignored) { /* skip malformed row */ }
            }
        }
        return list;
    }

    // --- CSV helpers ---

    private String csv(String s) {
        if (s == null) s = "";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    // Very small CSV parser for quoted, comma-separated fields
    private List<String> parse(String line) {
        ArrayList<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQ = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQ) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"'); i++; // escaped quote
                    } else {
                        inQ = false; // end of quoted field
                    }
                } else {
                    cur.append(ch);
                }
            } else {
                if (ch == '"') inQ = true;
                else if (ch == ',') { out.add(cur.toString()); cur.setLength(0); }
                else cur.append(ch);
            }
        }
        out.add(cur.toString());
        return out;
    }
}
