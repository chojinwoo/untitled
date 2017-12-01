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
    static int loopCnt = 0;
    static JSONArray useCurrency = null;


    static Runnable btc = () -> {
        String threadName = Thread.currentThread().getName();
        Call call = new Call();
        Decide decide = new Decide();
        JSONObject config = call.getConfig("BTC");
        config.put("loopCnt", loopCnt);
        config.put("useCurrency", useCurrency);
        decide.pattern1(config, priceMap);
    };

    static Runnable eth = () -> {
        String threadName = Thread.currentThread().getName();
        Call call = new Call();
        Decide decide = new Decide();
        JSONObject config = call.getConfig("ETH");
        config.put("loopCnt", loopCnt);
        config.put("useCurrency", useCurrency);
        decide.pattern1(config, priceMap);
    };

    static Runnable bch = () -> {
        String threadName = Thread.currentThread().getName();
        Call call = new Call();
        Decide decide = new Decide();
        JSONObject config = call.getConfig("BCH");
        config.put("loopCnt", loopCnt);
        config.put("useCurrency", useCurrency);
        decide.pattern1(config, priceMap);
    };

    static Runnable xrp = () -> {
        String threadName = Thread.currentThread().getName();
        Call call = new Call();
        Decide decide = new Decide();
        JSONObject config = call.getConfig("XRP");
        config.put("loopCnt", loopCnt);
        config.put("useCurrency", useCurrency);
        decide.pattern1(config, priceMap);
    };



    public static void main(String args[]) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
        try {
//            File file = new File("logs/log_"+sdf.format(new Date())+".txt");
//            PrintStream printStream = new PrintStream(new FileOutputStream(file));
//            PrintStream sysout = System.out;
//            System.setOut(printStream);

            /*설정값 정의*/

            priceMap.put("ETH_max_per", 1.5);
            priceMap.put("ETH_min_per", 1.5);
            priceMap.put("BTC_max_per", 0.5);
            priceMap.put("BTC_min_per", 0.5);
            priceMap.put("BCH_max_per", 1.5);
            priceMap.put("BCH_min_per", 1.5);
            priceMap.put("XRP_max_per", 1.5);
            priceMap.put("XRP_min_per", 1.5);
            useCurrency = new JSONArray();
            useCurrency.put("BTC");
//            useCurrency.put("ETH");
//            useCurrency.put("BCH");
//            useCurrency.put("XRP");

            /*////*/
            while(true) {
                List<Thread> thread = new ArrayList();
                thread.add(new Thread(btc));
//                thread.add(new Thread(eth));
//                thread.add(new Thread(bch));
//                thread.add(new Thread(xrp));
                loopCnt = thread.size();
                for(int i=0; i<thread.size(); i++) {
                    thread.get(i).start();
                }

                Thread.sleep( 1000);
            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}

