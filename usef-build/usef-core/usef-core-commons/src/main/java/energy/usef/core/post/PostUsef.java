package energy.usef.core.post;

import java.io.IOException; //drimpac
import java.util.stream.IntStream;
import java.io.BufferedReader; //Drimpac
import java.io.InputStreamReader; //Drimpac
import java.io.OutputStream;
import javax.net.ssl.HttpsURLConnection;//Drimpac
import java.net.URL; //Drimpac
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.SSLContext;

public class PostUsef {

public static void Post(String str,String str2) {
       
javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
new javax.net.ssl.HostnameVerifier(){

    public boolean verify(String hostname,
            javax.net.ssl.SSLSession sslSession) {
        //return hostname.equals("160.40.49.244");
         return hostname.equals("localhost");
    }
});

TrustManager[] trustAllCerts = new TrustManager[]{
    new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }
        public void checkClientTrusted(
            java.security.cert.X509Certificate[] certs, String authType) {
        }
        public void checkServerTrusted(
            java.security.cert.X509Certificate[] certs, String authType) {
        }
    }
};

try {
    SSLContext sc = SSLContext.getInstance("SSL");
    sc.init(null, trustAllCerts, new java.security.SecureRandom());
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
} catch (Exception e) {
}
   
   String url2 = "https://localhost:9000/drimpac-dso/rest/usefEvents";
   
   
      HttpsURLConnection con2 = null;
 try 
       {
            URL myurl2 = new URL(url2);
            con2 = (HttpsURLConnection) myurl2.openConnection();
            con2.setRequestMethod("POST");
            con2.setRequestProperty("Content-Type", "application/json");
            con2.setRequestProperty("Accept", "application/json");
            con2.setDoOutput(true);


String json = "{\"data\":\"" + str +"\",\"congp\":\"" + str2 +"\"}";
            System.out.println(json);
OutputStream os2 = con2.getOutputStream();      
os2.write(json.getBytes("utf-8"));
os2.flush();
os2.close();

 StringBuilder content2;

            try (BufferedReader in2 = new BufferedReader(
                    new InputStreamReader(con2.getInputStream()))) {

                String line2;
                content2 = new StringBuilder();

                while ((line2 = in2.readLine()) != null) {

                    content2.append(line2 + "\n");
                }
            }
            System.out.println(content2.toString()); 
 }
       catch(IOException e2) {
          
        e2.printStackTrace();
  }
       finally {

            con2.disconnect();
        }

   
   
   
    }

}