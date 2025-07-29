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
    private final long MAX_RUNTIME = 599000; // milliseconds; 10 minutes - 1 second

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
        PartialResult bestSolution = new PartialResult(null, 0);
        
        System.out.println("Orders number: " + orders.size());
        System.out.println("Aisles number: " + aisles.size());
        System.out.println("Items number: " + nItems);
        System.out.println("Wave size bounds: [" + waveSizeLB + ", " + waveSizeUB + "]");

        // Metodo 1 ---
        // resolver buscando la cantidad minima de pasillos e iterar desde ahi ------------
        bestSolution = solveMinimumFeasibleAisles(bestSolution, stopWatch);
        int minimumAisles = bestSolution.partialSolution().aisles().size();
        bestSolution = solveWithFixedAisles(bestSolution, stopWatch, minimumAisles);
        // start iterating with minimumAisles
        // -------------------------------------------------------------------------------

        // // Metodo 2 ---
        // // iterar sobre la cantidad de pasillos (desde 1 hasta el total) ------------
        // bestSolution = solveWithFixedAisles(bestSolution, stopWatch);
        // // start with 1 aisle
        // // -------------------------------------------------------------------------------

        // // Metodo 3 ---
        // // iterar sobre la cantidad de items (desde LB hasta UB) -> poco eficiente------------
        // bestSolution = solveWithFixedItems(bestSolution, stopWatch);
        // // -------------------------------------------------------------------------------
        
        
        System.out.println("\nBest solution found with value " + bestSolution.objValue());
        System.out.println("Final remaining time: " + getRemainingTime(stopWatch) + " seconds");
        return bestSolution.partialSolution();
    }

    protected PartialResult solveWithFixedAisles(PartialResult bestSolution, StopWatch stopWatch, int initialAislesNumber) {
        System.out.println("\n>> solveWithFixedAisles");
        
        for (int k = initialAislesNumber; k <= aisles.size(); k++) {

            if (getRemainingTime(stopWatch) == 0) {
                System.out.println("Max runtime reached, stopping iteration over k.");
                break;
            }
            System.out.println("Remaining time: " + getRemainingTime(stopWatch) + " seconds");

            if (waveSizeUB/k <= bestSolution.objValue()) {

                System.out.println("Current best solution with value " + bestSolution.objValue() + " is already better than the maximum possible for k = " + k);
                break;
            }

            System.out.println("Maximizing picked items for number of aisles k = " + k);
            PartialResult partialResult = problem1a(k, getRemainingTime(stopWatch));
    
            if (partialResult.partialSolution() == null) {
                System.out.println("No feasible solution found for k = " + k);
                continue;
            }

            System.out.println("Partial Solution:");
            System.out.println("Selected orders = " + partialResult.partialSolution().orders());
            System.out.println("Selected aisles = " + partialResult.partialSolution().aisles());
            System.out.println("Objective value = " + partialResult.objValue());
            
            if (partialResult.objValue() > bestSolution.objValue()) {
                bestSolution = partialResult;
            }

        }

        System.out.println("Done iterating over k (fixed aisles)");
        System.out.println("Best solution found with value " + bestSolution.objValue());


        return bestSolution;
    }
    protected PartialResult solveWithFixedAisles(PartialResult bestSolution, StopWatch stopWatch) {
        return solveWithFixedAisles(bestSolution, stopWatch, 1); // default value
    }

    protected PartialResult solveWithFixedItems(PartialResult bestSolution, StopWatch stopWatch) {
        System.out.println("\n>> solveWithFixedItems");

        for (int k = waveSizeUB; k >= waveSizeLB; k--) {

            if (getRemainingTime(stopWatch) == 0) {
                System.out.println("Max runtime reached, stopping iteration over k.");
                break;
            }
            System.out.println("Remaining time: " + getRemainingTime(stopWatch) + " seconds");

            if (k <= bestSolution.objValue()) {

                System.out.println("Current best solution with value " + bestSolution.objValue() + " is already better than the maximum possible for k = " + k);
                break;
            }

            System.out.println("Minimizing visited aisles for number of units k = " + k);
            PartialResult partialResult = problem1b(k, getRemainingTime(stopWatch));

            if (partialResult.partialSolution() == null) {
                System.out.println("No feasible solution found for k = " + k);
                continue;
            }

            System.out.println("Partial Solution:");
            System.out.println("Selected orders = " + partialResult.partialSolution().orders());
            System.out.println("Selected aisles = " + partialResult.partialSolution().aisles());
            System.out.println("Objective value = " + partialResult.objValue());
            
            if (partialResult.objValue() > bestSolution.objValue()) {
                bestSolution = partialResult;
            }

        }

        System.out.println("Done iterating over k (fixed items)");
        System.out.println("Best solution found with value " + bestSolution.objValue());


        return bestSolution;
    }

    protected PartialResult solveMinimumFeasibleAisles(PartialResult bestSolution, StopWatch stopWatch) {
        System.out.println("\n>> solveMinimumFeasibleAisles");
        System.out.println("Remaining time: " + getRemainingTime(stopWatch) + " seconds");

        System.out.println("Minimizing visited aisles");
        PartialResult partialResult = problem1c(getRemainingTime(stopWatch));

        if (partialResult.partialSolution() == null) {
            System.out.println("No feasible solution found");
            return bestSolution;
        }

        System.out.println("Partial Solution:");
        System.out.println("Selected orders = " + partialResult.partialSolution().orders());
        System.out.println("Selected aisles = " + partialResult.partialSolution().aisles());
        System.out.println("Objective value = " + partialResult.objValue());
            
        if (partialResult.objValue() > bestSolution.objValue()) {
            bestSolution = partialResult;
        }

        return bestSolution;
    }




    /**
     * Problem 1.a: Solve the problem assuming number of selected aisles is constant
     * @return the solution to the problem (optimal for the given k aisles)
     */
    protected PartialResult problem1a(int k, long remainingTime) {
        // Solver
        Loader.loadNativeLibraries();
        MPSolver solver = MPSolver.createSolver("SCIP");
        if (solver == null) {
            System.out.println("Could not create solver SCIP");
            return new PartialResult(null, 0);
            // return null;
        }

        // Variables
        int nOrders = orders.size();
        List<MPVariable> selected_orders = getVariablesOrders(solver, nOrders);
        int nAisles = aisles.size();
        List<MPVariable> selected_aisles = getVariablesAisles(solver, nAisles);

        // Unique sub problem constraint
        MPConstraint have_k_aisles = solver.makeConstraint(k, k, "Allow K aisles");
        for (MPVariable x : selected_orders) {
            have_k_aisles.setCoefficient(x, 0);
        }
        for (MPVariable y : selected_aisles) {
            have_k_aisles.setCoefficient(y, 1);
        }

        // General problem constraints
        makeWaveBoundsConstraint(solver, nOrders, selected_orders, selected_aisles, waveSizeLB, waveSizeUB);
        makeAvailableCapacityConstraint(solver, nOrders, selected_orders, nAisles, selected_aisles);

        // Objective
        MPObjective objective = solver.objective();
        for (int o = 0; o < nOrders; o++) {
            Map<Integer, Integer> order = orders.get(o);
            int coeff = 0;
            Collection<Integer> quantities = order.values();
            for (Integer quantity: quantities) {
                coeff += quantity;
            }
            MPVariable x = selected_orders.get(o);
            // objective.setCoefficient(x, (double) coeff / k);
            objective.setCoefficient(x, coeff);
        }
        for (MPVariable y : selected_aisles) {
            objective.setCoefficient(y, 0);
        }
        objective.setMaximization();

        solver.setTimeLimit(remainingTime * 1000); // Convert seconds to milliseconds

        PartialResult partialResult = calculatePartialResult(solver, objective, nOrders, selected_orders, nAisles, selected_aisles);
        return new PartialResult(partialResult.partialSolution(), partialResult.objValue() / k); // Normalize the objective value by k
    }


    /**
     * Problem 1.b: Solve the problem assuming number of picked units is constant
     * @return the solution to the problem (optimal for the given k units)
     */
    protected PartialResult problem1b(int k, long remainingTime) {
        // Solver
        Loader.loadNativeLibraries();
        MPSolver solver = MPSolver.createSolver("SCIP");
        if (solver == null) {
            System.out.println("Could not create solver SCIP");
            return new PartialResult(null, 0);
            // return null;
        }

        // Variables
        int nOrders = orders.size();
        List<MPVariable> selected_orders = getVariablesOrders(solver, nOrders);
        int nAisles = aisles.size();
        List<MPVariable> selected_aisles = getVariablesAisles(solver, nAisles);

        // Unique sub problem constraint
        makeWaveBoundsConstraint(solver, nOrders, selected_orders, selected_aisles, k, k);

        // General problem constraints
        makeAvailableCapacityConstraint(solver, nOrders, selected_orders, nAisles, selected_aisles);

        // Objective
        MPObjective objective = solver.objective();
        for (MPVariable x : selected_orders) {
            objective.setCoefficient(x, 0);
        }
        for (MPVariable y : selected_aisles) {
            objective.setCoefficient(y, 1);
        }
        
        objective.setMinimization();

        solver.setTimeLimit(remainingTime * 1000); // Convert seconds to milliseconds

        PartialResult partialResult = calculatePartialResult(solver, objective, nOrders, selected_orders, nAisles, selected_aisles);
        System.out.println("Minimum aisles number for feasibility: " + partialResult.objValue());
        return new PartialResult(partialResult.partialSolution(), k / partialResult.objValue()); // Normalize the objective value by k
    }

    /**
     * Problem 1.c: Minimize the number of aisles in order to get a feasible solution
     * @return the solution to the problem (not necessarily optimal for the original problem)
     */
    protected PartialResult problem1c(long remainingTime) {
        // Solver
        Loader.loadNativeLibraries();
        MPSolver solver = MPSolver.createSolver("SCIP");
        if (solver == null) {
            System.out.println("Could not create solver SCIP");
            return new PartialResult(null, 0);
            // return null;
        }

        // Variables
        int nOrders = orders.size();
        List<MPVariable> selected_orders = getVariablesOrders(solver, nOrders);
        int nAisles = aisles.size();
        List<MPVariable> selected_aisles = getVariablesAisles(solver, nAisles);

        // Unique sub problem constraint
        makeWaveBoundsConstraint(solver, nOrders, selected_orders, selected_aisles, waveSizeLB, Integer.MAX_VALUE);

        // General problem constraints
        makeAvailableCapacityConstraint(solver, nOrders, selected_orders, nAisles, selected_aisles);

        // Objective
        MPObjective objective = solver.objective();
        for (MPVariable x : selected_orders) {
            objective.setCoefficient(x, 0);
        }
        for (MPVariable y : selected_aisles) {
            objective.setCoefficient(y, 1);
        }
        
        objective.setMinimization();

        solver.setTimeLimit(remainingTime * 1000); // Convert seconds to milliseconds

        PartialResult partialResult = calculatePartialResult(solver, objective, nOrders, selected_orders, nAisles, selected_aisles);

        int waveSize = waveSize(partialResult.partialSolution());

        System.out.println("Minimum aisles number for feasibility: " + partialResult.objValue());
        return new PartialResult(partialResult.partialSolution(), waveSize / partialResult.objValue()); // Normalize the objective value by waveSize
    }


    protected List<MPVariable> getVariablesOrders(MPSolver solver, int nOrders) {
        ArrayList<MPVariable> selected_orders = new ArrayList<>(nOrders);
        for (int i = 0; i < nOrders; i++) {
            selected_orders.add(solver.makeBoolVar("order_" + i));
        }
        return selected_orders;
    }

    protected List<MPVariable> getVariablesAisles(MPSolver solver, int nAisles) {
        ArrayList<MPVariable> selected_aisles = new ArrayList<>(nAisles);
        for (int i = 0; i < nAisles; i++) {
            selected_aisles.add(solver.makeBoolVar("aisle_" + i));
        }
        return selected_aisles;
    }


    protected void makeWaveBoundsConstraint(MPSolver solver, int nOrders, List<MPVariable> selected_orders, List<MPVariable> selected_aisles, int LB, int UB) {
        MPConstraint wave_bounds = solver.makeConstraint(LB, UB, "Wave size bounds");
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
    }

    protected void makeAvailableCapacityConstraint(MPSolver solver, int nOrders, List<MPVariable> selected_orders, int nAisles, List<MPVariable> selected_aisles) {
        double infinity = Double.POSITIVE_INFINITY;
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
    }

    protected PartialResult calculatePartialResult(MPSolver solver, MPObjective objective, int nOrders, List<MPVariable> selected_orders, int nAisles, List<MPVariable> selected_aisles) {
        
        final MPSolver.ResultStatus resultStatus = solver.solve();

        Set<Integer> finalOrders = new HashSet<>();
        Set<Integer> finalAisles = new HashSet<>();
        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
            // revisar condicion .OPTIMAL

            for (int i = 0; i < nOrders; i++) {
                MPVariable x = selected_orders.get(i);
                if (x.solutionValue() == 1) {
                    // System.out.println("x_" + i + ": " + x.solutionValue());
                    finalOrders.add(i);
                }
            }

            for (int i = 0; i < nAisles; i++) {
                MPVariable y = selected_aisles.get(i);
                if (y.solutionValue() == 1) {
                    // System.out.println("y_" + i + ": " + y.solutionValue());
                    finalAisles.add(i);
                }
            }

            ChallengeSolution partialSolution = new ChallengeSolution(finalOrders, finalAisles);
            
            return new PartialResult(partialSolution, objective.value());
        } else {
            return new PartialResult(null, 0);
        }
    }

    /*
     * Get the remaining time in seconds
     */
    protected long getRemainingTime(StopWatch stopWatch) {
        return Math.max(
                TimeUnit.SECONDS.convert(MAX_RUNTIME - stopWatch.getTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS),
                0);
    }

    public int waveSize(ChallengeSolution partialSolution) {
        int unitsPicked = 0;

        // Calculate total units picked
        for (int order : partialSolution.orders()) {
            for (Map.Entry<Integer, Integer> entry : orders.get(order).entrySet()) {
                unitsPicked += entry.getValue();
            }
        }

        return unitsPicked;
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
