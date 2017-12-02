package common;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

public class Decide {
    Call call = new Call();

    /* 구매 패턴 1*/
    public void pattern1(JSONObject config, HashMap price) {

        String myKrw = String.valueOf(config.getInt("avakrw"));
        String avaCoin = config.getString("avacoin");
        String currency = config.getString("currency");
        String currency_price = (String) price.get(currency+"price");
        String currency_search = (String) price.get(currency+"search");
        String min_money = Util.getMinMoney(currency_price, (Double) price.get(currency+"_min_per"));
        String max_money = Util.getMaxMoney(currency_price, (Double) price.get(currency+"_max_per"));
        System.out.println(currency + " start");
        System.out.println(config);
        System.out.println("default price : " +price.get(currency+"price"));
        System.out.println("min : " + min_money + " max : " + max_money);
        /*order book 내역*/
        JSONArray asksJA = config.getJSONArray("asks");
        JSONArray bidsJA = config.getJSONArray("bids");

        String ask_price1 = asksJA.getJSONObject(0).getString("price");
        String ask_price2 = asksJA.getJSONObject(1).getString("price");

        String bid_price1 = bidsJA.getJSONObject(0).getString("price");
        String bid_price2 = bidsJA.getJSONObject(1).getString("price");

        if(currency_search == null || currency_search.equals("0")) {
            String search = config.getString("search");
            price.put(currency + "price", ask_price1);
            price.put(currency+"search", "2");
            price.put(currency+"cnt", "0");
            if(search.equals("2")) {
                price.put(currency+"search", "1");
                price.put(currency + "price", bid_price1);
            }

        } else {
            currency_search = (String) price.get(currency+"search");
            if (currency_search.equals("1")) {
                /*구매*/
                System.out.println("ask1 : " + ask_price1 + " ask2 : " + ask_price2);
                if(Integer.parseInt(currency_price) > Integer.parseInt(ask_price2)) {
                    System.out.println("구매 : 판매액 보다 낮음");
                    if(Integer.parseInt(min_money) > Integer.parseInt(ask_price2)) {
                        System.out.println("1% 하락");
                        price.put(currency+"price", ask_price1);
                        price.put(currency+"cnt", String.valueOf(Integer.parseInt((String)price.get(currency+"cnt")) + 1));
                    }
                } else {
                    System.out.println("구매 : 판매액 보다 높음");
                    price.put(currency+"cnt", String.valueOf(Integer.parseInt((String)price.get(currency+"cnt")) - 1));
                    if(Integer.parseInt(max_money) < Integer.parseInt(ask_price1)) {
                        if(Integer.parseInt((String)price.get(currency+"cnt")) < 1) {
                            System.out.println("구매 : 1%상승");
                            System.out.println("SUCCESS 구매 프로세서");
                            int avaCnt = call.getBuyCurrencyCnt(config); /* 구매할 비트코인종류의 카운트 */
                            String krw = String.valueOf(Integer.parseInt(myKrw) / avaCnt);
                            int flag = call.buy(currency, ask_price1, Util.krwToUnits(currency, krw, ask_price1));

                            System.out.println(config);
                            System.out.println("구매 결과값 : " + flag);
                            switch (flag) {
                                case 1 :
                                    price.put(currency + "price", bid_price1);
                                    price.put(currency + "search", "2");
                                    break;
                                case 2:
                                    System.out.println("구매 실패");
                                    int subFlag = call.buy(currency, ask_price2, Util.krwToUnits(currency, krw, ask_price2));
                                    switch(subFlag) {
                                        case 1 :
                                            price.put(currency + "price", bid_price1);
                                            price.put(currency + "search", "2");
                                            break;
                                        case 3 :
                                            System.out.println("잔액부족");
                                            break;
                                    }
                                    break;
                                case 3:
                                    System.out.println("잔액부족");
                                    break;
                            }
                        } else {
                            System.out.println("구매 : 1% 상승 하락 카운트 초기화 현재 카운트  : " + price.get(currency + "cnt"));
                            price.put(currency + "cnt", "0");
                        }
                    }
                }
            } else if (currency_search.equals("2")) {
            /*판매*/
                System.out.println("bid1 : " + bid_price1 + " bid2 : " + bid_price2);
                if(Integer.parseInt(currency_price) < Integer.parseInt(bid_price1)) {
                    System.out.println("판매 : 구매 금액보다 높음");
                    if(Integer.parseInt(max_money) < Integer.parseInt(bid_price1)) {
                        System.out.println("1% 상승 최근구매액 현 매도 금액으로 재설정");
                        price.put(currency+"price", bid_price1);
                    }
                } else if(Integer.parseInt(currency_price) > Integer.parseInt(bid_price1)) {
                    System.out.println("판매 : 구매 금액보다 낮음");
                    if (Integer.parseInt(min_money) > Integer.parseInt(bid_price1)) {
                        System.out.println("1%하락 판매");
                        System.out.println("SUCCESS 판매 프로세스 실행");
                        System.out.println(config);
                        int flag = call.sell(currency, bid_price1, Util.getUnits(currency, avaCoin));

                        System.out.println("판매 결과값 : " + flag);
                        switch (flag) {
                            case 1:
                                price.put(currency+"price", ask_price1);
                                price.put(currency+"search", "1");
                                break;
                            case 2:
                                System.out.println("판매 실패");
                                int subFlag = call.sell(currency, bid_price1, Util.getUnits(currency, avaCoin));
                                switch (subFlag) {
                                    case 1:
                                        price.put(currency+"price", ask_price1);
                                        price.put(currency+"search", "1");
                                        break;
                                    case 3:
                                        System.out.println("수량부족");
                                        break;
                                }
                                break;
                            case 3:
                                System.out.println("수량부족");
                                break;
                        }
                    }
                }
            }
        }
    }


