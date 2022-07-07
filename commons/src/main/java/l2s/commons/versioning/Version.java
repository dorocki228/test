package l2s.commons.versioning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

public class Version
{
	private static final Logger _log;
	private String _revisionNumber;
	private String _versionNumber;
	private String _buildDate;
	private String _buildJdk;
	private String _builderName;

	public Version(Class<?> c)
	{
		_revisionNumber = "exported";
		_versionNumber = "-1";
		_buildDate = "";
		_buildJdk = "";
		_builderName = "";
		File jarName = null;
		try
		{
			jarName = Locator.getClassSource(c);
			JarFile jarFile = null;
			try
			{
				jarFile = new JarFile(jarName);
			}
			catch(FileNotFoundException e)
			{
				_log.info("Unable to get soft information.");
			}

			if(jarFile == null)
			{
				_buildJdk = System.getProperty("java.version");
				_buildDate = ZonedDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT));
				_revisionNumber = "n/a";
			}
			else
			{
				Attributes attrs = jarFile.getManifest().getMainAttributes();

				setBuildJdk(attrs);
				setBuildDate(attrs);
				setRevisionNumber(attrs);
				setVersionNumber(attrs);
				setBuilderName(attrs);
			}
		}
		catch(IOException e)
		{
			_log.error("Unable to get soft information\nFile name '" + jarName.getAbsolutePath() + "' isn't a valid jar", e);
		}
	}

	private void setBuilderName(Attributes attrs)
	{
		String builderName = attrs.getValue("Builder-Name");
		if(builderName != null)
			_builderName = builderName;
		else
		{
			builderName = attrs.getValue("Created-By");
			if(builderName != null)
				_builderName = builderName;
		}
	}

	private void setVersionNumber(Attributes attrs)
	{
		String versionNumber = attrs.getValue("Implementation-Version");
		if(versionNumber != null)
			_versionNumber = versionNumber;
		else
			_versionNumber = "-1";
	}

	private void setRevisionNumber(Attributes attrs)
	{
		String revisionNumber = attrs.getValue("Implementation-Build");
		if(revisionNumber != null)
			_revisionNumber = revisionNumber;
		else
			_revisionNumber = "-1";
	}

	private void setBuildJdk(Attributes attrs)
	{
		String buildJdk = attrs.getValue("Build-Jdk");
		if(buildJdk != null)
			_buildJdk = buildJdk;
		else
		{
			buildJdk = attrs.getValue("Created-By");
			if(buildJdk != null)
				_buildJdk = buildJdk;
			else
				_buildJdk = "-1";
		}
	}

	private void setBuildDate(Attributes attrs)
	{
		String buildDate = attrs.getValue("Build-Date");
		if(buildDate != null)
			_buildDate = buildDate;
		else
			_buildDate = "-1";
	}

	public String getRevisionNumber()
	{
		return _revisionNumber;
	}

	public String getVersionNumber()
	{
		return _versionNumber;
	}

	public String getBuildDate()
	{
		return _buildDate;
	}

	public String getBuildJdk()
	{
		return _buildJdk;
	}

	public String getBuilderName()
	{
		return _builderName;
	}

	static
	{
		_log = LoggerFactory.getLogger(Version.class);
	}
}
