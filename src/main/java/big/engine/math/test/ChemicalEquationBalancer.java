package big.engine.math.test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.*;

public class ChemicalEquationBalancer {

    // 分数类，用于精确计算
    static class Fraction {
        long numerator;
        long denominator;

        Fraction(long num, long den) {
            if (den == 0) throw new ArithmeticException("Denominator cannot be zero");
            long gcd = gcd(Math.abs(num), Math.abs(den));
            numerator = num / gcd;
            denominator = den / gcd;
            if (denominator < 0) {
                numerator = -numerator;
                denominator = -denominator;
            }
        }

        Fraction(long num) {
            this(num, 1);
        }

        Fraction add(Fraction other) {
            long num = this.numerator * other.denominator + other.numerator * this.denominator;
            long den = this.denominator * other.denominator;
            return new Fraction(num, den);
        }

        Fraction subtract(Fraction other) {
            return this.add(new Fraction(-other.numerator, other.denominator));
        }

        Fraction multiply(Fraction other) {
            long num = this.numerator * other.numerator;
            long den = this.denominator * other.denominator;
            return new Fraction(num, den);
        }

        Fraction divide(Fraction other) {
            return this.multiply(new Fraction(other.denominator, other.numerator));
        }

        Fraction negate() {
            return new Fraction(-numerator, denominator);
        }

        boolean isZero() {
            return numerator == 0;
        }

        long toLong() {
            if (denominator != 1) {
                throw new ArithmeticException("Not an integer");
            }
            return numerator;
        }

        private long gcd(long a, long b) {
            return b == 0 ? a : gcd(b, a % b);
        }

        @Override
        public String toString() {
            if (denominator == 1) return Long.toString(numerator);
            return numerator + "/" + denominator;
        }
    }

    // 解析化学式，返回元素到计数的映射
    static Map<String, Integer> parseFormula(String formula) {
        Map<String, Integer> elementCounts = new ConcurrentHashMap<>();
        Pattern pattern = Pattern.compile("([A-Z][a-z]*)(?:\\[(\\d+)\\])?");
        Matcher matcher = pattern.matcher(formula);

        while (matcher.find()) {
            String element = matcher.group(1);
            String countStr = matcher.group(2);
            int count = countStr == null ? 1 : Integer.parseInt(countStr);
            elementCounts.put(element, elementCounts.getOrDefault(element, 0) + count);
        }
        return elementCounts;
    }


