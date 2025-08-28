package big.engine.math.util;

public class AvgCounter {
    private static double nullSpeed=-114514;
    private int recordTimes;
    private double[] records;
    public AvgCounter(int i){
        recordTimes=i;
        records=Util.createDoubles(nullSpeed,recordTimes);
    }
    public AvgCounter(){
        this(20);
    }
    public void add(double d){
        for(int i=0;i<recordTimes-1;i++){
            records[i]=records[i+1];
        }
        records[recordTimes-1]=d;
    }
    public double getAvg(){
        double sum=0;
        int count=0;
        for(int i=0;i<recordTimes;i++){
            if(records[i]!=nullSpeed) {
                sum += records[i];
                count++;
            }
        }
        return sum/count;
    }
}
