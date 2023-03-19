package cryptoinvestor.cryptoinvestor;

public record   Message (){

        private static String from;
        private static String to;
        private static String content;

        public void setContent(String s) {
                content = s;
        }
        public String getContent() {
                return content;
        }

        public void setFrom(String s) {
                from = s;
        }
        public String getFrom() {
                return from;
        }

        //standard constructors, getters, setters

}
