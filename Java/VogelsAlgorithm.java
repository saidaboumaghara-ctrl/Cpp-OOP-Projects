import java.util.Scanner;
import java.util.ArrayList;

public class Main {
    public static String toArabicNumbers(String text) {
        char[] arabicChars = {'٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩'};
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= '0' && c <= '9') {
                builder.append(arabicChars[c - '0']);
            } else if (c == '.') {
                builder.append('٫');
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        System.out.print("ادخل عدد الصوامع (المصادر): ");
        int numSilos = input.nextInt();

        System.out.print("ادخل عدد المطاحن (الوجهات): ");
        int numMills = input.nextInt();

        ArrayList<String> silos = new ArrayList<>();
        ArrayList<String> mills = new ArrayList<>();

        System.out.println("ادخل اسماء المطاحن (" + numMills + " أسماء): ");
        for (int i = 0; i < numMills; i++) {
            mills.add(input.next());
        }

        System.out.println("ادخل اسماء الصوامع (" + numSilos + " أسماء): ");
        for (int i = 0; i < numSilos; i++) {
            silos.add(input.next());
        }

        ArrayList<Double> supply = new ArrayList<>();
        System.out.println("\n--- أدخل كمية العرض لكل صومعة ---");
        for (int i = 0; i < numSilos; i++) {
            System.out.print("عرض الصومعة (" + silos.get(i) + "): ");
            supply.add(input.nextDouble());
        }

        ArrayList<Double> demand = new ArrayList<>();
        System.out.println("\n--- أدخل كمية الطلب لكل مطحنة ---");
        for (int i = 0; i < numMills; i++) {
            System.out.print("طلب المطحنة (" + mills.get(i) + "): ");
            demand.add(input.nextDouble());
        }

        ArrayList<ArrayList<Double>> costMatrix = new ArrayList<>();
        System.out.println("\n--- أدخل تكاليف النقل ---");
        for (int i = 0; i < numSilos; i++) {
            ArrayList<Double> row = new ArrayList<>();
            System.out.println("تكاليف الصومعة (" + silos.get(i) + "):");
            for (int j = 0; j < numMills; j++) {
                System.out.print("  - التكلفة للمطحنة " + mills.get(j) + ": ");
                row.add(input.nextDouble());
            }
            costMatrix.add(row);
        }

        double totalSupply = 0;
        for (double s : supply) totalSupply += s;
        double totalDemand = 0;
        for (double d : demand) totalDemand += d;

        if (totalSupply != totalDemand) {
            if (totalSupply < totalDemand) {
                silos.add("Dummy");
                supply.add(totalDemand - totalSupply);
                ArrayList<Double> dummyRow = new ArrayList<>();
                for (int j = 0; j < numMills; j++) dummyRow.add(0.0);
                costMatrix.add(dummyRow);
                numSilos++;
            } else {
                mills.add("Dummy");
                demand.add(totalSupply - totalDemand);
                for (int i = 0; i < numSilos; i++) {
                    costMatrix.get(i).add(0.0);
                }
                numMills++;
            }
        }

        ArrayList<ArrayList<Double>> allocation = new ArrayList<>();
        for (int i = 0; i < numSilos; i++) {
            ArrayList<Double> row = new ArrayList<>();
            for (int j = 0; j < numMills; j++) row.add(0.0);
            allocation.add(row);
        }

        boolean[] rowDone = new boolean[numSilos];
        boolean[] colDone = new boolean[numMills];

        while (true) {
            boolean allDone = true;
            for (int i = 0; i < numSilos; i++) if (!rowDone[i]) allDone = false;
            for (int j = 0; j < numMills; j++) if (!colDone[j]) allDone = false;
            if (allDone) break;

            double maxPenalty = -1;
            int chosenRow = -1;
            int chosenCol = -1;
            boolean isRowPenalty = true;

            for (int i = 0; i < numSilos; i++) {
                if (rowDone[i]) continue;
                ArrayList<Double> validCosts = new ArrayList<>();
                for (int j = 0; j < numMills; j++) {
                    if (!colDone[j]) validCosts.add(costMatrix.get(i).get(j));
                }
                if (validCosts.isEmpty()) continue;
                validCosts.sort(Double::compareTo);
                double penalty = 0;
                if (validCosts.size() >= 2) {
                    penalty = validCosts.get(1) - validCosts.get(0);
                } else if (validCosts.size() == 1) {
                    penalty = validCosts.get(0);
                }
                if (penalty > maxPenalty) {
                    maxPenalty = penalty;
                    chosenRow = i;
                    isRowPenalty = true;
                }
            }

            for (int j = 0; j < numMills; j++) {
                if (colDone[j]) continue;
                ArrayList<Double> validCosts = new ArrayList<>();
                for (int i = 0; i < numSilos; i++) {
                    if (!rowDone[i]) validCosts.add(costMatrix.get(i).get(j));
                }
                if (validCosts.isEmpty()) continue;
                validCosts.sort(Double::compareTo);
                double penalty = 0;
                if (validCosts.size() >= 2) {
                    penalty = validCosts.get(1) - validCosts.get(0);
                } else if (validCosts.size() == 1) {
                    penalty = validCosts.get(0);
                }
                if (penalty > maxPenalty) {
                    maxPenalty = penalty;
                    chosenCol = j;
                    isRowPenalty = false;
                }
            }

            if (isRowPenalty && chosenRow != -1) {
                int minCol = -1;
                double minCost = Double.MAX_VALUE;
                for (int j = 0; j < numMills; j++) {
                    if (!colDone[j] && costMatrix.get(chosenRow).get(j) < minCost) {
                        minCost = costMatrix.get(chosenRow).get(j);
                        minCol = j;
                    }
                }
                double qty = Math.min(supply.get(chosenRow), demand.get(minCol));
                allocation.get(chosenRow).set(minCol, qty);
                supply.set(chosenRow, supply.get(chosenRow) - qty);
                demand.set(minCol, demand.get(minCol) - qty);

                if (supply.get(chosenRow) == 0) rowDone[chosenRow] = true;
                if (demand.get(minCol) == 0) colDone[minCol] = true;
            } else if (!isRowPenalty && chosenCol != -1) {
                int minRow = -1;
                double minCost = Double.MAX_VALUE;
                for (int i = 0; i < numSilos; i++) {
                    if (!rowDone[i] && costMatrix.get(i).get(chosenCol) < minCost) {
                        minCost = costMatrix.get(i).get(chosenCol);
                        minRow = i;
                    }
                }
                double qty = Math.min(supply.get(minRow), demand.get(chosenCol));
                allocation.get(minRow).set(chosenCol, qty);
                supply.set(minRow, supply.get(minRow) - qty);
                demand.set(chosenCol, demand.get(chosenCol) - qty);

                if (supply.get(minRow) == 0) rowDone[minRow] = true;
                if (demand.get(chosenCol) == 0) colDone[chosenCol] = true;
            } else {
                break;
            }
        }

        System.out.println("\n========================================================");
        System.out.println("            مصفوفة التوزيع الأمثل لطريقة فوجل          ");
        System.out.println("========================================================");

        System.out.print(String.format("%-12s", "الصوامع/المطاحن"));
        for (String mill : mills) {
            System.out.print(String.format("%-10s", mill));
        }
        System.out.println();

        System.out.println("--------------------------------------------------------");

        double totalCost = 0;
        for (int i = 0; i < numSilos; i++) {
            System.out.print(String.format("%-12s", silos.get(i)));
            for (int j = 0; j < numMills; j++) {
                double qty = allocation.get(i).get(j);
                double cost = costMatrix.get(i).get(j);
                totalCost += (qty * cost);
                String formattedQty = String.format("%.1f", qty);
                System.out.print(String.format("%-10s", toArabicNumbers(formattedQty)));
            }
            System.out.println();
        }

        System.out.println("========================================================");
        String formattedCost = String.format("%.2f", totalCost);
        System.out.println("تكلفة النقل الكلية = " + toArabicNumbers(formattedCost));
        System.out.println("========================================================");

        input.close();
    }
}
