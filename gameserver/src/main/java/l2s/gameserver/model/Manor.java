package l2s.gameserver.model;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.manor.CropProcure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

public class Manor
{
	private static final Logger _log;
	private static Manor _instance;
	private static Map<Integer, SeedData> _seeds;

	public Manor()
	{
		_seeds = new ConcurrentHashMap<>();
		parseData();
	}

	public static Manor getInstance()
	{
		if(_instance == null)
			_instance = new Manor();
		return _instance;
	}

	public List<Integer> getAllCrops()
	{
		List<Integer> crops = new ArrayList<>();
		for(SeedData seed : _seeds.values())
			if(!crops.contains(seed.getCrop()) && seed.getCrop() != 0 && !crops.contains(seed.getCrop()))
				crops.add(seed.getCrop());
		return crops;
	}

	public Map<Integer, SeedData> getAllSeeds()
	{
		return _seeds;
	}

	public int getSeedBasicPrice(int seedId)
	{
		ItemTemplate seedItem = ItemHolder.getInstance().getTemplate(seedId);
		if(seedItem != null)
			return seedItem.getReferencePrice();
		return 0;
	}

	public int getSeedBasicPriceByCrop(int cropId)
	{
		for(SeedData seed : _seeds.values())
			if(seed.getCrop() == cropId)
				return getSeedBasicPrice(seed.getId());
		return 0;
	}

	public int getCropBasicPrice(int cropId)
	{
		ItemTemplate cropItem = ItemHolder.getInstance().getTemplate(cropId);
		if(cropItem != null)
			return cropItem.getReferencePrice();
		return 0;
	}

	public int getMatureCrop(int cropId)
	{
		for(SeedData seed : _seeds.values())
			if(seed.getCrop() == cropId)
				return seed.getMature();
		return 0;
	}

	public long getSeedBuyPrice(int seedId)
	{
		long buyPrice = getSeedBasicPrice(seedId) / 10;
		return buyPrice >= 0L ? buyPrice : 1L;
	}

	public int getSeedMinLevel(int seedId)
	{
		SeedData seed = _seeds.get(seedId);
		if(seed != null)
			return seed.getLevel() - 5;
		return -1;
	}

	public int getSeedMaxLevel(int seedId)
	{
		SeedData seed = _seeds.get(seedId);
		if(seed != null)
			return seed.getLevel() + 5;
		return -1;
	}

	public int getSeedLevelByCrop(int cropId)
	{
		for(SeedData seed : _seeds.values())
			if(seed.getCrop() == cropId)
				return seed.getLevel();
		return 0;
	}

	public int getSeedLevel(int seedId)
	{
		SeedData seed = _seeds.get(seedId);
		if(seed != null)
			return seed.getLevel();
		return -1;
	}

	public boolean isAlternative(int seedId)
	{
		for(SeedData seed : _seeds.values())
			if(seed.getId() == seedId)
				return seed.isAlternative();
		return false;
	}

	public int getCropType(int seedId)
	{
		SeedData seed = _seeds.get(seedId);
		if(seed != null)
			return seed.getCrop();
		return -1;
	}

	public synchronized int getRewardItem(int cropId, int type)
	{
		for(SeedData seed : _seeds.values())
			if(seed.getCrop() == cropId)
				return seed.getReward(type);
		return -1;
	}

	public synchronized long getRewardAmountPerCrop(int castle, int cropId, int type)
	{
		CropProcure cs = ResidenceHolder.getInstance().getResidence(Castle.class, castle).getCropProcure(0).get(cropId);
		for(SeedData seed : _seeds.values())
			if(seed.getCrop() == cropId)
				return cs.getPrice() / getCropBasicPrice(seed.getReward(type));
		return -1L;
	}

	public synchronized int getRewardItemBySeed(int seedId, int type)
	{
		SeedData seed = _seeds.get(seedId);
		if(seed != null)
			return seed.getReward(type);
		return 0;
	}

	public List<Integer> getCropsForCastle(int castleId)
	{
		List<Integer> crops = new ArrayList<>();
		for(SeedData seed : _seeds.values())
			if(seed.getManorId() == castleId && !crops.contains(seed.getCrop()))
				crops.add(seed.getCrop());
		return crops;
	}

