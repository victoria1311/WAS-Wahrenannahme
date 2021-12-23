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

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Queue<T extends SimulationObject> extends SimulationObject
{
	private String name;						//Liste aus Simulationsobjekten mit Name 
	private List<T> elements;					//und Listen mit Elementen oder nachfolgenden Elementen, erbt von SimulationObject
	private List<T> delayedElements;

	private static EventQueue eventQueue = EventQueue.getInstance(); //aktuelle Instance der Eventqueue holen

	public Queue(String name)
	{
		this.name = name;								//Konstruktor zum erstellen einer neuen Queue mit Name,
		elements = new LinkedList<>();					//Listen mit Elementen oder nachfolgenden Elementen 
		delayedElements = new LinkedList<>();			// und einer Instance der SImulationsobjekte
		SimulationObjects.getInstance().add(this);
	}

	public T add(T element)				//Methode zum hinzufügen eines Elements
	{
		elements.add(element);
		return element;
	}

	public void addWithDelay(T element, int delay, int timeStep)
	{																//Methode zum hinzufügen eines Elements mit Verzögerung
		delayedElements.add(element);
		eventQueue.add(new Event(timeStep + delay, CoreEventTypes.Delay, element, this.getClass(), this));
	}

	public T getNext()         //Methode, um das nächste Element der Liste abzufragen
	{
		if (elements.isEmpty())
			return null;

		return elements.get(0);
	}

	public T getNext(Map<String, Object> filters)		//Methode, um das nächste Element abzufragen, mit Map und Filter als Übergabeparameter
	{
		if (elements.isEmpty())		//Falls Elementliste leer wird null zurückgegeben
			return null;

		T matchingElement = filters == null || filters.isEmpty() ? elements.get(0) : null;

		for (int indexQueuedElement = 0; indexQueuedElement < elements.size()
				&& matchingElement == null; indexQueuedElement++)			//Solang wie indexQueuedElemet=0 und kleiner als Größe der Elementliste
		{																	//und kein passendes Element gefunden wird index um 1 erhöht
			T queuedElement = elements.get(indexQueuedElement);	//Element aus der Elementliste mit dem aktuellen Index wird in queuedElement gespeichert
			boolean allFilterTrue = true;

			for (Entry<String, Object> filterEntry : filters.entrySet())	//Ab hier kein Plan 
			{
				String attribute = filterEntry.getKey();
				Object filter = filterEntry.getValue();
				String getter = "get"
						+ (attribute.length() > 1 ? attribute.toUpperCase().charAt(0) + attribute.substring(1)
								: attribute.toUpperCase());
				try
				{
					Method method = queuedElement.getClass().getMethod(getter);
					Object attributeValue = method.invoke(queuedElement);

					if (filter instanceof Filter)
					{
						Filter filterExpression = (Filter) filter;
						if (!filterExpression.filter(attributeValue))
							allFilterTrue = false;
					}
					else if (filter instanceof Number || filter instanceof Boolean)
					{
						if (filter instanceof Number && attributeValue == null && ((Number) filter).doubleValue() == 0)
							continue;
						else if (!attributeValue.equals(filter))
							allFilterTrue = false;
					}
					else if (attributeValue != filter)
						allFilterTrue = false;
				}
				catch (Exception e)
				{
					allFilterTrue = false;
				}
			}

			if (allFilterTrue)
				matchingElement = queuedElement;
		}

		return matchingElement;
	}

	public T remove(T element)		//Methode zum löschen eines Elements
	{
		if (elements.remove(element))
			return element;
		return null;
	}

	@Override
	public boolean simulate(int timeStep) //Methode zum simulieren der Queue
	{
		Event event = eventQueue.getNextEvent(timeStep, true, CoreEventTypes.Delay, this.getClass(), this);//Nächstes Element mit Beschreibung "Delay" 
																										//in event speichern
		if (event != null && delayedElements.remove(event.getObjectAttached())) //Wenn event nicht null ist und in der Liste der delayed Elemente
		{		//events mit angehangenen Objekten vorhanden sind
			elements.add((T) event.getObjectAttached());//Angehangenes Objekt vom Event in Element Liste hinzufügen
			eventQueue.remove(event); //Event aus der Queue entfernen
			return true; //Methode gibt true zurück 
		}

		return false;//ANdernfalls Methode fehlgeschlagen oder keine Events vorhanden
	}

	@Override
	public String toString() //Methode tostring für die Ausgabe überschrieben
	{
		return name + " elements:" + elements + " delayedElements:" + delayedElements;
	}

}
