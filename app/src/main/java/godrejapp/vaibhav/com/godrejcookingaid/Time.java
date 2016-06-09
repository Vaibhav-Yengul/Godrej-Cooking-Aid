package godrejapp.vaibhav.com.godrejcookingaid;

/**
 * Created by vaibhav on 22/5/16.
 */
public class Time {
    private int minutes;
    private int seconds;

    public Time(int min,int sec){
        this.minutes = min;
        if(sec>0 && sec < 60){
            this.seconds = sec;
        }
        else{
            this.seconds = 0;
            this.minutes++;
        }
    }

    public int getMinute(){
        return minutes;
    }
    public int getSeconds(){
        return seconds;
    }
    public String toString(){
        return minutes + "," + seconds;
    }
}