    /* 지정가 거래 maker fee*/
    public void pattern2(JSONObject config, HashMap price) {
        String myKrw = String.valueOf(config.getInt("avakrw"));
        String avaCoin = config.getString("avacoin");
        String currency = config.getString("currency");
        String currency_price = (String) price.get(currency+"price");
        String currency_search = (String) price.get(currency+"search");
        String min_money = Util.getMinMoney(currency_price, (Double) price.get(currency+"_min_per"));
        String max_money = Util.getMaxMoney(currency_price, (Double) price.get(currency+"_max_per"));
        System.out.println(currency + " start");
        System.out.println(config);
        System.out.println("default price : " +price.get(currency+"price"));
        System.out.println("min : " + min_money + " max : " + max_money);
        /*order book 내역*/
        JSONArray asksJA = config.getJSONArray("asks");
        JSONArray bidsJA = config.getJSONArray("bids");

        String ask_price1 = asksJA.getJSONObject(0).getString("price");
        String ask_price2 = asksJA.getJSONObject(1).getString("price");
        String ask_price3 = asksJA.getJSONObject(2).getString("price");
        String ask_price4 = asksJA.getJSONObject(3).getString("price");
        String ask_price5 = asksJA.getJSONObject(4).getString("price");

        String bid_price1 = bidsJA.getJSONObject(0).getString("price");
        String bid_price2 = bidsJA.getJSONObject(1).getString("price");
        String bid_price3 = bidsJA.getJSONObject(2).getString("price");
        String bid_price4 = bidsJA.getJSONObject(3).getString("price");
        String bid_price5 = bidsJA.getJSONObject(4).getString("price");

        if(currency_search == null || currency_search.equals("0")) {
            String search = "";
            price.put(currency+"cnt", "0");
            try {
                search = config.getString("search");
                if(search.equals("2")) {
                    price.put(currency+"search", "1");
                    price.put(currency + "price", ask_price1);
//                    int flag = call.makerSell(currency, bidsJA, Util.getUnits(currency, avaCoin));
                } else if(search.equals("1")) {
                    price.put(currency+"search", "2");
                    price.put(currency + "price", bid_price1);
//                    int flag = call.makerSell(currency, bidsJA, Util.getUnits(currency, avaCoin));
                }
            } catch(Exception e) {
                price.put(currency+"search", "1");
                price.put(currency + "price", ask_price1);
//                int flag = call.makerSell(currency, bidsJA, Util.getUnits(currency, avaCoin));
            }

        } else {
            currency_search = (String) price.get(currency+"search");
            if (currency_search.equals("1")) {
                /*구매*/
                System.out.println("ask1 : " + ask_price1 + " ask2 : " + ask_price2 + " ask3 : " + ask_price3 + " ask4 : " + ask_price4 + " ask5 : " + ask_price5);
                if(Integer.parseInt(currency_price) > Integer.parseInt(ask_price1)) {
                    System.out.println("구매 : 판매액 보다 낮음");
                    if (Integer.parseInt(min_money) > Integer.parseInt(ask_price1)) {
                        System.out.println("1% 하락");
                        price.put(currency + "price", ask_price1);
                        price.put(currency + "cnt", String.valueOf(Integer.parseInt((String) price.get(currency + "cnt")) + 1));
                    } else {
                        System.out.println("구매 : 판매액 보다 높음");
                        price.put(currency + "cnt", String.valueOf(Integer.parseInt((String) price.get(currency + "cnt")) - 1));
                        if (Integer.parseInt(max_money) < Integer.parseInt(ask_price1)) {
                            if (Integer.parseInt((String) price.get(currency + "cnt")) < 1) {
                                System.out.println("구매 : 1%상승");
                                System.out.println("SUCCESS 구매 프로세서");
                                int avaCnt = call.getBuyCurrencyCnt(config); /* 구매할 비트코인종류의 카운트 */
                                String krw = String.valueOf(Integer.parseInt(myKrw) / avaCnt);
                                int flag = call.makerBuy(currency, asksJA, krw);
                                System.out.println("구매 결과값 : " + flag);
                                switch (flag) {
                                    case 1:
                                        price.put(currency + "price", bid_price1);
                                        price.put(currency + "search", "2");
                                        break;
                                    case 2:
                                        System.out.println("구매 실패");
                                        break;
                                    case 3:
                                        System.out.println("수량부족");
                                        break;
                                }
                            } else {
                                System.out.println("구매 : 1% 상승 하락 카운트 초기화 현재 카운트  : " + price.get(currency + "cnt"));
                                price.put(currency + "cnt", "0");
                            }
                        }
                    }
                }
            } else if (currency_search.equals("2")) {
            /*판매*/
                System.out.println("bid1 : " + bid_price1 + " bid2 : " + bid_price2 + " bid3 : " + bid_price3 + " bid4 : " + bid_price4 + " bid5 : " + bid_price5);
                if(Integer.parseInt(currency_price) < Integer.parseInt(bid_price1)) {
                    System.out.println("판매 : 구매 금액보다 높음");
                    if(Integer.parseInt(max_money) < Integer.parseInt(bid_price1)) {
                        System.out.println("1% 상승 최근구매액 현 매도 금액으로 재설정");
                        price.put(currency+"price", bid_price1);
                    }
                } else if(Integer.parseInt(currency_price) > Integer.parseInt(bid_price1)) {
                    System.out.println("판매 : 구매 금액보다 낮음");
                    if (Integer.parseInt(min_money) > Integer.parseInt(bid_price1)) {
                        System.out.println("1%하락 판매");
                        System.out.println("SUCCESS 판매 프로세스 실행");
                        int flag = call.makerSell(currency, bidsJA, Util.getUnits(currency, avaCoin));
                        System.out.println("판매 결과값 : " + flag);
                        switch (flag) {
                            case 1:
                                price.put(currency+"price", ask_price1);
                                price.put(currency+"search", "1");
                                break;
                            case 2:
                                System.out.println("판매 실패");
                                break;
                            case 3:
                                System.out.println("수량부족");
                                break;
                        }

                    }
                }
            }
        }
    }
}