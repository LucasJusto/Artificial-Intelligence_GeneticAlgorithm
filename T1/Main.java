import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.ArrayList;

public class Main {
    static int labyrinthSize = 0;
    static char[][] labyrinth;
    static int populationSize = 100;
    static ArrayList<ArrayList<Point>> paths = new ArrayList<ArrayList<Point>>();

    public static void main(String[] args) throws FileNotFoundException{
        labyrinth = readLabyrinth();
        paths = generateFirstGeneration();
        System.out.println(populationSize);
        System.out.println(paths.size());
        for (ArrayList<Point> path : paths) {
            for (Point point : path) {
                System.out.print(point + "   ");
            }
            System.out.println("");
        }
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
            //System.out.println(paths.size());
            ArrayList<Point> path = generateRandomPath();

            if (!pathAlreadyExist(path)) {
                p.add(path);
            }
        }
        return p;
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

 /*   public static boolean hasLoop(ArrayList<Point> path) {
        for (Point point : path) {
            int appearances = 0;
            for (Point point2 : path) {
                if (point.i == point2.i && point.j == point2.j) appearances++;
            }
            if (appearances > 1) return true;
        }
        return false;
    }*/

    public static boolean alreadyPassedHere(ArrayList<Point> path, Point point) {
        for (Point point2 : path) {
            if (point.i == point2.i && point.j == point2.j) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<Point> generateRandomPath(){
        ArrayList<Point> randomPath = new ArrayList<Point>();
        int sizeLimit = (labyrinthSize*labyrinthSize) - 1;
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

    public static int wallsCount(ArrayList<Point> path) {
        //how many walls are in this path
        int walls = 0;
        for(int i = 0; i < path.size(); i++) {
            if (labyrinth[path.get(i).i][path.get(i).j] == '1') walls++;
        }
        return walls;
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
}