package l2s.gameserver.cache;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.formats.dds.DDSConverter;
import l2s.gameserver.Config;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

public class ImagesCache
{
	private static final Logger _log;
	private static final int[] SIZES;
	private static final int MAX_SIZE;
	public static final Pattern HTML_PATTERN;
	private static final ImagesCache _instance;
	private final Map<String, Integer> _imagesId;
	private final TIntObjectMap<byte[]> _images;
	private final ReentrantReadWriteLock lock;
	private final Lock readLock;
	private final Lock writeLock;

	public static final ImagesCache getInstance()
	{
		return _instance;
	}

	private ImagesCache()
	{
		_imagesId = new HashMap<>();
		_images = new TIntObjectHashMap();
		lock = new ReentrantReadWriteLock();
		readLock = lock.readLock();
		writeLock = lock.writeLock();
		load();
	}

	public void load()
	{
		_log.info("ImagesCache: Loading images...");
		File dir = new File(Config.DATAPACK_ROOT, "images");
		if(!dir.exists() || !dir.isDirectory())
		{
			_log.info("ImagesCache: Files missing, loading aborted.");
			return;
		}
		int count = loadImagesDir(dir);
		_log.info("ImagesCache: Loaded " + count + " images");
	}

	private int loadImagesDir(File dir)
	{
		int count = 0;
		for(File file : dir.listFiles())
			if(file.isDirectory())
				count += loadImagesDir(file);
			else if(checkImageFormat(file))
			{
				String fileName = file.getName();
				if(_imagesId.containsKey(fileName.toLowerCase()))
					_log.warn("Duplicate image name \"" + fileName + "\". Replacing with " + file.getPath());
				else
				{
					BufferedImage image = resizeImage(file);
					if(image != null)
					{
						try
						{
							ByteBuffer buffer = DDSConverter.convertToDxt1NoTransparency(image);
							byte[] array = buffer.array();
							int imageId = Math.abs(new HashCodeBuilder(15, 87).append(fileName).append(array).toHashCode());
							_imagesId.put(fileName.toLowerCase(), imageId);
							_images.put(imageId, array);
						}
						catch(Exception e)
						{
							_log.error("ImagesChache: Error while loading " + fileName + " (" + image.getWidth() + "x" + image.getHeight() + ") image.", e);
						}
						++count;
					}
				}
			}
		return count;
	}

	private static BufferedImage resizeImage(File file)
	{
		BufferedImage image;
		try
		{
			image = ImageIO.read(file);
		}
		catch(IOException ioe)
		{
			_log.error("ImagesCache: Error while resizing " + file.getName() + " image.");
			return null;
		}
		if(image == null)
			return null;
		int width = image.getWidth();
		int height = image.getHeight();
		boolean resizeWidth = true;
		if(width > MAX_SIZE)
		{
			image = image.getSubimage(0, 0, MAX_SIZE, height);
			resizeWidth = false;
		}
		boolean resizeHeight = true;
		if(height > MAX_SIZE)
		{
			image = image.getSubimage(0, 0, width, MAX_SIZE);
			resizeHeight = false;
		}
		int resizedWidth = width;
		if(resizeWidth)
			for(int size : SIZES)
				if(size >= width)
				{
					resizedWidth = size;
					break;
				}
		int resizedHeight = height;
		if(resizeHeight)
			for(int size2 : SIZES)
				if(size2 >= height)
				{
					resizedHeight = size2;
					break;
				}
		if(resizedWidth != width || resizedHeight != height)
		{
			BufferedImage resizedImage = new BufferedImage(resizedWidth, resizedHeight, image.getType());
			Graphics2D g = resizedImage.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(image, 0, 0, resizedWidth, resizedHeight, 0, 0, image.getWidth(), image.getHeight(), null);
			g.dispose();
			return resizedImage;
		}
		return image;
	}

	public int getImageId(String val)
	{
        readLock.lock();
        int imageId = 0;
        try
		{
			if(_imagesId.get(val.toLowerCase()) != null)
				imageId = _imagesId.get(val.toLowerCase());
		}
		finally
		{
			readLock.unlock();
		}
		return imageId;
	}

	public byte[] getImage(int imageId)
	{
        readLock.lock();
        byte[] image = null;
        try
		{
			image = _images.get(imageId);
		}
		finally
		{
			readLock.unlock();
		}
		return image;
	}

	private static boolean checkImageFormat(File file)
	{
		String filename = file.getName();
		return filename.endsWith(".jpg") || filename.endsWith(".png") || filename.endsWith(".bmp");
	}

	static
	{
		_log = LoggerFactory.getLogger(ImagesCache.class);
		SIZES = new int[] { 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024 };
		MAX_SIZE = SIZES[SIZES.length - 1];
		HTML_PATTERN = Pattern.compile("%image:(.*?)%", 32);
		_instance = new ImagesCache();
	}
}
