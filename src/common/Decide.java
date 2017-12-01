package common;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class Decide {

    public int pattern1(HashMap map, HashMap price) {

        int result = 0;
        System.out.println(map);
        System.out.println(price);
        System.out.println(map.get("currency"));
        if((String) price.get(map.get("currency")+"price") != "0") {
            if (price.get(map.get("currency")+"search").equals("3")) {
                /*구매*/
                if (Integer.parseInt((String) price.get(map.get("currency")+"price")) > (int) map.get("ask1")) {
//                    System.out.println("최근 판매액 보다 낮음");
                    if ((int) map.get("min_money") >= (int) map.get("ask1")) {
                        System.out.println("구매 : 1% 하락");
                        price.put(map.get("currency")+"price", String.valueOf(map.get("ask1")));
                    } else if ((int) map.get("max_money") <= (int) map.get("ask1")) {
                        System.out.println("구매 : 1%상승");
                        System.out.println("BUY 구매 프로세서");
                        result = 1; /* 1 : 구매*/
                        price.put(map.get("currency")+"price", String.valueOf(map.get("bid1")));
                        price.put(map.get("currency")+"search", "2");
                    }
                } else {

                    System.out.println("최근판매금액 보다 높음");
                    if ((int) map.get("max_money") <= (int) map.get("ask1")) {
                        System.out.println("구매 : 1%상승");
                        System.out.println("BUY 구매 프로세서");
                        result = 1; /* 1 : 구매*/
                        price.put(map.get("currency")+"price", String.valueOf(map.get("bid1")));
                        price.put(map.get("currency")+"search", "2");
                    }
                }
            } else if (price.get(map.get("currency")+"search").equals("2")) {
            /*판매*/
                if (Integer.parseInt((String) price.get(map.get("currency")+"price")) < (int) map.get("bid1")) {
                    if ((int) map.get("max_money") <= (int) map.get("bid1")) {
                        System.out.println("판매 : 1% 상승 최근구매액 현 매도 금액으로 재설정");
                        price.put(map.get("currency")+"price", String.valueOf(map.get("bid1")));
                        System.out.println(price);
                    }
                } else if (Integer.parseInt((String) price.get(map.get("currency")+"price")) > (int) map.get("bid1")) {
                    if ((int) map.get("min_money") >= (int) map.get("bid1")) {
                        System.out.println("판매 :1%하락 판매");
                        System.out.println("SELL 판매 프로세스 실행");
                        result = 2; /* 1 : 판매*/
                        price.put(map.get("currency")+"price", String.valueOf(map.get("ask1")));
                        price.put(map.get("currency")+"search", "3");

                    }
                }
            }
            return result;
        } else {
            /*1 : 구매완료, 2 : 판매완료*/
            System.out.println(map.get("currency") + "초기값");
            try {
                if (map.get("search").equals("1")) {
                    price.put(map.get("currency") + "price", String.valueOf(map.get("bid1")));
                    price.put(map.get("currency") + "search", "2");
                } else if (map.get("search").equals("2")) {
                    price.put(map.get("currency") + "price", String.valueOf(map.get("ask1")));
                    price.put(map.get("currency") + "search", "3");
                }
            } catch(NullPointerException e) {
                result = 1;
                price.put(map.get("currency") + "price", String.valueOf(map.get("bid1")));
                price.put(map.get("currency") + "search", "2");
            }
            System.out.println(price);

        }
        return result;
    }
}