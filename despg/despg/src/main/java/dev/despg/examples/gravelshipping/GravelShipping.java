/**
 * Copyright (C) 2021 despg.dev, Ralf Buschermöhle
 *
 * DESPG is made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * see LICENSE
 *
 */
package dev.despg.examples.gravelshipping;

import java.util.logging.Level;
import java.util.logging.Logger;

import dev.despg.core.Event;
import dev.despg.core.EventQueue;
import dev.despg.core.Simulation;
import dev.despg.core.Time;

public class GravelShipping extends Simulation
{
	private static Logger logger = Logger.getLogger("GravelShipping");

	private static Integer gravelToShip = 2000;
	private static Integer gravelShipped = 0;
	private final int gravelToShippedFinal = gravelToShip;
	private static Integer successfulLoadings = 0;

	private static Integer successfulLoadingSizes = 0;
	private static Integer unsuccessfulLoadings = 0;
	private static Integer unsuccessfulLoadingSizes = 0;

	private static final int NUM_TRUCKS = 5;
	private static final int NUM_LOADING_DOCKS = 3;
	private static final int NUM_WEIGHING_STATIONS = 1;

	/**
	 * Defines the setup of simulation objects and starting events before executing
	 * the simulation. Prints utilization statistics afterwards
	 *
	 * @param args not used
	 */
	public static void main(String[] args)
	{
		EventQueue eventqueue = EventQueue.getInstance();

		for (int i = 0; i < NUM_TRUCKS; i++)
			eventqueue.add(new Event(0, GravelLoadingEventTypes.Loading, new Truck("T" + i), LoadingDock.class, null));

		for (int i = 0; i < NUM_LOADING_DOCKS; i++)
			new LoadingDock("LD" + i);

		for (int i = 0; i < NUM_WEIGHING_STATIONS; i++)
			new WeighingStation("WS" + i);

		GravelShipping gs = new GravelShipping();
		int timeStep = gs.simulate();

		// output some statistics after simulation run
		logger.log(Level.INFO, "Gravel shipped\t\t = " + gravelShipped + " tons");
		logger.log(Level.INFO, "Mean Time / Gravel Unit\t = " + ((double) timeStep / gravelShipped) + " minutes");

		logger.log(Level.INFO,
				String.format("Successfull loadings\t = %d(%.2f%%), mean size %.2ft", successfulLoadings,
						(double) successfulLoadings / (successfulLoadings + unsuccessfulLoadings) * 100,
						(double) successfulLoadingSizes / successfulLoadings));

		logger.log(Level.INFO,
				String.format("Unsuccessfull loadings\t = %d(%.2f%%), mean size %.2ft", unsuccessfulLoadings,
						(double) unsuccessfulLoadings / (successfulLoadings + unsuccessfulLoadings) * 100,
						(double) unsuccessfulLoadingSizes / unsuccessfulLoadings));
	}

	/**
	 * Prints information after every timeStep in which an event got triggered.
	 */
	@Override
	protected void printEveryStep(int numberOfSteps, int timeStep)
	{
		String time = numberOfSteps + ". " + Time.stepsToString(timeStep);
		String eventQueue = "EventQueue: " + EventQueue.getInstance().toString();
		String shipped = String.format("shipped/toShip : %dt(%.2f%%) / %dt", gravelShipped,
				(double) gravelShipped / gravelToShippedFinal * 100, gravelToShip);

		logger.log(Level.INFO, time + " " + shipped + "\n " + eventQueue + "\n");
	}

	public static Integer getGravelToShip()
	{
		return gravelToShip;
	}

	public static void setGravelToShip(Integer gravelToShip)
	{
		GravelShipping.gravelToShip = gravelToShip;
	}

	public static Integer getGravelShipped()
	{
		return gravelShipped;
	}

	public static void setGravelShipped(Integer gravelShipped)
	{
		GravelShipping.gravelShipped = gravelShipped;
	}

	public static Integer getSuccessfulLoadings()
	{
		return successfulLoadings;
	}

	public static void setSuccessfulLoadings(Integer successfulLoadings)
	{
		GravelShipping.successfulLoadings = successfulLoadings;
	}

	public static Integer getSuccessfulLoadingSizes()
	{
		return successfulLoadingSizes;
	}

	public static void setSuccessfulLoadingSizes(Integer successfulLoadingSizes)
	{
		GravelShipping.successfulLoadingSizes = successfulLoadingSizes;
	}

	public static Integer getUnsuccessfulLoadings()
	{
		return unsuccessfulLoadings;
	}

	public static void setUnsuccessfulLoadings(Integer unsuccessfulLoadings)
	{
		GravelShipping.unsuccessfulLoadings = unsuccessfulLoadings;
	}

	public static Integer getUnsuccessfulLoadingSizes()
	{
		return unsuccessfulLoadingSizes;
	}

	public static void setUnsuccessfulLoadingSizes(Integer unsuccessfulLoadingSizes)
	{
		GravelShipping.unsuccessfulLoadingSizes = unsuccessfulLoadingSizes;
	}

	public int getGravelToShippedFinal()
	{
		return gravelToShippedFinal;
	}
	
}
