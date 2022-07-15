package org.strixplatform.network;

import org.strixplatform.utils.StrixClientData;

public abstract interface IStrixClientData
{
	public abstract void setStrixClientData(final StrixClientData clientData);

	public abstract StrixClientData getStrixClientData();
}