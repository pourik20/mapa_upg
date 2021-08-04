import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.*;

/**
 * Panel pro vykresleni mapy
 * @author Tomas Pour, A20B0214P
 * @version 1.0
 */
public class MapaPanel extends JPanel {

	private int mapWidth;
	private int mapHeight;
	private int maxValue;
	/** Data ze souboru jako int */
	private List<Integer> intList = new ArrayList<>();
	/** Data ze souboru jako RGB */
	private int[] rgbArray;
	private BufferedImage mapaImg;
	private int maxPrevyseni;
	private int minPrevyseni;
	private int maxStoupani;
	private int maxPrevyseniIndex;
	private int minPrevyseniIndex;
	private int maxStoupaniIndex;

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		drawMap(g2, this.getWidth(), this.getHeight());
	}

	/**
	 * Nacte data ze souboru a ulozi je do intList
	 * @param file Vstupni soubor
	 * @param sc Scanner
	 */
	public void loadData(File file, Scanner sc) {

		// Nacteni rozmeru a max hodnoty
		sc.nextLine();
		String[] rozmery;
		String dalsi = sc.nextLine();
		boolean run = true;
		while(run) {
			if (dalsi.contains("#")) {
				dalsi = sc.nextLine();
			} else {
				rozmery = dalsi.split("\\s+");
				this.mapWidth = Integer.parseInt(rozmery[0]);
				if (rozmery.length == 1) {
					this.mapHeight = sc.nextInt();
				} else {
					this.mapHeight = Integer.parseInt(rozmery[1]);
				}
				run = false;
			}
		}
		this.maxValue = sc.nextInt();

		// Nacteni hodnot
		while (sc.hasNext()) {
			int value = sc.nextInt();
			this.intList.add(value);
		}

		// Min/max prevyseni a max stoupani
		int min = intList.get(0);
		int stoupani = 0;
		for (int i = 1; i < intList.size(); i++) {
			int value = intList.get(i);
			int rozdil = intList.get(i) - intList.get(i-1);
			if (value < min) {
				min = value;
			}
			if (rozdil > stoupani) {
				stoupani = rozdil;
			}
		}
		maxPrevyseni = maxValue;
		maxPrevyseniIndex = intList.indexOf(maxPrevyseni) + 1;
		minPrevyseni = min;
		minPrevyseniIndex = intList.indexOf(minPrevyseni) + 1;
		maxStoupani = stoupani;
		maxStoupaniIndex = intList.indexOf(maxStoupani) + 1;

	}

	/**
	 * Prevede hodnoty z intList do RGB podoby
	 * a ulozi je do rgbArray
	 */
	public void processData() {
		rgbArray = new int[mapWidth * mapHeight];
		if (maxValue > 255) {
			for (int i = 0; i < intList.size(); i++) {
				int novaHodnota = (int) ((double)intList.get(i) / maxValue * 255);
				intList.set(i, novaHodnota);
			}
		}
		for (int i = 0; i < intList.size(); i++) {
			int in_rgb = intList.get(i);

			int b = in_rgb & 0xFF;
			int g = (in_rgb >> 8) & 0xFF;
			int r = (in_rgb >> 16) & 0xFF;

			int gr = (7*b + 15*r + 10*g) / 10;
			b = gr;
			g = gr;
			r = gr;

			int out_rgb = (r << 16) | (g << 8) | b;
			rgbArray[i] = out_rgb;
		}
	}

	/**
	 * Vykresli mapu a sipky
	 * @param g2 Graficky kontext
	 * @param W Sirka
	 * @param H Vyska
	 */
	public void drawMap(Graphics2D g2, int W, int H) {
		mapaImg = new BufferedImage(mapWidth, mapHeight, BufferedImage.TYPE_3BYTE_BGR);

		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, this.getWidth(), this.getHeight());

		int iW = mapWidth;
		int iH = mapHeight;
		double scaleX = ((double)W) / iW;
		double scaleY = ((double)H) / iH;
		double scale = Math.min(scaleX, scaleY);
		int nW = (int)(iW*scale);
		int nH = (int)(iH*scale);
		int X = (W - nW) / 2;
		int Y = (H - nH) / 2;

		mapaImg.setRGB(0, 0, mapWidth, mapHeight, rgbArray, 0, mapWidth);
		g2.drawImage(mapaImg, X, Y, nW, nH, null);
		
		g2.setColor(Color.RED);
		drawMapArrow(maxPrevyseniIndex, X, Y, nW, nH, iW, iH, g2);
		drawMapArrow(minPrevyseniIndex, X, Y, nW, nH, iW, iH, g2);
		drawMapArrow(maxStoupaniIndex, X, Y, nW, nH, iW, iH, g2);
	}

	/**
	 * Vykresli sipku
	 * @param x1 Pocatek X
	 * @param y1 Pocatek Y
	 * @param x2 Hrot X
	 * @param y2 Hrot Y
	 * @param tip_length Delka hrotu
	 * @param g2 Graficky kontext
	 */
	private void drawArrow(double x1, double y1,
						   double x2, double y2, double tip_length,
						   Graphics2D g2) {

		double u_x = x2 - x1;
		double u_y = y2 - y1;
		double u_len1 = 1 / Math.sqrt(u_x * u_x + u_y*u_y);
		u_x *= u_len1;
		u_y *= u_len1;
		//u ma delku 1

		final int strokeSize = 3;
		g2.setStroke(new BasicStroke(strokeSize,
				BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_MITER));

		g2.draw(new Line2D.Double(x1, y1, x2 - u_x*strokeSize, y2 - u_y*strokeSize));

		//smer kolmy (jednotkova delka)
		double v_x = u_y;
		double v_y = -u_x;

		//smer kolmy - delka o 1/2 sirky hrotu
		v_x *= 0.5*tip_length;
		v_y *= 0.5*tip_length;

		double c_x = x2 - u_x*tip_length;
		double c_y = y2 - u_y*tip_length;

		Path2D tip = new Path2D.Double();
		tip.moveTo(c_x + v_x, c_y + v_y);
		tip.lineTo(x2, y2);
		tip.lineTo(c_x - v_x, c_y - v_y);

		g2.draw(tip);
	}

	/**
	 * Vykresli sipku s popiskem smerujici k zadane hodnote
	 * @param index Hodnota
	 * @param X souradnice X
	 * @param Y souradnice Y
	 * @param nW Sirka po zmene meritka
	 * @param nH Vyska po zmene meritka
	 * @param iW Sirka mapy
	 * @param iH Vyska mapy
	 * @param g2 Graficky kontext
	 */
	public void drawMapArrow(int index, int X, int Y, int nW, int nH, int iW, int iH, Graphics2D g2) {
		int hrotY = index / mapWidth;
		int zbytek = index % mapWidth;
		int hrotX = (zbytek == 0) ? mapWidth : zbytek;
		int startX = hrotX - 40;
		int startY = hrotY + 40;

		String text = null;
		if (index == maxPrevyseniIndex)
			text = "max prevyseni";
		else if (index == minPrevyseniIndex)
			text = "min prevyseni";
		else if (index == maxStoupaniIndex)
			text = "max stoupani";
		int textX;
		int textY;
		int textWidth = g2.getFontMetrics().stringWidth(text);

		if (startX < 0) {
			startX += 80;
		}

		if (startY > mapHeight) {
			startY -= 80;
			textY = nH*startY/iH + Y - 10;
		} else {
			textY = nH*startY/iH + Y + 10;
		}
		textX = nW*startX/iW + X - textWidth/2;

		if (textX < 0) {
			textX += textWidth/2;
		} else if (textX + textWidth > nW) {
			textX -= textWidth/2;
		}


		drawArrow(1.0*nW*startX/iW + X, 1.0*nH*startY/iH + Y, X + 1.0*nW*hrotX/iW, Y + 1.0*nH*hrotY/iH, 10, g2);
		g2.drawString(text, textX, textY);
	}

}
