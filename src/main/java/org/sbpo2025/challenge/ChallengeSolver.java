package org.sbpo2025.challenge;

import org.apache.commons.lang3.time.StopWatch;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ChallengeSolver {
    private final long MAX_RUNTIME = 599000; // milliseconds; 10 minutes - 1 second
    // private final long MAX_RUNTIME = 5990; // para testear timeout

    protected List<Map<Integer, Integer>> orders;
    protected List<Map<Integer, Integer>> aisles;
    protected List<Item> items;
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

        this.items = new ArrayList<>();
        for (int i = 0; i < nItems; i++) {
            this.items.add(new Item(i, new HashMap<>(), new HashMap<>()));
        }
        for (int orderId = 0; orderId < orders.size(); orderId++) {
            Map<Integer, Integer> order = orders.get(orderId);
            for (Map.Entry<Integer, Integer> entry : order.entrySet()) {
                int itemId = entry.getKey();
                int quantity = entry.getValue();
                this.items.get(itemId).addOrder(orderId, quantity);
            }
        }
        for (int aisleId = 0; aisleId < aisles.size(); aisleId++) {
            Map<Integer, Integer> aisle = aisles.get(aisleId);
            for (Map.Entry<Integer, Integer> entry : aisle.entrySet()) {
                int itemId = entry.getKey();
                int capacity = entry.getValue();
                this.items.get(itemId).addAisle(aisleId, capacity);
            }
        }

    }

    public ChallengeSolution solve(StopWatch stopWatch) {
        
        // Implement your solution here
        PartialResult bestSolution = new PartialResult(null, 0);
        PartialResult nullSolution = new PartialResult(null, 0);

        double[] orderStats = calculateMeanOrderSize(IntStream.range(0, orders.size()).boxed().collect(Collectors.toSet()));
        double meanOrderSize = orderStats[0];
        double meanOrderItems = orderStats[1];
        double[] aisleStats = calculateMeanAisleCapacity(IntStream.range(0, aisles.size()).boxed().collect(Collectors.toSet()));
        double meanAisleCapacity = aisleStats[0];
        double meanAisleItems = aisleStats[1];

        // problem parameters
        System.out.println("Orders number: " + orders.size());
        System.out.println("Aisles number: " + aisles.size());
        System.out.println("Items number: " + nItems);
        System.out.println("Wave size bounds: [" + waveSizeLB + ", " + waveSizeUB + "]");
        System.out.println("Mean order size: " + meanOrderSize);
        System.out.println("Mean order items: " + meanOrderItems);
        System.out.println("Mean aisle capacity: " + meanAisleCapacity);
        System.out.println("Mean aisle items: " + meanAisleItems);

        // Random random = new Random(1234);

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
        // // -> generalizado en Metodo 1
        // bestSolution = solveWithFixedAisles(bestSolution, stopWatch);
        // // start with 1 aisle
        // // -------------------------------------------------------------------------------

        // // Metodo 3 --- iterar sobre la cantidad de items (desde LB hasta UB)
        // // -> poco eficiente------------
        // bestSolution = solveWithFixedItems(bestSolution, stopWatch);
        // // -------------------------------------------------------------------------------

        // // Metodo 4 --- iterar sobre la cantidad de pasillos, seleccionando los pasillos con mayor capacidad
        // // resolver buscando la cantidad minima de pasillos e iterar desde ahi ------------
        // // -> parejo en valor objetivo con los metodos aleatorios, pero usando todo el tiempo disponible
        // // bestSolution = solveMinimumFeasibleAisles(bestSolution, stopWatch);
        // // if (bestSolution.partialSolution() == null) {
        // //     System.out.println("No feasible solution found, stopping.");
        // //     return null; // No feasible solution found (for the whole problem)
        // // }
        // // int minimumAisles = bestSolution.partialSolution().aisles().size();
        // int minimumAisles = 1;
        // bestSolution = solveWithSelectedAisles(bestSolution, stopWatch, minimumAisles);
        // // start iterating with minimumAisles
        // // -------------------------------------------------------------------------------

        // // Metodo 5 --- seleccionar ordenes al azar y minimizar pasillos necesarios
        // // -> algoritmo greedy es mas eficiente, la seleccion de ordenes es mas inteligente
        // int maxIterations = 100;
        // int n = 1;
        // double gap = (waveSizeUB - waveSizeLB) / 100.0f * n; // n% of the range [LB, UB]
        // int LB = waveSizeLB + (int) Math.ceil(gap * 10); // starting point: LB+gap*s, with s in [0,99]
        // int UB = LB + (int) Math.ceil(gap);
        // bestSolution = solveWithRandomSelectedOrders(bestSolution, stopWatch, LB, UB, maxIterations); // adapt waveSize
        // // bestSolution = solveWithRandomSelectedOrders(bestSolution, stopWatch, maxIterations);
        // // -------------------------------------------------------------------------------

        // // Metodo 6 --- algoritmo greedy para seleccionar ordenes
        // bestSolution = solveGreedySelection(bestSolution, stopWatch);
        // // -------------------------------------------------------------------------------   

        // // Metodo 6.1 --- algoritmo greedy para seleccionar ordenes aleatoriamente, obteniendo varias soluciones
        // // -> mejora minima con respecto a una unica ejecucion
        // int maxIterations = 10;
        // bestSolution = solveRandomGreedySelection(bestSolution, stopWatch, maxIterations);
        // // -------------------------------------------------------------------------------
        
        // // Metodo 7 -- usar preseleccion de pasillos (fija) y de ordenes (single-aisle) para poder agregar ordenes multi-pasillo
        // // -> no sirve para instancias con solo ordenes single-aisle
        // // -> necesita solucion previa para otras instancias ... (pendiente testear)
        // bestSolution = solveWithPreSelection(bestSolution, stopWatch);
        // // -------------------------------------------------------------------------------

        // // Metodo 7.1  -- usar preseleccion de ordenes y minimizar pasillos
        // // -> ejecuta rapido, pero no mejora la solucion
        // bestSolution = solveWithSelectedOrders(bestSolution, stopWatch);
        // // -------------------------------------------------------------------------------

        // // Metodo 8 --- algoritmo greedy para seleccionar ordenes sobre un subconjunto de pasillos
        // // desde minimumAisles hacia abajo
        // int minimumAisles = 1; // minimo por defecto
        // for (int k = minimumAisles; k > 0; k--) {
        //     PartialResult newSolution = solveSuperAisleGreedySelection(nullSolution, stopWatch, k);
        //     if (newSolution == null || newSolution.objValue() == 0) {
        //         break;
        //     } else if (newSolution != null && newSolution.objValue() > bestSolution.objValue()) {
        //         bestSolution = newSolution;
        //         if (k < minimumAisles) {
        //             minimumAisles = k;
        //         }
        //     }
        // }
        // // -------------------------------------------------------------------------------

        // // Metodo 8.1 --- algoritmo greedy para seleccionar ordenes sobre un subconjunto de pasillos
        // // desde 1 pasillo hacia arriba
        // for (int k = 1; k <= aisles.size(); k++) {
        //     if (bestSolution.objValue() >= waveSizeUB/k) {
        //         System.out.println("Current best solution with value " + bestSolution.objValue() + " is already better than the maximum possible for k >= " + k);
        //         break;
        //     }
        //     bestSolution = solveSuperAisleGreedySelection(bestSolution, stopWatch, k);
        //     // PartialResult newSolution = solveSuperAisleGreedySelection(nullSolution, stopWatch, k);
        //     // newSolution = solveWithPreSelection(newSolution, stopWatch);
        //     // if (newSolution != null && newSolution.objValue() > bestSolution.objValue()) {
        //     //     bestSolution = newSolution;
        //     // }
        // }
        // int bestAislesNumber = bestSolution.partialSolution().aisles().size();
        // -------------------------------------------------------------------------------

        // // Metodo 8.2 --- algoritmo greedy para seleccionar ordenes sobre un subconjunto de pasillos
        // // mantener cantidad de pasillos fija, pero seleccionar aleatoriamente
        // System.out.println("\n-> Random selection of aisles");
        // for (int i=0; i<10; i++) {
        //     System.out.println("Iteration " + (i+1));
        //     int k = bestAislesNumber;
        //     int n = (int)(bestAislesNumber*1.5);
        //     Set<Integer> selectedAisles = getRandomAislesSubset(k, getBestAislesSubset(n), random);
        //     bestSolution = solveSuperAisleGreedySelection(bestSolution, stopWatch, selectedAisles);
        //     bestSolution = solveWithAisleSubset(bestSolution, stopWatch, selectedAisles);
        // }
        // // -------------------------------------------------------------------------------

        // // Metodo 9 --- algoritmo de búsqueda tabú
        // int K = bestAislesNumber; // número de pasillos a considerar
        // int p = (int)Math.ceil(K*0.3); // número de elementos a intercambiar
        // // p = 1;
        // int n = 3*K; // tamaño del vecindario
        // int maxIterations = 20;
        // PartialResult newSolution = tabuSearch(nullSolution, stopWatch, K, p, n, maxIterations);
        // bestSolution = solveWithAisleSubset(bestSolution, stopWatch, newSolution.partialSolution().aisles());
        // // -------------------------------------------------------------------------------

        // bestSolution = solveWithFixedAisles(bestSolution, stopWatch, 10,10);

        // #########################################################################################
        // Metodo Final Completo
        // 1) Estimar valor objetivo para cada cantidad de pasillos
        // greedy sobre un super-pasillo ficticio compuesto por un subconjunto de "minimumAisles" pasillos
        int minimumAisles = 1; // minimo por defecto
        Map<Integer, Double> candidateAisleNumbers = new HashMap<>();

        boolean feasibleSolutionFound = false;
        for (int k = 1; k <= aisles.size(); k++) {
            PartialResult newSolution = solveSuperAisleGreedySelection(nullSolution, stopWatch, k);
            candidateAisleNumbers.put(k, newSolution.objValue());
            // bestSolution = solveSuperAisleGreedySelection(bestSolution, stopWatch, k);
            if (newSolution.partialSolution() != null) {
                if (!feasibleSolutionFound) {
                    minimumAisles = k; // actualizar minimo si es la primera factible
                    feasibleSolutionFound = true;
                }
                if (newSolution.objValue() > bestSolution.objValue()) {
                    bestSolution = newSolution;
                }
            }
            if (bestSolution.objValue() >= waveSizeUB/(k+1)) {
                System.out.println("Current best solution with value " + bestSolution.objValue() + " is already better than the maximum possible for k >= " + (k+1));
                break;
            }
        }
        bestSolution = solveWithAisleSubset(bestSolution, stopWatch, bestSolution.partialSolution().aisles());
        candidateAisleNumbers.put(bestSolution.partialSolution().aisles().size(), bestSolution.objValue());
        
        // 2) metodo exacto para instancias pequeñas
        boolean exactMinimumAisles = false;
        if (orders.size() <= 15000 && nItems <= 10000) {
            System.out.println("\n-> using exact method for small instances");
            // boolean exactMinimumAisles = false;
            bestSolution = solveMinimumFeasibleAisles(bestSolution, stopWatch, 60); // se limita en tiempo
            if (bestSolution.partialSolution() == null) {
                System.out.println("No feasible solution found, stopping.");
                // return null; // No feasible solution found (for the whole problem) or timeout
            } else {
                minimumAisles = bestSolution.partialSolution().aisles().size();
                candidateAisleNumbers.put(minimumAisles, bestSolution.objValue());
                exactMinimumAisles = true;
                final int minimumAislesFinal = minimumAisles;
                candidateAisleNumbers.entrySet().removeIf(entry -> entry.getKey() < minimumAislesFinal);
            }
        }

        if (!exactMinimumAisles) {
            for (int k = 1; k < minimumAisles; k++) {
                candidateAisleNumbers.put(k, 0.0);
            }
            candidateAisleNumbers.put(minimumAisles-1, bestSolution.objValue()); // probar factibilidad con uno pasillo menos
        }

        candidateAisleNumbers = candidateAisleNumbers.entrySet()
            .stream()
            .sorted((e1, e2) -> {
                int valueCompare = Double.compare(e2.getValue(), e1.getValue()); // Descending by value
                if (valueCompare != 0) {
                    return valueCompare;
                } else {
                    return Integer.compare(e2.getKey(), e1.getKey()); // Descending by key if values are equal
                }
            })
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));

        for (Integer key : candidateAisleNumbers.keySet()) System.out.println("\n-> Key: " + key + ", Value: " + candidateAisleNumbers.get(key));
        
        if (exactMinimumAisles) {
            bestSolution = solveWithFixedAisles(bestSolution, stopWatch, minimumAisles, aisles.size(), 5); // metodo exacto iterando sobre nAisles
        } else {
            for (Integer key : candidateAisleNumbers.keySet()) {
                if (getRemainingTime(stopWatch) < 1) {
                    System.out.println("Max runtime reached, stopping iteration over candidate aisles.");
                    break;
                }
                System.out.println("\n-> Key: " + key + ", Value: " + candidateAisleNumbers.get(key));
                if (meanOrderItems > 1) {
                    bestSolution = solveWithFixedAisles(bestSolution, stopWatch, key, key); // metodo exacto para key pasillos
                } else {
                    bestSolution = solveWithAisleSubset(bestSolution, stopWatch,
                        getBestAislesSubset(key)
                    ); // metodo exacto para key pasillos
                }
            }
        }
        // fin #########################################################################
        
    
        // retrieve the final best solution
        System.out.println("\nBest solution found with value " + bestSolution.objValue());
        System.out.println("Final remaining time: " + getRemainingTime(stopWatch) + " seconds");

        int capacity = totalCapacity(bestSolution.partialSolution().aisles());
        int demand = totalDemand(bestSolution.partialSolution().orders());
        System.out.println("Total capacity = " + capacity);
        System.out.println("Total demand = " + demand);
        System.out.println("Total capacity left = " + (capacity - demand) + " = " + (((double) (capacity - demand)/capacity)*100)+ "%");
        

        return bestSolution.partialSolution();
    }


    // solving methods

    protected PartialResult solveWithFixedAisles(PartialResult bestSolution, StopWatch stopWatch, int initialAislesNumber, int finalAislesNumber, int maxIterationsWithoutImprovement) {
        System.out.println("\n>> solveWithFixedAisles");
        
        int iterationsWithoutImprovement = 0;
        double prevObj = 0;

        // iterate over the number of aisles
        for (int k = initialAislesNumber; k <= finalAislesNumber; k++) {

            if (iterationsWithoutImprovement >= maxIterationsWithoutImprovement) {
                System.out.println("Max iterations without improvement reached, stopping.");
                break;
            }

            if (getRemainingTime(stopWatch) < 1) {
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
            // System.out.println("Partial Solution:");
            // System.out.println("Selected orders = " + partialResult.partialSolution().orders());
            // System.out.println("Selected aisles = " + partialResult.partialSolution().aisles());
            System.out.println("Objective value = " + partialResult.objValue());
            double usedCapacity = (1 - totalCapacityLeft(partialResult.partialSolution())) * 100.0;
            System.out.println(String.format("Total capacity used = %.2f%%", usedCapacity));

            if (partialResult.objValue() >= prevObj) {
                iterationsWithoutImprovement = 0;
            } else {
                iterationsWithoutImprovement++;
            }
            prevObj = partialResult.objValue();

            // update best solution
            if (partialResult.objValue() > bestSolution.objValue()) {
                bestSolution = partialResult;
            }

        }

        System.out.println("Done iterating over k (fixed aisles)");
        // System.out.println("Best solution found with value " + bestSolution.objValue());

        return bestSolution;
    }
    protected PartialResult solveWithFixedAisles(PartialResult bestSolution, StopWatch stopWatch) {
        return solveWithFixedAisles(bestSolution, stopWatch, 1, aisles.size(), aisles.size());
        // default value for initialAislesNumber is 1
    }
    protected PartialResult solveWithFixedAisles(PartialResult bestSolution, StopWatch stopWatch, int initialAislesNumber) {
        return solveWithFixedAisles(bestSolution, stopWatch, initialAislesNumber, aisles.size(), aisles.size());
        // default value for initialAislesNumber is 1
    }
    protected PartialResult solveWithFixedAisles(PartialResult bestSolution, StopWatch stopWatch, int initialAislesNumber, int finalAislesNumber) {
        return solveWithFixedAisles(bestSolution, stopWatch, initialAislesNumber, finalAislesNumber, aisles.size());
        // default value for initialAislesNumber is 1
    }

    protected PartialResult solveWithFixedItems(PartialResult bestSolution, StopWatch stopWatch) {
        System.out.println("\n>> solveWithFixedItems");

        // iterate over the number of items: UB, UB-1, ..., LB
        for (int k = waveSizeUB; k >= waveSizeLB; k--) {

            if (getRemainingTime(stopWatch) < 1) {
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
            // System.out.println("Partial Solution:");
            // System.out.println("Selected orders = " + partialResult.partialSolution().orders());
            // System.out.println("Selected aisles = " + partialResult.partialSolution().aisles());
            System.out.println("Objective value = " + partialResult.objValue());
            double usedCapacity = (1 - totalCapacityLeft(partialResult.partialSolution())) * 100.0;
            System.out.println(String.format("Total capacity used = %.2f%%", usedCapacity));

            // update best solution
            if (partialResult.objValue() > bestSolution.objValue()) {
                bestSolution = partialResult;
            }

        }

        System.out.println("Done iterating over k (fixed items)");
        // System.out.println("Best solution found with value " + bestSolution.objValue());


        return bestSolution;
    }

    protected PartialResult solveMinimumFeasibleAisles(PartialResult bestSolution, StopWatch stopWatch, int timeLimit) {
        System.out.println("\n>> solveMinimumFeasibleAisles");
        System.out.println("Remaining time: " + getRemainingTime(stopWatch) + " seconds");
        System.out.println("Time limit for solveMinimumFeasibleAisles: " + timeLimit + " seconds");

        // solve
        System.out.println("Minimizing visited aisles");
        PartialResult partialResult = solving.problem1c(timeLimit);

        if (partialResult.partialSolution() == null) {
            System.out.println("No feasible solution found");
            return bestSolution;
        } // no feasible solution found => time limit reached, or the whole problem is infeasible

        // System.out.println("Partial Solution:");
        // System.out.println("Selected orders = " + partialResult.partialSolution().orders());
        // System.out.println("Selected aisles = " + partialResult.partialSolution().aisles());
        System.out.println("Objective value = " + partialResult.objValue());
        double usedCapacity = (1 - totalCapacityLeft(partialResult.partialSolution())) * 100.0;
        System.out.println(String.format("Total capacity used = %.2f%%", usedCapacity));
            
        // update best solution
        if (partialResult.objValue() > bestSolution.objValue()) {
            bestSolution = partialResult;
        }

        return bestSolution;
    }

    protected PartialResult solveWithSelectedAisles(PartialResult bestSolution, StopWatch stopWatch, int initialAislesNumber, int maxIterationsWithoutImprovement) {
        System.out.println("\n>> solveWithSelectedAisles");

        Set<Integer> selectedAisles = new HashSet<>();
        Set<Integer> remainingAisles = IntStream.range(0, aisles.size()).boxed().collect(Collectors.toSet());

        int iterationsWithoutImprovement = 0;
        double prevObj = 0;

        // iterate over the number of aisles
        for (int k = 1; k <= aisles.size(); k++) {

            if (iterationsWithoutImprovement >= maxIterationsWithoutImprovement) {
                System.out.println("Max iterations without improvement reached, stopping.");
                break;
            }

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

            if (waveSizeUB/k <= bestSolution.objValue()) {
                // stopping condition due to optimality
                System.out.println("Current best solution with value " + bestSolution.objValue() + " is already better than the maximum possible for k >= " + k);
                break;
            }

            if (getRemainingTime(stopWatch) < 1) {
                System.out.println("Max runtime reached, stopping iteration over k.");
                break;
            } // stop iteration if no time left
            System.out.println("Remaining time: " + getRemainingTime(stopWatch) + " seconds");

            // solve
            System.out.println("\nMaximizing picked items for number of aisles k = " + k);
            System.out.println("Picked aisles: " + Arrays.toString(selectedAisles.toArray()));
            PartialResult partialResult = solving.problem2a(selectedAisles, getRemainingTime(stopWatch));
    
            if (partialResult.partialSolution() == null) {
                System.out.println("No feasible solution found for k = " + k);
                continue;
            } // no feasible

            // show optimal for k aisles
            // System.out.println("Partial Solution:");
            // System.out.println("Selected orders = " + partialResult.partialSolution().orders());
            // System.out.println("Selected aisles = " + partialResult.partialSolution().aisles());
            System.out.println("Objective value = " + partialResult.objValue());
            double usedCapacity = (1 - totalCapacityLeft(partialResult.partialSolution())) * 100.0;
            System.out.println(String.format("Total capacity used = %.2f%%", usedCapacity));
            
            if (partialResult.objValue() >= prevObj) {
                iterationsWithoutImprovement = 0;
            } else {
                iterationsWithoutImprovement++;
            }
            prevObj = partialResult.objValue();

            // update best solution
            if (partialResult.objValue() > bestSolution.objValue()) {
                bestSolution = partialResult;
            }

        }

        System.out.println("Done iterating over k (fixed aisles)");
        // System.out.println("Best solution found with value " + bestSolution.objValue());

        return bestSolution;
    }
    protected PartialResult solveWithSelectedAisles(PartialResult bestSolution, StopWatch stopWatch) {
        return solveWithSelectedAisles(bestSolution, stopWatch, 1, aisles.size());
        // all possible aisles number
    }
    protected PartialResult solveWithSelectedAisles(PartialResult bestSolution, StopWatch stopWatch, int initialAislesNumber) {
        return solveWithSelectedAisles(bestSolution, stopWatch, initialAislesNumber, aisles.size());
        // default max iterations = total number of aisles - initial number of aisles
    }

    protected PartialResult solveWithAisleSubset(PartialResult bestSolution, StopWatch stopWatch, Set<Integer> selectedAisles) {
        System.out.println("\n>> solveWithAisleSubset");

        // solve
        System.out.println("\nMaximizing picked items for picked aisles: " + Arrays.toString(selectedAisles.toArray()));
        PartialResult partialResult = solving.problem2a(selectedAisles, getRemainingTime(stopWatch));
    
        if (partialResult.partialSolution() == null) {
            System.out.println("No feasible solution found");
            return bestSolution;
        } // no feasible
            
        System.out.println("Objective value = " + partialResult.objValue());
        double usedCapacity = (1 - totalCapacityLeft(partialResult.partialSolution())) * 100.0;
        System.out.println(String.format("Total capacity used = %.2f%%", usedCapacity));
            
        // update best solution
        if (partialResult.objValue() > bestSolution.objValue()) {
            bestSolution = partialResult;
        }

        return bestSolution;
    }

    protected PartialResult solveWithSelectedOrders(PartialResult bestSolution, StopWatch stopWatch) {
        System.out.println("\n>> solveWithSelectedOrders");

        Set<Integer> selectedOrders = new HashSet<>();
        if (bestSolution.partialSolution() == null) {
            System.out.println("No previous solution found, stopping.");
            return bestSolution;
        } else {
            for (int order : bestSolution.partialSolution().orders()) {
                selectedOrders.add(order);
            }
        }

        // solve
        System.out.println("Minimizing visited aisles for selected orders from previous solution");
        // System.out.println("Picked orders: " + Arrays.toString(selectedOrders.toArray()));
        PartialResult partialResult = solving.problem2b(selectedOrders, getRemainingTime(stopWatch));
    
        // System.out.println("Partial Solution:");
        // System.out.println("Selected orders = " + partialResult.partialSolution().orders());
        // System.out.println("Selected aisles = " + partialResult.partialSolution().aisles());
        System.out.println("Objective value = " + partialResult.objValue());
        double usedCapacity = (1 - totalCapacityLeft(partialResult.partialSolution())) * 100.0;
        System.out.println(String.format("Total capacity used = %.2f%%", usedCapacity));

        // update best solution
        if (partialResult.objValue() > bestSolution.objValue()) {
            bestSolution = partialResult;
        }

        return bestSolution;
    }

    protected PartialResult solveWithRandomSelectedOrders(PartialResult bestSolution, StopWatch stopWatch, int LB, int UB, int iterations) {
        System.out.println("\n>> solveWithRandomSelectedOrders");

        Random random = new Random(123);
        int feasibleSolutions = 0;

        if (LB == 0) {
            LB = 1;
        }

        // iterate over the number of orders
        for (int k = 1; iterations == -1 || k <= iterations; k++) {

            if (getRemainingTime(stopWatch) < 1) {
                    System.out.println("Max runtime reached, stopping iteration over random selected orders.");
                    break;
                }

            int N = random.nextInt(UB - LB + 1) + LB; // cantidad de items aleatoria entre [LB, UB]
            System.out.println("\nIteration " + k + " with N=" + N + " items");

            Set<Integer> remainingOrders = IntStream.range(0, orders.size()).boxed().collect(Collectors.toSet());
            Set<Integer> selectedOrders = new HashSet<>();
            int waveSize = 0;
            long remainingTime = getRemainingTime(stopWatch);
            System.out.println("Remaining time: " + remainingTime + " seconds");

            while (remainingTime - getRemainingTime(stopWatch) < 10) {

                if (getRemainingTime(stopWatch) < 1) {
                    System.out.println("Max runtime reached, stopping iteration over random selected orders.");
                    break;
                } // stop iteration if no time left
                // System.out.println("Remaining time: " + getRemainingTime(stopWatch) + " seconds");
                
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

            // solve
            System.out.println("Minimizing visited aisles for selected orders (with N=" + N + " items)");
            // System.out.println("Picked orders: " + Arrays.toString(selectedOrders.toArray()));
            PartialResult partialResult = solving.problem2b(selectedOrders, getRemainingTime(stopWatch));

            if (partialResult.partialSolution() == null) {
                System.out.println("No feasible solution found for iteration " + k);
                continue;
            } // no feasible

            // show optimal for k orders
            // System.out.println("Partial Solution:");
            // System.out.println("Selected orders = " + partialResult.partialSolution().orders());
            // System.out.println("Selected aisles = " + partialResult.partialSolution().aisles());
            System.out.println("Objective value = " + partialResult.objValue());
            double usedCapacity = (1 - totalCapacityLeft(partialResult.partialSolution())) * 100.0;
            System.out.println(String.format("Total capacity used = %.2f%%", usedCapacity));
            feasibleSolutions++;

            // update best solution
            if (partialResult.objValue() > bestSolution.objValue()) {
                bestSolution = partialResult;
            }

        }

        System.out.println("Done iterating over random selected orders: " + iterations + " iterations");
        // System.out.println("Best solution found with value " + bestSolution.objValue());
        System.out.println("Feasibility rate: " + (feasibleSolutions / (double) iterations));

        return bestSolution;
    }
    protected PartialResult solveWithRandomSelectedOrders(PartialResult bestSolution, StopWatch stopWatch) {
        return solveWithRandomSelectedOrders(bestSolution, stopWatch, waveSizeLB, waveSizeUB, -1);
    }
    protected PartialResult solveWithRandomSelectedOrders(PartialResult bestSolution, StopWatch stopWatch, int iterations) {
        return solveWithRandomSelectedOrders(bestSolution, stopWatch, waveSizeLB, waveSizeUB, iterations);
    }
    protected PartialResult solveWithRandomSelectedOrders(PartialResult bestSolution, StopWatch stopWatch, int LB, int UB) {
        return solveWithRandomSelectedOrders(bestSolution, stopWatch, LB, UB, -1);
    }

    protected PartialResult solveGreedySelection(PartialResult bestSolution, StopWatch stopWatch) {
        System.out.println("\n>> solveGreedySelection");

        Set<Integer> selectedAisles = new HashSet<>();
        Set<Integer> remainingAisles = IntStream.range(0, aisles.size()).boxed().collect(Collectors.toSet());

        Set<Integer> selectedOrders = new HashSet<>();
        Set<Integer> remainingOrders = IntStream.range(0, orders.size()).boxed().collect(Collectors.toSet());

        int waveSize = 0;

        // iterate over the number of aisles
        for (int k = 1; k <= aisles.size(); k++) {

            if (getRemainingTime(stopWatch) < 1) {
                System.out.println("Max runtime reached, stopping iteration over k.");
                break;
            } // stop iteration if no time left
            // System.out.println("Remaining time: " + getRemainingTime(stopWatch) + " seconds");

            // if (waveSize >= waveSizeLB) {
            //     System.out.println("LB reached");
            //     break; // opcional
            // }
            
            if (waveSize >= waveSizeLB && waveSizeUB/k <= bestSolution.objValue()) {
                // stopping condition due to optimality
                System.out.println("Current best solution with value " + bestSolution.objValue() + " is already better than the maximum possible for k >= " + k);
                break;
            }


            System.out.println("\nSelecting orders of available items from " + k + " aisles");

            int aisle = maxCapacityAisle(remainingAisles);
            if (aisle == -1) {
                System.out.println("No aisles found in the list.");
                break;
            }
            remainingAisles.remove(aisle);
            selectedAisles.add(aisle);

            // if (k < initialAislesNumber) {
            //     continue; // skip iterations until we reach the initial aisles number
            // }

            int newOrdersCount = 0;
            Map<Integer, Integer> aisleItems = aisles.get(aisle);
            for (Map.Entry<Integer, Integer> entry : aisleItems.entrySet()) { // for item in aisle

                Item item = items.get(entry.getKey());
                int capacity = entry.getValue();
                
                for (Map.Entry<Integer, Integer> order : item.orders.entrySet()) { // for order with this item

                    int orderId = order.getKey();
                    int orderQuantity = order.getValue();

                    if (waveSize + orderQuantity > waveSizeUB) {
                        break;
                    }

                    // Check if the order can be fulfilled
                    if (orders.get(orderId).size() > 1 || capacity < orderQuantity) {
                        continue;
                    }
                    
                    if (remainingOrders.contains(orderId)) {
                        selectedOrders.add(orderId);
                        remainingOrders.remove(orderId);
                        capacity -= orderQuantity;
                        waveSize += orderQuantity;
                        newOrdersCount++;
                    }
                }
            }
            System.out.println("New orders count: " + newOrdersCount);

            PartialResult partialResult = generatePartialResult(selectedOrders, selectedAisles);

            if (partialResult.partialSolution() == null) {
                System.out.println("No feasible solution found for k = " + k);
                continue;
            } // no feasible

            // show selected solution
            // System.out.println("Partial Solution:");
            // System.out.println("Selected orders = " + partialResult.partialSolution().orders());
            // System.out.println("Selected aisles = " + partialResult.partialSolution().aisles());
            System.out.println("Objective value = " + partialResult.objValue());
            double usedCapacity = (1 - totalCapacityLeft(partialResult.partialSolution())) * 100.0;
            System.out.println(String.format("Total capacity used = %.2f%%", usedCapacity));
            
            // update best solution
            if (waveSize >= waveSizeLB && partialResult.objValue() > bestSolution.objValue()) {
                bestSolution = partialResult;
            }

        }

        System.out.println("Done iterating over selected aisles");
        System.out.println("Best solution found with value " + bestSolution.objValue());

        return bestSolution;
    }
    
    protected PartialResult solveRandomGreedySelection(PartialResult bestSolution, StopWatch stopWatch, int maxIterations) {
        System.out.println("\n>> solveRandomGreedySelection");

        Random random = new Random(12345);

        for (int i=0; i < maxIterations; i++) {

            System.out.println("\nIteration " + i);

            Set<Integer> selectedAisles = new HashSet<>();
            Set<Integer> remainingAisles = IntStream.range(0, aisles.size()).boxed().collect(Collectors.toSet());

            Set<Integer> selectedOrders = new HashSet<>();
            Set<Integer> remainingOrders = IntStream.range(0, orders.size()).boxed().collect(Collectors.toSet());

            int waveSize = 0;

            // iterate over the number of aisles
            for (int k = 1; k <= aisles.size(); k++) {

                if (getRemainingTime(stopWatch) < 1) {
                    System.out.println("Max runtime reached, stopping iteration over k.");
                    break;
                }
                
                if (waveSize >= waveSizeLB && waveSizeUB/k <= bestSolution.objValue()) {
                    // stopping condition due to optimality
                    System.out.println("Current best solution with value " + bestSolution.objValue() + " is already better than the maximum possible for k >= " + k);
                    break;
                }

                // System.out.println("\nSelecting orders of available items from " + k + " aisles");

                // int aisle = randomAisle(random, remainingAisles);
                int aisle = maxCapacityAisle(remainingAisles);
                if (aisle == -1) {
                    // System.out.println("No aisles found in the list.");
                    break;
                }
                remainingAisles.remove(aisle);
                selectedAisles.add(aisle);

                // if (k < initialAislesNumber) {
                //     continue; // skip iterations until we reach the initial aisles number
                // }
                
                List<Map.Entry<Integer, Integer>> shuffledItems = new ArrayList<>(aisles.get(aisle).entrySet());
                Collections.shuffle(shuffledItems, random);

                // int newOrdersCount = 0;
                for (Map.Entry<Integer, Integer> entry : shuffledItems) { // for item in aisle
                    Item item = items.get(entry.getKey());
                    int capacity = entry.getValue();

                    List<Map.Entry<Integer, Integer>> shuffledOrders = new ArrayList<>(item.orders.entrySet());
                    Collections.shuffle(shuffledOrders, random);

                    for (Map.Entry<Integer, Integer> order : shuffledOrders) { // for order with this item

                        int orderId = order.getKey();
                        int orderQuantity = order.getValue();

                        if (waveSize + orderQuantity > waveSizeUB) {
                            break;
                        }

                        // Check if the order can be fulfilled
                        if (orders.get(orderId).size() > 1 || capacity < orderQuantity) {
                            continue;
                        }
                        
                        if (remainingOrders.contains(orderId)) {
                            selectedOrders.add(orderId);
                            remainingOrders.remove(orderId);
                            capacity -= orderQuantity;
                            waveSize += orderQuantity;
                            // newOrdersCount++;
                        }
                    }
                }
                // System.out.println("New orders count: " + newOrdersCount);

                PartialResult partialResult = generatePartialResult(selectedOrders, selectedAisles);

                if (partialResult.partialSolution() == null) {
                    // System.out.println("No feasible solution found for k = " + k);
                    continue;
                } // no feasible

                // show selected solution
                // System.out.println("Partial Solution:");
                // System.out.println("Selected orders = " + partialResult.partialSolution().orders());
                // System.out.println("Selected aisles = " + partialResult.partialSolution().aisles());
                System.out.println("Objective value = " + partialResult.objValue());
                double usedCapacity = (1 - totalCapacityLeft(partialResult.partialSolution())) * 100.0;
                System.out.println(String.format("Total capacity used = %.2f%%", usedCapacity));
                
                // update best solution
                if (waveSize >= waveSizeLB && partialResult.objValue() > bestSolution.objValue()) {
                    bestSolution = partialResult;
                }

            }
            System.out.println("Current best objective value " + bestSolution.objValue());
        }

        System.out.println("Done iterating over random greedy selection");
        // System.out.println("Best solution found with value " + bestSolution.objValue());

        return bestSolution;
    }

    protected PartialResult solveSuperAisleGreedySelection(PartialResult bestSolution, StopWatch stopWatch, Set<Integer> selectedAisles, Random random) {
        System.out.println("\n>> solveSuperAisleGreedySelection");

        // Implementar el algoritmo greedy para seleccionar órdenes sobre un subconjunto de pasillos

        
        int nAisles = selectedAisles.size();

        System.out.println("\nGreedy selection over super-aisle with k = " + nAisles + " aisles");
        
        // Crear un "super-pasillo" ficticio que combine los nAisles pasillos seleccionados
        // set items stock
        for (Item item : items) {
            item.resetStock();
            for (Map.Entry<Integer, Integer> aisle : item.aisles.entrySet()) { // for order with this item
                if (selectedAisles.contains(aisle.getKey())) {
                    item.addStock(aisle.getValue()); // Add stock from selected aisles
                }
            }
        }

        Set<Integer> selectedOrders = selectOrders(random);

        PartialResult partialResult = generatePartialResult(selectedOrders, selectedAisles);

        if (partialResult.partialSolution() == null) {
            System.out.println("No feasible solution found");
            return bestSolution;
        } // no feasible

        
        System.out.println("Objective value = " + partialResult.objValue());
        // int capacity = totalCapacity(partialResult.partialSolution().aisles());
        // int demand = totalDemand(partialResult.partialSolution().orders());
        // System.out.println("Total capacity = " + capacity);
        // System.out.println("Total demand = " + demand);
        // System.out.println("Total capacity left = " + (capacity - demand) + " = " + (((double) (capacity - demand)/capacity)*100)+ "%");
        double usedCapacity = (1 - totalCapacityLeft(partialResult.partialSolution())) * 100.0;
        System.out.println(String.format("Total capacity used = %.2f%%", usedCapacity));


        // update best solution
        if (partialResult.objValue() > bestSolution.objValue()) {
            bestSolution = partialResult;
        }

        // System.out.println("Best solution found with value " + bestSolution.objValue());

        return bestSolution;
    }
    protected PartialResult solveSuperAisleGreedySelection(PartialResult bestSolution, StopWatch stopWatch, int nAisles) {
        Set<Integer> selectedAisles = getBestAislesSubset(nAisles); // por defecto se elijen los pasillos con mas capacidad
        return solveSuperAisleGreedySelection(bestSolution, stopWatch, selectedAisles, new Random(12)); // semilla por defecto
    }
    protected PartialResult solveSuperAisleGreedySelection(PartialResult bestSolution, StopWatch stopWatch, Set<Integer> selectedAisles) {
        return solveSuperAisleGreedySelection(bestSolution, stopWatch, selectedAisles, new Random(12)); // semilla por defecto
    }

    protected PartialResult solveWithPreSelection(PartialResult bestSolution, StopWatch stopWatch) {
        System.out.println("\n>> solveWithPreSelection");

        Set<Integer> selectedAisles = new HashSet<>();
        Set<Integer> selectedOrders = new HashSet<>();
        if (bestSolution.partialSolution() == null) {
            System.out.println("No previous solution found, stopping.");
            return bestSolution;
        } else {
            for (int aisle : bestSolution.partialSolution().aisles()) {
                selectedAisles.add(aisle);
            }
            for (int order : bestSolution.partialSolution().orders()) {
                selectedOrders.add(order);
            }
        }
        
        System.out.println("Remaining time: " + getRemainingTime(stopWatch) + " seconds");

        // solve
        System.out.println("\nMaximizing picked items for number of aisles k = " + selectedAisles.size());
        PartialResult partialResult = solving.problem2c(selectedAisles, selectedOrders, getRemainingTime(stopWatch));
        // PartialResult partialResult = solving.problem1aCP(k, getRemainingTime(stopWatch));

        // show optimal for k aisles
        // System.out.println("Partial Solution:");
        // System.out.println("Selected orders = " + partialResult.partialSolution().orders());
        // System.out.println("Selected aisles = " + partialResult.partialSolution().aisles());
        System.out.println("Objective value = " + partialResult.objValue());
        double usedCapacity = (1 - totalCapacityLeft(partialResult.partialSolution())) * 100.0;
        System.out.println(String.format("Total capacity used = %.2f%%", usedCapacity));
            
        // update best solution
        if (partialResult.objValue() > bestSolution.objValue()) {
            bestSolution = partialResult;
        }

        // System.out.println("Best solution found with value " + bestSolution.objValue());

        return bestSolution;
    }

    protected PartialResult tabuSearch(PartialResult bestSolution, StopWatch stopWatch, int k, int p, int n, int maxIterations) {
        System.out.println("\n>> Tabu Search");
        System.out.println("Conjuntos de " + k + " pasillos");
        System.out.println("Intercambiando " + p + " elementos");
        System.out.println("Tamaño del vecindario: " + n);

        int tabuTenure = 10;

        Set<Integer> universe = IntStream.range(0, aisles.size()).boxed().collect(Collectors.toSet());

        Random rand = new Random(42); // Semilla fija para reproducibilidad

        // Solución inicial: los k mayores elementos
        
        Set<Integer> current = getBestAislesSubset(k);
        PartialResult best = evaluate(current, stopWatch);

        // Lista tabú: pares de intercambios recientes
        Queue<Set<Integer>> tabuList = new LinkedList<>();

        for (int iter = 0; iter < maxIterations; iter++) {
            if (getRemainingTime(stopWatch) < 1) {
                System.out.println("Max runtime reached, stopping iteration over k.");
                break;
            }

            System.out.println("Iteracion " + (iter + 1) + " de " + maxIterations);
            List<PartialResult> neighbors = new ArrayList<>();

            // Generar vecinos por intercambios de p elementos
            List<Integer> inSet = new ArrayList<>(current);
            List<Integer> outSet = new ArrayList<>(universe);
            outSet.removeAll(current);

            PartialResult bestNeighbor = new PartialResult(null,0);

            for (int i = 0; i < n; i++) { // Limitar vecinos por eficiencia
                System.out.println("Generando vecino " + (i + 1) + " de " + n + " (iteracion " + (iter + 1) + ")");
                Set<Integer> newSet = new HashSet<>(current);

                // Seleccionar p elementos a remover y p a agregar
                Collections.shuffle(inSet, rand);
                Collections.shuffle(outSet, rand);
                Set<Integer> removed = new HashSet<>(inSet.subList(0, p));
                Set<Integer> added = new HashSet<>(outSet.subList(0, p));

                newSet.removeAll(removed);
                newSet.addAll(added);

                if (tabuList.contains(added)) continue; // Evitar movimientos tabú

                PartialResult neighbor = evaluate(newSet, stopWatch);
                if (neighbor == null || neighbor.partialSolution() == null) continue; // No factible
                neighbors.add(neighbor);
                double score = neighbor.objValue();

                if (score > bestNeighbor.objValue()) {
                    bestNeighbor = neighbor;
                    // Seleccionar mejor vecino
                }
            }

            if (bestNeighbor == null || bestNeighbor.partialSolution() == null) continue; // No factible
            current = bestNeighbor.partialSolution().aisles();

            // Actualizar mejor global
            if (bestNeighbor.objValue() > best.objValue()) {
                best = bestNeighbor;
            }

            // Actualizar lista tabú
            tabuList.add(new HashSet<>(current));
            if (tabuList.size() > tabuTenure) tabuList.poll();
        }

        System.out.println("Best solution found with value " + bestSolution.objValue());
        if (best.objValue() > bestSolution.objValue()) {
            bestSolution = best;
        }

        return bestSolution;

    }
    protected PartialResult evaluate(Set<Integer> subset, StopWatch stopWatch) {
        // Evaluate the objective value of the subset
        // return solveWithAisleSubset(new PartialResult(null, 0), stopWatch, subset);
        return solveSuperAisleGreedySelection(new PartialResult(null, 0), stopWatch, subset); // greedy

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

    public int randomAisle(Random random, Set<Integer> aislesList) {
        if (aislesList.isEmpty()) {
            return -1;
        }
        int index = random.nextInt(aislesList.size());
        Integer[] array = aislesList.toArray(new Integer[0]);
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
        // System.out.println("Max aisle: " + maxAisle + " with capacity " + max);
        return maxAisle;
    }

    public Set<Integer> getBestAislesSubset(int nAisles) {
        
        Set<Integer> selectedAisles = new HashSet<>();
        Set<Integer> remainingAisles = IntStream.range(0, aisles.size()).boxed().collect(Collectors.toSet());

        // select aisles subset
        for (int k = 1; k <= nAisles; k++) {

            int aisle = maxCapacityAisle(remainingAisles);
            if (aisle == -1) {
                System.out.println("No aisles found in the list.");
                break;
            }
            remainingAisles.remove(aisle);
            selectedAisles.add(aisle);
            
        }

        return selectedAisles;
    }

    public Set<Integer> getRandomAislesSubset(int nAisles, Set<Integer> aislesList, Random random) {
        
        Set<Integer> selectedAisles = new HashSet<>();
        Set<Integer> remainingAisles = new HashSet<>(aislesList);

        // select aisles subset
        while (selectedAisles.size() < nAisles && !remainingAisles.isEmpty()) {
            int aisle = randomAisle(random, remainingAisles);
            if (aisle != -1) {
                selectedAisles.add(aisle);
                remainingAisles.remove(aisle);
            }
        }

        return selectedAisles;
    }
    public Set<Integer> getRandomAislesSubset(int nAisles, Random random) {
        return getRandomAislesSubset(nAisles, IntStream.range(0, aisles.size()).boxed().collect(Collectors.toSet()), random);
        // todos los pasillos por defecto
    }


    

    public Set<Integer> selectOrders(Random random) {

        Set<Integer> selectedOrders = new HashSet<>();

        // recorrer items para seleccionar ordenes
        int waveSize = 0;

        List<Item> shuffledItems = new ArrayList<>(items);
        Collections.shuffle(shuffledItems, random);
        for (Item item : shuffledItems) { // for item in aisle

            List<Map.Entry<Integer, Integer>> shuffledOrders = new ArrayList<>(item.orders.entrySet());
            Collections.shuffle(shuffledOrders, random);
            for (Map.Entry<Integer, Integer> order : shuffledOrders) { // for order with this item

                int orderId = order.getKey();
                int orderDemand = 0;

                boolean enoughStock = true;
                // Check if the order can be fulfilled
                if (item.stock < order.getValue()) { // check only "item"
                    enoughStock = false;
                }
                for (Map.Entry<Integer, Integer> entry : orders.get(orderId).entrySet()) { // check all items
                    Item itemNeeded = items.get(entry.getKey());
                    int itemQuantity = entry.getValue();
                    orderDemand += itemQuantity;

                    if (itemNeeded.stock < itemQuantity) {
                        enoughStock = false; // Not enough stock for item found
                        break;
                    }
                }
                if (!enoughStock || waveSize + orderDemand > waveSizeUB) { // do not exceed upper bound
                    continue;
                }

                selectedOrders.add(orderId);

                // update stock
                for (Map.Entry<Integer, Integer> entry : orders.get(orderId).entrySet()) { // for item in order
                    Item itemNeeded = items.get(entry.getKey());
                    int itemQuantity = entry.getValue();
                    
                    itemNeeded.removeStock(itemQuantity);
                }

                waveSize += orderDemand;
            }
        }

        return selectedOrders;
    }

    public int totalCapacity(Set<Integer> aislesList) {

        int totalCapacity = 0;

        for (int aisle : aislesList) {
            for (Map.Entry<Integer, Integer> entry : aisles.get(aisle).entrySet()) {
                totalCapacity += entry.getValue();
            }
        }

        return totalCapacity;
    }

    public int totalDemand(Set<Integer> ordersList) {

        int totalDemand = 0;

        for (int order : ordersList) {
            for (Map.Entry<Integer, Integer> entry : orders.get(order).entrySet()) {
                totalDemand += entry.getValue();
            }
        }

        return totalDemand;
    }

    public double totalCapacityLeft(ChallengeSolution partialSolution) { // between 0 and 1
        if (partialSolution == null) {
            return 0;
        }

        int demand = totalDemand(partialSolution.orders());
        int capacity = totalCapacity(partialSolution.aisles());

        if (capacity == 0) {
            return 0;
        }
        return (capacity-demand)/(double)capacity;
    }

    public double[] calculateMeanAisleCapacity(Set<Integer> aislesList) {

        double meanSize = 0;
        double meanItems = 0;
        int aisleCapacity = 0;
        int count = 0;

        for (int aisle : aislesList) { // each aisle
            aisleCapacity = 0;
            count = 0;
            for (Map.Entry<Integer, Integer> entry : aisles.get(aisle).entrySet()) {
                aisleCapacity += entry.getValue(); // each item
                count++;
            }
            meanSize += aisleCapacity;
            meanItems += count;
        }

        return new double[] {meanSize / (double) aisles.size(), meanItems / (double) aisles.size()};
    }

    public double[] calculateMeanOrderSize(Set<Integer> ordersList) {

        double meanSize = 0;
        double meanItems = 0;
        int orderSize = 0;
        int count = 0;

        for (int order : ordersList) { // each order
            orderSize = 0;
            count = 0;
            for (Map.Entry<Integer, Integer> entry : orders.get(order).entrySet()) {
                orderSize += entry.getValue(); // each item
                count++;
            }
            meanSize += orderSize;
            meanItems += count;
        }

        return new double[] {meanSize / (double) orders.size(), meanItems / (double) orders.size()};
    }


    protected PartialResult generatePartialResult(Set<Integer> selectedOrders, Set<Integer> selectedAisles) {
        // Implement the logic to calculate the partial result based on selected orders and aisles

        Set<Integer> finalAisles = new HashSet<>();
        for (Integer a : selectedAisles) {
            finalAisles.add(a);
        }
        Set<Integer> finalOrders = new HashSet<>();
        for (Integer o : selectedOrders) {
            finalOrders.add(o);
        }

        ChallengeSolution challengeSolution = new ChallengeSolution(finalOrders, finalAisles);

        if (!isSolutionFeasible(challengeSolution)) {
            return new PartialResult(null, 0);
        }

        return new PartialResult(challengeSolution, computeObjectiveFunction(challengeSolution));
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
