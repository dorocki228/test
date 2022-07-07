package l2s.gameserver.component;

import l2s.gameserver.model.Creature;

public abstract class AbstractComponent<T extends Creature> {
	private final T object;

	public AbstractComponent(T object) {
		this.object = object;
	}

	public T getObject() {
		return object;
	}

	public void restore() {

	}

	public void store() {

	}

	public void logout() {

	}

	public void enterWorld() {
        
	}
}
