package pl.hycom.pip.messanger.nlp;

/**
 * Created by Piotr on 2017-06-05.
 */
public class Result {

    private String base;
    private  String result;

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Result(String base, String result) {
       this.base = base;
       this.result = result;


   }
}