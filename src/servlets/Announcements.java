package servlets;

import java.util.Map;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import services.Services;
import exceptions.ClientBoundException;
import exceptions.MalformedRequestException;

public class Announcements extends FratServlet {
  private static final long serialVersionUID = 1L;

  /* (non-Javadoc)
   * @see servlets.FratServlet#post(java.lang.String, java.util.Map, org.json.JSONObject)
   */
  @Override
  protected Object post(String token, Map<String, String> urlParams, JSONObject data) throws ClientBoundException, JSONException {
    String body = data.getString("body");
    
    Date now = new Date();
    Services.getAnnouncementService().create(token, body, now);
    
    return null;
  }

  /* (non-Javadoc)
   * @see servlets.FratServlet#get(java.lang.String, org.json.JSONObject)
   */
  @Override
  protected Object get(String token, Map<String, String> urlParams) throws ClientBoundException {    

    String startStr = urlParams.get("startTime");
    if (startStr == null) throw new MalformedRequestException("Expected url parameter: startTime");
    long start;
    try {
      start = Long.parseLong(startStr);
    } catch (NumberFormatException e) {
      throw new MalformedRequestException("startTime must be an integer");
    }
    
    String endStr = urlParams.get("endTime");
    if (endStr == null) throw new MalformedRequestException("Expected url parameter: endTime");
    long end;
    try {
      end = Long.parseLong(endStr);
    } catch (NumberFormatException e) {
      throw new MalformedRequestException("endTime must be an integer");
    }
    
    return Services.getAnnouncementService().get(token, new Date(start), new Date(end));
  }
}