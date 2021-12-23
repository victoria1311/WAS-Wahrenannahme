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

import dev.despg.core.Event;
import dev.despg.core.EventQueue;
import dev.despg.core.Queue;
import dev.despg.core.Randomizer;
import dev.despg.core.SimulationObject;
import dev.despg.core.SimulationObjects;
import dev.despg.examples.gravelshipping.GravelShipping;

public class WeighingStation extends SimulationObject
{
	private static final int TIME_TO_WEIGH_TRUCK = 10;
	private static final int MAXLOAD = 40;

	private String name;

	private Queue<Truck> roadToWeighingStations;
	private Queue<Truck> roadToLoadingDocks;
	private Truck truckInWeighingStation;

	private static Randomizer drivingToUnloadDock;
	private static Randomizer drivingToLoadingDock;
	private static EventQueue eventQueue;

	public WeighingStation(String name, Queue roadToWeighingStations, Queue roadToLoadingDocks)
	{
		eventQueue = EventQueue.getInstance();

		this.name = name;
		this.roadToLoadingDocks = roadToLoadingDocks;
		this.roadToWeighingStations = roadToWeighingStations;

		drivingToLoadingDock = new Randomizer();
		drivingToLoadingDock.addProbInt(0.5, 30);
		drivingToLoadingDock.addProbInt(1.0, 45);

		SimulationObjects.getInstance().add(this);
	}

	@Override
	public boolean simulate(int timeStep)
	{
		if (truckInWeighingStation == null)
		{
			truckInWeighingStation = roadToWeighingStations.getNext();
			if (truckInWeighingStation != null)
			{
				roadToWeighingStations.remove(truckInWeighingStation);
				eventQueue.add(new Event(timeStep + TIME_TO_WEIGH_TRUCK, GravelLoadingEventTypes.WeighingDone,
						truckInWeighingStation, null, this));

				utilStart(timeStep);
				return true;
			}
		}
		else
		{
			Event event = eventQueue.getNextEvent(timeStep, true, GravelLoadingEventTypes.WeighingDone, null, this);
			if (event != null && event.getObjectAttached() != null
					&& event.getObjectAttached().getClass() == Truck.class)
			{
				eventQueue.remove(event);

				final Integer truckToWeighLoad = truckInWeighingStation.getLoad();
				int driveToLoadingStation;

				if (truckToWeighLoad != null && truckToWeighLoad > MAXLOAD)
				{
					GravelShipping.setGravelToShip(GravelShipping.getGravelToShip() + truckToWeighLoad);
					GravelShipping.setUnsuccessfulLoadingSizes(GravelShipping.getUnsuccessfulLoadingSizes() + truckToWeighLoad);
					GravelShipping.setUnsuccessfulLoadings(GravelShipping.getUnsuccessfulLoadings() + 1);
				}
				else
				{
					GravelShipping.setGravelShipped(GravelShipping.getGravelShipped() + truckToWeighLoad);
					GravelShipping.setSuccessfulLoadingSizes(GravelShipping.getSuccessfulLoadingSizes() + truckToWeighLoad);
					GravelShipping.setSuccessfulLoadings(GravelShipping.getSuccessfulLoadings() + 1);
				}

				roadToLoadingDocks.addWithDelay(truckInWeighingStation, drivingToLoadingDock.nextInt(), timeStep);
				truckInWeighingStation.unload();
				truckInWeighingStation = null;

				utilStop(timeStep);
				return true;
			}
		}

		return false;
	}

	@Override
	public String toString()
	{
		String toString = "Weighing Station:" + name;
		if (truckInWeighingStation != null)
			toString += " " + "loading: " + truckInWeighingStation;
		return toString;
	}

}
