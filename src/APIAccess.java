import com.oracle.javafx.jmx.json.JSONReader;

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

  public static final int LANGUAGE_CODE_ENGLISH = 1;

  private static String sessionID = "";
  private static String signature = "";

  private static String xboxSessionID = "";
  private static String xboxSignature = "";

  private static String PS4SessionID = "";
  private static String PS4Signature = "";

  public static void main(String[] params) {
    signature = getSignature("createsession");
    createSession(signature);

    xboxSignature = getSignature("createsession");
    createXboxSession(xboxSignature);
    // getGods();
    getPlayer("Weak3n");
    getXboxPlayer("Aerovertics");
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
    createSession(signature, BASE_URL);
  }

  public static void createXboxSession(String signature) {
    createSession(signature, XBOX_BASE_URL);
  }

  public static void createPS4Session(String signature) {
    createSession(signature, PS4_BASE_URL);
  }

  private static void createSession(String signature, String platform) {
    String sessionLink = platform + "createsessionJson/" + DEV_ID + "/" + signature +
        "/" + getUtcTimeStamp();
    System.out.println("Session url request: " + sessionLink);
    if (platform.equals(BASE_URL)) {
      sessionID = getSessionID(makeGetRequest(sessionLink));
    } else if (platform.equals(XBOX_BASE_URL)) {
      xboxSessionID = getSessionID(makeGetRequest(sessionLink));
    } else {
      PS4SessionID = getSessionID(makeGetRequest(sessionLink));
    }
  }

  private static String getSessionID(String result) {
    String[] parts = result.split(",");
    return parts[1].split(":")[1].replace("\"", "");
  }

  /**
   * [ResponseFormat]/{developerId}/{signature}/{session}/{timestamp}/{languageCode}
   */
  public static void getGods() {
    signature = getSignature("getgods");
    String link = BASE_URL + "getgodsjson/" + DEV_ID + "/" + signature +
        "/" + sessionID + "/" + getUtcTimeStamp() + "/" + LANGUAGE_CODE_ENGLISH;
    System.out.println("Get gods link: " + link);
    makeGetRequest(link);
  }

  // getplayer[ResponseFormat]/{devId}/{signature}/{sessionId}/{timestamp}/{playerName}
  public static void getPlayer(String name) {
    getPlayer(name, BASE_URL);
  }

  public static void getXboxPlayer(String name) {
    getPlayer(name, XBOX_BASE_URL);
  }

  public static void getPS4Player(String name) {
    getPlayer(name, PS4_BASE_URL);
  }

  private static void getPlayer(String name, String platform) {
    String id = "";
    if (platform.equals(BASE_URL)) {
      id = sessionID;
    } else if (platform.equals(XBOX_BASE_URL)) {
      id = xboxSessionID;
    } else {
      id = PS4SessionID;
    }
    signature = getSignature("getplayer");
    String link = platform + "getplayerjson/" + DEV_ID + "/" + signature +
        "/" + id + "/" + getUtcTimeStamp() + "/" + name;
    System.out.println("Get player link: " + link);
    makeGetRequest(link, "Grabbing player: " + name);
  }

  private static String getUtcTimeStamp() {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    return dateFormat.format(new Date());
  }

  private static String getSignature(String method) {
    return getMD5Hash(DEV_ID + method + AUTH_KEY + getUtcTimeStamp());
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
      StringBuilder response = new StringBuilder();
      String output;
      String result = "";
      if (customMessage != null && customMessage.length() > 0) {
        System.out.println(customMessage);
      }
      System.out.println("Output from Server .... \n");
      while ((output = br.readLine()) != null) {
        response.append(output);
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
