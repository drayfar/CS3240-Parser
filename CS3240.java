import java.io.FileNotFoundException;
import java.io.IOException;


public class CS3240 {
	public static void main(String[] args) {
		try {
			GrammarParser g = new GrammarParser("/Users/drayfar/Downloads/grammarrules.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
