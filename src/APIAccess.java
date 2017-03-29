import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by yulong on 3/28/17.
 */
public class APIAccess {

  public static final String DEV_ID = "1948";
  public static final String AUTH_KEY = "FBDCBC658DDB43D1A3C212FFA2E79FC2";
  public static final String BASE_URL = "http://api.smitegame.com/smiteapi.svc/";
  public static final String XBOX_BASE_URL = "http://api.xbox.smitegame.com/smiteapi.svc/";
  public static final String PS4_BASE_URL = "http://api.ps4.smitegame.com/smiteapi.svc/";

  private static String sessionID = "";
  private static String signature = "";

  public static void main(String[] params) {
    signature = getSignature();
    createSession(signature);
    System.out.println("Signature: " + signature);
    System.out.println("sessionID: " + sessionID);
    getPlayer("HirezPlayer");
  }

  /**
   * API requires the use of an MD5 hash to create a signature, which is required
   * for each API method call.
   *
   * The pattern for creating a session is
   *
   *    base_url/createsessionJson/{devID}/{signature}/{timeStamp}
   *
   * @param input To create the input, we need ther devID, methodName, authKey,
   *              and utc timestamp (yyyyMMddHHmmss)
   *
   * @return hashed signature used for method calls
   *
   *
   */
  public static String getMD5Hash(String input) {
    try {
      java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
      byte[] array = md.digest(input.getBytes(Charset.forName("UTF-8")));
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < array.length; ++i) {
        sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
      }
      return sb.toString();
    } catch (java.security.NoSuchAlgorithmException e) {

    }
    return null;
  }

  /**
   * Creates a session to use Smite's API method calls. Each session lasts 15 minutes
   * Pattern is {methodName}/{devID}/{signature}/{timestamp}
   *
   * @param signature the signature required as a parameter
   *
   */
  public static void createSession(String signature) {
    String sessionLink = BASE_URL + "createsessionJson/" + DEV_ID + "/" + signature +
        "/" + getUtcTimeStamp();
    sessionID = getSessionID(makeGetRequest(sessionLink));
  }

  private static String getSessionID(String result) {
    String[] parts = result.split(",");
    return parts[1].split(":")[1].replace("\"", "");
  }

  // getplayer[ResponseFormat]/{devId}/{signature}/{sessionId}/{timestamp}/{playerName}
  public static void getPlayer(String name) {
    String link = BASE_URL + "getplayerjson/" + DEV_ID + "/" + signature +
        "/" + sessionID + "/" + getUtcTimeStamp() + "/" + name;
    System.out.println(link);
    makeGetRequest(link, "Grabbing Xbox player: " + name);
  }

  private static String getUtcTimeStamp() {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    return dateFormat.format(new Date());
  }

  private static String getSignature() {
    return getMD5Hash(DEV_ID + "createsession" + AUTH_KEY + getUtcTimeStamp());
  }

  public static String makeGetRequest(String link) {
    return makeGetRequest(link, "");
  }

  private static String makeGetRequest(String link, String customMessage) {
    try {
      URL url = new URL(link);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      if (connection.getResponseCode() != 200) {
        throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
      }
      BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String output;
      String result = "";
      System.out.println(customMessage);
      System.out.println("Output from Server .... \n");
      while ((output = br.readLine()) != null) {
        result += output;
        System.out.println(output);
      }
      connection.disconnect();
      return result;
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
