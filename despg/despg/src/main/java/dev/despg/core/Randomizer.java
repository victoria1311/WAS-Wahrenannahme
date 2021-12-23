/**
 * Copyright (C) 2021 despg.dev, Ralf Buschermöhle
 *
 * DESPG is made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * see LICENSE
 *
 */
package dev.despg.core;

import java.util.ArrayList;
import java.util.Random;

public class Randomizer
{
	private static final double MIN_PROBABILITY = 0.0;
	private static final double MAX_PROBABILITY = 1.0;

	public static double getMinProbability()
	{
		return MIN_PROBABILITY;
	}

	public static double getMaxProbability()
	{
		return MAX_PROBABILITY;
	}

	private static Random random = new Random();
	private ArrayList<Probability2Value<Integer>> prob2Int = new ArrayList<Probability2Value<Integer>>();

	public ArrayList<Probability2Value<Integer>> getProb2Int()
	{
		return prob2Int;
	}

	/**
	 * This method is used to assign Integer values to a certain probability.
	 *
	 * @param to    Upper limit for that probability to occur
	 * @param value Assigned value of that probability
	 */
	public void addProbInt(double to, int value) throws SimulationException
	{
		for (Probability2Value<Integer> prob2value : prob2Int)
		{
			if (prob2value.getProbabilityUpperLimit() == to)
				throw new SimulationException("Probability " + to + " already exists");
		}

		if (to >= MIN_PROBABILITY && to <= MAX_PROBABILITY)
			prob2Int.add(new Probability2Value<Integer>(to, value));
		else
			throw new SimulationException(
					"Probability " + to + " is out of bounds (" + MIN_PROBABILITY + "-" + MAX_PROBABILITY + ")");
	}

	/**
	 * This method iterates through an ArrayList of Probability2Value objects and
	 * compares its probabilityUpperLimit against a random Double.
	 *
	 * @return This returns the Integer value that had been assigned to the occurred
	 *         probability
	 */
	public int nextInt() throws SimulationException
	{
		if (prob2Int.isEmpty())
			throw new SimulationException("No probabilities in ArrayList");

		double r = random.nextDouble();

		for (Probability2Value<Integer> pI : prob2Int)
		{
			if (pI.getProbabilityUpperLimit() < MIN_PROBABILITY || pI.getProbabilityUpperLimit() > MAX_PROBABILITY)
				throw new SimulationException("Probability " + pI.getProbabilityUpperLimit() + " is out of bounds ("
						+ MIN_PROBABILITY + "-" + MAX_PROBABILITY + ")");
			else if (r <= pI.getProbabilityUpperLimit())
				return pI.getValue();
		}

		throw new SimulationException("Probability not covered");
	}

	public static int getPoisson(double lambda)
	{
		double l = Math.exp(-lambda);
		double p = 1.0;
		int k = 0;

		do
		{
			k++;
			p *= Math.random();
		} while (p > l);

		return k - 1;
	}
}
