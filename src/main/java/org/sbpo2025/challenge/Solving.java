package org.sbpo2025.challenge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.ortools.Loader;

import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.LinearExpr;
import com.google.ortools.sat.LinearExprBuilder;
import com.google.ortools.sat.BoolVar;
import com.google.ortools.sat.CpSolverStatus;


public class Solving {
    
    protected List<Map<Integer, Integer>> orders;
    protected List<Map<Integer, Integer>> aisles;
    protected int nItems;
    protected int waveSizeLB;
    protected int waveSizeUB;
    protected boolean enableOutput = false; // Enable or disable solver output

    public Solving(ChallengeSolver challengeSolver) {
        this.orders = challengeSolver.orders;
        this.aisles = challengeSolver.aisles;
        this.nItems = challengeSolver.nItems;
        this.waveSizeLB = challengeSolver.waveSizeLB;
        this.waveSizeUB = challengeSolver.waveSizeUB;
    }

    /**
     * Problem 1.a: Solve the problem assuming number of selected aisles is constant
     * @return the solution to the problem (optimal for the given k aisles)
     */
    public PartialResult problem1a(int k, long remainingTime) {
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
        makeAvailableCapacityConstraint(solver, selected_orders, selected_aisles);

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
        PartialResult partialResult = calculatePartialResult(solver, objective, selected_orders, selected_aisles);
        return new PartialResult(partialResult.partialSolution(), partialResult.objValue() / k); // Normalize the objective value by k
    }

    public PartialResult problem1aCP(int k, long remainingTime) {
        // Solver
        Loader.loadNativeLibraries();
        CpModel model = new CpModel();

        // Variables
        int nOrders = orders.size();
        List<BoolVar> selected_orders = getVariablesOrders(model, nOrders);
        int nAisles = aisles.size();
        List<BoolVar> selected_aisles = getVariablesAisles(model, nAisles);

        // Unique sub problem constraint
        LinearExpr aisleSum = LinearExpr.sum(selected_aisles.toArray(new BoolVar[0]));
        model.addEquality(aisleSum, k); // Ensure exactly k aisles are selected

        // General problem constraints
        makeWaveBoundsConstraint(model, nOrders, selected_orders, selected_aisles, waveSizeLB, waveSizeUB);
        makeAvailableCapacityConstraint(model, selected_orders, selected_aisles);

        // Objective
        LinearExprBuilder objectiveBuilder = LinearExpr.newBuilder();
        for (int o = 0; o < nOrders; o++) {
            Map<Integer, Integer> order = orders.get(o);
            int coeff = 0;
            for (Integer quantity : order.values()) {
                coeff += quantity;
            }
            BoolVar x = selected_orders.get(o);
            objectiveBuilder.addTerm(x, coeff);
        }

        model.maximize(objectiveBuilder);

        CpSolver solver = new CpSolver();
        solver.getParameters().setMaxTimeInSeconds(remainingTime);      // Time limit in seconds
        solver.getParameters().setLogSearchProgress(enableOutput);      // Enable logging
        
        PartialResult partialResult = calculatePartialResult(solver, model, selected_orders, selected_aisles);
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
        makeAvailableCapacityConstraint(solver, selected_orders, selected_aisles);

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

        PartialResult partialResult = calculatePartialResult(solver, objective, selected_orders, selected_aisles);
        System.out.println("Minimum aisles number for feasibility: " + partialResult.objValue());
        return new PartialResult(partialResult.partialSolution(), k / partialResult.objValue()); // Normalize the objective value by k
    }

