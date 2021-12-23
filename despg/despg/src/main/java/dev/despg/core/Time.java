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

public final class Time
{
	private static final int MINUTES_PER_HOUR = 60;
	private static final int HOURS_PER_DAY = 24;				//Festlegung von statischen Parametern
	private static final int MINUTES_PER_DAY = HOURS_PER_DAY * MINUTES_PER_HOUR;

	private Time()	//Konstruktor
	{

	}

	/**
	 * This method takes an int number of minutes and converts it into a String of
	 * days:hours:minutes.
	 *
	 * @param minutes The value of minutes that will be converted
	 * @return minutes converted to days:hours:minutes
	 */
	public static String stepsToString(int minutes) throws SimulationException
	{											//ANzahl an Minuten (als Integer) wird in String Formal tage-stunden-minuten umgewandelt
		if (minutes < 0)
			throw new SimulationException("Parameter can't be negative");		//Fehler bei Int<0
		StringBuilder result = new StringBuilder();
		int days = minutes / MINUTES_PER_DAY;		//Tage ermitteln
		if (days > 0)
		{
			result.append(days + "d");
			minutes -= days * MINUTES_PER_DAY;		//Ausgabe formatieren und Abzug der Tage von Gesamtminuten
		}

		int hours = minutes / MINUTES_PER_HOUR;		//Stunden ermitteln
		if (hours > 0)
		{
			if (result.length() > 0)			//Falls es Tage gibt wird : zwischen Tage und Stunden eingefügt
				result.append(":");

			result.append(hours + "h");
			minutes -= hours * MINUTES_PER_HOUR;	//Ausgabe formatieren und Abzug der Stunden von Minuten
		}

		if (result.length() == 0 || minutes > 0)		//Nur noch ausgeführt wenn es noch Minuten gibt
		{
			if (result.length() > 0)
				result.append(":");				//Wieder : zwischen Stunden und Minuten

			result.append(minutes + "m");		//Ausgabe formatieren
		}

		return result.toString();		//Gesamtausgabe
	}
}
