import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.*;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;

import static spark.Spark.*;


public class Main {

    public static void main(String[] args) {

        String distributorFilePath = "../server/resources/Distributors.xlsx";
        // id -> unit cost (stores the lowest unit cost of each item)
        HashMap<String, Double> itemCosts = new HashMap<>();
        /* reads each sheet in 'Distributors.xlsx' finding and storing the
        smallest unit cost of each item in itemCosts
        reading of Excel file adapted from
        https://github.com/chargeahead/ExcelRead/blob/master/src/main/java/com/demo/ExcelProject/ReadInvoices.java
        */
        try {
            FileInputStream file = new FileInputStream(distributorFilePath);
            Workbook workbook = new XSSFWorkbook(file);
            DataFormatter dataFormatter = new DataFormatter();
            int numSheets = workbook.getNumberOfSheets();
            for (int i = 0; i < numSheets; i++) {
                Sheet sh = workbook.getSheetAt(i);
                Iterator<Row> iterator = sh.iterator();
                iterator.next();
                while(iterator.hasNext()) {
                    Row row = iterator.next();
                    Iterator<Cell> cellIterator = row.iterator();
                    JSONObject item = new JSONObject();
                    String[] properties = new String[]{"name", "id", "cost"};
                    /* used to denote which column data is being read from.
                    count is column number and resets when reading a new row */
                    int count = 0;
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        String cellValue = dataFormatter.formatCellValue(cell);
                        // ignoring first column 'Name' from Excel sheet
                        if (count != 0) {
                            item.put(properties[count], cellValue);
                        }
                        count++;
                    }
                    // ignoring empty rows
                    if (!item.has("id")) {
                        continue;
                    }
                    String idStr = item.getString("id");
                    double currCost = Math.round(item.getDouble("cost") * 100.0) / 100.0;
                    // adding to hashmap itemCosts if new or modifying value if unit cost is lower
                    if (!itemCosts.containsKey(idStr) || currCost < itemCosts.get(idStr)) {
                        itemCosts.put(idStr, currCost);
                    }
                }
            }

            workbook.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        /* reading 'Inventory.xlsx' and storing low stock items (i.e. those with less than
        25% capacity) in JSONArray lowStockItems
        reading of Excel file adapted from
        https://github.com/chargeahead/ExcelRead/blob/master/src/main/java/com/demo/ExcelProject/ReadInvoices.java
        */
        String inventoryFilePath = "../server/resources/Inventory.xlsx";
        JSONArray lowStockItems = new JSONArray();
        try {
            FileInputStream file = new FileInputStream(inventoryFilePath);
            Workbook workbook = new XSSFWorkbook(file);
            DataFormatter dataFormatter = new DataFormatter();
            Sheet sh = workbook.getSheetAt(0);
            Iterator<Row> iterator = sh.iterator();
            iterator.next();
            while(iterator.hasNext()) {
                Row row = iterator.next();
                Iterator<Cell> cellIterator = row.iterator();
                JSONObject item = new JSONObject();
                /* iterating through column indices of the Excel sheet with 'count'
                and column names with 'properties' */
                String[] properties = new String[]{"name", "amount", "capacity", "id"};
                int count = 0;
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    String cellValue = dataFormatter.formatCellValue(cell);
                    item.put(properties[count++], cellValue);
                }
                // checking for and adding low stock items
                double amount = Double.parseDouble(item.get("amount").toString());
                double capacity = Double.parseDouble(item.get("capacity").toString());
                if (capacity > 0.0 && amount / capacity < 0.25) {
                    lowStockItems.put(item);
                }
            }
            workbook.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // This is required to allow GET and POST requests with the header 'content-type'
        options("/*",
                (request, response) -> {
                        response.header("Access-Control-Allow-Headers",
                                "content-type");

                        response.header("Access-Control-Allow-Methods",
                                "GET, POST");


                    return "OK";
                });

        // This is required to allow the React app to communicate with this API
        before((request, response) -> response.header("Access-Control-Allow-Origin", "http://localhost:3000"));

        // Return JSON containing the candies for which the stock is less than 25% of it's capacity
        get("/low-stock", (request, response) -> lowStockItems);

        // Return JSON containing the total cost of restocking candy
        post("/restock-cost", (request, response) -> {
            JSONArray req = new JSONArray(request.body());
            double total = 0.0;
            /* looping through and summing (the lowest unit cost * amount ordered)
            for each item */
            for (int i = 0; i < req.length(); i++) {
                JSONObject curObj = req.getJSONObject(i);
                String idStr = curObj.get("id").toString();
                double item_cost = itemCosts.get(idStr);
                total += item_cost * Integer.parseInt(curObj.get("amount").toString());
            }
            JSONObject cost = new JSONObject();
            total = Math.round(total * 100.0) / 100.0;
            cost.put("cost", total);
            return cost;
        });

    }
}