    /**
     * Problem 1.c: Minimize the number of aisles to get a feasible solution
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
        makeAvailableCapacityConstraint(solver, selected_orders, selected_aisles);

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

        PartialResult partialResult = calculatePartialResult(solver, objective, selected_orders, selected_aisles);

        int waveSize = waveSize(partialResult.partialSolution());

        System.out.println("Minimum aisles number for feasibility: " + partialResult.objValue());
        return new PartialResult(partialResult.partialSolution(), waveSize / partialResult.objValue()); // Normalize the objective value by waveSize
    }

    /**
     * Problem 2.a: Solve the problem assuming a subset of k selected aisles
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

        // General problem constraints, but with fixed aisles
        makeWaveBoundsConstraint(solver, nOrders, selected_orders, Collections.emptyList(), waveSizeLB, waveSizeUB);
        
        // available capacity constraint, considering fixed aisles (only order variables)
        makeAvailableCapacityConstraint(solver, selected_orders, Collections.emptyList(), Collections.emptySet(), selectedAisles);


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
        PartialResult partialResult = calculatePartialResult(solver, objective, selected_orders, Collections.emptyList(), Collections.emptySet(), selectedAisles);
        return new PartialResult(partialResult.partialSolution(), partialResult.objValue() / selectedAisles.size()); // Normalize the objective value by the number of selected aisles
    }

    /**
     * Problem 2.b: Solve the problem assuming a subset of selected orders
     * @return the solution to the problem (optimal for the given subset of orders)
     */
    protected PartialResult problem2b(Set<Integer> selectedOrders, long remainingTime) {
        // Solver
        Loader.loadNativeLibraries();
        MPSolver solver = MPSolver.createSolver("SCIP");
        if (solver == null) {
            System.out.println("Could not create solver SCIP");
            return new PartialResult(null, 0);
            // return null;
        }

        // Constants
        int nAisles = aisles.size();

        int waveSize = 0;
        for (Integer order : selectedOrders) {
            for (Map.Entry<Integer, Integer> entry : orders.get(order).entrySet()) {
                waveSize += entry.getValue();
            }
        }

        if (waveSize > waveSizeUB || waveSize < waveSizeLB) {
            System.out.println("Wave size out of bounds");
            return new PartialResult(null, 0);
        }

        // Variables
        List<MPVariable> selected_aisles = getVariablesAisles(solver, nAisles);
        
        // available capacity constraint, considering fixed orders (only aisle variables)
        makeAvailableCapacityConstraint(solver, Collections.emptyList(), selected_aisles, selectedOrders, Collections.emptySet());

        // Objective
        MPObjective objective = solver.objective();
        for (MPVariable y : selected_aisles) {
            objective.setCoefficient(y, 1);
        }
        objective.setMinimization();

        solver.setTimeLimit(remainingTime * 1000); // Convert seconds to milliseconds
        if (enableOutput) {
            solver.enableOutput();
        }

        PartialResult partialResult = calculatePartialResult(solver, objective, Collections.emptyList(), selected_aisles, selectedOrders, Collections.emptySet());

        return new PartialResult(partialResult.partialSolution(), waveSize / partialResult.objValue()); // Original problem objective value
    }

    /**
     * Problem 2.c: Solve the problem assuming a subset of selected aisles (fixed), and a subset of selected orders (that can be modified)
     * @return the solution to the problem (optimal for the given subset of aisles and preselected orders)
     */
    protected PartialResult problem2c(Set<Integer> selectedAisles, Set<Integer> preSelectedOrders, long remainingTime) {
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

        // Fix preselected orders
        makePreSelectionConstraint(solver, selected_orders, preSelectedOrders);

        // General problem constraints, but with fixed aisles
        makeWaveBoundsConstraint(solver, nOrders, selected_orders, Collections.emptyList(), waveSizeLB, waveSizeUB);
        
        // available capacity constraint, considering fixed aisles (only order variables)
        makeAvailableCapacityConstraint(solver, selected_orders, Collections.emptyList(), preSelectedOrders, selectedAisles);


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
            // sumar las ordenes preseleccionadas como constante ?
            // TODO
        }
        objective.setMaximization();

