package simpleConverter;

import com.google.gson.Gson;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws IllegalAccessException {

        JsonConverter jsonConverter=new JsonConverter();
        System.out.println(jsonConverter.toJson(new int[] {1,2,3} ));

        Gson gson = new Gson();
        System.out.println(gson.toJson(new int[] {1,2,3}));


    }
}
