package util.hatena;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class SimilarWord {

    private static final String API_URL = "http://d.hatena.ne.jp/xmlrpc";
    private static final String API_METHOD_NAME = "hatena.getSimilarWord";

    @SuppressWarnings("unchecked")
    public static List<String> get(String[] wordlist) {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        try {
            config.setServerURL(new URL(API_URL));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }

        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);

        Map param = new HashMap();
        param.put("wordlist", wordlist);

        try {
            Object result = client.execute(API_METHOD_NAME, new Object[] {param});
            if (result != null && result instanceof Map) {
                List list = new ArrayList<String>();
                for (Object word : (Object[])((Map) result).get("wordlist")) {
                    list.add(((Map) word).get("word"));
                }
                return list;
            }
        } catch (XmlRpcException e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }

}
