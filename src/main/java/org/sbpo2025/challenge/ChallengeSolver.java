package org.sbpo2025.challenge;

import org.apache.commons.lang3.time.StopWatch;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ChallengeSolver {
    private final long MAX_RUNTIME = 599000; // milliseconds; 10 minutes - 1 second

    protected List<Map<Integer, Integer>> orders;
    protected List<Map<Integer, Integer>> aisles;
    protected int nItems;
    protected int waveSizeLB;
    protected int waveSizeUB;
    protected boolean enableOutput = false; // Enable or disable solver output
    protected Solving solving;

    public ChallengeSolver(
        List<Map<Integer, Integer>> orders, List<Map<Integer, Integer>> aisles, int nItems, int waveSizeLB, int waveSizeUB) {
        this.orders = orders;
        this.aisles = aisles;
        this.nItems = nItems;
        this.waveSizeLB = waveSizeLB;
        this.waveSizeUB = waveSizeUB;
        this.solving = new Solving(this);
    }

    public ChallengeSolution solve(StopWatch stopWatch) {
        
        // Implement your solution here
        PartialResult bestSolution = new PartialResult(null, 0);

        float meanOrderSize = calculateMeanOrderSize(IntStream.range(0, orders.size()).boxed().collect(Collectors.toSet()));
        float meanAisleCapacity = calculateMeanAisleCapacity(IntStream.range(0, aisles.size()).boxed().collect(Collectors.toSet()));

        // problem parameters
        System.out.println("Orders number: " + orders.size());
        System.out.println("Aisles number: " + aisles.size());
        System.out.println("Items number: " + nItems);
        System.out.println("Wave size bounds: [" + waveSizeLB + ", " + waveSizeUB + "]");
        System.out.println("Mean order size: " + meanOrderSize);
        System.out.println("Mean aisle capacity: " + meanAisleCapacity);

        // // Metodo 1 --- iterar sobre la cantidad de pasillos comenzando desde el minimo factible
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

        // // Metodo 2 --- iterar sobre la cantidad de pasillos desde 1 hasta el total
        // bestSolution = solveWithFixedAisles(bestSolution, stopWatch);
        // // start with 1 aisle
        // // -------------------------------------------------------------------------------

        // // Metodo 3 --- iterar sobre la cantidad de items (desde LB hasta UB)
        // // -> poco eficiente------------
        // bestSolution = solveWithFixedItems(bestSolution, stopWatch);
        // // -------------------------------------------------------------------------------

        // // Metodo 4 --- iterar sobre la cantidad de pasillos, seleccionando los pasillos con mayor capacidad
        // // resolver buscando la cantidad minima de pasillos e iterar desde ahi ------------
        // bestSolution = solveMinimumFeasibleAisles(bestSolution, stopWatch);
        // if (bestSolution.partialSolution() == null) {
        //     System.out.println("No feasible solution found, stopping.");
        //     return null; // No feasible solution found (for the whole problem)
        // }
        // int minimumAisles = bestSolution.partialSolution().aisles().size();
        // // int minimumAisles = 1;
        // bestSolution = solveWithSelectedAisles(bestSolution, stopWatch, minimumAisles);
        // // start iterating with minimumAisles
        // // -------------------------------------------------------------------------------

        // Metodo 5 --- seleccionar ordenes al azar y minimizar pasillos necesarios
        int maxIterations = 100;
        int n = 1;
        int gap = (int) Math.ceil((waveSizeUB - waveSizeLB) / 100.0) * n; // n% of the range [LB, UB]
        int LB = waveSizeLB + gap*10; // starting point: LB+gap*s, with s in [0,99]
        int UB = LB + gap;
        bestSolution = solveWithSelectedOrders(bestSolution, stopWatch, LB, UB, maxIterations); // adapt waveSize
        // bestSolution = solveWithSelectedOrders(bestSolution, stopWatch, maxIterations);
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
            PartialResult partialResult = solving.problem1a(k, getRemainingTime(stopWatch));
            // PartialResult partialResult = solving.problem1aCP(k, getRemainingTime(stopWatch));
    
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
            PartialResult partialResult = solving.problem1b(k, getRemainingTime(stopWatch));

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
        PartialResult partialResult = solving.problem1c(getRemainingTime(stopWatch));

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
            PartialResult partialResult = solving.problem2a(selectedAisles, getRemainingTime(stopWatch));
    
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

    protected PartialResult solveWithSelectedOrders(PartialResult bestSolution, StopWatch stopWatch, int LB, int UB, int iterations) {
        System.out.println("\n>> solveWithSelectedOrders");

        Random random = new Random(123);
        int feasibleSolutions = 0;

        if (LB == 0) {
            LB = 1;
        }

        // iterate over the number of orders
        for (int k = 1; iterations == -1 || k <= iterations; k++) {

            int N = random.nextInt(UB - LB + 1) + LB; // cantidad de items aleatoria entre [LB, UB]
            System.out.println("\nIteration " + k + " with N=" + N + " items");

            Set<Integer> remainingOrders = IntStream.range(0, orders.size()).boxed().collect(Collectors.toSet());
            Set<Integer> selectedOrders = new HashSet<>();
            int waveSize = 0;
            long remainingTime = getRemainingTime(stopWatch);
            while (remainingTime - getRemainingTime(stopWatch) < 10) {
                
                int order = randomOrder(random, remainingOrders);                
                if (order == -1) {
                    System.out.println("No orders found in the list.");
                    return bestSolution;
                }

                int orderSize = 0;
                for (Map.Entry<Integer, Integer> entry : orders.get(order).entrySet()) {
                    orderSize += entry.getValue();
                }

                if (waveSize + orderSize > N) {
                    if (waveSize < waveSizeLB) {
                        continue; // Not enough items selected, trying next order
                    } else {
                        break; // Wave size exceeded N, stopping selection
                    }
                }

                selectedOrders.add(order);
                remainingOrders.remove(order);
                waveSize += orderSize;
            }
            
            if (getRemainingTime(stopWatch) == 0) {
                System.out.println("Max runtime reached, stopping iteration over random selected orders.");
                break;
            } // stop iteration if no time left
            System.out.println("Remaining time: " + getRemainingTime(stopWatch) + " seconds");

            // solve
            System.out.println("Minimizing visited aisles for selected orders (with N=" + N + " items)");
            // System.out.println("Picked orders: " + Arrays.toString(selectedOrders.toArray()));
            PartialResult partialResult = solving.problem2b(selectedOrders, getRemainingTime(stopWatch));

            if (partialResult.partialSolution() == null) {
                System.out.println("No feasible solution found for iteration " + k);
                continue;
            } // no feasible

            // show optimal for k orders
            System.out.println("Partial Solution:");
            System.out.println("Selected orders = " + partialResult.partialSolution().orders());
            System.out.println("Selected aisles = " + partialResult.partialSolution().aisles());
            System.out.println("Objective value = " + partialResult.objValue());
            feasibleSolutions++;

            // update best solution
            if (partialResult.objValue() > bestSolution.objValue()) {
                bestSolution = partialResult;
            }

        }

        System.out.println("Done iterating over random selected orders: " + iterations + " iterations");
        System.out.println("Best solution found with value " + bestSolution.objValue());
        System.out.println("Feasibility rate: " + (feasibleSolutions / (float) iterations));

        return bestSolution;
    }
    protected PartialResult solveWithSelectedOrders(PartialResult bestSolution, StopWatch stopWatch) {
        return solveWithSelectedOrders(bestSolution, stopWatch, waveSizeLB, waveSizeUB, -1);
    }
    protected PartialResult solveWithSelectedOrders(PartialResult bestSolution, StopWatch stopWatch, int iterations) {
        return solveWithSelectedOrders(bestSolution, stopWatch, waveSizeLB, waveSizeUB, iterations);
    }
    protected PartialResult solveWithSelectedOrders(PartialResult bestSolution, StopWatch stopWatch, int LB, int UB) {
        return solveWithSelectedOrders(bestSolution, stopWatch, LB, UB, -1);
    }


    /*
     * Get the remaining time in seconds
     */
    protected long getRemainingTime(StopWatch stopWatch) {
        return Math.max(
                TimeUnit.SECONDS.convert(MAX_RUNTIME - stopWatch.getTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS),
                0);
    }

    public int randomOrder(Random random, Set<Integer> ordersList) {
        if (ordersList.isEmpty()) {
            return -1;
        }
        int index = random.nextInt(ordersList.size());
        Integer[] array = ordersList.toArray(new Integer[0]);
        return array[index];
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

    public float calculateMeanAisleCapacity(Set<Integer> aislesList) {

        float mean = 0;
        int aisleCapacity = 0;

        for (int aisle : aislesList) { // each aisle
            aisleCapacity = 0;
            for (Map.Entry<Integer, Integer> entry : aisles.get(aisle).entrySet()) {
                aisleCapacity += entry.getValue(); // each item
            }
            mean += aisleCapacity / (float) aisles.size();
        }

        return mean;
    }

    public float calculateMeanOrderSize(Set<Integer> ordersList) {

        float mean = 0;
        int orderSize = 0;

        for (int order : ordersList) { // each order
            orderSize = 0;
            for (Map.Entry<Integer, Integer> entry : orders.get(order).entrySet()) {
                orderSize += entry.getValue(); // each item
            }
            mean += orderSize / (float) orders.size();
        }
        
        return mean;
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
