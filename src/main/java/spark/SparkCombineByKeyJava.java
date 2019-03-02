package spark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.sql.sources.In;
import scala.Tuple2;

public class SparkCombineByKeyJava {

    public static void main(String[] args){

        SparkConf conf = new SparkConf().setMaster("local").setAppName("SparkCombineByKeyJava");

        JavaSparkContext sc = new JavaSparkContext(conf);

        combineByKeyJava(sc);

        combineByKeyJava8(sc);


    }


    public static void combineByKeyJava(JavaSparkContext sc){

        JavaPairRDD<String,Integer> splitData = sc.textFile("./grades").mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                String[] splits = s.split(" ");
                return new Tuple2<>(splits[0],Integer.parseInt(splits[1]));
            }
        });

        splitData.combineByKey(new Function<Integer, Tuple2<Integer, Integer>>() {
            @Override
            public Tuple2<Integer, Integer> call(Integer integer) throws Exception {
                return new Tuple2<>(integer, 1);
            }
        }, new Function2<Tuple2<Integer, Integer>, Integer, Tuple2<Integer, Integer>>() {
            @Override
            public Tuple2<Integer, Integer> call(Tuple2<Integer, Integer> integerIntegerTuple2, Integer integer) throws Exception {
                return new Tuple2<>(integerIntegerTuple2._1 + integer, integerIntegerTuple2._2 + 1);
            }
        }, new Function2<Tuple2<Integer, Integer>, Tuple2<Integer, Integer>, Tuple2<Integer, Integer>>() {
            @Override
            public Tuple2<Integer, Integer> call(Tuple2<Integer, Integer> integerIntegerTuple2, Tuple2<Integer, Integer> integerIntegerTuple22) throws Exception {
                return new Tuple2<>(integerIntegerTuple2._1+integerIntegerTuple22._1,integerIntegerTuple2._2+integerIntegerTuple22._2);
            }
        }).map(new Function<Tuple2<String,Tuple2<Integer,Integer>>, Tuple2<String,Double>>() {
            @Override
            public Tuple2<String,Double> call(Tuple2<String, Tuple2<Integer, Integer>> stringTuple2Tuple2) throws Exception {
                return new Tuple2<>(stringTuple2Tuple2._1,stringTuple2Tuple2._2._1*1.0/stringTuple2Tuple2._2._2);
            }
        }).foreach(new VoidFunction<Tuple2<String, Double>>() {
            @Override
            public void call(Tuple2<String, Double> stringDoubleTuple2) throws Exception {
                System.out.println(stringDoubleTuple2._1+" "+stringDoubleTuple2._2);
            }
        });

    }

    public static void combineByKeyJava8(JavaSparkContext sc){

        JavaPairRDD<String,Integer> splitData = sc.textFile("./grades").mapToPair(line -> {
            String[] splits = line.split(" ");
            return new Tuple2<>(splits[0],Integer.parseInt(splits[1]));
        });

        splitData.combineByKey(
                x->new Tuple2<>(x,1),
                (x,y)->new Tuple2<>(x._1+y,x._2+1),
                (x,y)->new Tuple2<>(x._1+y._1,x._2+y._2)
        ).map(x->new Tuple2(x._1,x._2._1*1.0/x._2._2)).foreach(x->System.out.println(x._1+" "+x._2));

    }
}
