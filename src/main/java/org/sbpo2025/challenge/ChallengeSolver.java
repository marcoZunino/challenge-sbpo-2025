package org.sbpo2025.challenge;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import org.apache.commons.lang3.time.StopWatch;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ChallengeSolver {
    private final long MAX_RUNTIME = 600000; // milliseconds; 10 minutes

    protected List<Map<Integer, Integer>> orders;
    protected List<Map<Integer, Integer>> aisles;
    protected int nItems;
    protected int waveSizeLB;
    protected int waveSizeUB;

    public ChallengeSolver(
        List<Map<Integer, Integer>> orders, List<Map<Integer, Integer>> aisles, int nItems, int waveSizeLB, int waveSizeUB) {
        this.orders = orders;
        this.aisles = aisles;
        this.nItems = nItems;
        this.waveSizeLB = waveSizeLB;
        this.waveSizeUB = waveSizeUB;
    }

    public ChallengeSolution solve(StopWatch stopWatch) {
        // Implement your solution here
        ChallengeSolution best_solution;
        for (int k = 0; k < aisles.size(); k++) {
            System.out.println("Minimizing for k: " + k);
            ChallengeSolution solution = problem1a(k);
        }
        System.out.println("Done iterating over k.");
        return null;
    }


    /**
     * Problem 1.a: Solve the problem assuming number of selected aisles is constant and is the max number of aisles
     * @return the solution to the problem
     */
    protected ChallengeSolution problem1a(int k) {
        Loader.loadNativeLibraries();
        MPSolver solver = MPSolver.createSolver("SCIP");
        if (solver == null) {
            System.out.println("Could not create solver SCIP");
            return null;
        }
        int nOrders = orders.size();
        ArrayList<MPVariable> selected_orders = new ArrayList<>(nOrders);
        for (int i = 0; i < nOrders; i++) {
            selected_orders.add(solver.makeBoolVar("order_" + i));
        }

        int nAisles = aisles.size();
        ArrayList<MPVariable> selected_aisles = new ArrayList<>(nAisles);
        for (int i = 0; i < nAisles; i++) {
            selected_aisles.add(solver.makeBoolVar("aisle_" + i));
        }

        MPConstraint have_k_aisles = solver.makeConstraint(k, k, "Allow K aisles");
        for (MPVariable x : selected_orders) {
            have_k_aisles.setCoefficient(x, 0);
        }
        for (MPVariable y : selected_aisles) {
            have_k_aisles.setCoefficient(y, 1);
        }

        MPConstraint wave_bounds = solver.makeConstraint(waveSizeLB, waveSizeUB, "Wave size bounds");
        for (int o = 0; o < nOrders; o++) {
            Map<Integer, Integer> order = orders.get(o);
            int coeff = 0;
            Collection<Integer> quantities = order.values();
            for (Integer quantity: quantities) {
                coeff += quantity;
            }
            MPVariable x = selected_orders.get(o);
            wave_bounds.setCoefficient(x, coeff);
        }
        for (MPVariable y : selected_aisles) {
            wave_bounds.setCoefficient(y, 0);
        }

        double infinity = java.lang.Double.POSITIVE_INFINITY;
        Set<Integer> item_keys = new HashSet<>(Collections.emptySet());
        for (Map<Integer, Integer> order : orders) {
            item_keys.addAll(order.keySet());
        }
        for (Map<Integer, Integer> aisle : aisles) {
            item_keys.addAll(aisle.keySet());
        }

        for (Integer i : item_keys) {
            MPConstraint available_capacity = solver.makeConstraint(-infinity, 0, "Make sure items in orders are available in aisles");
            for (int o = 0; o < nOrders; o++) {
                MPVariable x = selected_orders.get(o);
                Integer coeff = orders.get(o).get(i);
                if (coeff == null) coeff = 0;
                available_capacity.setCoefficient(x, coeff);
            }
            for (int a = 0; a < nAisles; a++) {
                MPVariable y = selected_aisles.get(a);
                Integer coeff = aisles.get(a).get(i);
                if (coeff == null) coeff = 0;
                available_capacity.setCoefficient(y, -coeff);
            }
        }

        MPObjective objective = solver.objective();
        for (int o = 0; o < nOrders; o++) {
            Map<Integer, Integer> order = orders.get(o);
            int coeff = 0;
            Collection<Integer> quantities = order.values();
            for (Integer quantity: quantities) {
                coeff += quantity;
            }
            MPVariable x = selected_orders.get(o);
            objective.setCoefficient(x, (double) coeff / k);
        }
        for (MPVariable y : selected_aisles) {
            objective.setCoefficient(y, 0);
        }
        objective.setMaximization();
        final MPSolver.ResultStatus resultStatus = solver.solve();

        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
            System.out.println("Solution:");
            System.out.println("Objective value = " + objective.value());
            for (MPVariable x : selected_orders) {
                System.out.println(x.name() + ": " + x.solutionValue());
            }
            for (MPVariable y : selected_aisles) {
                System.out.println(y.name() + ": " + y.solutionValue());
            }
        } else {
            return null;
        }
        return null;
    }

    /*
     * Get the remaining time in seconds
     */
    protected long getRemainingTime(StopWatch stopWatch) {
        return Math.max(
                TimeUnit.SECONDS.convert(MAX_RUNTIME - stopWatch.getTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS),
                0);
    }

    protected boolean isSolutionFeasible(ChallengeSolution challengeSolution) {
        Set<Integer> selectedOrders = challengeSolution.orders();
        Set<Integer> visitedAisles = challengeSolution.aisles();
        if (selectedOrders == null || visitedAisles == null || selectedOrders.isEmpty() || visitedAisles.isEmpty()) {
            return false;
        }

        int[] totalUnitsPicked = new int[nItems];
        int[] totalUnitsAvailable = new int[nItems];

        // Calculate total units picked
        for (int order : selectedOrders) {
            for (Map.Entry<Integer, Integer> entry : orders.get(order).entrySet()) {
                totalUnitsPicked[entry.getKey()] += entry.getValue();
            }
        }

        // Calculate total units available
        for (int aisle : visitedAisles) {
            for (Map.Entry<Integer, Integer> entry : aisles.get(aisle).entrySet()) {
                totalUnitsAvailable[entry.getKey()] += entry.getValue();
            }
        }

        // Check if the total units picked are within bounds
        int totalUnits = Arrays.stream(totalUnitsPicked).sum();
        if (totalUnits < waveSizeLB || totalUnits > waveSizeUB) {
            return false;
        }

        // Check if the units picked do not exceed the units available
        for (int i = 0; i < nItems; i++) {
            if (totalUnitsPicked[i] > totalUnitsAvailable[i]) {
                return false;
            }
        }

        return true;
    }

    protected double computeObjectiveFunction(ChallengeSolution challengeSolution) {
        Set<Integer> selectedOrders = challengeSolution.orders();
        Set<Integer> visitedAisles = challengeSolution.aisles();
        if (selectedOrders == null || visitedAisles == null || selectedOrders.isEmpty() || visitedAisles.isEmpty()) {
            return 0.0;
        }
        int totalUnitsPicked = 0;

        // Calculate total units picked
        for (int order : selectedOrders) {
            totalUnitsPicked += orders.get(order).values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();
        }

        // Calculate the number of visited aisles
        int numVisitedAisles = visitedAisles.size();

        // Objective function: total units picked / number of visited aisles
        return (double) totalUnitsPicked / numVisitedAisles;
    }
}
