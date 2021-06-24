import javax.imageio.IIOException;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Synchronization {    // for shared resource.

    public static File managerOutput = new File("C:\\Users\\alireza\\Desktop\\NetworkProject\\src\\managerOutput.txt");
    public static Semaphore mutex = new Semaphore(1);

    public static void pollingWait(DataInputStream in)  {
        try {
            while (in.available() == 0);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void addDelaySec(int sec){

        try {
            TimeUnit.SECONDS.sleep(sec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
