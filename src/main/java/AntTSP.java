import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by colntrev on 4/8/18.
 */
public class AntTSP {
    private static double[][]graph = null;
    private static List<AntTSPInstance> instances = null;

    public static void main(String[] args){
        SparkConf conf = new SparkConf().setMaster("local").setAppName("Ant TSP");
        JavaSparkContext context = new JavaSparkContext(conf);
        long startTime;
        long endTime;
        int numInstances = args.length == 2? Integer.parseInt(args[1]) : 30;

        readGraph(context, "/Users/ColnTrev1/IdeaProjects/AntTSP/src/main/java/tspadata1.txt");
        init(numInstances);

        JavaRDD<AntTSPInstance> antTsp = context.parallelize(instances);
        startTime = System.currentTimeMillis();
        JavaRDD<Tuple2<Double, List<Integer>>> bestTours = antTsp.map(ant -> ant.solve(graph));

        List<Tuple2<Double, List<Integer>>> results = bestTours.collect();

        findBest(results);
        endTime = System.currentTimeMillis();
        context.close();
        System.out.println("Elapsed Time: " + (endTime - startTime));
    }

    public static void readGraph(JavaSparkContext context, String path){
        JavaRDD<List<String>> splitLines = context.textFile(path).map(s ->{
            String[] split = s.split(" ");
            List<String> results = new ArrayList<>();
            for(String str : split){
                if(!str.isEmpty()){
                    results.add(str);
                }
            }
            return results;
        });
        graphFromList(splitLines.collect());
    }

    public static void graphFromList(List<List<String>> graphList) {
        int i = 0;

        for(List<String> split : graphList){
            if(graph == null){
                graph = new double[split.size()][split.size()];
            }

            int j = 0;
            for(String s : split){
                if(!s.isEmpty()) {
                    graph[i][j++] = Double.parseDouble(s) + 1;
                }
            }
            i++;
        }
    }

    public static void init(int numInstances){
        int n = graph.length;
        instances = new ArrayList<>();
        for(int i = 0; i < numInstances; i++){
            instances.add(new AntTSPInstance(n));
        }
    }

    public static void findBest(List<Tuple2<Double,List<Integer>>> results){
        Double best = -1.0;
        List<Integer> tour = null;
        for(Tuple2<Double,List<Integer>> res : results){
            if(tour == null){
                best = res._1();
                tour = res._2();
            } else {
                if(res._1() < best){
                    best = res._1();
                    tour = res._2();
                }
            }

        }

        //System.out.println("Best Tour: " + tour + " " + "Length: " + best.toString());
    }
}
