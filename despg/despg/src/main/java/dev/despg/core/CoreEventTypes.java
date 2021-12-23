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

public enum CoreEventTypes implements UniqueEventDescription
{
	/**
	 * The delay literal.
	 */
	Delay("Delay");

	private String uniqueEventDescription;

	CoreEventTypes(String uniqueEventDescription)
	{
		this.uniqueEventDescription = uniqueEventDescription;
	}

	@Override
	public String get()
	{
		return uniqueEventDescription;
	}
}
