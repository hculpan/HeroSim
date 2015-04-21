package org.culpan.herosim.plugin.bonjour;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.JComponent;

import org.culpan.herosim.Person;
import org.culpan.herosim.Utils;

public class PersonLabel extends JComponent {
	protected boolean resize = true;
	
	protected int maxCellSize = 72;
	
	protected int minCellSize = 32;
	
	protected boolean isSelected = false;
	
	protected boolean displayTargeted = true;
	
	protected boolean centered = true;
	
	protected boolean showActed = false;
	
	protected Person person;
	
	static protected int lastCellHeight = 0;

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
	
	public PersonLabel(Person person) {
		super();
		this.person = person;
	}
	
	protected void drawContents(Graphics2D g) {
		int cellHeight = getHeight();
		if (lastCellHeight != cellHeight) {
			flashedImage = originalFlashedImage.getScaledInstance(cellHeight, cellHeight, Image.SCALE_SMOOTH);
			actedImage = originalActedImage.getScaledInstance(cellHeight, cellHeight, Image.SCALE_SMOOTH);
			notActedImage = originalNotActedImage.getScaledInstance(cellHeight, cellHeight, Image.SCALE_SMOOTH);
			unconsciousImage = originalUnconsciousImage.getScaledInstance(cellHeight, cellHeight, Image.SCALE_SMOOTH);
			stunnedImage = originalStunnedImage.getScaledInstance(cellHeight, cellHeight, Image.SCALE_SMOOTH);
			//setPreferredSize(new Dimension(list.getWidth(), cellHeight));
			//setMinimumSize(new Dimension(list.getWidth(), cellHeight));
			
			float fontSize = cellHeight / 2;
			normalFont = normalFont.deriveFont((fontSize < 24 ? 24 : fontSize));
			stunnedFont = normalFont.deriveFont(Font.ITALIC);
			unconsciousFont = stunnedFont;
			
			lastCellHeight = cellHeight;
		}
		
		if (isOpaque()) {
			g.setBackground(Color.white);

			g.setColor(Color.white);
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		
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

		if (showActed) {
			if (person.hasActed()) {
				g.drawImage(actedImage, x, 0, null);
			} else {
				g.drawImage(notActedImage, x, 0, null);
			}
			
			x += lastCellHeight;
		}
		
		if (person.isStunned()) {
			g.setColor(Color.orange);
		} else if (person.isUnconscious()) {
			g.setColor(Color.red);
		} else if (person.hasActed()) {
			g.setColor(Color.gray);
		}
		
		
		g.drawString(text, x, (int)(lastCellHeight * .65));
		
		drawConditions(g, x + width, person);
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

	public void paintComponent(Graphics g) {
		drawContents((Graphics2D)g);
		
		g.dispose();
	}

	public boolean isShowActed() {
		return showActed;
	}

	public void setShowActed(boolean showActed) {
		this.showActed = showActed;
	}


}
