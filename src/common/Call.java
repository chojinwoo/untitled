package common;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class Call {
    String result = "";
    String apiKey = "";
    String secretKey = "";
    private Api_Client api = null;

    public Call() {
        api = new Api_Client(apiKey,
                secretKey);
    }

    public JSONObject getResult(String alias, String currency) {



        HashMap<String, String> rgParams = new HashMap<String, String>();
        rgParams.put("order_currency", currency);
        rgParams.put("payment_currency", "KRW");


        try {
            if(alias.equals("UT")) {
                rgParams.put("searchGb", "0");
                rgParams.put("currency", currency);
                result = api.callApi("/info/user_transactions", rgParams);
                System.out.println(result);
            } else if(alias.equals("OR")) {
                result = api.callApi("/info/orders", rgParams);
            } else if(alias.equals("OD_ASK")) {
                rgParams.put("type", "ask");
                result = api.callApi("/info/order_detail", rgParams);
            } else if(alias.equals("OD_BID")) {
                rgParams.put("type", "bid");
                result = api.callApi("/info/order_detail", rgParams);
            } else if(alias.equals("BL")) {
                rgParams.put("currency", currency);
                result = api.callApi("/info/balance", rgParams);
            } else if (alias.equals("OB")) {
                result = api.callApi("/public/orderbook/"+currency, rgParams);
            } else if (alias.equals("ACC")) {
                result = api.callApi("/info/account", rgParams);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject jo = new JSONObject(result);
        return jo;
    }

    public JSONObject getConfig(String currency) {
        JSONObject config = new JSONObject();

        /*user transaction 정의*/

        try {
            JSONObject ut = getResult("UT", currency);
            JSONArray uta = ut.getJSONArray("data");
            HashMap dateMap = new HashMap();
            HashMap searchMap = new HashMap();
            List dateList = new LinkedList();
            for(int i=0; i<uta.length(); i++) {
                JSONObject o = uta.getJSONObject(i);
                String search = o.getString("search");
                if(search.equals("1") || search.equals("2")) {
                    System.out.print(o);
                    dateMap.put(o.getString("transfer_date"),o.getString(currency.toLowerCase()+"1krw"));
                    searchMap.put(o.getString("transfer_date"),search);
                    dateList.add(o.getString("transfer_date"));
                }
            }
            DescendingStr descendingStr = new DescendingStr();
            Collections.sort(dateList, descendingStr);

            config.put("price", dateMap.get(dateList.get(0)));
            config.put("search", searchMap.get(dateList.get(0)));
            /* 최근거래 판매 또는 구매 금액*/

        } catch(IndexOutOfBoundsException e) {
            config.put("price", "0");
        }

        /* orderbook 정의*/
        try {
            JSONObject ob = getResult("OB", currency);
            List askList = new LinkedList();
            HashMap quantity = new HashMap();
            List bidList = new LinkedList();
            ob = ob.getJSONObject("data");
            config.put("currency", ob.getString("order_currency"));
            JSONArray askJA = ob.getJSONArray("asks");
            JSONArray bidJA = ob.getJSONArray("bids");
            /*판매(매도) 설정 asc 사야될 코인*/
            for(int i=0; i<askJA.length(); i++) {
                JSONObject jo = askJA.getJSONObject(i);
                askList.add(jo.getString("price"));
                quantity.put(jo.getString("price"), jo.getString("quantity"));
            }
            DescendingStr descending = new DescendingStr();
            Collections.sort(askList, descending);

            JSONArray putAr = new JSONArray();
            JSONObject pjo = new JSONObject();
            pjo.put("price", askList.get(0));
            pjo.put("quantity", quantity.get(askList.get(0)));
            putAr.put(pjo);
            pjo = new JSONObject();
            pjo.put("price", askList.get(1));
            pjo.put("quantity", quantity.get(askList.get(1)));
            putAr.put(pjo);
            config.put("asks",putAr);

            /*구매(매수) 설정 desc 팔아야될 코인*/
            for(int i=0; i<bidJA.length(); i++) {
                JSONObject jo = bidJA.getJSONObject(i);
                bidList.add(jo.getString("price"));
                quantity.put(jo.getString("price"), jo.getString("quantity"));
            }
            AscendingStr ascendingStr = new AscendingStr();
            Collections.sort(bidList, ascendingStr);
            putAr = new JSONArray();
            pjo = new JSONObject();
            pjo.put("price", bidList.get(0));
            pjo.put("quantity", quantity.get(bidList.get(0)));
            putAr.put(pjo);
            pjo = new JSONObject();
            pjo.put("price", bidList.get(1));
            pjo.put("quantity", quantity.get(bidList.get(1)));
            putAr.put(pjo);
            config.put("bids",putAr);

            /*지갑 정보 정의*/
            JSONObject bl = getResult("BL", currency);
            JSONObject bljo = bl.getJSONObject("data");
            String avacoin = bljo.getString("available_"+currency.toLowerCase()); /*잔여 코인*/
            int avakrw = bljo.getInt("available_krw"); /*잔여 금액*/
            String usecoin = bljo.getString("in_use_"+currency.toLowerCase()); /*사용 코인*/
            int usekrw = bljo.getInt("in_use_krw"); /*사용 금액*/
            config.put("avacoin", avacoin );
            config.put("avakrw", avakrw );
            config.put("usecoin", usecoin );
            config.put("usekrw", usekrw );


        } catch(Exception e) {
            System.out.println(e);
        }

        return config;
    }

    public boolean sell(String currency, String bidPrice, String units) {
        boolean flag = false;
        HashMap<String, String> rgParams = new HashMap<String, String>();
        rgParams.put("order_currency", currency);
        rgParams.put("payment_currency", "KRW");
        rgParams.put("units", units);
        rgParams.put("price", String.valueOf(bidPrice));
        rgParams.put("type", "ask");
        result = api.callApi("/trade/place", rgParams);
        System.out.println("판매 result : " + result);
        JSONObject jo = new JSONObject(result);

        if(jo.getString("status").equals("0000")) {
            flag = true;
        }
        return flag;
    }

    public boolean buy(String currency, String askPrice, String units) {
        boolean flag = false;
        HashMap<String, String> rgParams = new HashMap<String, String>();
        rgParams.put("order_currency", currency);
        rgParams.put("payment_currency", "KRW");
        rgParams.put("units", units);
        rgParams.put("price", String.valueOf(askPrice));
        rgParams.put("type", "bid");
        result = api.callApi("/trade/place", rgParams);
        JSONObject jo = new JSONObject(result);
        System.out.println("구매 result : " + result);
        if(jo.getString("status").equals("0000")) {
            flag = true;
        }
        return flag;
    }

    public int getAvaCnt(JSONObject config) {
        int avaCnt = 1;
        JSONArray useCurrency = config.getJSONArray("useCurrency");
        String currency = config.getString("currency");
        for(int i=0; i<useCurrency.length(); i++) {
            if(!currency.equals(useCurrency.getString(i))) {
                JSONObject ut = getResult("UT", currency);
                JSONArray utJa = ut.getJSONArray("data");
                HashMap utMap = new HashMap();
                List utList = new LinkedList();
                for(int ii=0; ii< utJa.length(); ii++) {
                    JSONObject utJo = utJa.getJSONObject(ii);
                    String utSearch = utJo.getString("search");
                    if(utSearch.equals("1") || utSearch.equals("2")) {
                        utMap.put(utJo.getString("transfer_date"), utJo.getString("search"));
                        utList.add(utJo.getString("transfer_date"));
                    }
                }
                DescendingStr descendingStr = new DescendingStr();
                Collections.sort(utList, descendingStr);
                try {
                    String lastSearch = (String) utMap.get((String) utList.get(0));
                    if (lastSearch.equals("2")) {
                        avaCnt++;
                    }
                } catch(Exception e) {
                    avaCnt++;
                }
            }
        }
        return avaCnt;
    }

    // 내림차순
    class Descending implements Comparator<Integer> {

        @Override
        public int compare(Integer o1, Integer o2) {
            return o2.compareTo(o1);
        }

    }

    // 오름차순
    class Ascending implements Comparator<Integer> {

        @Override
        public int compare(Integer o1, Integer o2) {
            return o1.compareTo(o2);
        }

    }
    // 내림차순
    class DescendingStr implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            return o2.compareTo(o1);
        }

    }

    // 오름차순
    class AscendingStr implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }

    }
}
