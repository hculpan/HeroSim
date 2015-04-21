package org.culpan.herosim.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.culpan.herosim.Person;
import org.culpan.herosim.Utils;

public class BaseCharListRenderer extends JLabel implements ListCellRenderer {
	protected JList list;
	
	protected boolean resize = true;
	
	protected int maxCellSize = 72;
	
	protected int minCellSize = 32;
	
	protected boolean isSelected = false;
	
	protected boolean displayTargeted = true;
	
	protected boolean centered = true;
	
	protected Person person;
	
	static protected int lastCellHeight = 0;

	public BaseCharListRenderer() {
		setOpaque(true);
	}

	static protected Font normalFont = new Font("Arial", 0, 24);

	static protected Font stunnedFont = normalFont.deriveFont(Font.ITALIC);

	static protected Font unconsciousFont = stunnedFont;
	
	static protected Image flashedImage; 
	
	static protected Image actedImage;
	
	static protected Image notActedImage;
	
	static protected Image unconsciousImage;
	
	static protected Image stunnedImage;
		
	static protected Image originalFlashedImage;
	
	static protected Image originalActedImage;
	
	static protected Image originalNotActedImage;
	
	static protected Image originalUnconsciousImage;
	
	static protected Image originalStunnedImage;
		
	{
		if (originalFlashedImage == null) {
			originalFlashedImage = Utils.createImage(this, "/flash.png");
			originalActedImage = Utils.createImage(this, "/acted.png");
			originalNotActedImage = Utils.createImage(this, "/not_acted.png");
			originalUnconsciousImage = Utils.createImage(this, "/unconscious.png");
			originalStunnedImage = Utils.createImage(this, "/stunned.png");
		}
	}
	
	public Component getListCellRendererComponent(JList list, 
			Object value, // value to display
			int index, // cell index
			boolean isSelected, // is the cell selected
			boolean cellHasFocus) // the list and the cell have the focus
	{
		this.isSelected = isSelected || cellHasFocus;
		this.list = list;
		
		int cellHeight = minCellSize;
		if (resize) {
			cellHeight = list.getHeight() / list.getModel().getSize();
			if (cellHeight < minCellSize) {
				cellHeight = minCellSize;
			} else if (cellHeight > maxCellSize) {
				cellHeight = maxCellSize;
			}
		} 
		
		if (lastCellHeight != cellHeight) {
			flashedImage = originalFlashedImage.getScaledInstance(cellHeight, cellHeight, Image.SCALE_SMOOTH);
			actedImage = originalActedImage.getScaledInstance(cellHeight, cellHeight, Image.SCALE_SMOOTH);
			notActedImage = originalNotActedImage.getScaledInstance(cellHeight, cellHeight, Image.SCALE_SMOOTH);
			unconsciousImage = originalUnconsciousImage.getScaledInstance(cellHeight, cellHeight, Image.SCALE_SMOOTH);
			stunnedImage = originalStunnedImage.getScaledInstance(cellHeight, cellHeight, Image.SCALE_SMOOTH);
			setPreferredSize(new Dimension(list.getWidth(), cellHeight));
			setMinimumSize(new Dimension(list.getWidth(), cellHeight));
			
			float fontSize = cellHeight / 2;
			normalFont = normalFont.deriveFont((fontSize < 24 ? 24 : fontSize));
			stunnedFont = normalFont.deriveFont(Font.ITALIC);
			unconsciousFont = stunnedFont;
			
			lastCellHeight = cellHeight;
		}
		
		if (value instanceof Person) {
			person = (Person) value;
		}
//		setBorder(new LineBorder(Color.black, 2, true));
		
		return this;
	}
	
	protected void drawContents(Graphics2D g) {
		g.setBackground(Color.white);
		
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		int x = 0;
		
		g.setFont(normalFont);
		g.setColor(Color.black);
		
		String text;
		
		if (displayTargeted && person.getTargetName() != null) {
			text = person.getDisplayName() + "  >>>  " + person.getTargetName();
		} else {
			text = person.getDisplayName();
		}

		FontMetrics fm = g.getFontMetrics();
		int width = fm.stringWidth(text);
		
		if (centered) {
			x = getWidth() / 2 - ((width + lastCellHeight) / 2);
			if (person.isFlashed()) {
				x -= (lastCellHeight / 2);
			}
			
			if (person.isStunned() || person.isUnconscious()) {
				x -= (lastCellHeight / 2);
			}
		}

		if (person.hasActed()) {
			g.drawImage(actedImage, x, 0, null);
		} else {
			g.drawImage(notActedImage, x, 0, null);
		}
		
		g.drawString(text, x + lastCellHeight, (int)(lastCellHeight * .65));
		
		drawConditions(g, x + width + lastCellHeight, person);
	}
	
	protected void drawConditions(Graphics2D g, int xPosStart, Person p) {
		int condCount = 0;
		xPosStart += 10;
		if (p.isFlashed()) {
			g.drawImage(flashedImage, (condCount++ * lastCellHeight) + xPosStart, 0, null);
		}
		
		if (p.isUnconscious()) {
			g.drawImage(unconsciousImage, (condCount++ * lastCellHeight) + xPosStart, 0, null);
		} else if (p.isStunned()) {
			g.drawImage(stunnedImage, (condCount++ * lastCellHeight) + xPosStart, 0, null);
		}
	}

	public void paint(Graphics g) {
		super.paint(g);
		
		drawContents((Graphics2D)g);
		
		g.dispose();
	}

	public boolean isCentered() {
		return centered;
	}

	public void setCentered(boolean centered) {
		this.centered = centered;
	}

	public int getMaxCellSize() {
		return maxCellSize;
	}

	public void setMaxCellSize(int maxCellSize) {
		this.maxCellSize = maxCellSize;
	}

	public int getMinCellSize() {
		return minCellSize;
	}

	public void setMinCellSize(int minCellSize) {
		this.minCellSize = minCellSize;
	}
}
