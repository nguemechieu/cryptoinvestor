package cryptoinvestor.cryptoinvestor;

public record Message() {

    private static String from;
    private static String to;
    private static String content;

    public String getContent() {
        return content;
    }

    public void setContent(String s) {
        content = s;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String s) {
        from = s;
    }

    //standard constructors, getters, setters

}
