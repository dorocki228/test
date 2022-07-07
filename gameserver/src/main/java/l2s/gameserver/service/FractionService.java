package l2s.gameserver.service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.AdenaModifier;
import l2s.gameserver.model.base.Fraction;

/**
 * @author Java-man
 * @since 21.03.2018
 */
public final class FractionService {
	private static final FractionService INSTANCE = new FractionService();

	private final Map<Fraction, Integer> cache;

	private final Map<Fraction, AdenaModifier> fractionAdenaModifier = new HashMap<>();

	private FractionService() {
		cache = new EnumMap<>(Fraction.class);
		updatePercentages();

		ThreadPoolManager.getInstance().scheduleAtFixedDelay(() ->
		{
			updatePercentages();

			boolean needUpdate = false;
			for(Fraction fraction : Fraction.VALUES) {
				var percent = getFractionPlayersCountPercentage(fraction);
				var modifier = fractionAdenaModifier.getOrDefault(fraction, AdenaModifier.NONE);
				if(percent <= 40 && modifier != AdenaModifier.FRACTION_BONUS) {
					fractionAdenaModifier.put(fraction, AdenaModifier.FRACTION_BONUS);
					needUpdate = true;
				}
				else if(percent >= 60 && modifier != AdenaModifier.FRACTION_PENALTY) {
					fractionAdenaModifier.put(fraction, AdenaModifier.FRACTION_PENALTY);
					needUpdate = true;
				}
			}

			if(needUpdate) {
				updatePartiesAdenaModifier();
			}
		}, TimeUnit.SECONDS.toMillis(10), TimeUnit.SECONDS.toMillis(10));
	}

	private void updatePartiesAdenaModifier() {
		var parties = PartyService.getInstance().getParties();
		parties.forEach(Party::recalculatePartyData);
	}

	public int getFractionPlayersCountPercentage(Fraction fraction) {
		return cache.get(fraction);
	}

	private int getFractionPlayersCountPercentageNonCached(Fraction fraction) {

		final Map<Fraction, Long> fractionMap = GameObjectsStorage.getPlayers().stream()
			.filter(p -> !p.isInTrade() && !p.isLogoutStarted())
			.map(Player::getFraction)
			.filter(f -> f != null && f != Fraction.NONE)
			.collect(Collectors.groupingBy(f -> f, Collectors.counting()));

		int count1 = Math.toIntExact(fractionMap.getOrDefault(fraction, 0L));
		int count2 = Math.toIntExact(fractionMap.getOrDefault(fraction.revert(), 0L));
		int sum = count1 + count2;

		return sum > 0 ? Math.toIntExact(Math.round(count1 * 100d / (double) sum)) : 0;
	}

	private void updatePercentages() {
		if(GameObjectsStorage.getPlayersCount() == 0) {
			Arrays.stream(Fraction.VALUES_WITH_NONE).forEach(fraction -> cache.put(fraction, 50));
			return;
		}
		int countFire = getFractionPlayersCountPercentageNonCached(Fraction.FIRE);
		cache.clear();
		cache.put(Fraction.FIRE, countFire);
		cache.put(Fraction.WATER, 100 - countFire);
	}

	public int getPersonalFactionEfficiency(Player player) {
		if(player == null) {
			return 0;
		}
		ZonedDateTime createDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(player.getCreateTime()), ZoneId.systemDefault());
		ZonedDateTime currentDateTime = ZonedDateTime.now();
		Duration between = Duration.between(createDateTime, currentDateTime);
		long days = between.toDays();
		if(days <= 0) {
			return 0;
		}
		return (int) (player.getConfrontationComponent().getTotalPoints() / days);
	}

	public AdenaModifier getFractionAdenaModifier(Fraction fraction) {
		return fractionAdenaModifier.getOrDefault(fraction, AdenaModifier.NONE);
	}

	public static FractionService getInstance() {
		return INSTANCE;
	}
}
