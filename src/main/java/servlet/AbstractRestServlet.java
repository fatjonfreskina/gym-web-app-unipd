package servlet;

import com.google.gson.*;
import constants.Codes;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import resource.Message;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.sql.Date;
import java.sql.Time;


/**
 * Base class for all servlets that deals with REST. It provides some convenient methods to avoid
 * duplicating the code in all REST servlets, such as:
 * <ul>
 *     <li>
 *         Method {@code sendDataResponse()} to send a response with the specified data in JSON format.
 *     </li>
 *     <li>
 *         Method {@code sendErrorResponse()} to send a response with the specified error in JSON format.
 *     </li>
 *     <li>
 *         Method {@code checkAcceptMediaType()} to check if the "accept" header in the HTTP request
 *         is present and it reports accepting JSON format.
 *     </li>
 *     <li>
 *         Method {@code checkContentTypeMediaType()} to check if the "Content-Type" header in the
 *         HTTP request is present and it reports accepting JSON format.
 *     </li>
 *     <li>
 *         Default implementation of methods {@code doDelete()}, {@code doGet()}, {@code doHead()},
 *         {@code doOptions()}, {@code doPost()}, {@code doPut()} and {@code doTrace()},
 *         that returns a METHOD_NOT_ALLOWED error response.
 *     </li>
 * </ul>
 *
 * @author Marco Alessio
 */
public class AbstractRestServlet extends AbstractServlet
{
    protected static final GsonBuilder builder = new GsonBuilder();
    protected Gson GSON /*= new GsonBuilder().setDateFormat("yyyy-MM-dd").setPrettyPrinting().create()*/;
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIME_FORMAT = "HH:mm:ss";

    protected static final String ACCEPT_HEADER = "accept";

    protected static final String UTF8_ENCODING = "utf-8";

    protected static final String JSON_MEDIA_TYPE = "application/json";
    protected static final String ALL_MEDIA_TYPE = "*/*";

    @Override
    public void init(ServletConfig config) throws ServletException {
        builder.registerTypeAdapter(Date.class, new DateDeserializer());
        builder.registerTypeAdapter(Time.class, new TimeDeserializer());
        GSON = builder.create();
        super.init(config);
    }

    private class DateDeserializer implements JsonDeserializer<Date> {
        @Override
        public Date deserialize(JsonElement jsonElement, Type typeOF,
                                JsonDeserializationContext context) throws JsonParseException {
            try {
                return Date.valueOf(jsonElement.getAsString());
            } catch (IllegalArgumentException e) {
                throw new JsonParseException("Unparseable date: \"" + jsonElement.getAsString()
                        + "\". Supported formats: " + DATE_FORMAT);
            }
        }
    }

    private class TimeDeserializer implements JsonDeserializer<Time> {
        @Override
        public Time deserialize(JsonElement jsonElement, Type typeOF,
                                JsonDeserializationContext context) throws JsonParseException {
            try {
                return Time.valueOf(jsonElement.getAsString());
            } catch (IllegalArgumentException e) {
                throw new JsonParseException("Unparseable time: \"" + jsonElement.getAsString()
                        + "\". Supported formats: " + TIME_FORMAT);
            }
        }
    }

    /**
     * Check if the "accept" header in the HTTP request is present and reports accepting JSON format,
     * otherwise send back an appropriate error code.
     * @param req The HTTP request.
     * @return It can return 3 different error codes:
     * <ul>
     *     <li>
     *          {@code Codes.OK} if the header "accept" is present in the HTTP request
     *          and reports accepting JSON format.
     *     </li>
     *     <li>
     *         {@code Codes.ACCEPT_MISSING} if the header "accept" is not present in the HTTP request.
     *     </li>
     *     <li>
     *         {@code Codes.MEDIA_TYPE_NOT_SUPPORTED} if the header "accept" is present in the HTTP request,
     *         but does not reports accepting JSON format.
     *     </li>
     * </ul>
     */
    protected Codes checkAcceptMediaType(HttpServletRequest req)
    {
        final String accept = req.getHeader(ACCEPT_HEADER);

        // Check if the "accept" HTTP header is not found in the request.
        if (accept == null)
            return Codes.ACCEPT_MISSING;

        // Check if the "accept" HTTP header does not report accepting JSON format.
        if ((!accept.contains(JSON_MEDIA_TYPE)) && (!accept.equals(ALL_MEDIA_TYPE)))
            return Codes.MEDIA_TYPE_NOT_SUPPORTED;

        // HTTP header "accept" reports accepting JSON format.
        return Codes.OK;
    }


