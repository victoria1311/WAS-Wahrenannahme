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

import dev.despg.core.Event;
import dev.despg.core.EventQueue;
import dev.despg.core.Randomizer;
import dev.despg.core.SimulationObject;
import dev.despg.core.SimulationObjects;

public class WeighingStation extends SimulationObject	// DIe Klasse Wiegestation erstellt/beinhalet folgende Attribute(erbt ebenfalls von SimulationObjekt Attribute/Methoden)
{
	private static final int TIME_TO_WEIGH_TRUCK = 10;  //Zeit, die zum wiegen eines Trucks benötigt wird, wird gesetzt
	private static final int MAXLOAD = 40;				//Maximales Ladevolumen/Gewicht eines Trucks wird gesetzt. Ist ein Truck schwerer wird er nicht gewogen.

	private String name;								//Attribut für den Namen der Wiegestation
	private Truck truckInWeighingStation;				//Objekt vom Typ Truck erstellt

	private static Randomizer drivingToUnloadDock;      //Es wird ein neues Objekt vom Typ Randomizer erstellt (min0,0/max1.0)(Zum entladedock)
	private static Randomizer drivingToLoadingDock;		//Es wird ein neues Objekt vom Typ Randomizer erstellt (min0,0/max1.0)(Zum ladedock)
	private static EventQueue eventQueue;				//Es wird eine neue Eventqueue für die Wiegestation erstellt

	/**
	 * Constructor for new WeightingStations, injects its dependency to
	 * SimulationObjects and creates the required randomizer instances.
	 *
	 * @param name Name of the WeightingStation instance
	 */
	public WeighingStation(String name)					//Konstruktor der Klasse Wiegestation mit Namen als Übergabeparameter
	{
		this.name = name;								//Übergabeparameter Name wird auf Name der Wiegestation gesetzt

		eventQueue = EventQueue.getInstance();			//Holt sich die aktuelle EventQueue und speichert diese in der variable EventQueue	

		drivingToLoadingDock = new Randomizer();		//Für die oben erstellte variable drivingtoloaddock wird ein neuer randomizer erstellt
		drivingToLoadingDock.addProbInt(0.5, 120);		//Dem randomizer werden die Wahrscheinlichkeiten zugeordnet
		drivingToLoadingDock.addProbInt(0.8, 150);		//50% der Fälle 120ZE, 30% der Fälle 150ZE,20% der Fälle 180ZE
		drivingToLoadingDock.addProbInt(1.0, 180);

		SimulationObjects.getInstance().add(this);		//Aktuelle Liste mit den Simulationsobjekten wird geholt und das Grade erstellte
	}													//Objekt vom Typ Wiegestation wird zu der Liste der Simulationsobjekte hinzugefügt

	@Override
	public String toString()											//Überschreibt die Methode toString und setzt, wie die Ausgabe aussehen soll
	{
		String toString = "Weighing Station:" + name;					//Wiegestation+NAme der Wiegestation wird in toSTring gespeichert
		if (truckInWeighingStation != null)								//Falls ein Truck in der Wiegestation ist wird an toString angehägt
			toString += " " + "loading: " + truckInWeighingStation;		//loading+Truck der gerade beladen wird
		return toString;				//AUsgabe: Wiegestation X belädt Truck Y
	}

