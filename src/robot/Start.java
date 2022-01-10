package robot;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Start
{

	public static void main(String[] args)
	{
		MLP mlp = new MLP();
		mlp.learning();
		System.out.println("Gotowy");
		GUI(mlp);
	}

	// Interfejs graficzny
	public static void GUI(MLP mlp)
	{
		JFrame frame = new JFrame("Rêka robota: wsteczna propagacja bledu");
		BufferedImage robotImage = null;
		try
		{
			robotImage = ImageIO.read(Start.class.getClassLoader().getResourceAsStream("robotImage.jpg"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		JLabel robotLabel = new JLabel(new ImageIcon(robotImage));
		
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		final Line line = new Line();
		
		// Obliczenie pozycji ramienia podczas poruszania
		line.addMouseMotionListener(new MouseMotionAdapter()
		{
			@Override
			public void mouseMoved(MouseEvent evt)
			{
				SingleExample t = mlp.findResult(evt.getX(), evt.getY());
				Point firstArmPoint = mlp.getFirstArmPoint(t.getAlfa());
				Point secondArmPoint = mlp.getSecondArmPoint(t.getAlfa(), t.getBeta());

				line.clearLines();
				line.addLine(mlp.START_X, mlp.START_Y, firstArmPoint.x, firstArmPoint.y, secondArmPoint.x, secondArmPoint.y);
			}
		});

		line.setPreferredSize(new Dimension(200, 300));
		frame.getContentPane().add(robotLabel, BorderLayout.WEST);
		frame.getContentPane().add(line, BorderLayout.CENTER);
		frame.pack();
		// Wyœrodkownia UI
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
