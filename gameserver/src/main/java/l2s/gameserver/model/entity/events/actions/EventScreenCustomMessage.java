package l2s.gameserver.model.entity.events.actions;

import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventAction;

import java.util.function.Consumer;

public class EventScreenCustomMessage implements EventAction {
    private final String customMessage;
    private final String[] texts;
    private final Consumer<EventScreenCustomMessage.ScreenDto> consumer;

    public EventScreenCustomMessage(String customMessage, String[] texts, Consumer<EventScreenCustomMessage.ScreenDto> consumer) {
        this.customMessage = customMessage;
        this.texts = texts;
        this.consumer = consumer;
    }

    @Override
    public void call(Event p0) {
        consumer.accept(new EventScreenCustomMessage.ScreenDto(customMessage, texts, p0));
    }

    public static class ScreenDto {
        private final String customMessage;
        private final String[] texts;
        private final Event event;

        public ScreenDto(String customMessage, String[] texts, Event event) {
            this.customMessage = customMessage;
            this.texts = texts;
            this.event = event;
        }

        public String getCustomMessage() {
            return customMessage;
        }

        public String[] getTexts() {
            return texts;
        }

        public Event getEvent() {
            return event;
        }
    }
}