	/**
	 * Gets called every timeStep
	 *
	 * Checks events from the event queue that either are assigned to this class or
	 * to an object of this class. If it is assigned to this class, the object of
	 * which the simulate function got called, checks if it is currently occupied
	 * and if the attached object is indeed a truck. In that case, the event gets
	 * removed from the queue, gets executed and a new event gets added to the queue
	 * which gets triggered when the weighting is done.
	 *
	 * A "weighting is done" event gets pulled from the queue if the receiving
	 * object is the object on which the simulate function got called on. In that
	 * case the event gets removed from the queue and handled by checking if trucks
	 * load is above the maximum allowed load or not. If it is above, it will count
	 * as an unsuccessful loading, else it will count ass successful and be shipped.
	 * In either case there will be a new event added to the event queue with no
	 * difference in parameters.
	 *
	 * @return true if an assignable event got found and handled, false if no event
	 *         could get assigned
	 */
	@Override
	public boolean simulate(int timeStep)
	{
		Event event = eventQueue.getNextEvent(timeStep, true, GravelLoadingEventTypes.Weighing, this.getClass(), null); //In der Variable event wird das nächste Event der Eventqueue gespeichert
		if (truckInWeighingStation == null && event != null && event.getObjectAttached() != null		//Falls kein Truck gerade in der Wiegestation ist und die Variable event nicht null ist und das am evet hängende Objekt nicht null und ein Truck ist
				&& event.getObjectAttached().getClass() == Truck.class)
		{
			eventQueue.remove(event);		//Entferne event aus der Eventqueue
			truckInWeighingStation = (Truck) event.getObjectAttached();		//In die Variable wird das am Even hängende Objekt vom Typ Truck gespeichert
			eventQueue.add(new Event(timeStep + truckInWeighingStation.addUtilization(TIME_TO_WEIGH_TRUCK), //Der Eventqueue wird ein neues Event mit Zeitstempel,(aktuelle Zeit+ Beladezeit), einer Beschreibung und dem Objekt Truck angehängt
					GravelLoadingEventTypes.WeighingDone, truckInWeighingStation, null, this));
			utilStart(timeStep);	//Start des Timestamp
			return true;	//Gibt true zurück wenn das event erfolgreich hinzugefügt wurde (Erklärung: Der Truck stand vor der Wiegestation bzw. das Event was gelöscht wurde bedeutete das der Truck bereit zum wiegen ist. Das event was neu erstellt wird sagt der wiegestation das der truck fertig gewogen ist. AM anschluss daran wird im folgenden das gewogene gewicht analaysiert)
		}

		event = eventQueue.getNextEvent(timeStep, true, GravelLoadingEventTypes.WeighingDone, null, this); //nächstes event mit Beschreibung "Wiegen erfogreich/fertig" wird in der varable event gespeichert
		if (event != null && event.getObjectAttached() != null && event.getObjectAttached().getClass() == Truck.class) //Wenn event nicht null und am event ein objekt hängt das vom Typ Truck ist
		{
			eventQueue.remove(event); //Event löschen aus der queue
			final Integer truckToWeighLoad = truckInWeighingStation.getLoad(); //In die variable wird das Gewicht des Trucks gespeichert
			int driveToLoadingStation; //Variablendeklaration

			if (truckToWeighLoad != null && truckToWeighLoad > MAXLOAD) //Wenn Truck nicht leer und das Ladegewicht größer als das MAX Gewicht ist
			{
				GravelShipping.setGravelToShip(GravelShipping.getGravelToShip() + truckToWeighLoad); //Graveltohip wird um Gewicht des Trucks erhöht, da Truck zu schwer und Ladung nicht verschifft wird sondern zum "Pool" zurück geht
				GravelShipping.setUnsuccessfulLoadingSizes(GravelShipping.getUnsuccessfulLoadingSizes() + truckToWeighLoad); //Zähler für unzulässiges Gewicht der Ladungen wird erhöht
				GravelShipping.setUnsuccessfulLoadings(GravelShipping.getUnsuccessfulLoadings() + 1); //Zähler für unzulässige Ladungen wird erhöht
				driveToLoadingStation = truckInWeighingStation.addUtilization(drivingToLoadingDock.nextInt()); //Integer Wert für das Ladedock zu dem der Truck als nächstes fährt wird zugewiesen
			}
			else //Falls Truck nicht leer und Ladegewicht kleiner als MAX Ladegewicht ist
			{
				GravelShipping.setGravelShipped(GravelShipping.getGravelShipped() + truckToWeighLoad); //Verschiffte Menge wird um Lademenge erhöht
				GravelShipping.setSuccessfulLoadingSizes(GravelShipping.getSuccessfulLoadingSizes() + truckToWeighLoad); //Zähler für erfolgreich verschifftes Gewicht wird erhöht
				GravelShipping.setSuccessfulLoadings(GravelShipping.getSuccessfulLoadings() + 1);//Zähler für erfolgreiche Ladungen wird erhöht
				driveToLoadingStation = truckInWeighingStation.addUtilization(drivingToLoadingDock.nextInt());//Integer Wert für das Ladedock zu dem der Truck als nächstes fährt wird zugewiesen
			}
			eventQueue.add(new Event(timeStep + driveToLoadingStation, GravelLoadingEventTypes.Loading,
					truckInWeighingStation, LoadingDock.class, null)); // Der eventqueue wird ein neues event zum Befüllen bzw. beladen des Trucks erstellt(Das Event wird dann von der LAdestation abgearbeitet, da die Beschreibung Loading)

			truckInWeighingStation.unload(); //Truck in der WIegestation lädt Fracht ab
			truckInWeighingStation = null;//Truck in Wiegestation wird auf null gesetzt (Truck verlässt die Wiegestation)
			utilStop(timeStep);//Timestamp wird gestoppt
			return true; //Rückgabe true, Wiegestation hat sich erfolgreich simuliert
		}

		return false; //Andernfalls Rückgabe falls, Wiegestation konnte ich nicht simulieren oder es gibt keine zu simulierenden events
	}
}