	public List<Integer> getSeedsForCastle(int castleId)
	{
		List<Integer> seedsID = new ArrayList<>();
		for(SeedData seed : _seeds.values())
			if(seed.getManorId() == castleId && !seedsID.contains(seed.getId()))
				seedsID.add(seed.getId());
		return seedsID;
	}

	public int getCastleIdForSeed(int seedId)
	{
		SeedData seed = _seeds.get(seedId);
		if(seed != null)
			return seed.getManorId();
		return 0;
	}

	public long getSeedSaleLimit(int seedId)
	{
		SeedData seed = _seeds.get(seedId);
		if(seed != null)
			return seed.getSeedLimit();
		return 0L;
	}

	public long getCropPuchaseLimit(int cropId)
	{
		for(SeedData seed : _seeds.values())
			if(seed.getCrop() == cropId)
				return seed.getCropLimit();
		return 0L;
	}

	private void parseData()
	{
		File seedData = new File(Config.DATAPACK_ROOT, "data/seeds.csv");
		try(LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(seedData, StandardCharsets.UTF_8))))
		{
			String line = null;
			while((line = lnr.readLine()) != null)
				if(!line.trim().isEmpty())
				{
					if(line.startsWith("#"))
						continue;
					SeedData seed = parseList(line);
					_seeds.put(seed.getId(), seed);
				}
			_log.info("ManorManager: Loaded " + _seeds.size() + " seeds");
		}
		catch(FileNotFoundException e2)
		{
			_log.info("seeds.csv is missing in data folder");
		}
		catch(Exception e)
		{
			_log.error("Error while loading seeds!", e);
		}
	}

	private SeedData parseList(String line)
	{
		StringTokenizer st = new StringTokenizer(line, ";");
		int seedId = Integer.parseInt(st.nextToken());
		int level = Integer.parseInt(st.nextToken());
		int cropId = Integer.parseInt(st.nextToken());
		int matureId = Integer.parseInt(st.nextToken());
		int type1R = Integer.parseInt(st.nextToken());
		int type2R = Integer.parseInt(st.nextToken());
		int manorId = Integer.parseInt(st.nextToken());
		int isAlt = Integer.parseInt(st.nextToken());
		long limitSeeds = Math.round(Integer.parseInt(st.nextToken()) * Config.RATE_MANOR);
		long limitCrops = Math.round(Integer.parseInt(st.nextToken()) * Config.RATE_MANOR);
		SeedData seed = new SeedData(level, cropId, matureId);
		seed.setData(seedId, type1R, type2R, manorId, isAlt, limitSeeds, limitCrops);
		return seed;
	}

	static
	{
		_log = LoggerFactory.getLogger(Manor.class);
	}

	public class SeedData
	{
		private int _id;
		private final int _level;
		private final int _crop;
		private final int _mature;
		private int _type1;
		private int _type2;
		private int _manorId;
		private int _isAlternative;
		private long _limitSeeds;
		private long _limitCrops;

		public SeedData(int level, int crop, int mature)
		{
			_level = level;
			_crop = crop;
			_mature = mature;
		}

		public void setData(int id, int t1, int t2, int manorId, int isAlt, long lim1, long lim2)
		{
			_id = id;
			_type1 = t1;
			_type2 = t2;
			_manorId = manorId;
			_isAlternative = isAlt;
			_limitSeeds = lim1;
			_limitCrops = lim2;
		}

		public int getManorId()
		{
			return _manorId;
		}

		public int getId()
		{
			return _id;
		}

		public int getCrop()
		{
			return _crop;
		}

		public int getMature()
		{
			return _mature;
		}

		public int getReward(int type)
		{
			return type == 1 ? _type1 : _type2;
		}

		public int getLevel()
		{
			return _level;
		}

		public boolean isAlternative()
		{
			return _isAlternative == 1;
		}

		public long getSeedLimit()
		{
			return _limitSeeds;
		}

		public long getCropLimit()
		{
			return _limitCrops;
		}
	}
}