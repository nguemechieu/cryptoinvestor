package cryptoinvestor.cryptoinvestor;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

import static java.lang.System.out;

class News extends RecursiveTreeObject<News> {
    int hours;
    int seconds;
    String url = "https://nfs.faireconomy.media/ff_calendar_thisweek.json?version=1bed8a31256f1525dbb0b6daf6898823";
    private String title;// title of the
    private String country;// country of the news
    private String description;// description of the news
    private String impact;// impact of the news
    private String previous;// previous forecast of the news
    private Date date;// date of the news
    private String forecast;// forecast of the news
    private int minutes;
    private int offset;

    public News(String title, String country, String impact, Date date, String forecast, String previous) {
        this.title = title;
        this.country = country;
        this.impact = impact;
        this.date = date;
        this.forecast = forecast;
        this.previous = previous;

    }

    /**
     * Constructs a new object.
     */
    public News() {
        super();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns a hash code value for the object. This method is
     * supported for the benefit of hash tables such as those provided by
     * {@link HashMap}.
     * <p>
     * The general contract of {@code hashCode} is:
     * <ul>
     * <li>Whenever it is invoked on the same object more than once during
     *     an execution of a Java application, the {@code hashCode} method
     *     must consistently return the same integer, provided no information
     *     used in {@code equals} comparisons on the object is modified.
     *     This integer need not remain consistent from one execution of an
     *     application to another execution of the same application.
     * <li>If two objects are equal according to the {@link
     *     #equals(Object) equals} method, then calling the {@code
     *     hashCode} method on each of the two objects must produce the
     *     same integer result.
     * <li>It is <em>not</em> required that if two objects are unequal
     *     according to the {@link #equals(Object) equals} method, then
     *     calling the {@code hashCode} method on each of the two objects
     *     must produce distinct integer results.  However, the programmer
     *     should be aware that producing distinct integer results for
     *     unequal objects may improve the performance of hash tables.
     * </ul>
     *
     * @return a hash code value for this object.
     * @implSpec As far as is reasonably practical, the {@code hashCode} method defined
     * by class {@code Object} returns distinct integers for distinct objects.
     * @see Object#equals(Object)
     * @see System#identityHashCode
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * The {@code equals} method implements an equivalence relation
     * on non-null object references:
     * <ul>
     * <li>It is <i>reflexive</i>: for any non-null reference value
     *     {@code x}, {@code x.equals(x)} should return
     *     {@code true}.
     * <li>It is <i>symmetric</i>: for any non-null reference values
     *     {@code x} and {@code y}, {@code x.equals(y)}
     *     should return {@code true} if and only if
     *     {@code y.equals(x)} returns {@code true}.
     * <li>It is <i>transitive</i>: for any non-null reference values
     *     {@code x}, {@code y}, and {@code z}, if
     *     {@code x.equals(y)} returns {@code true} and
     *     {@code y.equals(z)} returns {@code true}, then
     *     {@code x.equals(z)} should return {@code true}.
     * <li>It is <i>consistent</i>: for any non-null reference values
     *     {@code x} and {@code y}, multiple invocations of
     *     {@code x.equals(y)} consistently return {@code true}
     *     or consistently return {@code false}, provided no
     *     information used in {@code equals} comparisons on the
     *     objects is modified.
     * <li>For any non-null reference value {@code x},
     *     {@code x.equals(null)} should return {@code false}.
     * </ul>
     *
     * <p>
     * An equivalence relation partitions the elements it operates on
     * into <i>equivalence classes</i>; all the members of an
     * equivalence class are equal to each other. Members of an
     * equivalence class are substitutable for each other, at least
     * for some purposes.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj
     * argument; {@code false} otherwise.
     * @implSpec The {@code equals} method for class {@code Object} implements
     * the most discriminating possible equivalence relation on objects;
     * that is, for any non-null reference values {@code x} and
     * {@code y}, this method returns {@code true} if and only
     * if {@code x} and {@code y} refer to the same object
     * ({@code x == y} has the value {@code true}).
     * <p>
     * In other words, under the reference equality equivalence
     * relation, each equivalence class only has a single element.
     * @apiNote It is generally necessary to override the {@link #hashCode() hashCode}
     * method whenever this method is overridden, so as to maintain the
     * general contract for the {@code hashCode} method, which states
     * that equal objects must have equal hash codes.
     * @see #hashCode()
     * @see HashMap
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /**
     * Creates and returns a copy of this object.  The precise meaning
     * of "copy" may depend on the class of the object. The general
     * intent is that, for any object {@code x}, the expression:
     * <blockquote>
     * <pre>
     * x.clone() != x</pre></blockquote>
     * will be true, and that the expression:
     * <blockquote>
     * <pre>
     * x.clone().getClass() == x.getClass()</pre></blockquote>
     * will be {@code true}, but these are not absolute requirements.
     * While it is typically the case that:
     * <blockquote>
     * <pre>
     * x.clone().equals(x)</pre></blockquote>
     * will be {@code true}, this is not an absolute requirement.
     * <p>
     * By convention, the returned object should be obtained by calling
     * {@code super.clone}.  If a class and all of its superclasses (except
     * {@code Object}) obey this convention, it will be the case that
     * {@code x.clone().getClass() == x.getClass()}.
     * <p>
     * By convention, the object returned by this method should be independent
     * of this object (which is being cloned).  To achieve this independence,
     * it may be necessary to modify one or more fields of the object returned
     * by {@code super.clone} before returning it.  Typically, this means
     * copying any mutable objects that comprise the internal "deep structure"
     * of the object being cloned and replacing the references to these
     * objects with references to the copies.  If a class contains only
     * primitive fields or references to immutable objects, then it is usually
     * the case that no fields in the object returned by {@code super.clone}
     * need to be modified.
     *
     * @return a clone of this instance.
     * @throws CloneNotSupportedException if the object's class does not
     *                                    support the {@code Cloneable} interface. Subclasses
     *                                    that override the {@code clone} method can also
     *                                    throw this exception to indicate that an instance cannot
     *                                    be cloned.
     * @implSpec The method {@code clone} for class {@code Object} performs a
     * specific cloning operation. First, if the class of this object does
     * not implement the interface {@code Cloneable}, then a
     * {@code CloneNotSupportedException} is thrown. Note that all arrays
     * are considered to implement the interface {@code Cloneable} and that
     * the return type of the {@code clone} method of an array type {@code T[]}
     * is {@code T[]} where T is any reference or primitive type.
     * Otherwise, this method creates a new instance of the class of this
     * object and initializes all its fields with exactly the contents of
     * the corresponding fields of this object, as if by assignment; the
     * contents of the fields are not themselves cloned. Thus, this method
     * performs a "shallow copy" of this object, not a "deep copy" operation.
     * <p>
     * The class {@code Object} does not itself implement the interface
     * {@code Cloneable}, so calling the {@code clone} method on an object
     * whose class is {@code Object} will result in throwing an
     * exception at run time.
     * @see Cloneable
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImpact() {
        return impact;
    }

    public void setImpact(String impact) {
        this.impact = impact;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getForecast() {
        return forecast;
    }

    public void setForecast(String forecast) {
        this.forecast = forecast;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    @Override
    public String toString() {
        return
                "hours=" + hours +
                        ", seconds=" + seconds +
                        ", title='" + title + '\'' +
                        ", country='" + country + '\'' +
                        ", description='" + description + '\'' +
                        ", impact='" + impact + '\'' +
                        ", previous='" + previous + '\'' +
                        ", date=" + date +
                        ", forecast='" + forecast + '\'' +
                        ", minutes=" + minutes +
                        ", offset=" + offset;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hour) {
        this.hours = hour;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int second) {
        this.seconds = second;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int codePointCount) {
        this.offset = codePointCount;
    }

    private @Nullable JSONArray makeRequest() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(this.url).openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            // connection.setRequestProperty("Authorization", "Bearer " + getToken());
            connection.connect();
            int responseCode = connection.getResponseCode();
            out.printf("Response Code: %d%n", responseCode);
            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader((in)));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
                response.append("\r\n");
            }
            reader.close();
            return new JSONArray(response.toString());


        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public HashMap<Object, Object> getNews() {
        HashMap<Object, Object> news = null;
        JSONArray data = makeRequest();
        if (data == null) {
            Log.e(String.valueOf(Integer.parseInt("News")), "Error getting news");
            return null;
        }
        for (int i = 0; i < data.length(); i++) {
            JSONObject obj = data.getJSONObject(i);
            String title = obj.getString("title");

            seconds = (int) (date.getTime() / 1000);

            minutes = seconds / 60;
            if (obj.has("title")) {


                news = new HashMap<>();
                news.put("title", title);
                news.put("country", country);
                news.put("description", description);
                news.put("impact", impact);
                news.put("date", date);
                news.put("forecast", forecast);
                news.put("previous", previous);
                news.put("hours", hours);
                news.put("seconds", seconds);
                news.put("minutes", minutes);
                news.put("offset", offset);

            } else {
                Log.warn(" Error getting news");
            }
        }

        System.out.println(news);
        return news;

    }

    public Object getTradePair() {
        HashMap<Object, Object> news = null;
        JSONArray data = makeRequest();
        if (data == null) {
            Log.e(String.valueOf(Integer.parseInt("News")), "Error getting news");
            return null;
        }
        for (int i = 0; i < data.length(); i++) {
            JSONObject obj = data.getJSONObject(i);
            String title = obj.getString("title");
            if (obj.has("title")) {
                news = new HashMap<>();
                news.put("title", title);
                news.put("country", country);
                news.put("impact", impact);
                news.put("date", date);
                news.put("forecast", forecast);
                news.put("previous", previous);


            } else {
                Log.warn(" Error getting news");
            }
        }
        System.out.println(news);
        assert news != null;
        return news.get("country");

    }


    public void draw() {
        Line newsLine = new Line();
        newsLine.setRotate(90);
        newsLine.setStroke(Paint.valueOf("BLACK"));

        if (getImpact().equals("High")) {
            newsLine.setStroke(Paint.valueOf("RED"));
        }
        if (getImpact().equals("Medium")) {
            newsLine.setStroke(Paint.valueOf("YELLOW"));
        }
        if (getImpact().equals("Low")) {
            newsLine.setStroke(Paint.valueOf("GREEN"));
        }
        newsLine.setStartX(0);
        newsLine.setStartY(0);
        newsLine.setEndX(100);
        newsLine.setEndY(100);
        Canvas canvas = new Canvas(500, 400);
        StableTicksAxis xAxis = new StableTicksAxis();
        xAxis.setLabel("Hours");
        xAxis.setMinorTickVisible(false);
        xAxis.setAutoRangePadding(0.05);
        xAxis.setMinorTickCount(1);

        StableTicksAxis yAxis = new StableTicksAxis();
        yAxis.setLabel("Percent");
        yAxis.setMinorTickVisible(false);
        yAxis.setAutoRangePadding(0.05);
        yAxis.setMinorTickCount(1);

        canvas.setLayoutX(0);

        canvas.setLayoutY(0);
        VBox vBox = new VBox();
        vBox.getChildren().add(newsLine);
        vBox.getChildren().add(xAxis);
        vBox.getChildren().add(yAxis);
        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(vBox);

        StackPane.setMargin(vBox, new Insets(10, 10, 10, 10));
        StackPane.setMargin(newsLine, new Insets(10, 10, 10, 10));
        StackPane.setMargin(xAxis, new Insets(10, 10, 10, 10));
        StackPane.setMargin(yAxis, new Insets(10, 10, 10, 10));
        stackPane.setStyle("-fx-background-color: WHITE");


    }
}