    /**
     * Check if the "Content-Type" header in the HTTP request is present and reports accepting JSON format,
     * otherwise send back an appropriate error code.
     * @param req The HTTP request.
     * @return It can return 3 different error codes:
     * <ul>
     *     <li>
     *          {@code Codes.OK} if the header "Content-Type" is present in the HTTP request
     *          and reports accepting JSON format.
     *     </li>
     *     <li>
     *         {@code Codes.CONTENTTYPE_MISSING} if the header "Content-Type" is not present in the HTTP request.
     *     </li>
     *     <li>
     *         {@code Codes.MEDIA_TYPE_NOT_SUPPORTED} if the header "Content-Type" is present in the HTTP request,
     *         but does not reports accepting JSON format.
     *     </li>
     * </ul>
     */
    protected Codes checkContentTypeMediaType(HttpServletRequest req)
    {
        final String contentType = req.getContentType();

        // Check if the "Content-Type" HTTP header is not found in the request.
        if (contentType == null)
            return Codes.CONTENTTYPE_MISSING;

        // Check if the "Content-Type" HTTP header does not report accepting JSON format.
        if (!contentType.contains(JSON_MEDIA_TYPE))
            return Codes.MEDIA_TYPE_NOT_SUPPORTED;

        // HTTP header "Content-Type" reports accepting JSON format.
        return Codes.OK;
    }


    /**
     * Send a response with the specified data in JSON format.
     * @param res The HTTP response.
     * @param data The data to send in JSON format.
     * @throws IOException If something happens when writing the response to the output stream.
     */
    protected void sendDataResponse(HttpServletResponse res, Object data) throws IOException
    {
        // Set headers of the response.
        res.setContentType(JSON_MEDIA_TYPE);
        res.setCharacterEncoding(UTF8_ENCODING);

        // Set HTTP error code.
        res.setStatus(HttpServletResponse.SC_OK);

        // Write the output.
        final PrintWriter out = res.getWriter();
        out.print(GSON.toJson(data));

        // Flush the output stream and close it.
        out.flush();
        out.close();
    }


    /**
     * Send a response with the specified error in JSON format.
     * @param res The HTTP response.
     * @param code The error to send in JSON format.
     * @throws IOException If something happens when writing the response to the output stream.
     */
    protected void sendErrorResponse(HttpServletResponse res, Codes code) throws IOException
    {
        // Set headers of the response.
        res.setContentType(JSON_MEDIA_TYPE);
        res.setCharacterEncoding(UTF8_ENCODING);

        // Set HTTP error code.
        res.setStatus(code.getHTTPCode());

        // Write the output.
        final PrintWriter out = res.getWriter();
        out.print(GSON.toJson(new Message(code.getErrorMessage(), true)));

        // Flush the output stream and close it.
        out.flush();
        out.close();
    }


    /**
     * Default implementation, which sends a METHOD_NOT_ALLOWED error back as response.
     * @param req The HTTP request.
     * @param res The HTTP response.
     * @throws ServletException Never thrown.
     * @throws IOException If something happens when writing the response to the output stream.
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        sendErrorResponse(res, Codes.METHOD_NOT_ALLOWED);
    }


    /**
     * Default implementation, which sends a METHOD_NOT_ALLOWED error back as response.
     * @param req The HTTP request.
     * @param res The HTTP response.
     * @throws ServletException Never thrown.
     * @throws IOException If something happens when writing the response to the output stream.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        sendErrorResponse(res, Codes.METHOD_NOT_ALLOWED);
    }


    /**
     * Default implementation, which sends a METHOD_NOT_ALLOWED error back as response.
     * @param req The HTTP request.
     * @param res The HTTP response.
     * @throws ServletException Never thrown.
     * @throws IOException If something happens when writing the response to the output stream.
     */
    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        sendErrorResponse(res, Codes.METHOD_NOT_ALLOWED);
    }


    /**
     * Default implementation, which sends a METHOD_NOT_ALLOWED error back as response.
     * @param req The HTTP request.
     * @param res The HTTP response.
     * @throws ServletException Never thrown.
     * @throws IOException If something happens when writing the response to the output stream.
     */
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        sendErrorResponse(res, Codes.METHOD_NOT_ALLOWED);
    }


    /**
     * Default implementation, which sends a METHOD_NOT_ALLOWED error back as response.
     * @param req The HTTP request.
     * @param res The HTTP response.
     * @throws ServletException Never thrown.
     * @throws IOException If something happens when writing the response to the output stream.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        sendErrorResponse(res, Codes.METHOD_NOT_ALLOWED);
    }


    /**
     * Default implementation, which sends a METHOD_NOT_ALLOWED error back as response.
     * @param req The HTTP request.
     * @param res The HTTP response.
     * @throws ServletException Never thrown.
     * @throws IOException If something happens when writing the response to the output stream.
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        sendErrorResponse(res, Codes.METHOD_NOT_ALLOWED);
    }


    /**
     * Default implementation, which sends a METHOD_NOT_ALLOWED error back as response.
     * @param req The HTTP request.
     * @param res The HTTP response.
     * @throws ServletException Never thrown.
     * @throws IOException If something happens when writing the response to the output stream.
     */
    @Override
    protected void doTrace(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        sendErrorResponse(res, Codes.METHOD_NOT_ALLOWED);
    }
}