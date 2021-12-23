/**
 * Copyright (C) 2021 despg.dev, Ralf Buschermöhle
 *
 * DESPG is made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * see LICENSE
 *
 */
package dev.despg.examples.gravelshippingWithQueue;

import java.util.HashMap;
import java.util.Map;

import dev.despg.core.Event;
import dev.despg.core.EventQueue;
import dev.despg.core.Filter;
import dev.despg.core.Queue;
import dev.despg.core.Randomizer;
import dev.despg.core.SimulationObject;
import dev.despg.core.SimulationObjects;

public class LoadingDock extends SimulationObject
{
	private String name;

	private Queue<Truck> roadToWeighingStations;
	private Queue<Truck> roadToLoadingDocks;
	private Truck truckCurrentlyLoaded;
	private Truck truckCurrentlyUnloaded;

	private static Randomizer loadingWeight;
	private static Randomizer loadingTime;
	private static Randomizer unloadingTime;
	private static Randomizer drivingToWeighingStation;

	private static EventQueue eventQueue;

	public LoadingDock(String name, Queue roadToWeighingStations, Queue roadToLoadingDocks)
	{
		eventQueue = EventQueue.getInstance();

		this.name = name;
		this.roadToLoadingDocks = roadToLoadingDocks;
		this.roadToWeighingStations = roadToWeighingStations;

		loadingWeight = new Randomizer();
		loadingWeight.addProbInt(0.3, 34);
		loadingWeight.addProbInt(0.6, 38);
		loadingWeight.addProbInt(1.0, 41);

		loadingTime = new Randomizer();
		loadingTime.addProbInt(0.3, 60);
		loadingTime.addProbInt(0.8, 120);
		loadingTime.addProbInt(1.0, 180);

		unloadingTime = new Randomizer();
		unloadingTime.addProbInt(0.3, 30);
		unloadingTime.addProbInt(0.8, 60);
		unloadingTime.addProbInt(1.0, 110);

		drivingToWeighingStation = new Randomizer();
		drivingToWeighingStation.addProbInt(0.5, 30);
		drivingToWeighingStation.addProbInt(0.78, 45);
		drivingToWeighingStation.addProbInt(1.0, 60);

		SimulationObjects.getInstance().add(this);
	}

	@Override
	public boolean simulate(int timeStep)
	{
		if (truckCurrentlyLoaded == null && truckCurrentlyUnloaded == null)
		{
			// unload
			Map<String, Object> filter = new HashMap<>();
			filter.put("load", (Filter) value -> value != null ? ((Number) value).intValue() > 0 : false);
			truckCurrentlyUnloaded = roadToLoadingDocks.getNext(filter);

			if (truckCurrentlyUnloaded != null)
			{
				eventQueue.add(new Event(timeStep + truckCurrentlyUnloaded.addUtilization(unloadingTime.nextInt()),
						GravelLoadingEventTypes.UnloadingDone, truckCurrentlyUnloaded, null, this));
				utilStart(timeStep);
				return true;
			}

			// or load
			if (GravelShipping.getGravelToShip() > 0)
			{
				filter = new HashMap<>();
				filter.put("load", 0); // filter.put("load", (Filter) value -> (value != null ? ((Number) value).intValue() == 0 : true));
				truckCurrentlyLoaded = roadToLoadingDocks.getNext(filter);

				if (truckCurrentlyLoaded != null)
				{
					roadToLoadingDocks.remove(truckCurrentlyLoaded);

					loadStart(timeStep);

					utilStart(timeStep);
					return true;
				}
			}
		}
		else
		{
			if (truckCurrentlyUnloaded != null)
			{
				Event event = eventQueue.getNextEvent(timeStep, true, GravelLoadingEventTypes.UnloadingDone, null,
						this);
				if (event != null && event.getObjectAttached() != null
						&& event.getObjectAttached().getClass() == Truck.class)
				{
					eventQueue.remove(event);

					GravelShipping.setGravelToShip(GravelShipping.getGravelToShip() + truckCurrentlyUnloaded.getLoad());
					truckCurrentlyUnloaded.load(0);

					truckCurrentlyLoaded = truckCurrentlyUnloaded;
					truckCurrentlyUnloaded = null;

					loadStart(timeStep);

					return true;
				}
			}
			else
			{ // truckCurrentlyLoaded != null

				Event event = eventQueue.getNextEvent(timeStep, true, GravelLoadingEventTypes.LoadingDone, null, this);
				if (event != null && event.getObjectAttached() != null
						&& event.getObjectAttached().getClass() == Truck.class)
				{
					eventQueue.remove(event);

					roadToWeighingStations.addWithDelay(truckCurrentlyLoaded, drivingToWeighingStation.nextInt(),
							timeStep);
					truckCurrentlyLoaded = null;

					utilStop(timeStep);
					return true;
				}
			}
		}

		return false;
	}

	private void loadStart(int timeStep)
	{
		truckCurrentlyLoaded.load(Math.min(loadingWeight.nextInt(), GravelShipping.getGravelToShip()));
		GravelShipping.setGravelToShip(GravelShipping.getGravelToShip() - truckCurrentlyUnloaded.getLoad());
		eventQueue.add(new Event(timeStep + truckCurrentlyLoaded.addUtilization(loadingTime.nextInt()),
				GravelLoadingEventTypes.LoadingDone, truckCurrentlyLoaded, null, this));
	}

	@Override
	public String toString()
	{
		String toString = "Loading Dock:" + name;
		if (truckCurrentlyLoaded != null)
			toString += " " + "loading: " + truckCurrentlyLoaded;
		else if (truckCurrentlyUnloaded != null)
			toString += " " + "unloading: " + truckCurrentlyUnloaded;
		return toString;
	}

}
