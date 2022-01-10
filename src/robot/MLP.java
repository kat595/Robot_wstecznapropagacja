package robot;

import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class MLP
{
	public static final int N = 10000;
	public static final int BREAK = 100000000;
	public static final int INTERVAL = 10000;
	public int armLength = 75;
	public final int START_X = 0;
	public final int START_Y = 200;

	Random rand = new Random();
	public Layer layers[];

	public MLP()
	{
		int[] numberOfPerceptrons = { 2, 11, 4, 2 };
		Random random = new Random();
		final double constantLearning = 0.1;

		layers = new Layer[numberOfPerceptrons.length];

		Perceptron[] perceptron = new Perceptron[numberOfPerceptrons[0]];
		for (int j = 0; j < numberOfPerceptrons[0]; j++)
		{
			double[] weights = new double[2];

			for (int i = 0; i < weights.length; i++) weights[i] = random.nextDouble();


			double weightForBias = random.nextDouble();
			Perceptron p = new Perceptron(weights, weightForBias, constantLearning);
			perceptron[j] = p;
		}

		Layer layer1 = new Layer(perceptron);
		layers[0] = layer1;

		for (int k = 1; k < numberOfPerceptrons.length; k++)
		{
			Perceptron[] perceptron2 = new Perceptron[numberOfPerceptrons[k]];
			for (int j = 0; j < numberOfPerceptrons[k]; j++)
			{

				double[] weights = new double[numberOfPerceptrons[k - 1]];
				for (int i = 0; i < weights.length; i++) weights[i] = random.nextDouble();

				double weightForBias = random.nextDouble();
				Perceptron p = new Perceptron(weights, weightForBias, constantLearning);
				perceptron2[j] = p;
			}

			Layer layer2 = new Layer(perceptron2);
			layers[k] = layer2;

		}
	}

	public Point countXY(Point poinXY, Point centerPoint, double alfa)
	{
		double rad = alfa * (Math.PI / 180);
		double cosRad = Math.cos(rad);
		double sinRad = Math.sin(rad);

		int x = (int) (cosRad * (poinXY.x - centerPoint.x) - sinRad * (poinXY.y - centerPoint.y) + centerPoint.x);
		int y = (int) (sinRad * (poinXY.x - centerPoint.x) + cosRad * (poinXY.y - centerPoint.y) + centerPoint.y);
		Point point = new Point(x, y);

		return point;
	}

	public Point getSecondArmPoint(double alfa, double beta)
	{
		beta = -180 + beta;
		Point center = new Point(START_X, START_Y);

		Point firstArm = countXY(new Point(START_X, START_Y - armLength), center, alfa);
		Point tempPoint = countXY(new Point(START_X, START_Y - 2 * armLength), center, alfa);
		Point secondPoint = countXY(new Point(tempPoint.x, tempPoint.y), new Point(firstArm.x, firstArm.y), beta);

		return secondPoint;
	}

	public Point getFirstArmPoint(double alfa)
	{
		Point center = new Point(START_X, START_Y);
		Point newPoint = countXY(new Point(START_X, START_Y - armLength), center, alfa);

		return newPoint;
	}

	// Uczenie sieci wraz z algorytmem wstecznej propagacji b³êdu
	public void learning()
	{
		double[][] listOfExamples = loadFileToArray("test.txt");
		int r = 1;
		double error = 0;
		int k = 0;

		while (true) {
			r = rand.nextInt(N - 1) + 0;
			SingleExample singleExample = new SingleExample(listOfExamples[r][0], listOfExamples[r][1], listOfExamples[r][2], listOfExamples[r][3]);
			countThrough(singleExample);
			countBack(singleExample);
			correctWeigths(singleExample);

			if (k % INTERVAL == 0)
			{
				error = countError(listOfExamples);
				System.out.println("Blad: " + error);
				if (error < 25)
					break;
			}

			if (k > BREAK)
				break;
			k++;
		}
	}

	public void countThrough(SingleExample singleExample)
	{
		List<Double> list = new ArrayList<>();
		list.add(singleExample.getX());
		list.add(singleExample.getY());

		for (int i = 0; i < layers.length; i++) list = layers[i].sum(list);

	}

	public void countBack(SingleExample singleExample)
	{
		List<Double> list = new ArrayList<>();
		list.add(singleExample.getAlfa());
		list.add(singleExample.getBeta());
		layers[layers.length - 1].delta(list);

		for (int i = layers.length - 2; i >= 0; i--)
		{
			for (int j = 0; j < layers[i].perceptron.length; j++)
			{
				for (int k = 0; k < layers[i + 1].perceptron.length; k++)
				{
					layers[i].perceptron[j].delta += layers[i + 1].perceptron[k].delta * layers[i + 1].perceptron[k].weigths[j];
				}
				layers[i].perceptron[j].delta *= layers[i].perceptron[j].sum * (1 - layers[i].perceptron[j].sum);
			}
		}
	}

	public void correctWeigths(SingleExample singleExample)
	{
		List<Double> listSum = new ArrayList<>();
		listSum.add(singleExample.getX());
		listSum.add(singleExample.getY());

		for (int i = 0; i < layers.length; i++)
		{
			layers[i].correctWeigths(listSum);
			listSum.clear();

			for (int j = 0; j < layers[i].perceptron.length; j++) listSum.add(layers[i].perceptron[j].sum);

		}
	}

	public SingleExample findResult(int x, int y)
	{
		double tx = ((double) x / 150) * 0.8 + 0.1;
		double ty = ((double) y / 300) * 0.8 + 0.1;

		SingleExample test = new SingleExample();
		test.setX(tx);
		test.setY(ty);

		countThrough(test);
		double alfa = layers[layers.length - 1].perceptron[0].sum;
		double beta = layers[layers.length - 1].perceptron[1].sum;

		alfa = (alfa - 0.1) / 0.8 * 180;
		beta = (beta - 0.1) / 0.8 * 180;
		test.setAlfa(alfa);
		test.setBeta(beta);

		return test;
	}

	// Obliczanie blêdu dla sieci
	private double countError(double[][] examples)
	{
		double error = 0;

		for (int n = 0; n < examples.length; n++)
		{
			SingleExample singleExample = new SingleExample(examples[n][0], examples[n][1], examples[n][2],
					examples[n][3]);
			error += countSingleExampleError(singleExample);
		}

		return error * 0.5;
	}

	// Obliczanie b³edu dla pojedyñczego przyk³adu
	public double countSingleExampleError(SingleExample singleExample)
	{
		double result = 0;
		double[] t = new double[] { singleExample.getAlfa(), singleExample.getBeta()};

		countThrough(singleExample);

		for (int k = 0; k < layers[layers.length - 1].perceptron.length; k++)
		{
			result += (Math.pow(layers[layers.length - 1].perceptron[k].sum - t[k], 2));
		}

		return result;
	}

	// Czytanie pliku z przyk³adami
	public double[][] loadFileToArray(String name)
	{
		int i = 0;

		File file = null;
		
		try
		{
			file = new File(this.getClass().getClassLoader().getResource(name).toURI());
		}
		catch (URISyntaxException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try
		{
			Scanner in = new Scanner(file);

			while (in.hasNextLine())
			{
				String line = in.nextLine();
				i++;
			}

			in.close();

			Scanner in2 = new Scanner(file);
			double[][] example = new double[i][4];
			int c = 0;

			while (in2.hasNextLine())
			{
				String line = in2.nextLine();
				String[] s = line.split(" ");

				for (int j = 0; j < 4; j++)
				{
					example[c][j] = Double.parseDouble(s[j]);
				}

				c++;
			}

			in2.close();

			return example;

		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
