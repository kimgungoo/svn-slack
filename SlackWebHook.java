import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.StringTokenizer;

public class SlackWebHook {
  
  private static String orgName = "your orgName";
  private static String token = "your token";
      
  public static void main(String[] args) throws UnsupportedEncodingException {
    String author = args[0];
    String log = args[1];
    String revision = args[2];
    String changes = args[3];
      
    String title = "New commit by " + author;
    String subTitle = "Revision #" + revision +" (" + changes + " changed)";
    String comment = encodeLog(log);
        
    StringBuilder sb = new StringBuilder();
    sb.append("{\"text\": \"" + title + "\",");
    sb.append("\"username\": \"svn\",");
    sb.append("\"attachments\": [{");
    sb.append("  \"fallback\": \"" + title + "\",");
    sb.append("  \"color\": \"#7CD197\",");
    sb.append("  \"fields\": [{\"title\": \"" + subTitle + "\",\"value\": \"" + comment + "\"}]}]}");
        
    String requestUrl = "https://" + orgName + ".slack.com/services/hooks/subversion?token=" + token;
    String param = "payload=" + sb.toString();
            
    //System.out.println(param);
        
    sendHttpRequest(requestUrl, param);
  }
    
    
  private static void sendHttpRequest(String requestUrl, String param) {
    HttpURLConnection connection = null;
    
    try {
      // Create connection
      URL url = new URL(requestUrl);
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setConnectTimeout(5000);
      connection.setUseCaches(false);
      connection.setDoInput(true);
      connection.setDoOutput(true);

      DataOutputStream wr = new DataOutputStream(
          connection.getOutputStream());
      wr.writeBytes(param);
      wr.flush();
      wr.close();

      InputStream is = connection.getInputStream();
      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
      String line;
      StringBuffer response = new StringBuffer();
      while ((line = rd.readLine()) != null) {
        response.append(line);
        response.append("\r");
      }

      //System.out.println(response.toString());
      rd.close();
    } 
    catch (Exception e) {
      e.printStackTrace();
    } 
    finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }
    
  
  // https://api.slack.com/docs/formatting
  private static String encodeLog(String log) throws UnsupportedEncodingException {
    StringBuilder sb = new StringBuilder();
    StringTokenizer st = new StringTokenizer(log, "\r\n");
    
    while (st.hasMoreTokens()) {
      String line = st.nextToken();    
      sb.append(URLEncoder.encode(line, "UTF-8"));
      sb.append("\\n");
    }
    
    return sb.toString();
  }
}
