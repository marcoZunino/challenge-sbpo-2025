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
    protected boolean enableOutput = false; // Enable or disable solver output

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
        
        // problem parameters
        System.out.println("Orders number: " + orders.size());
        System.out.println("Aisles number: " + aisles.size());
        System.out.println("Items number: " + nItems);
        System.out.println("Wave size bounds: [" + waveSizeLB + ", " + waveSizeUB + "]");

        // // Metodo 1 ---
        // // resolver buscando la cantidad minima de pasillos e iterar desde ahi ------------
        // bestSolution = solveMinimumFeasibleAisles(bestSolution, stopWatch);
        // if (bestSolution.partialSolution() == null) {
        //     System.out.println("No feasible solution found, stopping.");
        //     return null; // No feasible solution found (for the whole problem)
        // }
        // int minimumAisles = bestSolution.partialSolution().aisles().size();
        // bestSolution = solveWithFixedAisles(bestSolution, stopWatch, minimumAisles);
        // // start iterating with minimumAisles
        // // -------------------------------------------------------------------------------

        // // Metodo 2 ---
        // // iterar sobre la cantidad de pasillos (desde 1 hasta el total) ------------
        // bestSolution = solveWithFixedAisles(bestSolution, stopWatch);
        // // start with 1 aisle
        // // -------------------------------------------------------------------------------

        // // Metodo 3 ---
        // // iterar sobre la cantidad de items (desde LB hasta UB) -> poco eficiente------------
        // bestSolution = solveWithFixedItems(bestSolution, stopWatch);
        // // -------------------------------------------------------------------------------

        // Metodo 4 ---
        // resolver buscando la cantidad minima de pasillos e iterar desde ahi ------------
        // bestSolution = solveMinimumFeasibleAisles(bestSolution, stopWatch);
        // if (bestSolution.partialSolution() == null) {
        //     System.out.println("No feasible solution found, stopping.");
        //     return null; // No feasible solution found (for the whole problem)
        // }
        // int minimumAisles = bestSolution.partialSolution().aisles().size();
        bestSolution = solveWithSelectedAisles(bestSolution, stopWatch);
        // start iterating with minimumAisles
        // -------------------------------------------------------------------------------

        
        // retrieve the final best solution
        System.out.println("\nBest solution found with value " + bestSolution.objValue());
        System.out.println("Final remaining time: " + getRemainingTime(stopWatch) + " seconds");
        return bestSolution.partialSolution();
    }

    protected PartialResult solveWithFixedAisles(PartialResult bestSolution, StopWatch stopWatch, int initialAislesNumber) {
        System.out.println("\n>> solveWithFixedAisles");
        
        // iterate over the number of aisles
        for (int k = initialAislesNumber; k <= aisles.size(); k++) {

            if (getRemainingTime(stopWatch) == 0) {
                System.out.println("Max runtime reached, stopping iteration over k.");
                break;
            } // stop iteration if no time left
            System.out.println("Remaining time: " + getRemainingTime(stopWatch) + " seconds");

            if (waveSizeUB/k <= bestSolution.objValue()) {
                // stopping condition due to optimality
                System.out.println("Current best solution with value " + bestSolution.objValue() + " is already better than the maximum possible for k >= " + k);
                break;
            }

            // solve
            System.out.println("\nMaximizing picked items for number of aisles k = " + k);
            PartialResult partialResult = problem1a(k, getRemainingTime(stopWatch));
    
            if (partialResult.partialSolution() == null) {
                System.out.println("No feasible solution found for k = " + k);
                continue;
            } // no feasible

            // show optimal for k aisles
            System.out.println("Partial Solution:");
            System.out.println("Selected orders = " + partialResult.partialSolution().orders());
            System.out.println("Selected aisles = " + partialResult.partialSolution().aisles());
            System.out.println("Objective value = " + partialResult.objValue());
            
            // update best solution
            if (partialResult.objValue() > bestSolution.objValue()) {
                bestSolution = partialResult;
            }

        }

        System.out.println("Done iterating over k (fixed aisles)");
        System.out.println("Best solution found with value " + bestSolution.objValue());

        return bestSolution;
    }
    protected PartialResult solveWithFixedAisles(PartialResult bestSolution, StopWatch stopWatch) {
        return solveWithFixedAisles(bestSolution, stopWatch, 1);
        // default value for initialAislesNumber is 1
    }

    protected PartialResult solveWithFixedItems(PartialResult bestSolution, StopWatch stopWatch) {
        System.out.println("\n>> solveWithFixedItems");

        // iterate over the number of items: UB, UB-1, ..., LB
        for (int k = waveSizeUB; k >= waveSizeLB; k--) {

            if (getRemainingTime(stopWatch) == 0) {
                System.out.println("Max runtime reached, stopping iteration over k.");
                break;
            }   // stop iteration if no time left
            System.out.println("Remaining time: " + getRemainingTime(stopWatch) + " seconds");

            if (k <= bestSolution.objValue()) {
                // stopping condition due to optimality
                System.out.println("Current best solution with value " + bestSolution.objValue() + " is already better than the maximum possible for k <= " + k);
                break;
            }

            // solve
            System.out.println("\nMinimizing visited aisles for number of units k = " + k);
            PartialResult partialResult = problem1b(k, getRemainingTime(stopWatch));

            if (partialResult.partialSolution() == null) {
                System.out.println("No feasible solution found for k = " + k);
                continue;
            }

            // show optimal for k items
            System.out.println("Partial Solution:");
            System.out.println("Selected orders = " + partialResult.partialSolution().orders());
            System.out.println("Selected aisles = " + partialResult.partialSolution().aisles());
            System.out.println("Objective value = " + partialResult.objValue());

            // update best solution
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

        // solve
        System.out.println("Minimizing visited aisles");
        PartialResult partialResult = problem1c(getRemainingTime(stopWatch));

        if (partialResult.partialSolution() == null) {
            System.out.println("No feasible solution found");
            return bestSolution;
        } // no feasible solution found => time limit reached, or the whole problem is infeasible

        System.out.println("Partial Solution:");
        System.out.println("Selected orders = " + partialResult.partialSolution().orders());
        System.out.println("Selected aisles = " + partialResult.partialSolution().aisles());
        System.out.println("Objective value = " + partialResult.objValue());
            
        // update best solution
        if (partialResult.objValue() > bestSolution.objValue()) {
            bestSolution = partialResult;
        }

        return bestSolution;
    }


    protected PartialResult solveWithSelectedAisles(PartialResult bestSolution, StopWatch stopWatch, int initialAislesNumber) {
        System.out.println("\n>> solveWithSelectedAisles");

        Set<Integer> selectedAisles = new HashSet<>();
        Set<Integer> remainingAisles = new HashSet<>(aisles.size());
        for (int i = 0; i < aisles.size(); i++) {
            remainingAisles.add(i);
        }

        // iterate over the number of aisles
        for (int k = 1; k <= aisles.size(); k++) {

            int aisle = maxCapacityAisle(remainingAisles);
            if (aisle == -1) {
                System.out.println("No aisles found in the list.");
                return bestSolution;
            }
            remainingAisles.remove(aisle);
            selectedAisles.add(aisle);

            if (k < initialAislesNumber) {
                continue; // skip iterations until we reach the initial aisles number
            }

            if (getRemainingTime(stopWatch) == 0) {
                System.out.println("Max runtime reached, stopping iteration over k.");
                break;
            } // stop iteration if no time left
            System.out.println("Remaining time: " + getRemainingTime(stopWatch) + " seconds");

            if (waveSizeUB/k <= bestSolution.objValue()) {
                // stopping condition due to optimality
                System.out.println("Current best solution with value " + bestSolution.objValue() + " is already better than the maximum possible for k >= " + k);
                break;
            }

            // solve
            System.out.println("\nMaximizing picked items for number of aisles k = " + k);
            System.out.println("Picked aisles: " + Arrays.toString(selectedAisles.toArray()));
            PartialResult partialResult = problem2a(selectedAisles, getRemainingTime(stopWatch));
    
            if (partialResult.partialSolution() == null) {
                System.out.println("No feasible solution found for k = " + k);
                continue;
            } // no feasible

            // show optimal for k aisles
            System.out.println("Partial Solution:");
            System.out.println("Selected orders = " + partialResult.partialSolution().orders());
            System.out.println("Selected aisles = " + partialResult.partialSolution().aisles());
            System.out.println("Objective value = " + partialResult.objValue());
            
            // update best solution
            if (partialResult.objValue() > bestSolution.objValue()) {
                bestSolution = partialResult;
            }

        }

        System.out.println("Done iterating over k (fixed aisles)");
        System.out.println("Best solution found with value " + bestSolution.objValue());

        return bestSolution;
    }
    protected PartialResult solveWithSelectedAisles(PartialResult bestSolution, StopWatch stopWatch) {
        return solveWithSelectedAisles(bestSolution, stopWatch, 1);
        // default value for initialAislesNumber is 1
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
        if (enableOutput) {
            solver.enableOutput();
        }
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
        if (enableOutput) {
            solver.enableOutput();
        }

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
        makeWaveBoundsConstraint(solver, nOrders, selected_orders, selected_aisles, waveSizeLB, waveSizeUB);

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
        if (enableOutput) {
            solver.enableOutput();
        }

        PartialResult partialResult = calculatePartialResult(solver, objective, nOrders, selected_orders, nAisles, selected_aisles);

        int waveSize = waveSize(partialResult.partialSolution());

        System.out.println("Minimum aisles number for feasibility: " + partialResult.objValue());
        return new PartialResult(partialResult.partialSolution(), waveSize / partialResult.objValue()); // Normalize the objective value by waveSize
    }

        /**
     * Problem 1.a: Solve the problem assuming a subset of k selected aisles
     * @return the solution to the problem (optimal for the given subset of aisles)
     */
    protected PartialResult problem2a(Set<Integer> selectedAisles, long remainingTime) {
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

        // General problem constraints, but with fixed aisles
        makeWaveBoundsConstraint(solver, nOrders, selected_orders, Collections.emptyList(), waveSizeLB, waveSizeUB);
        
        // available capacity constraint, considering fixed aisles (only order variables)
        makeAvailableCapacityConstraint(solver, nOrders, selected_orders, nAisles, new ArrayList<>(), selectedAisles);


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
            
            objective.setCoefficient(x, coeff);
        }
        objective.setMaximization();

        solver.setTimeLimit(remainingTime * 1000); // Convert seconds to milliseconds
        if (enableOutput) {
            solver.enableOutput();
        }
        PartialResult partialResult = calculatePartialResult(solver, objective, nOrders, selected_orders, nAisles, Collections.emptyList(), selectedAisles);
        return new PartialResult(partialResult.partialSolution(), partialResult.objValue() / selectedAisles.size()); // Normalize the objective value by the number of selected aisles
    }

    


    // variables

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

    // constraints

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

    protected void makeAvailableCapacityConstraint(MPSolver solver, int nOrders, List<MPVariable> selected_orders, int nAisles, List<MPVariable> selected_aisles, Set<Integer> fixed_selected_aisles) {
        double infinity = Double.POSITIVE_INFINITY;
        Set<Integer> item_keys = new HashSet<>(Collections.emptySet());
        for (Map<Integer, Integer> order : orders) {
            item_keys.addAll(order.keySet());
        }
        // for (Map<Integer, Integer> aisle : aisles) {
        //     item_keys.addAll(aisle.keySet());
        // }

        for (Integer i : item_keys) {

            // Sum up availability of item i from fixed aisles
            int supply = 0;
            for (int a : fixed_selected_aisles) {
                Integer coeff = aisles.get(a).get(i);
                if (coeff != null) supply += coeff;
            }

            MPConstraint available_capacity = solver.makeConstraint(-infinity, supply, "Make sure items in orders are available in aisles");

            // Coefficients for orders
            for (int o = 0; o < nOrders; o++) {
                MPVariable x = selected_orders.get(o);
                Integer coeff = orders.get(o).get(i);
                if (coeff == null) coeff = 0;
                available_capacity.setCoefficient(x, coeff);
            }

            if (selected_aisles.isEmpty()) {
                continue;
            }
            for (int a = 0; a < nAisles; a++) {
                MPVariable y = selected_aisles.get(a); // try get the aisle from the variables
                if (y != null) {
                    Integer coeff = aisles.get(a).get(i);
                    if (coeff == null) coeff = 0;
                    available_capacity.setCoefficient(y, -coeff);
                }
            }
        }
    }
    protected void makeAvailableCapacityConstraint(MPSolver solver, int nOrders, List<MPVariable> selected_orders, int nAisles, List<MPVariable> selected_aisles) {
        makeAvailableCapacityConstraint(solver, nOrders, selected_orders, nAisles, selected_aisles, Collections.emptySet());
        // double infinity = Double.POSITIVE_INFINITY;
        // Set<Integer> item_keys = new HashSet<>(Collections.emptySet());
        // for (Map<Integer, Integer> order : orders) {
        //     item_keys.addAll(order.keySet());
        // }
        // // for (Map<Integer, Integer> aisle : aisles) {
        // //     item_keys.addAll(aisle.keySet());
        // // }

        // for (Integer i : item_keys) {
        //     MPConstraint available_capacity = solver.makeConstraint(-infinity, 0, "Make sure items in orders are available in aisles");
        //     for (int o = 0; o < nOrders; o++) {
        //         MPVariable x = selected_orders.get(o);
        //         Integer coeff = orders.get(o).get(i);
        //         if (coeff == null) coeff = 0;
        //         available_capacity.setCoefficient(x, coeff);
        //     }
        //     for (int a = 0; a < nAisles; a++) {
        //         MPVariable y = selected_aisles.get(a);
        //         Integer coeff = aisles.get(a).get(i);
        //         if (coeff == null) coeff = 0;
        //         available_capacity.setCoefficient(y, -coeff);
        //     }
        // }
    }


    // solve
    protected PartialResult calculatePartialResult(MPSolver solver, MPObjective objective, int nOrders, List<MPVariable> selected_orders, int nAisles, List<MPVariable> selected_aisles, Set<Integer> fixed_selected_aisles) {
        
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
            for (Integer a : fixed_selected_aisles) {
                finalAisles.add(a);
            }
            for (int i = 0; i < selected_aisles.size(); i++) {
                MPVariable y = selected_aisles.get(i);
                if (
                    y.solutionValue() == 1
                    // (y == null && fixed_selected_aisles.contains(i))
                    // || (y != null && y.solutionValue() == 1)
                    ) {
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
    protected PartialResult calculatePartialResult(MPSolver solver, MPObjective objective, int nOrders, List<MPVariable> selected_orders, int nAisles, List<MPVariable> selected_aisles) {
        return calculatePartialResult(solver, objective, nOrders, selected_orders, nAisles, selected_aisles, Collections.emptySet());
        // default value for fixed_selected_aisles is empty set
    }

    /*
     * Get the remaining time in seconds
     */
    protected long getRemainingTime(StopWatch stopWatch) {
        return Math.max(
                TimeUnit.SECONDS.convert(MAX_RUNTIME - stopWatch.getTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS),
                0);
    }

    // Calculate total units picked
    public int waveSize(ChallengeSolution partialSolution) {
        int unitsPicked = 0;

        if (partialSolution == null || partialSolution.orders() == null || partialSolution.orders().isEmpty()) {
            return 0;
        }

        for (int order : partialSolution.orders()) { // each order
            for (Map.Entry<Integer, Integer> entry : orders.get(order).entrySet()) {
                unitsPicked += entry.getValue(); // each item
            }
        }

        return unitsPicked;
    }

    public int maxCapacityAisle(Set<Integer> aislesList) {

        int maxAisle = -1;
        int max = 0;
        int capacity = 0;

        for (int aisle : aislesList) { // each aisle
            capacity = 0;
            for (Map.Entry<Integer, Integer> entry : aisles.get(aisle).entrySet()) {
                capacity += entry.getValue(); // each item
            }
            if (capacity > max) { // update
                max = capacity;
                maxAisle = aisle;
            }
        }

        if (maxAisle == -1) {
            System.out.println("Max aisle not found.");
        }
        System.out.println("Max aisle: " + maxAisle + " with capacity " + max);
        return maxAisle;
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
