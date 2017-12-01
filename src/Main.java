import common.Call;
import common.Decide;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.*;


public class Main {
    static String last_price = "0";
    static HashMap priceMap = new HashMap();


    static Runnable btc = () -> {
        String threadName = Thread.currentThread().getName();
        Call call = new Call();
        Decide decide = new Decide();
        JSONObject config = call.getConfig("BTC");
        int result = decide.pattern1(config, priceMap);
    };

    static Runnable eth = () -> {
        String threadName = Thread.currentThread().getName();
        Call call = new Call();
        Decide decide = new Decide();
        JSONObject config = call.getConfig("ETH");
        decide.pattern1(config, priceMap);
    };

    static Runnable bch = () -> {
        String threadName = Thread.currentThread().getName();
        Call call = new Call();
        Decide decide = new Decide();
        JSONObject config = call.getConfig("BCH");
        decide.pattern1(config, priceMap);
    };



    public static void main(String args[]) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
        try {
            File file = new File("log_"+sdf.format(new Date())+".txt");
            PrintStream printStream = new PrintStream(new FileOutputStream(file));
            PrintStream sysout = System.out;
            System.setOut(printStream);



            priceMap.put("BTCprice", last_price);/*테스트용*/
            priceMap.put("BTCsearch", "2"); /*테스트용*/
//            priceMap.put("ETHprice", last_price);/*테스트용*/
//            priceMap.put("ETHsearch", "2"); /*테스트용*/
//            priceMap.put("BCHprice", last_price);/*테스트용*/
//            priceMap.put("BCHsearch", "2"); /*테스트용*/
            while(true) {
                Thread thread1 = new Thread(btc);
                thread1.start();
//                Thread thread2 = new Thread(eth);
//                thread2.start();
//                Thread thread3 = new Thread(bch);
//                thread3.start();
                Thread.sleep(2000);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}

