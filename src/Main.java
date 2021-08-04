import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * @author Tomas Pour, A20B0214P
 * @version 1.0
 */
public class Main {

	public static Scanner sc;

	public static void main(String[] args) {
		// Vytvoreni okna
		JFrame okno = new JFrame();
		okno.setTitle("Vizualizace mapy - Tomas Pour");
		okno.setSize(800, 600);
		
		// Pridani panelu do okna
		MapaPanel panel = new MapaPanel();
		okno.add(panel, BorderLayout.CENTER);

		String path = args[0];
		File file = new File(path);
		try {
			sc = new Scanner(file);
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		}
		panel.loadData(file, sc);
		panel.processData();
		
		okno.setLocationRelativeTo(null);
		okno.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		okno.setVisible(true);
	}
}