    public static String balance(String equation) {

        String[] sides = equation.split("->");
        if (sides.length != 2) {
            throw new IllegalArgumentException("Invalid equation format. Use '->' to separate reactants and products.");
        }

        List<String> reactants = Arrays.asList(sides[0].trim().split("\\s*\\+\\s*"));
        List<String> products = Arrays.asList(sides[1].trim().split("\\s*\\+\\s*"));
        List<String> allCompounds = new ArrayList<>();
        allCompounds.addAll(reactants);
        allCompounds.addAll(products);


        Set<String> elementsSet = new HashSet<>();
        List<Map<String, Integer>> compoundElementMaps = new ArrayList<>();
        for (String compound : allCompounds) {
            Map<String, Integer> elementMap = parseFormula(compound);
            compoundElementMaps.add(elementMap);
            elementsSet.addAll(elementMap.keySet());
        }
        List<String> elements = new ArrayList<>(elementsSet);

        int numCompounds = allCompounds.size();
        int numElements = elements.size();


        Fraction[][] matrix = new Fraction[numElements][numCompounds + 1];

        for (int i = 0; i < numElements; i++) {
            String element = elements.get(i);
            for (int j = 0; j < numCompounds; j++) {
                int count = compoundElementMaps.get(j).getOrDefault(element, 0);

                if (j < reactants.size()) {
                    matrix[i][j] = new Fraction(count);
                } else {
                    matrix[i][j] = new Fraction(-count);
                }
            }
            matrix[i][numCompounds] = new Fraction(0);
        }


        int[] pivotRow = new int[numCompounds];
        Arrays.fill(pivotRow, -1);
        int pivotCol = 0;

        for (int r = 0; r < numElements && pivotCol < numCompounds; ) {

            int maxRow = r;
            for (int i = r + 1; i < numElements; i++) {
                if (matrix[i][pivotCol] != null && 
                    !matrix[i][pivotCol].isZero() && 
                    (matrix[maxRow][pivotCol] == null || matrix[maxRow][pivotCol].isZero() || 
                     Math.abs(matrix[i][pivotCol].numerator) > Math.abs(matrix[maxRow][pivotCol].numerator))) {
                    maxRow = i;
                }
            }

            if (matrix[maxRow][pivotCol] == null || matrix[maxRow][pivotCol].isZero()) {
                pivotCol++;
                continue;
            }


            Fraction[] temp = matrix[r];
            matrix[r] = matrix[maxRow];
            matrix[maxRow] = temp;


            Fraction pivot = matrix[r][pivotCol];
            for (int j = pivotCol; j <= numCompounds; j++) {
                if (matrix[r][j] != null) {
                    matrix[r][j] = matrix[r][j].divide(pivot);
                }
            }


            for (int i = 0; i < numElements; i++) {
                if (i == r || matrix[i][pivotCol] == null || matrix[i][pivotCol].isZero()) continue;
                Fraction factor = matrix[i][pivotCol].negate();
                for (int j = pivotCol; j <= numCompounds; j++) {
                    if (matrix[r][j] != null) {
                        Fraction term = matrix[r][j].multiply(factor);
                        if (matrix[i][j] == null) {
                            matrix[i][j] = term;
                        } else {
                            matrix[i][j] = matrix[i][j].add(term);
                        }
                    }
                }
            }

            pivotRow[pivotCol] = r;
            pivotCol++;
            r++;
        }


        Fraction[] coefficients = new Fraction[numCompounds];
        Arrays.fill(coefficients, new Fraction(1));


        coefficients[numCompounds - 1] = new Fraction(1);


        for (int j = numCompounds - 2; j >= 0; j--) {
            if (pivotRow[j] == -1) continue;
            int r = pivotRow[j];
            Fraction sum = new Fraction(0);
            for (int k = j + 1; k < numCompounds; k++) {
                if (matrix[r][k] != null && !matrix[r][k].isZero()) {
                    sum = sum.add(matrix[r][k].multiply(coefficients[k]));
                }
            }
            coefficients[j] = sum.negate();
        }


        long lcm = 1;
        for (Fraction coeff : coefficients) {
            if (coeff.denominator != 1) {
                lcm = lcm(lcm, coeff.denominator);
            }
        }

        long[] intCoefficients = new long[numCompounds];
        for (int i = 0; i < numCompounds; i++) {
            Fraction scaled = coefficients[i].multiply(new Fraction(lcm));
            intCoefficients[i] = scaled.toLong();
        }


        long gcd = intCoefficients[0];
        for (int i = 1; i < numCompounds; i++) {
            gcd = gcd(gcd, Math.abs(intCoefficients[i]));
        }
        if (gcd != 0) {
            for (int i = 0; i < numCompounds; i++) {
                intCoefficients[i] /= gcd;
            }
        }

        // 确保系数为正（若全负则取反）
        boolean allNegative = true;
        for (long coeff : intCoefficients) {
            if (coeff > 0) {
                allNegative = false;
                break;
            }
        }
        if (allNegative) {
            for (int i = 0; i < numCompounds; i++) {
                intCoefficients[i] = -intCoefficients[i];
            }
        }


        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < reactants.size(); i++) {
            if (intCoefficients[i] != 1) {
                sb.append(intCoefficients[i]);
            }
            sb.append(reactants.get(i));
            if (i < reactants.size() - 1) {
                sb.append(" + ");
            }
        }
        sb.append(" -> ");

        for (int i = reactants.size(); i < numCompounds; i++) {
            if (intCoefficients[i] != 1) {
                sb.append(intCoefficients[i]);
            }
            sb.append(products.get(i - reactants.size()));
            if (i < numCompounds - 1) {
                sb.append(" + ");
            }
        }
        return sb.toString();
    }

    private static long gcd(long a, long b) {
        while (b != 0) {
            long temp = b;
            b = a % b;
            a = temp;
        }
        return Math.abs(a);
    }

    private static long lcm(long a, long b) {
        if (a == 0 || b == 0) return 0;
        return Math.abs(a * b) / gcd(a, b);
    }

    public static void main(String[] args) {
        String equation = "Fe[3]C +HNO[3]->FeN[3]O[9]+NO[2]+CO[2]+H[2]0";
        String balanced = balance(equation);
        System.out.println("Balanced Equation: " + balanced);
    }
}