        solver.setTimeLimit(remainingTime * 1000); // Convert seconds to milliseconds
        if (enableOutput) {
            solver.enableOutput();
        }
        PartialResult partialResult = calculatePartialResult(solver, objective, selected_orders, Collections.emptyList(), Collections.emptySet(), selectedAisles);
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
    protected List<BoolVar> getVariablesOrders(CpModel model, int nOrders) {
        ArrayList<BoolVar> selected_orders = new ArrayList<>(nOrders);
        for (int i = 0; i < nOrders; i++) {
            selected_orders.add(model.newBoolVar("order_" + i));
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
    protected List<BoolVar> getVariablesAisles(CpModel model, int nAisles) {
        ArrayList<BoolVar> selected_aisles = new ArrayList<>(nAisles);
        for (int i = 0; i < nAisles; i++) {
            selected_aisles.add(model.newBoolVar("aisle_" + i));
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
    protected void makeWaveBoundsConstraint(CpModel model, int nOrders, List<BoolVar> selected_orders, List<BoolVar> selected_aisles, int LB, int UB) {
        LinearExpr wave_bounds = LinearExpr.sum(selected_orders.toArray(new BoolVar[0]));
        model.addLessOrEqual(wave_bounds, UB);
        model.addGreaterOrEqual(wave_bounds, LB);
    }

    protected void makeAvailableCapacityConstraint(MPSolver solver, List<MPVariable> selected_orders, List<MPVariable> selected_aisles, Set<Integer> fixed_selected_orders, Set<Integer> fixed_selected_aisles) {
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

            // Sum up demand of item i from fixed orders
            int demand = 0;
            for (int o : fixed_selected_orders) {
                Integer coeff = orders.get(o).get(i);
                if (coeff != null) demand += coeff;
            }

            MPConstraint available_capacity = solver.makeConstraint(-infinity, supply - demand, "Make sure items in orders are available in aisles");

            // Coefficients for orders
            if (!selected_orders.isEmpty()) {
                for (int o = 0; o < selected_orders.size(); o++) {
                    MPVariable x = selected_orders.get(o); // try get the order from the variables

                    if (fixed_selected_orders.contains(o)) {
                        available_capacity.setCoefficient(x, 0); // Fixed order, no capacity needed

                    } else if (x != null) {
                        Integer coeff = orders.get(o).get(i);
                        if (coeff == null) coeff = 0;
                        available_capacity.setCoefficient(x, coeff);
                    }
                }
            }

            // Coefficients for aisles
            if (!selected_aisles.isEmpty()) {
                for (int a = 0; a < selected_aisles.size(); a++) {
                    MPVariable y = selected_aisles.get(a); // try get the aisle from the variables

                    if (fixed_selected_aisles.contains(a)) {
                        available_capacity.setCoefficient(y, 0); // Fixed aisle, no capacity needed

                    } else if (y != null) {
                        Integer coeff = aisles.get(a).get(i);
                        if (coeff == null) coeff = 0;
                        available_capacity.setCoefficient(y, -coeff);
                    }
                }
            }
        }
    }
    protected void makeAvailableCapacityConstraint(MPSolver solver, List<MPVariable> selected_orders, List<MPVariable> selected_aisles) {
        makeAvailableCapacityConstraint(solver, selected_orders, selected_aisles, Collections.emptySet(), Collections.emptySet());
    }
    protected void makeAvailableCapacityConstraint(CpModel model, List<BoolVar> selected_orders, List<BoolVar> selected_aisles, Set<Integer> fixed_selected_orders, Set<Integer> fixed_selected_aisles) {
        Set<Integer> item_keys = new HashSet<>(Collections.emptySet());
        for (Map<Integer, Integer> order : orders) {
            item_keys.addAll(order.keySet());
        }

        for (Integer i : item_keys) {
            // Compute static supply from fixed aisles
            int supply = 0;
            for (int a : fixed_selected_aisles) {
                Integer coeff = aisles.get(a).get(i);
                if (coeff != null) supply += coeff;
            }

            // Build LinearExpr for total demand from orders
            List<LinearExpr> demandTerms = new ArrayList<>();
            for (int o = 0; o < orders.size(); o++) {
                Integer coeff = orders.get(o).get(i);
                if (coeff != null && coeff != 0) {
                    demandTerms.add(LinearExpr.term(selected_orders.get(o), coeff));
                }
            }

            // Build LinearExpr for variable aisle supply
            List<LinearExpr> aisleTerms = new ArrayList<>();
            for (int a = 0; a < aisles.size(); a++) {
                Integer coeff = aisles.get(a).get(i);
                if (coeff != null && coeff != 0) {
                    aisleTerms.add(LinearExpr.term(selected_aisles.get(a), coeff));
                }
            }

            LinearExpr demandExpr = LinearExpr.newBuilder().addSum(demandTerms.toArray(new LinearExpr[0])).build();
            LinearExpr aisleExpr = LinearExpr.newBuilder().addSum(aisleTerms.toArray(new LinearExpr[0])).build();
            aisleExpr = LinearExpr.sum(
                new LinearExpr[] { aisleExpr, LinearExpr.constant(supply) }
            );

            // Make the inequality: demand <= fixed + variable aisle supply
            model.addLessOrEqual(demandExpr, aisleExpr);
        }

    }
    protected void makeAvailableCapacityConstraint(CpModel model, List<BoolVar> selected_orders, List<BoolVar> selected_aisles) {
        makeAvailableCapacityConstraint(model, selected_orders, selected_aisles, Collections.emptySet(), Collections.emptySet());
    }

    protected void makePreSelectionConstraint(MPSolver solver, List<MPVariable> selected_orders, Set<Integer> preSelectedOrders) {

        for (int o : preSelectedOrders) {
            MPVariable x = selected_orders.get(o);
            MPConstraint fixedOrderConstraint = solver.makeConstraint(1, 1, "Fix order " + o);
            fixedOrderConstraint.setCoefficient(x, 1);
        }

    }

    // solve
    protected PartialResult calculatePartialResult(MPSolver solver, MPObjective objective, List<MPVariable> selected_orders, List<MPVariable> selected_aisles, Set<Integer> fixed_selected_orders, Set<Integer> fixed_selected_aisles) {
        
        final MPSolver.ResultStatus resultStatus = solver.solve();

        Set<Integer> finalOrders = new HashSet<>();
        Set<Integer> finalAisles = new HashSet<>();

        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
            // revisar condicion .OPTIMAL

            // pick orders
            for (Integer a : fixed_selected_orders) {
                finalOrders.add(a);
            }
            for (int i = 0; i < selected_orders.size(); i++) {
                MPVariable x = selected_orders.get(i);
                if (x.solutionValue() == 1) {
                    // System.out.println("x_" + i + ": " + x.solutionValue());
                    finalOrders.add(i);
                }
            }

            // pick aisles
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
    protected PartialResult calculatePartialResult(MPSolver solver, MPObjective objective, List<MPVariable> selected_orders, List<MPVariable> selected_aisles) {
        return calculatePartialResult(solver, objective, selected_orders, selected_aisles, Collections.emptySet(), Collections.emptySet());
        // default value for fixed_selected_aisles is empty set
    }
    protected PartialResult calculatePartialResult(CpSolver solver, CpModel model, List<BoolVar> selected_orders, List<BoolVar> selected_aisles, Set<Integer> fixed_selected_orders, Set<Integer> fixed_selected_aisles) {
        CpSolverStatus status = solver.solve(model);

        Set<Integer> finalOrders = new HashSet<>();
        Set<Integer> finalAisles = new HashSet<>();
        if (status == CpSolverStatus.OPTIMAL) {
            // revisar condicion .OPTIMAL

            for (int i = 0; i < selected_orders.size(); i++) {
                BoolVar x = selected_orders.get(i);
                if (solver.value(x) == 1) {
                    finalOrders.add(i);
                }
            }
            for (Integer a : fixed_selected_aisles) {
                finalAisles.add(a);
            }
            for (int i = 0; i < selected_aisles.size(); i++) {
                BoolVar y = selected_aisles.get(i);
                if (solver.value(y) == 1) {
                    finalAisles.add(i);
                }
            }

            ChallengeSolution partialSolution = new ChallengeSolution(finalOrders, finalAisles);
            
            return new PartialResult(partialSolution, solver.objectiveValue());
        } else {
            return new PartialResult(null, 0);
        }
    }
    protected PartialResult calculatePartialResult(CpSolver solver, CpModel model, List<BoolVar> selected_orders, List<BoolVar> selected_aisles) {
        return calculatePartialResult(solver, model, selected_orders, selected_aisles, Collections.emptySet(), Collections.emptySet());
        // default value for fixed_selected_aisles is empty set
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
    
}
