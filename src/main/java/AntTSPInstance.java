import scala.Tuple2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by colntrev on 4/8/18.
 */
public class AntTSPInstance implements Serializable{
    private Random rand;
    private int currentIndex = 0;
    private int towns;
    private int ants;

    private double c = 1.0;
    private double alpha = 1.0;
    private double beta = 5.0;
    private double evaporation = 0.5;
    private double Q = 500;
    private double pr = 0.01;
    private double tourLength;
    private double numAntFactor = 0.8;

    private int[] bestTour;
    private double[][] trails = null;
    private double[] probs = null;
    private Ant[] workers;

    public AntTSPInstance(){
        towns = 0;
        ants = 0;
        rand = new Random();
        System.out.println("Woops");
        System.exit(-1);
        //SHOULD NEVER BE CALLED
    }

    public AntTSPInstance(int n){
        towns = n;
        ants = (int)(n * numAntFactor);
        workers = new Ant[ants];
        trails = new double[towns][towns];
        probs = new double[towns];
        rand = new Random();

        for(int i = 0; i < ants; i++){
            workers[i] = new Ant();
        }
    }

    public Tuple2<Double, List<Integer>> solve(double[][] graph) {
        int iteration = 0;
        int maxIterations = 2000;
        clearTrails();

        while(iteration < maxIterations){
              setupAnts();
              moveAnts(graph);
              updateTrails(graph);
              updateBest(graph);
              iteration++;
        }

        Integer[] bt = new Integer[bestTour.length];

        for(int i = 0; i < bt.length;i++){
            bt[i] = bestTour[i];
        }

        return new Tuple2<>(new Double(tourLength), new ArrayList<>(Arrays.asList(bt)));
    }
/* Main Solve Methods */
    private void setupAnts(){
        currentIndex = -1;
        for(int i = 0; i < ants; i++){
            int next = rand.nextInt(towns);
            workers[i].clear();
            workers[i].visitTown(next);
        }
        currentIndex++;
    }

    private void moveAnts(double[][]graph){
        while(currentIndex < towns - 1){
            for(Ant a : workers){
                a.visitTown(selectNextTown(a,graph));
            }
            currentIndex++;
        }
    }

    private void updateTrails(double[][] graph){
        calculateEvaporation();
        for(Ant a : workers){
            double contribution = Q / a.tourLength(graph);
            for(int i = 0; i < towns - 1; i++){
                trails[a.tour[i]][a.tour[i+1]] += contribution;
            }
            trails[a.tour[towns - 1]][a.tour[0]] += contribution;
        }
    }

    private void updateBest(double[][] graph){
        if(bestTour == null){
            bestTour = workers[0].tour.clone();
            tourLength = workers[0].tourLength(graph);
        }
        for(Ant a : workers){
            if(a.tourLength(graph) < tourLength){
                tourLength = a.tourLength(graph);
                bestTour = a.tour.clone();
            }
        }
    }
/* HELPER METHODS */
    private void clearTrails(){
        for(int i = 0; i < towns; i++){
            for(int j = 0; j < towns; j++){
                trails[i][j] = c;
            }
        }
    }
    private void calculateEvaporation(){
        for(int i = 0; i < towns; i++){
            for(int j = 0; j < towns; j++){
                trails[i][j] *= evaporation;
            }
        }
    }

    protected double pow(final double a, final double b) {
        final int x = (int) (Double.doubleToLongBits(a) >> 32);
        final int y = (int) (b * (x - 1072632447) + 1072632447);
        return Double.longBitsToDouble(((long) y) << 32);
    }

    private int selectNextTown(Ant ant, double[][] graph) {
        // sometimes just randomly select
        if (rand.nextDouble() < pr) {
            int t = rand.nextInt(towns - currentIndex); // random town
            int j = -1;
            for (int i = 0; i < towns; i++) {
                if (!ant.visited(i))
                    j++;
                if (j == t)
                    return i;
            }

        }
        // calculate probabilities for each town (stored in probs)
        probTo(ant,graph);
        // randomly select according to probs
        double r = rand.nextDouble();
        double tot = 0;
        for (int i = 0; i < towns; i++) {
            tot += probs[i];
            if (tot >= r)
                return i;
        }
        // if both conditions fail select next town that is not visited
        for(int i = 0; i < towns; i++){
            if(!ant.visited(i)){
                return i;
            }
        }
        throw new RuntimeException("Not supposed to get here.");
    }
    private void probTo(Ant ant,double[][]graph){
        int i = ant.tour[currentIndex];

        double denom = 0.0;
        for (int l = 0; l < towns; l++)
            if (!ant.visited(l))
                denom += pow(trails[i][l], alpha)
                        * pow(1.0 / graph[i][l], beta);


        for (int j = 0; j < towns; j++) {
            if (ant.visited(j)) {
                probs[j] = 0.0;
            } else {
                double numerator = pow(trails[i][j], alpha)
                        * pow(1.0 / graph[i][j], beta);
                probs[j] = numerator / denom;
            }
        }
    }
    private class Ant implements Serializable{
        public int tour[] = new int[towns];

        public boolean visited[] = new boolean[towns];

        public void visitTown(int town) {
            tour[currentIndex + 1] = town;
            visited[town] = true;
        }

        public boolean visited(int i) {
            return visited[i];
        }

        public double tourLength(double[][] graph) {
            double length = graph[tour[towns - 1]][tour[0]];
            for (int i = 0; i < towns - 1; i++) {
                length += graph[tour[i]][tour[i + 1]];
            }
            return length;
        }

        public void clear() {
            for (int i = 0; i < towns; i++)
                visited[i] = false;
        }
    }

}
