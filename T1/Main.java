import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.ArrayList;

public class Main {
    static int labyrinthSize = 0;
    static char[][] labyrinth;
    static int populationSize = 3000;//how many chromossomes for each generation
    static ArrayList<ArrayList<Point>> paths = new ArrayList<ArrayList<Point>>();
    static int[] heuristicPointsForPaths = new int[populationSize];
    static int maxPoints = 500;//if a chromossome has this much points we found our answer
    static int maxGenerations = 10000;//how many generations will be created while we don't find the solution
    static int mutationPercentage = 75;//mutationPercentage% chance of mutate a cromossom of new generations
    static boolean printCrossover = false;
    static boolean printElitism = true;
    static boolean printMutations = false;
    static boolean printTournament = false;
    static boolean printGenerationHeuristicAverage = true;
    static long pauseTimeBetweenGenerations = 0;//(in milliseconds) pause to read prints in between each generation.
    static int elitismPointsAtFirstGeneration = 0;
    static int elitismPointsAtLastGeneration = 0;
    public static void main(String[] args) throws FileNotFoundException{
        labyrinth = readLabyrinth();
        runGeneticAlgorithm();
    }

    public static void runGeneticAlgorithm() {
        //create first generation
        paths = generateFirstGeneration();

        //set solution as an empty array (temporarily)
        ArrayList<Point> solution = new ArrayList<Point>();

        //setting the generation to first
        int currentGeneration = 1;

        //while we don't have a solution and didnt reach maxGeneration number, iterate to find a solution.
        while(solution.size() == 0 && currentGeneration <= maxGenerations) {
            System.out.println("At generation: " + currentGeneration);
            fillHeuristicPointsArray();
            int bestPoints = heuristic(elitism());
            if (currentGeneration == 1) {
                elitismPointsAtFirstGeneration = bestPoints;
            }
            else if (currentGeneration == maxGenerations) {
                elitismPointsAtLastGeneration = bestPoints;
            }
            if (printElitism) {
                printPathChar(fromPointsToChars(elitism()));
                System.out.println("Better heuristic: " + bestPoints);
            }
            if (printGenerationHeuristicAverage) {
                System.out.println("Average heuristic for this generation: " + heuristicAverage());
            }
            int finish = solutionPositionInPaths();
            //if finish return is higher than 0 we found a solution and will finish the algorithm printing solution
            if (finish > 0) {
                elitismPointsAtLastGeneration = bestPoints;
                System.out.print("Solution found at position " + finish + ":");
                solution = paths.get(finish);
                break;
            }
            //else we will do the crossover and mutations to generate new generation
            else{
                currentGeneration++;
                ArrayList<ArrayList<Point>> nextGeneration = new ArrayList<ArrayList<Point>>();
                //carrying the best cromossom to next generation
                nextGeneration.add(elitism());

                //crossover to fill the rest of next generation
                while(nextGeneration.size() < populationSize){
                    ArrayList<Point> parent1 = tournament();
                    ArrayList<Point> parent2 = tournament();
                    //add the first half of parent1 + the last half of parent 2 to next generation
                    ArrayList<ArrayList<Point>> crossover = crossover(parent1, parent2);
                    if (printCrossover) {
                        System.out.print("crossover1: ");
                        printPath(crossover.get(0));
                    }
                    if (containsGoal(crossover.get(0))) {
                        nextGeneration.add(filter(crossover.get(0)));
                    }
                    else {
                        nextGeneration.add(crossover.get(0));
                    }
                    if (nextGeneration.size() < populationSize) {
                        //if the last addition didnt completed the maxPopulationSize...
                        //we also add the first half of parent2 + the last half of parent 1 to next generation
                        if (printCrossover) {
                            System.out.print("crossover2: ");
                            printPath(crossover.get(1));
                        }
                        if (containsGoal(crossover.get(1))) {
                            nextGeneration.add(filter(crossover.get(1)));
                        }
                        else {
                            nextGeneration.add(crossover.get(1));
                        }
                    }
                }
                //randomly mutating others
                Random generator = new Random();
                boolean protectedFirst = false;//just to not mutate first (because it is elitism)
                for (ArrayList<Point> path : nextGeneration) {
                    if (protectedFirst) {
                        if (mutationPercentage < generator.nextInt(100)) {
                            path = mutate(path);
                        }
                    }
                    else {
                        protectedFirst = true;
                    }
                }
                //after crossover and mutations we set current generation = nextGeneration
                paths = nextGeneration;
                try {
                    Thread.sleep(pauseTimeBetweenGenerations);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Started with best heuristic of: " + elitismPointsAtFirstGeneration + " and ended with: " + elitismPointsAtLastGeneration + ".  Best points possible = " + maxPoints);
        //printing solution after getting out of the while through the break command.
        printPath(solution);
    }

    public static double heuristicAverage(){
        double sum = 0;
        for (int heuristic : heuristicPointsForPaths) {
            sum += heuristic;
        }
        return sum/heuristicPointsForPaths.length;
    }

    public static ArrayList<Point> filter(ArrayList<Point> path) {
        ArrayList<Point> filteredPath = new ArrayList<Point>();
        boolean didntFindGoal = true;
        int i = 0;
        while(didntFindGoal) {
            filteredPath.add(path.get(i));
            if (isGoal(path.get(i))) {
                didntFindGoal = false;
            }
            i++;
        }

        return filteredPath;
    }

    public static ArrayList<Point> mutate(ArrayList<Point> path) {
        int wallIndex = 0;
        Random generator = new Random();
        ArrayList<Integer> indexesWithWall = new ArrayList<Integer>();

        for (int i = 0; i < path.size(); i++) {
            Point point = path.get(i);
            if ((point.i >= 0 && point.i < labyrinthSize) && (point.j >= 0 && point.j < labyrinthSize)) {
                if (labyrinth[point.i][point.j] == '1') {
                    indexesWithWall.add(i);
                }
            }
        }

        if (indexesWithWall.size() > 0) {
            wallIndex = indexesWithWall.get(generator.nextInt(indexesWithWall.size()));
        }
        //convert the path to instructions (up, down, right, left)
        ArrayList<Character> pathChar = fromPointsToChars(path);

        //change the instruction at that first wall for a valid one
        if (wallIndex > 0) {
            int iBeforeFirstWall = path.get(wallIndex-1).i;
            int jBeforeFirstWall = path.get(wallIndex-1).j;
            int iAtFirstWall = path.get(wallIndex).i;
            int jAtFirstWall = path.get(wallIndex).j;
            pathChar.remove(wallIndex-1);
            if ((iBeforeFirstWall < 0 || iBeforeFirstWall > labyrinthSize-1) || (jBeforeFirstWall < 0 || jBeforeFirstWall > labyrinthSize-1)) {
                //return the path converted to points again
                return fromCharsToPoints(pathChar);
            }
            if (iBeforeFirstWall < labyrinthSize-1) {
                if (labyrinth[iBeforeFirstWall+1][jBeforeFirstWall] == '0' || labyrinth[iBeforeFirstWall+1][jBeforeFirstWall] == 'S') {
                    pathChar.add(wallIndex-1, 'D');
                    if (printMutations) {
                        System.out.println("mutated path from (" + iAtFirstWall + ", " + jAtFirstWall + ") = " 
                        + labyrinth[iAtFirstWall][jAtFirstWall] + "; to (" + (iBeforeFirstWall+1) + ", " + 
                        (jBeforeFirstWall) + ") = " + labyrinth[iBeforeFirstWall+1][jBeforeFirstWall]);
                    }
                    return fromCharsToPoints(pathChar);
                }
            }
            if (iBeforeFirstWall > 0) {
                if (labyrinth[iBeforeFirstWall-1][jBeforeFirstWall] == '0' || labyrinth[iBeforeFirstWall-1][jBeforeFirstWall] == 'S') {
                    pathChar.add(wallIndex-1, 'U');
                    if (printMutations) {
                        System.out.println("mutated path from (" + iAtFirstWall + ", " + jAtFirstWall + ") = " 
                        + labyrinth[iAtFirstWall][jAtFirstWall] + "; to (" + (iBeforeFirstWall-1) + ", " + 
                        (jBeforeFirstWall) + ") = " + labyrinth[iBeforeFirstWall-1][jBeforeFirstWall]);
                    }
                    return fromCharsToPoints(pathChar);
                }
            }
            if (jBeforeFirstWall < labyrinthSize-1) {
                if (labyrinth[iBeforeFirstWall][jBeforeFirstWall+1] == '0' || labyrinth[iBeforeFirstWall][jBeforeFirstWall+1] == 'S') {
                    pathChar.add(wallIndex-1, 'R');
                    if (printMutations) {
                        System.out.println("mutated path from (" + iAtFirstWall + ", " + jAtFirstWall + ") = " 
                        + labyrinth[iAtFirstWall][jAtFirstWall] + "; to (" + (iBeforeFirstWall) + ", " + 
                        (jBeforeFirstWall+1) + ") = " + labyrinth[iBeforeFirstWall][jBeforeFirstWall+1]);
                    }
                    return fromCharsToPoints(pathChar);
                }
            }
            if (jBeforeFirstWall > 0) {
                if (labyrinth[iBeforeFirstWall][jBeforeFirstWall-1] == '0' || labyrinth[iBeforeFirstWall][jBeforeFirstWall-1] == 'S') {
                    pathChar.add(wallIndex-1, 'L');
                    if (printMutations) {
                        System.out.println("mutated path from (" + iAtFirstWall + ", " + jAtFirstWall + ") = " 
                        + labyrinth[iAtFirstWall][jAtFirstWall] + "; to (" + (iBeforeFirstWall) + ", " + 
                        (jBeforeFirstWall-1) + ") = " + labyrinth[iBeforeFirstWall][jBeforeFirstWall-1]);
                    }
                    return fromCharsToPoints(pathChar);
                }
            }
        }

        //return the path converted to points again
        return fromCharsToPoints(pathChar);
    }

    public static ArrayList<ArrayList<Point>> crossover(ArrayList<Point> parent1, ArrayList<Point> parent2){
        ArrayList<ArrayList<Point>> crossedPaths = new ArrayList<ArrayList<Point>>();

        //convert parameters from points to instructions (up, down, left, right)
        ArrayList<Character> parent1Char = fromPointsToChars(parent1);
        ArrayList<Character> parent2Char = fromPointsToChars(parent2);

        ArrayList<Character> crossedPath1 = new ArrayList<Character>();
        ArrayList<Character> crossedPath2 = new ArrayList<Character>();

        int size = 0;
        if (parent1Char.size() < parent2Char.size()) {
            size = parent1Char.size();
        }
        else {
            size = parent2Char.size();
        }
        Random generator = new Random();

        for (int i = 0; i < size; i++) {
            if (generator.nextInt(2) == 0) {
                crossedPath1.add(parent1Char.get(i));
                crossedPath2.add(parent2Char.get(i));
            }
            else {
                crossedPath1.add(parent2Char.get(i));
                crossedPath2.add(parent1Char.get(i));
            }
        }
        crossedPaths.add(fromCharsToPoints(crossedPath1));
        crossedPaths.add(fromCharsToPoints(crossedPath2));
        //return the crossedPath converted from char to points
        return crossedPaths;
    }

    public static ArrayList<Point> tournament() {
        //choose 2 random paths and return the best (based at heuristic) to be dad or mom.
        Random generator = new Random();
        int randomIndex1 = generator.nextInt(paths.size());
        int randomIndex2 = randomIndex1;
        while (randomIndex1 == randomIndex2) {
            randomIndex2 = generator.nextInt(paths.size());
        }
        if (printTournament) {
            System.out.print("First random path: ");
            printPath(paths.get(randomIndex1));
            System.out.println("Heuristic: " + heuristicPointsForPaths[randomIndex1]);

            System.out.print("Second random path: ");
            printPath(paths.get(randomIndex2));
            System.out.println("Heuristic: " + heuristicPointsForPaths[randomIndex2]);
        }
        if (heuristicPointsForPaths[randomIndex1] > heuristicPointsForPaths[randomIndex2]) {
            if (printTournament) {
                System.out.println("The first random path was chosen.");
            }
            return paths.get(randomIndex1);
        }
        if (printTournament) {
            System.out.println("The second random path was chosen.");
        }
        return paths.get(randomIndex2);
    }

    public static ArrayList<Point> elitism() {
        //returns the best cromossom from the current generation
        int higherPoints = heuristicPointsForPaths[0];
        int higherPosition = 0;
        for (int i = 1; i < heuristicPointsForPaths.length; i++) {
            if (heuristicPointsForPaths[i] > higherPoints) {
                higherPoints = heuristicPointsForPaths[i];
                higherPosition = i;
            }
        }
        return paths.get(higherPosition);
    }

    public static int solutionPositionInPaths() {
        for (int i = 0; i < heuristicPointsForPaths.length; i++) {
            if (heuristicPointsForPaths[i] == maxPoints) {
                return i;
            }
        }
        return -1;
    }

    public static void fillHeuristicPointsArray() {
        for (int i = 0; i < paths.size(); i++) {
            heuristicPointsForPaths[i] = heuristic(paths.get(i));
        }
    }

    public static int heuristic(ArrayList<Point> path){
        int points = 0;
        if (containsGoal(path)) {
            points += maxPoints;
        }
        else {
            points += (maxPoints/2) - manhattanDistanceToGoal(path.get(path.size()-1));
        }
        points -= 3 * wallsCount(path);
        points -= 5 * invalidPoints(path);
        points -= 5 * cicles(path);
        return points;
    }

    public static int cicles(ArrayList<Point> path) {
        int cicles = 0;

        for (Point point : path) {
            int ocurrencies = 0;
            for (Point point2 : path) {
                if (point.i == point2.i && point.j == point2.j) {
                    ocurrencies++;
                }
            }

            if (ocurrencies > 1) {
                cicles++;
            }
        }

        return cicles;
    }

    public static int invalidPoints(ArrayList<Point> path) {
        int invalidPoints = 0;
        for (Point p : path) {
            if ((p.i < 0 || p.i > labyrinthSize-1) || (p.j < 0 || p.j > labyrinthSize-1)) {
                invalidPoints++;
            }
        }
        return invalidPoints;
    }

    public static boolean containsGoal(ArrayList<Point> path) {
        for (int i = 0; i < path.size(); i++) {
            Point point = path.get(i);
            if ((point.i >= 0 && point.i < labyrinthSize) && (point.j >= 0 && point.j < labyrinthSize)) {
                if (labyrinth[point.i][point.j] == 'S') {
                    return true;
                }
            }
        }
        return false;
    }

    public static int wallsCount(ArrayList<Point> path) {
        int wallsCount = 0;
        for (int i = 0; i < path.size(); i++) {
            Point point = path.get(i);
            if ((point.i >= 0 && point.i < labyrinthSize) && (point.j >= 0 && point.j < labyrinthSize)) {
                if (labyrinth[point.i][point.j] == '1') {
                    wallsCount++;
                }
            }
        }
        return wallsCount;
    }

    public static char[][] readLabyrinth() throws FileNotFoundException{
        String directoryPath = new File(".").getAbsolutePath();
        String labyrinthFilePath = directoryPath.substring(0, directoryPath.length()-2) + "\\T1\\labyrinth.txt".trim();
        File file = new File(labyrinthFilePath);
        Scanner in = new Scanner(file);
        labyrinthSize = in.nextInt();
        char[][] labyrinth = new char[labyrinthSize][labyrinthSize];
        int lineNumber = 0;
        while(in.hasNext()){
            /* Por algum motivo o lineNumber é atualizado antes de começar a botar na matriz
             * e por isso tenho que usar ele com -1 mesmo que ele seja inicializado em 0... tem algo
             * estranho nesse java e esse in.hasNext().
             */
            String line = in.nextLine();
            
            for(int i = 0; i < line.length(); i += 2) {
                labyrinth[lineNumber-1][i/2] = line.charAt(i);
            }
            lineNumber++;
        }
        in.close();
        return labyrinth;
    }

    public static ArrayList<ArrayList<Point>> generateFirstGeneration(){
        ArrayList<ArrayList<Point>> p = new ArrayList<ArrayList<Point>>();
        while(p.size() < populationSize) {
            ArrayList<Point> path = generateRandomPath();

            if (!pathAlreadyExist(path)) {
                p.add(path);
            }
        }
        return p;
    }

    public static ArrayList<Point> fromCharsToPoints(ArrayList<Character> path) {
        ArrayList<Point> pathPoint = new ArrayList<Point>();
        pathPoint.add(new Point(0, 0));
        for(int k = 0; k < path.size(); k++) {
            int i = pathPoint.get(k).i;
            int j = pathPoint.get(k).j;
            if (path.get(k) == 'D') {
                i++;
            }
            else if (path.get(k) == 'U') {
                i--;
            }
            else if (path.get(k) == 'R') {
                j++;
            }
            else if (path.get(k) == 'L') {
                j--;
            }
            pathPoint.add(new Point(i, j));
        }

        return pathPoint;
    }

    public static ArrayList<Character> fromPointsToChars(ArrayList<Point> path){
        ArrayList<Character> pathChar = new ArrayList<Character>();
        for(int i = 1; i < path.size(); i++){
            Point last = path.get(i-1);
            Point current = path.get(i);
            if (last.i < current.i) pathChar.add('D');//from lower i to higher i means going down.
            else if (last.i > current.i) pathChar.add('U');//from higher i to lower i means going up.
            else if (last.j < current.j) pathChar.add('R');//from lower j to higher j means going right.
            else if (last.j > current.j) pathChar.add('L');//from higher j to lower j means going left.
        }
        return pathChar;
    }

    public static boolean pathAlreadyExist(ArrayList<Point> path) {
        for (ArrayList<Point> path2 : paths) {
            if (path2.size() == path.size()) {
                for (int i = 0; i < path.size(); i++) {
                    if (path.get(i).i != path2.get(i).i || path.get(i).j != path2.get(i).j) {
                        //at least one different points, no more need to check
                        break;
                    }
                    else if (i==path.size()-1 && ((path.get(i).i == path2.get(i).i && path.get(i).j == path2.get(i).j))) {
                        //if it is last index and it stills equal... it is the same path
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean alreadyPassedHere(ArrayList<Point> path, Point point) {
        for (Point point2 : path) {
            if (point.i == point2.i && point.j == point2.j) {
                return true;
            }
        }
        return false;
    }

    public static int pathMaxSize(){
        int maxSize = 0;
        for (int i = 0; i < labyrinth.length; i++) {
            for (int j = 0; j < labyrinth[i].length; j++) {
                if (labyrinth[i][j] != '1') {
                    //if it isn't a wall we can use it, if we can use it maxSize++
                    maxSize++;
                }
            }
        }
        return maxSize;
    }

    public static ArrayList<Point> generateRandomPath(){
        ArrayList<Point> randomPath = new ArrayList<Point>();
        int sizeLimit = pathMaxSize();
        randomPath.add(new Point(0,0));
        int antiStuck = 0;//sometimes it get stuck and needs make a loop to get out of it.
        while(!isGoal(randomPath.get(randomPath.size()-1)) && randomPath.size() != sizeLimit ) {
            /*o while continua enquanto o objetivo nao foi alcançado e enquanto o array 
            ainda nao atingiu o limite maximo. Assim que um dos 2 acontecer, sai do while.
            */
            Point currentPoint = randomPath.get(randomPath.size()-1);
            Point nextPoint = generateNextRandomPoint(currentPoint);
            if (!alreadyPassedHere(randomPath, nextPoint) || antiStuck > 10) {
                randomPath.add(nextPoint);
                antiStuck = 0;
            }
            else {
                antiStuck++;
            }
        }
        return randomPath;
    }
    
    public static Point generateNextRandomPoint(Point lastRandomPoint) {
        Random generator = new Random();
        int nextI = lastRandomPoint.i;
        int nextJ = lastRandomPoint.j;
        if (generator.nextInt(2) == 0) {
            //50% chance to go left or right
            if (lastRandomPoint.j == 0) {
                //can only go right, otherwise will go out of labyrinth
                nextJ++;
            }
            else if (lastRandomPoint.j == labyrinthSize-1) {
                //can only go left, otherwise will go out of labyrinth
                nextJ--;
            }
            else {
                if (generator.nextInt(2) == 0) {
                    //50% chance to go right
                    nextJ++;
                }
                else {
                    //50% chance to go left
                    nextJ--;
                }
            }
        }
        else {
            //50% chance to go up or down
            if (lastRandomPoint.i == 0) {
                //can only go down, otherwise will go out of labyrinth
                nextI++;
            }
            else if (lastRandomPoint.i == labyrinthSize-1) {
                //can only go up, otherwise will go out of labyrinth
                nextI--;
            }
            else {
                if (generator.nextInt(2) == 0) {
                    //50% chance to go down
                    nextI++;
                }
                else {
                    //50% chance to go up
                    nextI--;
                }
            }
        }
        
        return new Point(nextI, nextJ);
    }

    public static boolean isGoal(Point point) {
        return point.i == labyrinthSize-1 && point.j == labyrinthSize-1;
    }

    public static int manhattanDistanceToGoal(Point point){
        return (labyrinthSize-1-point.i) + (labyrinthSize-1-point.j);
    }

    public static void printLabyrinth(char[][] labyrinth){
        for(int i=0; i < labyrinth.length; i++){
            for(int j = 0; j < labyrinth[i].length; j++) {
                System.out.print(labyrinth[i][j] + " ");
            }
            System.out.println("");
        }
    }

    public static void printPath(ArrayList<Point> path) {
        for (Point point : path) {
            System.out.print(point + "  ");
        }
        System.out.println("");
    }

    public static void printPathChar(ArrayList<Character> path) {
        for (Character character: path) {
            System.out.print(character + " ");
        }
        System.out.println("");
    }

    public static void printPath(ArrayList<Point> path, int index) {
        for (Point point : path) {
            System.out.print(point + "  ");
        }
        System.out.println(" Heuristic: " + heuristicPointsForPaths[index]);
    }
}