import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.ArrayList;

public class Main {
    static int labyrinthSize = 0;
    static char[][] labyrinth;

    public static void main(String[] args) throws FileNotFoundException{
        labyrinth = readLabyrinth();
        ArrayList<Point> path = generateRandomPath();
        for(int i = 0; i < path.size(); i++){
            System.out.println(path.get(i));
        }
        System.out.println("Walls: " + wallsCount(path));
    }

    public static ArrayList<Point> generateRandomPath(){
        ArrayList<Point> randomPath = new ArrayList<Point>();
        int sizeLimit = (labyrinthSize*labyrinthSize) - 1;
        randomPath.add(new Point(0,0));
        while(!isGoal(randomPath.get(randomPath.size()-1)) && randomPath.size() != sizeLimit ) {
            /*o while continua enquanto o objetivo nao foi alcançado e enquanto o array 
            ainda nao atingiu o limite maximo. Assim que um dos 2 acontecer, sai do while.
            */
            randomPath.add(generateNextRandomPoint(randomPath.get(randomPath.size()-1)));
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

    public static char[][] readLabyrinth() throws FileNotFoundException{
        File file = new File("labyrinth.txt");
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

    public static void printLabyrinth(char[][] labyrinth){
        for(int i=0; i < labyrinth.length; i++){
            for(int j = 0; j < labyrinth[i].length; j++) {
                System.out.print(labyrinth[i][j] + " ");
            }
            System.out.println("");
        }
    }
}