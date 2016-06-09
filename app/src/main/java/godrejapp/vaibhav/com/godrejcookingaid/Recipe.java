package godrejapp.vaibhav.com.godrejcookingaid;

import java.util.ArrayList;

/**
 * Created by vaibhav on 22/5/16.
 */
public class Recipe {

    private String ingredient;
    private String quantity;
    private Time[] time = new Time[1000];
    private double[] temperature = new double[1000];

    public Recipe(String ing,String qty,Time t, double temp){
        this.ingredient = ing;
        this.quantity = qty;
        setTimes(0,t);
        setTemp(0,temp);
    }

    public String getIngredient(){
        return this.ingredient;
    }

    public String getQuantity(){
        return this.quantity;
    }

    public Time getTime(int i){
        return time[0];
    }
    public Time getTime(){
        return time[0];
    }

    public void setTimes(int i, Time t){
        time[0] = t;
    }
    public void setTemp(int i, double d){
        temperature[i] = d;
    }

    public double getTemperature(int i){
        return this.temperature[i];
    }
    public double getTemperature(){
        return this.temperature[0];
    }

    public String toString(){
        return ingredient+ "," + quantity + "," + time[0].toString() + "," + temperature[0];
    }
}
